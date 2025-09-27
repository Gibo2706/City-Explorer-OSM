package pmf.rma.cityexplorerosm.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pmf.rma.cityexplorerosm.data.local.dao.BadgeDao;
import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;
import pmf.rma.cityexplorerosm.data.local.entities.Badge;
import pmf.rma.cityexplorerosm.data.local.entities.User;
import pmf.rma.cityexplorerosm.data.local.entities.Visit;
import pmf.rma.cityexplorerosm.domain.model.BadgeDomain;
import pmf.rma.cityexplorerosm.domain.model.UserDomain;
import pmf.rma.cityexplorerosm.domain.model.VisitStatus;

public class GamificationRepository {

    private static final String DEFAULT_USER_ID = "local_user";
    private static final int POINTS_PER_VISIT = 10;
    private static final String BADGE_VISIT_3_ID = "quest_visit_3";

    private final UserDao userDao;
    private final BadgeDao badgeDao;
    private final VisitDao visitDao;
    private final PlaceDao placeDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    public GamificationRepository(UserDao userDao, BadgeDao badgeDao, VisitDao visitDao, PlaceDao placeDao) {
        this.userDao = userDao;
        this.badgeDao = badgeDao;
        this.visitDao = visitDao;
        this.placeDao = placeDao;
        ensureDefaultUser();
    }

    private void ensureDefaultUser() {
        io.execute(() -> {
            User u = userDao.getUserSync(DEFAULT_USER_ID);
            if (u == null) {
                userDao.insert(new User(DEFAULT_USER_ID, "Gost", 0));
            }
        });
    }

    public LiveData<UserDomain> observeUser() {
        return Transformations.map(userDao.observeUser(DEFAULT_USER_ID), u -> {
            if (u == null) return new UserDomain(DEFAULT_USER_ID, "Gost", 0);
            return new UserDomain(u.id, u.displayName, u.points);
        });
    }

    public LiveData<List<BadgeDomain>> observeBadges() {
        return Transformations.map(badgeDao.observeBadges(), list -> {
            List<BadgeDomain> out = new ArrayList<>();
            for (Badge b : list) {
                out.add(new BadgeDomain(b.id, b.title, b.description, b.unlockedAt));
            }
            return out;
        });
    }


    public LiveData<VisitStatus> observeVisitStatus(int placeId) {
        return Transformations.map(visitDao.observeByPlaceId(placeId), v -> {
            if (v == null) return VisitStatus.NOT_VISITED;
            if ("VERIFIED".equals(v.status)) return VisitStatus.VERIFIED;
            return VisitStatus.PENDING;
        });
    }

    // korisnik klikne "Označi kao posećeno"
    public void markVisited(int placeId) {
        io.execute(() -> {
            Visit v = visitDao.getByPlaceIdSync(placeId);
            if (v == null) {
                // uzmi pravila sa mesta
                pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
                String initialStatus = "PENDING";
                String proofType = "NONE";
                if (p != null && "NONE".equalsIgnoreCase(nullToEmpty(p.verificationType))) {
                    initialStatus = "VERIFIED";
                    proofType = "NONE";
                }
                visitDao.insert(new Visit(0, placeId, System.currentTimeMillis(), initialStatus, proofType, null));
                if ("VERIFIED".equals(initialStatus)) {
                    onVerifiedAward(placeId, "NONE", null);
                }
            }
        });
    }

    // QR verifikacija
    public boolean verifyWithQr(int placeId, String scannedPayload) {
        final boolean[] ok = {false};
        io.execute(() -> {
            pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
            if (p == null) return;
            String expected = nullToEmpty(p.verificationSecret);
            if ("QR".equalsIgnoreCase(nullToEmpty(p.verificationType)) && expected.equals(scannedPayload)) {
                Visit v = visitDao.getByPlaceIdSync(placeId);
                if (v == null) {
                    v = new Visit(0, placeId, System.currentTimeMillis(), "VERIFIED", "QR", scannedPayload);
                    visitDao.insert(v);
                } else {
                    v.status = "VERIFIED";
                    v.proofType = "QR";
                    v.proofValue = scannedPayload;
                    visitDao.update(v);
                }
                onVerifiedAward(placeId, "QR", scannedPayload);
                ok[0] = true;
            }
        });
        return ok[0];
    }

    // GPS verifikacija (MVP: samo provera udaljenosti)
    public boolean verifyWithGps(int placeId, double lat, double lon) {
        final boolean[] ok = {false};
        io.execute(() -> {
            pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
            if (p == null) return;
            if (!"GPS".equalsIgnoreCase(nullToEmpty(p.verificationType))) return;

            double dMeters = haversineMeters(p.latitude, p.longitude, lat, lon);
            int radius = p.verificationRadiusM != null ? p.verificationRadiusM : 75;
            if (dMeters <= radius) {
                Visit v = visitDao.getByPlaceIdSync(placeId);
                String proof = lat + "," + lon;

                if (v == null) {
                    v = new Visit(0, placeId, System.currentTimeMillis(), "VERIFIED", "GPS", proof);
                    visitDao.insert(v);
                } else {
                    v.status = "VERIFIED";
                    v.proofType = "GPS";
                    v.proofValue = proof;
                    visitDao.update(v);
                }
                onVerifiedAward(placeId, "GPS", proof);
                ok[0] = true;
            }
        });
        return ok[0];
    }

    private void onVerifiedAward(int placeId, String proofType, String proofValue) {
        // +10 poena za verifikovanu posetu
        User u = userDao.getUserSync(DEFAULT_USER_ID);
        if (u == null) {
            u = new User(DEFAULT_USER_ID, "Gost", 0);
            userDao.insert(u);
        }
        int newPoints = u.points + POINTS_PER_VISIT;
        userDao.updatePoints(DEFAULT_USER_ID, newPoints);

        // quest: 3 različita verifikovana mesta
        int uniqueCount = visitDao.countDistinctVerifiedPlacesSync();
        if (uniqueCount >= 3 && badgeDao.hasBadgeSync(BADGE_VISIT_3_ID) == 0) {
            badgeDao.insert(new Badge(
                    BADGE_VISIT_3_ID,
                    "Istraživač I",
                    "Obiđi 3 različita mesta",
                    System.currentTimeMillis()
            ));
            userDao.updatePoints(DEFAULT_USER_ID, newPoints + 50);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}

package pmf.rma.cityexplorerosm.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import pmf.rma.cityexplorerosm.auth.AuthManager;
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
import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;

@Singleton
public class GamificationRepository {

    private static final int POINTS_PER_VISIT = 10;
    private static final String BADGE_VISIT_3_ID = "VISIT_3";

    private final UserDao userDao;
    private final BadgeDao badgeDao;
    private final VisitDao visitDao;
    private final PlaceDao placeDao;
    private final AuthManager auth;
    private final FirebaseSyncManager firebaseSync;

    private final Executor io = Executors.newSingleThreadExecutor();

    @Inject
    public GamificationRepository(UserDao userDao, BadgeDao badgeDao, VisitDao visitDao,
                                  PlaceDao placeDao, AuthManager auth, FirebaseSyncManager firebaseSync) {
        this.userDao = userDao;
        this.badgeDao = badgeDao;
        this.visitDao = visitDao;
        this.placeDao = placeDao;
        this.auth = auth;
        this.firebaseSync = firebaseSync;
    }

    private String uid() { return auth.currentUserId(); }

    public void ensureUserRow() {
        io.execute(() -> {
            if (userDao.getUserSync(uid()) == null) {
                userDao.insert(new User(uid(), "Gost", 0));
            }
        });
    }

    public LiveData<UserDomain> observeUser() {
        return Transformations.map(userDao.observeUser(uid()), u -> {
            if (u == null) return new UserDomain(uid(), "Gost", 0);
            return new UserDomain(u.id, u.displayName, u.points);
        });
    }

    public LiveData<List<BadgeDomain>> observeBadges() {
        return Transformations.map(badgeDao.observeBadges(uid()), list -> {
            List<BadgeDomain> out = new ArrayList<>();
            for (Badge b : list) {
                out.add(new BadgeDomain(b.id, b.title, b.description, b.unlockedAt));
            }
            return out;
        });
    }

    public LiveData<VisitStatus> observeVisitStatus(int placeId) {
        return Transformations.map(visitDao.observeByPlaceId(uid(), placeId), v -> {
            if (v == null) return VisitStatus.NOT_VISITED;
            if ("VERIFIED".equals(v.status)) return VisitStatus.VERIFIED;
            return VisitStatus.PENDING;
        });
    }

    public void markVisited(int placeId) {
        io.execute(() -> {
            Visit v = visitDao.getByPlaceIdSync(uid(), placeId);
            if (v == null) {
                pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
                String initialStatus = "PENDING";
                String proofType = "NONE";
                if (p != null && "NONE".equalsIgnoreCase(nullToEmpty(p.verificationType))) {
                    initialStatus = "VERIFIED";
                    proofType = "NONE";
                }
                visitDao.insert(new Visit(uid(), placeId, System.currentTimeMillis(), initialStatus, proofType, null));
                if ("VERIFIED".equals(initialStatus)) {
                    onVerifiedAward();
                }
            }
        });
    }

    public void verifyWithQr(int placeId, String scannedPayload, java.util.function.Consumer<Boolean> cb) {
        io.execute(() -> {
            boolean ok = false;
            pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
            if (p != null && "QR".equalsIgnoreCase(nullToEmpty(p.verificationType))
                    && nullToEmpty(p.verificationSecret).equals(scannedPayload)) {
                Visit v = visitDao.getByPlaceIdSync(uid(), placeId);
                if (v == null) {
                    v = new Visit(uid(), placeId, System.currentTimeMillis(), "VERIFIED", "QR", scannedPayload);
                    visitDao.insert(v);
                } else {
                    v.status = "VERIFIED";
                    v.proofType = "QR";
                    v.proofValue = scannedPayload;
                    visitDao.update(v);
                }
                onVerifiedAward();
                ok = true;
            }
            cb.accept(ok);
        });
    }

    public void verifyWithGps(int placeId, double lat, double lon, java.util.function.Consumer<Boolean> cb) {
        io.execute(() -> {
            boolean ok = false;
            pmf.rma.cityexplorerosm.data.local.entities.Place p = placeDao.getPlaceByIdSync(placeId);
            if (p != null && "GPS".equalsIgnoreCase(nullToEmpty(p.verificationType))) {
                double dMeters = haversineMeters(p.latitude, p.longitude, lat, lon);
                int radius = p.verificationRadiusM != null ? p.verificationRadiusM : 75;
                if (dMeters <= radius) {
                    Visit v = visitDao.getByPlaceIdSync(uid(), placeId);
                    String proof = lat + "," + lon;
                    if (v == null) {
                        v = new Visit(uid(), placeId, System.currentTimeMillis(), "VERIFIED", "GPS", proof);
                        visitDao.insert(v);
                    } else {
                        v.status = "VERIFIED";
                        v.proofType = "GPS";
                        v.proofValue = proof;
                        visitDao.update(v);
                    }
                    onVerifiedAward();
                    ok = true;
                }
            }
            cb.accept(ok);
        });
    }

    private void onVerifiedAward() {
        User u = userDao.getUserSync(uid());
        if (u == null) { u = new User(uid(), "Gost", 0); userDao.insert(u); }
        int newPoints = u.points + POINTS_PER_VISIT;
        userDao.updatePoints(uid(), newPoints);

        int uniqueCount = visitDao.countDistinctVerifiedPlacesSync(uid());
        if (uniqueCount >= 3 && badgeDao.hasBadgeSync(uid(), BADGE_VISIT_3_ID) == 0) {
            badgeDao.insert(new Badge(uid(), BADGE_VISIT_3_ID,
                    "Istraživač I", "Obiđi 3 različita mesta", System.currentTimeMillis()));
            userDao.updatePoints(uid(), newPoints + 50);
        }
        firebaseSync.pushLocalToRemote(uid());
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

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

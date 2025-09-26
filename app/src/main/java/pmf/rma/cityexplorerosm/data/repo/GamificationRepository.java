package pmf.rma.cityexplorerosm.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pmf.rma.cityexplorerosm.data.local.dao.BadgeDao;
import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;
import pmf.rma.cityexplorerosm.data.local.entities.Badge;
import pmf.rma.cityexplorerosm.data.local.entities.User;
import pmf.rma.cityexplorerosm.data.local.entities.Visit;
import pmf.rma.cityexplorerosm.domain.model.BadgeDomain;
import pmf.rma.cityexplorerosm.domain.model.UserDomain;

public class GamificationRepository {

    private static final String DEFAULT_USER_ID = "local_user";
    private static final int POINTS_PER_VISIT = 10;
    private static final String BADGE_VISIT_3_ID = "quest_visit_3";

    private final UserDao userDao;
    private final BadgeDao badgeDao;
    private final VisitDao visitDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    public GamificationRepository(UserDao userDao, BadgeDao badgeDao, VisitDao visitDao) {
        this.userDao = userDao;
        this.badgeDao = badgeDao;
        this.visitDao = visitDao;
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

    public void addVisitAndReward(int placeId) {
        io.execute(() -> {
            // zabeleži posetu
            visitDao.insert(new Visit(0, placeId, System.currentTimeMillis()));

            // dodeli poene
            User u = userDao.getUserSync(DEFAULT_USER_ID);
            if (u == null) {
                u = new User(DEFAULT_USER_ID, "Gost", 0);
                userDao.insert(u);
            }
            int newPoints = u.points + POINTS_PER_VISIT;
            userDao.updatePoints(DEFAULT_USER_ID, newPoints);

            // proveri quest: 3 različita mesta
            int uniqueCount = visitDao.countDistinctPlacesSync();
            if (uniqueCount >= 3 && badgeDao.hasBadgeSync(BADGE_VISIT_3_ID) == 0) {
                badgeDao.insert(new Badge(
                        BADGE_VISIT_3_ID,
                        "Istraživač I",
                        "Obiđi 3 različita mesta",
                        System.currentTimeMillis()
                ));
                // bonus poeni za bedž (opciono)
                userDao.updatePoints(DEFAULT_USER_ID, newPoints + 50);
            }
        });
    }
}

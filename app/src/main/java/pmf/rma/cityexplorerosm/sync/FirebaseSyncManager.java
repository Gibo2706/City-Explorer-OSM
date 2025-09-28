package pmf.rma.cityexplorerosm.sync;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import pmf.rma.cityexplorerosm.data.local.dao.BadgeDao;
import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;
import pmf.rma.cityexplorerosm.data.local.entities.Badge;
import pmf.rma.cityexplorerosm.data.local.entities.User;
import pmf.rma.cityexplorerosm.data.local.entities.Visit;

@Singleton
public class FirebaseSyncManager {
    private static final String TAG = "FirebaseSyncManager";
    private final FirebaseFirestore db;
    private final UserDao userDao;
    private final VisitDao visitDao;
    private final BadgeDao badgeDao;

    @Inject
    public FirebaseSyncManager(FirebaseFirestore db, UserDao userDao, VisitDao visitDao, BadgeDao badgeDao) {
        this.db = db;
        this.userDao = userDao;
        this.visitDao = visitDao;
        this.badgeDao = badgeDao;
    }

    /** Push lokalne poene/bedževe/posete na Firestore. Pozvati posle registracije. */
    public void pushLocalToRemote(String uid) {
        try {
            User u = userDao.getUserSync(uid);
            Map<String, Object> userDoc = new HashMap<>();
            userDoc.put("displayName", u != null ? u.displayName : "Gost");
            userDoc.put("firstName", u != null ? u.firstName : null);
            userDoc.put("lastName",  u != null ? u.lastName : null);
            userDoc.put("username",  u != null ? u.username : null);
            userDoc.put("points",    u != null ? u.points : 0);
            userDoc.put("updatedAt", FieldValue.serverTimestamp());
            Tasks.await(db.collection("users").document(uid).set(userDoc, SetOptions.merge()));

            List<Visit> visits = visitDao.getAllSyncForUser(uid);
            WriteBatch batch = db.batch();
            for (Visit v : visits) {
                DocumentReference ref = db.collection("users").document(uid)
                        .collection("visits").document(String.valueOf(v.placeId));
                Map<String, Object> m = new HashMap<>();
                m.put("placeId", v.placeId);
                m.put("ts", v.timestamp);
                m.put("status", v.status);
                m.put("proofType", v.proofType);
                m.put("proofValue", v.proofValue);
                batch.set(ref, m, SetOptions.merge());
            }
            List<Badge> badges = badgeDao.getAllSyncForUser(uid);
            for (Badge b : badges) {
                DocumentReference ref = db.collection("users").document(uid)
                        .collection("badges").document(b.id);
                Map<String, Object> m = new HashMap<>();
                m.put("title", b.title);
                m.put("description", b.description);
                m.put("unlockedAt", b.unlockedAt);
                batch.set(ref, m, SetOptions.merge());
            }
            Tasks.await(batch.commit());
        } catch (Exception e) { Log.w(TAG, "pushLocalToRemote failed", e); }
    }

    public void pullRemoteToLocal(String uid) {
        try {
            DocumentSnapshot snap = Tasks.await(db.collection("users").document(uid).get());
            if (snap.exists()) {
                String displayName = snap.getString("displayName");
                String firstName = snap.getString("firstName");
                String lastName  = snap.getString("lastName");
                String username  = snap.getString("username");
                Number points    = snap.getLong("points");
                int pts = points == null ? 0 : points.intValue();

                User local = userDao.getUserSync(uid);
                if (local == null) local = new User(uid, displayName != null ? displayName : "Korisnik", pts, firstName, lastName, username);
                else {
                    local.displayName = displayName;
                    local.firstName   = firstName;
                    local.lastName    = lastName;
                    local.username    = username;
                    local.points      = pts;
                }
                userDao.insert(local);
            }

            // VISITS
            QuerySnapshot vs = Tasks.await(db.collection("users").document(uid).collection("visits").get());
            for (DocumentSnapshot d : vs.getDocuments()) {
                int placeId = Integer.parseInt(d.getId());
                Number ts = d.getLong("ts");
                String status = d.getString("status");
                String proofType = d.getString("proofType");
                String proofValue = d.getString("proofValue");

                Visit local = visitDao.getByPlaceIdSync(uid, placeId);
                if (local == null) {
                    visitDao.insert(new Visit(uid, placeId, ts == null ? System.currentTimeMillis() : ts.longValue(),
                            status == null ? "VERIFIED" : status, proofType, proofValue));
                } else {
                    local.timestamp = ts == null ? local.timestamp : ts.longValue();
                    if (status != null) local.status = status;
                    local.proofType = proofType;
                    local.proofValue = proofValue;
                    visitDao.update(local);
                }
            }

            // BADGES
            QuerySnapshot bs = Tasks.await(db.collection("users").document(uid).collection("badges").get());
            for (DocumentSnapshot d : bs.getDocuments()) {
                String id = d.getId();
                String title = d.getString("title");
                String description = d.getString("description");
                Number unlockedAt = d.getLong("unlockedAt");
                badgeDao.insert(new Badge(uid, id,
                        title == null ? id : title,
                        description,
                        unlockedAt == null ? System.currentTimeMillis() : unlockedAt.longValue()));
            }

        } catch (Exception e) { Log.w(TAG, "pullRemoteToLocal failed", e); }
    }
}

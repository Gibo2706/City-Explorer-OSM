package pmf.rma.cityexplorerosm.auth;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.entities.User;
import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;

@Singleton
public class UserAccountRepository {

    private final AuthManager auth;
    private final FirebaseFirestore store;
    private final UserDao userDao;
    private final FirebaseSyncManager sync;
    private final Executor dbExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public UserAccountRepository(AuthManager auth, FirebaseFirestore store, UserDao userDao, FirebaseSyncManager sync) {
        this.auth = auth;
        this.store = store;
        this.userDao = userDao;
        this.sync = sync;
    }

    public interface Callback {
        void onSuccess();

        void onError(String msg);
    }

    /**
     * Registracija + rezervacija username-a (FireStore transaction) + kreiranje profila + push lokalnih podataka.
     */
    public void registerWithProfile(String email, String password,
                                    String firstName, String lastName, String username,
                                    boolean analytics, boolean marketing,
                                    Callback cb) {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            cb.onError("Popuni email, lozinku i korisničko ime");
            return;
        }
        final String unameKey = username.trim().toLowerCase(Locale.US);

        if (!auth.isFirebaseEnabled()) {
            cb.onError("Firebase Auth nije podešen");
            return;
        }

        FirebaseAuth fa = FirebaseAuth.getInstance();
        fa.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fu = result.getUser();
                    if (fu == null) {
                        cb.onError("Nije moguće kreirati nalog");
                        return;
                    }
                    String uid = fu.getUid();

                    // rezervacija usernamea putem transakcije
                    store.runTransaction(transaction -> {
                        DocumentReference unameRef = store.collection("usernames").document(unameKey);
                        DocumentSnapshot existing = transaction.get(unameRef);
                        if (existing.exists())
                            throw new FirebaseFirestoreException("Korisničko ime zauzeto",
                                    FirebaseFirestoreException.Code.ABORTED);

                        transaction.set(unameRef, new HashMap<String, Object>() {{
                            put("uid", uid);
                            put("createdAt", FieldValue.serverTimestamp());
                        }});

                        DocumentReference userRef = store.collection("users").document(uid);
                        Map<String, Object> prof = new HashMap<>();
                        prof.put("firstName", firstName);
                        prof.put("lastName", lastName);
                        prof.put("username", unameKey);
                        prof.put("displayName", (firstName + " " + lastName).trim());
                        prof.put("points", 0);
                        prof.put("createdAt", FieldValue.serverTimestamp());
                        transaction.set(userRef, prof, SetOptions.merge());

                        return null;
                    }).addOnSuccessListener(v -> {
                        dbExecutor.execute(() -> {
                            userDao.insert(new User(uid,
                                    (firstName + " " + lastName).trim(),
                                    0,
                                    firstName,
                                    lastName,
                                    unameKey));

                            auth.setAnalyticsConsent(analytics);
                            auth.setMarketingConsent(marketing);
                            sync.pushLocalToRemote(uid);

                            cb.onSuccess();
                        });
                    }).addOnFailureListener(e -> {
                        fu.delete(); // oslobodi auth nalog ako username pao
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Greška pri rezervaciji korisničkog imena");
                    });

                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    /**
     * Posle uspešne prijave povuci profil i stanje sa Firestore-a.
     */
    public void afterLoginSync(Callback cb) {
        if (!auth.isFirebaseEnabled()) {
            cb.onError("Firebase nije podešen");
            return;
        }
        String uid = auth.currentUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                sync.pullRemoteToLocal(uid);

                new Handler(Looper.getMainLooper()).post(cb::onSuccess);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        cb.onError(e.getMessage() != null ? e.getMessage() : "Greška pri sync-u"));
            }
        });
    }

}

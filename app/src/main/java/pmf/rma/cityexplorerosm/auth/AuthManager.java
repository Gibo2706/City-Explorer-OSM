package pmf.rma.cityexplorerosm.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;

/**
 * Upravljanje prijavom, tokenima i GDPR consent-om. Rad i bez Firebase-a (guest).
 */
@Singleton
public class AuthManager {

    private static final String TAG = "AuthManager";
    private static final String PREF_FILE = "auth_secure_prefs";
    private static final String KEY_ID_TOKEN = "id_token";
    private static final String KEY_ANALYTICS = "consent_analytics";
    private static final String KEY_MARKETING = "consent_marketing";

    private final Context appCtx;
    private final SharedPreferences securePrefs;
    private final MutableLiveData<String> userIdLive = new MutableLiveData<>("local_user");
    private boolean firebaseEnabled = false;
    private FirebaseAuth auth;
    private final FirebaseSyncManager firebaseSyncManager;

    @Inject
    public AuthManager(Context appCtx, FirebaseSyncManager firebaseSyncManager) {
        this.appCtx = appCtx.getApplicationContext();
        this.firebaseSyncManager = firebaseSyncManager;
        this.securePrefs = buildSecurePrefs(this.appCtx);
        try {
            firebaseEnabled = !FirebaseApp.getApps(this.appCtx).isEmpty();
            if (firebaseEnabled) {
                auth = FirebaseAuth.getInstance();
                FirebaseUser u = auth.getCurrentUser();
                userIdLive.setValue(u != null ? u.getUid() : "local_user");
                auth.addAuthStateListener(a -> {
                    FirebaseUser cu = a.getCurrentUser();
                    userIdLive.postValue(cu != null ? cu.getUid() : "local_user");
                });
            }
        } catch (Throwable t) {
            Log.w(TAG, "Firebase not configured, running in guest mode", t);
            firebaseEnabled = false;
            userIdLive.setValue("local_user");
        }
    }

    private SharedPreferences buildSecurePrefs(Context ctx) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREF_FILE, masterKey, ctx,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback na običan SharedPreferences (i dalje radi, samo nije šifrovano)
            return ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        }
    }

    public boolean isFirebaseEnabled() {
        return firebaseEnabled;
    }

    public LiveData<String> observeUserId() {
        return userIdLive;
    }

    public String currentUserId() {
        String id = userIdLive.getValue();
        return id == null ? "local_user" : id;
    }

    public void signUpEmail(String email, String password, Callback cb) {
        if (!firebaseEnabled) {
            cb.onError("Auth nije podešen (guest mod)");
            return;
        }
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    userIdLive.postValue(auth.getCurrentUser().getUid());
                    refreshIdToken(null);
                    cb.onSuccess();
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void signInEmail(String email, String password, Callback cb) {
        if (!firebaseEnabled) {
            cb.onError("Auth nije podešen (guest mod)");
            return;
        }
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    userIdLive.postValue(auth.getCurrentUser().getUid());
                    refreshIdToken(null);
                    cb.onSuccess();
                    firebaseSyncManager.pullRemoteToLocal(auth.getCurrentUser().getUid());
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void signOut() {
        if (firebaseEnabled) auth.signOut();
        userIdLive.postValue("local_user");
        securePrefs.edit().remove(KEY_ID_TOKEN).apply();
    }

    public void refreshIdToken(@Nullable Callback cb) {
        if (!firebaseEnabled) {
            if (cb != null) cb.onError("Auth nije podešen");
            return;
        }
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) {
            if (cb != null) cb.onError("Niste prijavljeni");
            return;
        }
        u.getIdToken(true).addOnSuccessListener(res -> {
            securePrefs.edit().putString(KEY_ID_TOKEN, res.getToken()).apply();
            if (cb != null) cb.onSuccess();
        }).addOnFailureListener(e -> {
            if (cb != null) cb.onError(e.getMessage());
        });
    }

    @Nullable
    public String getSavedIdToken() {
        return securePrefs.getString(KEY_ID_TOKEN, null);
    }

    // GDPR consents
    public void setAnalyticsConsent(boolean granted) {
        securePrefs.edit().putBoolean(KEY_ANALYTICS, granted).apply();
    }

    public void setMarketingConsent(boolean granted) {
        securePrefs.edit().putBoolean(KEY_MARKETING, granted).apply();
    }

    public boolean hasAnalyticsConsent() {
        return securePrefs.getBoolean(KEY_ANALYTICS, false);
    }

    public boolean hasMarketingConsent() {
        return securePrefs.getBoolean(KEY_MARKETING, false);
    }

    public interface Callback {
        void onSuccess();

        void onError(String msg);
    }
}

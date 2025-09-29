package pmf.rma.cityexplorerosm.ui.settings;

import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.cityexplorer.R;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.auth.AuthManager;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;
import pmf.rma.cityexplorerosm.ui.base.BaseActivity;
import pmf.rma.cityexplorerosm.ui.onboarding.OnboardingPrefs;
import pmf.rma.cityexplorerosm.util.FcmTokenHelper;
import pmf.rma.cityexplorerosm.util.UiEvents;

import javax.inject.Inject;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {

    private MaterialSwitch swAnalytics, swMarketing;
    private Button btnCopyFcm, btnForceSync, btnResetOnboarding, btnClearLocal, btnExportUser;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Inject AuthManager authManager;
    @Inject FirebaseSyncManager syncManager;

    @Override
    protected int getLayoutResId() { return R.layout.activity_settings; }

    @Override
    protected void initViews() {
        setToolbarTitle(getString(R.string.action_settings));
        swAnalytics = findViewById(R.id.swAnalytics);
        swMarketing = findViewById(R.id.swMarketing);
        btnCopyFcm = findViewById(R.id.btnCopyFcm);
        btnForceSync = findViewById(R.id.btnForceSync);
        btnResetOnboarding = findViewById(R.id.btnResetOnboarding);
        btnClearLocal = findViewById(R.id.btnClearLocal);
        btnExportUser = findViewById(R.id.btnExportUser);

        swAnalytics.setChecked(authManager.hasAnalyticsConsent());
        swMarketing.setChecked(authManager.hasMarketingConsent());

        swAnalytics.setOnCheckedChangeListener((b,v)-> authManager.setAnalyticsConsent(v));
        swMarketing.setOnCheckedChangeListener((b,v)-> authManager.setMarketingConsent(v));

        btnCopyFcm.setOnClickListener(v -> FcmTokenHelper.copyTokenToClipboard(this));
        btnForceSync.setOnClickListener(v -> forceSync());
        btnResetOnboarding.setOnClickListener(v -> resetOnboarding());
        btnClearLocal.setOnClickListener(v -> clearLocalData());
        btnExportUser.setOnClickListener(v -> exportUserData());

        if (!authManager.isFirebaseEnabled() || "local_user".equals(authManager.currentUserId())) {
            btnForceSync.setEnabled(false);
        }
    }

    @Override
    protected void setupObservers() { }

    private void forceSync() {
        UiEvents.get().emit(getString(R.string.sync_starting));
        io.execute(() -> {
            try {
                String uid = authManager.currentUserId();
                if ("local_user".equals(uid)) { UiEvents.get().emit(getString(R.string.sync_guest_unavailable)); return; }
                syncManager.pushLocalToRemote(uid);
                syncManager.pullRemoteToLocal(uid);
                UiEvents.get().emit(getString(R.string.sync_done));
            } catch (Exception e) { UiEvents.get().emit(getString(R.string.sync_failed)); }
        });
    }

    private void resetOnboarding() {
        OnboardingPrefs.setDone(this, false);
        OnboardingPrefs.setLimitedLocation(this, false);
        UiEvents.get().emit(getString(R.string.onboarding_reset));
    }

    private void clearLocalData() {
        UiEvents.get().emit(getString(R.string.clearing_local));
        io.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).clearAllTables();
            UiEvents.get().emit(getString(R.string.local_cleared));
        });
    }

    private void exportUserData() {
        io.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            String uid = authManager.currentUserId();
            int places = db.placeDao().countSync();
            int visits = db.visitDao().getAllSyncForUser(uid).size();
            String json = "{\n" +
                    "  \"userId\": \"" + uid + "\",\n" +
                    "  \"analyticsConsent\": " + authManager.hasAnalyticsConsent() + ",\n" +
                    "  \"marketingConsent\": " + authManager.hasMarketingConsent() + ",\n" +
                    "  \"stats\": {\n" +
                    "    \"places\": " + places + ",\n" +
                    "    \"visits\": " + visits + "\n" +
                    "  }\n" +
                    "}";
            UiEvents.get().emit(json.length() > 120 ? json.substring(0,118) + "…" : json);
        });
    }

    @Override
    protected boolean shouldShowBackButton() { return true; }
}

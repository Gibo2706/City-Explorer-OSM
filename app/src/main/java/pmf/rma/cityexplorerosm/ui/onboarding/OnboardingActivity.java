package pmf.rma.cityexplorerosm.ui.onboarding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cityexplorer.R;

import pmf.rma.cityexplorerosm.ui.map.MapActivity;

/** Multi-step onboarding sa fallback modom (bez lokacije). */
public class OnboardingActivity extends AppCompatActivity {

    private Button btnPrimary, btnFallback, btnBack, btnNext;
    private TextView tvDesc, tvStatus, btnSkipAll;

    private int page = 0; // 0=intro,1=location,2=notifications(optional),3=badges

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                updatePageUi();
            });

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                updatePageUi();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OnboardingPrefs.isDone(this)) {
            goNext();
            return;
        }
        setContentView(R.layout.activity_onboarding);
        tvDesc = findViewById(R.id.tvDesc);
        tvStatus = findViewById(R.id.tvStatus);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnFallback = findViewById(R.id.btnFallback);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnSkipAll = findViewById(R.id.btnSkipAll);

        btnPrimary.setOnClickListener(v -> handlePrimary());
        btnFallback.setOnClickListener(v -> handleFallback());
        btnBack.setOnClickListener(v -> { if (page > 0) { page--; updatePageUi(); } });
        btnNext.setOnClickListener(v -> handleNext());
        btnSkipAll.setOnClickListener(v -> {
            // Ako skip, a nema lokacije -> aktiviraj limited mode
            if (!hasLocationPermission()) {
                OnboardingPrefs.setLimitedLocation(this, true);
            }
            finishOnboarding();
        });

        // Ako API <33 preskoči notifikacije stranicu kasnije dinamički
        updatePageUi();
    }

    private void handlePrimary() {
        switch (page) {
            case 0: // Intro -> next
                page = nextLogicalPage(page);
                updatePageUi();
                break;
            case 1: // Location permission request
                if (!hasLocationPermission()) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    page = nextLogicalPage(page);
                    updatePageUi();
                }
                break;
            case 2: // Notifications
                if (needsNotificationRuntime() && !hasNotificationPermission()) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    page = nextLogicalPage(page);
                    updatePageUi();
                }
                break;
            case 3: // Finish
                finishOnboarding();
                break;
        }
    }

    private void handleFallback() {
        if (page == 1) { // location fallback
            OnboardingPrefs.setLimitedLocation(this, true);
            page = nextLogicalPage(page);
            updatePageUi();
        }
    }

    private void handleNext() {
        if (page == 1) {
            // Ako nema lokacije i nije fallback, onemogući direktni next
            if (!hasLocationPermission() && !OnboardingPrefs.isLimitedLocation(this)) {
                return; // čekaj da user izabere
            }
        }
        if (page == 3) {
            finishOnboarding();
        } else {
            page = nextLogicalPage(page);
            updatePageUi();
        }
    }

    private int nextLogicalPage(int current) {
        int candidate = current + 1;
        if (candidate == 2 && !needsNotificationRuntime()) {
            // preskoči notifikacije ako nije potrebno
            candidate = 3;
        }
        return candidate;
    }

    private void updatePageUi() {
        btnBack.setVisibility(page == 0 ? View.GONE : View.VISIBLE);
        btnFallback.setVisibility(View.GONE);
        btnPrimary.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.GONE);

        switch (page) {
            case 0:
                setTitleAndDesc(getString(R.string.app_name), getString(R.string.onb_intro_desc));
                btnPrimary.setText(getString(R.string.onb_btn_start));
                btnNext.setText(getString(R.string.onb_btn_next));
                btnNext.setEnabled(true);
                break;
            case 1:
                setTitleAndDesc(getString(R.string.onb_location_title), getString(R.string.onb_location_desc));
                tvStatus.setVisibility(View.VISIBLE);
                boolean hasLoc = hasLocationPermission();
                boolean limited = OnboardingPrefs.isLimitedLocation(this);
                if (hasLoc) {
                    tvStatus.setText(getString(R.string.onb_status_granted));
                    tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    btnPrimary.setText(getString(R.string.onb_btn_continue));
                    btnFallback.setVisibility(View.GONE);
                    btnNext.setEnabled(true);
                } else if (limited) {
                    tvStatus.setText(getString(R.string.onb_mode_limited_active));
                    tvStatus.setTextColor(getColor(android.R.color.holo_orange_dark));
                    btnPrimary.setText(getString(R.string.onb_btn_continue));
                    btnNext.setEnabled(true);
                } else {
                    tvStatus.setText(getString(R.string.onb_status_not_granted));
                    tvStatus.setTextColor(getColor(android.R.color.darker_gray));
                    btnPrimary.setText(getString(R.string.onb_btn_location));
                    btnFallback.setVisibility(View.VISIBLE);
                    btnNext.setEnabled(false);
                }
                break;
            case 2:
                setTitleAndDesc(getString(R.string.onb_notifications_title), getString(R.string.onb_notifications_desc));
                tvStatus.setVisibility(View.VISIBLE);
                if (needsNotificationRuntime()) {
                    boolean has = hasNotificationPermission();
                    tvStatus.setText(has ? getString(R.string.onb_status_granted) : getString(R.string.onb_status_not_granted));
                    tvStatus.setTextColor(getColor(has ? android.R.color.holo_green_dark : android.R.color.darker_gray));
                    btnPrimary.setText(has ? getString(R.string.onb_btn_continue) : getString(R.string.onb_btn_notifications));
                } else {
                    tvStatus.setText(getString(R.string.onb_status_granted));
                    tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    btnPrimary.setText(getString(R.string.onb_btn_continue));
                }
                btnNext.setEnabled(true);
                break;
            case 3:
                setTitleAndDesc(getString(R.string.onb_badges_title), getString(R.string.onb_badges_desc));
                btnPrimary.setText(getString(R.string.onb_btn_finish));
                btnNext.setText(getString(R.string.onb_btn_finish));
                btnNext.setEnabled(true);
                break;
        }

        // Sync next button label for non-final pages
        if (page != 3) {
            btnNext.setText(getString(R.string.onb_btn_next));
        }
    }

    private void setTitleAndDesc(String title, String desc) {
        // Title je fiksan u layoutu, mijenjamo samo opis
        tvDesc.setText(desc);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean needsNotificationRuntime() { return Build.VERSION.SDK_INT >= 33; }

    private boolean hasNotificationPermission() {
        if (!needsNotificationRuntime()) return true;
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private void finishOnboarding() {
        OnboardingPrefs.setDone(this, true);
        goNext();
    }

    private void goNext() {
        startActivity(new Intent(this, MapActivity.class));
        finish();
    }
}

package pmf.rma.cityexplorerosm.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.example.cityexplorer.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.CustomZoomButtonsController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.auth.AuthManager;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.notifications.NotificationHelper;
import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;
import pmf.rma.cityexplorerosm.ui.base.BaseActivity;
import pmf.rma.cityexplorerosm.ui.detail.DetailFragment;
import pmf.rma.cityexplorerosm.ui.onboarding.OnboardingPrefs;
import pmf.rma.cityexplorerosm.ui.placeholder.PlaceholderFragment;
import pmf.rma.cityexplorerosm.ui.profile.ProfileActivity;
import pmf.rma.cityexplorerosm.util.FcmTokenHelper;

import javax.inject.Inject;

@AndroidEntryPoint
public class MapActivity extends BaseActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String STATE_SELECTED_PLACE = "state_selected_place";

    private MapView mapView;
    private MapViewModel viewModel;
    private View snackbarAnchor;
    private boolean limitedModeShown = false;
    private Integer selectedPlaceId = null;
    private boolean tabletMode = false;

    // Enhanced UI elements
    private RecyclerView rvPlaceList;
    private DomainPlaceAdapter listAdapter;
    private EditText etSearch;
    private TextView tvDailyProgress;
    private TextView tvSyncState;
    private TextView tvEmpty; // empty state
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Filtering state
    private final List<PlaceDomain> allPlaces = new ArrayList<>();
    private final Set<String> selectedCategories = new HashSet<>();
    private String searchQuery = "";

    @Inject FirebaseSyncManager syncManager;
    @Inject AuthManager authManager;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    showSnackbar(getString(R.string.notifications_enabled));
                }
            });

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_map;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return false; // Main activity
    }

    @Override
    protected void initViews() {
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.mapView);
        snackbarAnchor = findViewById(R.id.snackbarAnchor);
        etSearch = findViewById(R.id.etSearch);
        tvDailyProgress = findViewById(R.id.tvDailyProgress);
        tvSyncState = findViewById(R.id.tvSyncState);
        rvPlaceList = findViewById(R.id.rvPlaceList);
        tvEmpty = findViewById(R.id.tvEmpty);
        View bottomSheet = findViewById(R.id.bottomSheet);
        View detailContainer = findViewById(R.id.detailContainer);
        tabletMode = detailContainer != null;

        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        setupMapView();
        setupFilterChips();
        setupList();
        setupSearch();
        setupFabs();
        setupPermissions();
        updateSyncState();
        updateProgress();

        // Log FCM token for debugging
        FcmTokenHelper.logCurrentToken();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreDetailIfNeeded(savedInstanceState);
        if (tabletMode && (savedInstanceState == null || selectedPlaceId == null)) {
            showDetailPlaceholder();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedPlaceId != null) {
            outState.putInt(STATE_SELECTED_PLACE, selectedPlaceId);
        }
    }

    @Override
    protected void setupObservers() {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getPlaces().observe(this, places -> {
            allPlaces.clear();
            if (places != null) allPlaces.addAll(places);
            applyFiltersAndRender();
        });
        // Observe daily progress count
        viewModel.getDailyProgressCount().observe(this, count -> updateProgressChip(count == null ? 0 : count));
        // Trigger initial compute
        triggerDailyProgressCompute();
    }

    private void triggerDailyProgressCompute() {
        if (viewModel != null && authManager != null) {
            viewModel.computeDailyProgress(authManager.currentUserId());
        }
    }

    private void updateProgressChip(int count) {
        if (tvDailyProgress == null || viewModel == null) return;
        int goal = viewModel.getDailyGoal();
        tvDailyProgress.setText(count + "/" + goal);
        float ratio = goal == 0 ? 0f : Math.min(1f, (float) count / goal);
        // Simple visual emphasis via alpha scaling (could later switch to gradient background)
        tvDailyProgress.setAlpha(0.6f + 0.4f * ratio);
        tvDailyProgress.setContentDescription("Dnevni napredak: " + count + " od " + goal);
    }

    private void setupMapView() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(44.8176, 20.4569)); // Belgrade center
    }

    private void setupFilterChips() {
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipCulture = findViewById(R.id.chipCulture);
        Chip chipFood = findViewById(R.id.chipFood);
        Chip chipNightlife = findViewById(R.id.chipNightlife);

        chipAll.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                // Clear all other selections and show all
                selectedCategories.clear();
                // Uncheck others without triggering logic loops
                chipCulture.setChecked(false);
                chipFood.setChecked(false);
                chipNightlife.setChecked(false);
                applyFiltersAndRender();
            } else {
                // Prevent having no selection at all (keep 'Svi' unless at least one other is chosen)
                if (selectedCategories.isEmpty()) {
                    chipAll.setChecked(true); // revert
                }
            }
        });

        chipCulture.setOnCheckedChangeListener((btn, checked) -> onCategoryToggle(chipAll, "Kultura", checked));
        chipFood.setOnCheckedChangeListener((btn, checked) -> onCategoryToggle(chipAll, "Restoran", checked));
        chipNightlife.setOnCheckedChangeListener((btn, checked) -> onCategoryToggle(chipAll, "Noćni život", checked));
    }

    private void onCategoryToggle(Chip chipAll, String category, boolean checked) {
        if (checked) {
            selectedCategories.add(category);
            if (chipAll.isChecked()) chipAll.setChecked(false);
        } else {
            selectedCategories.remove(category);
            if (selectedCategories.isEmpty()) {
                // Auto revert to ALL state
                chipAll.setChecked(true);
            }
        }
        applyFiltersAndRender();
    }

    private void setupList() {
        listAdapter = new DomainPlaceAdapter(place -> {
            openPlaceDetails(place.getId());
            NotificationHelper.sendSimple(this, getString(R.string.opening), place.getName());
            if (!tabletMode && bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        if (tabletMode) {
            // Grid with 2 columns for better use of width
            rvPlaceList.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            rvPlaceList.setLayoutManager(new LinearLayoutManager(this));
        }
        rvPlaceList.setAdapter(listAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                applyFiltersAndRender();
            }
        });
    }

    private void setupFabs() {
        FloatingActionButton fabProfile = findViewById(R.id.fabProfile);
        fabProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupPermissions() {
        if (OnboardingPrefs.isLimitedLocation(this)) {
            showLimitedModeSnackbar();
        } else {
            checkLocationPermission();
        }
        checkNotificationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void showLimitedModeSnackbar() {
        if (!limitedModeShown) {
            Snackbar.make(snackbarAnchor, getString(R.string.limited_mode_toast),
                            Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.enable_location_action), v -> {
                        OnboardingPrefs.setLimitedLocation(this, false);
                        checkLocationPermission();
                    })
                    .show();
            limitedModeShown = true;
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_SHORT).show();
    }

    private void applyFiltersAndRender() {
        List<PlaceDomain> filtered = new ArrayList<>();
        boolean noCategoryFilter = selectedCategories.isEmpty();
        for (PlaceDomain p : allPlaces) {
            boolean catOk = noCategoryFilter || selectedCategories.stream().anyMatch(sel -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(sel));
            boolean searchOk = searchQuery.isEmpty() || (p.getName() != null && p.getName().toLowerCase().contains(searchQuery.toLowerCase()));
            if (catOk && searchOk) filtered.add(p);
        }
        renderMarkers(filtered);
        listAdapter.submit(filtered);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void renderMarkers(List<PlaceDomain> places) {
        mapView.getOverlays().clear();
        if (places == null) return;
        for (PlaceDomain place : places) {
            Marker marker = createStyledMarker(place);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private Marker createStyledMarker(PlaceDomain place) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
        marker.setTitle(place.getName());
        marker.setSubDescription(place.getDescription());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setOnMarkerClickListener((m, v) -> {
            openPlaceDetails(place.getId());
            NotificationHelper.sendSimple(this, getString(R.string.opening), place.getName());
            if (!tabletMode && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            return true;
        });
        return marker;
    }

    private void updateProgress() {
        // Recompute every time we explicitly refresh UI (kept for backward compatibility)
        triggerDailyProgressCompute();
    }

    private void updateSyncState() {
        if (tvSyncState == null) return;
        boolean online = isOnline();
        tvSyncState.setText(online ? R.string.map_sync_online : R.string.map_sync_offline);
        tvSyncState.setAlpha(online ? 1f : 0.6f);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network nw = cm.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(nw);
            return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
        } else {
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            performManualSync();
            return true;
        } else if (id == R.id.action_help) {
            showSnackbar(getString(R.string.help_hint));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performManualSync() {
        showSnackbar(getString(R.string.sync_started));
        final String uid = authManager != null ? authManager.currentUserId() : "local_user";
        if ("local_user".equals(uid) || syncManager == null || !authManager.isFirebaseEnabled()) {
            showSnackbar(getString(R.string.sync_guest_unavailable));
            return;
        }
        new Thread(() -> {
            try {
                syncManager.pushLocalToRemote(uid);
                syncManager.pullRemoteToLocal(uid);
                runOnUiThread(() -> {
                    showSnackbar(getString(R.string.sync_done));
                    updateSyncState();
                });
            } catch (Exception e) {
                runOnUiThread(() -> showSnackbar(getString(R.string.sync_error)));
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar(getString(R.string.location_enabled));
            } else {
                showSnackbar(getString(R.string.location_denied));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        updateSyncState();
        triggerDailyProgressCompute(); // refresh progress when returning
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void openPlaceDetails(int placeId) {
        if (selectedPlaceId != null && selectedPlaceId == placeId && tabletMode) return; // skip reload
        if (!tabletMode) {
            startActivity(new Intent(this, pmf.rma.cityexplorerosm.ui.detail.DetailActivity.class)
                    .putExtra("place_id", placeId));
            return;
        }
        selectedPlaceId = placeId;
        Fragment frag = DetailFragment.newInstance(placeId);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.detailContainer, frag, "detail_fragment");
        ft.setReorderingAllowed(true);
        ft.commit();
    }

    private void showDetailPlaceholder() {
        if (!tabletMode) return;
        Fragment existingDetail = getSupportFragmentManager().findFragmentByTag("detail_fragment");
        if (existingDetail != null) return; // already showing real detail
        Fragment existingPlaceholder = getSupportFragmentManager().findFragmentByTag("detail_placeholder");
        if (existingPlaceholder == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailContainer, PlaceholderFragment.newInstance(), "detail_placeholder")
                    .commit();
        }
    }

    private void restoreDetailIfNeeded(Bundle saved) {
        if (tabletMode && saved != null && saved.containsKey(STATE_SELECTED_PLACE)) {
            int pid = saved.getInt(STATE_SELECTED_PLACE);
            openPlaceDetails(pid);
        }
    }
}

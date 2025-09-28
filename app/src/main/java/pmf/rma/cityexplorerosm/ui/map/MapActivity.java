package pmf.rma.cityexplorerosm.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.cityexplorer.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.notifications.NotificationHelper;
import pmf.rma.cityexplorerosm.ui.detail.DetailActivity;
import pmf.rma.cityexplorerosm.ui.onboarding.OnboardingPrefs;

@AndroidEntryPoint
public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView;
    private MapViewModel mapViewModel;

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new RequestPermission(), granted -> {
                if (granted) {
                    NotificationHelper.sendSimple(this, "Notifications enabled", "You will get updates.");
                }
            });

    private boolean limitedToastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(44.8176, 20.4569));

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.getPlaces().observe(this, this::showPlaces);

        boolean limited = OnboardingPrefs.isLimitedLocation(this);
        if (!limited) {
            requestBasePermissions();
        } else if (!limitedToastShown) {
            Toast.makeText(this, getString(R.string.limited_mode_toast), Toast.LENGTH_LONG).show();
            limitedToastShown = true;
        }
        requestNotificationPermissionIfNeeded();

        findViewById(R.id.btnAll).setOnClickListener(v ->
                mapViewModel.getPlaces().observe(this, this::showPlaces));
        findViewById(R.id.btnCulture).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Kultura").observe(this, this::showPlaces));
        findViewById(R.id.btnFood).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Restoran").observe(this, this::showPlaces));
        findViewById(R.id.btnNight).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Noćni život").observe(this, this::showPlaces));
        findViewById(R.id.btnOpenProfile).setOnClickListener(v ->
                startActivity(new android.content.Intent(this, pmf.rma.cityexplorerosm.ui.profile.ProfileActivity.class)));


    }

    private void showPlaces(List<PlaceDomain> places) {
        mapView.getOverlays().clear();
        for (PlaceDomain place : places) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            marker.setTitle(place.getName());
            marker.setSubDescription(place.getDescription());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener((m, v) -> {
                startActivity(new android.content.Intent(this, DetailActivity.class)
                        .putExtra("place_id", place.getId()));
                NotificationHelper.sendSimple(this, "Opening", place.getName());
                return true;
            });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void requestBasePermissions() {
        // Samo lokacija ako je potrebna
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            // Lokacija odobrena ili ne; eventualno reagovati
        }
    }
}

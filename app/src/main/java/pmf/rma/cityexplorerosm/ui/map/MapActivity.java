package pmf.rma.cityexplorerosm.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

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
import pmf.rma.cityexplorerosm.ui.detail.DetailActivity;

@AndroidEntryPoint
public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView;
    private MapViewModel mapViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(44.8176, 20.4569)); // Beograd

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        mapViewModel.getPlaces().observe(this, this::showPlaces);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });

        findViewById(R.id.btnAll).setOnClickListener(v ->
                mapViewModel.getPlaces().observe(this, this::showPlaces));

        findViewById(R.id.btnCulture).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Kultura").observe(this, this::showPlaces));

        findViewById(R.id.btnFood).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Restoran").observe(this, this::showPlaces));

        findViewById(R.id.btnNight).setOnClickListener(v ->
                mapViewModel.getFilteredPlaces("Noćni život").observe(this, this::showPlaces));
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
                Intent i = new Intent(this, DetailActivity.class);
                i.putExtra("place_id", place.getId());
                startActivity(i);
                return true;
            });

            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQUEST_PERMISSIONS_REQUEST_CODE
                );
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

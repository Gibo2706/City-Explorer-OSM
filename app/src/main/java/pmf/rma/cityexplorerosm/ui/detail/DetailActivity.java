package pmf.rma.cityexplorerosm.ui.detail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cityexplorer.R;
import com.google.android.gms.location.LocationServices;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.domain.model.VisitStatus;
import pmf.rma.cityexplorerosm.location.DwellVerifier;

@AndroidEntryPoint
public class DetailActivity extends AppCompatActivity {

    private DetailViewModel viewModel;
    private int placeId = -1;
    private Button btnMarkVisited, btnScanQr, btnVerifyGps;
    private DwellVerifier dwellVerifier;

    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                    String payload = result.getData().getStringExtra("qr_payload");
                    boolean ok = viewModel.verifyWithQr(placeId, payload);
                    if (ok) {
                        toast(getString(R.string.verified_ok));
                    } else {
                        toast(getString(R.string.verified_fail));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        placeId = getIntent().getIntExtra("place_id", -1);

        btnMarkVisited = findViewById(R.id.btnMarkVisited);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnVerifyGps = findViewById(R.id.btnVerifyGps);

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.getPlaceById(placeId).observe(this, this::bindData);
        viewModel.observeVisitStatus(placeId).observe(this, this::renderVisitStatus);

        btnMarkVisited.setOnClickListener(v -> viewModel.markVisited(placeId));
        btnScanQr.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 20);
            } else {
                qrLauncher.launch(new Intent(this, QrScanActivity.class));
            }
        });
        btnVerifyGps.setOnClickListener(v -> doGpsVerify());
    }

    private void renderVisitStatus(VisitStatus status) {
        if (status == VisitStatus.NOT_VISITED) {
            btnMarkVisited.setEnabled(true);
            btnMarkVisited.setText(getString(R.string.mark_as_visited));
            btnScanQr.setVisibility(Button.GONE);
            btnVerifyGps.setVisibility(Button.GONE);
        } else if (status == VisitStatus.PENDING) {
            btnMarkVisited.setEnabled(false);
            btnMarkVisited.setText(getString(R.string.visit_pending));
            // koji tip? pogledaj place-u verificationType pa odluči:
            viewModel.getPlaceById(placeId).observe(this, place -> {
                if (place == null) return;
                String type = place.getVerificationType() == null ? "NONE" : place.getVerificationType();
                if ("QR".equalsIgnoreCase(type)) {
                    btnScanQr.setVisibility(Button.VISIBLE);
                    btnVerifyGps.setVisibility(Button.GONE);
                } else if ("GPS".equalsIgnoreCase(type)) {
                    btnScanQr.setVisibility(Button.GONE);
                    btnVerifyGps.setVisibility(Button.VISIBLE);
                } else {
                    btnScanQr.setVisibility(Button.GONE);
                    btnVerifyGps.setVisibility(Button.GONE);
                }
            });
        } else if (status == VisitStatus.VERIFIED) {
            btnMarkVisited.setEnabled(false);
            btnMarkVisited.setText(getString(R.string.visited_ok));
            btnScanQr.setVisibility(Button.GONE);
            btnVerifyGps.setVisibility(Button.GONE);
        }
    }

    private void bindData(PlaceDomain place) {
        if (place == null) return;

        TextView name = findViewById(R.id.placeName);
        TextView category = findViewById(R.id.placeCategory);
        TextView desc = findViewById(R.id.placeDescription);
        TextView hours = findViewById(R.id.placeWorkingHours);
        ImageView img = findViewById(R.id.placeImage);

        name.setText(place.getName());
        category.setText(place.getCategory());
        desc.setText(place.getDescription());
        hours.setText(getString(R.string.working_hours_prefix, place.getWorkingHours()));

        Glide.with(this)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(img);
    }

    private void doGpsVerify() {
        viewModel.getPlaceById(placeId).observe(this, place -> {
            if (place == null) return;

            int dwell = place.getVerificationDwellSec() != null ? place.getVerificationDwellSec() : 0;
            int radius = place.getVerificationRadiusM() != null ? place.getVerificationRadiusM() : 75;

            if (dwell <= 0) {
                // trenutna lokacija (instant)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 21);
                    return;
                }
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                @Nullable android.location.Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (last == null) { toast(getString(R.string.location_unavailable)); return; }
                boolean ok = viewModel.verifyWithGps(placeId, last.getLatitude(), last.getLongitude());
                toast(ok ? getString(R.string.verified_ok) : getString(R.string.verified_fail));
            } else {
                // štedljivi dwell (foreground)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 21);
                    return;
                }
                if (dwellVerifier != null) dwellVerifier.stop();
                dwellVerifier = new DwellVerifier(LocationServices.getFusedLocationProviderClient(this));
                btnVerifyGps.setEnabled(false);
                toast(getString(R.string.dwell_started, dwell, radius));

                dwellVerifier.start(place.getLatitude(), place.getLongitude(), radius, dwell,
                        new DwellVerifier.Callback() {
                            @Override public void onProgress(int sec, int total) { /* po želji progress UI */ }
                            @Override public void onSuccess(double lat, double lon) {
                                boolean ok = viewModel.verifyWithGps(placeId, lat, lon);
                                toast(ok ? getString(R.string.verified_ok) : getString(R.string.verified_fail));
                                btnVerifyGps.setEnabled(true);
                            }
                            @Override public void onFail(String reason) {
                                toast(reason);
                                btnVerifyGps.setEnabled(true);
                            }
                        });
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (dwellVerifier != null) dwellVerifier.stop();
    }

    private void toast(String m) {
        android.widget.Toast.makeText(this, m, android.widget.Toast.LENGTH_SHORT).show();
    }
}

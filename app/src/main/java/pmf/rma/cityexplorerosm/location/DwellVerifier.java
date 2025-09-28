package pmf.rma.cityexplorerosm.location;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class DwellVerifier {

    public interface Callback {
        void onProgress(int secondsInside, int totalRequired);
        void onSuccess(double lastLat, double lastLon);
        void onFail(String reason);
    }

    private final FusedLocationProviderClient fused;
    private final Handler main = new Handler(Looper.getMainLooper());
    private LocationCallback callback;
    private boolean started = false;

    public DwellVerifier(FusedLocationProviderClient fused) { this.fused = fused; }

    @SuppressLint("MissingPermission")
    public void start(double targetLat, double targetLon, int radiusM, int dwellSec, Callback cb) {
        if (started) return;
        started = true;

        final int[] insideSec = {0};
        final long startMs = System.currentTimeMillis();

        LocationRequest req = LocationRequest.create()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10_000)
                .setFastestInterval(5_000);

        callback = new LocationCallback() {
            @Override public void onLocationResult(LocationResult lr) {
                if (lr == null || lr.getLastLocation() == null) return;
                double lat = lr.getLastLocation().getLatitude();
                double lon = lr.getLastLocation().getLongitude();
                double d = haversineMeters(targetLat, targetLon, lat, lon);

                if (d <= radiusM) {
                    insideSec[0] += 10; // približno po intervalu
                    cb.onProgress(insideSec[0], dwellSec);
                    if (insideSec[0] >= dwellSec) {
                        stop();
                        cb.onSuccess(lat, lon);
                    }
                } else {
                    cb.onProgress(insideSec[0], dwellSec);
                    // opcionalno: reset insideSec ako izađe iz zone
                }

                // maksimalno trajanje: 15 min da ne troši
                if (System.currentTimeMillis() - startMs > 15 * 60_000) {
                    stop();
                    cb.onFail("Isteklo vreme verifikacije");
                }
            }
        };

        fused.requestLocationUpdates(req, callback, Looper.getMainLooper());
    }

    public void stop() {
        if (!started) return;
        started = false;
        if (callback != null) fused.removeLocationUpdates(callback);
        callback = null;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                        Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R*c;
    }
}

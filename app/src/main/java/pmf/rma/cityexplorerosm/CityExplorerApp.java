package pmf.rma.cityexplorerosm;

import android.app.Application;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.notifications.NotificationHelper;
import pmf.rma.cityexplorerosm.sync.PeriodicFirebaseSyncWorker;
import pmf.rma.cityexplorerosm.sync.PlaceRefreshWorker;
import androidx.hilt.work.HiltWorkerFactory;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import android.util.Log;

@HiltAndroidApp
public class CityExplorerApp extends Application implements Configuration.Provider {
    @Inject
    HiltWorkerFactory workerFactory;

    @Inject
    PlaceRepository placeRepository;

    // Use the single Hilt-provided database instance
    @Inject
    AppDatabase appDatabase;

    private static final String TAG = "CityExplorerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createNotificationChannel(this);

        // Seed initial local data only if empty, using injected DB (avoids duplicate DB instance)
        new Thread(() -> {
            try {
                if (appDatabase.placeDao().countSync() == 0) {
                    appDatabase.placeDao().insertAll(Arrays.asList(
                            new Place(1, "Petrovaradin", "...", 45.2440, 19.8813,
                                    "Kultura", "...", "08:00 - 22:00",
                                    "GPS", null, 7000, 10),
                            new Place(
                                    2, "Trg Republike", "Centralni trg u Beogradu",
                                    44.8176, 20.4569, "Kultura",
                                    "https://upload.wikimedia.org/wikipedia/commons/a/ac/Trg_republike_2021.jpg",
                                    "00:00 - 24:00",
                                    "NONE", null, null, null
                            ),
                            new Place(
                                    3, "Kafeterija XYZ", "Specijalitet: single origin kafa",
                                    44.8120, 20.4600, "Kafić",
                                    "https://upload.wikimedia.org/wikipedia/commons/4/45/A_small_cafe_in_Tokyo.jpg",
                                    "08:00 - 22:00",
                                    "QR", "KAFETERIJA-XYZ-2025", null, null
                            )
                    ));
                }
            } catch (Exception e) {
                Log.w(TAG, "Seeding failed: " + e.getMessage());
            }
        }).start();

        schedulePeriodicSync();
        schedulePlaceRefresh();

        // Initial remote fetch so that newest places are available immediately on first app entry
        placeRepository.refreshFromRemoteWithStats(new PlaceRepository.RefreshStatsCallback() {
            @Override
            public void onComplete(PlaceRepository.RefreshStats stats) {
                Log.i(TAG, "Initial remote places fetch: new=" + stats.newCount + ", updated=" + stats.updatedCount + ", orphan(local only)=" + stats.orphanCount + ", totalRemote=" + stats.totalRemote);
            }
            @Override
            public void onError(Throwable t) {
                Log.w(TAG, "Initial remote places fetch failed: " + t.getMessage());
            }
        });
    }

    private void schedulePeriodicSync() {
        Constraints c = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(PeriodicFirebaseSyncWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(c)
                .addTag("firebase_periodic_sync")
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "firebase_periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                req
        );
    }

    private void schedulePlaceRefresh() {
        Constraints c = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(PlaceRefreshWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(c)
                .addTag("place_periodic_refresh")
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "place_periodic_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                req
        );
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}

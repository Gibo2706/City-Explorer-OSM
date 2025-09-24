package pmf.rma.cityexplorerosm.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.List;

import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.data.remote.RetrofitClient;
import pmf.rma.cityexplorerosm.data.remote.model.PlaceDto;
import pmf.rma.cityexplorerosm.notifications.NotificationHelper;
import retrofit2.Call;
import retrofit2.Response;

public class SyncWorker extends Worker {

    private static final String TAG = "CityExplorerSync";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Sync started...");

        ApiService apiService = RetrofitClient.getApiService();
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        try {
            Call<List<PlaceDto>> call = apiService.getPlaces();
            Response<List<PlaceDto>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                db.placeDao().clearAll();
                for (PlaceDto dto : response.body()) {
                    Place place = new Place(dto.id, dto.name, dto.description, dto.latitude, dto.longitude);
                    db.placeDao().insert(place);
                }
                int count = response.body().size();
                Log.d(TAG, "Sync complete: " + count + " places");

                // Prikaži notifikaciju
                NotificationHelper.showSyncNotification(getApplicationContext(), count);

                return Result.success();
            } else {
                Log.e(TAG, "Sync failed: " + response.code());
                return Result.retry();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error during sync", e);
            return Result.retry();
        }
    }
}

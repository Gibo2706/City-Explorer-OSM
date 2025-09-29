package pmf.rma.cityexplorerosm.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

@HiltWorker
public class PlaceRefreshWorker extends Worker {

    private static final String TAG = "PlaceRefreshWorker";
    private final PlaceRepository placeRepository;

    @AssistedInject
    public PlaceRefreshWorker(@Assisted @NonNull Context context,
                               @Assisted @NonNull WorkerParameters params,
                               PlaceRepository placeRepository) {
        super(context, params);
        this.placeRepository = placeRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            int count = placeRepository.refreshFromRemoteBlocking();
            Log.i(TAG, "Refreshed places: " + count);
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "Remote refresh failed: " + e.getMessage());
            return Result.retry();
        }
    }
}


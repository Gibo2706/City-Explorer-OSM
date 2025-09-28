package pmf.rma.cityexplorerosm.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import pmf.rma.cityexplorerosm.auth.AuthManager;

/** WorkManager job koji periodično sync-uje user state kad ima mreže. */
public class PeriodicFirebaseSyncWorker extends Worker {
    private static final String TAG = "PeriodicFirebaseSync";

    private AuthManager auth;
    private FirebaseSyncManager firebaseSyncManager;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface SyncDeps {
        AuthManager auth();
        FirebaseSyncManager firebaseSyncManager();
    }

    public PeriodicFirebaseSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Manual resolve via Hilt entry point (works with default WorkerFactory)
        SyncDeps deps = EntryPointAccessors.fromApplication(context.getApplicationContext(), SyncDeps.class);
        this.auth = deps.auth();
        this.firebaseSyncManager = deps.firebaseSyncManager();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (auth != null && auth.isFirebaseEnabled()) {
                String uid = auth.currentUserId();
                if (!"local_user".equals(uid)) {
                    firebaseSyncManager.pushLocalToRemote(uid);
                    firebaseSyncManager.pullRemoteToLocal(uid);
                    Log.d(TAG, "Periodic sync OK for " + uid);
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "Periodic sync failed", e);
            return Result.retry();
        }
    }
}

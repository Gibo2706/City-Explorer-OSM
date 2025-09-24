package pmf.rma.cityexplorerosm.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.cityexplorer.R;

import java.util.concurrent.TimeUnit;

import pmf.rma.cityexplorerosm.sync.SyncWorker;
import pmf.rma.cityexplorerosm.ui.detail.DetailFragment;
import pmf.rma.cityexplorerosm.ui.list.ListFragment;

public class MainActivity extends AppCompatActivity implements ListFragment.OnPlaceSelectedListener {

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        WorkManager.getInstance(this).enqueue(
                new OneTimeWorkRequest.Builder(SyncWorker.class).build()
        );

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodicSync =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 3, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CityExplorerSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSync
        );

        // ako postoji detail_container → tablet layout
        twoPane = findViewById(R.id.detail_container) != null;

        if (savedInstanceState == null) {
            if (twoPane) {
                // Tablet layout - postavi oba fragmenta
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.list_container, new ListFragment())
                        .commit();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment())
                        .commit();
            } else {
                // Phone layout - postavi samo list fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ListFragment())
                        .commit();
            }
        }
    }

    @Override
    public void onPlaceSelected(long placeId) {
        DetailFragment fragment = DetailFragment.newInstance(placeId);

        if (twoPane) {
            // menja desni panel na tabletu
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            // menja ceo ekran na telefonu
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack("list") // Dodajte ovu liniju za back navigation
                    .commit();
        }
    }

    // Dodajte back navigation
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
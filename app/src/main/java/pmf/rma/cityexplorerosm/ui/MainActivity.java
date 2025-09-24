package pmf.rma.cityexplorerosm.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.cityexplorer.R;

import pmf.rma.cityexplorerosm.notifications.NotificationHelper;
import pmf.rma.cityexplorerosm.sync.SyncWorker;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_NOTIF = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kreiraj notification channel
        NotificationHelper.createNotificationChannel(this);

        // Runtime permission za Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            }
        }
    }
}

package pmf.rma.cityexplorerosm.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cityexplorer.R; // proveri da li je ovo pravi package

public class NotificationHelper {

    public static final String CHANNEL_ID = "sync_channel";
    private static final String CHANNEL_NAME = "Sync Notifications";
    private static final String CHANNEL_DESC = "Notifikacije o sinhronizaciji podataka";

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESC);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void showSyncNotification(Context context, int placeCount) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("CityExplorer Sync")
                .setContentText("Uspešno sinhronizovano " + placeCount + " mesta.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Eksplicitna provera permisije (Android 13+)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(1001, builder.build());
        }
    }
}

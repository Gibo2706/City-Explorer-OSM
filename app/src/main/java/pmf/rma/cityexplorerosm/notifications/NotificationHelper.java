package pmf.rma.cityexplorerosm.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.cityexplorer.R;

public final class NotificationHelper {
    public static final String CHANNEL_ID = "visits_channel";
    private static final String CHANNEL_NAME = "Visits";
    private static final String CHANNEL_DESC = "Place visit & updates";

    private static final int ID_BASE_VISIT = 1000;
    private static final int ID_BASE_BADGE = 2000;
    private static final int ID_SYNC = 3000;

    private NotificationHelper() {}

    private static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                ch.setDescription(CHANNEL_DESC);
                nm.createNotificationChannel(ch);
            }
        }
    }

    /** Backwards compatibility with earlier call site in Application */
    public static void createNotificationChannel(Context ctx) {
        ensureChannel(ctx);
    }

    private static boolean hasPostPermission(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static void send(Context ctx, int id, String title, String text) {
        ensureChannel(ctx);
        if (!hasPostPermission(ctx)) return;
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        try {
            NotificationManagerCompat.from(ctx).notify(id, b.build());
        } catch (SecurityException ignored) {
            // Permission might have been revoked at runtime.
        }
    }

    /** Generic simple notification (kept for manual/test triggering). */
    public static void sendSimple(Context ctx, String title, String text) {
        send(ctx, (int) System.currentTimeMillis(), title, text);
    }

    // === Domain specific helpers used across the app ===
    public static void showVisitedNotification(Context ctx, String placeName) {
        send(ctx, ID_BASE_VISIT + (placeName != null ? placeName.hashCode() : 0),
                ctx.getString(R.string.notification_place_verified_title),
                ctx.getString(R.string.notification_place_verified_body, placeName));
    }

    public static void showBadgeUnlockedNotification(Context ctx, String badgeTitle) {
        send(ctx, ID_BASE_BADGE + (badgeTitle != null ? badgeTitle.hashCode() : 0),
                ctx.getString(R.string.notification_badge_unlocked_title),
                ctx.getString(R.string.notification_badge_unlocked_body, badgeTitle));
    }

    public static void showSyncNotification(Context ctx, int count) {
        send(ctx, ID_SYNC,
                ctx.getString(R.string.notification_sync_done_title),
                ctx.getResources().getQuantityString(R.plurals.notification_sync_done_body, count, count));
    }
}

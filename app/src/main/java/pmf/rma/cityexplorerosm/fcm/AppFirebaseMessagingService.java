package pmf.rma.cityexplorerosm.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import pmf.rma.cityexplorerosm.notifications.NotificationHelper;

/** Firebase Cloud Messaging handler: prima tokene i simple data poruke. */
public class AppFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        // TODO: poslati token backendu / Firestore-u ako bude potrebno (npr. /userTokens/{uid})
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        String title = null;
        String body = null;
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }
        if (message.getData() != null && !message.getData().isEmpty()) {
            if (title == null) title = message.getData().get("title");
            if (body == null) body = message.getData().get("body");
        }
        if (title == null) title = "Poruka";
        if (body == null) body = "Stigla je nova obavest.";
        NotificationHelper.sendSimple(this, title, body);
    }
}


package pmf.rma.cityexplorerosm.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

/** Helper za FCM token management i debugging. */
public final class FcmTokenHelper {
    private static final String TAG = "FCMToken";

    private FcmTokenHelper() {}

    /** Dobavi trenutni FCM token i logovaj ga. */
    public static void logCurrentToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                });
    }

    /** Dobavi FCM token i kopiraj u clipboard sa toast notifikacijom. */
    public static void copyTokenToClipboard(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("FCM Token", token);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "FCM token kopiran u clipboard", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "FCM Token copied: " + token);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to get FCM token", e);
                    Toast.makeText(context, "Greška pri dobijanju FCM token-a", Toast.LENGTH_SHORT).show();
                });
    }
}

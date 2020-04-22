package org.pepppt.core.notifications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.pepppt.core.ProximityTracingService;

import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MessagingService extends FirebaseMessagingService {
    private static final String LOG_TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.d(LOG_TAG, "remoteMessage.getData: " + remoteMessage.getData());
            Map<String, String> mess_data = remoteMessage.getData();
            ProximityTracingService.getInstance().checkForNewMessages(false);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(LOG_TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onDeletedMessages() {
        //TODO: onDeletedMessages -> https://firebase.google.com/docs/cloud-messaging/android/receive#override-ondeletedmessages
    }

    @Override
    public void onNewToken(@NonNull String newToken) {
        Log.d(LOG_TAG, "onNewToken: " + newToken);
    }


}

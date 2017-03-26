package com.mateyinc.marko.matey.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mateyinc.marko.matey.activity.home.HomeActivity;

public class MateyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "MateyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
                String message = remoteMessage.getData().get("body");
                sendNotification(message);
    }


    //    /**
//     * Called when message is received.
//     *
//     * @param from SenderID of the sender.
//     * @param data Data bundle containing message data as key/value pairs.
//     *             For Set of keys use data.keySet().
//     */
//    // [START receive_message]
//    @Override
//    public void onMessageReceived(String from, Bundle data) {
//        String message = data.getString("message");
//        Log.d(TAG, "From: " + from);
//        Log.d(TAG, "Message: " + message);
//
//        if (from.startsWith("/topics/")) {
//            // message received from some topic.
//        } else {
//            // normal downstream message.
//        }
//
//        DataAccess dataAccess = DataAccess.getInstance(getApplicationContext());
//        // [START_EXCLUDE]
//        /**
//         * Production applications would usually process the message here.
//         * Eg: - Syncing with server.
//         *     - Store message in local database.
//         *     - Update UI.
//         */
//
//        /**
//         * In some cases it may be useful to show a notification indicating to the user
//         * that a message was received.
//         */
//        sendNotification(message);
//        // [END_EXCLUDE]
//    }
//    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("GCM Message")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

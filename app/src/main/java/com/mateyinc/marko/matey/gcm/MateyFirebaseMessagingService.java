package com.mateyinc.marko.matey.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.mateyinc.marko.matey.activity.utils.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.model.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MateyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "MateyGCMService";

    private Notification mNotification;
    private Bitmap mIconBitmap;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Notification received.");
                PendingIntent intent = parseNotificationData(remoteMessage);
                if (intent == null) return;
                sendNotification(mNotification.buildNotificationMessage(getApplicationContext()), intent);
            }
        });
        t.start();
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message       GCM message received.
     * @param pendingIntent {@link PendingIntent} object to build notification with
     */
    private void sendNotification(String message, PendingIntent pendingIntent) {
//        Intent resultIntent = new Intent(this, ProfileActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
//        stackBuilder.addNextIntent(new Intent(this, ProfileActivity.class));
//// Adds the back stack
////        stackBuilder.addParentStack(ResultActivity.class);
//// Adds the Intent to the top of the stack
////        stackBuilder.addNextIntent(resultIntent);
//// Gets a PendingIntent containing the entire back stack
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        ...
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(id, builder.build());
////
//        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setAction(HomeActivity.ACTION_SHOW_PROFILE);
//        intent.putExtra(HomeActivity.EXTRA_FROM_NOTIFICATION, true);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);

        // Remove markup styling from text
        Spanned spanMsg = Util.fromHtml(message);
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        Spannable buffer = new SpannableString(spanMsg);
        buffer.removeSpan(styleSpan);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Matey")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentText(buffer.toString())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (mIconBitmap != null)
            notificationBuilder.setLargeIcon(mIconBitmap);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Creates pending intent based on data provided in remoteMessage, parse message and picture url;
     *
     * @param remoteMessage message received from the server.
     * @return newly created pending intent.
     */
    private PendingIntent parseNotificationData(RemoteMessage remoteMessage) {
        Intent intent;

        try {
            JSONObject object = new JSONObject(remoteMessage.getData().get("data"));

            mNotification = new Notification().parse(object);
            intent = mNotification.buildIntent(getApplicationContext());

            downloadImage(mNotification.buildIconUrl());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse notification data.", e);
            return null;
        }

        intent.putExtra(HomeActivity.EXTRA_FROM_NOTIFICATION, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Parse info for notification that will show on user screen.
     */
    private void downloadImage(String url) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            InputStream in = response.body().byteStream();
            mIconBitmap = BitmapFactory.decodeStream(in);
            response.body().close();
        } catch (IOException | OutOfMemoryError | NullPointerException e) {
            Log.e(TAG, "Failed to parse notification message.", e);
            mIconBitmap = null;
        }
    }
}

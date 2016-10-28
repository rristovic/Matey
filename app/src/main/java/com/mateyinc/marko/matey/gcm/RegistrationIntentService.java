package com.mateyinc.marko.matey.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.io.IOException;

import static com.mateyinc.marko.matey.activity.Util.STATUS_NO_INTERNET;
import static com.mateyinc.marko.matey.activity.main.MainActivity.NEW_GCM_TOKEN;
import static com.mateyinc.marko.matey.activity.main.MainActivity.OLD_GCM_TOKEN;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};
    private String device_id;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.device_id = intent.getStringExtra("device_id");
        String token = null;
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

//            sendRegistrationToServer(token);
            // Saving tokens
            if (sharedPreferences.getString(NEW_GCM_TOKEN, null) == null)
                sharedPreferences.edit().putString(NEW_GCM_TOKEN, token).commit();
            else {
                String oldToken = sharedPreferences.getString(NEW_GCM_TOKEN, "");
                sharedPreferences.edit().putString(OLD_GCM_TOKEN, oldToken).putString(NEW_GCM_TOKEN, token).commit();
            }

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed
        Intent registrationComplete = new Intent(MateyGCMPreferences.REGISTRATION_COMPLETE);
        registrationComplete.putExtra(MainActivity.EXTRA_GCM_TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

//        SecurePreferences  mSecurePreferences = new SecurePreferences(this, "credentials", "1checkMate1717", true);
//        String device_id = mSecurePreferences.getString("device_id");
//
//        if (device_id == null) {
//            if (!Util.isInternetConnected(this))
//
//            return new SessionManager().getInstallationID(this, mSecurePreferences);
//        }
//        try {
//            String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8") + "&" +
//                    URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(this.device_id, "UTF-8");
//            HTTP http = new HTTP(UrlData.GCM_REGISTRATION, "POST");
//
//            String result = null;
//            if (http.sendPost(data)) result = http.getData();
//        } catch (Exception e) {}

    }

    /**
     * Method for getting the application id
     *
     * @return <p> SessionManager.STATUS_OK if the app id is in SecurePrefs and on hard drive;
     * <br> SessionManager.STATUS_ERROR_APPID if there was an error and the appid isn't saved;
     * <br> Util.STATUS_NO_INTERNET if there is no internet connection
     * </p>
     */
    private int deviceIDSet() {
        SecurePreferences securePreferences = new SecurePreferences(this, "credentials", "1checkMate1717", true);
        String device_id = securePreferences.getString("device_id");

        if (device_id == null) {
            if (!Util.isInternetConnected(this))
                return STATUS_NO_INTERNET;
            return SessionManager.getInstance(this).getInstallationID(this, securePreferences);
        }

        return SessionManager.STATUS_OK;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}

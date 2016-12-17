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
import com.mateyinc.marko.matey.activity.main.MainActivity;

import java.io.IOException;

import static com.mateyinc.marko.matey.activity.main.MainActivity.NEW_GCM_TOKEN;
import static com.mateyinc.marko.matey.activity.main.MainActivity.OLD_GCM_TOKEN;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};

    // Indicates if service is running or not
    public static boolean isRunning = false;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
            Log.d(TAG, "GCM Registration Token: " + token);

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
//            sharedPreferences.edit().putBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, true).apply();
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

        isRunning = false;
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
//        final WeakReference<MainActivity> reference = new WeakReference<>(activity);
//        final WeakReference<SecurePreferences> prefRef = new WeakReference<SecurePreferences>(securePreferences);
//        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
//
//        // GCM complete with success
//        if (gcmToken != null && gcmToken.length() != 0) {
//
//            // No old token found
////            if (sharedPreferences.getString(OLD_GCM_TOKEN, null) == null) {
//            // Request a string response from the provided URL.
//            MateyRequest stringRequest = new MateyRequest(Request.Method.POST, UrlData.REGISTER_DEVICE, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    MainActivity activity = reference.get();
//                    SecurePreferences securePreferences = prefRef.get();
//                    try {
//                        // Parse data
//                        JSONObject object = new JSONObject(response);
//                        String device_id = object.getString(KEY_DEVICE_ID);
//
//                        // Save data
//                        securePreferences.put(KEY_DEVICE_ID, device_id);
//                        MotherActivity.device_id = device_id;
//                        // Save prefs that token has been sent to the server
//                        sharedPreferences.edit().putBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, true).apply();
//
//                        // Notify UI
//                        activity.mDeviceReady = true;
//                        Log.d(TAG, "Device id=" + device_id);
//                    } catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage(), e);
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    MainActivity activity = reference.get();
//                    if (activity != null) {
//                        activity.mDeviceReady = false;
//                        activity.endLoadingAnim();
//                    }
//                    Log.e(TAG, error.getLocalizedMessage(), error);
//                }
//            });
//            stringRequest.addParam(UrlData.PARAM_NEW_GCM_ID, gcmToken);
//
//            // Add the request to the RequestQueue.
//            mInstance.addToRequestQueue(stringRequest);
//            activity = null;
////            } else {
//            // TODO - finish
////            }
//        } else {
//            activity.mDeviceReady = false;
//            activity.endLoadingAnim();
//            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
//        }
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

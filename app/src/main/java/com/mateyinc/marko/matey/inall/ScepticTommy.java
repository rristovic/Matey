package com.mateyinc.marko.matey.inall;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.lang.ref.WeakReference;

import static com.mateyinc.marko.matey.activity.Util.STATUS_NO_INTERNET;
import static com.mateyinc.marko.matey.internet.SessionManager.KEY_EXPIRES_IN;
import static com.mateyinc.marko.matey.internet.SessionManager.PREF_DEVICE_ID;
import static com.mateyinc.marko.matey.internet.SessionManager.TOKEN_SAVED_TIME;

/**
 * AsyncTask class used to check if the user is logged in, and to retrieve the app_id from the server
 */
public class ScepticTommy extends AsyncTask<String, Void, Integer> {

    private static final String TAG = ScepticTommy.class.getSimpleName();

    /**
     * Status code when user is logged in
     */
    private static final int STATUS_LOGGED_IN = 8;

    /**
     * Status code when user is not logged in, can proceed to the server
     */
    private static final int STATUS_NO_LOGIN = 0;


    private WeakReference<MotherActivity> activityRef;
    private MotherActivity activity;
    private SecurePreferences securePreferences;

    // constructor for referencing activity and secure preferences
    public ScepticTommy(MotherActivity activity) {

        this.activityRef = new WeakReference<>(activity);
        this.securePreferences = activity.getSecurePreferences();
    }


    // starting checking all important things like device_id and user credentials in background
    @Override
    protected Integer doInBackground(String... params) {
        activity = activityRef.get();
        return checkAll();
    }

    @Override
    protected void onPostExecute(Integer checkResult) {

        // if some error occured with the app id
        if (checkResult == SessionManager.STATUS_ERROR_APPID)
            activity.showDialog(1000);

            // if there is no internet connection
        else if (checkResult == STATUS_NO_INTERNET) {
            Bundle bundle = new Bundle();
            bundle.putString("message", "There is no internet connection! Please connect and try again.");
            activity.showDialog(1004, bundle);
        }
        // if user isn't logged in but everything is ok
        else if (checkResult == STATUS_NO_LOGIN) activity.mDeviceReady = true;
            // if user is logged in
        else if (checkResult == STATUS_LOGGED_IN) {
//            activity.showDialog(2);
            Intent i = new Intent(activity, HomeActivity.class);
            activity.startActivity(i);
            activity.finish();
        }

    }

    /**
     * Method for checking all the required parameters for the user login
     *
     * @return <p> SessionManager.STATUS_ERROR_APPID when there was an error while getting and saving the app id;
     * <br> STATUS_NO_INTERNET when there is no internet;
     * <br> STATUS_NO_LOING when user isn't logged in but can proceed to the server;
     * <br> STATUS_LOGGED_IN when the user is logged in, can proceed to the HomeActivity.class
     * </p>
     */
    private int checkAll() {

        if(!Util.isInternetConnected(activity)) return STATUS_NO_INTERNET;
//        int devIdSet = deviceIDSet();
//
//        if (devIdSet == SessionManager.STATUS_ERROR_APPID)
//            return SessionManager.STATUS_ERROR_APPID;
//        if (devIdSet == STATUS_NO_INTERNET) return STATUS_NO_INTERNET;
        if(securePreferences.getString(PREF_DEVICE_ID)!=null){
            activity.mServerReady = true;
        }

        // when app_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        // 0 if not, 7 if is

        return isUserLoggedOnPhone();

    }

    /**
     * Method for getting the application id
     *
     * @return <p> SessionManager.STATUS_OK if the app id is in SecurePrefs and on hard drive;
     * <br> SessionManager.STATUS_ERROR_APPID if there was an error and the appid isn't saved;
     * <br> STATS_NO_INTERNET if there is no internet connection
     * </p>
     */
    private int deviceIDSet() {
        String device_id = securePreferences.getString("device_id");

        if (device_id == null) {
            if (activity != null && !Util.isInternetConnected(activity)) return STATUS_NO_INTERNET;
            return new SessionManager().getInstallationID(activity, activity.getSecurePreferences());
        }

        return SessionManager.STATUS_OK;
    }


    /**
     * Method for checking offline if the user is logged on
     *
     * @return <p> STATUS_ERROR_APPID if the user isn't logged in, or it was suspicious and app logged him out;
     * <br> STATUS_LOGGED_IN the user is logged in offline, but now it needs to check on server
     * </p>
     */
    private int isUserLoggedOnPhone() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        long accessTokenTime = preferences.getLong(TOKEN_SAVED_TIME, -1);

        if (accessTokenTime == -1) {
            return STATUS_NO_LOGIN;
        }
        // If token has expired, must get new one
        int expiresIn;
        try{
            expiresIn = Integer.parseInt(securePreferences.getString(KEY_EXPIRES_IN));
            // Token saved in seconds, must convert to ms
            expiresIn*=1000;
        }catch (NumberFormatException e){
            Log.e(TAG, e.getLocalizedMessage(), e);
            return STATUS_NO_LOGIN;
        }
        if (System.currentTimeMillis() - accessTokenTime >= expiresIn){
            SessionManager.clearUserCredentials(activity, securePreferences);
            return STATUS_NO_LOGIN;
        }else{
            return STATUS_LOGGED_IN;
        }


//        String uid = securePreferences.getString("uid");
//        String username = securePreferences.getString("email");
//        String firstname = securePreferences.getString("firstname");
//        String lastname = securePreferences.getString("lastname");

//        if (user_id == null || uid == null || username == null || firstname == null || lastname == null) {
//            activity.clearUserCredentials();
//            return STATUS_NO_LOGIN;
//        }
 }

}

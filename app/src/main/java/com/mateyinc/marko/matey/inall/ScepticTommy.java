package com.mateyinc.marko.matey.inall;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.data_and_managers.InstallationIDManager;
import com.mateyinc.marko.matey.internet.MateyRequest;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.lang.ref.WeakReference;

import static com.mateyinc.marko.matey.activity.Util.STATUS_NO_INTERNET;

/**
 * AsyncTask class used to check if the user is logged in, and to retrieve the app_id from the server
 */
public class ScepticTommy extends AsyncTask<String, Void, Integer> {

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
        if (checkResult == InstallationIDManager.STATUS_ERROR_APPID) activity.showDialog(1000);
            // if there is no internet connection
        else if (checkResult == STATUS_NO_INTERNET) {
            Bundle bundle = new Bundle();
            bundle.putString("message", "There is no internet connection! Please connect and try again.");
            activity.showDialog(1004, bundle);
        }
        // if user isn't logged in but everything is ok
        else if (checkResult == STATUS_NO_LOGIN) activity.mServerReady = true;
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
     * @return <p> InstallationIDManager.STATUS_ERROR_APPID when there was an error while getting and saving the app id;
     * <br> STATUS_NO_INTERNET when there is no internet;
     * <br> STATUS_NO_LOING when user isn't logged in but can proceed to the server;
     * <br> STATUS_LOGGED_IN when the user is logged in, can proceed to the HomeActivity.class
     * </p>
     */
    private int checkAll() {

        if(!Util.isInternetConnected(activity)) return STATUS_NO_INTERNET;
//        int devIdSet = deviceIDSet();
//
//        if (devIdSet == InstallationIDManager.STATUS_ERROR_APPID)
//            return InstallationIDManager.STATUS_ERROR_APPID;
//        if (devIdSet == STATUS_NO_INTERNET) return STATUS_NO_INTERNET;


        // when app_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        // 0 if not, 7 if is

        return isUserLoggedOnPhone();

    }

    /**
     * Method for getting the application id
     *
     * @return <p> InstallationIDManager.STATUS_OK if the app id is in SecurePrefs and on hard drive;
     * <br> InstallationIDManager.STATUS_ERROR_APPID if there was an error and the appid isn't saved;
     * <br> STATS_NO_INTERNET if there is no internet connection
     * </p>
     */
    private int deviceIDSet() {
        String device_id = securePreferences.getString("device_id");

        if (device_id == null) {
            if (activity != null && !Util.isInternetConnected(activity)) return STATUS_NO_INTERNET;
            return new InstallationIDManager().getInstallationID(activity, activity.getSecurePreferences());
        }

        return InstallationIDManager.STATUS_OK;
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
        long accessTokenTime = preferences.getLong(MateyRequest.TOKEN_SAVED_TIME, -1);

        if (accessTokenTime == -1) {
            return STATUS_NO_LOGIN;
        }
        // If token has expired, must get new one
        else if (System.currentTimeMillis() - accessTokenTime >= Integer.parseInt(securePreferences.getString(MateyRequest.KEY_EXPIRES_IN))){
            activity.clearUserCredentials();
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

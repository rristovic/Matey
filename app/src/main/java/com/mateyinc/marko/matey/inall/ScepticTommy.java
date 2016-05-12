package com.mateyinc.marko.matey.inall;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.data_and_managers.InstallationIDManager;
import com.mateyinc.marko.matey.storage.SecurePreferences;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class ScepticTommy extends AsyncTask<String,Void,Integer> {

    MotherActivity activity;
    private SecurePreferences securePreferences;

    // constructor for referencing activity and secure preferences
    public ScepticTommy(MotherActivity activity) {

        this.activity = activity;
        this.securePreferences = activity.getSecurePreferences();

    }

    // starting checking all important things like device_id and user credentials in background
    @Override
    protected Integer doInBackground(String... params) {
        return checkAll();
    }

    // after checking everything giving response to UI thread
    @Override
    protected void onPostExecute(Integer checkResult) {

        // if some error occured
        if (checkResult == 0) activity.showDialog(0);
        // if there is no internet connection
        else if(checkResult == 18) {
            Bundle bundle = new Bundle();
            bundle.putString("message", "There is no internet connection! Please connect and try again.");
            activity.showDialog(4, bundle);
        }
        // if user isn't logged in but everything is ok
        else if(checkResult == 1) activity.mServerReady = true;
        // if user is logged in
        else if(checkResult == 7) {
//            activity.showDialog(2);
            Intent i = new Intent(activity, HomeActivity.class);
            activity.startActivity(i);
            activity.finish();
        }

    }

    // this function returns 0 if there is a problem with device_id
    // it returns 1 if user isn't logged in
    // returns 7 if everything is ok and the app can proceed
    // returns 18 if there is no internet connection
    public int checkAll() {

        int devIdSet = deviceIDSet();

        // if it returns 0 it means there is some error occurred
        // if 18 there is no internet connection
        // if 7 all ok
        if (devIdSet == 0) return 0;
        if (devIdSet == 18) return 18;

        // when device_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        // 0 if not, 7 if is
        if (isUserLoggedOnPhone() == 0) return 1;

        return 7;

    }

    // if returns 7 device_id is set to SecurePreferences
    // otherwise there is an error
    public int deviceIDSet() {

        String device_id = securePreferences.getString("device_id");
        if (device_id == null) {

            if(!activity.isInternetConnected()) return 18;

            // returns
            // 7-ok
            // 0-error
            return new InstallationIDManager().retreveInstallationId(activity);

        }
        // 7-ok
        return 7;

    }

    // returns 0 if user isn't logged in, or it was suspicious and
    // app logged him out
    // if it returns 1 than it started checking the user on server
    // it will mean that the user is ok until it proves otherwise
    public int isUserLoggedOnPhone() {

        String user_id = securePreferences.getString("user_id");
        String uid = securePreferences.getString("uid");
        String username = securePreferences.getString("email");
        String firstname = securePreferences.getString("firstname");
        String lastname = securePreferences.getString("lastname");

        if (user_id == null || uid == null || username == null || firstname == null || lastname == null) {

            activity.clearUserCredentials();
            return 0;

        }

        return 7;

    }

}

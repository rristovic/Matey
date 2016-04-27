package com.mateyinc.marko.matey.inall;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;

import com.mateyinc.marko.matey.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.home.Home;
import com.mateyinc.marko.matey.data_and_managers.InstallationIDManager;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.internet.procedures.CheckUserAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class ScepticTommy extends AsyncTask<String,Void,Integer> {

    MotherActivity activity;
    private SecurePreferences securePreferences;

    public final static int ERROR_LAYOUT = R.layout.error_screen;
    public final static int WAITING_LAYOUT = R.layout.waiting_screen;

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

        FragmentTransaction fragmentTransaction = activity.fragmentManager.beginTransaction();

        if (checkResult == 0 || (checkResult == 1 && !(activity instanceof MainActivity))) {
            fragmentTransaction.replace(R.id.fragment, activity.errorScreen);
            fragmentTransaction.commit();
        } else {

            if (checkResult == 7) {

                if(activity instanceof MainActivity) {
                    Intent goHome = new Intent(activity, Home.class);
                    activity.startActivity(goHome);
                } else checkUserServerResponse();

            }

            fragmentTransaction.replace(R.id.fragment, activity.desiredScreen);
            fragmentTransaction.commit();
        }

    }

    // this function returns 0 if there is a problem with device_id
    // it returns 1 if user isn't logged in
    // returns 7 if everything is ok and the app can proceed
    public int checkAll() {

        // if it returns 0 it means there is some error occurred
        if (deviceIDSet() == 0) return 0;

        // when device_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        if (isUserLoggedOnPhone() == 0) return 1;

        return 7;

    }

    // if returns 7 device_id is set to SecurePreferences
    // otherwise there is an error
    public int deviceIDSet() {

        String device_id = securePreferences.getString("device_id");
        if (device_id == null) {

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

        String uid = securePreferences.getString("uid");
        String username = securePreferences.getString("username");
        String firstname = securePreferences.getString("firstname");
        String lastname = securePreferences.getString("lastname");

        if (uid == null || username == null || firstname == null || lastname == null) {

            activity.clearUserCredentials();

            // if there is still uid or username but not firstname and lastname logout user
            // just in case
            if (uid != null && username != null) {

                String result;
                int exceptionTime = 0;

                while (exceptionTime < 5) {
                    try {

                        String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                                URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                                URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(securePreferences.getString("device_id"), "UTF-8");
                        HTTP http = new HTTP(UrlData.LOG_URL, "POST");

                        if (http.sendPost(data)) result = http.getData();

                        break;

                    } catch (Exception e) {
                        exceptionTime++;
                    }
                }

            }

            return 0;
        }

        return 7;

    }

    // checking json string from server about user - is logged
    // returns true if is, else false
    private void checkUserServerResponse() {

        int exceptionTime = 0;

        while (exceptionTime < 5) {
            try {
                CheckUserAs checkUserAs = new CheckUserAs(activity);
                checkUserAs.execute(securePreferences.getString("uid"), securePreferences.getString("username"));

            } catch (Exception e) {
                exceptionTime++;
            }

        }

    }
}

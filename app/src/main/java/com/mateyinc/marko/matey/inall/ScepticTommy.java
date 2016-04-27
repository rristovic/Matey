package com.mateyinc.marko.matey.inall;

import android.app.FragmentTransaction;

import com.mateyinc.marko.matey.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data_and_managers.InstallationIDManager;
import com.mateyinc.marko.matey.internet.procedures.CheckUserAs;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONObject;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class ScepticTommy implements Runnable {

    MotherActivity activity;
    private SecurePreferences securePreferences;

    public final static int ERROR_LAYOUT = R.layout.error_screen;
    public final static int WAITING_LAYOUT = R.layout.waiting_screen;

    public ScepticTommy(MotherActivity activity) {

        this.activity = activity;
        this.securePreferences = activity.getSecurePreferences();

    }

    @Override
    public void run() {

        FragmentTransaction fragmentTransaction = activity.fragmentManager.beginTransaction();

        int checkResult = checkAll();

        if(checkResult == 0 || (checkResult == 1 && !(activity instanceof MainActivity))) {
            fragmentTransaction.replace(R.id.fragment, activity.errorScreen);
            fragmentTransaction.commit();
        }
        else {
            fragmentTransaction.replace(R.id.fragment, activity.desiredScreen);
            fragmentTransaction.commit();

            if(checkResult == 7) {
                checkUserServerResponse();
            }
        }

    }

    // checking json string from server about user - is logged
    // returns true if is, else false
    private boolean checkUserServerResponse () {

        int exceptionTime = 0;

        while(exceptionTime < 5) {
            try {
                CheckUserAs checkUserAs = new CheckUserAs(activity);
                String result = checkUserAs.execute(securePreferences.getString("uid"), securePreferences.getString("username")).get();

                if (result != null) {

                    try {

                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.getBoolean("success")) {

                            if(!jsonObject.getBoolean("logged")) {

                                activity.clearUserCredentials();
                                return false;

                            } else return true;

                        } else return false;

                    } catch (Exception e) {

                        return false;

                    }

                } else return false;

            } catch (Exception e) {
                exceptionTime++;
            }
        }

        return false;

    }

    // this function returns 0 if there is a problem with device_id
    // it returns 1 if user isn't logged in
    // returns 7 if everything is ok and the app can proceed
    public int checkAll () {

        // if it returns 0 it means there is some error occurred
        if(deviceIDSet() == 0) return 0;

        // when device_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        if(isUserLoggedOnPhone() == 0) return 1;

        return 7;

    }

    // if returns 7 device_id is set to SecurePreferences
    // otherwise there is an error
    public int deviceIDSet () {

        String device_id = securePreferences.getString("device_id");
        if(device_id == null) {

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
    public int isUserLoggedOnPhone () {

        String uid = securePreferences.getString("uid");
        String username = securePreferences.getString("username");
        String firstname = securePreferences.getString("firstname");
        String lastname = securePreferences.getString("lastname");

        if(uid == null || username == null || firstname == null || lastname == null) {

            activity.clearUserCredentials();

            // if there is still uid or username but not firstname and lastname logout user
            // just in case
            if(uid!=null && username!=null) {

                LogoutAs logoutAs = new LogoutAs(activity);
                logoutAs.execute(securePreferences.getString("device_id"), uid, username);

            }

            return 0;
        }

        return 7;

    }

}

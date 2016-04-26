package com.mateyinc.marko.matey.inall;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data_and_managers.InstallationIDManager;
import com.mateyinc.marko.matey.internet.procedures.CheckUserAs;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class ScepticTommy {

    MotherActivity activity;
    private SecurePreferences securePreferences;

    public final static int ERROR_LAYOUT = R.layout.error_screen;
    public final static int WAITING_LAYOUT = R.layout.waiting_screen;

    public ScepticTommy(MotherActivity activity) {

        this.activity = activity;
        this.securePreferences = activity.getSecurePreferences();

    }

    // static function for showing error screen
    public void showErrorLayout (MotherActivity activity) {
        activity.setContentView(ERROR_LAYOUT);
    }
    // static function for showing waiting screen
    public void showWaitingLayout (MotherActivity activity) {
        activity.setContentView(ERROR_LAYOUT);
    }

    // this function returns 0 if there is a problem with device_id
    // it returns 1 if user isn't logged in
    // returns 7 if everything is ok and the app can proceed
    public int checkAll () {

        // if it returns 0 it means there is some error occurred
        if(deviceIDSet() == 0) return 0;

        // when device_id is in SecurePreferences, we go further
        // checking if the user credentials is in place
        //if(isUserLogged() == 0) return 1;

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
    public int isUserLogged () {

        String uid = securePreferences.getString("uid");
        String username = securePreferences.getString("username");
        String firstname = securePreferences.getString("firstname");
        String lastname = securePreferences.getString("lastname");

        if(uid.equals("") || username.equals("") || firstname.equals("") || lastname.equals("")) {
            securePreferences.removeValue("uid");
            securePreferences.removeValue("username");
            securePreferences.removeValue("firstname");
            securePreferences.removeValue("lastname");

            // if there is still uid or username but not firstname and lastname logout user
            // just in case
            if(!uid.equals("") && !username.equals("") && !securePreferences.getString("device_id").equals("")) {

                LogoutAs logoutAs = new LogoutAs(activity);
                logoutAs.execute(securePreferences.getString("device_id"), uid, username);

            }

            return 0;
        }

        CheckUserAs checkUserAs = new CheckUserAs(activity);
        checkUserAs.execute(uid, username);
        return 7;

    }

}

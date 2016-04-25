package com.mateyinc.marko.matey.inall;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.InstallationIDManager;
import com.mateyinc.marko.matey.internet.procedures.CheckUserAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class ScepticTommy {

    MotherActivity activity;
    public final static int ERROR_LAYOUT = R.layout.error_screen;
    public final static int WAITING_LAYOUT = R.layout.waiting_screen;
    int DESIRED_LAYOUT;
    private SecurePreferences securePreferences;

    public ScepticTommy(MotherActivity activity, int DESIRED_LAYOUT) {

        this.activity = activity;
        this.DESIRED_LAYOUT = DESIRED_LAYOUT;
        this.securePreferences = activity.getSecurePreferences();

    }

    public boolean isDeviceIDSet () {

        String device_id = securePreferences.getString("device_id");
        if(device_id.equals("")) {
            InstallationIDManager.retreveInstallationId(activity);
            return false;
        }

        return true;

    }

    public boolean isUserLogged () {

        String uid = securePreferences.getString("uid");
        String username = securePreferences.getString("username");
        String firstname = securePreferences.getString("firstname");
        String lastname = securePreferences.getString("lastname");

        if(uid.equals("") || username.equals("") || firstname.equals("") || lastname.equals("")) {
            securePreferences.removeValue("uid");
            securePreferences.removeValue("username");
            securePreferences.removeValue("firstname");
            securePreferences.removeValue("lastname");
            return false;
        }

        CheckUserAs checkUserAs = new CheckUserAs(activity, DESIRED_LAYOUT, WAITING_LAYOUT, ERROR_LAYOUT);
        checkUserAs.execute(uid, username);

        return true;

    }

}

package com.mateyinc.marko.matey.data_and_managers;

import android.os.Build;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class UrlData {

    public final static String FB_LOG_URL = "http://10.0.2.2/NotifindaAPI/web/index.php/api/user/fblogin";
    public final static String LOG_URL = "http://10.0.2.2/NotifindaAPI/web/index.php/api/user/login";
    public final static String FIRST_RUN_URL = "http://10.0.2.2/NotifindaAPI/web/index.php/api/user/firstRun/" + Build.BOARD + "/" + Build.BRAND + "/" + Build.DEVICE + "/" + Build.MODEL;
    public final static String CHECK_USER_URL = "http://10.0.2.2/NotifindaAPI/web/index.php/api/user/firstRun";

}

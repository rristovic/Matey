package com.mateyinc.marko.matey.data_and_managers;

import android.os.Build;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class UrlData {

    public final static String FIRST_RUN_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/firstRun/" +
            Build.BOARD + "/" + Build.BRAND + "/" + Build.DEVICE + "/" + Build.MODEL;
    public final static String REGISTER_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/register";
    public final static String LOG_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/login";
    public final static String FACEBOOK_LOG_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/fblogin";
    public final static String LOG_OUT_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/logout";
    public final static String GCM_REGISTRATION = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/registrationGCM";

    public final static String FETCH_ALL_POSTS = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/requestAllPosts";
    public final static String REQUEST_USER_PROFILE = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/requestUserProfile";

}

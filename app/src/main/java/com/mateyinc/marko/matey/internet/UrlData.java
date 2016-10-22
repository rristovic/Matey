package com.mateyinc.marko.matey.internet;

import android.os.Build;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class UrlData {
    public final static String BASE_URL = "https://matey-api-m4rk07.c9users.io/web/index.php";

    private final static String API_VERSION = "v1";
    public final static String FIRST_RUN_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/firstRun/" +
            Build.BOARD + "/" + Build.BRAND + "/" + Build.DEVICE + "/" + Build.MODEL;

    public final static String LOG_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/login";
    public final static String FACEBOOK_LOG_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/fblogin";
    public final static String LOG_OUT_URL = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/logout";
    public final static String GCM_REGISTRATION = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/registrationGCM";

    public final static String FETCH_ALL_POSTS = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/requestAllPosts";
    public final static String REQUEST_USER_PROFILE = "http://notifinda-api-m4rk07-1.c9users.io/web/index.php/api/user/requestUserProfile";

    public static final String FETCH_ALL_POSTS_WITH_FRIENDS = "http://example.com"; // TODO - Complete method on server

    // Register new device
    public final static String REGISTER_DEVICE = BASE_URL.concat("/register/device");
    /**
     * Used when app is freshly installed without old gcm token
     */
    public final static String PARAM_DEVICE_ID = "device_id";

    /**
     * Used when the token has already been created but it needs refresh
     */
    public final static String PARAM_NEW_GCM_ID = "gcm";
    public final static String PARAM_OLD_GCM_ID = "old_gcm";

    // Register new user
    public final static String REGISTER_USER = BASE_URL.concat("/register/user");
    public final static String PARAM_USER_FIRST_NAME = "first_name";
    public final static String PARAM_USER_LAST_NAME = "last_name";
    public final static String PARAM_PASSWORD = "password";
    public final static String PARAM_EMAIL = "email";

    // Login OAuth2
    public static final String OAUTH_BASE_URL = "https://matey-oauth2-m4rk07.c9users.io/web/index.php";
    public final static String OAUTH_LOGIN = OAUTH_BASE_URL.concat("/api/oauth2/token");
    public static final  String PARAM_GRANT_TYPE = "grant_type";
    public static final  String PARAM_GRANT_TYPE_PASSWORD = "password";
    public static final  String PARAM_CLIENT_ID = "client_id";
    public static final  String PARAM_CLIENT_ID_VALUE = "1";
    public static final  String PARAM_CLIENT_SECRET = "client_secret";
    public static final  String PARAM_CLIENT_SECRET_VALUE = "";
    public final static String PARAM_USERNAME = "username";


    // Login user
    public final static String LOGIN_USER = BASE_URL.concat("/api/").concat(API_VERSION).concat("/login");
    public static final String PARAM_ACCESS_TOKEN = "access_token";


}

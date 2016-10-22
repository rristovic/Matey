package com.mateyinc.marko.matey.internet;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class UrlData {
    public final static String BASE_URL = "http://192.168.1.80/matey-api/web/index.php";
//    public final static String BASE_URL = "https://matey-api-m4rk07.c9users.io/web/index.php";
    private final static String API_VERSION = "v1";

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
    public static final String OAUTH_BASE_URL = "http://192.168.1.80/matey-oauth2/web/index.php";
//    public static final String OAUTH_BASE_URL = "https://matey-oauth2-m4rk07.c9users.io/web/index.php";
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

    // Facebook login
    public final static String FACEBOOK_LOGIN = OAUTH_LOGIN;
    public static final  String PARAM_GRANT_TYPE_SOCIAL = "social_exchange";
    public static final  String PARAM_FB_ACCESS_TOKEN = PARAM_ACCESS_TOKEN;



}

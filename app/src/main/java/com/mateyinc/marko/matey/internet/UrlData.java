package com.mateyinc.marko.matey.internet;

public class UrlData {
    /** Server API version */
    private final static String API_VERSION = "v1";

    /** Server authorisation header */
    public final static String PARAM_AUTH_TYPE ="X-Bearer-Authorization";

    /** Base server url */
    public final static String BASE_URL = "https://matey-api-m4rk07.c9users.io/web/index.php";

    /**
     * URL for downloading and uploading to resource server
     * NOTE: X-Bearer-Authorization required
     */
    public final static String ACCESS_BASE_URL = BASE_URL.concat("/api/").concat(API_VERSION);

    /** Url for registering new device */
    public final static String REGISTER_DEVICE = BASE_URL.concat("/register/device");

    /** The device id retrieved from the server */
    public final static String PARAM_DEVICE_ID = "device_id";

    /** GCM Token param */
    public final static String PARAM_NEW_GCM_ID = "gcm";
    /** GCM Token param */
    public final static String PARAM_OLD_GCM_ID = "old_gcm";

    /////////////////////////////////////////////////////////////////////////////////////
    // Data download params and urls ////////////////////////////////////////////////////

    /** Url for downloading news feed */
     static final String GET_NEWSFEED_ROUTE = ACCESS_BASE_URL.concat("/newsfeed");

    /** Position parameter of the post in the database for route {@link UrlData#GET_NEWSFEED_ROUTE} */
     static final String PARAM_START_POS = "start";

    /** Count parameter for post count to download at route {@link UrlData#GET_NEWSFEED_ROUTE} */
     static final String PARAM_COUNT = "count";
    /////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////
    // Data upload params and urls //////////////////////////////////////////////////////

    /** Url for uploading new post to the server. */
    public static final String POST_NEW_BULLETINS_ROUTE = ACCESS_BASE_URL.concat("/post/add");
    /** Interest_id parameter of the selected interest in new post */
    public static final String PARAM_INTEREST_ID = "interest_id";
    /** Text param of new post */
    public static final String PARAM_TEXT_DATA = "text";

    /** Url for uploading new followed friends list to the server. */
    public static final String POST_NEW_FOLLOWED_FRIENDS = ACCESS_BASE_URL.concat("/follower/follow");
    /** User_id param of the followed user */
    public static final String PARAM_FOLLOWED_USER_ID = "to_user";
    /////////////////////////////////////////////////////////////////////////////////////

    // Register new user
    public final static String REGISTER_USER = BASE_URL.concat("/register/user");
    public final static String PARAM_USER_FIRST_NAME = "first_name";
    public final static String PARAM_USER_LAST_NAME = "last_name";
    public final static String PARAM_PASSWORD = "password";
    public final static String PARAM_EMAIL = "email";

    // Login OAuth2
//    public static final String OAUTH_BASE_URL = "http://192.168.1.80/matey-oauth2/web/index.php";
//    public static final String OAUTH_BASE_URL = "https://matey-oauth2-m4rk07.c9users.io/web/index.php";
    public final static String OAUTH_LOGIN = BASE_URL.concat("/api/oauth2/token");
    public static final  String PARAM_GRANT_TYPE = "grant_type";
    public static final  String PARAM_GRANT_TYPE_PASSWORD = "password";
    public static final  String PARAM_CLIENT_ID = "client_id";
    public static final  String PARAM_CLIENT_ID_VALUE = "1";
    public static final  String PARAM_CLIENT_SECRET = "client_secret";
    public static final  String PARAM_CLIENT_SECRET_VALUE = "";
    public final static String PARAM_USERNAME = "username";

    // Login user
    public final static String LOGIN_USER = ACCESS_BASE_URL.concat("/login");
    public final static String PARAM_FBTOKEN = "access_token";

    // Facebook login
    public final static String FACEBOOK_LOGIN = OAUTH_LOGIN;
    public static final  String PARAM_GRANT_TYPE_SOCIAL = "social_exchange";
    // Path for merging standard account with facebook account
    public final static String FACEBOOK_MERGE = LOGIN_USER.concat("/merge/facebook");
    // Path for merging facebook account with newly created standard account
    public final static String STD_EMAIL_MERGE = LOGIN_USER.concat("/merge/standard");



}

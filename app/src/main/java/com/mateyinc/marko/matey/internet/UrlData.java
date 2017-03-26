package com.mateyinc.marko.matey.internet;

public abstract class UrlData {
    /** Server API version */
    public static final String API_VERSION = "v1";

    /** Server authorisation header */
    public static final String PARAM_AUTH_TYPE ="X-Bearer-Authorization";

    /** Base server url */
    public static final String BASE_URL = "https://matey-api-m4rk07.c9users.io/web/index.php";

    /**
     * URL for downloading and uploading to resource server
     * NOTE: X-Bearer-Authorization required
     */
    public static final String ACCESS_BASE_URL = BASE_URL.concat("/api/").concat(API_VERSION);

    /** Url for registering new device */
    public static final String REGISTER_DEVICE = BASE_URL.concat("/devices");

    /** The device id retrieved from the server */
    public static final String PARAM_DEVICE_ID = "device_id";

    /** GCM Token param */
    public static final String PARAM_NEW_GCM_ID = "gcm";
    /** GCM Token param */
    public static final String PARAM_OLD_GCM_ID = "old_gcm";

    /////////////////////////////////////////////////////////////////////////////////////
    ///// Profile data /////////////////////////////////////////////////////////////////////////////////////
    /** Url for uploading new followed friends list to the server. */
    public static final String POST_NEW_FOLLOWED_FRIENDS = ACCESS_BASE_URL.concat("/follower/follow");
    /** User_id param of the followed user */
    public static final String PARAM_FOLLOWED_USER_ID = "user_id";

    /** Url for uploading new followed friend */
    private static final String POST_NEW_FOLLOWED_USER = ACCESS_BASE_URL.concat("/users/me/users/:userId/follow");
    /** Url for uploading new unfollowed friend */
    private static final String DELETE_FOLLOWED_USER = ACCESS_BASE_URL.concat("/users/me/users/:userId/follow");
    /** Url for download followers list */
    private static final String GET_FOLLOWERS = ACCESS_BASE_URL.concat("/users/:userId/followers");
    /** Url for download followers list */
    private static final String GET_FOLLOWING = ACCESS_BASE_URL.concat("/users/:userId/following");
    /** Limit query param for followers list */
    public static final String QPARAM_LIMIT = "limit";
    /** Position query param for followers list */
    public static final String QPARAM_OFFSET = "offset";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///// Pic/file data ///////////////////////////////////////////////////////////////////////////////////////
    /** Url for uploading new profile pic */
    public static final String POST_PROFILE_PIC = ACCESS_BASE_URL.concat("/users/me/profiles/pictures");
    /** Url for uploading new cover pic */
    public static final String POST_COVER_PIC = ACCESS_BASE_URL.concat("/users/me/profiles/pictures");
    /** Param for url data that is returned when upload is success */
    public static final String PARAM_PIC_LINK = "Location";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Register new user
    public static final String REGISTER_USER = BASE_URL.concat("/users/accounts");
    public static final String PARAM_USER_FIRST_NAME = "first_name";
    public static final String PARAM_USER_LAST_NAME = "last_name";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_EMAIL = "email";

    // Login OAuth2
    public static final String OAUTH_LOGIN = BASE_URL.concat("/api/oauth2/token");
    public static final String PARAM_GRANT_TYPE = "grant_type";
    public static final String PARAM_GRANT_TYPE_PASSWORD = "password";
    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_ID_VALUE = "1";
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    public static final String PARAM_CLIENT_SECRET_VALUE = "";
    public static final String PARAM_USERNAME = "username";

    // Login user
    private static final String LOGIN_USER = ACCESS_BASE_URL.concat("/users/me/devices/:deviceId/login");
    public static final String PARAM_FBTOKEN = "access_token";

    // Facebook login
    public static final String FACEBOOK_LOGIN = OAUTH_LOGIN;
    public static final String PARAM_GRANT_TYPE_SOCIAL = "social_exchange";
    // Path for merging standard account with facebook account
    public static final String FACEBOOK_MERGE = LOGIN_USER.concat("/merge/facebook");
    // Path for merging facebook account with newly created standard account
    public static final String STD_EMAIL_MERGE = LOGIN_USER.concat("/merge/standard");

    // Logout
    private static final String LOGOUT_USER = ACCESS_BASE_URL.concat("/users/me/devices/:deviceId/login");

    public static String createLoginUrl(String deviceId){
        return LOGIN_USER.replace(":deviceId", deviceId);
    }

    public static String createLogoutUrl(String deviceId){
        return LOGOUT_USER.replace(":deviceId", deviceId);
    }

    // Profile methods
    /** Url for downloading user profile data */
    private static final String GET_USER_PROFILE_ROUTE = ACCESS_BASE_URL.concat("/users/:userId/profile");
    public static String createProfileDataUrl(long userId){
        return GET_USER_PROFILE_ROUTE.replace(":userId", Long.toString(userId));
    }

    public static String createUnfollowUrl(long userId){
        return DELETE_FOLLOWED_USER.replace(":userId", Long.toString(userId));
    }
    public static String createFollowUrl(long userId){
        return POST_NEW_FOLLOWED_USER.replace(":userId", Long.toString(userId));
    }
    public static String createFollowingListUrl(long userId){
        return GET_FOLLOWING.replace(":userId", Long.toString(userId));
    }
    public static String createFollowersListUrl(long userId){
        return GET_FOLLOWERS.replace(":userId", Long.toString(userId));
    }
    /////////////////////////////////////////////////////////////////////////////////////


    ////////  NEWS FEED UrlData  ////////////////////////////////////////////////////
    /** Url for downloading news feed */
    public static final String GET_NEWSFEED_ROUTE = ACCESS_BASE_URL.concat("/deck");

    /** Position parameter of the post in the database for route {@link UrlData#GET_NEWSFEED_ROUTE} */
    public static final String PARAM_START_POS = "start";

    /** Count parameter for post count to startDownloadAction at route {@link UrlData#GET_NEWSFEED_ROUTE} */
    public static final String PARAM_COUNT = "count";


    //////// BULLETIN UrlData //////////////
    public static final String POST_NEW_BULLETIN = ACCESS_BASE_URL.concat("/posts");

    private static final String GET_BULLETIN = ACCESS_BASE_URL.concat("/posts/:postId");
    public static String buildGetBulletinUrl(long postId){
        return GET_BULLETIN.replace(":postId", Long.toString(postId));
    }

    private static final String PUT_BULLETIN_APPROVE = ACCESS_BASE_URL.concat("/posts/:postId/boosts");
    public static String buildBulletinBoostUrl(long postId){
        return PUT_BULLETIN_APPROVE.replace(":postId", Long.toString(postId));
    }
    ///////////////////////////////////////////

    //////// Reply UrlData //////////////
    private static final String POST_NEW_BULLETIN_REPLY = ACCESS_BASE_URL.concat("/posts/:postId/replies");
    public static String buildNewBulletinReplyUrl(long postId) {
        return POST_NEW_BULLETIN_REPLY.replace(":postId", Long.toString(postId));
    }

    private static final String PUT_REPLY_APPROVE = ACCESS_BASE_URL.concat("/replies/:replyId/approves");
    public static String buildReplyLikeUrl(long replydId){
        return PUT_REPLY_APPROVE.replace(":replyId", Long.toString(replydId));
    }

    private static final String POST_NEW_REPLY_REPLY = ACCESS_BASE_URL.concat("/replies/:replyId/rereplies");
    public static String buildNewReplyReplyUrl(long replyId) {
        return POST_NEW_REPLY_REPLY.replace(":replyId", Long.toString(replyId));
    }

    private static final String PUT_REREPLY_APPROVE = ACCESS_BASE_URL.concat("/rereplies/:rereplyId/approves");
    public static String buildReReplyLikeUrl(long reReplydId){
        return PUT_REREPLY_APPROVE.replace(":rereplyId", Long.toString(reReplydId));
    }

    private static final String GET_REREPLIES = ACCESS_BASE_URL.concat("/replies/:replyId/rereplies");
    public static String buildGetReReplies(long replyId){
        return GET_REREPLIES.replace(":replyId", Long.toString(replyId));
    }

    ///////////////////////////////////////////

    ////////  GROUP  UrlData //////////////
    private static final String GET_GROUPS = ACCESS_BASE_URL.concat("/users/:userId/groups/following");
    public static String buildGetGroupList(long userId){
        return GET_GROUPS.replace(":userId", Long.toString(userId));
    }

    private static final String GET_GROUP_INFO = ACCESS_BASE_URL.concat("/groups/:groupId");
    public static String buildGetGroupInfo(long groupId){
        return GET_GROUP_INFO.replace(":groupId", Long.toString(groupId));
    }
    public static final String POST_NEW_GROUP = ACCESS_BASE_URL.concat("/groups");
    ///////////////////////////////////////////

    ////////  SEARCH  UrlData //////////////
    public static final String GET_SEARCH_TOP = ACCESS_BASE_URL.concat("/search/top");
    public static final String GET_SEARCH_USERS = ACCESS_BASE_URL.concat("/search/users");
    public static final String GET_SEARCH_GROUPS = ACCESS_BASE_URL.concat("/search/groups");
    public static final String GET_SEARCH_BULLETINS = ACCESS_BASE_URL.concat("/search/posts");
    public static final String GET_SEARCH_AUTOCOMPLETE = ACCESS_BASE_URL.concat("/autocomplete");
    ///////////////////////////////////////////

    //////// NOTIFICATION UrlData /////////////
    public static final String GET_NOTIFICATIONS = ACCESS_BASE_URL.concat("/notifications");
    ///////////////////////////////////////////

}

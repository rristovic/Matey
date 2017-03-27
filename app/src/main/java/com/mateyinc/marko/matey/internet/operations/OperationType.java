package com.mateyinc.marko.matey.internet.operations;


/**
 * Operation type that can be used in {@link Operations}
 */
public enum OperationType {

    // Newsfeed operation
    DOWNLOAD_NEWS_FEED,
//    DOWNLOAD_NEWS_FEED_NEXT,
//    DOWNLOAD_NEWS_FEED_NEW,

    // Bulletin operation
    POST_NEW_BULLETIN_NO_ATTCH,
    POST_NEW_BULLETIN_WITH_ATTCH,
    DOWNLOAD_BULLETIN,

    // User profile operations
    DOWNLOAD_USER_PROFILE,
    FOLLOW_USER_PROFILE,
    UNFOLLOW_USER_PROFILE,
    DOWNLOAD_FOLLOWERS,

    // Approve operations
    POST_LIKED,
    POST_UNLIKED,
    REPLY_UNLIKED,
    REPLY_LIKED,
    RE_REPLY_LIKED,
    RE_REPLY_UNLIKED,

    // Reply operations
    REPLY_ON_POST,
    REPLY_ON_REPLY,
    DOWNLOAD_RE_REPLIES,

    // Group operations
    DOWNLOAD_GROUP_LIST,
    DOWNLOAD_GROUP_INFO,
    DOWNLOAD_GROUP_ACTIVITY_LIST,
    POST_NEW_GROUP,

    // Search ops
    SEARCH_TOP,
    SEARCH_BULLETINS,
    SEARCH_GROUPS,
    SEARCH_PROFILES,
    SEARCH_AUTOCOMPLETE,

    // Notification ops
    DOWNLOAD_NOTIF_LIST,


    NO_OPERATION
}

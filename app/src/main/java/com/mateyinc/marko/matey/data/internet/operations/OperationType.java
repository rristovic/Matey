package com.mateyinc.marko.matey.data.internet.operations;


/**
 * Operation type that can be used in {@link Operations}
 */
public enum OperationType {
    // Bulletin operations
    DOWNLOAD_NEWS_FEED,
    POST_NEW_BULLETIN_NO_ATTCH,
    POST_NEW_BULLETIN_WITH_ATTCH,

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

    // Reply operations
    REPLY_ON_POST,
    REPLY_ON_REPLY,

    NO_OPERATION
}

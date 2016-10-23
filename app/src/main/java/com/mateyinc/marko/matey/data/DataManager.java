package com.mateyinc.marko.matey.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataContract.NotificationEntry;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Message;
import com.mateyinc.marko.matey.model.Notification;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

/**
 * The manager for the data entries
 */
public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    public static final int CACHE_SIZE_MB = 100; // Max cache size on disk for storing data
    /**
     * Number of bulletins to download from the server
     */
    public static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /**
     * the current count of the friends list in the database
     */
    public static int mFriendsListCount = 0; // for dummy data is 120

    /**
     * The current page of bulletins in the database
     */
    public static int mCurrentPage = 0;

    // One day in milliseconds
    public static final int ONE_DAY = 86400000;
    // One minute in milliseconds
    public static final int ONE_MIN = 60000;

    /**
     * Loader identifiers for data
     */
    public static final int BULLETINS_LOADER = 100;
    public static final int BULLETIN_LOADER = 101;
    public static final int REPLIES_LOADER = 200;

    /**
     * Columns that bulletin cursor loaders will load up
     */
    public static final String[] BULLETIN_COLUMNS = {
            DataContract.BulletinEntry.TABLE_NAME + "." + DataContract.BulletinEntry.COLUMN_POST_ID,
            DataContract.BulletinEntry.COLUMN_USER_ID,
            DataContract.BulletinEntry.COLUMN_FIRST_NAME,
            DataContract.BulletinEntry.COLUMN_LAST_NAME,
            DataContract.BulletinEntry.COLUMN_TEXT,
            DataContract.BulletinEntry.COLUMN_DATE,
            DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES,
            DataContract.BulletinEntry.COLUMN_ATTACHMENTS
    };

    // These indices are tied to BULLETIN_COLUMNS.  If BULLETIN_COLUMNS changes, these
    // must change.
    public static final int COL_POST_ID = 0;
    public static final int COL_USER_ID = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_LAST_NAME = 3;
    public static final int COL_TEXT = 4;
    public static final int COL_DATE = 5;
    public static final int COL_NUM_OF_REPLIES = 6;
    public static final int COL_ATTCHS = 7;


    public final ArrayList<Notification> mNotificationList = new ArrayList<>();
    public final ArrayList<Message> mMessageList;

    // JSON array names
    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.internet.home.bulletins_loaded_count";

    // Global instance fields
    private static final Object mLock = new Object(); // for synchronised blocks
    private static DataManager mInstance = null;
    private final Context mAppContext;

    private static UserProfile mCurrentUserProfile;

    /**
     * Method for retrieving DataManager singleton
     *
     * @param context the context of calling class
     * @return the DataManager instance
     */
    public static DataManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                if (mInstance.mCurrentUserProfile == null) {

                }
                Log.d("DataManager", "New instance of manager created.");
            }
            return mInstance;
        }
    }

    private DataManager(Context context) {
        mAppContext = context;
        mMessageList = new ArrayList<>();
    }

    public static void setFriendsListCount(int numOfFriends) {
        mFriendsListCount = numOfFriends;
    }

    // UserProfile methods /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Key for storing user prfile id in shared prefs
     */
    public static final String CUR_USERPROFILE_ID = "cur_userprofile_id";

    /**
     * Helper method for storing current user profile in memory, and in shared prefs.
     * @param preferences the Shared Preferences object used to store user_id
     * @param userProfile the current user profile
     */
    public static void setCurrentUserProfile(SharedPreferences preferences, UserProfile userProfile) {
        mCurrentUserProfile = userProfile;
        preferences.edit().putInt(DataManager.CUR_USERPROFILE_ID, userProfile.getUserId()).apply();

        if (userProfile != null)
            mFriendsListCount = userProfile.getNumOfFriends();
    }

    public static synchronized UserProfile getCurrentUserProfile() {
        return mCurrentUserProfile;
    }

    /**
     * Helper method to handle the insertion of a new user profile to the db and the server
     *
     * @param userId       id of the user
     * @param userName     name of the user
     * @param userLastName last name of the user
     * @param lastMsgId    last message id that the user has sent to the current user
     * @return the row ID of the added user profile.
     */
    public long addUserProfile(int userId, String userName, String userLastName, String email, String picture, int lastMsgId, boolean isFriend) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ProfileEntry.CONTENT_URI,
                new String[]{DataContract.ProfileEntry._ID},
                DataContract.ProfileEntry._ID + " = " + userId,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            updateUserProfile(userId, userName, userLastName, email, picture, lastMsgId, isFriend);
        } else {
            ContentValues userValues = new ContentValues();

            userValues.put(DataContract.ProfileEntry._ID, userId);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, userName);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, userLastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
            userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, picture);
            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, isFriend?1:0);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);

            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.ProfileEntry.CONTENT_URI,
                    userValues
            );

            if (null == insertedUri) {
                Log.e(TAG, "Error inserting UserProfile: ID=" + userId + "; Name=" + userName + "; Last name=" + userLastName);
            } else {
                Log.d("DataManager", "UserProfile added: ID=" + userId +
                        "; Name=" + userName + "; LastName=" + userLastName + "; LastMsgId=" + lastMsgId);
            }
        }
        if (cursor != null)
            cursor.close();

        return userId;
    }

    public void addUserProfile(UserProfile profile) {
        addUserProfile(profile.getUserId(), profile.getFirstName(), profile.getLastName(), profile.getEmail(), profile.getProfilePictureLink(), profile.getLastMsgId(), profile.isFriend());
    }

    /**
     * Method for changing user profile in db
     *
     * @param userId       id of the user
     * @param userName     name of the user
     * @param userLastName last name of the user
     * @param lastMsgId    last message id that the user has sent to the current user
     */
    public void updateUserProfile(int userId, String userName, String userLastName, String email, String picture, int lastMsgId, boolean isFriend) {
        ContentValues userValues = new ContentValues();

        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);
        userValues.put(DataContract.ProfileEntry.COLUMN_NAME, userName);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, userLastName);
        userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
        userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, isFriend?1:0);
        userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, picture);

        int numOfUpdated = mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, userValues,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(userId)});

        if (numOfUpdated != 1) {
            Log.e(TAG, "Error setting UserProfile: ID=" + userId + "; Name=" + userName + "; Last name=" + userLastName + "; Number of updated rows=" + numOfUpdated);
        } else
            Log.d("DataManager", "UserProfile changed: ID=" + userId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; LastMsgId=" + lastMsgId);
    }

    /**
     * Returns user profile from db
     *
     * @param index position of user profile in database
     * @return new instance of UserProfile from database
     */
    public UserProfile getUserProfile(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ProfileEntry.CONTENT_URI,
                new String[]{DataContract.ProfileEntry._ID, DataContract.ProfileEntry.COLUMN_NAME,
                        DataContract.ProfileEntry.COLUMN_LAST_NAME, DataContract.ProfileEntry.COLUMN_LAST_MSG_ID},
                null,
                null,
                null);

        UserProfile profile = null;
        try {
            cursor.moveToPosition(index);

            profile = new UserProfile();
            profile.setUserId(cursor.getInt(0));
            profile.setFirstName(cursor.getString(1));
            profile.setLastName(cursor.getString(2));
            profile.setLastMsgId(cursor.getInt(3));

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        } finally {
            try {
                cursor.close();
            } catch (Exception e) {
            }
        }

        return profile;
    }

    public void removeUserProfile(int user_id) {
        int numOfRows = mAppContext.getContentResolver().delete(DataContract.ProfileEntry.CONTENT_URI,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(user_id)});

        if (numOfRows == 1) {
            Log.d(TAG, "User profile with id=" + user_id + " has been successfully removed from db.");
        } else {
            Log.d(TAG, "Error deleting user profile with id=" + user_id);
        }
    }

    public void removeUserProfile(UserProfile userProfile) {
        removeUserProfile(userProfile.getUserId());
    }


    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Notification methods ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Helper method to handle insertion of a new notification in the database.
     *
     * @param senderId   profile id of user that generated the notification
     * @param senderName profile name of user
     * @param body       notification text
     * @param time       that notification has been created
     * @param linkId     id of the post that generated the notification
     * @return the row ID of the added notification.
     */

    public long addNotif(int senderId, String senderName, String body, String time, String linkId) {
        long notifId;

        Cursor cursor = mAppContext.getContentResolver().query(
                NotificationEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                NotificationEntry.COLUMN_NOTIF_LINK_ID + " = " + "\"" + linkId + "\"",// TODO - change selection
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            setNotification(senderId, senderName, body, time, linkId);
            int notifIdIndex = cursor.getColumnIndex(DataContract.MessageEntry._ID);
            notifId = cursor.getLong(notifIdIndex);
        } else {
            ContentValues values = new ContentValues();

            values.put(NotificationEntry.COLUMN_SENDER_ID, senderId);
            values.put(NotificationEntry.COLUMN_SENDER_NAME, senderName);
            values.put(NotificationEntry.COLUMN_NOTIF_TEXT, body);
            values.put(NotificationEntry.COLUMN_NOTIF_TIME, time);
            values.put(NotificationEntry.COLUMN_NOTIF_LINK_ID, linkId);

            Uri insertedUri = mAppContext.getContentResolver().insert(
                    NotificationEntry.CONTENT_URI,
                    values);

            notifId = ContentUris.parseId(insertedUri);

            if (null == insertedUri) {
                Log.d("DataManager", "Error inserting notification: sender id=" + senderId +
                        "; Sender name=" + senderName + "; Text=" + body + "; Time=" + time);
            } else {
                Log.d("DataManager", "Notification added: sender id=" + senderId +
                        "; Sender name=" + senderName + "; Text=" + body + "; Time=" + time);
            }
        }
        if (cursor != null)
            cursor.close();

        return notifId;
    }

    private void setNotification(int senderId, String senderName, String body, String time, String linkId) {
        // TODO - finish method
    }
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Message methods /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Helper method to handle insertion of a new message in the database.
     *
     * @param senderId   profile id of user that generated the message
     * @param senderName profile name of user
     * @param body       of the message
     * @param time       that message has been created
     * @param isRead     if message is read-1 or not-0
     * @return the row ID of the added message.
     */
    public long addMessage(int senderId, String senderName, String body, String time, boolean isRead) {
        long msgId;

        // TODO - check if exist in db then add it
// First, check if the location with this city name exists in the db
        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.MessageEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                DataContract.MessageEntry.COLUMN_MSG_BODY + " = " + "\"" + body + "\"",// TODO - change selection
                null,
                null);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            int msgIdIndex = msgCursor.getColumnIndex(DataContract.MessageEntry._ID);
            msgId = msgCursor.getLong(msgIdIndex);

        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues msgValues = new ContentValues();
            ContentValues profileValues = new ContentValues();


            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_ID, senderId);
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_NAME, senderName);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_BODY, body);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_TIME, time);
            msgValues.put(DataContract.MessageEntry.COLUMN_IS_READ, isRead ? 1 : 0);

            // Finally, insert data into the database.
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.MessageEntry.CONTENT_URI,
                    msgValues
            );

            // The resulting URI contains the ID for the row.  Extract the msgId from the Uri.
            msgId = ContentUris.parseId(insertedUri);

            profileValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, msgId);
            mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, profileValues,
                    DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(senderId)});

        }

        if (msgCursor != null)
            msgCursor.close();

        return msgId;
    }
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Bulletins methods ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    public static final String BULLETIN_ORDER = DataContract.BulletinEntry.COLUMN_DATE + " DESC";

    /**
     * Method for adding empty Bulletin to first row into the database, called on first launch of the app;
     * NOTE: It must be added, doesn't matter when;
     */
    public void addNullBulletin() {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = ?",
                new String[]{"-1"},
                null);

        if (cursor != null && !cursor.moveToFirst()) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_DATE, new Date().getTime());

            mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            Log.d(TAG, "Null bulletin added.");
        } else {
            updateNullBulletin();
        }

        if (cursor != null)
            cursor.close();
    }

    public void updateNullBulletin() {
        ContentValues values = new ContentValues(1);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, new Date().getTime());
        mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values, DataContract.BulletinEntry.COLUMN_POST_ID + " = -1",
                null);

        Log.d(TAG, "Null bulletin updated.");
    }


    /**
     * Method for adding list of Bulletin to database
     *
     * @param list to be added
     */
    public void addBulletins(ArrayList<Bulletin> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);

        for (Bulletin b : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, b.getPostID());
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, b.getUserID());
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, b.getFirstName());
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, b.getLastName());
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, b.getMessage());
            values.put(DataContract.BulletinEntry.COLUMN_DATE, b.getDate().getTime());
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, b.getNumOfReplies());
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(b.getAttachments()));

            cVVector.add(values);
            Log.d(TAG, "Bulletin added: " + b.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            updateNullBulletin();
            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " bulletins added");
    }

    /**
     * Inserting new bulletin into the database
     *
     * @param b bulletin to insert
     */
    public void addBulletin(Bulletin b) {
        addBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(),
                b.getDate(), b.getNumOfReplies(), b.getAttachments());
    }

    /**
     * Helper method to handle the insertion of new bulletin
     *
     * @param postId       bulletin id from the server
     * @param userId       id of user that posted
     * @param userName     name of user
     * @param userLastName last name of user
     * @param text         post text
     * @param date         post date in UTC
     * @param numOfReplies the number of replies
     * @param attachments  list of post attachments
     * @return the row ID of the added bulletin.
     */
    public void addBulletin(int postId, int userId, String userName, String userLastName, String text, Date date,
                            int numOfReplies, LinkedList<Bulletin.Attachment> attachments) {

        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + postId,
                null,
                BULLETIN_ORDER);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            updateBulletin(postId, userId, userName, userLastName, text, date, numOfReplies, attachments);
        } else {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, postId);
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
            values.put(DataContract.BulletinEntry.COLUMN_DATE, date.getTime());
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, numOfReplies);
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));

            updateNullBulletin();
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            if (insertedUri == null) {
                Log.e(TAG, "Error inserting Bulletin: ID=" + postId + "; UserID=" + userId + "; Text=" + text);
            } else {
                String debugtext = "Bulletin added: ID=" + postId +
                        "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text
                        + "...; Date=" + date;
                debugtext += "; Num of replies=" + numOfReplies;
                debugtext += "; Num of attachments=";
                debugtext += attachments == null ? '0' : Integer.toString(attachments.size());
                Log.d(TAG, debugtext);
            }
        }
        if (msgCursor != null)
            msgCursor.close();
    }

    /**
     * Method for changing bulletin in db
     */
    public void updateBulletin(int postId, int userId, String userName, String userLastName, String text, Date date,
                               int numOfReplies, LinkedList<Bulletin.Attachment> attachments) {

        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, date.getTime());
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, numOfReplies);
        values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));

        updateNullBulletin();
        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry.COLUMN_POST_ID + " = ?", new String[]{Integer.toString(postId)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error setting bulletin: PostID=" + postId + "; UserID=" + userId + "; Number of rows updated=" + numOfUpdatedRows);
        } else {
            String debugtext = "Bulletin added: ID=" + postId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text
                    + "...; Date=" + date;
            debugtext += "; Num of replies=" + numOfReplies;
            debugtext += "; Num of attachments=";
            debugtext += attachments == null ? '0' : Integer.toString(attachments.size());

            Log.d("BulletinManager", debugtext);
        }
    }


    /**
     * Method for getting the bulletin from the database
     *
     * @param index  the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index, Cursor cursor) {
        Bulletin bulletin = new Bulletin();
        try {
            cursor.moveToPosition(index);

            bulletin.setUserID(cursor.getInt(COL_USER_ID));
            bulletin.setPostID(cursor.getInt(COL_POST_ID));
            bulletin.setFirstName(cursor.getString(2));
            bulletin.setLastName(cursor.getString(3));
            bulletin.setMessage(cursor.getString(4));
            bulletin.setDate(cursor.getLong(5));
            bulletin.setNumOfReplies(cursor.getInt(6));
            bulletin.setAttachmentsFromJSON(cursor.getString(7));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return bulletin;
    }

    /**
     * Method for getting the bulletin from the database with the default cursor
     *
     * @param index the position of the bulletin in the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                BULLETIN_COLUMNS,
                null,
                null,
                BULLETIN_ORDER);

        Bulletin bulletin = null;
        try {
            bulletin = getBulletin(index, cursor);

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }

        if (cursor != null)
            cursor.close();

        return bulletin;
    }

    public void addReplyApprove(int mBulletinPos, int replyId) {
        // TODO - finish method
    }

    public void removeReplyApprove(int mBulletinPos, int replyId) {
        // TODO - finish method
    }

//    public void updateBulletinReplies(Bulletin mCurBulletin) {
//        ContentValues values = new ContentValues();
//        values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(mCurBulletin.getReplies()));
//
//        int updated = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values, DataContract.BulletinEntry.COLUMN_POST_ID
//                + " = " + mCurBulletin.getPostID(), null);
//        if (updated > 0) {
//            Log.d(TAG, "Bulletin updated with postId=" + mCurBulletin.getPostID());
//        } else
//            Log.e(TAG, "Failed to updated bulletin with postId=" + mCurBulletin.getPostID());
//    }

    /**
     * Helper method for preparing bulletin for insertion into the db
     *
     * @param b bulletin to insert
     * @return prepared values from bulletin
     */
//    private ContentValues parseBulletin(Bulletin b) {
//        return parseBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(), b.getDate().toString(),
//                b.getReplies(), b.getAttachments());
//    }

    /**
     * Helper method for preparing bulletin for insertion into the db
     *
     * @return prepared values from bulletin
     */
//    private ContentValues parseBulletin(int postId, int userId, String userName, String userLastName, String text, String date,
//                                        LinkedList<Bulletin.Reply> replies, LinkedList<Bulletin.Attachment> attachments) {
//        ContentValues values = new ContentValues();
//        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
//        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
//        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
//        values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
//        values.put(DataContract.BulletinEntry.COLUMN_DATE, date);
//        values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(replies));
//        values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));
//
//        return values;
//    }

    /**
     * Method for parsing Reply list to JSON String format;
     *
     * @param attachments LinkedList of Attachemnt objects
     * @return JSON formatted string
     */
    private String parseAttachmentsToJSON(LinkedList<Bulletin.Attachment> attachments) {
        if (attachments == null || attachments.size() == 0)
            return "";
        return ""; // TODO - finish method
    }


    ////// Replies methods ///////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////

    /**
     * Cursor sort order
     */
    public static final String REPLIES_ORDER_BY = DataContract.ReplyEntry.COLUMN_DATE + " DESC";

    /**
     * Method for adding list of Replies to the database
     *
     * @param list the list to be added to the db
     */
    public void addReplies(LinkedList<Bulletin.Reply> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(list.size());

        int numReplies = 0;
        for (Bulletin.Reply r : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.ReplyEntry.COLUMN_REPLY_ID, r.replyId);
            values.put(DataContract.ReplyEntry.COLUMN_POST_ID, r.postId);
            values.put(DataContract.ReplyEntry.COLUMN_USER_ID, r.userId);
            values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, r.userFirstName);
            values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, r.userLastName);
            values.put(DataContract.ReplyEntry.COLUMN_TEXT, r.replyText);
            values.put(DataContract.ReplyEntry.COLUMN_DATE, r.replyDate.getTime());
            values.put(DataContract.ReplyEntry.COLUMN_APPRVS, parseApprovesToJSON(r.replyApproves));
            values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, r.numOfApprvs);

            cVVector.add(values);
            Log.d(TAG, "Reply added: " + r.toString());
            numReplies++;
        }


        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.ReplyEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " replies added");
    }

    /**
     * Helper method to handle the insertion of the new reply
     *
     * @param reply the reply to be inserted into the database
     */
    public void addReply(Bulletin.Reply reply) {
        addReply(reply.replyId, reply.userId, reply.postId, reply.userFirstName, reply.userLastName, reply.replyText, reply.replyDate
                , reply.replyApproves.size(), reply.replyApproves);
    }

    /**
     * Helper method to handle the insertion of the new reply
     *
     * @param replyId     the id of the new reply
     * @param userId      the id of the user that has replied
     * @param postId      the id of the post that has been replied on
     * @param firstName   user's first name
     * @param lastName    user's last name
     * @param text        the message of the reply
     * @param date        the date of the reply in UTC
     * @param numOfApprvs the number of the reply approves
     * @param approves    the list of approves
     */
    public void addReply(int replyId, int userId, int postId, String firstName, String lastName, String text, Date date,
                         int numOfApprvs, LinkedList<UserProfile> approves) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry.COLUMN_REPLY_ID, replyId);
        values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, firstName);
        values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, lastName);
        values.put(DataContract.ReplyEntry.COLUMN_TEXT, text);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, date.getTime());
        values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, numOfApprvs);
        values.put(DataContract.ReplyEntry.COLUMN_APPRVS, parseApprovesToJSON(approves));

        Uri insertedUri = mAppContext.getContentResolver().insert(
                DataContract.ReplyEntry.CONTENT_URI,
                values
        );

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting reply: ID=" + replyId + "; UserID=" + userId + "; Text=" + text.substring(0, 30) + "...");
        } else {
            String debugtext = "Reply added: ID=" + replyId +
                    "; Name=" + firstName + "; LastName=" + lastName + "; Text=" + text
                    + "...; Date=" + date;
            debugtext += "; Num of approves=" + numOfApprvs;
            Log.d(TAG, debugtext);
        }
    }

    private void updateReply(int replyId, int userId, int postId, String firstName, String lastName, String text, Date date, int numOfApprvs, LinkedList<UserProfile> approves) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry.COLUMN_REPLY_ID, replyId);
        values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, firstName);
        values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, lastName);
        values.put(DataContract.ReplyEntry.COLUMN_TEXT, text);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, date.getTime());
        values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, numOfApprvs);
        values.put(DataContract.ReplyEntry.COLUMN_APPRVS, parseApprovesToJSON(approves));


        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.ReplyEntry.CONTENT_URI, values,
                DataContract.ReplyEntry.COLUMN_REPLY_ID + " = ?", new String[]{Integer.toString(replyId)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error updating reply: replyId=" + replyId + "; UserID=" + userId + "; Number of rows updated=" + numOfUpdatedRows);
        } else {
            String debugtext = "Reply updated: ID=" + replyId +
                    "; Name=" + firstName + "; LastName=" + lastName + "; Text=";
            try {
                debugtext += text.substring(0, 30);
            } catch (Exception e) {
                debugtext += text;
            }
            debugtext += "...; Date=" + date;
            debugtext += "; Num of approves=" + numOfApprvs;
            Log.d(TAG, debugtext);
        }
    }

    /**
     * Method for getting the reply from the database
     *
     * @param index  the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin.Reply getReply(int index, Cursor cursor) {
        Bulletin.Reply reply = new Bulletin().getReplyInstance();

        try {
            cursor.moveToPosition(index);

            reply.replyId = cursor.getInt(BulletinViewActivity.COL_REPLY_ID);
            reply.userId = cursor.getInt(BulletinViewActivity.COL_USER_ID);
            reply.postId = cursor.getInt(BulletinViewActivity.COL_POST_ID);
            reply.userFirstName = cursor.getString(BulletinViewActivity.COL_FIRST_NAME);
            reply.userLastName = cursor.getString(BulletinViewActivity.COL_LAST_NAME);
            reply.setDate(cursor.getLong(BulletinViewActivity.COL_DATE));
            reply.replyText = cursor.getString(BulletinViewActivity.COL_TEXT);
            reply.numOfApprvs = cursor.getInt(BulletinViewActivity.COL_NUM_OF_APPRVS);
            reply.replyApproves = reply.setApprovesFromJSON(cursor.getString(BulletinViewActivity.COL_APPRVS));

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return reply;
    }

    /**
     * Method for getting the bulletin from the database with the default cursor
     *
     * @param index the position of the bulletin in the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin.Reply getReply(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ReplyEntry.CONTENT_URI,
                BulletinViewActivity.REPLIES_COLUMNS,
                null,
                null,
                REPLIES_ORDER_BY);

        Bulletin.Reply reply;
        try {
            reply = getReply(index, cursor);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }

        if (cursor != null)
            cursor.close();

        return reply;
    }

    private String parseApprovesToJSON(LinkedList<UserProfile> replyApproves) {
        if (replyApproves == null || replyApproves.size() == 0)
            return "";

        JSONObject jObject = new JSONObject();
        try {
            JSONArray replyApprvs = new JSONArray();
            for (UserProfile profile : replyApproves) {
                JSONObject apprvJson = new JSONObject();
                apprvJson.put(UserProfile.USER_ID, profile.getUserId());

                replyApprvs.put(apprvJson);
            }
            jObject.put(REPLY_APPRVS, replyApprvs);
        } catch (JSONException jse) {
            Log.e(TAG, jse.getLocalizedMessage(), jse);
        }

        return jObject.toString();
    }


}

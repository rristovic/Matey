package com.mateyinc.marko.matey.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataContract.*;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

/**
 * The manager for the data entries
 */
public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    public static final int CACHE_SIZE_MB = 100; // Max cache size on disk for storing data

    /** Number of bulletins to download from the server */
    private static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /** The current page of bulletins in the database */
    public static int mCurrentPage = 0;

    // One day in milliseconds
    private static final int ONE_DAY = 86400000;
    // One minute in milliseconds
    public static final int ONE_MIN = 60000;

    /** Loader identifiers for data */
    public static final int BULLETINS_LOADER = 100;
    public static final int BULLETIN_LOADER = 101;
    public static final int REPLIES_LOADER = 200;

    /** Columns that bulletin cursor loaders will load up */
    public static final String[] BULLETIN_COLUMNS = {
            DataContract.BulletinEntry.TABLE_NAME + "." + DataContract.BulletinEntry._ID,
            DataContract.BulletinEntry.COLUMN_USER_ID,
            DataContract.BulletinEntry.COLUMN_FIRST_NAME,
            DataContract.BulletinEntry.COLUMN_LAST_NAME,
            DataContract.BulletinEntry.COLUMN_TEXT,
            DataContract.BulletinEntry.COLUMN_DATE,
            DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES,
            DataContract.BulletinEntry.COLUMN_SERVER_STATUS,
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
    public static final int COL_ON_SERVER = 7;
    public static final int COL_ATTCHS = 8;

    /** Activity upload status that is saved in database, and used for UI control */
    public static final int STATUS_RETRY_UPLOAD = -1;
    /** @see #STATUS_RETRY_UPLOAD */
    public static final int STATUS_UPLOADING = 0;
    /**
     * Activity upload status that is saved in database, and used for UI control;
     * If data is downloaded from the server, this is the status that gets saved
     */
    public static final int STATUS_SUCCESS = 1;

    /** Used for new post that doesn't yet have the post_id */
    public static final int NO_POST_ID = -1;


    // JSON array names
    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOAD_FAILED = "com.mateyinc.marko.matey.internet.home.bulletins_load_failed";
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.internet.home.bulletins_loaded_count";

    // Global instance fields
    private  final Object mLock = new Object(); // for synchronised blocks
    private static DataManager mInstance ;
    private Context mAppContext;

    private UserProfile mCurrentUserProfile;
    private int newPostID;

    /**
     * Method for retrieving DataManager singleton
     *
     * @param context the context of calling class
     * @return the DataManager instance
     */
    public static synchronized DataManager getInstance(Context context) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                mInstance.addNullBulletin();
                Log.d(TAG, "New instance of DataManager created.");
            }

            return mInstance;
    }

    private DataManager(Context context) {
        mAppContext = context;
    }


    //  General methods ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    private static final int CLASS_BULLETIN = 0;
    private static final int CLASS_USERPROFILE = 1;
    private static final int CLASS_REPLY = 2;


    /**
     * Helper method for adding new activity into the database in table {@link DataContract.NotUploadedEntry} so it can be uploaded later
     * @param activityObject the object that needs to be reuploaded
     */
    private void addNotUploadedActivity(Object activityObject, int objectType){
        long id;

        switch (objectType){
            case CLASS_BULLETIN:{
                id = ((Bulletin) activityObject).getPostID();
                break;
            }
            case CLASS_USERPROFILE:{
                id = ((UserProfile) activityObject).getUserId();
                break;
            }
            case CLASS_REPLY:{
                id = ((Bulletin.Reply) activityObject).replyId;
                break;
            }
            default:
                return;
        }


        // TODO - finish method later
        ContentValues values = new ContentValues(2);
        values.put(NotUploadedEntry._ID, id);
        values.put(NotUploadedEntry.COLUMN_ENTRY_TYPE, objectType);

        Uri uri;
        uri = mAppContext.getContentResolver().insert(NotUploadedEntry.CONTENT_URI, values);

        if (null == uri) {
            Log.e(TAG, "Failed to insert object in NotUploaded table with id=" + id + "; object type=" + objectType);
        }else{
            Log.d(TAG, "Object inserted in NotUploaded table with id=" + id + "; object type=" + objectType);
        }
    }

    /** Method for creating new activity_id, which is later replaced by the id returned from the server */
    public long getNewActivityId() {
        Cursor c = mAppContext.getContentResolver().query(NotUploadedEntry.CONTENT_URI, null, null, null, null);
        long id = 0;

        if (c != null) {
            id = -(c.getCount() + 1);
            c.close();
        }

        return id;
    }
    ////////////////////////////////////////////////////////////////


    // UserProfile methods /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /** Key for storing user pref file id in shared prefs */
    public static final String KEY_CUR_USER_ID = "cur_userprofile_id";

    /**
     * Method for storing current user profile in memory, and in shared prefs.
     *
     * @param preferences the Shared Preferences object used to store user_id
     * @param userProfile the current user profile
     */
    public void setCurrentUserProfile(SharedPreferences preferences, UserProfile userProfile) {
        mCurrentUserProfile = userProfile;

        if (userProfile != null) {
            preferences.edit().putLong(DataManager.KEY_CUR_USER_ID, userProfile.getUserId()).apply();
            MotherActivity.user_id = mCurrentUserProfile.getUserId();
            Log.d(TAG, "Current user updated.");
        } else {
            preferences.edit().remove(DataManager.KEY_CUR_USER_ID).commit();
            Log.d(TAG, "Current user removed from prefs.");
        }
    }

    /**
     * Set's the {@link #mCurrentUserProfile} from {@link SharedPreferences}
     * @param preferences the SharedPreferences object provided to retrieve the current user profile id;
     * NOTE: Method is synchronizes because everything needs to wait for the current user to set up
     * @return true if settings the current user profile was successful
     */
    public synchronized boolean setCurrentUserProfile(SharedPreferences preferences) {
        if  (mCurrentUserProfile != null)
            return true;

        long id = preferences.getLong(DataManager.KEY_CUR_USER_ID, -1);

        Cursor c = mAppContext.getContentResolver().query(ProfileEntry.CONTENT_URI, null,
                ProfileEntry._ID + " = ?", new String[]{Long.toString(id)}, null);

        if (c != null)
            if (c.moveToFirst()) {
                mCurrentUserProfile = new UserProfile(id, c.getString(c.getColumnIndex(ProfileEntry.COLUMN_NAME)),
                        c.getString(c.getColumnIndex(ProfileEntry.COLUMN_LAST_NAME)),
                        c.getString(c.getColumnIndex(ProfileEntry.COLUMN_EMAIL)),
                        c.getString(c.getColumnIndex(ProfileEntry.COLUMN_PICTURE)));

                Log.d(TAG, "Current user profile updated with user_id=" + id);
                c.close();
            } else {
                Log.e(TAG, "Error setting current user profile from shared prefs.");
                c.close();
                return false;
        } else
            return false;

        return true;
    }

    /**
     * Sets the {@link #mCurrentUserProfile} to the user profile provided in method param
     * @param userProfile the current user profile used in this session
     */
    public void setCurrentUserProfile(UserProfile userProfile) {
        mCurrentUserProfile = userProfile;
    }

    /**
     * Return {@link #mCurrentUserProfile}
     * @return current {@link UserProfile} object
     */
    public UserProfile getCurrentUserProfile() {
        return mCurrentUserProfile;
    }

    /**
     * Gets the current user profile id from the {@link SharedPreferences}
     * @param preferences shared preferences object used to retrieve id
     * @return the current user id
     */
    public long getCurrentUserProfileIdFromPrefs(SharedPreferences preferences) {
        return preferences.getLong(DataManager.KEY_CUR_USER_ID, -1);
    }

    private LinkedList<UserProfile> suggestedFriends;

    public LinkedList<UserProfile> getSuggestedFriendsList() {
        return suggestedFriends;
    }

    public void setSuggestedFriends(LinkedList<UserProfile> list) {
        suggestedFriends = list;
    }


    /**
     * Method for adding list of user profile to the database
     *
     * @param list the list of user profiles ready for the database
     * @param areFollowed indicates if these profiles are followed by the current user
     */
    public void addUserProfiles(ArrayList<UserProfile> list, boolean areFollowed) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);

        for (UserProfile profile : list) {
            ContentValues userValues = new ContentValues();

            userValues.put(DataContract.ProfileEntry._ID, profile.getUserId());
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, profile.getFirstName());
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, profile.getLastName());
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, profile.getEmail());
            userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, profile.getProfilePictureLink());
            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, profile.isFriend() ? 1 : 0);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, profile.getLastMsgId());
            if(areFollowed)
                userValues.put(ProfileEntry.COLUMN_IS_FRIEND, true);

            cVVector.add(userValues);
            Log.d(TAG, "Bulletin added: " + userValues.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.ProfileEntry.CONTENT_URI, cvArray);
            // TODO - delete old data
        }
        Log.d(TAG, inserted + " user profile added.");
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
    public long addUserProfile(long userId, String userName, String userLastName, String email, String picture, int lastMsgId, boolean isFriend) {
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
            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, isFriend);
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
    public void updateUserProfile(long userId, String userName, String userLastName, String email, String picture, int lastMsgId, boolean isFriend) {
        ContentValues userValues = new ContentValues();

        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);
        userValues.put(DataContract.ProfileEntry.COLUMN_NAME, userName);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, userLastName);
        userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
        userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, isFriend);
        userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, picture);

        int numOfUpdated = mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, userValues,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Long.toString(userId)});

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

    public void removeUserProfile(long user_id) {
        int numOfRows = mAppContext.getContentResolver().delete(DataContract.ProfileEntry.CONTENT_URI,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Long.toString(user_id)});

        if (numOfRows == 1) {
            Log.d(TAG, "User profile with id=" + user_id + " has been successfully removed from db.");
        } else {
            Log.d(TAG, "Error deleting user profile with id=" + user_id);
        }
    }

    public void removeUserProfile(UserProfile userProfile) {
        removeUserProfile(userProfile.getUserId());
    }

    public void updateProfileServerStatus(UserProfile userProfile, int serverStatus){
        if(STATUS_RETRY_UPLOAD == serverStatus){
            addNotUploadedActivity(userProfile, CLASS_USERPROFILE);
        }

        ContentValues values = new ContentValues();
        values.put(ProfileEntry.COLUMN_SERVER_STATUS, serverStatus);

        int numOfUpdatedRows = mAppContext.getContentResolver().update(ProfileEntry.CONTENT_URI, values,
                ProfileEntry._ID + " = ?", new String[]{Long.toString(userProfile.getUserId())});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error updating bulletin: PostID=" + userProfile.getUserId());
        } else {
            String debugtext = "Bulletin updated: ID=" + userProfile.getUserId();
            Log.d("BulletinManager", debugtext);
        }
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
    public static final int NUM_OF_BULLETINS_TO_DOWNLOAD = 40;

    /**
     * Method for updating a new post_id, created with {@link DataManager#getNewActivityId()}, with the new
     * one returned from the server;
     *
     * NOTE: Should only be called when post_id is retrieved from the server, because it's updating the
     * {@link Bulletin#mServerStatus} to {@value #STATUS_SUCCESS}
     *
     * @param oldPost_id the old post_id to be updated
     * @param newPost_id the new post_id from the server
     */
    public void updateBulletinPostId(long oldPost_id, long newPost_id) {
        ContentValues values = new ContentValues(2);
        values.put(DataContract.BulletinEntry._ID, newPost_id);
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, STATUS_SUCCESS);

        int numOfRows = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI,
                values, DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(oldPost_id)});

        if (numOfRows == 1) {
            Log.d(TAG, "Bulletin update with new post_id=" + newPost_id);
        } else {
            Log.e(TAG, "Failed to update the bulletin with old post_id=" + oldPost_id);
        }
    }

    /**
     * Method for parsing the JSON data retrieved from the server to the database
     *
     * @param response the string response from the server
     */
    public void parseBulletins(String response) throws JSONException, ParseException {
        JSONArray bulletinArray = new JSONArray(response);
        ArrayList<Bulletin> bulletinList = new ArrayList<>(bulletinArray.length());

        for (int i = 0; i < bulletinArray.length(); i++) {
            Bulletin b = Bulletin.parseBulletin(bulletinArray.get(i).toString());
            bulletinList.add(b);
        }

        addBulletins(bulletinList);
    }

    /**
     * Method for adding empty Bulletin to first row into the database;
     * Called on instance initialisation;
     */
    private void addNullBulletin() {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry._ID},
                DataContract.BulletinEntry._ID + " = ?",
                new String[]{"-1"},
                null);

        if (cursor != null && !cursor.moveToFirst()) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
            values.put(DataContract.BulletinEntry._ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_DATE, new Date().getTime());

            Uri uri = mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            if (uri != null) {
                Log.d(TAG, "Null bulletin added.");
            } else {
                Log.e(TAG, "Failed to add null bulletin.");
            }

        } else {
            updateNullBulletin();
        }

        if (cursor != null)
            cursor.close();
    }

    public void updateNullBulletin() {
        ContentValues values = new ContentValues(1);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, new Date().getTime());
        mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values, DataContract.BulletinEntry._ID + " = -1",
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

            values.put(DataContract.BulletinEntry._ID, b.getPostID());
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, b.getUserID());
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, b.getFirstName());
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, b.getLastName());
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, b.getMessage());
            values.put(DataContract.BulletinEntry.COLUMN_DATE, b.getDate().getTime());
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, b.getNumOfReplies());
            values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, STATUS_SUCCESS);
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(b.getAttachments()));

            cVVector.add(values);
            Log.d(TAG, "Bulletin added: " + b.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI, cvArray);
            updateNullBulletin();

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " bulletins added");
    }

    /**
     * Inserting new bulletin into the database
     *
     * @param b bulletin to insert
     */
    public void addBulletin(Bulletin b, boolean isOnServer) {
        addBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(),
                b.getDate(), b.getNumOfReplies(), b.getAttachments(), isOnServer ? STATUS_SUCCESS : STATUS_UPLOADING);
    }

    /**
     * Inserting new bulletin into the database
     *
     * @param b bulletin to insert
     */
    public void addBulletin(Bulletin b, int serverStatus) {
        addBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(),
                b.getDate(), b.getNumOfReplies(), b.getAttachments(), serverStatus);
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
     */
    public void addBulletin(long postId, long userId, String userName, String userLastName, String text, Date date,
                            int numOfReplies, LinkedList<Bulletin.Attachment> attachments, int serverStatus) {

        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry._ID},
                DataContract.BulletinEntry._ID + " = " + postId,
                null,
                BULLETIN_ORDER);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            updateBulletin(postId, userId, userName, userLastName, text, date, numOfReplies, attachments, serverStatus);
        } else {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry._ID, postId);
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
            values.put(DataContract.BulletinEntry.COLUMN_DATE, date.getTime());
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, numOfReplies);
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));
            values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, serverStatus);
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
                updateNullBulletin();
            }

        }
        if (msgCursor != null)
            msgCursor.close();
    }

    /**
     * Method for updating bulletin in db
     */
    public void updateBulletin(long postId, long userId, String userName, String userLastName, String text, Date date,
                               int numOfReplies, LinkedList<Bulletin.Attachment> attachments, int serverStatus) {

        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, date.getTime());
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, numOfReplies);
        values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));
        values.put(BulletinEntry.COLUMN_SERVER_STATUS, serverStatus);

        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(postId)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error updating bulletin: PostID=" + postId + "; UserID=" + userId + "; Number of rows updated=" + numOfUpdatedRows);
        } else {
            String debugtext = "Bulletin updated: ID=" + postId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text
                    + "...; Date=" + date;
            debugtext += "; Num of replies=" + numOfReplies;
            debugtext += "; Num of attachments=";
            debugtext += attachments == null ? '0' : Integer.toString(attachments.size());

            Log.d("BulletinManager", debugtext);
            updateNullBulletin();
        }
    }

    /**
     * Method for changing bulletin {@link Bulletin#mServerStatus} in db
     *
     * @param bulletin the bulletin that needs to be updated
     */
    public void updateBulletinServerStatus(Bulletin bulletin, int serverStatus) {

        if(STATUS_RETRY_UPLOAD == serverStatus){
            addNotUploadedActivity(bulletin, CLASS_BULLETIN);
        }

        ContentValues values = new ContentValues();
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, serverStatus);

        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(bulletin.getPostID())});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error updating bulletin: PostID=" + bulletin.getPostID());
        } else {
            String debugtext = "Bulletin updated: ID=" + bulletin.getPostID();

            Log.d("BulletinManager", debugtext);
            updateNullBulletin();
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
        Bulletin bulletin;
        try {
            cursor.moveToPosition(index);

            bulletin = new Bulletin(
                    cursor.getInt(COL_POST_ID),
                    cursor.getInt(COL_USER_ID),
                    cursor.getString(COL_FIRST_NAME),
                    cursor.getString(COL_LAST_NAME),
                    cursor.getString(COL_TEXT),
                    new Date(cursor.getLong(COL_DATE)));

            bulletin.setServerStatus(cursor.getInt(COL_ON_SERVER));
            bulletin.setNumOfReplies(cursor.getInt(COL_NUM_OF_REPLIES));
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

    /**
     * Returns the number of bulletins in the database
     */
    public int getNumOfBulletinsInDb() {
        try {
            Cursor c = mAppContext.getContentResolver().query(DataContract.BulletinEntry.CONTENT_URI, null, null, null, null, null);
            // -1 becouse of null bulletin
            int count = c.getCount() - 1;
            c.close();
            return count;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return 0;
        }
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

            values.put(DataContract.ReplyEntry._ID, r.replyId);
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
    public void addReply(long replyId, long userId, long postId, String firstName, String lastName, String text, Date date,
                         int numOfApprvs, LinkedList<UserProfile> approves) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry._ID, replyId);
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
//            Log.d(TAG, debugtext);
        }
    }

    private void updateReply(int replyId, int userId, int postId, String firstName, String lastName, String text, Date date, int numOfApprvs, LinkedList<UserProfile> approves) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry._ID, replyId);
        values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, firstName);
        values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, lastName);
        values.put(DataContract.ReplyEntry.COLUMN_TEXT, text);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, date.getTime());
        values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, numOfApprvs);
        values.put(DataContract.ReplyEntry.COLUMN_APPRVS, parseApprovesToJSON(approves));


        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.ReplyEntry.CONTENT_URI, values,
                DataContract.ReplyEntry._ID + " = ?", new String[]{Integer.toString(replyId)});

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

    public Context getContext() {
        return mAppContext;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void createDummyData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Random r = new Random();
                int namesSize = Util.names.length;
                int lNamesSize = Util.lastNames.length;

                getCurrentUserProfile().setNumOfFriends(40);
                for (int i = 1; i <= getCurrentUserProfile().getNumOfFriends(); i++) {
                    addUserProfile(i, Util.names[r.nextInt(namesSize)], Util.lastNames[r.nextInt(lNamesSize)],
                            mAppContext.getString(R.string.dev_email), mAppContext.getString(R.string.dev_nopic), 0, true);
                }

                int itemDownloaded = 0;

                ArrayList<Bulletin> list = new ArrayList<>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);
                LinkedList<Bulletin.Reply> repliesList = new LinkedList<>();
                Cursor c = mAppContext.getContentResolver().query(DataContract.ProfileEntry.CONTENT_URI,
                        null, null, null, null);
                int count = c.getCount();
                c.close();
                for (int i = 0; i < DataManager.NO_OF_BULLETIN_TO_DOWNLOAD; i++) {


                    UserProfile friend = getUserProfile(r.nextInt(count));
                    Date date = new Date();
                    date.setTime(date.getTime() - i * Util.ONE_MIN - DataManager.ONE_DAY * DataManager.mCurrentPage);

                    Bulletin bulletin = new Bulletin();
                    bulletin.setPostID(i + DataManager.NO_OF_BULLETIN_TO_DOWNLOAD * DataManager.mCurrentPage);
                    bulletin.setUserID(friend.getUserId());
                    bulletin.setFirstName(friend.getFirstName());
                    bulletin.setLastName(friend.getLastName());
                    bulletin.setDate(date);
                    bulletin.setMessage(Util.loremIspum);
                    bulletin.setNumOfReplies(r.nextInt(20));
                    bulletin.setServerStatus(STATUS_SUCCESS);

                    for (int j = 0; j < bulletin.getNumOfReplies(); j++) {

                        UserProfile friendReplied = getUserProfile(r.nextInt(getCurrentUserProfile().getNumOfFriends()));
                        Bulletin.Reply reply = bulletin.getReplyInstance();

                        reply.replyId = Integer.parseInt(Long.toString(bulletin.getPostID()) + Integer.toString(j)); // replyId eg - 05: 0 - postId, 5 - replyId;
                        reply.userId = friendReplied.getUserId();
                        reply.postId = bulletin.getPostID();
                        reply.userFirstName = friendReplied.getFirstName();
                        reply.userLastName = friendReplied.getLastName();
                        reply.replyText = Util.loremIpsumShort;
                        reply.replyDate = new Date(date.getTime() - Util.ONE_MIN * j - Util.ONE_DAY * DataManager.mCurrentPage);

                        for (int k = 0; k < r.nextInt(5); k++) {
                            reply.replyApproves.add(getUserProfile(r.nextInt(getCurrentUserProfile().getNumOfFriends())));
                            reply.numOfApprvs++;
                        }

                        repliesList.add(reply);
                    }

                    list.add(bulletin);
                    itemDownloaded++;
                }
                addBulletins(list);
                addReplies(repliesList);

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mAppContext);
                Intent i = new Intent(DataManager.BULLETIN_LIST_LOADED);
                i.putExtra(DataManager.EXTRA_ITEM_DOWNLOADED_COUNT, itemDownloaded);

                // Notifying HomeActivity that the data has been downloaded with broadcast and static member TODO - notify in onPostExecute later
                broadcastManager.sendBroadcast(i);
            }
        });
        thread.start();
    }


}


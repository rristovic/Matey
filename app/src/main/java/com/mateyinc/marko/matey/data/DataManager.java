package com.mateyinc.marko.matey.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataContract.ApproveEntry;
import com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.data.DataContract.NotUploadedEntry;
import com.mateyinc.marko.matey.data.DataContract.NotificationEntry;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.DataContract.ReplyEntry;
import com.mateyinc.marko.matey.data.operations.DownloadOp;
import com.mateyinc.marko.matey.data.operations.Operation;
import com.mateyinc.marko.matey.data.operations.UploadOp;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.inall.MyApplication;
import com.mateyinc.marko.matey.model.Approve;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static com.facebook.login.widget.ProfilePictureView.TAG;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_SUCCESS;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_UPLOADING;

/**
 * The manager for the data entries
 */
public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    public static final int CACHE_SIZE_MB = 100; // Max cache size on disk for storing data

    /** Number of bulletins to download from the server */
    public static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /** The current page of bulletins in the database */
    public static int mCurrentPage = 0;

    // One day in milliseconds
    public static final int ONE_DAY = 86400000;
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

    public interface ServerStatus {
        /** Activity upload status that is saved in database, and used for UI control */
        int STATUS_RETRY_UPLOAD = -1;
        /** @see #STATUS_RETRY_UPLOAD */
        int STATUS_UPLOADING = 0;
        /**
         * Activity upload status that is saved in database, and used for UI control;
         * If data is downloaded from the server, this is the status that gets saved
         */
        int STATUS_SUCCESS = 1;
    }

    /** Used for new post that doesn't yet have the post_id */
    public static final int NO_POST_ID = -1;


    // JSON array names
    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOAD_FAILED = "com.mateyinc.marko.matey.data.internet.home.bulletins_load_failed";
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.data.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.data.internet.home.bulletins_loaded_count";

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
                Log.d(TAG, "New instance of DataManager created.");
            }

            return mInstance;
    }

    private DataManager(Context context) {
        mAppContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }


    //  General methods ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Method for creating new activity_id, which is later replaced by the id returned from the server;
     * Newly created ids are starting from -2 to {@value Integer#MIN_VALUE}
     */
    public long createNewActivityId() {
        Cursor c = mAppContext.getContentResolver().query(NotUploadedEntry.CONTENT_URI, null, null, null, null);
        long id = -2;

        if (c != null) {
            int count = c.getCount()+1;
            id = -(count + 1);
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


    public static final String BULLETIN_ORDER = DataContract.BulletinEntry.COLUMN_DATE + " DESC";
    public static final int NUM_OF_BULLETINS_TO_DOWNLOAD = 40;

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
     * Method for adding list of Bulletin to database
     *
     * @param list to be added
     */
    public void addBulletins(ArrayList<Bulletin> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(list.size());

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

            cVVector.add(values);
            Log.d(TAG, "Bulletin added: " + b.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " bulletins added");
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
    public static final String REPLIES_ORDER_BY = DataContract.ReplyEntry.COLUMN_DATE + " ASC";

    /**
     * Method for getting the reply from the database
     *
     * @param index  the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public Reply getReply(int index, Cursor cursor) {
        Reply reply = new Reply();

        try {
            cursor.moveToPosition(index);

            reply._id = cursor.getInt(BulletinViewActivity.COL_REPLY_ID);
            reply.userId = cursor.getInt(BulletinViewActivity.COL_USER_ID);
            reply.postId = cursor.getInt(BulletinViewActivity.COL_POST_ID);
            reply.userFirstName = cursor.getString(BulletinViewActivity.COL_FIRST_NAME);
            reply.userLastName = cursor.getString(BulletinViewActivity.COL_LAST_NAME);
            reply.setDate(cursor.getLong(BulletinViewActivity.COL_DATE));
            reply.replyText = cursor.getString(BulletinViewActivity.COL_TEXT);
            reply.numOfApprvs = cursor.getInt(BulletinViewActivity.COL_NUM_OF_APPRVS);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return reply;
    }

    public Context getContext() {
        return mAppContext;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    private RequestQueue mRequestQueue;

    private List<Operation> opList = new ArrayList<>();

    public DataManager addOperation(Operation op){
        opList.add(op);
        return this;
    }

    public void performOperations(){
        Iterator i = opList.iterator();

        while (i.hasNext()){
            ((Operation) i.next()).execute(mAppContext, mRequestQueue, MotherActivity.access_token);
            i.remove();
        }
    }

}


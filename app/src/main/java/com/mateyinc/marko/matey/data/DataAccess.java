package com.mateyinc.marko.matey.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Approve;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;

import static com.mateyinc.marko.matey.data.OperationManager.BULLETIN_ORDER;

/**
 * Class used for storing and retrieving data from the local database
 */
public class DataAccess {

    private static final String TAG = DataAccess.class.getSimpleName();

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
            DataContract.BulletinEntry.COLUMN_ATTACHMENTS,
            DataContract.BulletinEntry.COLUMN_NUM_OF_LIKES,
            DataContract.BulletinEntry.COLUMN_SUBJECT
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
    public static final int COL_NUM_OF_LIKES = 9;
    public static final int COL_SUBJECT = 10;

    private static DataAccess mInstance;

    private final Object mLock = new Object();
    private Context mContext;
//    private UserProfile mCurrentUserProfile;

    private DataAccess(Context context){
        mContext = context;
    }

    public static synchronized DataAccess getInstance(Context context){
        if (mInstance == null) {
            mInstance = new DataAccess(context);

            Log.d(TAG, "New instance of OperationManager created.");
        }

        return mInstance;
    }

    ///// Approve methods //////

    /**
     * Method for saving new approve to db
     * @param cursor cursor to the db
     * @param position of the post in db
     */
    public void newPostLike(Cursor cursor, int position) {
        cursor.moveToPosition(position);

        ContentValues values = new ContentValues(2);

        // Updates approve table
        long postId = cursor.getLong(OperationManager.COL_POST_ID);
        long userId = cursor.getLong(OperationManager.COL_USER_ID);
        values.put(DataContract.ApproveEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ApproveEntry.COLUMN_USER_ID, userId);
        Uri uri = mContext.getContentResolver().insert(DataContract.ApproveEntry.CONTENT_URI, values);
        if (uri == null) {
            Log.e(TAG, String.format("Error adding new approve (post_id:%d, user_id:%d) to the database.",
                    postId, userId));
        } else {
            // Update bulletin num of likes
            values = new ContentValues(1);
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_LIKES, cursor.getInt(OperationManager.COL_NUM_OF_LIKES) + 1);
            int updated = mContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                    DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(postId)});

            if (updated == 0) {
                Log.e(TAG, "Failed to updated num of likes for post with id:" + postId);
            }
        }
    }

    /**
     * Set's the {@link MotherActivity#mCurrentUserProfile} from {@link SharedPreferences}
     * @return true if settings current user profile was successful
     */
    public boolean setCurrentUserProfile() {
        if  (MotherActivity.mCurrentUserProfile != null)
            return true;

        Cursor c = mContext.getContentResolver().query(DataContract.ProfileEntry.CONTENT_URI, null,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Long.toString(MotherActivity.user_id)}, null);

        if (c != null)
            if (c.moveToFirst()) {
                MotherActivity.mCurrentUserProfile = new UserProfile(MotherActivity.user_id, c.getString(c.getColumnIndex(DataContract.ProfileEntry.COLUMN_NAME)),
                        c.getString(c.getColumnIndex(DataContract.ProfileEntry.COLUMN_LAST_NAME)),
                        c.getString(c.getColumnIndex(DataContract.ProfileEntry.COLUMN_EMAIL)),
                        c.getString(c.getColumnIndex(DataContract.ProfileEntry.COLUMN_PROF_PIC)));

                Log.d("MotherActivity", "Current user profile updated with user_id=" + MotherActivity.user_id);
                c.close();
            } else {
                Log.e("MotherActivity", "Error setting current user profile from shared prefs.");
                c.close();
                return false;
            } else
            return false;

        return true;
    }

    /** Key for storing user pref file id in shared prefs */
    public static final String KEY_CUR_USER_ID = "cur_userprofile_id";

    /**
     * Method for storing current user profile in memory, and in shared prefs.
     *
     * @param preferences the Shared Preferences object used to store user_id
     * @param userProfile the current user profile
     */
    public static void setCurrentUserProfile(SharedPreferences preferences, UserProfile userProfile) {
        MotherActivity.mCurrentUserProfile = userProfile;

        if (userProfile != null) {
            preferences.edit().putLong(KEY_CUR_USER_ID, userProfile.getUserId()).apply();
            MotherActivity.user_id = userProfile.getUserId();
            Log.d(TAG, "Current user updated in prefs.");
        } else {
            MotherActivity.user_id = Long.MIN_VALUE;
            preferences.edit().remove(KEY_CUR_USER_ID).commit();
            Log.d(TAG, "Current user removed from prefs.");
        }
    }

    /**
     * Helper method for checking if approve exists in database
     * @param post_id id of post liked
     * @param replyId id of reply liked
     * @param context context used for db access
     * @return true if approve exists
     */
    public static boolean isApproveInDb(long post_id, long replyId, Context context) {
        Cursor c = context.getContentResolver().query(DataContract.ApproveEntry.CONTENT_URI,
                new String[]{DataContract.ApproveEntry._ID}, DataContract.ApproveEntry.COLUMN_POST_ID + " = ? AND "
                + DataContract.ApproveEntry.COLUMN_USER_ID + " = ? AND " + DataContract.ApproveEntry.COLUMN_REPLY_ID + " = ?",
                new String[]{Long.toString(post_id), Long.toString(MotherActivity.user_id), Long.toString(replyId)},null);

        return (c != null && c.getCount() != 0);
    }


    /**
     * Method for getting the bulletin from the database
     *
     * @param index  the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public static Bulletin getBulletin(int index, Cursor cursor) {
        Bulletin bulletin = null;
        try {
            cursor.moveToPosition(index);

            bulletin = new Bulletin(
                    cursor.getInt(COL_POST_ID),
                    cursor.getInt(COL_USER_ID),
                    cursor.getString(COL_FIRST_NAME),
                    cursor.getString(COL_LAST_NAME),
                    cursor.getString(COL_TEXT),
                    new Date(cursor.getLong(COL_DATE)));

            bulletin.setmNumOfLikes(cursor.getInt(COL_NUM_OF_LIKES));
            bulletin.setSubject(cursor.getString(COL_SUBJECT));

            bulletin.setServerStatus(cursor.getInt(COL_ON_SERVER));
            bulletin.setNumOfReplies(cursor.getInt(COL_NUM_OF_REPLIES));
            bulletin.setAttachmentsFromJSON(cursor.getString(7));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return bulletin;
    }

    /**
     * Method for getting the bulletin from the database with the default cursor
     *
     * @param index the position of the bulletin in the database
     * @return the new instance of Bulletin from the database
     */
    public static Bulletin getBulletin(int index, Context context) {
        Cursor cursor = context.getContentResolver().query(
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
    public static int getNumOfBulletinsInDb(Context context) {
        try {
            Cursor c = context.getContentResolver().query(DataContract.BulletinEntry.CONTENT_URI, null, null, null, null, null);
            // -1 becouse of null bulletin
            int count = c.getCount() - 1;
            c.close();
            return count;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return 0;
        }
    }
}

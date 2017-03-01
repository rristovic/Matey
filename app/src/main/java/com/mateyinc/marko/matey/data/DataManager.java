package com.mateyinc.marko.matey.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.UserProfile;

/**
 * Class used for storing and retrieving data from the local database
 */
public class DataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    private static DataManager mInstance;

    private final Object mLock = new Object();
    private Context mContext;
//    private UserProfile mCurrentUserProfile;

    private DataManager(Context context){
        mContext = context;
    }

    public static synchronized DataManager getInstance(Context context){
        if (mInstance == null) {
            mInstance = new DataManager(context);

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
}

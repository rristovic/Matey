package com.mateyinc.marko.matey.data.operations;

import android.content.ContentValues;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.OperationFactory;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static android.R.attr.id;

/**
 * User profile downloading and uploading operations, also updates the db;
 */
public class UserProfileOp extends Operations {
    private static final String TAG = UserProfileOp.class.getSimpleName();

    /**
     * Upload action that can be performed.
     */
    private enum UploadAction{
        /** Indicates that the user has followed a new user profile **/
        FOLLOW_PROFILE,
        /** Indicates that the user has unfollowed a profile **/
        UNFOLLOW_PROFILE,
        /** Indicates that the user has saved new picture **/
        UPLOAD_PIC,
        /** Indicates that the user has save new cover picture **/
        UPLOAD_COVER,
        /** Indicates that the user has updates it's info **/
        UPLOAD_INFO
    }

    // Json keys in response from the server TODO - finish keys
    private static final String KEY_ID = "user_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PIC = "picture_url";
    private static final String KEY_COVER_PIC = "cover_url";
    private static final String KEY_FOLLOWERS_NUM = "num_of_followers";
    private static final String KEY_FOLLOWING_NUM = "num_of_following";
    private static final String KEY_VERIFIED = "verified";

    // Holds the profile data for upload
    private static ProfileData mProfileData;

    public UserProfileOp(OperationProvider provider, MotherActivity context, OperationFactory.OperationType operationType) {
        super(provider, context, operationType);
        mProfileData = new ProfileData();
    }

    @Override
    public void download(long id) {
        String url = UrlData.createProfileDataUrl(id);
        createDownloadReq(url);
        startDownload();
    }

    @Override
    public void onDownloadSuccess(final String response) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                saveToDb(response);
            }
        });
    }

    @Override
    public void onDownloadFailed(VolleyError error) {

    }

    @Override
    public <T> void upload(T object) {
        if (!(object instanceof ProfileData)){
            throw new RuntimeException("Wrong data format is queued for upload. Please pass ProfileData as parameter.");
        }
        String url;
        int method;
        switch (mProfileData.mAction){
            case FOLLOW_PROFILE:{
                url = UrlData.createFollowUrl(2);
                method = Request.Method.POST;
                submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        saveFollowedUser(true);
                    }
                });
                break;
            }

            case UNFOLLOW_PROFILE:{
                url = UrlData.createUnfollowUrl(2);
                method = Request.Method.DELETE;
                submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        saveFollowedUser(false);
                    }
                });
                break;
            }

            default:{
                url = "#";
                method = Request.Method.POST;
            }
        }

        createNewUploadReq(url, method);
        startUpload();
    }

    @Override
    public void onUploadSuccess(String response) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                updateServerStatus(mProfileData.userId, ServerStatus.STATUS_SUCCESS);
            }
        });
    }

    @Override
    public void onUploadFailed(VolleyError error) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                updateServerStatus(mProfileData.userId, ServerStatus.STATUS_RETRY_UPLOAD);
            }
        });
    }

    /**
     * Method for updating db with the followed or unfollowed user profile.
     * Updates user profile column {@link ProfileEntry#COLUMN_FOLLOWED} to provided boolean;
     * @param isFollowed indicates if the user is followed or not
     */
    private void saveFollowedUser(boolean isFollowed) {
        ContentValues values = new ContentValues(1);
        values.put(DataContract.ProfileEntry.COLUMN_FOLLOWED, isFollowed);

        int numOfUpdatedRows = mContextRef.get().getContentResolver().update(
                ProfileEntry.CONTENT_URI, values, ProfileEntry._ID + " = ?",
                new String[]{Long.toString(mProfileData.followedUserId)}
        );

        if (numOfUpdatedRows != 1) {
            Log.e(getTag(), String.format("Error updating user profile (with ID=%d) to %s.", id, isFollowed ? "followed" : "unfollowed"));
        } else {
            Log.d(getTag(), String.format("User profile (with ID=%d) updated to %s.", id, isFollowed ? "followed" : "unfollowed"));
        }
    }

    void saveToDb(String response) {
        try {
            ContentValues userValues;
            JSONObject object;
            // TODO - finish download multiple users
            object = new JSONObject(response);

            // Parsing
            Long id = object.getLong(KEY_ID);
            String name = object.getString(KEY_FIRST_NAME);
            String lastName = object.getString(KEY_LAST_NAME);
            String fullName = object.getString(KEY_FULL_NAME);
            String email = object.getString(KEY_EMAIL);
            String picLink = object.getString(KEY_PROFILE_PIC);
            String coverLink = object.getString(KEY_COVER_PIC);
            int followersNum = object.getInt(KEY_FOLLOWERS_NUM);
            int followingNum = object.getInt(KEY_FOLLOWING_NUM);
            boolean verified = object.getBoolean(KEY_VERIFIED);

            userValues = new ContentValues();
            userValues.put(DataContract.ProfileEntry._ID, id);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, name);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_FULL_NAME, fullName);
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
            userValues.put(DataContract.ProfileEntry.COLUMN_PROF_PIC, picLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_COVER_PIC, coverLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWERS_NUM, followersNum);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWING_NUM, followingNum);

//            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, mIsFriend);
//            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);


            // Add to db
            int insertedUri = mContextRef.get().getContentResolver().update(
                    DataContract.ProfileEntry.CONTENT_URI,
                    userValues,
                    ProfileEntry._ID + " = ?", new String[]{Long.toString(id)});

            // Debug
            String debugString = getString(id, name, lastName, email, picLink);
            if (insertedUri == 0 ) {
                Log.e(TAG, "Error inserting " + debugString);
            } else {
                Log.d(TAG, "New profile added: " + debugString);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Method for creating new data file for upload with action {@link UploadAction#FOLLOW_PROFILE}.
     * @param curUserId  current user id that is following someone.
     * @param followedUserId  user id of the followed profile.
     * @return newly created data file for upload.
     */
    public static ProfileData followNewUser(long curUserId, long followedUserId){
        mProfileData.userId = curUserId;
        mProfileData.followedUserId = followedUserId;
        mProfileData.mAction = UploadAction.FOLLOW_PROFILE;
        return mProfileData;
    }

    /**
     * Method for creating new data file for upload with action {@link UploadAction#UNFOLLOW_PROFILE}.
     * @param curUserId current user id that is unfollowing someone.
     * @param unfollowedUserId user id of the followed profile.
     * @return newly created data file for upload.
     */
    public static ProfileData unfollowUser(long curUserId, long unfollowedUserId) {
        mProfileData.userId = curUserId;
        mProfileData.followedUserId = unfollowedUserId;
        mProfileData.mAction = UploadAction.UNFOLLOW_PROFILE;
        return mProfileData;
    }

    /**
     * Helper method for creating debug text string
     */
    public String getString(long id, String name, String lastName, String email, String picLink){
        return String.format(Locale.US,"UserProfile: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", id, name, lastName, email, picLink);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    private class ProfileData {

        private long userId;
        private long followedUserId;

        // Booleans which indicates what kind of request this is
        private UploadAction mAction;
    }
}


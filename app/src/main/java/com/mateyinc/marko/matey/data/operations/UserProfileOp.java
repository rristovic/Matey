package com.mateyinc.marko.matey.data.operations;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.OperationFactory;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.data.internet.NetworkAction;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Vector;

/**
 * User profile downloading and uploading operations, also updates the db;
 */
public class UserProfileOp extends Operations {
    private static final String TAG = UserProfileOp.class.getSimpleName();


    /**
     * Upload action that can be performed.
     */
    private enum UploadAction {
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

    /**
     * Download action that can be performed.
     */
    private enum DownloadAction {
        /** Action for downloading followed list for the current user */
        GET_FOLLOWED_LIST,
        /** Action for downloading following profiles list for the current user */
        GET_FOLLOWING_LIST,
        /** Action for downloading someone's user profile */
        GET_USER_PROFILE
    }

    // Json keys in response from the server TODO - finish keys
    public static final String KEY_ID = "user_id";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROFILE_PIC = "picture_url";
    public static final String KEY_COVER_PIC = "cover_url";
    public static final String KEY_FOLLOWERS_NUM = "num_of_followers";
    public static final String KEY_FOLLOWING_NUM = "num_of_following";
    public static final String KEY_VERIFIED = "verified";
    public static final String KEY_FOLLOWING = "following";

    // Holds the profile data for startUploadAction
    private static ProfileNetworkAction mNetworkAction;

    public UserProfileOp(OperationProvider provider, MotherActivity context, OperationFactory.OperationType operationType) {
        super(provider, context, operationType);
        mNetworkAction = new ProfileNetworkAction();
    }

    @Override
    public void startDownloadAction(NetworkAction action) {
        String url;

        switch (mNetworkAction.getDownloadAction()){
            case GET_FOLLOWED_LIST:{
                Uri uri =  Uri.parse(UrlData.createFollowersListUrl(mNetworkAction.mUserProfileId)).buildUpon()
                        .appendQueryParameter(UrlData.QPARAM_LIMIT, Integer.toString(mNetworkAction.limit))
                        .appendQueryParameter(UrlData.QPARAM_OFFSET, Integer.toString(mNetworkAction.offset))
                        .build();
                url = uri.toString();
                break;
            }
            case GET_FOLLOWING_LIST:{
                // TODO - finish params
                Uri uri =  Uri.parse(UrlData.createFollowingListUrl(mNetworkAction.mUserProfileId)).buildUpon()
                        .appendQueryParameter(UrlData.QPARAM_LIMIT, Integer.toString(mNetworkAction.limit))
                        .appendQueryParameter(UrlData.QPARAM_OFFSET, Integer.toString(mNetworkAction.offset))
                        .build();
                url = uri.toString();
                break;
            }
            case GET_USER_PROFILE:{
                url = UrlData.createProfileDataUrl(mNetworkAction.mUserProfileId);
                break;
            }

            default:{
                url = "#";
            }
        }

        createNewDownloadReq(url);
        startDownload();
    }

    @Override
    public void onDownloadSuccess(final String response) {
        final Context c = mContextRef.get();
        final String parsedResponse;

        switch (mNetworkAction.getDownloadAction()){
            case GET_USER_PROFILE:{
                parsedResponse = response;
                break;
            }
            default: {
                parsedResponse = "";
            }
        }
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                saveToDb(parsedResponse, c);
            }
        });
    }

    @Override
    public void onDownloadFailed(VolleyError error) {

    }


    @Override
    public void startUploadAction(NetworkAction action) {
        String url;
        int method;
        switch (mNetworkAction.getUploadAction()){
            case FOLLOW_PROFILE:{
                url = UrlData.createFollowUrl(mNetworkAction.mUserProfileId);
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
                url = UrlData.createUnfollowUrl(mNetworkAction.mUserProfileId);
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
                updateServerStatus(mNetworkAction.mUserProfileId, ServerStatus.STATUS_SUCCESS);
            }
        });
    }

    @Override
    public void onUploadFailed(VolleyError error) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                updateServerStatus(mNetworkAction.mUserProfileId, ServerStatus.STATUS_RETRY_UPLOAD);
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
                new String[]{Long.toString(mNetworkAction.mUserProfileId)}
        );

        if (numOfUpdatedRows != 1) {
            Log.e(getTag(), String.format("Error updating user profile (with ID=%d) to %s.", mNetworkAction.mUserProfileId, isFollowed ? "followed" : "unfollowed"));
        } else {
            Log.d(getTag(), String.format("User profile (with ID=%d) updated to %s.", mNetworkAction.mUserProfileId, isFollowed ? "followed" : "unfollowed"));
        }
    }

    /**
     * Method for saving multiple values into the database
     * @param cVVector {@link Vector} object which contains {@link ContentValues} object values;
     * @param c context used for db control
     * @param isNew boolean that indicates if this data is new or not, thus updates it or just inerts it into db
     */
    void saveToDbMultiple(Vector<ContentValues> cVVector, Context c, boolean isNew){
    }

    void saveToDb(String response, Context c) {
         try {
            JSONObject object =  new JSONObject(response);

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
//            boolean verified = object.getBoolean(KEY_VERIFIED);

             // Saving
            ContentValues userValues = new ContentValues();
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
            Uri uri = c.getContentResolver().insert(
                    DataContract.ProfileEntry.CONTENT_URI, userValues);

            // Debug
            String debugString = getString(id, name, lastName, email, picLink);
            if (uri == null ) {
                Log.e(TAG, "Error inserting " + debugString);
            } else {
                Log.d(TAG, "New profile added: " + debugString);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Method for creating new action {@link UploadAction#FOLLOW_PROFILE}.
     * @param followedUserId  user id of the followed profile.
     * @return newly created {@link NetworkAction}.
     */
    public static NetworkAction followNewUserAction(long followedUserId){
        mNetworkAction.mUserProfileId = followedUserId;
        mNetworkAction.setUploadAction(UploadAction.FOLLOW_PROFILE);
        return mNetworkAction;
    }

    /**
     * Method for creating new action {@link UploadAction#UNFOLLOW_PROFILE}.
     * @param unfollowedUserId user id of the followed profile.
     * @return newly created data file for startUploadAction.
     */
    public static NetworkAction unfollowUserAction( long unfollowedUserId) {
        mNetworkAction.mUserProfileId = unfollowedUserId;
        mNetworkAction.setUploadAction(UploadAction.UNFOLLOW_PROFILE);
        return mNetworkAction;
    }

    /**
     * Method for creating new action {@link DownloadAction#GET_FOLLOWED_LIST}
     * @param offset
     * @param limit
     * @return network action to be performed
     */
    public static NetworkAction getFollowersListAction(long profileId, int offset, int limit) {
        mNetworkAction.mUserProfileId = profileId;
        mNetworkAction.offset = offset;
        mNetworkAction.limit = limit;
        mNetworkAction.setDownloadAction(DownloadAction.GET_FOLLOWED_LIST);
        return mNetworkAction;
    }

    /**
     * Method for creating new action {@link DownloadAction#GET_FOLLOWED_LIST}
     * @param offset
     * @param limit
     * @return network action to be performed
     */
    public static NetworkAction getFollowingListAction(long profileId, int offset, int limit) {
        mNetworkAction.mUserProfileId = profileId;
        mNetworkAction.offset = offset;
        mNetworkAction.limit = limit;
        mNetworkAction.setDownloadAction(DownloadAction.GET_FOLLOWING_LIST);
        return mNetworkAction;
    }

    /**
     * Method for creating new action {@link DownloadAction#GET_USER_PROFILE}
     * @param profileId profile id of the requested user
     * @return network action to be performed
     */
    public static NetworkAction getUserProfileAction(long profileId){
        mNetworkAction.mUserProfileId = profileId;
        mNetworkAction.setDownloadAction(DownloadAction.GET_USER_PROFILE);
        return mNetworkAction;
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


    private class ProfileNetworkAction extends NetworkAction<UploadAction, DownloadAction>{

        long mUserProfileId;

        int offset;
        int limit;

        private UploadAction mUploadAction;
        private DownloadAction mDownloadAction;

        @Override
        public UploadAction getUploadAction() {
            return mUploadAction;
        }

        @Override
        public DownloadAction getDownloadAction() {
            return mDownloadAction;
        }

        @Override
        public void setDownloadAction(DownloadAction action) {
            mDownloadAction = action;
        }

        @Override
        public void setUploadAction(UploadAction action) {
            mUploadAction = action;
        }
    }
}


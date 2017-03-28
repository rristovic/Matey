package com.mateyinc.marko.matey.internet.operations;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.model.UserProfile;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * User profile downloading and uploading operations, also updates the db;
 */
public class UserProfileOp extends Operations {
    private static final String TAG = UserProfileOp.class.getSimpleName();

    private long mUserId;
    private UserProfile mUserProfile;

    public UserProfileOp(MotherActivity context) {
        super(context);
    }

    public UserProfileOp(UserProfile userProfile, MotherActivity context) {
        super(context);
        mUserProfile = userProfile;
    }

    @Override
    public void startDownloadAction() {

        switch (mOpType) {
            case DOWNLOAD_FOLLOWERS: {
//                Uri uri = Uri.parse(UrlData.createFollowersListUrl(mUserId)).buildUpon()
//                        .appendQueryParameter(UrlData.QPARAM_LIMIT, Integer.toString(mCount))
//                        .appendQueryParameter(UrlData.QPARAM_OFFSET, Integer.toString(mOffset))
//                        .build();
//                mUrl = uri.toString();
                break;
            }
//            case GET_FOLLOWING_LIST:{
//                // TODO - finish params
//                Uri uri =  Uri.parse(UrlData.createFollowingListUrl(mNetworkAction.mUserProfileId)).buildUpon()
//                        .appendQueryParameter(UrlData.QPARAM_LIMIT, Integer.toString(mNetworkAction.limit))
//                        .appendQueryParameter(UrlData.QPARAM_OFFSET, Integer.toString(mNetworkAction.offset))
//                        .build();
//                url = uri.toString();
//                break;
//            }
            case DOWNLOAD_USER_PROFILE: {
                mUrl = UrlData.createProfileDataUrl(mUserId);
                break;
            }

            default: {
                Log.e(TAG, "No operation type has been specified!");
                mUrl = "#";
            }
        }

        startDownload();
    }

    @Override
    public void onDownloadSuccess(final String response) {
        final Context c = mContextRef.get();

        if (mOpType.equals(OperationType.DOWNLOAD_USER_PROFILE))
            try {
                // Parse
                UserProfile profile = new UserProfile().parse(new JSONObject(response).getJSONObject(KEY_DATA));
                DataAccess.getInstance(mContextRef.get()).addUserProfile(profile);
                // Notify UI
                EventBus.getDefault().post(new DownloadEvent(
                        true, profile, OperationType.DOWNLOAD_USER_PROFILE));
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse user profile data.", e);
                // Notify UI
                EventBus.getDefault().post(new DownloadEvent(
                        false, OperationType.DOWNLOAD_USER_PROFILE));
            }

//        mDownloadListener.onDownloadSuccess();

//        switch (mNetworkAction.getDownloadAction()){
//            case GET_USER_PROFILE:{
//                parsedResponse = response;
//                break;
//            }
//            default: {
//                parsedResponse = "";
//            }
//        }

    }

    @Override
    public void onDownloadFailed(VolleyError error) {
        String errorMsg;

        try {
            errorMsg = new String(error.networkResponse.data);
            Log.e(TAG, errorMsg);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse volley error:" + error.toString());
        }

//        mDownloadListener.onDownloadFailed();
    }


    @Override
    public void startUploadAction() {
        String url;
        int method;
        switch (mOpType) {
            case FOLLOW_USER_PROFILE: {
                url = UrlData.createFollowUrl(mUserId);
                method = Request.Method.POST;
                submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        saveFollowedUser(true);
                    }
                });
                break;
            }
            case UNFOLLOW_USER_PROFILE: {
                url = UrlData.createUnfollowUrl(mUserId);
                method = Request.Method.DELETE;
                submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        saveFollowedUser(false);
                    }
                });
                break;
            }

            default: {
                url = "#";
                Log.e(TAG, "No operation type has been specified!");
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
                updateServerStatus(1, ServerStatus.STATUS_SUCCESS);
            }
        });
    }

    @Override
    public void onUploadFailed(VolleyError error) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
//                updateServerStatus(mNetworkAction.mUserProfileId, ServerStatus.STATUS_RETRY_UPLOAD);
            }
        });
    }

    /**
     * Method for updating db with the followed or unfollowed user profile.
     * Updates user profile column {@link ProfileEntry#COLUMN_FOLLOWED} to provided boolean;
     *
     * @param isFollowed indicates if the user is followed or not
     */
    private void saveFollowedUser(boolean isFollowed) {
        ContentValues values = new ContentValues(1);
        values.put(DataContract.ProfileEntry.COLUMN_FOLLOWED, isFollowed);

        int numOfUpdatedRows = mContextRef.get().getContentResolver().update(
                ProfileEntry.CONTENT_URI, values, ProfileEntry._ID + " = ?",
                new String[]{Long.toString(mUserId)}
        );

        if (numOfUpdatedRows != 1) {
            Log.e(getTag(), String.format("Error updating user profile (with ID=%d) to %s.", mUserId, isFollowed ? "followed" : "unfollowed"));
        } else {
            Log.d(getTag(), String.format("User profile (with ID=%d) updated to %s.", mUserId, isFollowed ? "followed" : "unfollowed"));
        }
    }


    /**
     * Settings user id param for upload/download operation.
     *
     * @param userId user id param
     * @return {@link UserProfileOp} instance
     */
    public UserProfileOp setUserId(long userId) {
        mUserId = userId;
        return this;
    }

    /**
     * Settings user id param for upload/download operation.
     *
     * @param userProfile user profile to download data to.
     * @return {@link UserProfileOp} instance
     */
    public UserProfileOp setUserProfile(UserProfile userProfile) {
        mUserProfile = userProfile;
        return this;
    }

    /**
     * Setting count for user profile list download.
     * Indicates how much of user profiles to download from server.
     *
     * @param count count number to download
     * @return {@link UserProfileOp} instance
     */
    public UserProfileOp setCount(int count) {
//        mCount = count;
        return this;
    }

    /**
     * Setting offset for user profile list download.
     * Indicates from what position to download the list.
     * Set it relative to how much of profiles has already been downloaded.
     *
     * @param offset offset number
     * @return {@link UserProfileOp} instance
     */
    public UserProfileOp setOffset(int offset) {
//        mOffset = offset;
        return this;
    }

    /**
     * Helper method for creating debug text string
     */
    public String getString(long id, String name, String lastName, String email, String picLink) {
        return String.format(Locale.US, "UserProfile: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", id, name, lastName, email, picLink);
    }

    @Override
    protected void clearNextUrl() {

    }

    @Override
    protected String getTag() {
        return TAG;
    }

}


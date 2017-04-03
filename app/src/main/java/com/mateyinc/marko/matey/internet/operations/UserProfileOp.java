package com.mateyinc.marko.matey.internet.operations;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.TemporaryDataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.DownloadTempListEvent;
import com.mateyinc.marko.matey.model.Notification;
import com.mateyinc.marko.matey.model.UserProfile;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mateyinc.marko.matey.internet.operations.OperationType.DOWNLOAD_FOLLOWERS;
import static com.mateyinc.marko.matey.internet.operations.OperationType.DOWNLOAD_USER_PROFILE;
import static com.mateyinc.marko.matey.internet.operations.OperationType.DOWNLOAD_USER_PROFILE_ACTIVITIES;

/**
 * User profile downloading and uploading operations, also updates the db;
 */
public class UserProfileOp extends Operations {
    private static final String TAG = UserProfileOp.class.getSimpleName();

    private long mUserId;
    private UserProfile mUserProfile;
    private static String mActivitiesNextUrl = "";
    private static String mFollowingListNextUrl = "";
    private static String mFollowersListNextUrl = "";

    public UserProfileOp(Context context) {
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
                if (mFollowersListNextUrl.isEmpty())
                    mUrl = UrlData.buildGetFollowersList(this.mUserId);
                else
                    mUrl = buildNextPageUrl(mFollowersListNextUrl);
                break;
            }
            case DOWNLOAD_FOLLOWING: {
                if (mFollowingListNextUrl.isEmpty())
                    mUrl = UrlData.buildGetFollowingList(this.mUserId);
                else
                    mUrl = buildNextPageUrl(mFollowingListNextUrl);
                break;
            }
            case DOWNLOAD_USER_PROFILE: {
                mUrl = UrlData.buildProfileDataUrl(mUserId);
                break;
            }
            case DOWNLOAD_USER_PROFILE_ACTIVITIES: {
                if (mActivitiesNextUrl.isEmpty())
                    mUrl = UrlData.buildProfileActivitiesUrl(mUserId);
                else
                    mUrl = buildNextPageUrl(mActivitiesNextUrl);
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

        switch (mOpType) {
            case DOWNLOAD_USER_PROFILE:
                try {
                    // Parse
                    UserProfile profile = new UserProfile().parse(new JSONObject(response).getJSONObject(KEY_DATA));
                    DataAccess.getInstance(mContextRef.get()).addUserProfile(profile);
                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent<UserProfile>(
                            true, profile, DOWNLOAD_USER_PROFILE));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse user profile data.", e);
                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(
                            false, mOpType));
                }
                break;
            case DOWNLOAD_USER_PROFILE_ACTIVITIES:
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array = object.getJSONArray(KEY_DATA);
                    mActivitiesNextUrl = parseNextUrl(object);
                    // Parse
                    int size = array.length();
                    List<Notification> notifications = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        try {
                            Notification n = new Notification(true).parse(array.getJSONObject(i));
                            notifications.add(n);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse activity.");
                        }
                    }
                    // Notify UI
                    EventBus.getDefault().post(new DownloadTempListEvent<Notification>(
                            true,
                            mOpType,
                            new TemporaryDataAccess<Notification>(notifications, shouldClearData()), null
                    ));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse user profile data.", e);
                    // Notify UI
                    EventBus.getDefault().post(new DownloadEvent(
                            false, DOWNLOAD_USER_PROFILE));
                }
                break;
            case DOWNLOAD_FOLLOWERS:
                try {
                    JSONObject object = new JSONObject(response);
                    mFollowersListNextUrl = parseNextUrl(object);
                    // Parse data
                    JSONArray array = object.getJSONArray(KEY_DATA);
                    int size = array.length();
                    List<UserProfile> profiles = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        // Parse
                        UserProfile profile = new UserProfile().parse(array.getJSONObject(i));
                        profiles.add(profile);
                    }
                    // Notify UI
                    EventBus.getDefault().post(new DownloadTempListEvent<UserProfile>(
                            true, mOpType,
                            new TemporaryDataAccess<UserProfile>(profiles, shouldClearData()), null
                    ));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse user profile data.", e);
                    // Notify UI
                    EventBus.getDefault().post(new DownloadTempListEvent<UserProfile>(
                            false, mOpType,
                            null, null
                    ));
                }
                break;
            case DOWNLOAD_FOLLOWING:
                try {
                    JSONObject object = new JSONObject(response);
                    mFollowingListNextUrl = parseNextUrl(object);
                    // Parse data
                    JSONArray array = object.getJSONArray(KEY_DATA);
                    int size = array.length();
                    List<UserProfile> profiles = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        // Parse
                        UserProfile profile = new UserProfile().parse(array.getJSONObject(i));
                        profiles.add(profile);
                    }
                    // Notify UI
                    EventBus.getDefault().post(new DownloadTempListEvent<UserProfile>(
                            true, mOpType,
                            new TemporaryDataAccess<UserProfile>(profiles, shouldClearData()), null
                    ));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse user profile data.", e);
                    // Notify UI
                    EventBus.getDefault().post(new DownloadTempListEvent<UserProfile>(
                            false, mOpType,
                            null, null
                    ));
                }
                break;
            default:
                return;
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
                url = UrlData.buildFollowUrl(mUserId);
                method = Request.Method.PUT;
//                submitRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        saveFollowedUser(true);
//                    }
//                });
                break;
            }
            case UNFOLLOW_USER_PROFILE: {
                url = UrlData.buildUnfollowUrl(mUserId);
                method = Request.Method.DELETE;
//                submitRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        saveFollowedUser(false);
//                    }
//                });
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
//                updateServerStatus(1, ServerStatus.STATUS_SUCCESS);
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
     * Helper method for creating debug text string
     */
    public String getString(long id, String name, String lastName, String email, String picLink) {
        return String.format(Locale.US, "UserProfile: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", id, name, lastName, email, picLink);
    }

    @Override
    protected void clearNextUrl() {
        if (mOpType.equals(DOWNLOAD_USER_PROFILE_ACTIVITIES))
            mActivitiesNextUrl = "";
        else if (mOpType.equals(DOWNLOAD_FOLLOWERS))
            mFollowersListNextUrl = "";
        else
            mFollowingListNextUrl = "";
    }

    @Override
    protected String getTag() {
        return TAG;
    }

}


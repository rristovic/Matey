package com.mateyinc.marko.matey.data.internet.profile;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/15/2016.
 */
public class UserProfileAs extends AsyncTask<String, Void, String> {
    private static final String TAG = UserProfileAs.class.getSimpleName();

    private final OperationManager mManager;
    private final OperationManager mOperationManager;
    // TODO - rework weak reference
    MotherActivity activity;
    private WeakReference<UserProfile> mUserProfile;
    private long mUserId;

    public UserProfileAs(MotherActivity activity, WeakReference<UserProfile> userProfile, long reqUserId) {
        this.activity = activity;
        this.mUserProfile = userProfile;
        mManager = OperationManager.getInstance(activity);
        mOperationManager = OperationManager.getInstance(activity);
        mUserId = reqUserId;
    }

    @Override
    protected void onPreExecute() {

        if(mUserId == MotherActivity.user_id){
            mUserProfile.get().copy(MotherActivity.mCurrentUserProfile);
            notifyActivity();
            this.cancel(true);
            return;
        }

        if (!isCancelled() ) {
            Cursor c = null;
            try {
                c = activity.getContentResolver().query(DataContract.ProfileEntry.CONTENT_URI,
                        new String[]{DataContract.ProfileEntry.COLUMN_NAME, DataContract.ProfileEntry.COLUMN_LAST_NAME},
                        DataContract.ProfileEntry._ID + " = " + mUserId, null, null);

                // Setting pre downloaded data from db
                UserProfile profile = mUserProfile.get();
                if (c != null && c.moveToFirst() && profile != null) {
                    profile.setFirstName(c.getString(0));
                    profile.setLastName(c.getString(1));

                    notifyActivity();
                }

                 } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } finally {
                if (null != c)
                    c.close();
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {

        if (!isCancelled()) {

            String cur_user_id = params[0];
            String uid = params[1];
            String device_id = params[2];
            String requested_user_id = params[3];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(cur_user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8") + "&" +
                        URLEncoder.encode("requested_user_id", "UTF-8") + "=" + URLEncoder.encode(requested_user_id, "UTF-8");
//                HTTP http = new HTTP(UrlData.REQUEST_USER_PROFILE, "POST");

//                if (!isCancelled() && http.sendPost(data)) return http.getData();
            } catch (Exception e) {
                return null;
            }

        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {

        try {
            // if there is some result check if successful
            if (!isCancelled() && result != null) {

                JSONObject jsonObject = new JSONObject(result);

                UserProfile profile = mUserProfile.get();

                // if successful, set everything to SecurePreferences
                if (jsonObject.getBoolean("success")) {

                    // converting data
                    JSONArray dataArr = new JSONArray(jsonObject.getString("user_profile"));

                    JSONObject dataObj = new JSONObject(dataArr.get(0).toString());

                    if (mUserProfile.get() != null) {

                        profile.setFirstName(dataObj.getString("first_name"));
                        profile.setLastName(dataObj.getString("last_name"));
                        profile.setBirthday(dataObj.getString("birthday"));
                        profile.setGender(dataObj.getString("gender"));
                        profile.setHometown(dataObj.getString("hometown"));
                        profile.setLocation(dataObj.getString("location"));
                        profile.setProfilePictureLink(dataObj.getString("profile_picture_link"));
                        profile.setQuoteStatus(dataObj.getString("quote_status"));
                        profile.setNumOfFriends(dataObj.getInt("num_of_friends"));
                        profile.setNumOfPosts(dataObj.getInt("num_of_posts"));

                        // Adding to db
//                        mOperationManager.addUserProfile(profile);

                        notifyActivity();
                        Log.d("UserProfileAs", "Profile data is downloaded.");
                    }

                } else if (!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized"))) {

//                    activity.clearUserCredentials();

                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();

                } else if (!isCancelled() && !jsonObject.getBoolean("success")) {

                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    activity.showDialog(1004, bundle);

                } else throw new Exception();

            } else if (!isCancelled())
                throw new Exception();

        } catch (Exception e) {
            if (!isCancelled())
                activity.showDialog(1000);
        }

    }

    @Override
    protected void onCancelled(String result) {
        activity = null;
        mUserProfile = null;
    }

    /**
     * Notifies the activity that the data has been downloaded
     */
    private void notifyActivity() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
        broadcastManager.sendBroadcast(new Intent(ProfileActivity.PROFILE_DOWNLOADED));
    }


}
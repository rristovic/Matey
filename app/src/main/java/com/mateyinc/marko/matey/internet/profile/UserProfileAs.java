package com.mateyinc.marko.matey.internet.profile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/15/2016.
 */
public class UserProfileAs extends AsyncTask<String, Void, String> {

    private final BulletinManager mManager;
    MotherActivity activity;
    private WeakReference<UserProfile> mUserProfile;
    private int mUserId;

    public UserProfileAs(MotherActivity activity, WeakReference<UserProfile> userProfile) {
        this.activity = activity;
        this.mUserProfile = userProfile;
        mManager = BulletinManager.getInstance(activity);
    }

    @Override
    protected String doInBackground(String... params) {

        if (!isCancelled()) {

            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];
            String requested_user_id = params[3];
            mUserId = Integer.parseInt(user_id);

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8") + "&" +
                        URLEncoder.encode("requested_user_id", "UTF-8") + "=" + URLEncoder.encode(requested_user_id, "UTF-8");
                HTTP http = new HTTP(UrlData.REQUEST_USER_PROFILE, "POST");

                if (!isCancelled() && http.sendPost(data)) return http.getData();
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
                setPreDownloadedData(profile);

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

                        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
                        broadcastManager.sendBroadcast(new Intent(ProfileActivity.PROFILE_DOWNLOADED));
                        Log.d("UserProfileAs", "Profile data is downloaded.");
                    }

                } else if (!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized"))) {

                    activity.clearUserCredentials();

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

    private void setPreDownloadedData(UserProfile profile) {
        profile.setFirstName("Radovan"); // TODO - finish from cursor
    }

    @Override
    protected void onCancelled(String result) {
        activity = null;
        mUserProfile = null;
    }


}

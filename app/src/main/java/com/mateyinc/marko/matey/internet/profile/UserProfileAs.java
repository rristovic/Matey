package com.mateyinc.marko.matey.internet.profile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/15/2016.
 */
public class UserProfileAs extends AsyncTask<String, Void, String> {

    MotherActivity activity;

    public UserProfileAs(MotherActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {

        if (!isCancelled()) {

            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];
            String requested_user_id = params[3];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8") + "&" +
                        URLEncoder.encode("requested_user_id", "UTF-8") + "=" + URLEncoder.encode(requested_user_id, "UTF-8");
                HTTP http = new HTTP(UrlData.REQUEST_USER_PROFILE, "POST");

                if (http.sendPost(data)) return http.getData();


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
            if (result != null) {

                JSONObject jsonObject = new JSONObject(result);

                // if successful, set everything to SecurePreferences
                if (jsonObject.getBoolean("success")) {

                    // converting data
                    JSONArray dataArr = new JSONArray(jsonObject.getString("profile_data"));

                        JSONObject dataObj = new JSONObject(dataArr.get(0).toString());

                        UserProfile userProfile = new UserProfile();
                        userProfile.setFirstName(dataObj.getString("first_name"));
                        userProfile.setLastName(dataObj.getString("last_name"));
                        userProfile.setBirthday(dataObj.getString("birthday"));
                        userProfile.setGender(dataObj.getString("gender"));
                        userProfile.setHometown(dataObj.getString("hometown"));
                        userProfile.setLocation(dataObj.getString("location"));
                        userProfile.setProfilePictureLink(dataObj.getString("profile_picture_link"));
                        userProfile.setQuoteStatus(dataObj.getString("quote_status"));
                        userProfile.setNumOfFriends(dataObj.getInt("num_of_friends"));
                        userProfile.setNumOfPosts(dataObj.getInt("num_of_posts"));

                } else if (!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized"))) {

                    activity.clearUserCredentials();

                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();

                } else if (!jsonObject.getBoolean("success")) {

                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    activity.showDialog(1004, bundle);

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            activity.showDialog(1000);
        }

    }
}

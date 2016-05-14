package com.mateyinc.marko.matey.internet.profile;

import android.os.AsyncTask;
import android.os.Bundle;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/15/2016.
 */
public class ProfileAs extends AsyncTask<String, Void, String> {

    MotherActivity activity;

    public ProfileAs(MotherActivity activity) {
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

                    // punjenje objekata podacima
                    for (int i = 0; i < dataArr.length(); i++) {

                    }

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

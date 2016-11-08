package com.mateyinc.marko.matey.data.internet.procedures;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class LoginAs extends AsyncTask<String, Void, String> {

    WeakReference<MainActivity> mActivity;
    private ProgressDialog mProgDialog;

    public LoginAs(MainActivity activity) {
        this.mActivity = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... params) {

        if (!isCancelled()) {

            String username = params[0];
            String password = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
//                HTTP http = new HTTP(UrlData.LOG_URL, "POST");

//                if (http.sendPost(data)) return http.getData();

            } catch (Exception e) {

                Log.e("LoginAs", e.getMessage());
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
                    JSONArray dataArr = new JSONArray(jsonObject.getString("data"));
                    JSONObject dataObj = new JSONObject(dataArr.get(0).toString());

                    MainActivity activity = mActivity.get();
                    // put to preferences
                    if (activity != null) {
                        SecurePreferences preferences = activity.getSecurePreferences();
                        preferences.put("user_id", dataObj.getString("user_id"));
                        preferences.put("email", dataObj.getString("email"));
                        preferences.put("uid", dataObj.getString("uid"));
                        preferences.put("firstname", dataObj.getString("first_name"));
                        preferences.put("lastname", dataObj.getString("last_name"));
                    }
                    // notify user about successful login
//                   Toast.makeText(mActivity, "You have successfully login!", Toast.LENGTH_SHORT).show();
                    if (mProgDialog.isShowing())
                        mProgDialog.dismiss();

                    Intent intent = new Intent(activity, HomeActivity.class);
                    activity.startActivity(intent);
                    activity.finish();

                } else if (!jsonObject.getBoolean("success")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    if (mProgDialog.isShowing())
                        mProgDialog.dismiss();
                    MainActivity activity = mActivity.get();
                    if (activity != null)
                        activity.showDialog(1004, bundle);

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            // if there was an error, show corresponding alert dialog
            if (mProgDialog.isShowing())
                mProgDialog.dismiss();

            MainActivity activity = mActivity.get();
            if (activity != null)
                activity.showDialog(1000);
        }


    }

}

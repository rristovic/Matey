package com.mateyinc.marko.matey.internet.home;

import android.content.Intent;
import android.os.AsyncTask;

import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.model.Bulletin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/12/2016.
 */
public class BulletinAs extends AsyncTask<String,Void,String> {

    HomeActivity activity;
    BulletinManager bulletinManager;

    public BulletinAs(HomeActivity activity)
    {
        this.activity = activity;
        bulletinManager = BulletinManager.getInstance(activity);
    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String user_id = params[0];
            String uid = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(UrlData.FETCH_ALL_POSTS, "POST");

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
                    JSONArray dataArr = new JSONArray(jsonObject.getString("posts"));

                    // punjenje Bulletin objekata podacima
                    for(int i=0; i<dataArr.length(); i++) {

                        JSONObject dataObj = new JSONObject(dataArr.get(0).toString());

                        Bulletin bulletin = new Bulletin();
                        bulletin.setPostID(dataObj.getString("post_id"));
                        bulletin.setUserID(dataObj.getString("user_id"));
                        bulletin.setFirstName(dataObj.getString("first_name"));
                        bulletin.setLastName(dataObj.getString("last_name"));
                        bulletin.setDate(dataObj.getString("date_created"));
                        bulletin.setMessage(dataObj.getString("text"));

                        bulletinManager.addBulletin(bulletin);
                    }

                } else if(!jsonObject.getBoolean("success") && (jsonObject.getString("message").equals("not_logged") || jsonObject.getString("message").equals("not_authorized")) ) {

                    activity.clearUserCredentials();

                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            activity.showDialog(1000);
        }

    }

}

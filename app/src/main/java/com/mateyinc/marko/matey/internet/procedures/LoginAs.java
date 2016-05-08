package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class LoginAs extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String username = params[0];
            String password = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(UrlData.LOG_URL, "POST");

                if(http.sendPost(data)) return http.getData();

            } catch (Exception e) {

                return null;

            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}

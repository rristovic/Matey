package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import java.net.URLEncoder;

public class FacebookLoginAs extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String accessToken = params[0];
            String facebook_id = params[1];
            String firstname = params[2];
            String lastname = params[3];
            String email = params[4];
            String device_id = params[5];

            try {

                String data = URLEncoder.encode("accessToken", "UTF-8") + "=" + URLEncoder.encode(accessToken, "UTF-8") + "&" +
                        URLEncoder.encode("facebook_id", "UTF-8") + "=" + URLEncoder.encode(facebook_id, "UTF-8") + "&" +
                        URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("firstname", "UTF-8") + "=" + URLEncoder.encode(firstname, "UTF-8") + "&" +
                        URLEncoder.encode("lastname", "UTF-8") + "=" + URLEncoder.encode(lastname, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(UrlData.FACEBOOK_LOG_URL, "POST");

                if (http.sendPost(data)) return http.getData();


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

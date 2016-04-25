package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;

import com.mateyinc.marko.matey.data.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import java.net.URLEncoder;

public class FacebookLoginAs extends AsyncTask<String,Void,String> {

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String token = params[0];
            String fbid = params[1];
            String firstName = params[2];
            String lastName = params[3];
            String email = params[4];

            try {

                String data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8") + "&" +
                        URLEncoder.encode("fbid", "UTF-8") + "=" + URLEncoder.encode(fbid, "UTF-8") + "&" +
                        URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("firstName", "UTF-8") + "=" + URLEncoder.encode(firstName, "UTF-8") + "&" +
                        URLEncoder.encode("lastName", "UTF-8") + "=" + URLEncoder.encode(lastName, "UTF-8");
                HTTP http = new HTTP(UrlData.LOG_URL, "POST");

                if (http.sendPost(data)) return http.getData();


            } catch (Exception e) {

                return null;

            }

        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {

    }


}

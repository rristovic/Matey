package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/7/2016.
 */
public class RegisterAs extends AsyncTask<String,Void,String>{

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String email = params[0];
            String password = params[1];

            try {

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("firstname", "UTF-8") + "=" + URLEncoder.encode("Marko", "UTF-8") + "&" +
                        URLEncoder.encode("lastname", "UTF-8") + "=" + URLEncoder.encode("Ognjenovic", "UTF-8");
                HTTP http = new HTTP(UrlData.REGISTER_URL, "POST");

                if(http.sendPost(data)) return http.getData();

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

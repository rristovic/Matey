package com.mateyinc.marko.matey.internet.procedures;

import android.content.Context;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherAs;
import com.mateyinc.marko.matey.inall.ScepticTommy;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class LogoutAs extends MotherAs {

    public LogoutAs (Context context) {
        super(context);
    }

    @Override
    protected void onPreExecute() {

        if(!isCancelled()) activity.setContentView(ScepticTommy.WAITING_LAYOUT);

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String device_id = params[0];
            String uid = params[1];
            String username = params[2];

            try {

                String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
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

        activity.clearPreferencess();

        if(!isCancelled()) {

            if (result != null) {

                try {

                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getBoolean("success")) activity.setContentView(ScepticTommy.ERROR_LAYOUT);

                } catch (Exception e) {
                    activity.setContentView(ScepticTommy.ERROR_LAYOUT);
                }

            } else activity.setContentView(ScepticTommy.ERROR_LAYOUT);

        }

    }

}

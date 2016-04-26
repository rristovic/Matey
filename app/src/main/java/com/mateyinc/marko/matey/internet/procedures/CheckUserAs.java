package com.mateyinc.marko.matey.internet.procedures;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.inall.MotherAs;
import com.mateyinc.marko.matey.inall.ScepticTommy;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class CheckUserAs extends MotherAs {

    public CheckUserAs (MotherActivity activity) {
        super(activity);
    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String username = params[0];
            String uid = params[1];

            try {

                String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8");
                HTTP http = new HTTP(UrlData.CHECK_USER_URL, "GET");

                return http.getData();

            } catch (Exception e) {

                return null;

            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        if(!isCancelled()) {

            if (result != null) {

                try {

                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.getBoolean("success")) activity.setContentView(ScepticTommy.ERROR_LAYOUT);

                } catch (Exception e) {

                    activity.setContentView(ScepticTommy.ERROR_LAYOUT);

                }

            } else activity.setContentView(ScepticTommy.ERROR_LAYOUT);

        }

    }

}

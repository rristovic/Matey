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
public class LoginAs extends MotherAs {

    public LoginAs (MotherActivity activity) {
        super(activity);
    }

    @Override
    protected void onPreExecute() {

        if(!isCancelled()) activity.setContentView(ScepticTommy.WAITING_LAYOUT);

    }

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
        if(!isCancelled()) {

            if (result != null) {

                try {

                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getBoolean("success")) {

                        JSONObject data = jsonObject.getJSONObject("data");

                        activity.putToPreferences("firstname", data.getString("firstname"));
                        activity.putToPreferences("lastname", data.getString("lastname"));
                        activity.putToPreferences("email", data.getString("email"));
                        activity.putToPreferences("uid", data.getString("uid"));
                        activity.putToPreferences("user_id", data.getString("user_id"));

                    } else activity.setContentView(ScepticTommy.ERROR_LAYOUT);

                } catch (Exception e) {
                    activity.setContentView(ScepticTommy.ERROR_LAYOUT);
                }

            } else activity.setContentView(ScepticTommy.ERROR_LAYOUT);

        }

    }

}

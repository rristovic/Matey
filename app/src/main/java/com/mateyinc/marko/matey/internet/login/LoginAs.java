package com.mateyinc.marko.matey.internet.login;

import android.content.Context;
import android.os.AsyncTask;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class LoginAs extends AsyncTask<String,Void,String> {

    private Context context;
    MainActivity activity;

    public LoginAs(Context context) {

        if(context instanceof MainActivity) {
            this.context = context;
            activity = (MainActivity) context;
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String username = params[0];
            String password = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
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

        if(result != null) {

            try {

                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.getBoolean("success")) {

                    JSONObject data = jsonObject.getJSONObject("data");

                    activity.putToPreferences("firstname", data.getString("firstname"));
                    activity.putToPreferences("lastname", data.getString("lastname"));
                    activity.putToPreferences("uid", data.getString("uid"));

                } else activity.setContentView(R.layout.error_screen);

            } catch (Exception e) {

                activity.setContentView(R.layout.error_screen);

            }

        } else activity.setContentView(R.layout.error_screen);

    }

}

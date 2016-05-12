package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 5/7/2016.
 */
public class RegisterAs extends AsyncTask<String,Void,String>{

    MainActivity activity;

    public RegisterAs (MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String email = params[0];
            String password = params[1];
            String merge = params[2];
            String accessToken = params[3];

            try {

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("firstname", "UTF-8") + "=" + URLEncoder.encode("Marko", "UTF-8") + "&" +
                        URLEncoder.encode("lastname", "UTF-8") + "=" + URLEncoder.encode("Ognjenovic", "UTF-8") + "&" +
                        URLEncoder.encode("merge", "UTF-8") + "=" + URLEncoder.encode(merge, "UTF-8") + "&" +
                        URLEncoder.encode("accessToken", "UTF-8") + "=" + URLEncoder.encode(accessToken, "UTF-8");
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

        // if there is a result, check if it was successful
        try {

            if (result != null) {

                JSONObject jsonObject = new JSONObject(result);

                // if it was successful, take user to login page
                if (jsonObject.getBoolean("success")) {

                    Toast.makeText(activity, "You have successfully registered!", Toast.LENGTH_SHORT).show();
                    activity.startRegReverseAnim();
                    activity.mRegFormVisible = false;
                    activity.etEmail.setText("");
                    activity.etPass.setText("");

                } else if (!jsonObject.getBoolean("success") && jsonObject.getString("message").equals("facebook_acc")) {

                    Bundle bundle = new Bundle();
                    bundle.putString("name", jsonObject.getString("name"));
                    activity.showDialog(3, bundle);

                } else if (!jsonObject.getBoolean("success")){

                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    activity.showDialog(4, bundle);

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
                // if there was an error, show corresponding alert dialog
                activity.showDialog(0);
        }

    }

}

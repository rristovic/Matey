package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.mateyinc.marko.matey.activity.main.MainActivity;

import org.json.JSONObject;

/**
 * Created by M4rk0 on 5/7/2016.
 */
public class RegisterAs extends AsyncTask<String,Void,String>{

    MainActivity activity;
    private String result = "";

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
                    activity.showDialog(1003, bundle);

                } else if (!jsonObject.getBoolean("success")){

                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    activity.showDialog(1004, bundle);

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
                // if there was an error, show corresponding alert dialog
                activity.showDialog(1000);
        }

    }

}

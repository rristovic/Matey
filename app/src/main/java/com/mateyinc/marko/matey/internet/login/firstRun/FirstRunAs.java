package com.mateyinc.marko.matey.internet.login.firstRun;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mateyinc.marko.matey.data.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by M4rk0 on 3/7/2016.
 */
public class FirstRunAs extends AsyncTask<String,Void,String> {

    private Context context;

    public FirstRunAs (Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {

        String getDeviceIdUrl = "http://10.0.2.2/NotifindaAPI/web/index.php/api/user/firstRun";

        try{

            URL url = new URL(getDeviceIdUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");


            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String response = "";
            String line = "";
            while((line=br.readLine()) != null) {
                response += line;
            }
            br.close();

            httpURLConnection.disconnect();

            return response;

        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {
        try {
            if (result == null) Log.d("device", "Something went wrong.");
            else {
                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.getBoolean("success") == true) {
                    Log.d("device", jsonObject.getString("device_id"));

                    Device dev = new Device(context);
                    dev.putInformation(dev, jsonObject.getString("device_id"));

                } else {
                    Log.d("device", "Something went wrong.");
                }
            }

        } catch (JSONException e) {
            Log.d("device", "Something went wrong.");
        }
    }

}

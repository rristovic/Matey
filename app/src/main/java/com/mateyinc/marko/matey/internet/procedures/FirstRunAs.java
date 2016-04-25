package com.mateyinc.marko.matey.internet.procedures;

import android.content.Context;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data.UrlData;
import com.mateyinc.marko.matey.inall.MotherAs;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class FirstRunAs extends MotherAs {

    public FirstRunAs(Context context, int DESIRED_LAYOUT, int WAITING_LAYOUT, int ERROR_LAYOUT) {
        super(context, DESIRED_LAYOUT, WAITING_LAYOUT, ERROR_LAYOUT);
    }

    @Override
    protected void onPreExecute() {

        if(!isCancelled()) {

            activity.setContentView(WAITING_LAYOUT);

        } else activity.setContentView(ERROR_LAYOUT);

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {
            // get data from server
            // return null if something went wrong
            try {

                HTTP http = new HTTP (UrlData.FIRST_RUN_URL, "GET");

                return http.getData();

            } catch (Exception e) {

                return null;

            }

        }

        return null;

    }

    protected void onPostExecute(String result) {

        if (!isCancelled()) {

            try {
                // if returned null, show error screen
                if (result == null) {
                        activity.setContentView(ERROR_LAYOUT);
                } else {
                    // if data is here, see if it was successful
                    // if not show error screen
                    // else write it to file
                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getBoolean("success")) {

                        try {
                            FileOutputStream fOut = context.openFileOutput("did.dat", Context.MODE_PRIVATE);
                            OutputStreamWriter osw = new OutputStreamWriter(fOut);
                            osw.write(jsonObject.getString("device_id"));
                            osw.flush();
                            osw.close();

                            if (context instanceof MainActivity) {
                                activity.putToPreferences("device_id", jsonObject.getString("device_id"));
                                activity.setContentView(DESIRED_LAYOUT);
                            } else {
                                activity.setContentView(ERROR_LAYOUT);
                            }
                        } catch (Exception e) {
                            activity.setContentView(ERROR_LAYOUT);
                        }

                    } else {
                        activity.setContentView(ERROR_LAYOUT);
                    }
                }

            } catch (JSONException e) {
                    activity.setContentView(ERROR_LAYOUT);
            }

        }

    }

}

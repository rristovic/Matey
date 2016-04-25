package com.mateyinc.marko.matey.internet.login;

import android.content.Context;
import android.os.AsyncTask;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class FirstRunAs extends AsyncTask <String, Void, String> {

    private Context context;
    MainActivity activity;

    public FirstRunAs(Context context) {

        if(context instanceof MainActivity) {
            this.context = context;
            activity = (MainActivity) context;
        }

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        // on start of the execution, show waiting screen
        if(context instanceof MainActivity) {
            activity.setContentView(R.layout.waiting_screen);
        } else {
            cancel(true);
        }

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

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {

        if (!isCancelled()) {

            try {
                // if returned null, show error screen
                if (result == null) {
                    if (context instanceof MainActivity) {
                        activity.setContentView(R.layout.error_screen);
                    }
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
                                activity.setLoginScreen();
                            }
                        } catch (Exception e) {
                            if (context instanceof MainActivity) {
                                activity.setContentView(R.layout.error_screen);
                            }
                        }

                    } else {
                        if (context instanceof MainActivity) {
                            activity.setContentView(R.layout.error_screen);
                        }
                    }
                }

            } catch (JSONException e) {
                if (context instanceof MainActivity) {
                    activity.setContentView(R.layout.error_screen);
                }

            }

        }

    }

}

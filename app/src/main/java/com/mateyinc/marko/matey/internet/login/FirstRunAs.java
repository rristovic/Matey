package com.mateyinc.marko.matey.internet.login;

import android.content.Context;
import android.os.AsyncTask;

import com.mateyinc.marko.matey.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.UrlData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class FirstRunAs extends AsyncTask <String, Void, String> {

    private Context context;
    MainActivity activity;

    public FirstRunAs(Context context) {

        this.context = context;
        activity = (MainActivity) context;

    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        // on start of the execution, show waiting screen
        if(context instanceof MainActivity) {
            activity.setContentView(R.layout.waiting_screen);
        }

    }

    @Override
    protected String doInBackground(String... params) {

        // get data from server
        // return null if something went wrong
        try {

            URL url = new URL(UrlData.FIRST_RUN_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);

            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String response = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
            httpURLConnection.disconnect();

            if(response.equals("")) return null;

            return response;

        } catch (MalformedURLException e) {

            return null;

        } catch (IOException e) {

            return null;

        } catch (Exception e) {

            return null;

        }

    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {

        try {
            // if returned null, show error screen
            if (result == null) {
                if(context instanceof MainActivity) {
                    activity.setContentView(R.layout.error_screen);
                }
            }
            else {
                // uf data is here, see if it was successful
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
                            activity.setLoginScreen();
                        }
                    } catch (Exception e) {
                        if (context instanceof MainActivity) {
                            activity.setLoginScreen();
                        }
                    }

                } else {
                    if(context instanceof MainActivity) {
                        activity.setContentView(R.layout.error_screen);
                    }
                }
            }

        } catch (JSONException e) {
            if(context instanceof MainActivity) {
                activity.setContentView(R.layout.error_screen);
            }

        } catch (Exception e) {
            if(context instanceof MainActivity) {
                activity.setContentView(R.layout.error_screen);
            }
        }

    }

}

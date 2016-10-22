package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;
import android.util.Log;

import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by M4rk0 on 3/10/2016.
 */

/**
 * Class for working with the application id created on the server
 */
public class InstallationIDManager {

    private static final String TAG = InstallationIDManager.class.getSimpleName();
    public static final String APPID_FILE_NAME = "did.dat";

    /**
     * The application id is on hard drive
     */
    public static final int STATUS_OK = 100;

    /**
     * Error with getting the application id
     */
    public static final int STATUS_ERROR_APPID = 400;


    /**
     * Method for downloading application id from the server and saving it in securePrefs or reading it from file if it already exists
     * @param activity a context used for opening FileOutput etc
     * @return the status code. (STATUS_OK, STATUS_ERROR_APPID)
     */
    public int getInstallationID(Context activity, SecurePreferences securePreferences) {

        // try to read from file
        String device_id = readFromFile(activity);

        // if nothing was red it means that this is the first launch
        // of the application
        // so request device id from the server
        if(device_id.equals("")) {

            try {

                // TODO - use volley
                // http things, get device_id
                String result;
                HTTP http = new HTTP (UrlData.FIRST_RUN_URL, "GET");
                result = http.getData();

                    // if returned null
                    if (result == null) throw new Exception();

                    else {
                        // if data is here, see if it was successful
                        // if not show error screen
                        // else write it to file
                        JSONObject jsonObject = new JSONObject(result);

                        if (jsonObject.getBoolean("success")) {

                            FileOutputStream fOut = activity.openFileOutput(APPID_FILE_NAME, Context.MODE_PRIVATE);
                            OutputStreamWriter osw = new OutputStreamWriter(fOut);
                            osw.write(jsonObject.getString("device_id"));
                            osw.flush();
                            osw.close();

                            securePreferences.put("device_id", jsonObject.getString("device_id"));
                            Log.d(TAG, jsonObject.getString("device_id"));

                            return STATUS_OK;

                        } else throw new Exception();

                    }

            } catch (Exception e) {
                return STATUS_ERROR_APPID;
            }

        } else securePreferences.put("device_id", device_id);

        return STATUS_OK;

    }

    public String readFromFile (Context context) {
        StringBuffer datax = new StringBuffer("");

        try {
            FileInputStream fIn = context.openFileInput(APPID_FILE_NAME) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( Exception e ) {}

        return datax.toString() ;
    }

}

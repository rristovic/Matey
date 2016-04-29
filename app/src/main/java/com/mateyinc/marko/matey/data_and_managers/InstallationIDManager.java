package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;
import android.util.Log;

import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class InstallationIDManager {

    // function returns 7 if installationID is put in the SecurePreferences
    // otherwise it returns 0

    // THIS METHOD CAN ONLY BE CALLED FROM THE ASYNCTASK DOINBACKGROUND METHOD!!!

    public int retreveInstallationId (MotherActivity activity) {

        // try to read from file
        String device_id = readFromFile(activity);

        // if nothing was red it means that this is the first launch
        // of the application
        // so request device id from the server
        if(device_id.equals("")) {

            try {

                //FirstRunAs firstRun = new FirstRunAs(activity);

                //firstRun.execute();
                //String result = firstRun.get(30000, TimeUnit.MILLISECONDS);

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

                            FileOutputStream fOut = activity.openFileOutput("did.dat", Context.MODE_PRIVATE);
                            OutputStreamWriter osw = new OutputStreamWriter(fOut);
                            osw.write(jsonObject.getString("device_id"));
                            osw.flush();
                            osw.close();

                            activity.putToPreferences("device_id", jsonObject.getString("device_id"));
                            Log.d("evee", jsonObject.getString("device_id"));
                            return 7;

                        } else throw new Exception();

                    }

            } catch (Exception e) {
                return 0;
            }

        } else activity.putToPreferences("device_id", device_id);

        return 7;

    }

    public String readFromFile (Context context) {
        StringBuffer datax = new StringBuffer("");

        try {
            FileInputStream fIn = context.openFileInput("did.dat") ;
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

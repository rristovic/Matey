package com.mateyinc.marko.matey.data;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.internet.login.FirstRunAs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class InstallationID {

    public static String retreveInstallationId (Context context) {
        AppCompatActivity activity;
        // Make shure that this method is called
        // only from MainActivity
        // if not, do nothing
        if(context instanceof MainActivity) {
            activity = (AppCompatActivity) context;
            activity.setContentView(R.layout.error_screen);
        } else {
            return "";
        }

        // try to read from file
        String device_id = readFromFile(context);

        // if nothing was red it means that this is the first launch
        // of the application
        // so request device id from the server
        if(device_id.equals("")) {
            FirstRunAs firstRun = new FirstRunAs(context);
            try {
                firstRun.execute();
            } catch (Exception e){
                activity.setContentView(R.layout.error_screen);
            }
        }

        // if returned empty string it means that installation ID
        // request is started
        return device_id ;
    }

    public static String readFromFile (Context context) {
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
        } catch ( Exception e ) {

        }
        return datax.toString() ;
    }

}

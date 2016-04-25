package com.mateyinc.marko.matey.data;

import android.content.Context;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.inall.ScepticTommy;
import com.mateyinc.marko.matey.internet.procedures.FirstRunAs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class InstallationIDManager {

    public static String retreveInstallationId (Context context) {

        MotherActivity activity;
        // Make shure that this method is called
        // only from MainActivity
        // if not, do nothing
        if(context instanceof MainActivity) {
            activity = (MotherActivity) context;
            activity.setContentView(ScepticTommy.ERROR_LAYOUT);
        } else {
            return "";
        }

        // try to read from file
        String device_id = readFromFile(context);

        // if nothing was red it means that this is the first launch
        // of the application
        // so request device id from the server
        if(device_id.equals("")) {
            FirstRunAs firstRun = new FirstRunAs(context, R.layout.main_activity, ScepticTommy.WAITING_LAYOUT, ScepticTommy.ERROR_LAYOUT);
            try {
                firstRun.execute();
            } catch (Exception e){
                activity.setContentView(ScepticTommy.ERROR_LAYOUT);
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

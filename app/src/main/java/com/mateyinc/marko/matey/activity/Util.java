package com.mateyinc.marko.matey.activity;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by Sarma on 9/2/2016.
 */
public class Util {
    public static String getReadableDateText(Date date) {
        // TODO - set the timezone to server timezone
        //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        // TODO - Get correct day with timezone
        Date now = new Date();
        int hour = Math.round(now.getTime() - date.getTime()) / (1000 * 60 * 60);
        if (hour > 24)
            return hour / 24 + " days ago";
        else if (hour >= 1)
            return hour + " hours ago";
        else {
            return (now.getTime() - date.getTime()) / (1000 * 60) + " mins ago";
        }
    }

    public static String getReadableDateText(String date) {
        Date d = new Date(date);
        // TODO - set the timezone to server timezone
        //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        // TODO - Get correct day with timezone
        Date now = new Date();
        int hour = Math.round(now.getTime() - d.getTime()) / (1000 * 60 * 60);
        if (hour > 24)
            return hour / 24 + " days ago";
        else if (hour >= 1)
            return hour + " hours ago";
        else {
            return (now.getTime() - d.getTime()) / (1000 * 60) + " mins ago";
        }
    }


    public static int getCurrentUserProfileId() {
        return -1;
    }

    //load file from apps res/raw folder or Assets folder
    public static String LoadFile(String fileName, boolean loadFromRawFolder, Context context) throws IOException {
        Resources resources = context.getResources();
        //Create a InputStream to read the file into
        InputStream iS;

        if (loadFromRawFolder) {
            //get the resource id from the file name
            int rID = resources.getIdentifier(fileName, "raw", context.getPackageName());

            //get the file as a stream
            iS = resources.openRawResource(rID);
        } else {
            //get the file as a stream
            iS = resources.getAssets().open(fileName);
        }

        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[iS.available()];
        //read the text file as a stream, into the buffer
        iS.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        //write this buffer to the output stream
        oS.write(buffer);
        //Close the Input and Output streams
        oS.close();
        iS.close();

        context = null;
        resources = null;

        //return the output stream as a String
        return oS.toString();
    }

    public static int getDp(int value, Resources res) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, res.getDisplayMetrics());
    }
}

package com.mateyinc.marko.matey.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.main.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    /**
     * Helper method to return user readable date string which represents
     * the time passed from time provided to current time
     *
     * @param date the date that object was created on
     * @return string representing time passed since the provided time
     */
    public static String getReadableDateText(String date) {
        if (date != null || date.length() == 0) {
            return "Invalid date";
        }
        Date d = new Date(date);
        // TODO - set the timezone to server timezone
        //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        // TODO - Get correct day with timezone
        Date now = new Date();
        int hour = Math.round(now.getTime() - d.getTime()) / (1000 * 60 * 60);
        if (hour >= 24)
            return hour / 24 + " days ago";
        else if (hour >= 1)
            return hour + " hours ago";
        else {
            return (now.getTime() - d.getTime()) / (1000 * 60) + " mins ago";
        }
    }

    /**
     * @see #getReadableDateText(Date)
     */
    public static String getReadableDateText(Date date) {
        if (date == null) {
            return "Invalid date";
        }
        // TODO - set the timezone to server timezone
        //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

        // TODO - Get correct day with timezone
        Date now = new Date();
        int hour = Math.round(now.getTime() - date.getTime()) / (1000 * 60 * 60);
        if (hour >= 24)
            return hour / 24 + " days ago";
        else if (hour >= 1)
            return hour + " hours ago";
        else {
            return (now.getTime() - date.getTime()) / (1000 * 60) + " mins ago";
        }
    }

    /**
     * @see #getReadableDateText(String)
     */
    public static String getReadableDateText(long timeInMilis) {
        return getReadableDateText(new Date(timeInMilis));
    }

//    //load file from apps res/raw folder or Assets folder
//    public static String LoadFile(String fileName, boolean loadFromRawFolder, Context context) throws IOException {
//        Resources resources = context.getResources();
//        //Create a InputStream to read the file into
//        InputStream iS;
//
//        if (loadFromRawFolder) {
//            //get the resource id from the file name
//            int rID = resources.getIdentifier(fileName, "raw", context.getPackageName());
//
//            //get the file as a stream
//            iS = resources.openRawResource(rID);
//        } else {
//            //get the file as a stream
//            iS = resources.getAssets().open(fileName);
//        }
//
//        //create a buffer that has the same size as the InputStream
//        byte[] buffer = new byte[iS.available()];
//        //read the text file as a stream, into the buffer
//        iS.read(buffer);
//        //create a output stream to write the buffer into
//        ByteArrayOutputStream oS = new ByteArrayOutputStream();
//        //write this buffer to the output stream
//        oS.write(buffer);
//        //Close the Input and Output streams
//        oS.close();
//        iS.close();
//
//        context = null;
//        resources = null;
//
//        //return the output stream as a String
//        return oS.toString();
//    }

    public static float parseDp(float value, Resources res) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, res.getDisplayMetrics());
    }

    /**
     * Method for parsing the date string into {@link Date}
     *
     * @param date the date string retrieved from the server in format "YYYY-mm-DD HH:MM:SS"
     * @return the Date object represented by the date argument
     */
    public static Date parseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return format.parse(date);
        } catch (ParseException e) {
            Log.d("Util", "Failed parsing date: " + date);
        }

        return new Date();
    }


    /**
     * Helper method to check if the is an internet connection
     *
     * @param context the context used for getting the system service
     * @return true if there is an internet connection; <br> false otherwise;
     */
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private static AlertDialog mDialog;

    public static void showServerNotResponding(MainActivity context, DialogInterface.OnClickListener listener) {
        if (mDialog == null || !mDialog.isShowing())
            mDialog = new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.server_not_responding_msg))
                    .setIcon(R.drawable.matey_logo)
                    .setNeutralButton("OK", listener)
                    .setCancelable(false)
                    .show();
    }

    /**
     * Helper method to build an {@link AlertDialog} and show it in the provided context
     *
     * @param context  the {@link Context} in which to show the dialog
     * @param message  the message string for the dialog
     * @param title    the dialog title
     * @param listener the ClickListener callback for the neutral button
     */
    public static void showAlertDialog(Context context, String message, String title, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setIcon(R.drawable.matey_logo)
                .setNeutralButton("OK", listener)
                .setCancelable(false)
                .setTitle(title)
                .show();
    }

    /**
     * Helper method to build an {@link AlertDialog} and show it in the provided context
     *
     * @param context         the {@link Context} in which to show the dialog
     * @param message         the message string for the dialog
     * @param title           the dialog title
     * @param firstListener   the first button callback listener
     * @param firstBtnTitle   the first button tittle
     * @param secondListener  the second button callback listener
     * @param secondBtnTittle the second button title
     */
    public static void showTwoBtnAlertDialog(Context context, String message, String title, DialogInterface.OnClickListener firstListener, String firstBtnTitle, DialogInterface.OnClickListener secondListener, String secondBtnTittle) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setIcon(R.drawable.matey_logo)
                .setPositiveButton(firstBtnTitle, firstListener)
                .setNegativeButton(secondBtnTittle, secondListener)
                .setCancelable(false)
                .setTitle(title)
                .show();
    }


    public static final int ONE_DAY = 86400000;
    public static final int ONE_MIN = 60000;

}

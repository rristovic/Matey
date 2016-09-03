package com.mateyinc.marko.matey.activity;

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
    }public static String getReadableDateText(String date) {
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
}

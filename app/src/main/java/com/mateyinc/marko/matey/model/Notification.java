package com.mateyinc.marko.matey.model;

import java.util.Date;

/**
 * Created by Sarma on 8/28/2016.
 */
public class Notification {
    private String mMessage;
    private Date mDate;

    public Notification(String message, Date date){
        mMessage = message;
        mDate = date;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }
}

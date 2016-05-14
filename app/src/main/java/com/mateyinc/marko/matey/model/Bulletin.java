package com.mateyinc.marko.matey.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class Bulletin {
    private String mFirstName;
    private String mLastName;
    private Date mDate;
    private String mMessage;
    private String mPostID;
    private String mUserID;
    private LinkedList<BulletinAttachment> mAttachments;

    public String getPostID() {
        return mPostID;
    }

    public void setPostID(String mPostID) {
        this.mPostID = mPostID;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String mUserID) {
        this.mUserID = mUserID;
    }


    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(String mDateString) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.mDate = dateFormat.parse(mDateString);
        } catch (Exception e) {
        }

    }

    public void setDate(Date date){
        this.mDate = date;
    }

    public LinkedList<BulletinAttachment> getAttachments() {
        return mAttachments;
    }

    public void setAttachments(LinkedList<BulletinAttachment> mAttachments) {
        this.mAttachments = mAttachments;
    }

    @Override
    public String toString() {
        return "Message: " + mMessage
                + "; From: " + mFirstName + " " + mLastName
                + "; Date: " + mDate;
    }
}

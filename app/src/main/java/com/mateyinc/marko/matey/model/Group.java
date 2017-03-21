package com.mateyinc.marko.matey.model;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Group extends MModel {

    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_NUM_OF_FOLLOWERS = "num_of_followers";
    private static final String KEY_GROUP_PIC_URL = "group_picture_url";

    private String mGroupName;
    private String mDescription;
    private Date mDateCreated;
    private String mPicUrl;
    private int mNumOfFollowers;

    public Group() {
    }

    public Group(long groupId) {
        this._id = groupId;
    }


    @Override
    public void onDownloadSuccess(String response, Context c) {

    }

    @Override
    public void onDownloadFailed(String error, Context c) {

    }

    @Override
    public void onUploadSuccess(String response, Context c) {

    }

    @Override
    public void onUploadFailed(String error, Context c) {

    }

    @Override
    public Group parse(JSONObject object) throws JSONException {
        this._id = object.getLong(KEY_GROUP_ID);
        this.mGroupName = object.getString(KEY_GROUP_ID);
        this.mNumOfFollowers = object.getInt(KEY_NUM_OF_FOLLOWERS);
        this.mPicUrl = object.getString(KEY_GROUP_PIC_URL);
//        this.mDateCreated = Util.parseDate(object.getString(KEY_DATE_ADDED));
        return this;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String mGroupName) {
        this.mGroupName = mGroupName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public Date getDateCreated() {
        return mDateCreated != null ? mDateCreated : new Date();
    }

    public void setDateCreated(Date mDateCreated) {
        this.mDateCreated = mDateCreated;
    }

    public String getPicUrl() {
        return mPicUrl != null ? mPicUrl : "#";
    }

    public void setPicUrl(String mPicUrl) {
        this.mPicUrl = mPicUrl;
    }

    public int getNumOfFollowers() {
        return mNumOfFollowers;
    }

    public void setNumOfFollowers(int mNumOfFollowers) {
        this.mNumOfFollowers = mNumOfFollowers;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            Group g = (Group) obj;
            return this._id == g._id;
        } catch (ClassCastException e) {
            return false;
        }
    }
}

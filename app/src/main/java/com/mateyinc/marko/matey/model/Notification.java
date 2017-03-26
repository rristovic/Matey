package com.mateyinc.marko.matey.model;

import android.content.Context;
import android.content.Intent;

import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sarma on 8/28/2016.
 */
public class Notification {

    private static final String KEY_ACTIVITY_ID = "activity_id";
    private static final String KEY_ACTIVITY_TYPE = "activity_type";
    // Activity type values
    private static final String VALUE_TYPE_APPROVE = "APPROVE";
    private static final String VALUE_TYPE_BOOST = "BOOST";
    private static final String VALUE_TYPE_FOLLOW = "FOLLOW";
    private static final String VALUE_TYPE_POST_CREATE = "POST_CREATE";
    private static final String VALUE_TYPE_REPLY_CREATE = "REPLY_CREATE";
    private static final String VALUE_TYPE_RE_REPLY_CREATE = "REREPLY_CREATE";

    private static final String KEY_TIME_C = "time_c";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PIC_URL = "picture_url";
    private static final String KEY_MSG = "message";

    private static final String KEY_POST = "post";
    private static final String KEY_USER = "user";
    private static final String KEY_REPLY = "reply";
    private static final String KEY_REREPLY = "rereply";

    private static final String KEY_SOURCE_TYPE = "source_type";
    private static final String KEY_PARENT_TYPE = "parent_type";
    // Source type and parent type values
    private static final String VALUE_TYPE_GROUP = "GROUP";
    private static final String VALUE_TYPE_USER = "MATEY_USER";
    private static final String VALUE_TYPE_BULLETIN = "POST";
    private static final String VALUE_TYPE_REPLY = "REPLY";
    private static final String VALUE_TYPE_REREPLY = "REREPLY";

    private long postId, replyId, reReplyId, userId;

    private String mActivityType;
    private Class mClass;
    private String mMessage;
    private String mTitle;
    private String mPicUrl;
    private String mDate;

    public Notification() {
    }

    public Notification parse(JSONObject object) throws JSONException {
        mActivityType = object.getString(KEY_ACTIVITY_TYPE);
        parseNotifInfo(object);
        switch (mActivityType) {
            case VALUE_TYPE_APPROVE:
                throw new JSONException("Not implemented.");
            case VALUE_TYPE_BOOST:
                mClass = BulletinViewActivity.class;
                this.postId = object.getJSONObject(KEY_POST).getLong(Bulletin.KEY_POST_ID);
                break;
            case VALUE_TYPE_FOLLOW:
                mClass = ProfileActivity.class;
                userId = object.getJSONObject(KEY_USER).getLong(UserProfile.KEY_ID);
                break;
            case VALUE_TYPE_POST_CREATE:
                throw new JSONException("Not implemented.");
            case VALUE_TYPE_REPLY_CREATE:
                mClass = BulletinViewActivity.class;
                postId = object.getJSONObject(KEY_POST).getLong(Bulletin.KEY_POST_ID);
                replyId = object.getJSONObject(KEY_REPLY).getLong(Reply.KEY_REPLY_ID);
                break;
            case VALUE_TYPE_RE_REPLY_CREATE:
                mClass = BulletinViewActivity.class;
                postId = object.getJSONObject(KEY_POST).getLong(Bulletin.KEY_POST_ID);
                replyId = object.getJSONObject(KEY_REPLY).getLong(Reply.KEY_REPLY_ID);
                reReplyId = object.getJSONObject(KEY_REREPLY).getLong(Reply.KEY_RE_REPLY_ID);
                break;
            default:
                throw new JSONException("Not implemented.");
        }

        return this;
    }

    private void parseNotifInfo(JSONObject object) throws JSONException{
        mDate = object.getString(KEY_TIME_C);
        mTitle = object.getString(KEY_TITLE);
        mPicUrl = object.getString(KEY_PIC_URL);
        mMessage = object.getString(KEY_MSG);
    }

    public Intent buildIntent(Context context) {
        Intent i = new Intent(context, mClass);
        switch (mActivityType) {
            case VALUE_TYPE_APPROVE:
                return null;
            case VALUE_TYPE_BOOST:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, postId);
                i.setAction(HomeActivity.ACTION_SHOW_BULLETIN);
                break;
            case VALUE_TYPE_FOLLOW:
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, userId);
                i.setAction(HomeActivity.ACTION_SHOW_PROFILE);
                break;
            case VALUE_TYPE_POST_CREATE:
                return null;
            case VALUE_TYPE_REPLY_CREATE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID,
                        postId);
                i.putExtra("1",
                        replyId);
                i.setAction(HomeActivity.ACTION_SHOW_REPLY);
                break;
            case VALUE_TYPE_RE_REPLY_CREATE:
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID,
                        postId);
                i.putExtra("1", replyId);
                i.putExtra("2", reReplyId);
                i.setAction(HomeActivity.ACTION_SHOW_REREPLY);
                break;
            default:
                return null;
        }

        return i;
    }


    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getReplyId() {
        return replyId;
    }

    public void setReplyId(long replyId) {
        this.replyId = replyId;
    }

    public long getReReplyId() {
        return reReplyId;
    }

    public void setReReplyId(long reReplyId) {
        this.reReplyId = reReplyId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getmActivityType() {
        return mActivityType;
    }

    public void setmActivityType(String mActivityType) {
        this.mActivityType = mActivityType;
    }

    public Class getmClass() {
        return mClass;
    }

    public void setmClass(Class mClass) {
        this.mClass = mClass;
    }

    public String getmMessage() {
        return mMessage;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmPicUrl() {
        return mPicUrl;
    }

    public void setmPicUrl(String mPicUrl) {
        this.mPicUrl = mPicUrl;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }
}

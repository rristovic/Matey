package com.mateyinc.marko.matey.model;

import android.util.Log;

import com.mateyinc.marko.matey.data.DataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Reply extends MModel{
    private static final String TAG = Reply.class.getSimpleName();

    // Keys for JSON data
    public static final String REPLY_ID = "reply_id";
    public static final String FIRST_NAME = "reply_username";
    public static final String LAST_NAME = "reply_lastname";
    public static final String USER_ID = "reply_userid";
    public static final String DATE = "reply_date";
    public static final String TEXT = "reply_text";
    public static final String REPLY_APPRVS = "reply_approves";

    /**
     * The ID of the reply
     */
    public long replyId;
    /**
     * The ID of the user that has replied
     */
    public long userId;
    /**
     * The ID of the post/bulletin that has been replied on
     */
    public long postId;

    public String userFirstName;
    public String userLastName;
    public Date replyDate;
    public String replyText;
    public int numOfApprvs;
    public LinkedList<UserProfile> replyApproves;// Not in DB

    public Reply() {
        replyApproves = new LinkedList<>();
    }

    public boolean hasReplyApproveWithId(long id) {
        for (UserProfile p :
                replyApproves) {
            if (p != null && p.getUserId() == id)
                return true;
        }
        return false;
    }

    public boolean removeReplyApproveWithId(int id) {
        for (UserProfile p :
                replyApproves) {
            if (p != null && p.getUserId() == id) {
                replyApproves.remove();
                return true;
            }
        }
        return false;
    }

    public LinkedList<UserProfile> setApprovesFromJSON(String string) {
        LinkedList<UserProfile> apprvs = new LinkedList<>();
        if (string == null || string.length() == 0) {
            return apprvs;
        }

        try {
            JSONObject jObject = new JSONObject(string);
            JSONArray jArray = jObject.getJSONArray(DataManager.REPLY_APPRVS);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject apprv = jArray.getJSONObject(i);

                UserProfile r = new UserProfile();
                r.setUserId(apprv.getInt(UserProfile.USER_ID));

                apprvs.add(r);
            }
        } catch (JSONException jse) {
            Log.e(TAG, jse.getLocalizedMessage(), jse);
        }

        return apprvs;
    }

    public void setDate(String mDateString) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            replyDate = dateFormat.parse(mDateString);
        } catch (Exception e) {
            try {
                replyDate = new Date(mDateString);
            } catch (Exception es) {
                Log.e(TAG, es.getLocalizedMessage(), es);
                replyDate = new Date();
            }
        }

    }

    public void setDate(Date date) {
        replyDate = date;
    }

    public void setDate(long timeInMilis) {
        replyDate = new Date(timeInMilis);
    }


    @Override
    public String toString() {
        return "Reply: " + "id=" + replyId + "; postId=" + postId
                + "; From: " + userFirstName + " " + userLastName
                + "; Date: " + replyDate +
                "; Text=" + replyText;
    }
}

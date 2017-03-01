package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

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

    /** The ID of the user that has replied */
    public long userId;
    /** The ID of the post/bulletin that has been replied on */
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


    public void addToDb(Context c) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry._ID, _id);
        values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, userFirstName);
        values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, userFirstName);
        values.put(DataContract.ReplyEntry.COLUMN_TEXT, replyText);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, replyDate.getTime());
        values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_LIKES, numOfApprvs);
        values.put(DataContract.ReplyEntry.COLUMN_SERVER_STATUS, mServerStatus);

        // Add to db
        Uri insertedUri = c.getContentResolver().insert(
                DataContract.ReplyEntry.CONTENT_URI,
                values
        );

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting Reply: " + Reply.this);
        } else {
            Log.e(TAG, "New reply added: " + Reply.this);
        }
    }


    @Override
    public String toString() {
        String text, date;
        try {
            text = replyText.substring(0,15).concat("...");
        } catch (Exception e){
            text = replyText;
        }

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(replyDate);
        } catch (Exception e){
            date = replyDate.toString();
        }
        return String.format(Locale.US, "Reply: ID=%d; Text=%s; UserName=%s %s; UserId=%d; Date=%s",
                _id, text, userFirstName, userLastName, userId, date);
    }
}

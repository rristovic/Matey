package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.internet.MateyRequest;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.data.updater.DataUpdater;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;

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


    @Override
    public void upload(final Context context, RequestQueue queue, String accessToken) {
        // Adding to db
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                addToDb(context);
            }
        });
        t.start();

        // Creating net request
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_REPLY_ROUTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                uploadSucceeded(response, context);
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                uploadFailed(error, context);
                            }
                        });
                        t.start();
                    }
                }
        );
        uploadRequest.setAuthHeader(accessToken);
        uploadRequest.addParam(UrlData.PARAM_REPLY_POST_ID, Long.toString(postId));
        uploadRequest.addParam(UrlData.PARAM_REPLY_TEXT_DATA, replyText);

        // Send request to the network
        queue.add(uploadRequest);
    }

    @Override
    protected void uploadSucceeded(String response, Context context) {
        try {
            JSONObject object = new JSONObject(response);
            // Parse data
            Date date = Util.parseDate(object.getString(Reply.DATE));
            // Update data
            Reply.this.updateIdAndDate(_id, object.getLong(Reply.REPLY_ID),
                    date, DataUpdater.REPLY_CLASS, context );
        } catch (ParseException | JSONException e){
            uploadFailed(e, context);
        }
    }

    @Override
    protected void uploadFailed(Exception e, Context context) {
        Reply.this.updateServerStatus(STATUS_RETRY_UPLOAD, DataUpdater.REPLY_CLASS, context);
        Log.e(TAG, e.getLocalizedMessage(), e);
    }

    @Override
    public void addToDb(Context c) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ReplyEntry._ID, _id);
        values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, userFirstName);
        values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, userFirstName);
        values.put(DataContract.ReplyEntry.COLUMN_TEXT, replyText);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, replyDate.getTime());
        values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, numOfApprvs);
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
    public void download(Context context, RequestQueue queue, String accessToken) {

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

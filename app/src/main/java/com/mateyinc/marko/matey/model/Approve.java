package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.MateyRequest;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.data.updater.DataUpdater;

import java.util.Locale;

import static com.facebook.login.widget.ProfilePictureView.TAG;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;

public class Approve extends MModel{

    public Approve(long userId, long postId, long replyId){
        this.userId = userId;
        this.postId = postId;
        this.replyId = replyId;
    }

    public long userId;
    public long postId;
    public long replyId;

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
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_LIKE_ROUTE,
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
        uploadRequest.addParam(UrlData.PARAM_LIKED_POST_ID, Long.toString(postId));
        uploadRequest.addParam(UrlData.PARAM_LIKED_REPLY_ID, Long.toString(_id));

        // Send request to the network
        queue.add(uploadRequest);
    }

    @Override
    protected void uploadSucceeded(String response, Context context) {
        Approve.this.updateServerStatus(DataManager.ServerStatus.STATUS_SUCCESS, DataUpdater.APPROVE_CLASS, context);
    }

    @Override
    protected void uploadFailed(Exception e, Context context) {
        Approve.this.updateServerStatus(STATUS_RETRY_UPLOAD, DataUpdater.APPROVE_CLASS, context);
        Log.e(TAG, e.getLocalizedMessage(), e);
    }

    @Override
    public void addToDb(Context c) {
        ContentValues values = new ContentValues();

        values.put(DataContract.ApproveEntry.COLUMN_POST_ID, postId);
        values.put(DataContract.ApproveEntry.COLUMN_REPLY_ID, replyId);
        values.put(DataContract.ApproveEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.ApproveEntry.COLUMN_SERVER_STATUS, mServerStatus);

        // Add to db
        Uri insertedUri = c.getContentResolver().insert(
                DataContract.ApproveEntry.CONTENT_URI,
                values
        );

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting: " + Approve.this);
        } else {
            Log.e(TAG, "New approve added: " + Approve.this);
        }
    }

    @Override
    public void download(Context context, RequestQueue queue, String accessToken) {

    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Approve: UserId=%d; ReplyId=%d; PostId=%d", userId, replyId, postId);
    }
}

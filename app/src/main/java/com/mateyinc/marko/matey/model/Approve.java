package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;

import java.util.Locale;

import static com.facebook.login.widget.ProfilePictureView.TAG;

public class Approve extends MModel{

    public Approve(long userId, long postId, long replyId){
        this.userId = userId;
        this.postId = postId;
        this.replyId = replyId;
    }

    public long userId;
    public long postId;
    public long replyId;


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
    public String toString() {
        return String.format(Locale.US, "Approve: UserId=%d; ReplyId=%d; PostId=%d", userId, replyId, postId);
    }
}

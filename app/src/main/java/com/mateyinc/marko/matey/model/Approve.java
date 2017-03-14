package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ApproveEntry;
import com.mateyinc.marko.matey.internet.operations.ApproveOp;
import com.mateyinc.marko.matey.internet.operations.OperationType;

import java.util.Locale;

public class Approve extends MModel {
    private static final String TAG = Approve.class.getSimpleName();

    public Approve(long userId, long postId, long replyId) {
        this.userId = userId;
        this.postId = postId;
        this.replyId = replyId;
    }

    public Approve() {
    }

    public Approve(long _id) {
        this._id = _id;
    }

    public long userId = -1;
    public long postId = -1;
    public long replyId = -1;


    /**
     * Method to call when post/reply has been liked/unliked. See also {@link MModel#save(Context)}.*
     * @param context context used for database communication.
     */
    @Override
    public void save(Context context) {
       save(context, DataAccess.isApproveInDb(postId, replyId, context));
    }

    public void save(Context context, boolean approveExists) {
        ApproveOp op = new ApproveOp(context, this);

        if (approveExists) { // Unlike
            if (replyId != -1)
                op.setOperationType(OperationType.REPLY_UNLIKED);
            else
                op.setOperationType(OperationType.POST_UNLIKED);

            // Remove post form db. If upload fail, put it back in
            removeFromDb(context);
        } else {
            if (replyId != -1)
                op.setOperationType(OperationType.REPLY_LIKED);
            else
                op.setOperationType(OperationType.POST_LIKED);

            // Add to db
            addToDb(context);
        }

        // Save to not uploaded
        addToNotUploaded(TAG, context);
        op.startUploadAction();
    }

    @Override
    protected void addToDb(Context context) {
        ContentValues values = new ContentValues();

        values.put(ApproveEntry._ID, this._id);
        values.put(ApproveEntry.COLUMN_POST_ID, postId);
        values.put(ApproveEntry.COLUMN_REPLY_ID, replyId);
        values.put(ApproveEntry.COLUMN_USER_ID, userId);
        values.put(ApproveEntry.COLUMN_SERVER_STATUS, mServerStatus.ordinal());

        // Add to db
        Uri insertedUri = context.getContentResolver().insert(
                DataContract.ApproveEntry.CONTENT_URI,
                values
        );

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting to database: " + Approve.this);
        } else {
            Log.d(TAG, "New approve added to database: " + Approve.this);
        }
    }

    @Override
    protected void removeFromDb(Context context) {
        // Remove from db
        int deleted = context.getContentResolver().delete(DataContract.ApproveEntry.CONTENT_URI,
                ApproveEntry.COLUMN_POST_ID + " = ? AND " + ApproveEntry.COLUMN_REPLY_ID + " = ?",
                new String[]{Long.toString(postId), Long.toString(replyId)});

        if (deleted == 1) {
            Log.d(TAG, "Approve removed from database: " + Approve.this);
        } else {
            Log.e(TAG, "Error removing from database: " + Approve.this);
        }
    }


    @Override
    public void onDownloadSuccess(String response, Context c) {
        // Parse data


        // Save data
        addToDb(c);
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
    protected void notifyDataChanged(Context context) {

    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Approve(%d): UserId=%d; ReplyId=%d; PostId=%d", this._id, userId, replyId, postId);
    }

}

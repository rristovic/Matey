package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ApproveEntry;

import org.json.JSONException;
import org.json.JSONObject;

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




    public void save(Context context, boolean approveExists) {
//        ApproveOp op = new ApproveOp(context, this);
//
//        if (approveExists) { // Unlike
//            if (replyId != -1)
//                op.setOperationType(OperationType.REPLY_UNLIKED);
//            else
//                op.setOperationType(OperationType.POST_UNLIKED);
//
//            // Remove post form db. If upload fail, put it back in
//            removeFromDb(context);
//        } else {
//            if (replyId != -1)
//                op.setOperationType(OperationType.REPLY_LIKED);
//            else
//                op.setOperationType(OperationType.POST_LIKED);
//
//            // Add to db
//            addToDb(context);
//        }
//
//        // Save to not uploaded
//        addToNotUploaded(TAG, context);
//        op.startUploadAction();
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
    public MModel parse(JSONObject object) throws JSONException {
        return null;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Approve(%d): UserId=%d; ReplyId=%d; PostId=%d", this._id, userId, replyId, postId);
    }

}

package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ReplyEntry;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.internet.operations.Operations;
import com.mateyinc.marko.matey.internet.operations.ReplyOp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Reply extends MModel {
    private static final String TAG = Reply.class.getSimpleName();

    // Keys for JSON data
    public static final String KEY_REPLY_ID = "reply_id";
    public static final String KEY_NUM_OF_APPROVES = "num_of_approves";
    public static final String KEY_FIRST_NAME = "reply_username";
    public static final String KEY_LAST_NAME = "reply_lastname";
    public static final String KEY_USER_ID = "reply_userid";
    public static final String KEY_DATE = "reply_date";
    public static final String KEY_TEXT = "reply_text";
    public static final String KEY_REPLY_APPRVS = "reply_approves";

    /**
     * The ID of the user that has replied
     */
    private long userId;
    /**
     * The ID of the post/bulletin that has been replied on
     */
    private long postId;
    /**
     * The ID of the reply that has been replied on
     */
    private long reReplyId;

    public boolean isPostReply = true;
    private String userFirstName;
    private String userLastName;
    private Date replyDate;
    private String replyText;

    private int mNumOfReplies = 0;
    private int mNumOfLikes = 0;
    private int mNumOfAttachs = 0;
    private List<String> mAttchList;
    private List<Reply> mReplyList;
    private List<Approve> mApproveList;

    public Reply() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getReReplyId() {
        return reReplyId;
    }

    public void setReReplyId(long reReplyId) {
        this.reReplyId = reReplyId;
    }

    public boolean isPostReply() {
        return isPostReply;
    }

    public void setPostReply(boolean postReply) {
        isPostReply = postReply;
    }

    public String getStatistics(Context context) {
        if (isPostReply)
            return String.format(context.getString(R.string.statistics),
                    mNumOfLikes, mNumOfReplies);
        else
            return String.format(context.getString(R.string.boost_statistics),
                    mNumOfLikes);

    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public Date getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(Date replyDate) {
        this.replyDate = replyDate;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
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


    /**
     * Method to call when replying on bulletin.
     *
     * @param bulletin {@link Bulletin} object that is being replied on.
     * @param context  context used for database communication.
     */
    public void reply(Context context, Bulletin bulletin) {
        ReplyOp replyOp = new ReplyOp(context, this);
        replyOp.setOperationType(OperationType.REPLY_ON_POST);

        this.postId = bulletin.getId();
        this._id = OperationManager.getInstance(context).generateId();
        bulletin.addReply(this);

        // Start upload
        replyOp.startUploadAction();
    }

    /**
     * Method to call when replying on reply.
     *
     * @param reply   {@link Reply} reply to reply on
     * @param context context used for database communication
     */
    public void reply(Context context, Reply reply) {
        ReplyOp replyOp = new ReplyOp(context, this);
        replyOp.setOperationType(OperationType.REPLY_ON_REPLY);

        this._id = OperationManager.getInstance(context).generateId();

        // Save reply
        addToDb(context);
        // Notify observers
        context.getContentResolver().notifyChange(DataContract.ReplyEntry.CONTENT_URI, null);
        // Save to not uploaded
        addToNotUploaded(TAG, context);
        // Start upload
        replyOp.startUploadAction();
    }

    /**
     * Method to call when reply has been liked.
     *
     * @param context used for db communication.
     */
    public void like(final Context context) {
        Approve approve = new Approve(OperationManager.getInstance(context).generateId());
        approve.postId = _id;
        approve.userId = MotherActivity.user_id;
        boolean isInDb = DataAccess.isApproveInDb(postId, _id, context);
        approve.save(context, isInDb);

        updateNumOfApprvs(!isInDb, context); // if approve is in db, then decrement num of likes
        notifyDataChanged(context);
    }

    /**
     * Method for incrementing reply's number of likes.
     *
     * @param context   context object required for db access.
     * @param increment boolean indicating if this reply is liked.
     */
    public void updateNumOfApprvs(boolean increment, Context context) {
        int num;
//        if (increment)
//            num = ++this.numOfApprvs;
//        else
//            num = --this.numOfApprvs;

        ContentValues values = new ContentValues(1);
        values.put(ReplyEntry.COLUMN_NUM_OF_LIKES, 1);
        int rows = context.getContentResolver().update(ReplyEntry.CONTENT_URI, values,
                ReplyEntry._ID + " = ?", new String[]{Long.toString(this._id)});

        if (rows == 1) {
            Log.d(TAG, String.format("Updated reply's(_id=%d) number of likes to %d.", this._id, this.mNumOfLikes));
        } else {
            Log.e(TAG, String.format("Failed to update reply's(_id=%d) number of likes.", this._id));
        }
    }

//    /**
//     * Method for incrementing reply's number of replies.
//     *
//     * @param context   context object required for db access.
//     * @param newReply boolean indicating if new reply on reply has been posted.
//     */
//    public void updateNumOfReplies(boolean newReply, Context context) {
//        int num;
//        if (newReply)
//            num = ++this.numOfReplies;
//        else
//            num = --this.numOfReplies;
//
//        ContentValues values = new ContentValues(1);
//        values.put(ReplyEntry.COLUMN_NUM_OF_REPLIES, num);
//        int rows = context.getContentResolver().update(ReplyEntry.CONTENT_URI, values,
//                ReplyEntry._ID + " = ?", new String[]{Long.toString(this._id)});
//
//        if (rows == 1) {
//            Log.d(TAG, String.format("Updated reply's(_id=%d) number of replies to %d.", this._id, this.numOfReplies));
//        } else {
//            Log.e(TAG, String.format("Failed to update reply's(_id=%d) number of replies.", this._id));
//        }
//    }


    public void addToDb(Context c) {
        ContentValues values = new ContentValues();
        Uri insertedUri = null;

        if (isPostReply) { // Add to post replies database
            values.put(DataContract.ReplyEntry._ID, _id);
            values.put(DataContract.ReplyEntry.COLUMN_USER_ID, userId);
            values.put(DataContract.ReplyEntry.COLUMN_POST_ID, postId);
            values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, userFirstName);
            values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, userFirstName);
            values.put(DataContract.ReplyEntry.COLUMN_TEXT, replyText);
            values.put(DataContract.ReplyEntry.COLUMN_DATE, replyDate.getTime());
//            values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_LIKES, numOfApprvs);
//            values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_REPLIES, numOfReplies);
            values.put(DataContract.ReplyEntry.COLUMN_SERVER_STATUS, mServerStatus.ordinal());

            // Add to db
            insertedUri = c.getContentResolver().insert(
                    DataContract.ReplyEntry.CONTENT_URI,
                    values
            );
        } else { // Add to reply replies database
            // TODO - finish
        }

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting Reply: " + Reply.this);
        } else {
            Log.d(TAG, "New reply added: " + Reply.this);
        }
    }

    @Override
    protected void removeFromDb(Context context) {

    }


    @Override
    public String toString() {
        String text, date;
        try {
            text = replyText.substring(0, 15).concat("...");
        } catch (Exception e) {
            text = replyText;
        }

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(replyDate);
        } catch (Exception e) {
            date = replyDate.toString();
        }
        return String.format(Locale.US, "Reply: ID=%d; Text=%s; UserName=%s %s; UserId=%d; Date=%s",
                _id, text, userFirstName, userLastName, userId, date);
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
//        DataAccess.getInstance(c).getBulletinById(postId).removeReply(this);
    }

    @Override
    protected void notifyDataChanged(Context context) {
        context.getContentResolver().notifyChange(DataContract.ReplyEntry.CONTENT_URI, null);
    }

    @Override
    public Reply parse(JSONObject object) throws JSONException {
        this.setId(object.getLong(KEY_REPLY_ID));
        this.setPostId(object.getLong(Bulletin.KEY_POST_ID));
        this.setReplyText(object.getString(Bulletin.KEY_TEXT));

        this.setNumOfReplies(object.getInt(Bulletin.KEY_NUM_OF_REPLIES));
        if (mNumOfReplies > 0) {
            mReplyList = new ArrayList<>(mNumOfReplies);
            // Try parsing reply list if it is present
            try {
                JSONObject replies = object.getJSONObject(Bulletin.KEY_REPLIES);
                JSONArray repliesList = replies.getJSONArray(Operations.KEY_DATA);
                int size = repliesList.length();
                for (int i = 0; i < size; i++) {
                    Reply r = new Reply().parse(repliesList.getJSONObject(i));
                    mReplyList.add(r);
                }
            } catch (JSONException e) {
                Log.e(TAG, "No reply list for replies count = " + mNumOfReplies);
            }
        }
        int attchs = object.getInt(Bulletin.KEY_NUM_OF_ATTACHMENTS);
        int locations = object.getInt(Bulletin.KEY_NUM_OF_LOCATIONS);
        setNumOfAttachs(attchs + locations);
        if (locations > 0) {
            mAttchList = new ArrayList<>(attchs + locations);

            // Try parsing location list if it's present
            try {
                JSONArray locationList = object.getJSONArray(Bulletin.KEY_LOCATIONS);
                for (int i = 0; i < locationList.length(); i++)
                    mAttchList.add(locationList.get(i).toString());
            } catch (JSONException e) {
                Log.e(TAG, "No location list for  locations count = " + locations);
            }
        }
        if (attchs > 0) {
            if (mAttchList == null)
                mAttchList = new ArrayList<>(attchs);

            // Try parsing attachment list if it's present
            try {
                JSONArray locationList = object.getJSONArray(Bulletin.KEY_ATTACHMENTS);
                for (int i = 0; i < locationList.length(); i++)
                    mAttchList.add(locationList.getJSONObject(i).getString(Bulletin.KEY_FILE_URL));
            } catch (JSONException e) {
                Log.e(TAG, "No attch list for attch count = " + locations);
            }
        }
        setNumOfLikes(object.getInt(KEY_NUM_OF_APPROVES));

        return this;
    }

    @Override
    void save(Context context) {

    }

    @Override
    public boolean equals(Object obj) {
        try {
            Reply r = (Reply) obj;
            return r.getId() == this.getId();
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int getNumOfReplies() {
        return mNumOfReplies;
    }

    public void setNumOfReplies(int mNumOfReplies) {
        this.mNumOfReplies = mNumOfReplies;
    }

    public int getNumOfLikes() {
        return mNumOfLikes;
    }

    public void setNumOfLikes(int mNumOfLikes) {
        this.mNumOfLikes = mNumOfLikes;
    }

    public int getNumOfAttachs() {
        return mNumOfAttachs;
    }

    public void setNumOfAttachs(int mNumOfAttachs) {
        this.mNumOfAttachs = mNumOfAttachs;
    }

    public List<String> getAttachments() {
        if (mAttchList == null)
            mAttchList = new ArrayList<>();
        return mAttchList;
    }

    public void setAttachments(List<String> mAttachments) {
        this.mAttchList = mAttachments;
    }

    public List<Reply> getReplyList() {
        if (mReplyList == null)
            mReplyList = new ArrayList<>();
        return mReplyList;
    }

    public void setReplyList(List<Reply> mReplyList) {
        this.mReplyList = mReplyList;
    }

    public List<Approve> getApproveList() {
        if (mApproveList == null)
            mApproveList = new ArrayList<>();
        return mApproveList;
    }

    public void setApproveList(List<Approve> mApproveList) {
        this.mApproveList = mApproveList;
    }
}

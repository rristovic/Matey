package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
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

import static com.mateyinc.marko.matey.model.Bulletin.KEY_POST_ID;

public class Reply extends MModel {
    private static final String TAG = Reply.class.getSimpleName();

    // Keys for JSON data
    public static final String KEY_REPLY_ID = "reply_id";
    public static final String KEY_NUM_OF_APPROVES = "num_of_approves";
    public static final String KEY_IS_LIKED = "approved";
    public static final String KEY_FIRST_NAME = "reply_username";
    public static final String KEY_LAST_NAME = "reply_lastname";
    public static final String KEY_USER_ID = "reply_userid";
    public static final String KEY_DATE = "reply_date";
    public static final String KEY_TEXT = "reply_text";
    public static final String KEY_REPLY_APPRVS = "reply_approves";
    private static final String KEY_RE_REPLY_ID = "rereply_id";


    /**
     * The ID of the post/bulletin that has been replied on
     */
    private long postId;
    /**
     * The ID of the reply that has been replied on
     */
    private long reReplyId = -1;

    private UserProfile mUserProfile;
    private boolean isPostReply = true;
    private Date replyDate = new Date();
    private String replyText;

    private int mNumOfReplies = 0;
    private int mNumOfLikes = 0;
    private int mNumOfAttachs = 0;
    private List<String> mAttchList;
    private List<Reply> mReplyList;
    private List<Approve> mApproveList;
    private boolean isLiked;

    public Reply() {
    }

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public void setUserProfile(UserProfile profile) {
        this.mUserProfile = profile;
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
        this.isPostReply = false;
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
        // Start upload
        replyOp.startUploadAction();
    }


    public void addToDb(Context c) {
        ContentValues values = new ContentValues();
        Uri insertedUri = null;

        if (isPostReply) { // Add to post replies database
            values.put(DataContract.ReplyEntry._ID, _id);
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
    public void onDownloadSuccess(String response, Context c) {

    }

    @Override
    public void onDownloadFailed(String error, Context c) {

    }

    @Override
    public void onUploadSuccess(String response, Context c) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            jsonObject = jsonObject.getJSONObject(Operations.KEY_DATA);

            this.replyDate = Util.parseDate(jsonObject.getString(MModel.KEY_DATE_ADDED));
            this._id = jsonObject.getLong(KEY_REPLY_ID);
            this.mServerStatus = ServerStatus.STATUS_SUCCESS;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing reply id and date: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void onUploadFailed(String error, Context c) {
//        DataAccess.getInstance(c).getBulletinById(postId).removeReply(this);
        this.mServerStatus = ServerStatus.STATUS_RETRY_UPLOAD;
    }


    @Override
    public Reply parse(JSONObject object) throws JSONException {
        this.setId(object.getLong(KEY_REPLY_ID));
        this.setDate(Util.parseDate(object.getString(KEY_DATE_ADDED)));
        this.setReplyText(object.getString(Bulletin.KEY_TEXT));
        this.isLiked = object.getBoolean(KEY_IS_LIKED);

        int attchs = 0;
        int locations = 0;
        if (!parseReReply(object)){
            this.setPostId(object.getLong(KEY_POST_ID));
            this.setNumOfReplies(object.getInt(Bulletin.KEY_NUM_OF_REPLIES));
            attchs = object.getInt(Bulletin.KEY_NUM_OF_ATTACHMENTS);
            locations = object.getInt(Bulletin.KEY_NUM_OF_LOCATIONS);
            setNumOfAttachs(attchs + locations);
        }

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

    /**
     * Helper method for parsing rereply fields. If fails, it means that this is post reply.
     * @return true if parsing was completed.
     */
    private boolean parseReReply(JSONObject object) {
        try {
            this.setReReplyId(object.getLong(KEY_RE_REPLY_ID));
            return true;
        } catch (JSONException e) {
            Log.w(TAG, "Not rereply.");
            return false;
        }
    }

    public boolean like() {
        if (isLiked)
            mNumOfLikes--;
        else
            mNumOfLikes++;

        isLiked = !isLiked;
        return isLiked;
    }


    public void addReply(Reply r) {
        this.mReplyList.add(0, r);
    }


    @Override
    public boolean equals(Object obj) {
        try {
            Reply r = (Reply) obj;
            if (!r.isPostReply)
                return r.reReplyId == this.reReplyId;
            return r.getId() == this.getId();
        } catch (ClassCastException e) {
            return false;
        }
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
        return String.format(Locale.US, "Reply: ID=%d; Text=%s; User:{%s} Date=%s",
                _id, text, mUserProfile.toString(), date);
    }

    private Object mLock = new Object();


}

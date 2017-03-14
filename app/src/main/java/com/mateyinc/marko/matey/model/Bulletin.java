package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.operations.BulletinOp;
import com.mateyinc.marko.matey.internet.operations.Operations;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mateyinc.marko.matey.activity.Util.parseDate;
import static com.mateyinc.marko.matey.data.DataContract.BulletinEntry;

public class Bulletin extends MModel {
    private String TAG = Bulletin.class.getSimpleName();

    // Keys for JSON data
    public static final String KEY_POST_ID = "post_id";
    public static final String KEY_SUBJECT = "title";
    public static final String KEY_TEXT = "text";
    public static final String KEY_DATE = "time_c";
    public static final String KEY_USER_PROFILE = "user";
    public static final String KEY_NUM_OF_REPLIES = "num_of_replies";
    public static final String KEY_NUM_OF_BOOSTS = "num_of_boosts";
    public static final String KEY_NUM_OF_ATTACHMENTS = "attachs_num";
    public static final String KEY_NUM_OF_LOCATIONS = "locations_num";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_ATTACHMENTS = "attachs";

    private long mUserID;
    private Date mDate;
    private String mFirstName;
    private String mLastName;
    private String mText;
    private String mSubject;
    private int mNumOfReplies = 0;
    private int mNumOfLikes = 0;


    private int mNumOfAttachs = 0;
    private List<String> mAttachments;


    public Bulletin(long postId, long userId, String title, String text, Date date) {
        this._id = postId;
        this.mUserID = userId;
        this.mSubject = title;
        this.mText = text;
        this.mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(long user_id, String firstName, String lastName, String subject, String text, Date date) {
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mSubject = subject;
        mText = text;
        mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String title, Date date) {
        _id = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mSubject = title;
        mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String title, Date date, int serverStatus) {
        _id = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mSubject = title;
        mDate = date;
        setServerStatus(serverStatus);
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public List<String> getAttachments() {
        return mAttachments;
    }

    public void setAttachments(List<String> mAttachments) {
        this.mAttachments = mAttachments;
    }

    public int getNumOfReplies() {
        return mNumOfReplies;
    }

    public void setNumOfReplies(int noOfReplies) {
        this.mNumOfReplies = noOfReplies;
    }

    public int getNumOfLikes() {
        return mNumOfLikes;
    }

    public void setNumOfLikes(int mNumOfLikes) {
        this.mNumOfLikes = mNumOfLikes;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getStatistics(Context context) {
        return String.format(context.getString(R.string.statistics),
                mNumOfLikes, mNumOfReplies);
    }

    public int getmNumOfAttachs() {
        return mNumOfAttachs;
    }

    public void setNumOfAttachs(int mNumOfAttachs) {
        this.mNumOfAttachs = mNumOfAttachs;
    }

    public void setSubject(String mSubject) {
        this.mSubject = mSubject;
    }

    public long getPostID() {
        return _id;
    }

    public void setPostID(long mPostID) {
        this._id = mPostID;
    }

    public long getUserID() {
        return mUserID;
    }

    public void setUserID(long mUserID) {
        this.mUserID = mUserID;
    }

    /**
     * Return empty string if there's is no mesasge
     *
     * @return string object
     */
    public String getMessage() {
        return mText == null ? "" : mText;
    }

    public void setMessage(String mMessage) {
        this.mText = mMessage;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(String mDateString) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.mDate = dateFormat.parse(mDateString);
        } catch (Exception e) {
            try {
                this.mDate = new Date(mDateString);
            } catch (Exception es) {
                Log.e(TAG, es.getLocalizedMessage(), es);
                this.mDate = new Date();
            }
        }

    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public void setDate(long timeInMilis) {
        this.mDate = new Date(timeInMilis);
    }

    public void setAttachmentsFromJSON(String string) {
        return; // TODO - finish method
    }

    /**
     * Method for parsing JSON response into new {@link Bulletin}
     *
     * @param response string response retrieved from the server
     * @return A Bulletin object made from JSON data
     */
    public static Bulletin parseBulletin(String response) throws JSONException {
        JSONObject object = new JSONObject(response);

        JSONObject user = object.getJSONObject(KEY_USER_PROFILE);
        Bulletin b = new Bulletin(
                object.getLong(KEY_POST_ID),
                user.getLong(KEY_USER_ID),
                object.getString(KEY_SUBJECT),
                object.getString(KEY_TEXT),
                parseDate(object.getString(KEY_DATE_ADDED))
        );

        b.setNumOfReplies(object.getInt(KEY_NUM_OF_REPLIES));
        b.setNumOfLikes(object.getInt(KEY_NUM_OF_BOOSTS));
        b.setNumOfAttachs(object.getInt(KEY_NUM_OF_ATTACHMENTS) + object.getInt(KEY_NUM_OF_LOCATIONS));
        return b;
    }

    /**
     * Method for parsing bulletin data to {@link ContentValues} object ready for db.
     */
    public ContentValues toValues() {
        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry._ID, _id);
        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, mUserID);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, mFirstName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, mLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, mText);
        values.put(DataContract.BulletinEntry.COLUMN_SUBJECT, mSubject);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, mDate.getTime());
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, mNumOfReplies);
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_LIKES, mNumOfLikes);
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, mServerStatus.ordinal());

        return values;
    }


    /**
     * Method to call when bulletin has been liked/unliked.*
     *
     * @param context context used for database communication.
     */
    public void like(final Context context) {
        Approve approve = new Approve(OperationManager.getInstance(context).generateId());
        approve.postId = _id;
        approve.userId = MotherActivity.user_id;
        boolean isInDb = DataAccess.isApproveInDb(_id, -1, context); // -1 if default value
        approve.save(context, isInDb);

        updateNumOfApprvs(!isInDb, context); // if approve is in db, then decrement num of likes
        notifyDataChanged(context);
    }

    /**
     * Method for incrementing bulletin number of likes.
     *
     * @param context   context object required for db access.
     * @param increment boolean indicating if this bulletin is liked.
     */
    public void updateNumOfApprvs(boolean increment, Context context) {
        int num;
        if (increment)
            num = ++this.mNumOfLikes;
        else
            num = --this.mNumOfLikes;

        ContentValues values = new ContentValues(1);
        values.put(BulletinEntry.COLUMN_NUM_OF_LIKES, num);
        int rows = context.getContentResolver().update(BulletinEntry.CONTENT_URI, values,
                BulletinEntry._ID + " = ?", new String[]{Long.toString(this._id)});

        if (rows == 1) {
            Log.d(TAG, String.format("Updated bulletin(_id=%d) number of likes to %d.", this._id, this.mNumOfLikes));
        } else {
            Log.e(TAG, String.format("Failed to update bulletin(_id=%d) number of likes.", this._id));
        }
    }

    /**
     * Method for incrementing bulletin number of replies.
     *
     * @param context  context object required for db access.
     * @param newReply boolean indicating if new reply has been posted.
     */
    public void updateNumOfReplies(boolean newReply, Context context) {
        int num;
        if (newReply)
            num = ++this.mNumOfReplies;
        else
            num = --this.mNumOfReplies;

        ContentValues values = new ContentValues(1);
        values.put(BulletinEntry.COLUMN_NUM_OF_REPLIES, num);
        int rows = context.getContentResolver().update(BulletinEntry.CONTENT_URI, values,
                BulletinEntry._ID + " = ?", new String[]{Long.toString(this._id)});

        if (rows == 1) {
            Log.d(TAG, String.format("Updated bulletin(_id=%d) number of replies to %d.", this._id, this.mNumOfReplies));
        } else {
            Log.e(TAG, String.format("Failed to update bulletin(_id=%d) number of replies.", this._id));
        }
    }


    /**
     * Method to call when new bulletin has been created.
     *
     * @param context context used for database communication.
     */
    @Override
    public void save(Context context) {
        BulletinOp bulletinOp = new BulletinOp(context, this);

//        if (mAttachments == null || mAttachments.size() == 0) {
//            // Use volley
//            bulletinOp.setOperationType(OperationType.POST_NEW_BULLETIN_NO_ATTCH);
//        } else {
//            // Send through OkHTTP
//            bulletinOp.setOperationType(OperationType.POST_NEW_BULLETIN_WITH_ATTCH);
//        }

        addToDb(context);
        notifyDataChanged(context);
        addToNotUploaded(TAG, context);
        bulletinOp.startUploadAction();
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

            ContentValues values = new ContentValues(3);
            Date date = Util.parseDate(jsonObject.getString(MModel.KEY_DATE_ADDED));
            values.put(BulletinEntry.COLUMN_DATE, date.getTime());
            values.put(BulletinEntry._ID, jsonObject.getLong(KEY_POST_ID));
            values.put(BulletinEntry.COLUMN_SERVER_STATUS, ServerStatus.STATUS_SUCCESS.ordinal());

            int rows = c.getContentResolver().update(BulletinEntry.CONTENT_URI, values,
                    BulletinEntry._ID + " = ?", new String[]{Long.toString(this._id)});

            if (rows != 1) {
                Log.e(TAG, "Failed to update bulletin data retrieved from server.");
            } else
                Log.d(TAG, String.format("Updated bulletin(id:%d) with id:%d and date:%s.", this._id,
                        jsonObject.getLong(KEY_POST_ID), date));

            notifyDataChanged(c);
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

    }

    @Override
    public void onUploadFailed(String error, Context c) {
        // Notify user
        this.mServerStatus = ServerStatus.STATUS_RETRY_UPLOAD;
        updateServerStatus(c);
    }

    public void addToDb(final Context c) {
        ContentValues values = toValues();

        // Add to db
        Uri insertedUri = c.getContentResolver().insert(
                DataContract.BulletinEntry.CONTENT_URI,
                values);

        if (insertedUri == null) {
            Log.e(TAG, "Error inserting Bulletin: " + Bulletin.this);
        } else {
            Log.e(TAG, "New bulletin added: " + Bulletin.this);
        }
    }


    @Override
    protected void removeFromDb(Context context) {

    }

    @Override
    protected void notifyDataChanged(Context context) {
        context.getContentResolver().notifyChange(DataContract.BulletinEntry.CONTENT_URI, null);
    }

    public class Attachment {

        public Attachment() {
        }
    }

    @Override
    public String toString() {
        String message, date;

        try {
            message = mText.substring(0, 15).concat("...");
        } catch (Exception e) {
            message = mText;
        }

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(mDate);
        } catch (Exception e) {
            date = mDate.toString();
        }

        return String.format(Locale.US, "Bulletin: ID=%d; Text=%s; UserName=%s %s; UserId=%d Date=%s; RepliesCount=%d",
                _id, message, mFirstName, mLastName, mUserID, date, mNumOfReplies);
    }

}

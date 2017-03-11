package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.operations.BulletinOp;
import com.mateyinc.marko.matey.internet.operations.OperationType;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mateyinc.marko.matey.activity.Util.parseDate;

public class Bulletin extends MModel {
    private String TAG = Bulletin.class.getSimpleName();

    // Keys for JSON data
    public static final String KEY_DATA = "data";
    public static final String KEY_POST_ID = "post_id";
    public static final String KEY_TEXT = "text";
    public static final String KEY_STATISTICS = "statistics";
    public static final String KEY_NUM_OF_RESPONSES = "num_of_responses";
    public static final String KEY_NUM_OF_SHARES = "num_of_shares";
    public static final String KEY_LAST_USER_RESPOND = "last_user_respond";

    private long mUserID;
    private String mFirstName;
    private String mLastName;
    private Date mDate;
    private String mText;
    private String mSubject;
    private int mNumOfReplies = 0;
    private int mNumOfLikes = 0;
    private List<String> mAttachments;


    public Bulletin() {
    }

    public Bulletin(long user_id, String firstName, String lastName, String subject, String text, Date date) {
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mSubject = subject;
        mText = text;
        mDate = date;
    }

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String text, Date date) {
        _id = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mText = text;
        mDate = date;
    }

    public Bulletin(long post_id, long user_id, String firstName, String lastName, String text, Date date, int serverStatus) {
        _id = post_id;
        mUserID = user_id;
        mFirstName = firstName;
        mLastName = lastName;
        mText = text;
        mDate = date;
        this.mServerStatus = serverStatus;
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

    public void setmNumOfLikes(int mNumOfLikes) {
        this.mNumOfLikes = mNumOfLikes;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getStatistics(Context context) {
        return String.format(context.getString(R.string.statistics),
                mNumOfLikes, mNumOfReplies);
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
    public static Bulletin parseBulletin(String response) throws JSONException, ParseException {
        JSONObject object = new JSONObject(response);
        JSONObject dataObject = object.getJSONObject(KEY_DATA);

        Bulletin b = new Bulletin(
                dataObject.getLong(KEY_POST_ID),
                object.getLong(KEY_USER_ID),
                object.getString(KEY_FIRSTNAME),
                object.getString(KEY_LASTNAME),
                dataObject.getString(KEY_TEXT),
                parseDate(object.getString(KEY_DATE_ADDED))
        );

        return b;
    }

    /**
     * Method to call when bulletin has been liked/unliked.*
     * @param context context used for database communication.
     */
    public void like(final Context context) {
        Approve approve = new Approve();
        approve.postId = _id;
        approve.userId = MotherActivity.user_id;

        approve.save(context);
        notifyDataChanged(context);
    }

    /**
     * Method to call when new bulletin has been created.
     * @param context context used for database communication.
     */
    @Override
    public void save(Context context) {
        BulletinOp bulletinOp = new BulletinOp(context, this);

        if (mAttachments == null) {
            // Use volley
            bulletinOp.setOperationType(OperationType.POST_NEW_BULLETIN_NO_ATTCH);
        } else {
            // Send through OkHTTP
            bulletinOp.setOperationType(OperationType.POST_NEW_BULLETIN_WITH_ATTCH);
        }

        addToDb(context);
        notifyDataChanged(context);

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
        // Notify user

    }

    @Override
    public void onUploadFailed(String error, Context c) {
        // Notify user

    }

    public void addToDb(final Context c) {
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
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, mServerStatus);

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

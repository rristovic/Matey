package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
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
    private LinkedList<Attachment> mAttachments;

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

    public void setmSubject(String mSubject) {
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

    public String getMessage() {
        return mText;
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

    public LinkedList<Attachment> getAttachments() {
        return mAttachments;
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

    public void addToDb(final Context c) {
        ContentValues values = new ContentValues();
        values.put(DataContract.BulletinEntry._ID, _id);
        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, mUserID);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, mFirstName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, mLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, mText);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, mDate.getTime());
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, mNumOfReplies);
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

    private void updateNullBulletin(final Context context) {
        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
        values.put(DataContract.BulletinEntry._ID, -1);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, "nn");
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, "nn");
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, "nn");
        values.put(DataContract.BulletinEntry.COLUMN_DATE, new Date().getTime());

        Uri uri = context.getContentResolver().insert(
                DataContract.BulletinEntry.CONTENT_URI,
                values
        );

        if (uri != null) {
            Log.d(TAG, "Null bulletin added.");
        } else {
            Log.e(TAG, "Failed to add null bulletin.");
        }
    }

    public static void decrementRepliesCount(long postId, Context context){
        new Bulletin(postId).decrementRepliesCount(context);
    }

    private void decrementRepliesCount(final Context context){
        ContentValues values = new ContentValues(1);
        Cursor c = context.getContentResolver().query(DataContract.BulletinEntry.CONTENT_URI, new String[]{DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES},
                DataContract.BulletinEntry._ID + " = " + _id, null, null);

        if (c != null && c.moveToFirst()){
            // Retrieve the value
            int count = c.getInt(0);
            // Close db
            c.close();
            // Update db
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, --count);
            int i = context.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                    DataContract.BulletinEntry._ID + " = " + _id, null);
            if (i == 1)
                Log.d(TAG, "Bulletin replies count decremented: " + Bulletin.this);
            else
                Log.e(TAG, "Failed to decrement bulletin replies count: " + Bulletin.this);
        }
    }

    public static void incrementRepliesCount(long postId, Context context) {
        new Bulletin(postId).incrementRepliesCount(context);
    }

    private void incrementRepliesCount(Context context) {
        ContentValues values = new ContentValues(1);
        Cursor c = context.getContentResolver().query(DataContract.BulletinEntry.CONTENT_URI, new String[]{DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES},
                DataContract.BulletinEntry._ID + " = " + _id, null, null);

        if (c != null && c.moveToFirst()){
            // Retrieve the value
            int count = c.getInt(0);
            // Close db
            c.close();
            // Update db
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, ++count);
            int i = context.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                    DataContract.BulletinEntry._ID + " = " + _id, null);
            if (i == 1)
                Log.d(TAG, "Bulletin replies count incremented: " + Bulletin.this);
            else
                Log.e(TAG, "Failed to increment bulletin replies count: " + Bulletin.this);
        }
    }



    @Override
    public String toString() {
        String message, date;

        try {
            message = mText.substring(0,15).concat("...");
        } catch (Exception e){
            message = mText;
        }

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(mDate);
        } catch (Exception e){
            date = mDate.toString();
        }

        return String.format(Locale.US, "Bulletin: ID=%d; Text=%s; UserName=%s %s; UserId=%d Date=%s; RepliesCount=%d",
                _id, message, mFirstName, mLastName, mUserID, date, mNumOfReplies);
    }



    private Bulletin(){}
    private Bulletin(long postId){
        _id = postId;
    }

    public class Attachment {

        public Attachment() {
        }
    }

}

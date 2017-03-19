package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.internet.operations.Operations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final String KEY_IS_BOOSTED = "boosted";
    public static final String KEY_USER_PROFILE = "user";
    public static final String KEY_NUM_OF_REPLIES = "num_of_replies";
    private static final String KEY_NUM_OF_BOOSTS = "num_of_boosts";
    public static final String KEY_NUM_OF_ATTACHMENTS = "attachs_num";
    public static final String KEY_NUM_OF_LOCATIONS = "locations_num";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_ATTACHMENTS = "attachs";
    public static final String KEY_FILE_URL = "file_url";
    public static final String KEY_REPLIES = "replies";

    private UserProfile mUserProfile;
    private Date mDate;
    private String mText;
    private String mSubject;
    private Group mGroup;

    private int mNumOfReplies = 0;
    private int mNumOfLikes = 0;
    private int mNumOfAttachs = 0;
    private boolean isBoosted;

    private List<String> mAttachments;
    private List<Reply> mReplyList;
    private List<Approve> mApproves;


    public Bulletin() {
    }

    public Bulletin(long _id) {
        this._id = _id;
    }

    public Bulletin(long postId, UserProfile user, String title, String text, Date date) {
        this._id = postId;
        this.mUserProfile = user;
        this.mSubject = title;
        this.mText = text;
        this.mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(UserProfile user, String subject, String text, Date date) {
        this.mUserProfile = user;
        mSubject = subject;
        mText = text;
        mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(long post_id, UserProfile user, String title, Date date) {
        _id = post_id;
        this.mUserProfile = user;
        mSubject = title;
        mDate = date;
        this.mUri = BulletinEntry.CONTENT_URI;
    }

    public Bulletin(long post_id, UserProfile user, String title, Date date, int serverStatus) {
        _id = post_id;
        this.mUserProfile = user;
        mSubject = title;
        mDate = date;
        setServerStatus(serverStatus);
        this.mUri = BulletinEntry.CONTENT_URI;
    }


    public List<Reply> getReplies() {
        if (mReplyList == null)
            mReplyList = new ArrayList<>();
        return mReplyList;
    }

    public void setReplies(List<Reply> mReplies) {
        this.mReplyList = mReplies;
    }

    public List<Approve> getApproves() {
        if (mApproves == null)
            mApproves = new ArrayList<>();
        return mApproves;
    }

    public void setApproves(List<Approve> mApproves) {
        this.mApproves = mApproves;
    }

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public void setUserProfile(UserProfile mUserProfile) {
        this.mUserProfile = mUserProfile;
    }

    public List<String> getAttachments() {
        if (mAttachments == null)
            mAttachments = new ArrayList<>();
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

    public void setPostID(long mPostID) {
        this._id = mPostID;
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

    @Override
    /**
     * Method for parsing JSON response into new {@link Bulletin}
     *
     * @param object string response retrieved from the server
     * @return A Bulletin object made from JSON data
     */
    public Bulletin parse(JSONObject object) throws JSONException {

        // Parse requireed fields
        this._id = object.getLong(KEY_POST_ID);
        this.mSubject = object.getString(KEY_SUBJECT);
        this.mText = object.getString(KEY_TEXT);
        this.mDate = parseDate(object.getString(KEY_DATE_ADDED));
        this.isBoosted = object.getBoolean(KEY_IS_BOOSTED);

        setNumOfReplies(object.getInt(KEY_NUM_OF_REPLIES));
        if (getNumOfReplies() > 0) {
            mReplyList = new ArrayList<>(mNumOfReplies);
            // Try parsing reply list if it is present
            try {
                JSONObject replies = object.getJSONObject(KEY_REPLIES);
                JSONArray repliesList = replies.getJSONArray(Operations.KEY_DATA);
                int size = repliesList.length();
                for (int i = 0; i < size; i++) {
                    Reply reply = new Reply().parse(repliesList.getJSONObject(i));
                    reply.setUserProfile(this.mUserProfile);
                    mReplyList.add(reply);
                }
            } catch (JSONException e) {
                Log.e(TAG, "No reply list for replies count = " + mNumOfReplies);
            }
        }
        int attchs = object.getInt(KEY_NUM_OF_ATTACHMENTS);
        int locations = object.getInt(KEY_NUM_OF_LOCATIONS);
        setNumOfAttachs(attchs + locations);
        if (locations > 0) {
            mAttachments = new ArrayList<>(attchs + locations);

            // Try parsing location list if it's present
            try {
                JSONArray locationList = object.getJSONArray(KEY_LOCATIONS);
                for (int i = 0; i < locationList.length(); i++)
                    mAttachments.add(locationList.get(i).toString());
            } catch (JSONException e) {
                Log.e(TAG, "No location list for  locations count = " + locations);
            }
        }
        if (attchs > 0) {
            if (mAttachments == null)
                mAttachments = new ArrayList<>(attchs);

            // Try parsing attachment list if it's present
            try {
                JSONArray locationList = object.getJSONArray(KEY_ATTACHMENTS);
                for (int i = 0; i < locationList.length(); i++)
                    mAttachments.add(locationList.getJSONObject(i).getString(KEY_FILE_URL));
            } catch (JSONException e) {
                Log.e(TAG, "No attch list for attch count = " + locations);
            }
        }
        setNumOfLikes(object.getInt(KEY_NUM_OF_BOOSTS));
        return this;
    }

    /**
     * Method for parsing bulletin data to {@link ContentValues} object ready for db.
     */
    public ContentValues toValues() {
        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry._ID, _id);
//        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, mUserID);
//        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, mFirstName);
//        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, mLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, mText);
        values.put(DataContract.BulletinEntry.COLUMN_SUBJECT, mSubject);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, mDate.getTime());
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, mNumOfReplies);
        values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_LIKES, mNumOfLikes);
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, mServerStatus.ordinal());

        return values;
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

            this.mDate = Util.parseDate(jsonObject.getString(MModel.KEY_DATE_ADDED));
            this._id = jsonObject.getLong(KEY_POST_ID);
            this.mServerStatus = ServerStatus.STATUS_SUCCESS;
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

    }

    @Override
    public void onUploadFailed(String error, Context c) {
        // Notify user
        this.mServerStatus = ServerStatus.STATUS_RETRY_UPLOAD;
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

        return String.format(Locale.US, "Bulletin: ID=%d; Text=%s; User={%s} Date=%s; RepliesCount=%d",
                _id, message, mUserProfile, date, mNumOfReplies);
    }

    public void addReply(Reply reply) {
        this.mNumOfReplies++;
        this.mReplyList.add(0, reply);
    }

    public void removeReply(Reply reply) {
        this.mReplyList.remove(reply);
    }

    /**
     * Method to boost and unboost post.
     *
     * @return return true if post is boosted. False if post has been un boosted.
     */
    public boolean boost() {
        if (isBoosted)
            mNumOfLikes--;
        else
            mNumOfLikes++;

        isBoosted = !isBoosted;
        return isBoosted;
    }
}

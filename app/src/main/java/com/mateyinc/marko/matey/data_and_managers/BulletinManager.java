package com.mateyinc.marko.matey.data_and_managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mateyinc.marko.matey.activity.home.BulletinsFragment;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Bulletin.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by Sarma on 5/12/2016.
 */
public class BulletinManager {
    private String TAG = BulletinManager.class.getSimpleName();

    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String BULLETIN_ITEM_UPDATED = "com.mateyinc.marko.matey.internet.home.bulletin_item_updated";
    public static final String EXTRA_ITEM_CHANGED_POS = "item_changed_position"; // extra for the bulletin_item_updated intent
    public static final String MORE_BULLETIN_DATA_DOWNLOADED = "com.mateyinc.marko.matey.internet.home.more_bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.internet.home.bulletins_loaded_count";

    private final ArrayList<Bulletin> mData;
    private final Context mAppContext;
    private boolean mBulletinsLoaded;
    public static int mCurrentPage = 0;


    private static final Object mLock = new Object();
    private static BulletinManager mInstance = null;

    public static BulletinManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new BulletinManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }


    private BulletinManager(Context context) {
        mAppContext = context;
        mData = new ArrayList<>();
    }

    public void addBulletins(ArrayList<Bulletin> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);

        for (Bulletin b :
                list) {

            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, b.getPostID());
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, b.getUserID());
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, b.getFirstName());
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, b.getLastName());
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, b.getMessage());
            values.put(DataContract.BulletinEntry.COLUMN_DATE, b.getDate().toString());
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(b.getReplies()));
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(b.getAttachments()));

            cVVector.add(values);

            Log.d(TAG, "Bulletin added: " + b.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " bulletins added");

//        mAppContext.getContentResolver().insert(
//                DataContract.BulletinEntry.CONTENT_URI,
//                values
//        );
//
//        String debugtext = "Bulletin added: ID=" + postId +
//                "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
//                + "...; Date=" + date;
//        debugtext += "; Num of replies=";
//        debugtext += replies == null ? '0' : Integer.toString(replies.size());
//        debugtext += "; Num of attachments=";
//        debugtext += attachments == null ? '0' : Integer.toString(attachments.size());
//
//        Log.d("BulletinManager", debugtext);
    }


    /**
     * Helper method to handle the insertion of new bulletin
     *
     * @param postId       bulletin id from the server
     * @param userId       id of user that posted
     * @param userName     name of user
     * @param userLastName last name of user
     * @param text         post text
     * @param date         post date
     * @param replies      list of rost replies
     * @param attachments  list of post attachments
     * @return the row ID of the added bulletin.
     */
    public void addBulletin(int postId, int userId, String userName, String userLastName, String text, String date,
                            LinkedList<Bulletin.Reply> replies, LinkedList<Bulletin.Attachment> attachments) {

        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + postId,
                null,
                null);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            setBulletin(postId, userId, userName, userLastName, text, date, replies, attachments);
        } else {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, postId);
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
            values.put(DataContract.BulletinEntry.COLUMN_DATE, date);
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(replies));
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));

            mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            String debugtext = "Bulletin added: ID=" + postId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
                    + "...; Date=" + date;
            debugtext += "; Num of replies=";
            debugtext += replies == null ? '0' : Integer.toString(replies.size());
            debugtext += "; Num of attachments=";
            debugtext += attachments == null ? '0' : Integer.toString(attachments.size());

            Log.d("BulletinManager", debugtext);
        }
        if (msgCursor != null)
            msgCursor.close();
    }

    /**
     * Method for changing bulletin in db
     */
    public void setBulletin(int postId, int userId, String userName, String userLastName, String text, String date,
                            LinkedList<Bulletin.Reply> replies, LinkedList<Bulletin.Attachment> attachments) {

        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, date);
        values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(replies));
        values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));


        mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry.COLUMN_POST_ID + " = ?", new String[]{Integer.toString(postId)});

        String debugtext = "Bulletin added: ID=" + postId +
                "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
                + "...; Date=" + date;
        debugtext += "; Num of replies=";
        debugtext += replies == null ? '0' : Integer.toString(replies.size());
        debugtext += "; Num of attachments=";
        debugtext += attachments == null ? '0' : Integer.toString(attachments.size());

        Log.d("BulletinManager", debugtext);
    }

    private String parseRepliesToJSON(LinkedList<Bulletin.Reply> replies) {
        JSONObject jObject = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();
            for (Bulletin.Reply r : replies) {
                JSONObject replyJson = new JSONObject();
                replyJson.put(Reply.REPLY_ID, r.replyId);
                replyJson.put(Reply.FIRST_NAME, r.userFirstName);
                replyJson.put(Reply.LAST_NAME, r.userLastName);
                replyJson.put(Reply.USER_ID, r.userId);
                replyJson.put(Reply.TEXT, r.replyText);
                replyJson.put(Reply.DATE, r.replyDate);

                JSONArray replyApprvs = new JSONArray();
                for (UserProfile profile : r.replyApproves) {
                    JSONObject apprvJson = new JSONObject();
                    apprvJson.put(UserProfile.USER_ID, profile.getUserId());
                    apprvJson.put(UserProfile.FIRST_NAME, profile.getFirstName());
                    apprvJson.put(UserProfile.LAST_NAME, profile.getLastName());
                    apprvJson.put(UserProfile.LAST_MSG_ID, profile.getLastMsgId());

                    replyApprvs.put(apprvJson);
                }
                replyJson.put(REPLY_APPRVS, replyApprvs);

                jArray.put(replyJson);
            }
            jObject.put(REPLIES_LIST, jArray);
        } catch (JSONException jse) {
            Log.e(TAG, jse.getLocalizedMessage(), jse);
        }

        return jObject.toString();
    }

    private String parseAttachmentsToJSON(LinkedList<Bulletin.Attachment> attachments) {
        return ""; // TODO - finish method
    }

    public void addBulletin(Bulletin b) {
        addBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(),
                b.getDate().toString(), b.getReplies(), b.getAttachments());
    }

    public void addNullBulletin() {

        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + -1,
                null,
                null);

        if (cursor != null && !cursor.moveToFirst()) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_DATE, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, "");
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, "");


            mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );
        }

        if (cursor != null)
            cursor.close();
    }

    public ArrayList<Bulletin> getBulletinList() {
        synchronized (mLock) {
            return mData;
        }
    }

    public Bulletin getBulletin(int index, Cursor cursor) {
        Bulletin bulletin = new Bulletin();
        try {
            cursor.moveToPosition(index);

            bulletin.setUserID(cursor.getInt(BulletinsFragment.COL_USER_ID));
            bulletin.setPostID(cursor.getInt(BulletinsFragment.COL_POST_ID));
            bulletin.setFirstName(cursor.getString(2));
            bulletin.setLastName(cursor.getString(3));
            bulletin.setMessage(cursor.getString(4));
            bulletin.setDate(cursor.getString(5));
            bulletin.setRepliesFromJSON(cursor.getString(6));
            bulletin.setAttachmentsFromJSON(cursor.getString(7));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return bulletin;
    }

    public Bulletin getBulletin(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                BulletinsFragment.BULLETIN_COLUMNS,
                null,
                null,
                null);

        Bulletin bulletin = null;
        try {
            bulletin = getBulletin(index, cursor);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }

        return bulletin;
    }


    public Bulletin getBulletinByPostID(int id) {
        synchronized (mLock) {
            for (Bulletin b :
                    mData) {
                if (b != null && b.getPostID() == id) // Can be null because of bulletins first item
                    return b;
            }
        }
        return null;
    }

    public int getBulletinIndex(Bulletin b) {
        synchronized (mLock) {
            return mData.indexOf(b);
        }
    }

    public int getBulletinID(int index) {
        synchronized (mLock) {
            return mData.indexOf(getBulletin(index));
        }
    }

    public int getSize() {
        return mData.size();
    }

    public void setBulletinsLoaded(boolean isLoaded) {
        mBulletinsLoaded = isLoaded;
    }

    public boolean isBulletinsLoaded() {
        return mBulletinsLoaded;
    }


}

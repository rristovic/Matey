package com.mateyinc.marko.matey.data_and_managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mateyinc.marko.matey.model.Bulletin;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class BulletinManager {
    private String TAG = BulletinManager.class.getSimpleName();

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
                DataContract.MessageEntry.CONTENT_URI,
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

            Log.d("BulletinManager", "Bulletin added: ID=" + postId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
                    + "...; Date=" + date + "; Num of replies=" + replies.size() + "; Num of attachments=" + attachments.size());
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
    }

    private String parseRepliesToJSON(LinkedList<Bulletin.Reply> replies) {
        return new JSONArray(replies).toString();
    }

    private String parseAttachmentsToJSON(LinkedList<Bulletin.Attachment> attachments) {
        return new JSONArray(attachments).toString();
    }

    public void addBulletin(Bulletin b) {
        synchronized (mLock) {
            mData.add(b);
        }
    }

    public void addNullBulletin() {

        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + -1,
                null,
                null);

        if (msgCursor == null) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
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
    }

    public ArrayList<Bulletin> getBulletinList() {
        synchronized (mLock) {
            return mData;
        }
    }


    public Bulletin getBulletin(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_USER_ID,
                        DataContract.BulletinEntry.COLUMN_POST_ID,
                        DataContract.BulletinEntry.COLUMN_FIRST_NAME,
                        DataContract.BulletinEntry.COLUMN_LAST_NAME,
                        DataContract.BulletinEntry.COLUMN_TEXT,
                        DataContract.BulletinEntry.COLUMN_DATE,
                        DataContract.BulletinEntry.COLUMN_REPLIES,
                        DataContract.BulletinEntry.COLUMN_ATTACHMENTS},
                null,
                null,
                null);

        try {
            cursor.moveToPosition(index);
        }catch (NullPointerException e){
            Log.e(TAG,e.getLocalizedMessage(),e);
            return null;
        }
       Bulletin bulletin = new Bulletin();
        bulletin.setUserID(cursor.getInt(0));
        bulletin.setPostID(cursor.getInt(1));
        bulletin.setFirstName(cursor.getString(2));
        bulletin.setLastName(cursor.getString(3));
        bulletin.setMessage(cursor.getString(4));
        bulletin.setDate(cursor.getString(5));
        bulletin.setReplies(cursor.getString(6));
        bulletin.setAttachments(cursor.getString(7));

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

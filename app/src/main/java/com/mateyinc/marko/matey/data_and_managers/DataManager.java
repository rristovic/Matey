package com.mateyinc.marko.matey.data_and_managers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mateyinc.marko.matey.model.Message;
import com.mateyinc.marko.matey.model.Notification;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Sarma on 8/28/2016.
 */
public class DataManager {

    public final ArrayList<Notification> mNotificationList = new ArrayList<>();
    public final ArrayList<Message> mMessageList;
    public final ArrayList<UserProfile> mFriendsList;

    private final Context mAppContext;
    private static final Object mLock = new Object();
    private static DataManager mInstance;

    public static DataManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                mInstance.createDummyData();
            }
            return mInstance;
        }
    }

    private DataManager(Context context) {
        mAppContext = context;
        mFriendsList = new ArrayList<>();
        mMessageList = new ArrayList<>();
    }

    private void createDummyData() {
        Random r = new Random();
        for (int i = 0; i < 50; i++) {
            Notification n = new Notification("Proba " + i, new Date(new Date().getTime() - r.nextInt(86400000)));
            mNotificationList.add(n);
            addMessage("Proba " + i);
        }


    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param msgBody The body text of message.
     * @return the row ID of the added message.
     */
    public long addMessage(String msgBody) {
        long msgId;

        // TODO - check if exist in db then add it
// First, check if the location with this city name exists in the db
        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.MessageEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                DataContract.MessageEntry.COLUMN_MSG_BODY + " = ?",// TODO - change selection
                new String[]{msgBody},
                null);

        if (msgCursor.moveToFirst()) {
            int msgIdIndex = msgCursor.getColumnIndex(DataContract.MessageEntry._ID);
            msgId = msgCursor.getLong(msgIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues values = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            values.put(DataContract.MessageEntry.COLUMN_MSG_BODY, msgBody);

            // Finally, insert location data into the database.
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.MessageEntry.CONTENT_URI,
                    values
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            msgId = ContentUris.parseId(insertedUri);
        }
        msgCursor.close();
        // Wait, that worked?  Yes!
        return msgId;
    }

    // TODO - add data management system
}

package com.mateyinc.marko.matey.data_and_managers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data_and_managers.DataContract.NotificationEntry;
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

    public static final int BULLETIN_NO_MAX = 20; // Def for max number of bulletins downloaded and saved on disk
    public static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;
                                                        // maybe depend on no of friends and group

    public static final int ONE_DAY = 86400000;
    public static final int ONE_MIN = 60000;

    public final ArrayList<Notification> mNotificationList = new ArrayList<>();
    public final ArrayList<Message> mMessageList;
    public ArrayList<UserProfile> mFriendsList;

    private final Context mAppContext;
    private static final Object mLock = new Object();
    private static DataManager mInstance = null;

    public static DataManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                mInstance.createDummyData();
                Log.d("DataManager", "New instance of manager created.");
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
        for (int i = 1; i <= 3; i++) {
            ContentValues profileValues = new ContentValues();
            profileValues.put(DataContract.ProfileEntry._ID, i);
            profileValues.put(DataContract.ProfileEntry.COLUMN_NAME, "Radovan " + i);
            profileValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, i);

            Cursor cursor = mAppContext.getContentResolver().query(
                    DataContract.ProfileEntry.CONTENT_URI, null,
                    DataContract.ProfileEntry._ID + " = ?",
                    new String[]{Integer.toString(i)},
                    null);

            if (!(cursor != null && cursor.moveToFirst()))
                try {
                    Uri link = mAppContext.getContentResolver().insert(DataContract.ProfileEntry.CONTENT_URI, profileValues);
                } catch (Exception e) {
                    Log.d("DataManager", e.getLocalizedMessage(), e);
                }
            cursor.close();
        }
        Random r = new Random();
        Date d = new Date();
        for (int i = 0; i < 50; i++) {
            Date d1 = new Date();
            d1.setTime(d.getTime() - r.nextInt(ONE_DAY));
            int random = r.nextInt(3)+1;
            addMessage(random, "Radovan "+i, "Poruka " + i, d1.toString(), false);
            addNotif(i, "Radovan", "has commented on your", d1.toString(), "ID" + i);
        }


    }

    /**
     * Helper method to handle insertion of a new message in the database.
     *
     * @param senderId   profile id of user that generated the notification
     * @param senderName profile name of user
     * @param body       notification text
     * @param time       that notification has been created
     * @param linkId     id of the post that generated the notification
     * @return the row ID of the added notification.
     */

    public long addNotif(int senderId, String senderName, String body, String time, String linkId) {
        long notifId;

//        // TODO - check if exist in db then add it
// First, check if the location with this city name exists in the db
        Cursor cursor = mAppContext.getContentResolver().query(
                NotificationEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                NotificationEntry.COLUMN_NOTIF_LINK_ID + " = " + "\"" + linkId + "\"",// TODO - change selection
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            int msgIdIndex = cursor.getColumnIndex(DataContract.MessageEntry._ID);
            notifId = cursor.getLong(msgIdIndex);

        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues values = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            values.put(NotificationEntry.COLUMN_SENDER_ID, senderId);
            values.put(NotificationEntry.COLUMN_SENDER_NAME, senderName);
            values.put(NotificationEntry.COLUMN_NOTIF_TEXT, body);
            values.put(NotificationEntry.COLUMN_NOTIF_TIME, time);
            values.put(NotificationEntry.COLUMN_NOTIF_LINK_ID, linkId);

            // Finally, insert location data into the database.
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    NotificationEntry.CONTENT_URI,
                    values
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            notifId = ContentUris.parseId(insertedUri);

        }
        if (cursor != null)
            cursor.close();

        return notifId;
    }

    /**
     * Helper method to handle insertion of a new message in the database.
     *
     * @param senderId   profile id of user that generated the message
     * @param senderName profile name of user
     * @param body       of the message
     * @param time       that message has been created
     * @param isRead     if message is read-1 or not-0
     * @return the row ID of the added message.
     */
    public long addMessage(int senderId, String senderName, String body, String time, boolean isRead) {
        long msgId;

        // TODO - check if exist in db then add it
// First, check if the location with this city name exists in the db
        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.MessageEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                DataContract.MessageEntry.COLUMN_MSG_BODY + " = " + "\"" + body + "\"",// TODO - change selection
                null,
                null);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            int msgIdIndex = msgCursor.getColumnIndex(DataContract.MessageEntry._ID);
            msgId = msgCursor.getLong(msgIdIndex);

        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues msgValues = new ContentValues();
            ContentValues profileValues = new ContentValues();


            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_ID, senderId);
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_NAME, senderName);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_BODY, body);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_TIME, time);
            msgValues.put(DataContract.MessageEntry.COLUMN_IS_READ, isRead ? 1 : 0);

            // Finally, insert data into the database.
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.MessageEntry.CONTENT_URI,
                    msgValues
            );

            // The resulting URI contains the ID for the row.  Extract the msgId from the Uri.
            msgId = ContentUris.parseId(insertedUri);

            profileValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, msgId);
            mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, profileValues,
                    DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(senderId)});

        }

        if (msgCursor != null)
            msgCursor.close();

        return msgId;
    }

    // TODO - add data management system
}

package com.mateyinc.marko.matey.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.model.Approve;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static android.provider.Contacts.SettingsColumns.KEY;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_SUCCESS;

public class DummyData {
    private static final String TAG = DummyData.class.getSimpleName();

    private Context mAppContext;
    private UserProfile mCurUserProfile = new UserProfile();

    private UserProfile getUserProfile(int index){
        return DataManager.getInstance(mAppContext).getUserProfile(index);
    }

    public void createDummyData(Context context) {
        mAppContext = context;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
                preferences.edit().putBoolean("DATA_CREATED", true).apply();
                preferences.edit().putBoolean(KEY, true).apply();
                Random r = new Random();
                int namesSize = Util.names.length;
                int lNamesSize = Util.lastNames.length;

                mCurUserProfile.setNumOfFriends(40);
                for (int i = 1; i <= mCurUserProfile.getNumOfFriends(); i++) {
                    ContentValues userValues = new ContentValues();

                    userValues.put(DataContract.ProfileEntry._ID, i);
                    userValues.put(DataContract.ProfileEntry.COLUMN_NAME, Util.names[r.nextInt(namesSize)]);
                    userValues.put(DataContract.ProfileEntry.COLUMN_FULL_NAME, "Lorem Ipsum");
                    userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, Util.lastNames[r.nextInt(lNamesSize)]);
                    userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, mAppContext.getString(R.string.dev_email));
                    userValues.put(DataContract.ProfileEntry.COLUMN_PROF_PIC, mAppContext.getString(R.string.dev_nopic));
                    userValues.put(DataContract.ProfileEntry.COLUMN_COVER_PIC, mAppContext.getString(R.string.dev_nopic));
                    userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWING, true);
                    userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, 0);

                    mAppContext.getContentResolver().insert(
                            DataContract.ProfileEntry.CONTENT_URI,
                            userValues
                    );
                }
                Log.d(TAG, "User profiles added.");

                int itemDownloaded = 0;
                ArrayList<Bulletin> list = new ArrayList<>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);
                LinkedList<Reply> repliesList = new LinkedList<>();
                ArrayList<Approve> approveList = new ArrayList<>();
                Cursor c = mAppContext.getContentResolver().query(DataContract.ProfileEntry.CONTENT_URI,
                        null, null, null, null);
                int count = c.getCount();
                c.close();
                for (int i = 0; i < DataManager.NO_OF_BULLETIN_TO_DOWNLOAD; i++) {


                    UserProfile friend = getUserProfile(r.nextInt(count));
                    Date date = new Date();
                    date.setTime(date.getTime() - i * Util.ONE_MIN - DataManager.ONE_DAY * DataManager.mCurrentPage);

                    Bulletin bulletin = new Bulletin(
                            i + DataManager.NO_OF_BULLETIN_TO_DOWNLOAD * DataManager.mCurrentPage,
                            friend.getUserId(),
                            friend.getFirstName(),
                            friend.getLastName(),
                            Util.loremIspum,
                            new Date(),
                            STATUS_SUCCESS
                    );
                    bulletin.setNumOfReplies(r.nextInt(20));


                    for (int j = 0; j < bulletin.getNumOfReplies(); j++) {

                        UserProfile friendReplied = getUserProfile(r.nextInt(mCurUserProfile.getNumOfFriends()));
                        Reply reply = new Reply();

                        reply._id = Integer.parseInt(Long.toString(bulletin.getPostID()) + Integer.toString(j)); // replyId eg - 05: 0 - postId, 5 - replyId;
                        reply.userId = friendReplied.getUserId();
                        reply.postId = bulletin.getPostID();
                        reply.userFirstName = friendReplied.getFirstName();
                        reply.userLastName = friendReplied.getLastName();
                        reply.replyText = Util.loremIpsumShort;
                        reply.replyDate = new Date(date.getTime() - Util.ONE_MIN * j - Util.ONE_DAY * DataManager.mCurrentPage);

                        for (int k = 0; k < r.nextInt(5); k++) {
                            UserProfile profile = getUserProfile(r.nextInt(mCurUserProfile.getNumOfFriends()));
                            reply.replyApproves.add(profile);
                            approveList.add(new Approve(profile.getUserId(), bulletin.getPostID(), reply._id));
                            reply.numOfApprvs++;
                        }

                        repliesList.add(reply);
                    }

                    list.add(bulletin);
                    itemDownloaded++;
                }
                addBulletins(list);
                addReplies(repliesList);
                addApproves(approveList);

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mAppContext);
                Intent i = new Intent(DataManager.BULLETIN_LIST_LOADED);
                i.putExtra(DataManager.EXTRA_ITEM_DOWNLOADED_COUNT, itemDownloaded);

                // Notifying HomeActivity that the data has been downloaded with broadcast and static member TODO - notify in onPostExecute later
                broadcastManager.sendBroadcast(i);
            }
        });
        thread.start();
    }

    /**
     * Method for adding list of Replies to the database
     *
     * @param list the list to be added to the db
     */
    public void addReplies(LinkedList<Reply> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(list.size());

        int numReplies = 0;
        for (Reply r : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.ReplyEntry._ID, r._id);
            values.put(DataContract.ReplyEntry.COLUMN_POST_ID, r.postId);
            values.put(DataContract.ReplyEntry.COLUMN_USER_ID, r.userId);
            values.put(DataContract.ReplyEntry.COLUMN_FIRST_NAME, r.userFirstName);
            values.put(DataContract.ReplyEntry.COLUMN_LAST_NAME, r.userLastName);
            values.put(DataContract.ReplyEntry.COLUMN_TEXT, r.replyText);
            values.put(DataContract.ReplyEntry.COLUMN_DATE, r.replyDate.getTime());
            values.put(DataContract.ReplyEntry.COLUMN_NUM_OF_APPRVS, r.numOfApprvs);

            cVVector.add(values);
            numReplies++;
        }


        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.ReplyEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " replies added");
    }

    /**
     * Method for adding list of Bulletin to database
     *
     * @param list to be added
     */
    public void addBulletins(ArrayList<Bulletin> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(list.size());

        for (Bulletin b : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry._ID, b.getPostID());
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, b.getUserID());
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, b.getFirstName());
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, b.getLastName());
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, b.getMessage());
            values.put(DataContract.BulletinEntry.COLUMN_DATE, b.getDate().getTime());
            values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, b.getNumOfReplies());
            values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, STATUS_SUCCESS);

            cVVector.add(values);
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
    }

    /**
     * method for adding new approves to the database
     * @param list list of {@link Approve} objects
     */
    public void addApproves(List<Approve> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(list.size());

        for (Approve approve : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.ApproveEntry.COLUMN_REPLY_ID, approve.replyId);
            values.put(DataContract.ApproveEntry.COLUMN_USER_ID, approve.userId);
            values.put(DataContract.ApproveEntry.COLUMN_POST_ID, approve.postId);
            values.put(DataContract.ApproveEntry.COLUMN_SERVER_STATUS, approve.getServerStatus());
            cVVector.add(values);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.ApproveEntry.CONTENT_URI, cvArray);
            // TODO - delete old data
        }
        Log.d(TAG, inserted + " approves added.");
    }
}


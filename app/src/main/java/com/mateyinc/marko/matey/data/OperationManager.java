package com.mateyinc.marko.matey.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mateyinc.marko.matey.data.DataContract.ApproveEntry;
import com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.data.DataContract.NotUploadedEntry;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.internet.operations.NewsfeedOp;
import com.mateyinc.marko.matey.data.internet.operations.OperationType;
import com.mateyinc.marko.matey.data.internet.operations.Operations;
import com.mateyinc.marko.matey.data.internet.operations.UserProfileOp;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mateyinc.marko.matey.data.OperationManager.ServerStatus.STATUS_SUCCESS;

/**
 * Class used for downloading and uploading data to th server
 */
public class OperationManager implements OperationProvider {
    private static final String TAG = OperationManager.class.getSimpleName();

    public static final int CACHE_SIZE_MB = 100; // Max cache size on disk for storing data

    /** Number of bulletins to startDownloadAction from the server */
    public static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /** The current page of bulletins in the database */
    public static int mCurrentPage = 0;

    // One day in milliseconds
    public static final int ONE_DAY = 86400000;
    // One minute in milliseconds
    public static final int ONE_MIN = 60000;

    /** Loader identifiers for data */
    public static final int BULLETINS_LOADER = 100;
    public static final int BULLETIN_LOADER = 101;

    /** Columns that bulletin cursor loaders will load up */
    public static final String[] BULLETIN_COLUMNS = {
            DataContract.BulletinEntry.TABLE_NAME + "." + DataContract.BulletinEntry._ID,
            DataContract.BulletinEntry.COLUMN_USER_ID,
            DataContract.BulletinEntry.COLUMN_FIRST_NAME,
            DataContract.BulletinEntry.COLUMN_LAST_NAME,
            DataContract.BulletinEntry.COLUMN_TEXT,
            DataContract.BulletinEntry.COLUMN_DATE,
            DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES,
            DataContract.BulletinEntry.COLUMN_SERVER_STATUS,
            DataContract.BulletinEntry.COLUMN_ATTACHMENTS,
            BulletinEntry.COLUMN_NUM_OF_LIKES,
            BulletinEntry.COLUMN_SUBJECT,
            "(SELECT COUNT(" + DataContract.ReplyEntry.COLUMN_POST_ID +
                    ") FROM " + DataContract.ReplyEntry.TABLE_NAME + " WHERE " +
                    DataContract.ReplyEntry.TABLE_NAME + "." + DataContract.ReplyEntry.COLUMN_POST_ID + " = "
                    + BulletinEntry.TABLE_NAME + "." + BulletinEntry._ID + ")",
            "(SELECT COUNT(" + ApproveEntry._ID +
                    ") FROM " + ApproveEntry.TABLE_NAME + " WHERE " +
                    ApproveEntry.TABLE_NAME + "." + ApproveEntry.COLUMN_POST_ID + " = "
                    + BulletinEntry.TABLE_NAME + "." + BulletinEntry._ID + ")"

    };

    // These indices are tied to BULLETIN_COLUMNS.  If BULLETIN_COLUMNS changes, these
    // must change.
    public static final int COL_POST_ID = 0;
    public static final int COL_USER_ID = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_LAST_NAME = 3;
    public static final int COL_TEXT = 4;
    public static final int COL_DATE = 5;
    public static final int COL_NUM_OF_REPLIESs = 6;
    public static final int COL_ON_SERVER = 7;
    public static final int COL_ATTCHS = 8;
    public static final int COL_NUM_OF_LIKESs = 9;
    public static final int COL_SUBJECT = 10;
    public static final int COL_NUM_OF_REPLIES = 11;
    public static final int COL_NUM_OF_LIKES = 12;


    public  final ImageLoader mImageLoader;

    @Override
    public void submitRequest(Request request) {
        mRequestQueue.add(request);
    }

    @Override
    public void submitRunnable(Runnable runnable) {
        mExecutor.submit(runnable);
    }

    @Override
    public String getAccessToken() {
        return MotherActivity.access_token;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public interface ServerStatus {
        /** Activity startUploadAction status that is saved in database, and used for UI control */
        int STATUS_RETRY_UPLOAD = -1;
        /** @see #STATUS_RETRY_UPLOAD */
        int STATUS_UPLOADING = 0;
        /**
         * Activity startUploadAction status that is saved in database, and used for UI control;
         * If data is downloaded from the server, this is the status that gets saved
         */
        int STATUS_SUCCESS = 1;
    }

    /** Used for new post that doesn't yet have the post_id */
    public static final int NO_POST_ID = -1;


    // JSON array names
    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOAD_FAILED = "com.mateyinc.marko.matey.data.internet.home.bulletins_load_failed";
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.data.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.data.internet.home.bulletins_loaded_count";

    // Global instance fields
    private  final Object mLock = new Object(); // for synchronised blocks
    private static OperationManager mInstance ;
    private Context mAppContext;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(10);

//    private UserProfile mCurrentUserProfile;
    private int newPostID;

    /**
     * Method for retrieving OperationManager singleton
     *
     * @param context the context of calling class
     * @return the OperationManager instance
     */
    public static synchronized OperationManager getInstance(Context context) {
            if (mInstance == null) {
                mInstance = new OperationManager(context.getApplicationContext());
//                mInstance.addNullBulletin();
                Log.d(TAG, "New instance of OperationManager created.");
            }

            return mInstance;
    }

    private OperationManager(Context context) {
        mAppContext = context;
        mRequestQueue = Volley.newRequestQueue(context);

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }


    //  General methods ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Method for creating new activity_id, which is later replaced by the id returned from the server;
     * Newly created ids are starting from -2 to {@value Integer#MIN_VALUE}
     */
    public long createNewActivityId() {
        Cursor c = mAppContext.getContentResolver().query(NotUploadedEntry.CONTENT_URI, null, null, null, null);
        long id = -2;

        if (c != null) {
            int count = c.getCount()+1;
            id = -(count + 1);
            c.close();
        }

        return id;
    }
    ////////////////////////////////////////////////////////////////


    // UserProfileOps methods /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    private LinkedList<UserProfile> suggestedFriends;

    public LinkedList<UserProfile> getSuggestedFriendsList() {
        return suggestedFriends;
    }

    public void setSuggestedFriends(LinkedList<UserProfile> list) {
        suggestedFriends = list;
    }


    /**
     * Method for adding list of user profile to the database
     *
     * @param list the list of user profiles ready for the database
     * @param areFollowed indicates if these profiles are followed by the current user
     */
    public void addUserProfiles(ArrayList<UserProfile> list, boolean areFollowed) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(OperationManager.NO_OF_BULLETIN_TO_DOWNLOAD);

        for (UserProfile profile : list) {
            ContentValues userValues = new ContentValues();

            userValues.put(ProfileEntry._ID, profile.getUserId());
            userValues.put(ProfileEntry.COLUMN_NAME, profile.getFirstName());
            userValues.put(ProfileEntry.COLUMN_LAST_NAME, profile.getLastName());
            userValues.put(ProfileEntry.COLUMN_EMAIL, profile.getEmail());
            userValues.put(ProfileEntry.COLUMN_PROF_PIC, profile.getProfilePictureLink());
            userValues.put(ProfileEntry.COLUMN_FOLLOWING, profile.isFriend() ? 1 : 0);
            userValues.put(ProfileEntry.COLUMN_LAST_MSG_ID, profile.getLastMsgId());
            if(areFollowed)
                userValues.put(ProfileEntry.COLUMN_FOLLOWING, true);

            cVVector.add(userValues);
            Log.d(TAG, "Bulletin added: " + userValues.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(ProfileEntry.CONTENT_URI, cvArray);
            // TODO - delete old data
        }
        Log.d(TAG, inserted + " user profile added.");
    }


    /**
     * Returns user profile from db
     *
     * @param index position of user profile in database
     * @return new instance of UserProfileOps from database
     */
    public UserProfile getUserProfile(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ProfileEntry.CONTENT_URI,
                new String[]{DataContract.ProfileEntry._ID, DataContract.ProfileEntry.COLUMN_NAME,
                        DataContract.ProfileEntry.COLUMN_LAST_NAME, DataContract.ProfileEntry.COLUMN_LAST_MSG_ID},
                null,
                null,
                null);

        UserProfile profile = null;
        try {
            cursor.moveToPosition(index);

            profile = new UserProfile();
            profile.setUserId(cursor.getInt(0));
            profile.setFirstName(cursor.getString(1));
            profile.setLastName(cursor.getString(2));
            profile.setLastMsgId(cursor.getInt(3));

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        } finally {
            try {
                cursor.close();
            } catch (Exception e) {
            }
        }

        return profile;
    }

    public void removeUserProfile(long user_id) {
        int numOfRows = mAppContext.getContentResolver().delete(DataContract.ProfileEntry.CONTENT_URI,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Long.toString(user_id)});

        if (numOfRows == 1) {
            Log.d(TAG, "User profile with id=" + user_id + " has been successfully removed from db.");
        } else {
            Log.d(TAG, "Error deleting user profile with id=" + user_id);
        }
    }

    // Bulletins methods ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    public static final String BULLETIN_ORDER = DataContract.BulletinEntry.COLUMN_DATE + " DESC";
    public static final int NUM_OF_BULLETINS_TO_DOWNLOAD = 40;

    /**
     * Method for parsing the JSON data retrieved from the server to the database
     *
     * @param response the string response from the server
     */
    public void parseBulletins(String response) throws JSONException, ParseException {
        JSONArray bulletinArray = new JSONArray(response);
        ArrayList<Bulletin> bulletinList = new ArrayList<>(bulletinArray.length());

        for (int i = 0; i < bulletinArray.length(); i++) {
            Bulletin b = Bulletin.parseBulletin(bulletinArray.get(i).toString());
            bulletinList.add(b);
        }

        addBulletins(bulletinList);
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
    }

    /**
     * Method for getting the bulletin from the database
     *
     * @param index  the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index, Cursor cursor) {
        Bulletin bulletin;
        try {
            cursor.moveToPosition(index);

            bulletin = new Bulletin(
                    cursor.getInt(COL_POST_ID),
                    cursor.getInt(COL_USER_ID),
                    cursor.getString(COL_FIRST_NAME),
                    cursor.getString(COL_LAST_NAME),
                    cursor.getString(COL_TEXT),
                    new Date(cursor.getLong(COL_DATE)));

            bulletin.setmNumOfLikes(cursor.getInt(COL_NUM_OF_LIKES));
            bulletin.setSubject(cursor.getString(COL_SUBJECT));

            bulletin.setServerStatus(cursor.getInt(COL_ON_SERVER));
            bulletin.setNumOfReplies(cursor.getInt(COL_NUM_OF_REPLIES));
            bulletin.setAttachmentsFromJSON(cursor.getString(7));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return bulletin;
    }

    /**
     * Method for getting the bulletin from the database with the default cursor
     *
     * @param index the position of the bulletin in the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                BULLETIN_COLUMNS,
                null,
                null,
                BULLETIN_ORDER);

        Bulletin bulletin = null;
        try {
            bulletin = getBulletin(index, cursor);

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }

        if (cursor != null)
            cursor.close();

        return bulletin;
    }

    /**
     * Returns the number of bulletins in the database
     */
    public int getNumOfBulletinsInDb() {
        try {
            Cursor c = mAppContext.getContentResolver().query(DataContract.BulletinEntry.CONTENT_URI, null, null, null, null, null);
            // -1 becouse of null bulletin
            int count = c.getCount() - 1;
            c.close();
            return count;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return 0;
        }
    }

    ////// Replies methods ///////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////

    /**
     * Cursor sort order
     */
    public static final String REPLIES_ORDER_BY = DataContract.ReplyEntry.COLUMN_DATE + " DESC";

    public Context getContext() {
        return mAppContext;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    private RequestQueue mRequestQueue;
    private Response.Listener<String> mSuccessListener;
    private Response.ErrorListener mErrorListener;


    /// Bulletins methods   ////
    ////////////////////////////
    /**
     * Method for downloading and parsing news feed from the server, and all data around it
     * @param startPosition the start position of the bulletin
     * @param count the total bulletin count that needs to be downloaded in a single burst
     * @param context the Context used for notifying when the parsing result is complete
     */
    public void downloadNewsFeed(int startPosition, int count, MotherActivity context) {
        NewsfeedOp newsfeedOp = new NewsfeedOp(this, context);
        setListeners(newsfeedOp);
        newsfeedOp.setCount(count).setStartPos(startPosition)
                .startDownloadAction();
    }

    /**
     * Helper method for downloading news feed from the server to the database;
     * Downloads {@value OperationManager#NUM_OF_BULLETINS_TO_DOWNLOAD} bulletins from the server;
     * Automatically determines from what bulletin position to startDownloadAction by calling {@link OperationManager#getNumOfBulletinsInDb()}
     *
     * @param context the context of activity which is calling this method
     */
    public void downloadNewsFeed(final MotherActivity context) {
        int start = getNumOfBulletinsInDb();
        downloadNewsFeed(start, OperationManager.NUM_OF_BULLETINS_TO_DOWNLOAD, context);
    }

    /**
     * Helper method for posting new bulletin with attachments.
     * @param subject bulletin's subject
     * @param message bulletin's text
     * @param attachments bulletin's attachment list that contains file paths.
     */
    public void postNewBulletin(String subject, String message, @Nullable List<String> attachments, Context context) {
        Bulletin b = new Bulletin(MotherActivity.user_id, MotherActivity.mCurrentUserProfile.getFirstName(),
                MotherActivity.mCurrentUserProfile.getLastName(), subject, message, new Date());
        b.setAttachments(attachments); // TODO - set threading
        b.save(context);
    }

    /**
     * Helper method for posting new bulletin without attachments.
     * @param subject bulletin's subject.
     * @param message bulletin's text.
     */
    public void postNewBulletin(String subject, String message, Context context) {
        postNewBulletin(subject, message, null, context);
    }

    /**
     * Helper method for liking/unliking bulletin
     * @param bulletinPosition position of bulletin in database
     * @param cursor {@link Cursor} opened to the database
     * @param context context for database communication
     */
    public void newPostLike(int bulletinPosition, Cursor cursor, Context context) {
        Bulletin b = DataAccess.getBulletin(bulletinPosition, cursor);
        if (b != null)
            b.like(context);
    }
    /**
     * Helper method for liking/unliking bulletin
     * @param b bulletitn to be liked/unliked
     * @param context context for database communication
     */
    public void newPostLike(Bulletin b, Context context) {
        if (b != null)
            b.like(context);
    }

    /**
     * Helper method for liking/unliking reply
     * @param replyPosition position of reply in database
     * @param cursor {@link Cursor} opened to the database
     * @param context context for database communication
     */
    public void newReplyLike(int replyPosition, Cursor cursor, Context context) {
        Reply r = DataAccess.getReply(replyPosition, cursor);
        if (r != null)
            r.like(context);
    }
    //// Reply methods         /////
    ////////////////////////////////

    /**
     * Helper method for posting new reply on bulletin
     * @param bulletin {@link Bulletin} bulletin to reply on
     * @param r {@link Reply} new reply object
     * @param context context used for database communication
     */
    public void postNewReply(Reply r, Bulletin bulletin, Context context) {
        r.reply(context, bulletin);
    }

    /**
     * Helper method for posting new reply on reply
     * @param reply {@link Reply} reply to reply on
     * @param r {@link Reply} new reply object
     * @param context context used for database communication
     */
    public void postNewReply(Reply r, Reply reply, Context context) {
        r.reply(context, reply);
    }

    //// User profile methods ///////
    /////////////////////////////////
    /**
     * Helper method for downloading user profile data from the server;
     * @param userId id of the user profile
     */
    public void downloadUserProfile(long userId, MotherActivity context) {
        UserProfileOp op = new UserProfileOp(context);
        setListeners(op);
        op.setOperationType(OperationType.DOWNLOAD_USER_PROFILE);
        op.setUserId(userId).startDownloadAction();
    }

    /**
     * Helper method for downloading user followers.
     * @param offset starting position of followers;
     * @param count offset indicates how much will be downloaded;
     * @param context {@link MotherActivity} context used for download/save ops;
     */
    public void downloadFollowers(int offset, int count, long id, MotherActivity context) {
        UserProfileOp op = new UserProfileOp(context);
        setListeners(op);
        op.setOperationType(OperationType.DOWNLOAD_FOLLOWERS);
        op.setCount(count).setOffset(offset).setUserId(id)
                .startDownloadAction();
    }

    /**
     * Helper method to use when new user profile has been followed.
     * Updates followed user profile database.
     * @param userId id of the user being followed;
     * @param context {@link MotherActivity} context used for upload/save operations;
     */
    public void followNewUser(long userId, MotherActivity context) {
        UserProfileOp operation = new UserProfileOp(context);
        setListeners(operation);
        operation.setOperationType(OperationType.FOLLOW_USER_PROFILE);
        operation.setUserId(userId).startUploadAction();
    }

    /**
     * Helper method to use when user profile has been unfollowed.
     * Updates unfollowed user profile database.
     * @param userId id of unfollowed user
     * @param context {@link MotherActivity} context used for upload/save operations;
     */
    public void unfollowUser(long userId, MotherActivity context) {
        UserProfileOp operation = new UserProfileOp(context);
        setListeners(operation);
        operation.setOperationType(OperationType.UNFOLLOW_USER_PROFILE);
        operation.setUserId(userId).startUploadAction();
    }

    /**
     * Method for adding custom success download/upload listener
     * @param listener listener to be added
     */
    public void addSuccessListener(Response.Listener<String> listener){
        mSuccessListener = listener;
    }

    /**
     * Method for adding custom error download/upload listener
     * @param listener listener to be added
     */
    public void addErrorListener(Response.ErrorListener listener){
        mErrorListener = listener;
    }

    /**
     * Method for setting operation listeners if they exits, otherwise they will be set by default
     * @param operation {@link Operations} to set listeners on
     */
    private void setListeners(Operations operation){
        if (null != mSuccessListener)
            operation.addSuccessListener(mSuccessListener);

        if (null != mErrorListener)
            operation.addFailedListener(mErrorListener);
    }
}


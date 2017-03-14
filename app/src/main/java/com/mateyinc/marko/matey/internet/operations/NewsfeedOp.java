package com.mateyinc.marko.matey.internet.operations;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationProvider;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import static com.mateyinc.marko.matey.internet.UrlData.ACCESS_BASE_URL;
import static com.mateyinc.marko.matey.internet.UrlData.GET_NEWSFEED_ROUTE;
import static com.mateyinc.marko.matey.internet.operations.OperationType.DOWNLOAD_NEWS_FEED;


public class NewsfeedOp extends Operations {
    private static final String TAG = NewsfeedOp.class.getSimpleName();

    private boolean mClearData = false;

    /**
     * Contains url for next page of data. If empty, no page has already been downloaded.
     */
    private static String mNextUrl = "";


    public NewsfeedOp(MotherActivity context) {
        super(context, DOWNLOAD_NEWS_FEED);
    }

    public NewsfeedOp(OperationProvider provider, MotherActivity context) {
        super(provider, context, DOWNLOAD_NEWS_FEED);
    }

    @Override
    public void startDownloadAction() {
        String url;

        switch (mOpType) {
            default:
            case DOWNLOAD_NEWS_FEED: {
                Log.d(TAG, "Downloading news feed.");
                Uri.Builder builder;
                if (mNextUrl.isEmpty()) { // If initial download, clear everything first
                    builder = Uri.parse(GET_NEWSFEED_ROUTE).buildUpon();
                    mClearData = true;
                } else
                    builder = Uri.parse(ACCESS_BASE_URL).buildUpon()
                            .appendEncodedPath(mNextUrl);
                url = builder.build().toString();
                break;
            }
            case DOWNLOAD_NEWS_FEED_NEW: {
                Log.d(TAG, "Downloading news feed.");
                mClearData = true;
                Uri.Builder builder = Uri.parse(GET_NEWSFEED_ROUTE).buildUpon();
                url = builder.build().toString();
                break;
            }
        }

        createNewDownloadReq(url);
        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        try {
            JSONObject object = new JSONObject(response);

            JSONObject pagination = object.getJSONObject(KEY_PAGINATION);
            pagination = pagination.getJSONObject(KEY_LINKS);

            try {
                mNextUrl = pagination.getString(KEY_NEXT_URL);
                mNextUrl = mNextUrl.substring(mNextUrl.lastIndexOf("/") + 1);// save next url
            } catch (Exception e){
                Log.d(TAG,"No value for next link.");
                mNextUrl = "#";
            }

            JSONArray data = object.getJSONArray(Operations.KEY_DATA);
            // Parsing
            int length = data.length();
            Vector<ContentValues> bulletinList = new Vector<>(length);
            Vector<ContentValues> userList = new Vector<>();
            for (int i = 0; i < length; i++) {
                // Get first object in array
                object = data.getJSONObject(i);
                // See if this is POST object
                if (object.getString(Operations.KEY_ACTIVITY_TYPE)
                        .equals(Operations.VALUE_ATYPE_POST)) { // This is bulletin
                    // Parse it and add to values array
                    try {
                        JSONObject activityObj = object.getJSONObject(Operations.KEY_ACTIVITY_OBJECT);
                        Bulletin b = Bulletin.parseBulletin(activityObj.toString());
                        b.setServerStatus(ServerStatus.STATUS_SUCCESS);
                        UserProfile userProfile = UserProfile.parseUserProfile(
                                activityObj.getString(Bulletin.KEY_USER_PROFILE));
                        userProfile.setServerStatus(ServerStatus.STATUS_SUCCESS);
                        b.setFirstName(userProfile.getFirstName());
                        b.setLastName(userProfile.getLastName());
                        userList.add(userProfile.toValues());
                        bulletinList.add(b.toValues());
                    } catch (JSONException e) { // Failed parsing this bulletin
                        e.printStackTrace();
                    }
                }
            }

            if (mClearData)
                clearDb(mContextRef.get());

            // Save
            Context c = mContextRef.get();
            if (c != null) {
                ContentValues[] values = new ContentValues[bulletinList.size()];
                int entries = c.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI,
                        bulletinList.toArray(values));
                if (entries != 0) {
                    Log.d(TAG, String.format("Inserted %d bulletins out of %d into db.", entries, length));
                } else {
                    Log.e(TAG, String.format("Failed to insert %d bulletins.", length));
                }
            }
            if (c != null) {
                ContentValues[] values = new ContentValues[userList.size()];
                int entries = c.getContentResolver().bulkInsert(DataContract.ProfileEntry.CONTENT_URI,
                        userList.toArray(values));
                if (entries != 0) {
                    Log.d(TAG, String.format("Inserted %d user profiles out of %d.", entries, userList.size()));
                } else {
                    Log.e(TAG, String.format("Failed to insert %d profiles.", userList.size()));
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Method for cleaning up db
     */
    private void clearDb(Context context) {
        Log.w(TAG, "Clearing data.");

        context.getContentResolver().delete(DataContract.ApproveEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.ReReplyEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.ReplyEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.BulletinEntry.CONTENT_URI, null, null);

        mClearData = false;
    }


    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public void startUploadAction() {

    }

    @Override
    protected void onUploadSuccess(String response) {

    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    @Override
    protected String getTag() {
        return null;
    }
}

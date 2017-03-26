package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationProvider;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mateyinc.marko.matey.internet.operations.OperationType.DOWNLOAD_NEWS_FEED;


public class NewsfeedOp extends Operations {
    private static final String TAG = NewsfeedOp.class.getSimpleName();

    /**
     * Contains url for next page of data. If empty, no page has already been downloaded.
     */
    protected static String mNextUrl = "";


    public NewsfeedOp(MotherActivity context) {
        super(context, DOWNLOAD_NEWS_FEED);
    }

    public NewsfeedOp(OperationProvider provider, MotherActivity context) {
        super(provider, context, DOWNLOAD_NEWS_FEED);
    }


    @Override
    public void startDownloadAction() {

        switch (mOpType) {
            default:
            case DOWNLOAD_NEWS_FEED: {
                Log.d(TAG, "Downloading news feed.");
                if (mNextUrl.isEmpty()) { // If initial download, clear everything first
                    mUrl = UrlData.GET_NEWSFEED_ROUTE;
//                    mClearData = true;
                } else
                    mUrl = buildNextPageUrl(mNextUrl);
                break;
            }
//            case DOWNLOAD_NEWS_FEED_NEW: {
//                Log.d(TAG, "Downloading news feed.");
//                mClearData = true;
//                Uri.Builder builder = Uri.parse(GET_NEWSFEED_ROUTE).buildUpon();
//                mUrl = builder.build().toString();
//                break;
//            }
        }
        startDownload();
    }


    @Override
    protected void onDownloadSuccess(String response) {
        try {
            JSONObject object = new JSONObject(response);

            mNextUrl = parseNextUrl(object);

            JSONArray data = object.getJSONArray(Operations.KEY_DATA);
            // Parsing
            int length = data.length();
//            Vector<ContentValues> bulletinList = new Vector<>(length);
            List<Bulletin> bulletinList = new ArrayList<>();
            List<UserProfile> userList = new ArrayList<>();
//            Vector<ContentValues> userList = new Vector<>();
            for (int i = 0; i < length; i++) {
                // Get first object in array
                object = data.getJSONObject(i);
                // See if this is POST object
                if (object.getString(Operations.KEY_ACTIVITY_TYPE)
                        .equals(Operations.VALUE_ATYPE_POST)) { // This is bulletin
                    // Parse it and add to values array
                    try {
                        JSONObject activityObj = object.getJSONObject(Operations.KEY_ACTIVITY_OBJECT);
                        // Parse bulletin
                        Bulletin b = new Bulletin().parse(activityObj);
                        b.setServerStatus(ServerStatus.STATUS_SUCCESS);
                        // Parse user profile
                        UserProfile userProfile = UserProfile.parseUserProfile(
                                activityObj.getString(Bulletin.KEY_USER_PROFILE));
                        userProfile.setServerStatus(ServerStatus.STATUS_SUCCESS);
                        // Set bulletin's user
                        b.setUserProfile(userProfile);
//                        userList.add(userProfile.toValues());
                        // Add data to list
//                        userList.add(userProfile);
                        bulletinList.add(b);
                    } catch (JSONException e) { // Failed parsing this bulletin
                        e.printStackTrace();
                    }
                }
            }

            Context c = mContextRef.get();

            if (c != null) {
                DataAccess dataAccess = DataAccess.getInstance(c);
                if (shouldClearData()) {
                    dataAccess.setBulletins(bulletinList);
                    dataCleared();
                } else
                    dataAccess.addBulletins(bulletinList);
//                dataAccess.addUserProfile(bulletinList);
//                if (mDownloadListener != null)
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mDownloadListener.onDownloadSuccess();
//                        }
//                    });
                mEventBus.post(new DownloadEvent(true));
            }


        } catch (JSONException e) {
//            if (mDownloadListener != null)
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mDownloadListener.onDownloadFailed();
//                    }
//                });
            mEventBus.post(new DownloadEvent(false));
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

//        mClearData = false;
    }


    @Override
    protected void onDownloadFailed(VolleyError error) {
//        if (mDownloadListener != null)
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadListener.onDownloadFailed();
//                }
//            });
        mEventBus.post(new DownloadEvent(false));
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
    protected void clearNextUrl() {
        NewsfeedOp.mNextUrl = "";
    }

    @Override
    protected String getTag() {
        return NewsfeedOp.class.getSimpleName();
    }
}

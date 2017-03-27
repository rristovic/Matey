package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.model.Notification;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class NotificationOp extends Operations {
    private static final String TAG = NotificationOp.class.getSimpleName();
    /**
     * Contains url for next page of data. If empty, no page has already been downloaded.
     */
    protected static String mNextUrl = "";

    private long mUserId;

    public NotificationOp(Context context) {
        super(context);
    }

    @Override
    public void startDownloadAction() {
        Log.d(TAG, "Downloading notification list.");
        mUrl = UrlData.GET_NOTIFICATIONS;
        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        String r = response;
        try {
            JSONObject object = new JSONObject(response);
            mNextUrl = parseNextUrl(object);

            JSONArray array = object.getJSONArray(KEY_DATA);
            int size = array.length();
            List<Notification> notificationList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                try {
                    Notification n = new Notification().parse(array.getJSONObject(i));
                    notificationList.add(n);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse notification.", e);
                }
            }

            DataAccess access = DataAccess.getInstance(mContextRef.get());
            if (shouldClearData()) {
                access.mNotificationList.clear();
                dataCleared();
            }
            access.mNotificationList.addAll(notificationList);

            // Notify UI
            EventBus.getDefault().post(new DownloadEvent(true));
        } catch (JSONException e) {
            EventBus.getDefault().post(new DownloadEvent(false));
            Log.e(TAG, "Failed to parse notification list.", e);
        }
    }

    private void parseItem(String response) {
//        try{
//            JSONObject object = new JSONObject(response).getJSONObject(KEY_DATA);
//            this.group = group.parse(object);
//            // Notify
//            EventBus.getDefault().post(new DownloadEvent(true, OperationType.DOWNLOAD_GROUP_INFO));
//        }catch (JSONException e){
//            Log.e(TAG, "Failed to parse group item.", e);
//        }
    }

    private void parseList(String response) {
//        try {
//            JSONObject object = new JSONObject(response);
//            mNextUrl = parseNextUrl(object);
//            JSONArray groupArray = object.getJSONArray(KEY_DATA);
//
//            // Parsing
//            int size = groupArray.length();
//            List<Group> groupList = new ArrayList<>(size);
//            for (int i = 0; i < size; i++) {
//                Group group = new Group().parse(groupArray.getJSONObject(i));
//                groupList.add(group);
//            }
//
//            // Save
//            Context c = mContextRef.get();
//            if (c != null) {
//                DataAccess da = DataAccess.getInstance(c);
//                if (shouldClearData()) {
//                    da.setGroups(groupList);
//                    dataCleared();
//                } else {
//                    da.addGroups(groupList);
//                }
//            }
//
//            // Notify UI
//            mEventBus.post(new DownloadEvent(true));
//        } catch (JSONException e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
//            // Notify UI
//            mEventBus.post(new DownloadEvent(false));
//        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public void startUploadAction() {
        switch (mOpType) {
            case POST_NEW_GROUP: {
                mUrl = UrlData.POST_NEW_GROUP;
                mMethod = Request.Method.POST;
                uploadGroup();
                break;
            }
            default:
                return;
        }
    }

    private void uploadGroup() {
//        // Build request
//        RequestBody requestBody;
//        try {
//            if (picFilePath != null) {
//                String path = picFilePath.toString();
//                requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("json_data", null,
//                                RequestBody.create(MediaType.parse("application/json"), buildJSONData().toString()))
//                        .addFormDataPart("group_picture", path.substring(path.lastIndexOf("/") + 1),
//                                RequestBody.create(MediaType.parse("image/jpeg"),
//                                        ImageCompress.compressImageToFile(picFilePath, mContextRef.get()))).build();
//
//            } else {
//                requestBody = RequestBody.create(MediaType.parse("application/json"), buildJSONData().toString());
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "Failed to prepare group data for upload.");
//            return;
//        }
//
//        // Create network request
//        final OkHttpClient client = new OkHttpClient();
//        final okhttp3.Request request = new okhttp3.Request.Builder()
//                .header(UrlData.PARAM_AUTH_TYPE, "Bearer " + MotherActivity.access_token)
//                .url(mUrl)
//                .post(requestBody)
//                .build();
//
//        try {
//            // Send request
//            Response response = client.newCall(request).execute();
//            // Get context
//            Context c = mContextRef.get();
//            if (c == null) return;
//            // Process response
//            if (response.isSuccessful()) {
//                String re = response.body().string();
//                // Parse
//                this.group.parse(new JSONObject(re).getJSONObject(KEY_DATA));
//                // Notify UI
//
//            } else {
//                Log.e(TAG, "Upload failed: " + response.message() + " " + response.body().string());
//                // Notify UI
//            }
//        } catch (IOException e) {
//            Log.e(TAG, e.getLocalizedMessage(), e);
//        } catch (JSONException e) {
//            Log.e(TAG, "Failed to parse response.");
//        }


    }
//
//    private JSONObject buildJSONData() throws JSONException {
//        JSONObject object = new JSONObject();
//        object.put(GROUP_NAME_FIELD, this.group.getGroupName());
//        object.put(DESCR_FIELD, this.group.getDescription());
//        return object;
//    }

    @Override
    protected void onUploadSuccess(String response) {

    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    @Override
    protected void clearNextUrl() {
        mNextUrl = "";
    }

//    public void setPicFilePath(File picFilePath) {
//        this.picFilePath = picFilePath;
//    }
//
//    public void setUserId(long userId) {
//        this.mUserId = userId;
//    }

    @Override
    protected String getTag() {
        return TAG;
    }


}

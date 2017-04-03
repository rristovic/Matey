package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.TemporaryDataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.DownloadTempListEvent;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Group;
import com.mateyinc.marko.matey.utils.ImageCompress;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GroupOp extends Operations {
    private static final String TAG = GroupOp.class.getSimpleName();
    /**
     * Required json data field - group name
     **/
    private static final String GROUP_NAME_FIELD = "group_name";
    /**
     * Required json data field - description
     **/
    private static final String DESCR_FIELD = "description";

    /**
     * Contains url for next page of data. If empty, no page has already been downloaded.
     */
    protected static String mGroupListNextUrl = "";
    protected static String mGroupActivitiesNextUrl = "";

    private long mUserId;
    private long mGroupId;
    private Group mGroup;
    private File picFilePath;

    public GroupOp(Context context) {
        super(context);
    }

    public GroupOp(Context context, long groupId) {
        super(context);
        this.mGroupId = groupId;
    }

    public GroupOp(Context context, Group group) {
        super(context);
        this.mGroup = group;
    }


    @Override
    public void startDownloadAction() {
        switch (mOpType) {
            case DOWNLOAD_GROUP_LIST: {
                Log.d(TAG, "Downloading group list.");
                if (mGroupListNextUrl.isEmpty()) {
//                    mClearData = true;
                    mUrl = UrlData.buildGetGroupList(mUserId);
                } else {
                    mUrl = buildNextPageUrl(mGroupListNextUrl);
                }
                break;
            }
            case DOWNLOAD_GROUP_INFO: {
                Log.d(TAG, "Downloading group info.");
                mUrl = UrlData.buildGetGroupInfo(mGroupId);
                break;
            }
            case DOWNLOAD_GROUP_ACTIVITY_LIST: {
                Log.d(TAG, "Downloading group activity list.");
                if (mGroupActivitiesNextUrl.isEmpty()) {
                    mUrl = UrlData.buildGetGroupActivityList(mGroupId);
                } else
                    mUrl = buildNextPageUrl(mGroupActivitiesNextUrl);
                break;
            }
            default:
                return;
        }

        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        switch (mOpType) {
            case DOWNLOAD_GROUP_LIST:
                parseGroupList(response);
                break;
            case DOWNLOAD_GROUP_INFO:
                parseGroupInfo(response);
                break;
            case DOWNLOAD_GROUP_ACTIVITY_LIST:
                try {
                    JSONObject object = new JSONObject(response);
                    List<Bulletin> bulletinList = NewsfeedOp.parseBulletinList(object.getJSONArray(KEY_DATA));

                    mGroupActivitiesNextUrl = parseNextUrl(object);
                    EventBus.getDefault().post(
                            new DownloadTempListEvent<Bulletin>(
                                    true, mOpType,
                                    new TemporaryDataAccess<Bulletin>(bulletinList, shouldClearData()),
                                    null
                            )
                    );
                } catch (JSONException e) {
                    EventBus.getDefault().post(new DownloadTempListEvent<Bulletin>(
                            false, mOpType, null, null
                    ));
                    Log.e(TAG, "Failed to parse bulletin list.", e);
                }
            default:
                break;
        }
    }

    /**
     * Helper method for parsing group information.
     *
     * @param response response from server.
     */
    private void parseGroupInfo(String response) {
        try {
            JSONObject object = new JSONObject(response).getJSONObject(KEY_DATA);
            this.mGroup = new Group().parse(object);
            // Notify
            EventBus.getDefault().post(new DownloadEvent<Group>(true, mGroup, OperationType.DOWNLOAD_GROUP_INFO));
        } catch (JSONException e) {
            EventBus.getDefault().post(new DownloadEvent(false, OperationType.DOWNLOAD_GROUP_INFO));

            Log.e(TAG, "Failed to parse group item.", e);
        }
    }

    /**
     * Method for parsing list of group from server response.
     *
     * @param response response string received from server.
     */
    private void parseGroupList(String response) {
        try {
            JSONObject object = new JSONObject(response);
            mGroupListNextUrl = parseNextUrl(object);
            JSONArray groupArray = object.getJSONArray(KEY_DATA);

            // Parsing
            int size = groupArray.length();
            List<Group> groupList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Group group = new Group().parse(groupArray.getJSONObject(i));
                groupList.add(group);
            }

            // Save
            Context c = mContextRef.get();
            if (c != null) {
                DataAccess da = DataAccess.getInstance(c);
                if (shouldClearData()) {
                    da.setGroups(groupList);
                    dataCleared();
                } else {
                    da.addGroups(groupList);
                }
            }

            // Notify UI
            mEventBus.post(new DownloadEvent(true, OperationType.DOWNLOAD_GROUP_LIST));
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            // Notify UI
            mEventBus.post(new DownloadEvent(false, OperationType.DOWNLOAD_GROUP_LIST));
        }
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
                return;
            }
            case FOLLOW_GROUP: {
                mUrl = UrlData.buildFollowGroupUrl(this.mGroup._id);
                mMethod = Request.Method.PUT;
                break;
            }
            case UNFOLLOW_GROUP: {
                mUrl = UrlData.buildFollowGroupUrl(this.mGroup._id);
                mMethod = Request.Method.DELETE;
                break;
            }
            default:
                return;
        }

        createNewUploadReq(mUrl, mMethod);
        startUpload();
    }

    /**
     * method for uploading new group to the server.
     */
    private void uploadGroup() {
        // Build request
        RequestBody requestBody;
        try {
            if (picFilePath != null) {
                String path = picFilePath.toString();
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("json_data", null,
                                RequestBody.create(MediaType.parse("application/json"), buildJSONData().toString()))
                        .addFormDataPart("group_picture", path.substring(path.lastIndexOf("/") + 1),
                                RequestBody.create(MediaType.parse("image/jpeg"),
                                        ImageCompress.compressImageToFile(picFilePath, mContextRef.get()))).build();

            } else {
                requestBody = RequestBody.create(MediaType.parse("application/json"), buildJSONData().toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to prepare group data for upload.");
            return;
        }

        // Create network request
        final OkHttpClient client = new OkHttpClient();
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .header(UrlData.PARAM_AUTH_TYPE, "Bearer " + MotherActivity.access_token)
                .url(mUrl)
                .post(requestBody)
                .build();

        try {
            // Send request
            Response response = client.newCall(request).execute();
            // Get context
            Context c = mContextRef.get();
            if (c == null) return;
            // Process response
            if (response.isSuccessful()) {
                String re = response.body().string();
                // Parse
                this.mGroup.parse(new JSONObject(re).getJSONObject(KEY_DATA));
                // Notify UI

            } else {
                Log.e(TAG, "Upload failed: " + response.message() + " " + response.body().string());
                // Notify UI
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse response.");
        }


    }

    /**
     * Helper method for building json object to send as body of a network request.
     *
     * @return newly built {@link JSONObject} object.
     * @throws JSONException
     */
    private JSONObject buildJSONData() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(GROUP_NAME_FIELD, this.mGroup.getGroupName());
        object.put(DESCR_FIELD, this.mGroup.getDescription());
        return object;
    }

    @Override
    protected void onUploadSuccess(String response) {

    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    @Override
    protected void clearNextUrl() {
        if (mOpType.equals(OperationType.DOWNLOAD_GROUP_ACTIVITY_LIST))
            mGroupActivitiesNextUrl = "";
        else if (mOpType.equals(OperationType.DOWNLOAD_GROUP_LIST))
            mGroupListNextUrl = "";
    }

    public void setPicFilePath(File picFilePath) {
        this.picFilePath = picFilePath;
    }

    public void setUserId(long userId) {
        this.mUserId = userId;
    }

    @Override
    protected String getTag() {
        return TAG;
    }


}

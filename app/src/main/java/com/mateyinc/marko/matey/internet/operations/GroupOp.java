package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Group;
import com.mateyinc.marko.matey.utils.ImageCompress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

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

    private Group group;
    private File picFilePath;

    public GroupOp(Context context) {
        super(context);
    }

    public GroupOp(Context context, Group group) {
        super(context);
        this.group = group;
    }

    @Override
    public void startDownloadAction() {

    }

    @Override
    protected void onDownloadSuccess(String response) {

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
                this.group.parse(new JSONObject(re).getJSONObject(KEY_DATA));
                // Notify UI
                if (mUploadListener != null)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUploadListener.onUploadSuccess();
                        }
                    });
            } else {
                Log.e(TAG, "Upload failed: " + response.message() + " " + response.body().string());
                if (mUploadListener != null)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUploadListener.onUploadFailed();
                        }
                    });
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse response.");
        }


    }

    private JSONObject buildJSONData() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(GROUP_NAME_FIELD, this.group.getGroupName());
        object.put(DESCR_FIELD, this.group.getDescription());
        return object;
    }

    @Override
    protected void onUploadSuccess(String response) {

    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    public void setPicFilePath(File picFilePath) {
        this.picFilePath = picFilePath;
    }

    @Override
    protected String getTag() {
        return TAG;
    }

}

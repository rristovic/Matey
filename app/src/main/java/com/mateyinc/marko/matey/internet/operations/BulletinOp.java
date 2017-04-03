package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.TemporaryDataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.events.DownloadTempListEvent;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.utils.ImageCompress;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mateyinc.marko.matey.model.Bulletin.KEY_REPLIES;

public class BulletinOp extends Operations {
    private static final String TAG = BulletinOp.class.getSimpleName();
    /**
     * Required json data field - title
     **/
    private static final String TITLE_FIELD_NAME = "title";
    /**
     * Json data field - group
     **/
    private static final String GROUP_ID_FIELD_NAME = "group_id";


    final Bulletin bulletin;
    // For group bulletin upload
    private Long mGroupId = null;

    public BulletinOp(Context context, Bulletin bulletin) {
        super(context);
        this.bulletin = bulletin;
    }

    @Override
    public void startDownloadAction() {
        switch (mOpType) {
            case DOWNLOAD_BULLETIN: {
                mUrl = UrlData.buildGetBulletinUrl(bulletin.getId());
                break;
            }

            default: {
                Log.e(TAG, "No operation type has been specified!");
                mUrl = "#";
            }
        }

        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        try {
            JSONObject object = new JSONObject(response);
            Bulletin b = new Bulletin();
//            UserProfile profile = new UserProfile().parse(
//                    object.getJSONObject(KEY_DATA).getJSONObject(Bulletin.KEY_USER_PROFILE));
//            b.setUserProfile(profile);
            b.parse(object.getJSONObject(KEY_DATA));
            UserProfile profile = b.getUserProfile();

            ArrayList<Reply> mReplyList = new ArrayList<>(b.getNumOfReplies());
            // Try parsing reply list if it is present
            if (b.getNumOfReplies() > 0)
                try {
                    JSONObject replies = object.getJSONObject(KEY_DATA).getJSONObject(KEY_REPLIES);
                    JSONArray repliesList = replies.getJSONArray(Operations.KEY_DATA);
                    int size = repliesList.length();
                    for (int i = 0; i < size; i++) {
                        Reply reply = new Reply().parse(repliesList.getJSONObject(i));
                        reply.setUserProfile(b.getUserProfile());
                        mReplyList.add(reply);
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "No reply list for replies count = " + b.getNumOfReplies());
                }
            DataAccess.getInstance(mContextRef.get()).addBulletin(b);
            DataAccess.getInstance(mContextRef.get()).addUserProfile(profile);

            //Notify UI
            EventBus.getDefault().post(new DownloadTempListEvent<Reply>(
                    true,
                    mOpType,
                    new TemporaryDataAccess<Reply>(mReplyList, shouldClearData()),
                    b));

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadListener.onDownloadSuccess();
//                }
//            });
        } catch (JSONException e) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadListener.onDownloadFailed();
//                }
//            });
            Log.e(TAG, "Failed to parse bulletin. " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mDownloadListener.onDownloadFailed();
//            }
//        });
    }

    @Override
    public void startUploadAction() {
        String url;
        int method;

        switch (mOpType) {
            default:
            case POST_NEW_BULLETIN_WITH_ATTCH: {
                mUrl = UrlData.POST_NEW_BULLETIN;
                // Using OkHTTP for sending files
                uploadFile();
            }
            case POST_NEW_GROUP_BULLETIN: {
                mUrl = UrlData.POST_NEW_BULLETIN;
                // Using OkHTTP for sending files
                uploadFile();
            }
        }

//        createNewUploadReq(url, method);
//        startUploadAction();
    }

    public void uploadFile() {
//        String[] parts = selectedFilePath.split("/");
//        final String fileName = parts[parts.length - 1];

        if (mContextRef.get() != null)
            notifyUI(R.string.upload_started);


        List<String> mFilePaths = bulletin.getAttachments();
        List<String> mMarkers = new ArrayList<>();
        LinkedList<File> files = new LinkedList<>();

        // Create list of valid files
        Iterator i = mFilePaths.iterator();
        while (i.hasNext()) {
            // Checking to see if file paths are valid, if not just dismiss them
            String s = (String) i.next();
            File selectedFile = new File(s);
            if (!selectedFile.isFile()) {
//                Toast.makeText(mContextRef.get(), "Source file doesn't exits: " + selectedFile, Toast.LENGTH_LONG).show();
                mMarkers.add(s);
            } else
                files.add(new File(s));
        }

        // Create first part for multipart that contains text
        JSONObject jsonObject = new JSONObject();
        try {
            // Add title
            jsonObject.put(TITLE_FIELD_NAME, bulletin.getSubject());
            // Add details
            String message = bulletin.getMessage();
            if (!message.isEmpty()) // Is there is no message, send just the subject
                jsonObject.put(CONTENT_FIELD_NAME, bulletin.getMessage());
            // Add locations
            if (mMarkers.size() != 0) {
                JSONArray array = new JSONArray();
                for (String s :
                        mMarkers) {
                    array.put(s);
                }
                jsonObject.put(LOCATIONS_FIELD_NAME, array);
            }
            // Add group id if present
            if (mGroupId != null)
                jsonObject.put(GROUP_ID_FIELD_NAME, mGroupId);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create data for bulletin upload.", e);
        }

        // Create builder and add first file
        final OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("json_data", null,
                        RequestBody.create(MediaType.parse("application/json"), jsonObject.toString()));

        // Add attachments
        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            // Substring last segment and get file name
            String fileName = file.toString().substring(
                    file.toString().lastIndexOf("/") + 1);

            requestBodyBuilder.addFormDataPart(file.toString(), fileName,
                    RequestBody.create(MediaType.parse("image/jpeg"),
                            ImageCompress.compressImageToFile(file, mContextRef.get())));
        }

        // Create network request
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .header(UrlData.PARAM_AUTH_TYPE, "Bearer " + MotherActivity.access_token)
                .url(mUrl)
                .post(requestBodyBuilder.build())
                .build();

        try {
            // Notify user
            Context c = mContextRef.get();

            Response response = client.newCall(request).execute();
            if (c != null) {
                if (!response.isSuccessful()) {
                    String s = response.body().string();
                    Log.e(TAG, "Upload failed: " + s);
                    bulletin.onUploadFailed(s, c);
                    notifyUI(R.string.upload_failed);
                } else {
                    String s = response.body().string();
                    Log.d(TAG, "Upload success: " + s);
                    bulletin.onUploadSuccess(s, c);
                    notifyUI(R.string.upload_success);

                }
                response.body().close();
            }
            // TODO - finish response parsing

            System.out.println(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void onUploadSuccess(final String response) {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    protected void onUploadFailed(final VolleyError error) {
        throw new RuntimeException("Not implemented.");
    }


    public void setGroupId(long groupId){
        this.mGroupId = groupId;
    }

    @Override
    protected void clearNextUrl() {
    }

    @Override
    protected String getTag() {
        return BulletinOp.class.getSimpleName();
    }

}

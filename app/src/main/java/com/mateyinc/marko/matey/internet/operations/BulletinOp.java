package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.utils.ImageCompress;

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

public class BulletinOp extends Operations {
    private static final String TAG = BulletinOp.class.getSimpleName();
    /**
     * Required json data field - title
     **/
    private static final String TITLE_FIELD_NAME = "title";


    final Bulletin bulletin;

    public BulletinOp(Context context, Bulletin bulletin) {
        super(context);
        this.bulletin = bulletin;
    }

    @Override
    public void startDownloadAction() {
        String url;

        switch (mOpType) {
            case DOWNLOAD_BULLETIN: {
                url = UrlData.buildGetBulletinUrl(bulletin.getId());
                break;
            }

            default: {
                Log.e(TAG, "No operation type has been specified!");
                url = "#";
            }
        }

        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {
        try {
            JSONObject object = new JSONObject(response);
            Bulletin b = new Bulletin();
            UserProfile profile = new UserProfile().parse(
                    object.getJSONObject(KEY_DATA).getJSONObject(Bulletin.KEY_USER_PROFILE));
            b.setUserProfile(profile);
            b.parse(object.getJSONObject(KEY_DATA));
            DataAccess.getInstance(mContextRef.get()).addBulletin(b);
            DataAccess.getInstance(mContextRef.get()).addUserProfile(profile);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onDownloadSuccess();
                }
            });
        } catch (JSONException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onDownloadFailed();
                }
            });
            Log.e(TAG, "Failed to parse bulletin. " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadListener.onDownloadFailed();
            }
        });
    }

    @Override
    public void startUploadAction() {
        String url;
        int method;

        switch (mOpType) {
            default:
            case POST_NEW_BULLETIN_WITH_ATTCH: {
                // Using OkHTTP for sending files
                uploadFile();
            }
        }

//        createNewUploadReq(url, method);
//        startUploadAction();
    }

    @Override
    protected void onUploadSuccess(final String response) {
        final Context c = mContextRef.get();

        if (c != null)
            bulletin.onUploadSuccess(response, c);

    }

    @Override
    protected void onUploadFailed(final VolleyError error) {
        final Context c = mContextRef.get();

        if (c != null)
            bulletin.onUploadFailed(error.toString(), c);
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
            jsonObject.put(TITLE_FIELD_NAME, bulletin.getSubject());
            String message = bulletin.getMessage();
            if (!message.isEmpty()) // Is there is no message, send just the subject
                jsonObject.put(CONTENT_FIELD_NAME, bulletin.getMessage());

            if (mMarkers.size() != 0) { // Add locations
                JSONArray array = new JSONArray();
                for (String s :
                        mMarkers) {
                    array.put(s);
                }
                jsonObject.put(LOCATIONS_FIELD_NAME, array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                .url(UrlData.POST_NEW_BULLETIN)
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
            }
            // TODO - finish response parsing

            System.out.println(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

    }

    @Override
    protected String getTag() {
        return BulletinOp.class.getSimpleName();
    }

}

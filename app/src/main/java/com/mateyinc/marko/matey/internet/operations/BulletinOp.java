package com.mateyinc.marko.matey.internet.operations;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.utils.ImageCompress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
     * Field name for multi part post request body for json data
     **/
    private static final String JSON_DATA_FIELD_NAME = "json_data";
    /**
     * Required json data field - title
     **/
    private static final String TITLE_FIELD_NAME = "title";
    /**
     * Content json data field - post message
     **/
    private static final String CONTENT_FIELD_NAME = "text";


    Bulletin bulletin;

    public BulletinOp(Context context, Bulletin bulletin) {
        super(context);
        this.bulletin = bulletin;
    }

    @Override
    public void startDownloadAction() {
        String url;

        switch (mOpType) {

            default: {
                Log.e(TAG, "No operation type has been specified!");
                url = "#";
            }
        }

        createNewDownloadReq(url);
        startDownload();
    }

    @Override
    protected void onDownloadSuccess(String response) {

    }

    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public void startUploadAction() {
        String url;
        int method;

        switch (mOpType) {
            case POST_NEW_BULLETIN_NO_ATTCH: {
                url = "#";
                method = Request.Method.POST;
                break;
            }
            case POST_NEW_BULLETIN_WITH_ATTCH: {
                // Using OkHTTP for sending files

                submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        uploadFile();
                    }
                });
                return;
            }
            default: {
                Log.e(TAG, "No operation type has been specified!");
                url = "#";
                method = Request.Method.POST;
            }
        }

        createNewUploadReq(url, method);
        startUploadAction();
    }

    @Override
    protected void onUploadSuccess(final String response) {
        final Context c = mContextRef.get();

        if (c != null)
            submitRunnable(new Runnable() {
                @Override
                public void run() {
                    bulletin.onUploadSuccess(response, c);
                }
            });
    }

    @Override
    protected void onUploadFailed(VolleyError error) {

    }

    @Override
    protected String getTag() {
        return null;
    }

    public void uploadFile() {
//        String[] parts = selectedFilePath.split("/");
//        final String fileName = parts[parts.length - 1];

        if (mContextRef.get() != null)
            notifyUI(R.string.upload_started);


        List<String> mFilePaths = bulletin.getAttachments();
        LinkedList<File> files = new LinkedList<>();

        // Create list of valid files
        Iterator i = mFilePaths.iterator();
        while (i.hasNext()) {
            // Checking to see if file paths are valid, if not just dismiss them
            String s = (String) i.next();
            File selectedFile = new File(s);
            if (!selectedFile.isFile()) {
//                Toast.makeText(mContextRef.get(), "Source file doesn't exits: " + selectedFile, Toast.LENGTH_LONG).show();
                i.remove();
            } else
                files.add(new File(s));
        }

        // Create first part for multipart that contains text
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(TITLE_FIELD_NAME, bulletin.getSubject());
            jsonObject.put(CONTENT_FIELD_NAME, bulletin.getMessage());
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
                    bulletin.onUploadFailed(response.body().toString(), c);
                    notifyUI(R.string.upload_failed);
                } else {
                    bulletin.onUploadSuccess(response.body().toString(), c);
                    notifyUI(R.string.upload_success);

                }
            }
            // TODO - finish response parsing

            System.out.println(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

    }
}

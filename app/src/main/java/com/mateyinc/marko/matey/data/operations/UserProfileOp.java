package com.mateyinc.marko.matey.data.operations;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * User profile downloading and uploading operations, also updates the db;
 */
public class UserProfileOp extends Operations {
    private static final String TAG = UserProfileOp.class.getSimpleName();

    // Json keys in response from the server TODO - finish keys
    private static final String KEY_ID = "";
    private static final String KEY_FIRST_NAME = "";
    private static final String KEY_LAST_NAME = "";
    private static final String KEY_PIC_LINK = "";
    private static final String KEY_EMAIL = "";

    public UserProfileOp(OperationProvider provider, MotherActivity context) {
        super(provider, context);
    }

    @Override
    public void download(long id) {
        String url = "";
        createDownloadReq(url);
        startDownload();
    }

    @Override
    public void onDownloadSuccess(final String response) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                saveToDb(response);
            }
        };
        mProvider.submitRunnable(r);
    }

    @Override
    public void onDownloadFailed(VolleyError error) {

    }

    @Override
    public <T> void upload(T object) {

    }

    @Override
    public <T> void onUploadSuccess(T object) {

    }

    @Override
    public <T> void onUploadFailed(T object) {

    }


    void saveToDb(String response) {
        try {
            ContentValues userValues;
            JSONObject object;
            // TODO - finish download multiple users
            object = new JSONObject(response);

            // Parsing
            Long id = object.getLong(KEY_ID);
            String name = object.getString(KEY_FIRST_NAME);
            String lastName = object.getString(KEY_LAST_NAME);
            String email = object.getString(KEY_EMAIL);
            String picLink = object.getString(KEY_PIC_LINK);

            userValues = new ContentValues();
            userValues.put(DataContract.ProfileEntry._ID, id);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, name);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
            userValues.put(DataContract.ProfileEntry.COLUMN_PICTURE, picLink);
//            userValues.put(DataContract.ProfileEntry.COLUMN_IS_FRIEND, mIsFriend);
//            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);


            // Add to db
            Uri insertedUri = mContextRef.get().getContentResolver().insert(
                    DataContract.ProfileEntry.CONTENT_URI,
                    userValues);

            // Debug
            String debugString = getString(id, name, lastName, email, picLink);
            if (insertedUri == null) {
                Log.e(TAG, "Error inserting " + debugString);
            } else {
                Log.d(TAG, "New profile added: " + debugString);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    public String getString(long id, String name, String lastName, String email, String picLink){
        return String.format(Locale.US,"UserProfile: ID=%d; UserName:%s %s; Email:%s; PicLink:%s", id, name, lastName, email, picLink);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}


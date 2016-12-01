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
    private static final String KEY_ID = "user_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PIC = "picture_url";
    private static final String KEY_COVER_PIC = "picture_url";
    private static final String KEY_FOLLOWERS_NUM = "num_of_followers";
    private static final String KEY_FOLLOWING_NUM = "num_of_following";
    private static final String KEY_VERIFIED = "verified";
    private static final String KEY_FIRST_LOGIN = "first_login";

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
            String fullName = object.getString(KEY_FULL_NAME);
            String email = object.getString(KEY_EMAIL);
            String picLink = object.getString(KEY_PROFILE_PIC);
            String coverLink = object.getString(KEY_COVER_PIC);
            int followersNum = object.getInt(KEY_FOLLOWERS_NUM);
            int followingNum = object.getInt(KEY_FOLLOWING_NUM);
            boolean verified = object.getBoolean(KEY_VERIFIED);
            boolean firstLogin = object.getBoolean(KEY_FIRST_LOGIN);

            userValues = new ContentValues();
            userValues.put(DataContract.ProfileEntry._ID, id);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, name);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, lastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_FULL_NAME, fullName);
            userValues.put(DataContract.ProfileEntry.COLUMN_EMAIL, email);
            userValues.put(DataContract.ProfileEntry.COLUMN_PROF_PIC, picLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_COVER_PIC, coverLink);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWERS_NUM, followersNum);
            userValues.put(DataContract.ProfileEntry.COLUMN_FOLLOWING_NUM, followingNum);

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


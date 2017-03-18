package com.mateyinc.marko.matey.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.MBaseColumns;
import com.mateyinc.marko.matey.data.ServerStatus;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class MModel {

    // Keys for JSON data from the server
    static final String KEY_ID = "activity_id";
    static final String KEY_USER_ID = "user_id";
    static final String KEY_SOURCE_ID = "source_id";
    static final String KEY_PARENT_ID = "parent_id";
    static final String KEY_PARENT_TYPE = "parent_type";
    static final String KEY_ACTIVITY_TYPE = "activity_type";
    static final String KEY_DATE_ADDED = "time_c";
    static final String KEY_FIRSTNAME = "first_name";
    static final String KEY_LASTNAME = "last_name";
    static final String KEY_PROFILE_PIC = "profile_picture";

    protected ServerStatus mServerStatus = ServerStatus.STATUS_UPLOADING;
    public long _id = -1;
    public Uri mUri;

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public ServerStatus getServerStatus() {
        return mServerStatus;
    }

    public void setServerStatus(ServerStatus status) {
        mServerStatus = status;
    }

    public void setServerStatus(int status) {
        mServerStatus = ServerStatus.fromInteger(status);
    }


    public void updateServerStatus(Context context) {
        ContentValues values = new ContentValues(2);
        values.put(MBaseColumns.COLUMN_SERVER_STATUS, mServerStatus.ordinal());
        values.put(MBaseColumns._ID, _id);

        int rows = context.getContentResolver().update(mUri, values,
                MBaseColumns._ID + " = ?", new String[]{Long.toString(_id)});
        if (rows != 1)
            Log.d("MMODEL", String.format("Failed to update server status at uri %s, with model id %d.", mUri.toString(), _id));
        else
            Log.e("MMODEL", String.format("Updated server status at uri %s, model id %d.", mUri.toString(), _id));
    }

    public abstract void onDownloadSuccess(String response, Context c);

    public abstract void onDownloadFailed(String error, Context c);

    public abstract void onUploadSuccess(String response, Context c);

    public abstract void onUploadFailed(String error, Context c);


    abstract public MModel parse(JSONObject object) throws JSONException;

}


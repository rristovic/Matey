package com.mateyinc.marko.matey.model;

import android.content.Context;

import com.android.volley.VolleyError;

public abstract class MModel {

    // Keys for JSON data from the server
    static final String KEY_ID = "activity_id";
    static final String KEY_USER_ID = "user_id";
    static final String KEY_SOURCE_ID = "source_id";
    static final String KEY_PARENT_ID = "parent_id";
    static final String KEY_PARENT_TYPE = "parent_type";
    static final String KEY_ACTIVITY_TYPE = "activity_type";
    static final String KEY_DATE_ADDED = "activity_time";
    static final String KEY_FIRSTNAME = "first_name";
    static final String KEY_LASTNAME = "last_name";
    static final String KEY_PROFILE_PIC = "profile_picture";

    protected int mServerStatus = 0;
    public long _id;

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public int getServerStatus() {
        return mServerStatus;
    }

    public void setServerStatus(int status) {
        mServerStatus = status;
    }


    public abstract void onDownloadSuccess(String response, Context c);

    public abstract void onDownloadFailed(VolleyError error, Context c);

    public abstract void onUploadSuccess(String response, Context c);

    public abstract void onUploadFailed(VolleyError error, Context c);

    protected abstract void notifyDataChanged(Context context);

    /** Saves model in database and start upload sequence. **/
    abstract void save(Context context);

    protected abstract void addToDb(Context context);
    protected abstract void removeFromDb(Context context);

}


package com.mateyinc.marko.matey.model;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.mateyinc.marko.matey.data.updater.ApproveUpdater;
import com.mateyinc.marko.matey.data.updater.BulletinUpdater;
import com.mateyinc.marko.matey.data.updater.DataUpdater;
import com.mateyinc.marko.matey.data.updater.ReplyUpdater;

import java.util.Date;

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

    /**
     * Helper method for updating this model server status in db;
     * <br> Only calls the chain of updaters;
     * @see DataUpdater#updateServerStatus(int, long, Context)
     * @param serverStatus status of the model
     * @param classLevel class level: {@link DataUpdater#BULLETIN_CLASS}, {@link DataUpdater#APPROVE_CLASS}, {@link DataUpdater#REPLY_CLASS};
     * @param context context used for db control
     */
    void updateServerStatus(int serverStatus, int classLevel, Context context){
        DataUpdater updaters = getChainOfUpdaters();
        updaters.updateServerStatus(classLevel, serverStatus, _id, context);
    }

    /**
     * Helper method for updating this model id with the new one and updating the model date created
     * <br> Only calls the chain of updaters;
     * @param oldId old id of the model
     * @param newId new id of the model
     * @param date the date created retrieved from the server
     * @param classLevel class level: {@link DataUpdater#BULLETIN_CLASS}, {@link DataUpdater#APPROVE_CLASS}, {@link DataUpdater#REPLY_CLASS};
     * @param context context used for db control
     * @see DataUpdater#updateIdAndDate(long, long, Date, Context) (int, long, Context)
     */
    void updateIdAndDate(long oldId, long newId, Date date, int classLevel, Context context){
        DataUpdater updaters = getChainOfUpdaters();
        updaters.updateIdAndDate(classLevel, oldId, newId, date, context);
    }

    /**
     * Helper method for creating chain of updaters
     * @return first DataUpdater in chain of updaters which is {@link BulletinUpdater}
     */
    private static DataUpdater getChainOfUpdaters(){
        DataUpdater bulletinUpdater = new BulletinUpdater(DataUpdater.BULLETIN_CLASS);
        DataUpdater replyUpdater = new ReplyUpdater(DataUpdater.REPLY_CLASS);
        DataUpdater approveUpdater = new ApproveUpdater(DataUpdater.APPROVE_CLASS);

        bulletinUpdater.setNextUpdater(replyUpdater);
        replyUpdater.setNextUpdater(approveUpdater);

        return bulletinUpdater;
    }

    /**
     * Method for uploading activity's data to the server and caching it in database
     * @param context context used for db control
     * @param queue {@link RequestQueue} used for network control
     * @param accessToken access token string used for server authorisation
     */
    abstract public void upload(Context context, RequestQueue queue, String accessToken);

    /**
     * Method to call when startUploadAction request has been completed
     * @param response String response retrieved from the server
     * @param context context used for db control
     */
    abstract protected void uploadSucceeded(String response, Context context);

    /**
     * Method to call when startUploadAction request has failed
     * @param e error message retrieved from the server
     * @param context context used for db control
     */
    abstract protected void uploadFailed(Exception e, Context context);

    /**
     * Method to call when data needs to be added to the database
     * @param c {@link Context} used for database control
     */
    abstract public void addToDb(Context c);

    /**
     * Method for downloading activity's data from the server and caching it in database
     * @param context context used for db control
     * @param queue {@link RequestQueue} used for network control
     * @param accessToken access token string used for server authorisation
     */
    abstract public void download(Context context, RequestQueue queue, String accessToken);
}


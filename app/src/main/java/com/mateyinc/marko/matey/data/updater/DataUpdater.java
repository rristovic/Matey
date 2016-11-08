package com.mateyinc.marko.matey.data.updater;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataManager;

import java.util.Date;

public abstract class DataUpdater {
    private static final String TAG = DataUpdater.class.getSimpleName();

    public static final int BULLETIN_CLASS = 3;
    public static final int REPLY_CLASS = 2;
    public static final int APPROVE_CLASS = 1;

    /**
     * Used for chain of responsibility control;
     * Indicates what updater should process the request
     */
    int level;

    // next element in chain or responsibility
    private DataUpdater nextUpdater;

    public void setNextUpdater(DataUpdater nextUpdater){
        this.nextUpdater = nextUpdater;
    }

    public void updateServerStatus(int classLevel, int serverStatus, long id, Context context){
        if(this.level <= classLevel){
            updateServerStatus(serverStatus, id, context);
            return;
        }
        if(nextUpdater != null){
            nextUpdater.updateServerStatus(classLevel, serverStatus, id, context);
        }
    }

    public void updateIdAndDate(int level, long oldId, long newId, Date date, Context context){
        if(this.level <= level){
            updateIdAndDate(oldId, newId, date, context);
        }
        if(nextUpdater != null){
            nextUpdater.updateIdAndDate(level, oldId, newId, date, context);
        }
    }

    /**
     * Method for updating activity server status in database;
     * <br> NOTE: Can be set to {@link com.mateyinc.marko.matey.data.DataManager.ServerStatus#STATUS_SUCCESS},
     * {@link com.mateyinc.marko.matey.data.DataManager.ServerStatus#STATUS_RETRY_UPLOAD}, {@link com.mateyinc.marko.matey.data.DataManager.ServerStatus#STATUS_UPLOADING}
     * @param serverStatus server status int constant
     * @param id the id of the activity to be updated
     * @param context context used for db control
     */
    abstract void updateServerStatus(int serverStatus, long id, Context context);

    /**
     * Method for updating the activity id in the database when retrieved from the server;
     * <br> NOTE: also updates the server status to {@link com.mateyinc.marko.matey.data.DataManager.ServerStatus#STATUS_SUCCESS}
     * @param oldId old id of the activity to be updated
     * @param newId new id of the activity to replace the old one
     * @param date new date retrieved from the server
     * @param context context used for db control
     */
    abstract void updateIdAndDate(long oldId, long newId, Date date, Context context);

    void addNotUploadedActivity(long id, int objectType, Context context) {
        ContentValues values = new ContentValues(2);
        values.put(DataContract.NotUploadedEntry._ID, id);
        values.put(DataContract.NotUploadedEntry.COLUMN_ENTRY_TYPE, objectType);
        Uri uri;
        uri = context.getContentResolver().insert(DataContract.NotUploadedEntry.CONTENT_URI, values);
        String objectTypeName;
        switch (objectType){
            case BULLETIN_CLASS:{
                objectTypeName = "BULLETIN CLASS";
                break;
            }
            case APPROVE_CLASS:{
                objectTypeName = "APPROVE CLASS";
                break;
            }
            case REPLY_CLASS:{
                objectTypeName = "REPLY CLASS";
                break;
            }
            default:{
                objectTypeName = "";
                break;
            }
        }
        if (null == uri) {
            Log.e(TAG, "Failed to insert object in NotUploaded table with id=" + id + "; object type:" + objectTypeName);
        } else {
            Log.d(TAG, "Object inserted in NotUploaded table with id=" + id + "; object type:" + objectTypeName);
        }
    }

    /**
     * Method for debugging;
     * @param serverStatus server status code
     * @return the server status name based on status code
     */
    String getServerStatusName(int serverStatus){
        switch (serverStatus){
            case DataManager.ServerStatus.STATUS_RETRY_UPLOAD:{
                return "STATUS RETRY UPLOAD";
            }
            case DataManager.ServerStatus.STATUS_SUCCESS:{
                return "STATUS SUCCESS";
            }
            case DataManager.ServerStatus.STATUS_UPLOADING:{
                return "STATUS UPLOADING";
            }
            default: return "";
        }
    }
}

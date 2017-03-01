package com.mateyinc.marko.matey.data.operations;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.data.internet.MateyRequest;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.lang.ref.WeakReference;

/**
 * An operation class which define actions that can be performed on defined data models
 */
public abstract class Operations {

    private Response.Listener<String> successListener;
    private Response.ErrorListener failedListener;

    /** Indicates what kind of operations this is, used for general db control in {@link #addNotUploadedActivity(long)} */
    protected OperationType mOpType;

    /** Activity startUploadAction status that is saved in database, and used for UI control */
    public enum ServerStatus {
        /** Indicates that the data startUploadAction to the server has failed */
        STATUS_RETRY_UPLOAD,
        /** Indicates that the data is currently being uploaded to the server */
        STATUS_UPLOADING,
        /** Indicates that data is successfully uploaded to the server */
        STATUS_SUCCESS
    }

    // Network request used for networking
    protected MateyRequest mRequest;
    // Used for db control and to check if the provided context still exists
    protected WeakReference<MotherActivity> mContextRef;
    // Used for networking and threading
    private OperationProvider mProvider;

    Operations(MotherActivity context){
        mOpType = OperationType.NO_OPERATION;
        this.mProvider = OperationManager.getInstance(context);
        mContextRef = new WeakReference<MotherActivity>(context);
    }

    Operations(MotherActivity context, OperationType operationType){
        this.mOpType = operationType;
        this.mProvider = OperationManager.getInstance(context);
        mContextRef = new WeakReference<MotherActivity>(context);
    }

    Operations(OperationProvider provider, MotherActivity context, OperationType operationType){
        this.mOpType = operationType;
        this.mProvider = provider;
        mContextRef = new WeakReference<MotherActivity>(context);
    }

    public void addSuccessListener(Response.Listener<String> listener){
        successListener = listener;
    }
    public void addFailedListener(Response.ErrorListener listener){
        failedListener = listener;
    }

    /**
     * Method for setting the type of operation that will be performed.
     * @param operationType {@link OperationType} type of the operation
     */
    public void setOperationType(OperationType operationType) {
        mOpType = operationType;
    }

    // Download methods
    public abstract void startDownloadAction();
    protected abstract void
    onDownloadSuccess(String response);
    protected abstract void onDownloadFailed(VolleyError error);
    // Upload methods
    public abstract void startUploadAction();
    protected void onUploadSuccess(String response){
        Log.d(getTag(), "Upload has succeed.");
    };
    protected void onUploadFailed(VolleyError error){
        Log.d(getTag(), "Upload has failed.");
    };

    /**
     * Method for creating new network request with provided parameters
     * @param url url to startDownloadAction from
     */
    void createNewDownloadReq(final String url){
        // Creating new request
         mRequest = new MateyRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (successListener != null) {
                            successListener.onResponse(response);
                            Log.d(getTag(), "Download has succeed.");
                            return;
                        }

                        MotherActivity c = mContextRef.get();
                        if (c != null){
                            Log.d(getTag(), "Download has succeed.");
                            onDownloadSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (failedListener != null) {
                            failedListener.onErrorResponse(error);
                            Log.e(getTag(), "Download has failed: " + error.getLocalizedMessage(), error);
                            return;
                        }

                        MotherActivity c = mContextRef.get();
                        if (c != null){
                            Log.e(getTag(), "Download has failed: " + error.getLocalizedMessage(), error);
                            onDownloadFailed(error);
                        }
                    }
                });
    }

    /**
     * Method for creating new network request with provided parameters
     * @param url url to startUploadAction to
     * @param method http method used for startUploadAction{@link com.android.volley.Request.Method}
     */
    void createNewUploadReq(final String url, int method){
        mRequest = new MateyRequest(method, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (successListener != null)
                            successListener.onResponse(response);

                        MotherActivity c = mContextRef.get();
                        if (c != null) {
                            Log.d(getTag(), "Upload has succeed.");
                            onUploadSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (failedListener != null)
                            failedListener.onErrorResponse(error);

                        MotherActivity c = mContextRef.get();
                        if (c != null) {
                            Log.e(getTag(), "Upload has failed: " + error.getLocalizedMessage(), error);
                            onUploadFailed(error);
                        }
                    }
                });
    }


    /**
     * Helper method for starting newly created request with {@link #createNewDownloadReq(String)};
     * Must be called to initiate the startDownloadAction process;
     * Sets the authorisation header with {@link OperationProvider#getAccessToken()};
     */
    void startDownload(){
        setDefaultAuthHeader();
        // Starting the startDownloadAction
        mProvider.submitRequest(mRequest);
    }

    /**
     * Helper method for starting newly create request;
     * Must be called to initiate the startUploadAction process;
     * Sets the default authorisation header with {@link OperationProvider#getAccessToken()}
     */
    void startUpload(){
        setDefaultAuthHeader();
         // Starting the startDownloadAction
        mProvider.submitRequest(mRequest);
    }

    /**
     * Helper method for settings the default authorisation header by {@link MateyRequest#setAuthHeader(String)};
     */
    void setDefaultAuthHeader(){
        mRequest.setAuthHeader(mProvider.getAccessToken());
    }

    /**
     * Helper method for adding params to the request; Only used with POST method
     * @param key parameter key
     * @param value parameter value
     */
    protected void addParam(String key, String value){
        mRequest.addParam(key, value);
    }

    /**
     * Method for updating {@link com.mateyinc.marko.matey.data.MBaseColumns#COLUMN_SERVER_STATUS} in db.
     * @param id id of the activity/data.
     * @param serverStatus the {@link ServerStatus} to startUploadAction.
     */
    protected void updateServerStatus(long id, ServerStatus serverStatus){
        // Add to not uploaded table if needed
        if(serverStatus == ServerStatus.STATUS_RETRY_UPLOAD){
            addNotUploadedActivity(id);
        }
        // Update status
        ContentValues values = new ContentValues();
        values.put(getCorrectTableColumn(), serverStatus.ordinal());
        int numOfUpdatedRows = mContextRef.get().getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(id)});

        if (numOfUpdatedRows != 1) {
            Log.e(getTag(), String.format("Error updating %s (with ID=%d) server status to %s.",mOpType.name(), id, serverStatus.name()));
        } else {
            Log.d(getTag(), String.format("%s (with ID=%d) server status updated to %s.",mOpType.name(), id, serverStatus.name()));
        }
    }

    /**
     * Helper method for creating string path to the database
     * @return correct string path based on {@link #mOpType}
     */
    private String getCorrectTableColumn() {
        switch (mOpType){
            case DOWNLOAD_USER_PROFILE:{
                return DataContract.ProfileEntry.COLUMN_SERVER_STATUS;
            }

            default:
                return "";
        }
    }

    void addNotUploadedActivity(long id) {
        ContentValues values = new ContentValues(2);
        values.put(DataContract.NotUploadedEntry._ID, id);
        values.put(DataContract.NotUploadedEntry.COLUMN_ENTRY_TYPE, mOpType.ordinal());
        Uri uri = mContextRef.get().getContentResolver().insert(DataContract.NotUploadedEntry.CONTENT_URI, values);

        if (null == uri) {
            Log.e(getTag(), "Failed to insert object in NotUploaded table with id=" + id + "; object type:" + mOpType.name());
        } else {
            Log.d(getTag(), "Object inserted in NotUploaded table with id=" + id + "; object type:" + mOpType.name());
        }
    }

    /**
     * Helper method for starting new {@link Runnable} in a different thread.
     * @param r the runnable to execute
     */
    protected void submitRunnable(Runnable r){
        mProvider.submitRunnable(r);
    }

    /** Returns the TAG constant for logging **/
    protected abstract String getTag();
}

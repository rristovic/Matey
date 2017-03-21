package com.mateyinc.marko.matey.internet.operations;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.internet.DownloadListener;
import com.mateyinc.marko.matey.internet.MateyRequest;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.OperationProvider;
import com.mateyinc.marko.matey.internet.UploadListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.facebook.GraphRequest.TAG;
import static com.mateyinc.marko.matey.internet.UrlData.ACCESS_BASE_URL;
import static com.mateyinc.marko.matey.internet.operations.NewsfeedOp.mNextUrl;

/**
 * Class which contains upload/download actions that can be performed on defined data models.
 */
public abstract class Operations {

    /**
     * Field name for multi part post request body for json data
     **/
    protected static final String JSON_DATA_FIELD_NAME = "json_data";
    /**
     * Content json data field - post message
     **/
    protected static final String CONTENT_FIELD_NAME = "text";
    /**
     * Content json data field - post message
     **/
    protected static final String LOCATIONS_FIELD_NAME = "locations";

    /**
     * JSON key for data object in server response, contains every data that has been required.
     */
    public static final String KEY_DATA = "data";
    /**
     * JSON key for pagination object in server response, contains information about pages that are available to request and download.
     */
    protected static final String KEY_PAGINATION = "pagination";
    /**
     * JSON key for json object in server response, contains url and data for url of next page ready to download.
     */
    protected static final String KEY_LINKS = "_links";
    /**
     * JSON key for url string in server response, contains url for next page of data ready for download.
     */
    protected static final String KEY_NEXT_URL = "next";
    /**
     * JSON key for type of activity object in data array object
     */
    protected static final String KEY_ACTIVITY_TYPE = "activity_type";
    /**
     * JSON Value for post type of activity in data array object
     */
    protected static final String VALUE_ATYPE_POST = "POST";
    /**
     * JSON key for activity object containing activity data
     */
    protected static final String KEY_ACTIVITY_OBJECT = "activity_object";


    /**
     * Indicates what kind of operations this is.
     */
    protected OperationType mOpType;
    /**
     * Contains url path of desired end point.
     */
    protected String mUrl = "#";
    protected int mMethod;
    /**
     * Indicates if current data should be cleared before adding new one.
     */
    boolean mClearData = false;

    // Network request used for networking
    private MateyRequest mRequest;
    // Used for db control and to check if the provided context still exists
    protected final WeakReference<Context> mContextRef;
    // Used for networking and threading
    private OperationProvider mProvider;
    // Used for notifying classes
    protected EventBus mEventBus;

    protected DownloadListener mDownloadListener;
    protected UploadListener mUploadListener;

    Operations(Context context) {
        init();
        mOpType = OperationType.NO_OPERATION;
        this.mProvider = OperationManager.getInstance(context);
        mContextRef = new WeakReference<>(context);
    }

    Operations(Context context, OperationType operationType) {
        init();
        this.mOpType = operationType;
        this.mProvider = OperationManager.getInstance(context);
        mContextRef = new WeakReference<>(context);
    }

    Operations(OperationProvider provider, Context context, OperationType operationType) {
        init();
        this.mOpType = operationType;
        this.mProvider = provider;
        mContextRef = new WeakReference<>(context);
    }

    private void init() {
        mEventBus = EventBus.getDefault();
    }

    String buildNextPageUrl(String mNextUrl) {
        return Uri.parse(ACCESS_BASE_URL).buildUpon()
                .appendEncodedPath(mNextUrl).build().toString();
    }

    /**
     * Method for calling when old data should be cleared.
     *
     * @param clearData is true, old data will be cleared.
     */
    public void setDownloadFreshData(boolean clearData) {
        this.mClearData = clearData;
    }

    public void addDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    public void addUploadListener(UploadListener listener) {
        mUploadListener = listener;
    }

    /**
     * Method for setting the type of operation that will be performed.
     *
     * @param operationType {@link OperationType} type of the operation
     */
    public void setOperationType(OperationType operationType) {
        mOpType = operationType;
    }

    // Download methods
    public abstract void startDownloadAction();

    protected abstract void onDownloadSuccess(String response);

    protected abstract void onDownloadFailed(VolleyError error);

    // Upload methods
    public abstract void startUploadAction();

    protected abstract void onUploadSuccess(String response);

    protected abstract void onUploadFailed(VolleyError error);


    /**
     * Helper method for starting newly created request with {@link #createNewDownloadReq()};
     * Must be called to initiate the startDownloadAction process;
     * Sets the authorisation header with {@link OperationProvider#getAccessToken()};
     */
    void startDownload() {
        createNewDownloadReq();
        setDefaultAuthHeader();
        // Starting the startDownloadAction
        mProvider.submitRequest(mRequest);
    }

    /**
     * Method for creating new network request with provided parameters.
     * Url will be string value from global field {@link #mUrl}.
     */
    private void createNewDownloadReq() {
        // Creating new request
        mRequest = new MateyRequest(Request.Method.GET, mUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Context c = mContextRef.get();
                        if (c != null) {
                            Log.d(getTag(), "Download has succeed.");
                            submitRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    onDownloadSuccess(response);
                                }
                            });
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        Context c = mContextRef.get();
                        if (c != null) {
                            Log.e(getTag(), "Download has failed: " + error.getLocalizedMessage(), error);
                            submitRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    onDownloadFailed(error);
                                }
                            });
                        }
                    }
                });
    }

    String parseNextUrl(JSONObject object) {
        try {
            JSONObject pagination = object.getJSONObject(KEY_PAGINATION);
            pagination = pagination.getJSONObject(KEY_LINKS);
            String url = pagination.getString(KEY_NEXT_URL);
            url = url.substring(mNextUrl.lastIndexOf("/") + 1);// save next url
            return url;
        } catch (Exception e) {
            Log.w(TAG, "No value for next link.");
            return "";
        }
    }

    /**
     * Method for creating new network request with provided parameters
     *
     * @param url    url to startUploadAction to
     * @param method http method used for startUploadAction{@link com.android.volley.Request.Method}
     */
    void createNewUploadReq(final String url, int method) {
        mRequest = new MateyRequest(method, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        notifyUI(R.string.upload_success);

//                        if (successListener != null) {
//                            successListener.onResponse(response);
//                        }

                        Context c = mContextRef.get();
                        if (c != null) {
                            Log.d(getTag(), "Upload has succeed.");
                            onUploadSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        notifyUI(R.string.upload_failed);

//                        if (failedListener != null) {
//                            failedListener.onErrorResponse(error);
//                        }

                        Context c = mContextRef.get();
                        if (c != null) {
                            Log.e(getTag(), "Upload has failed: " + error.getLocalizedMessage(), error);
                            onUploadFailed(error);
                        }
                    }
                });
    }

    /**
     * Helper method for starting newly create request;
     * Must be called to initiate the startUploadAction process;
     * Sets the default authorisation header with {@link OperationProvider#getAccessToken()}
     */
    void startUpload() {
        setDefaultAuthHeader();
        // Starting the startDownloadAction
        mProvider.submitRequest(mRequest);
    }

    /**
     * Helper method for settings the default authorisation header by {@link MateyRequest#setAuthHeader(String)};
     */
    private void setDefaultAuthHeader() {
        mRequest.setAuthHeader(mProvider.getAccessToken());
    }

    /**
     * Helper method for adding params to the request; Only used with POST method
     *
     * @param key   parameter key
     * @param value parameter value
     */
    protected void addParam(String key, String value) {
        mRequest.addParam(key, value);
    }

    /**
     * Method for updating {@link com.mateyinc.marko.matey.data.MBaseColumns#COLUMN_SERVER_STATUS} in db.
     *
     * @param id           id of the activity/data.
     * @param serverStatus the {@link ServerStatus} to startUploadAction.
     */
    protected void updateServerStatus(long id, ServerStatus serverStatus) {
        // Add to not uploaded table if needed
        if (serverStatus == ServerStatus.STATUS_RETRY_UPLOAD) {
            addNotUploadedActivity(id);
        }
        // Update status
        ContentValues values = new ContentValues();
        values.put(getCorrectTableColumn(), serverStatus.ordinal());
        int numOfUpdatedRows = mContextRef.get().getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(id)});

        if (numOfUpdatedRows != 1) {
            Log.e(getTag(), String.format("Error updating %s (with ID=%d) server status to %s.", mOpType.name(), id, serverStatus.name()));
        } else {
            Log.d(getTag(), String.format("%s (with ID=%d) server status updated to %s.", mOpType.name(), id, serverStatus.name()));
        }
    }

    protected void runOnUiThread(Runnable r) {
        Handler handler = new Handler(mContextRef.get().getMainLooper());
        handler.post(r);
    }

    protected void notifyUI(final int stringId) {
        Handler handler = new Handler(mContextRef.get().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContextRef.get(), stringId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void notifyUI(final String message) {
        Handler handler = new Handler(mContextRef.get().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContextRef.get(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper method for creating string path to the database
     *
     * @return correct string path based on {@link #mOpType}
     */
    private String getCorrectTableColumn() {
        switch (mOpType) {
            case DOWNLOAD_USER_PROFILE: {
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
     *
     * @param r the runnable to execute
     */
    protected void submitRunnable(Runnable r) {
        mProvider.submitRunnable(r);
    }

    /**
     * Returns the TAG constant for logging
     **/
    protected abstract String getTag();

    /**
     * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
     */
    public class DownloadEvent {
        DownloadEvent(boolean isSuccess){
            this.isSuccess = isSuccess;
        }
        boolean isSuccess;
    }

    /**
     * Static class used for event bus. Event that indicates that upload has been success of failed.
     */
    public class UploadEvent {
        UploadEvent(boolean isSuccess){
            this.isSuccess = isSuccess;
        }
        boolean isSuccess;
    }

}

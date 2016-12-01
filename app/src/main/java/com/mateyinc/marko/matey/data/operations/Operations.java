package com.mateyinc.marko.matey.data.operations;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.data.internet.MateyRequest;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.lang.ref.WeakReference;

public abstract class Operations {
    // Network request used for networking
    private MateyRequest mRequest;
    // Used for db control and to check if the provided context still exists
    protected WeakReference<MotherActivity> mContextRef;
    // Used for networking and threading
    protected OperationProvider mProvider;

     Operations(OperationProvider provider, MotherActivity context){
        this.mProvider = provider;
         mContextRef = new WeakReference<MotherActivity>(context);
    }

    // Download methods
    public abstract void download(long id);
    protected abstract void onDownloadSuccess(String response);
    protected abstract void onDownloadFailed(VolleyError error);
    // Upload methods
    public abstract <T> void upload(T object);
    protected abstract <T> void onUploadSuccess(T object);
    protected abstract <T> void onUploadFailed(T object);

    /** Returns the TAG constant for logging **/
    protected abstract String getTag();

    /**
     * Method for creating new network request with provided parameters
     * @param url url to download from
     */
    void createDownloadReq(String url){
        // Creating new request
         mRequest = new MateyRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        MotherActivity c = mContextRef.get();
                        if (c != null){
                            onDownloadSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getTag(), error.getLocalizedMessage(), error);
                        MotherActivity c = mContextRef.get();
                        if (c != null){
                            onDownloadFailed(error);
                        }
                    }
                });

        // Settings the default auth header
        mRequest.setAuthHeader(mProvider.getAccessToken());
    }

    /**
     * Helper method for starting the newly created request with {@link #createDownloadReq(String)}  ;
     * Must be called to initiate the download process;
     */
    void startDownload(){
        mProvider.submitRequest(mRequest);
    }

    /**
     * Helper method for adding params to the request; Only used with POST method
     * @param key parameter key
     * @param value parameter value
     */
    protected void addParam(String key, String value){
        mRequest.addParam(key, value);
    }
}

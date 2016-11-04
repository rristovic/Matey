package com.mateyinc.marko.matey.internet.procedures;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.internet.MateyRequest;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mateyinc.marko.matey.data.DataManager.STATUS_RETRY_UPLOAD;

public class UploadService extends Service {

    private static final String TAG = UploadService.class.getSimpleName();

    public UploadService() {
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public UploadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UploadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "entered onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "entered onDestroy()");
        super.onDestroy();
    }


    public void uploadFailedData(){
        SessionManager sessionManager = SessionManager.getInstance(this);
        final DataManager dataManager = DataManager.getInstance(this);

        // TODO - finish method
    }

    /**
     * Method for uploading Bulletin to the server
     * @param b the bulletin to be uploaded
     */
    public void uploadBulletins(final Bulletin b, String accessToken) {
        SessionManager sessionManager = SessionManager.getInstance(this);
        final DataManager dataManager = DataManager.getInstance(this);
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_BULLETINS_ROUTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            dataManager.updateBulletinPostId(b.getPostID(), object.getLong(Bulletin.KEY_POST_ID));
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage(), e);
                            dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
                        }
                        dataManager.updateBulletinPostId(b.getPostID(), -1);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }
        );

        uploadRequest.setAuthHeader(accessToken);
        uploadRequest.addParam(UrlData.PARAM_INTEREST_ID, "1");
        uploadRequest.addParam(UrlData.PARAM_TEXT_DATA, b.getMessage());

        sessionManager.addToRequestQueue(uploadRequest);
    }

    /**
     * Method for uploading profiles that are followed by the current user to the server
     * @param profileList the list of profiles to be uploaded
     */
    public void uploadFollowedFriends(final ArrayList<UserProfile> profileList, String accessToken) {
        SessionManager sessionManager = SessionManager.getInstance(this);
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_FOLLOWED_FRIENDS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
                        Log.e(TAG, error.getLocalizedMessage(), error);
                        // TODO - finish error handling
                    }
                }
        );
        uploadRequest.setAuthHeader(accessToken);
        uploadRequest.setBodyFromFollowedProfiles(profileList);

        sessionManager.addToRequestQueue(uploadRequest);
    }
}
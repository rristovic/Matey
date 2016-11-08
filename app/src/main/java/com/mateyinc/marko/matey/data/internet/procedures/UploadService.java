package com.mateyinc.marko.matey.data.internet.procedures;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.MateyRequest;
import com.mateyinc.marko.matey.data.internet.NetworkManager;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;


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
        NetworkManager networkManager = NetworkManager.getInstance(this);
        final DataManager dataManager = DataManager.getInstance(this);

        // TODO - finish method
    }

//    /**
//     * Method for uploading Bulletin to the server
//     * @param b the bulletin to be uploaded
//     * @param accessToken access token string used to access the server
//     */
//    public void uploadBulletin(final Bulletin b, String accessToken) {
//        NetworkManager networkManager = NetworkManager.getInstance(this);
//        final DataManager dataManager = DataManager.getInstance(this);
//        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_BULLETINS_ROUTE,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject object = new JSONObject(response);
//                            dataManager.updateBulletinPostId(b.getPostID(), object.getLong(Bulletin.KEY_POST_ID));
//                        } catch (JSONException e) {
//                            Log.e(TAG, e.getLocalizedMessage(), e);
//                            dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
//                        Log.e(TAG, error.getLocalizedMessage(), error);
//                    }
//                }
//        );
//        uploadRequest.setAuthHeader(accessToken);
////        uploadRequest.addParam(UrlData.PARAM_INTEREST_ID, "1");
//        uploadRequest.addParam(UrlData.PARAM_TEXT_DATA, b.getMessage());
//
//        networkManager.addToRequestQueue(uploadRequest);
//    }

//    /**
//     * Method for uploading Reply to the server
//     * @param reply the reply to be uploaded
//     * @param accessToken access token string used to access the server
//     */
//    public void uploadReply(final Reply reply, String accessToken) {
//        NetworkManager networkManager = NetworkManager.getInstance(this);
//        final DataManager dataManager = DataManager.getInstance(this);
//        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_REPLY_ROUTE,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(final String response) {
//                        Runnable r = new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    JSONObject object = new JSONObject(response);
//                                    dataManager.updateReplyId(reply.replyId, object.getLong(Reply.REPLY_ID));
//                                    dataManager.updateReplyDate(reply, new Date(object.getString(Reply.DATE)));
//                                } catch (JSONException e) {
//                                    Log.e(TAG, e.getLocalizedMessage(), e);
//                                    // Update reply server status and bulletin replies count
//                                    dataManager.updateReplyServerStatus(reply, STATUS_RETRY_UPLOAD);
//                                    dataManager.decrementBulletinRepliesCount(reply.postId);
//                                }
//                            }
//                        };
//
//                        Thread t = new Thread(r);
//                        t.start();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        dataManager.updateReplyServerStatus(reply, STATUS_RETRY_UPLOAD);
////                        dataManager.decrementBulletinRepliesCount(reply.postId);
//                        Log.e(TAG, error.getLocalizedMessage(), error);
//                    }
//                }
//        );
//
//        uploadRequest.setAuthHeader(accessToken);
//        uploadRequest.addParam(UrlData.PARAM_REPLY_POST_ID, Long.toString(reply.postId));
//        uploadRequest.addParam(UrlData.PARAM_REPLY_TEXT_DATA, reply.replyText);
//
//        networkManager.addToRequestQueue(uploadRequest);
//    }

//    /**
//     * Uploading multiple replies to the server
//     *  @see #uploadReply(Reply, String)
//     */
//    public void uploadReplies(ArrayList<Reply> list, String accessToken){
//        for (Reply r : list) {
//            uploadReply(r, accessToken);
//        }
//    }

    /**
     * Method for uploading Approve to the server
     * @param postId id of the post that contains approved reply
     * @param replyId id of the reply that has been approved;
     */
    public void uploadApprove(long postId, long replyId, String accessToken) {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        final DataManager dataManager = DataManager.getInstance(this);
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_LIKE_ROUTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        try {
//                        } catch (JSONException e) {
//                            Log.e(TAG, e.getLocalizedMessage(), e);
////                            dataManager.updateReplyServerStatus(reply, STATUS_RETRY_UPLOAD);
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        dataManager.updateReplyServerStatus(reply, STATUS_RETRY_UPLOAD);
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }
        );

        uploadRequest.setAuthHeader(accessToken);
        uploadRequest.addParam(UrlData.PARAM_LIKED_POST_ID, Long.toString(postId));
        if (replyId != 0)
            uploadRequest.addParam(UrlData.PARAM_LIKED_REPLY_ID, Long.toString(replyId));

        networkManager.addToRequestQueue(uploadRequest);
    }

    /**
     * Method for uploading Approve to the server
     * @param postId id of the post that has been approved
     */
    public void uploadApprove(long postId, String accessToken) {
        uploadApprove(postId, 0, accessToken);
    }

    /**
     * Method for uploading profiles that are followed by the current user to the server
     * @param profileList the list of profiles to be uploaded
     */
    public void uploadFollowedFriends(final ArrayList<UserProfile> profileList, String accessToken) {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        MateyRequest uploadRequest = new MateyRequest(Request.Method.POST, UrlData.POST_NEW_FOLLOWED_FRIENDS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO - finish
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

        networkManager.addToRequestQueue(uploadRequest);
    }
}

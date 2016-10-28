package com.mateyinc.marko.matey.internet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.JSONParserAs;
import com.mateyinc.marko.matey.internet.procedures.UploadService;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.KVPair;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.mateyinc.marko.matey.gcm.MateyGCMPreferences.SENT_TOKEN_TO_SERVER;
import static com.mateyinc.marko.matey.internet.UrlData.GET_NEWSFEED_ROUTE;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_AUTH_TYPE;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_COUNT;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_START_POS;


/**
 * Created by M4rk0 on 3/10/2016.
 */

/**
 * Class for syncing with the server (e.g. LOGIN, LOGOUT, REGISTER, DOWNLOAD & UPLOAD DATA)
 */
public class SessionManager {
    private static final String TAG = SessionManager.class.getSimpleName();
    public static final String APPID_FILE_NAME = "did.dat";

    // Key for securePreference to store the device_id
    public static final String PREF_DEVICE_ID = "device_id";

    // Fields downloaded from OAuth2 Server
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    /**
     * SharedPref name for data that indicates when is the ACCESS_TOKEN saved in db
     */
    public static final String TOKEN_SAVED_TIME = "tst";

    // Fields downloaded from Resource Server
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_VERIFIED = "verified";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_PROFILE_PICTURE = "profile_picture";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";

    /**
     * The application id is on hard drive
     */
    public static final int STATUS_OK = 100;

    /**
     * Something when wrong
     */
    public static final int STATUS_ERROR = 100;

    /**
     * Error with getting the application id
     */
    public static final int STATUS_ERROR_APPID = 400;

    /**
     * Number of milliseconds that the app will keep connecting to the server
     */
    public static final long SERVER_CONECTION_TIMEOUT = 15000;

    /**
     * ACCESS_TOKEN used to authorise with the server
     */
    private String ACCESS_TOKEN = "";


    private static SessionManager mInstance;
    private static Context mAppContext;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private ProgressDialog mProgDialog;
    private UploadService mUploadService;
    private final Object mLock;
    private boolean mIsBound;


    public static synchronized SessionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SessionManager(context);
            Log.d(TAG, "New instance of SessionManager created.");

        }

        return mInstance;
    }

    private SessionManager(Context context) {
        mAppContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
        mLock = new Object();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mAppContext);
            Log.e(TAG, "New requestQueue has been created!");
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        synchronized (mLock) {
            getRequestQueue().add(req);
        }
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /** Helper method to start {@link UploadService} used for uploading data to the server */
    public void startUploadService() {
        Intent intent = new Intent(mAppContext, UploadService.class);
        mAppContext.startService(intent);
        mIsBound = mAppContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** Helper method to stop {@link UploadService} */
    public void stopUploadService() {

        if (mIsBound) {
            mAppContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            UploadService.LocalBinder binder = (UploadService.LocalBinder) service;
            mUploadService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO - finish error handling
            mUploadService = null;
            Log.e(TAG, "Service disconected");
        }
    };
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////// INTERNET METHODS ////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /** Method for uploading failed data to the server */
    public void uploadFailedData() {
        Log.d(TAG, "Uploading failed data.");

        if (mUploadService != null && mConnection != null)
            mUploadService.uploadFailedData();
        else {
            startUploadService();
        }
    }


    public void postNewBulletin(Bulletin b, DataManager dataManager) {
        Log.d(TAG, "Posting new bulletin.");

        // First add the bulletin to the database then upload it to the server
        dataManager.addBulletin(b, DataManager.STATUS_UPLOADING);

        if (mUploadService != null && mConnection != null)
            mUploadService.uploadBulletin(b);
        else {
            dataManager.updateBulletinServerStatus(b, DataManager.STATUS_RETRY_UPLOAD);
            startUploadService();
        }
    }

    public void getNewsFeed(int start, int count, final DataManager dm) {

        Log.d(TAG, "Downloading news feed. Start position=".concat(Integer.toString(start))
                .concat("; Count=").concat(Integer.toString(count)));

        Uri.Builder builder = Uri.parse(GET_NEWSFEED_ROUTE).buildUpon();
        builder.appendQueryParameter(PARAM_START_POS, Integer.toString(start))
                .appendQueryParameter(PARAM_COUNT, Integer.toString(count));
        URL url;
        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Downloading failed: " + e.getLocalizedMessage(), e);
            return;
        }

        MateyRequest request = new MateyRequest(Request.Method.GET, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Parse data in new thread
                JSONParserAs jsonParserAS = new JSONParserAs(mAppContext);
                jsonParserAS.execute(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MateyRequest.TAG, error.getLocalizedMessage(), error);
            }
        });
        request.setAuthHeader(PARAM_AUTH_TYPE, String.format("Bearer %s", ACCESS_TOKEN));

        mRequestQueue.add(request);
    }

    /**
     * Helper method for downloading news feed from the server to the database;
     * Downloads {@value DataManager#NUM_OF_BULLETINS_TO_DOWNLOAD} bulletins from the server;
     * Automatically determines from what bulletin position to download by calling {@link DataManager#getNumOfBulletinsInDb()}
     *
     * @param context the context of activity which is calling this method
     */
    public void getNewsFeed(final Context context) {
        int start = DataManager.getInstance(context).getNumOfBulletinsInDb();
        getNewsFeed(start, DataManager.NUM_OF_BULLETINS_TO_DOWNLOAD, DataManager.getInstance(context));
    }

    /**
     * Helper method for user registration on to the server, also updates the UI
     *
     * @param email user email address
     * @param pass  user password
     */
    public void registerWithVolley(final String email, String pass, final MainActivity context) {
        // Showing progress dialog
        mProgDialog = new ProgressDialog(context);
        mProgDialog.setMessage(context.getResources().getString(R.string.registering_dialog_message));
        mProgDialog.show();

        // Making new request and contacting the server
        MateyRequest request = new MateyRequest(Request.Method.POST, UrlData.REGISTER_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(context, R.string.success_reg_message, Toast.LENGTH_SHORT).show();

                if (mProgDialog.isShowing())
                    mProgDialog.dismiss();

                // Adding new account to AM
                AccountManager am = AccountManager.get(context);
                Account account = new Account(email, context.getString(R.string.account_type));
                am.addAccountExplicitly(account, null, null);

                // Updating UI
                context.startRegReverseAnim();
                context.mRegFormVisible = false;
                context.etEmail.setText("");
                context.etPass.setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while registering user.");
                // TODO - finish method
            }
        });

        // TODO - finish params in UI
        request.addParam(UrlData.PARAM_USER_FIRST_NAME, "KURAC");
        request.addParam(UrlData.PARAM_USER_LAST_NAME, "KURAC");
        request.addParam(UrlData.PARAM_EMAIL, email);
        request.addParam(UrlData.PARAM_PASSWORD, pass);
        mRequestQueue.add(request);
    }

    /**
     * Helper method for user login on to the server, also updates the UI
     *
     * @param email user's email address
     * @param pass  user's password
     */
    public void loginWithVolley(final String email, String pass, final SecurePreferences securePreferences, final MainActivity context) {

        // Showing progress dialog
        mProgDialog = new ProgressDialog(context);
        mProgDialog.setMessage(context.getResources().getString(R.string.login_dialog_message));
        mProgDialog.show();

        if (email.equals("sarma@nis.com") && pass.equals("radovan")) {
            login(securePreferences, context);
        }

        // First contacting OAuth2 server
        MateyRequest oauthRequest = new MateyRequest(Request.Method.POST, UrlData.OAUTH_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // Parsing response.data
                try {
                    JSONObject dataObj = new JSONObject(response);

                    // Add data to secure prefs TODO - rework secure prefs Editor()
                    ArrayList<KVPair> list = new ArrayList<KVPair>();
                    list.add(new KVPair(KEY_ACCESS_TOKEN, dataObj.getString(KEY_ACCESS_TOKEN)));
                    list.add(new KVPair(KEY_EXPIRES_IN, dataObj.getString(KEY_EXPIRES_IN)));
                    list.add(new KVPair(KEY_REFRESH_TOKEN, dataObj.getString(KEY_REFRESH_TOKEN)));
                    list.add(new KVPair(KEY_TOKEN_TYPE, dataObj.getString(KEY_TOKEN_TYPE)));
                    securePreferences.putValues(list);

                    // Saves the time when token is created
                    final SharedPreferences preferences = getDefaultSharedPreferences(context);
                    preferences.edit().putLong(TOKEN_SAVED_TIME, System.currentTimeMillis()).apply();

                    // Immediately after contacting OAuth2 Server proceed to resource server for login
                    // Creating new request for the resource server
                    MateyRequest resRequest = new MateyRequest(Request.Method.POST, UrlData.LOGIN_USER, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject object;

                            // Parsing response.data
                            try {
                                object = new JSONObject(response);

                                // Adding current user to the database
                                DataManager dataManager = DataManager.getInstance(context);
                                UserProfile userProfile = new UserProfile(object.getInt(KEY_USER_ID),
                                        object.getString(KEY_FIRST_NAME),
                                        object.getString(KEY_LAST_NAME),
                                        object.getString(KEY_EMAIL),
                                        object.getString(KEY_PROFILE_PICTURE));
                                dataManager.addUserProfile(userProfile);
                                dataManager.setCurrentUserProfile(preferences, userProfile);

                            } catch (JSONException e) {
                                // TODO - finish error handling
                                Log.e(TAG, e.getLocalizedMessage(), e);
                                return;
                            } catch (SecurityException se) {
                                Log.e(TAG, se.getLocalizedMessage(), se);
                            }

                            // Close progress dialog
                            if (mProgDialog.isShowing())
                                mProgDialog.dismiss();

                            // Close activity and proceed to HomeActivity
                            context.loggedIn();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO - handle errors
                            Log.e(TAG, error.toString());
                            if (mProgDialog.isShowing())
                                mProgDialog.dismiss();
                        }
                    });
                    // Setting request params and sending POST request
                    resRequest.addParam(UrlData.PARAM_EMAIL, email);
                    resRequest.addParam(UrlData.PARAM_DEVICE_ID, securePreferences.getString(PREF_DEVICE_ID));
                    resRequest.setAuthHeader(UrlData.PARAM_AUTH_TYPE,
                            String.format("Bearer %s", securePreferences.getString(KEY_ACCESS_TOKEN)));
                    mRequestQueue.add(resRequest);

                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                    if (mProgDialog.isShowing())
                        mProgDialog.dismiss();
                    // TODO - finish error handling
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while authenticating user.", error.getCause());
                // TODO - finish method
            }
        });

        // Contacting OAuth2 server with required params
        oauthRequest.addParam(UrlData.PARAM_GRANT_TYPE, UrlData.PARAM_GRANT_TYPE_PASSWORD);
        oauthRequest.addParam(UrlData.PARAM_CLIENT_ID, UrlData.PARAM_CLIENT_ID_VALUE);
        oauthRequest.addParam(UrlData.PARAM_CLIENT_SECRET, UrlData.PARAM_CLIENT_SECRET_VALUE);
        oauthRequest.addParam(UrlData.PARAM_USERNAME, email);
        oauthRequest.addParam(UrlData.PARAM_PASSWORD, pass);
        mRequestQueue.add(oauthRequest);
    }

    public void loginWithFacebook(final String accessToken, String profileId, final String email, final SecurePreferences securePreferences, final MainActivity context) {
        String url = UrlData.FACEBOOK_LOGIN;

        // Request a string response from the provided URL.
        MateyRequest oauthRequest = new MateyRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Parsing response.data
                try {
                    JSONObject dataObj = new JSONObject(response);

                    // Add data to secure prefs TODO - rework secure prefs Editor()
                    ArrayList<KVPair> list = new ArrayList<KVPair>();
                    list.add(new KVPair(KEY_ACCESS_TOKEN, dataObj.getString(KEY_ACCESS_TOKEN)));
                    list.add(new KVPair(KEY_EXPIRES_IN, dataObj.getString(KEY_EXPIRES_IN)));
                    list.add(new KVPair(KEY_REFRESH_TOKEN, dataObj.getString(KEY_REFRESH_TOKEN)));
                    list.add(new KVPair(KEY_TOKEN_TYPE, dataObj.getString(KEY_TOKEN_TYPE)));
                    securePreferences.putValues(list);

                    // Saves the time when token is created
                    final SharedPreferences preferences = getDefaultSharedPreferences(context);
                    preferences.edit().putLong(TOKEN_SAVED_TIME, System.currentTimeMillis()).apply();

                    // Immediately after contacting OAuth2 Server proceed to resource server for login
                    // Creating new request for the resource server
                    MateyRequest resRequest = new MateyRequest(Request.Method.POST, UrlData.LOGIN_USER, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject object;

                            // Parsing response.data
                            try {
                                object = new JSONObject(response);

                                // Adding current user to the database
                                DataManager dataManager = DataManager.getInstance(context);
                                UserProfile userProfile = new UserProfile(object.getInt(KEY_USER_ID),
                                        object.getString(KEY_FIRST_NAME),
                                        object.getString(KEY_LAST_NAME),
                                        object.getString(KEY_EMAIL),
                                        object.getString(KEY_PROFILE_PICTURE));
                                dataManager.addUserProfile(userProfile);
                                dataManager.setCurrentUserProfile(preferences, userProfile);

                            } catch (JSONException e) {
                                // TODO - finish error handling
                                Log.e(TAG, e.getLocalizedMessage(), e);
                                return;
                            } catch (SecurityException se) {
                                Log.e(TAG, se.getLocalizedMessage(), se);
                            }

                            // Close progress dialog
//                            if (mProgDialog.isShowing())
//                                mProgDialog.dismiss();

                            // Close activity and proceed to HomeActivity
                            context.loggedIn();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO - handle errors
                            Log.e(TAG, error.toString());
//                            if (mProgDialog.isShowing())
//                                mProgDialog.dismiss();
                        }
                    });
                    // Setting request params and sending POST request
                    resRequest.addParam(UrlData.PARAM_EMAIL, email);
                    resRequest.addParam(UrlData.PARAM_DEVICE_ID, securePreferences.getString(PREF_DEVICE_ID));
//                    resRequest.addParam(UrlData.PARAM_ACCESS_TOKEN, mSecurePreferences.getString(KEY_ACCESS_TOKEN));
                    resRequest.setAuthHeader(UrlData.PARAM_AUTH_TYPE,
                            String.format("Bearer %s", securePreferences.getString(KEY_ACCESS_TOKEN)));
                    mRequestQueue.add(resRequest);

                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
//                    if (mProgDialog.isShowing())
//                        mProgDialog.dismiss();
                    // TODO - finish error handling
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getDefaultSharedPreferences(context).edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                Log.e(TAG, error.getLocalizedMessage(), error);
                // TODO - error in response
            }
        });
        // Contacting OAuth2 server with required params
        oauthRequest.addParam(UrlData.PARAM_GRANT_TYPE, UrlData.PARAM_GRANT_TYPE_SOCIAL);
        oauthRequest.addParam(UrlData.PARAM_CLIENT_ID, UrlData.PARAM_CLIENT_ID_VALUE);
        oauthRequest.addParam(UrlData.PARAM_CLIENT_SECRET, UrlData.PARAM_CLIENT_SECRET_VALUE);
        oauthRequest.addParam(UrlData.PARAM_FBTOKEN, accessToken);
        mRequestQueue.add(oauthRequest);
    }

    /**
     * Helper method for logging out from the app
     *
     * @param context           the HomeActivity context
     * @param securePreferences the SecuredPrefs user to clear user credentials
     */
    public void logout(HomeActivity context, SecurePreferences securePreferences) {
        clearUserCredentials(context, securePreferences);
        clearDatabase(context);
        context.mLoggedIn = false;

        if (context.isDebug()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().remove("IS_DEBUG").remove("DATA_CREATED").apply();
        }
        // TODO - Inform server about logout

        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
        context.finish();
    }

    public static void clearUserCredentials(Context context, SecurePreferences securePreferences) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        DataManager dataManager = DataManager.getInstance(context);
        dataManager.removeUserProfile(preferences.getInt(DataManager.CUR_USERPROFILE_ID, -1));
        dataManager.setCurrentUserProfile(preferences, null);

        preferences.edit().remove(DataManager.CUR_USERPROFILE_ID).remove(TOKEN_SAVED_TIME).commit();

        // Clearing user credentials
        securePreferences.removeValue(KEY_ACCESS_TOKEN);
        securePreferences.removeValue(KEY_EXPIRES_IN);
        securePreferences.removeValue(KEY_REFRESH_TOKEN);
        securePreferences.removeValue(KEY_TOKEN_TYPE);
    }

    private static void clearDatabase(Context context) {
        context.getContentResolver().delete(DataContract.ProfileEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.ReplyEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.BulletinEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.MessageEntry.CONTENT_URI, null, null);
        context.getContentResolver().delete(DataContract.NotificationEntry.CONTENT_URI, null, null);
    }

    /**
     * Method for downloading application id from the server and saving it in securePrefs or reading it from file if it already exists
     *
     * @param activity a context used for opening FileOutput etc
     * @return the status code. (STATUS_OK, STATUS_ERROR_APPID)
     */
    public int getInstallationID(Context activity, SecurePreferences securePreferences) {

//        // try to read from file
//        String device_id = readFromFile(activity);
//
//        // if nothing was red it means that this is the first launch
//        // of the application
//        // so request device id from the server
//        if (device_id.equals("")) {
//
//            try {
//
//                // TODO - use volley
//                // http things, get device_id
//                String result = null;
////                HTTP http = new HTTP(UrlData.FIRST_RUN_URL, "GET");
////                result = http.getData();
//
//                // if returned null
//                if (result == null) throw new Exception();
//
//                else {
//                    // if data is here, see if it was successful
//                    // if not show error screen
//                    // else write it to file
//                    JSONObject jsonObject = new JSONObject(result);
//
//                    if (jsonObject.getBoolean("success")) {
//
//                        FileOutputStream fOut = activity.openFileOutput(APPID_FILE_NAME, Context.MODE_PRIVATE);
//                        OutputStreamWriter osw = new OutputStreamWriter(fOut);
//                        osw.write(jsonObject.getString("device_id"));
//                        osw.flush();
//                        osw.close();
//
//                        mSecurePreferences.put("device_id", jsonObject.getString("device_id"));
//                        Log.d(TAG, jsonObject.getString("device_id"));
//
//                        return STATUS_OK;
//
//                    } else throw new Exception();
//
//                }
//
//            } catch (Exception e) {
//                return STATUS_ERROR_APPID;
//            }
//
//        } else mSecurePreferences.put("device_id", device_id);

        if (securePreferences.getString(SessionManager.PREF_DEVICE_ID) != null)
            return STATUS_OK;
        else {
            return STATUS_ERROR_APPID;
        }

    }

    public String readFromFile(Context context) {
        StringBuffer datax = new StringBuffer("");

        try {
            FileInputStream fIn = context.openFileInput(APPID_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
        } catch (Exception e) {
        }

        return datax.toString();
    }

    public void setAccessToken(String string) {
        ACCESS_TOKEN = string;
    }

    /**
     * Helper method for returning {@link com.mateyinc.marko.matey.internet.SessionManager#ACCESS_TOKEN};
     *
     * @return the access_token from this class; empty string if there is no access_token
     */
    public String getAccessToken() {
        return ACCESS_TOKEN;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////// DEBUG/TEST //////////////////////////////////////////////////////////////////////////////////////

    private void login(SecurePreferences securePreferences, MainActivity context) {
        ArrayList<KVPair> list = new ArrayList<KVPair>();
        list.add(new KVPair(KEY_ACCESS_TOKEN, "radovan"));
        list.add(new KVPair(KEY_EXPIRES_IN, "100000000000"));
        list.add(new KVPair(KEY_REFRESH_TOKEN, "radovan"));
        list.add(new KVPair(KEY_TOKEN_TYPE, "rade"));
        securePreferences.putValues(list);

        // Saves the time when token is created
        final SharedPreferences preferences = getDefaultSharedPreferences(context);
        preferences.edit().putLong(TOKEN_SAVED_TIME, System.currentTimeMillis()).apply();

        // Adding current user to the database
        DataManager dataManager = DataManager.getInstance(context);
        UserProfile userProfile = new UserProfile(666,
                context.getString(R.string.dev_name),
                context.getString(R.string.dev_lname),
                context.getString(R.string.dev_email),
                context.getString(R.string.dev_pic));
        userProfile.setNumOfFriends(40);
        dataManager.addUserProfile(userProfile);
        dataManager.setCurrentUserProfile(preferences, userProfile);

        // Close progress dialog
        if (mProgDialog.isShowing())
            mProgDialog.dismiss();

        // Close activity and proceed to HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        getDefaultSharedPreferences(context).edit().putBoolean("IS_DEBUG", true).commit();
        context.startActivity(intent);
        context.finish();
    }

    public void createDummyData(HomeActivity homeActivity) {
        DataManager dm = DataManager.getInstance(homeActivity);
        dm.createDummyData();
    }
}

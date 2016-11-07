package com.mateyinc.marko.matey.internet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.AddFriendsActivity;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.JSONParserAs;
import com.mateyinc.marko.matey.gcm.MateyGCMPreferences;
import com.mateyinc.marko.matey.gcm.RegistrationIntentService;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.procedures.UploadService;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.KVPair;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.mateyinc.marko.matey.activity.main.MainActivity.NEW_GCM_TOKEN;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_UPLOADING;
import static com.mateyinc.marko.matey.gcm.MateyGCMPreferences.SENT_TOKEN_TO_SERVER;
import static com.mateyinc.marko.matey.inall.MotherActivity.access_token;
import static com.mateyinc.marko.matey.internet.UrlData.GET_NEWSFEED_ROUTE;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_AUTH_TYPE;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_COUNT;
import static com.mateyinc.marko.matey.internet.UrlData.PARAM_START_POS;


/**
 * Class for syncing with the server (e.g. LOGIN, LOGOUT, REGISTER, DOWNLOAD & UPLOAD DATA)
 */
public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();

    // Key for securePreference to store the device_id
    public static final String KEY_DEVICE_ID = "device_id";

    // Fields downloaded from OAuth2 Server
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    /** SharedPref name for data that indicates when is the ACCESS_TOKEN saved in db */
    public static final String TOKEN_SAVED_TIME = "tst";

    // Fields downloaded from Resource Server
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_SUGGESTED_FRIENDS = "suggested_friends";

    /** The application id is on hard drive NetworkManager status */
    public static final int STATUS_OK = 100;

    /** Something when wrong NetworkManager status*/
    public static final int STATUS_ERROR = 200;

    /** Error with getting the application id NetworkManager status*/
    private static final int STATUS_ERROR_APPID = 400;

    private static NetworkManager mInstance;
    private static final Object mInstanceLock = new Object();

    //    private Context mAppContext;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private ProgressDialog mProgDialog;
    private UploadService mUploadService;
    private boolean mIsBound;

    private final Object mLock = new Object();
    private final BlockingQueue<Runnable> mDecodeWorkQueue;
    private final ThreadPoolExecutor mExecutor;

    // Threading constants
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 10;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private CallbackManager mFbCallback;

    public static NetworkManager getInstance(Context context) {
        synchronized (mInstanceLock) {
            if (mInstance == null) {
                mInstance = new NetworkManager(context.getApplicationContext());
                Log.d(TAG, "New instance of NetworkManager created.");
            }
        }

        return mInstance;
    }

    private NetworkManager(Context context) {

        mRequestQueue = Volley.newRequestQueue(context);
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

        // Creates a thread pool manager
        mExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);

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

    /** Helper method that checks all the required parameters for starting a new session with the server */
    public void startSession(final MainActivity activity){
        // If there is no internet connection, show alert dialog
        if(!Util.isInternetConnected(activity)){
            Util.showAlertDialog(activity, activity.getString(R.string.no_internet_msg)
                    , null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.endLoadingAnim();
                            activity.mDeviceReady = false;
                            dialog.dismiss();
                        }
                    });
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        // If user is logged in, proceed to next activity
        if(isUserLoggedIn(sharedPreferences)) {
            loggedIn(activity);
            return;
        }

        if (!RegistrationIntentService.isRunning && checkPlayServices(activity)) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);

            if (!sentToken) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(activity, RegistrationIntentService.class);
                activity.startService(intent);
            } else if (activity.getSecurePreferences().getString(KEY_DEVICE_ID) == null) {
                // The device has already been registered with GCM but not with the server
                registerDevice(activity, activity.getSecurePreferences(),
                        sharedPreferences.getString(NEW_GCM_TOKEN, ""));
            } else {
                // The device is registered both with GCM and with the server
                // Proceed further
                activity.mDeviceReady = true;
            }
        }
    }

    /** Method to check if user is logged in */
    private boolean isUserLoggedIn(SharedPreferences preferences) {
        // Only check if the id exists because on logout it gets deleted
        long user_id = preferences.getLong(DataManager.KEY_CUR_USER_ID, -1);

        return  user_id != -1;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(final MainActivity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, 1000)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                Util.showAlertDialog(activity, activity.getString(R.string.error_tittle),
                        activity.getString(R.string.nogcm_message), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish();
                            }

                        });
            }
            return false;
        }
        return true;
    }

    public void registerDevice(MainActivity activity, SecurePreferences securePreferences, final String gcmToken){
        final WeakReference<MainActivity> reference = new WeakReference<>(activity);
        final WeakReference<SecurePreferences> prefRef = new WeakReference<SecurePreferences>(securePreferences);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        // GCM complete with success
        if (gcmToken != null && gcmToken.length() != 0) {

            // No old token found
//            if (sharedPreferences.getString(OLD_GCM_TOKEN, null) == null) {
                // Request a string response from the provided URL.
                MateyRequest stringRequest = new MateyRequest(Request.Method.POST, UrlData.REGISTER_DEVICE, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (mProgDialog != null)
                            dismissProgressDialog(mProgDialog);
                        MainActivity activity = reference.get();
                        SecurePreferences securePreferences = prefRef.get();
                        try {
                            // Parse data
                            JSONObject object = new JSONObject(response);
                            String device_id = object.getString(KEY_DEVICE_ID);

                            // Save data
                            securePreferences.put(KEY_DEVICE_ID, device_id);
                            MotherActivity.device_id = device_id;
                            // Save prefs that token has been sent to the server
                            sharedPreferences.edit().putBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, true).apply();

                            // Notify UI
                            activity.mDeviceReady = true;
                            Log.d(TAG, "Device id=" + device_id);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MainActivity activity = reference.get();
                        if (activity != null) {
                            if (mProgDialog != null)
                                dismissProgressAndShowAlert(activity);
                            else
                                showServerAlert(activity);
                            activity.mDeviceReady = false;
                            activity.endLoadingAnim();
                        }
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                });
                stringRequest.addParam(UrlData.PARAM_NEW_GCM_ID, gcmToken);

                // Add the request to the RequestQueue.
                mInstance.addToRequestQueue(stringRequest);
                activity = null;
//            } else {
                // TODO - finish
//            }
        } else {
            activity.mDeviceReady = false;
            activity.endLoadingAnim();
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Method for user registration on to the server, also updates the UI
     *
     * @param context the MainActivity context used for UI control
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user email address
     * @param pass  user password
     */
    public void registerWithVolley(final MainActivity context, String firstName, String lastName, final String email, String pass) {
        // Showing progress dialog
        mProgDialog = new ProgressDialog(context);
        mProgDialog.setMessage(context.getResources().getString(R.string.registering_dialog_message));
        mProgDialog.show();

        // Making new request and contacting the server
        MateyRequest request = new MateyRequest(Request.Method.POST, UrlData.REGISTER_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, context.getString(R.string.success_reg_message), Toast.LENGTH_LONG).show();

                        // Adding new account to AM
                        AccountManager am = AccountManager.get(context);
                        Account account = new Account(email, context.getString(R.string.account_type));
                        am.addAccountExplicitly(account, null, null);

                        // Updating UI
                        dismissProgressDialog(mProgDialog);
                        context.startRegReverseAnim();
                        context.mRegFormVisible = false;
                        context.etEmail.setText("");
                        context.etPass.setText("");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error while registering user.");

                        // Parsing an error
                        NetworkResponse response = error.networkResponse;
                        String errorData = new String(response.data);
                        int statusCode = response.statusCode;

                        if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                            try {
                                // See if this is an expected error from server, if not, use def action
                                String[] errorDesc = parseJsonError(errorData);
                                if (errorDesc[0].equals(MateyRequest.ErrorType.MERGE)) {
                                    dismissProgressDialog(mProgDialog);
                                    requestFbToken(context, context.getSecurePreferences(), errorDesc, context.etPass.getText().toString());
                                } else
                                    dismissProgressAndShowAlert(context, errorDesc[1]);
                            } catch (JSONException e) {
                                dismissProgressAndShowAlert(context);
                            }
                        } else
                            dismissProgressAndShowAlert(context);
                    }
                }
        );

        request.addParam(UrlData.PARAM_USER_FIRST_NAME, firstName);
        request.addParam(UrlData.PARAM_USER_LAST_NAME, lastName);
        request.addParam(UrlData.PARAM_EMAIL, email);
        request.addParam(UrlData.PARAM_PASSWORD, pass);
        mRequestQueue.add(request);
    }

    /**
     * Helper method for requesting fb access token and proceeding with the merge
     * @param context MainActivity context
     * @param securePreferences SecurePrefs object
     * @param errorDesc error object as string retrieved from the server
     * @param password user's password
     */
    private void requestFbToken(final MainActivity context, final SecurePreferences securePreferences, final String[] errorDesc, final String password) {
        Util.showTwoBtnAlertDialog(context, errorDesc[1], null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LoginManager.getInstance().logInWithReadPermissions(context,
                                Arrays.asList("public_profile"));
                        dialogInterface.dismiss();
                    }
                }, context.getString(R.string.positive_dialog_answer),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }, context.getString(R.string.negative_dialog_answer));


        LoginManager.getInstance().registerCallback(context.fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
                // Send access token req with default listeners
                showProgressDialog(context, context.getString(R.string.gettingIn_dialog_message));
                sendAccessTokenReq(context, "", "", accessToken.getToken(),
                        createDefaultResponseListener(context, securePreferences, accessToken.getToken(), context.etEmail.getText().toString()),
                        createDefaultErrorListener(context));
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                dismissProgressDialog(mProgDialog);
                Log.e(TAG, error.getLocalizedMessage(), error);
            }
        });
    }

    public void showProgressDialog(Context context, String message) {
        mProgDialog = new ProgressDialog(context);
        mProgDialog.setMessage(message);
        mProgDialog.show();
    }

    /**
     * Helper method for user login onto the server, also updates the UI
     *
     * @param email user's email address
     * @param pass  user's password
     * @param securePreferences the {@link SecurePreferences} instance used to store credentials
     * @param context the {@link MainActivity} context used to show and dismiss dialogs
     */
    public void loginWithVolley(final String email, String pass, final SecurePreferences securePreferences, final MainActivity context) {
        // Send access token request with default response listener
        showProgressDialog(context, context.getString(R.string.gettingIn_dialog_message));
        sendAccessTokenReq(context, securePreferences, pass, email, "");
    }

    /**
     * Method for registering and logging a user onto the server with facebook access token
     * @param fbAccessToken provided facebook access token
     * @param email the current user email address
     * @param securePreferences the {@link SecurePreferences} instance used for storing user credentials
     * @param context the {@link MainActivity} context
     */
    public void loginWithFacebook(final String fbAccessToken, final String email, final SecurePreferences securePreferences, final MainActivity context) {

        showProgressDialog(context, context.getString(R.string.gettingIn_dialog_message));
        sendAccessTokenReq(context, "", "", fbAccessToken,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parsing response.data
                        try {
                            parseOAuthResponse(response, securePreferences);
                        } catch (JSONException e){
                            Log.e(TAG.concat(" Unexpected error"), e.getLocalizedMessage(), e);
                            dismissProgressAndShowAlert(context);
                            return;
                        }
                        sendLoginReq(context, email, MotherActivity.device_id, MotherActivity.access_token);
                    }
                },
                // An error can be received if there's no connection to the server,
                // or the has already been registered on to the server so ask to merge accounts
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dismissProgressDialog(mProgDialog);

                        // Parsing an error
                        NetworkResponse response = error.networkResponse;
                        String errorData = new String(response.data);
                        int statusCode = response.statusCode;

                        if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                            try {
                                // See if this is an expected error from server, if not, use def action
                                String[] errorDesc = parseJsonError(errorData);
                                if (errorDesc[0].equals(MateyRequest.ErrorType.MERGE))
                                    requestToMerge(context, securePreferences, fbAccessToken, errorDesc[1], errorDesc[2]);
                            } catch (JSONException e) {
                                dismissProgressAndShowAlert(context);
                            }
                        } else
                            dismissProgressAndShowAlert(context);

                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }
        );
    }

    /** Helper method for sending a facebook merge request with std user, to the server
     *
     * @param context the MainActivity context to show dialogs in
     * @param securePreferences the {@link SecurePreferences} object to save access_token
     * @param fbAccessToken the facebook access token
     * @param mergeMessage the message to show to the user when asking to merge
     * @param emailToMergeWith email from account that is trying to merge with
     */
    private void requestToMerge(final MainActivity context, final SecurePreferences securePreferences, final String fbAccessToken, String mergeMessage, final String emailToMergeWith) {
        Util.showTwoBtnAlertDialog(context, mergeMessage, null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPassword();
                        dialog.dismiss();
                    }

                    private void requestPassword() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        // Set up the input
                        final EditText input = new EditText(context);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        int margin = Util.parseDp(16, context.getResources());
                        layoutParams.setMargins(margin, 0, margin, 0);
                        // Specify the type of input expected; sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String passwordString = input.getText().toString();
                                showProgressDialog(context, context.getString(R.string.gettingIn_dialog_message));
                                sendAccessTokenReq(context, securePreferences, passwordString, emailToMergeWith, fbAccessToken);
                                dialog.dismiss();
                            }
                        });
                        builder.setTitle(String.format(context.getString(R.string.enter_pass_title), emailToMergeWith));
                        builder.show();
                    }
                }, context.getString(R.string.positive_dialog_answer),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, context.getString(R.string.negative_dialog_answer)
        );
    }


    /**
     * Method for contacting OAuth2 server for access token request with default listeners
     * NOTE: Shows progress dialog
     *
     * @param context MainActivity context used for dialog control and data entries of tokens, ids..
     * @param securePreferences {@link SecurePreferences} object used for storing ACCESS_TOKEN for default listeners
     * @param passwordString password string param
     * @param email email string param
     * @param fbAccessToken facebook access token if needed
     */
    private void sendAccessTokenReq(final MainActivity context, final SecurePreferences securePreferences, String passwordString, final String email, @NonNull String fbAccessToken) {
        Response.Listener<String> listener = createDefaultResponseListener(context, securePreferences, fbAccessToken, email);
        Response.ErrorListener errorListener = createDefaultErrorListener(context);

        sendAccessTokenReq(context, passwordString, email, "", listener, errorListener);
    }

    /**
     * Method for creating default {@link com.android.volley.Response.Listener}, which saves UserProfile data retrieved from the server;
     * NOTE:  used for fb merge request and normal login process
     *
     * @param context MainActivity contest
     * @param securePreferences the secure prefs
     * @param fbAccessToken facebook access token; if this is empty, the response will proceed with normal login, otherwise offer to merge fb accounts
     * @param email user email address
     * @return newly created listener
     */
    private Response.Listener<String> createDefaultResponseListener(final MainActivity context, final SecurePreferences securePreferences, @NonNull final String fbAccessToken, final String email) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Parsing response.data
                    parseOAuthResponse(response, securePreferences);

                    // Proceeding further with login
                    // If there is fb access token, proceed with fb merge
                    if (fbAccessToken.length() != 0)
                        sendLoginReq(context, email, MotherActivity.device_id, MotherActivity.access_token, fbAccessToken);
                    else
                        sendLoginReq(context, email, MotherActivity.device_id, MotherActivity.access_token);
                } catch (JSONException e) {
                    Log.e(TAG, "Error retrieving access token: " + e.getLocalizedMessage(), e);
                    dismissProgressAndShowAlert(context);
                }
            }
        };
    }

    /**
     * Method for creating default {@link com.android.volley.Response.ErrorListener} which updates the UI
     * @param context the context to show dialogs in
     * @return newly created listener
     */
    private Response.ErrorListener createDefaultErrorListener(final MainActivity context) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error retrieving access token: " + error.getLocalizedMessage(), error);
                dismissProgressAndShowAlert(context);
            }
        };
    }

    /**
     * Method for sending login request to the server
     * @param context MainActivity context for UI control
     * @param email email string
     * @param deviceId deviceId retrieved from the server
     * @param accessToken accessToken retrieved from the server
     */
    private void sendLoginReq(final MainActivity context, String email, String deviceId, String accessToken) {
        sendLoginReq(context, email, deviceId, accessToken, "");
    }

    /**
     * Method for sending login request to the server
     * @param context MainActivity context for UI control
     * @param email email string
     * @param deviceId deviceId retrieved from the server
     * @param accessToken accessToken retrieved from the server
     */
    private void sendLoginReq(final MainActivity context, String email, String deviceId, String accessToken, @NonNull String fbAccessToken) {
        final SharedPreferences preferences = getDefaultSharedPreferences(context);
        // Immediately after contacting OAuth2 Server proceed to resource server for login
        // Creating new request for the resource server
        MateyRequest resRequest = new MateyRequest(Request.Method.POST, UrlData.LOGIN_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Parsing response.data
                try {
                    JSONObject object = new JSONObject(response);
                    parseUserDataAndLogin(context, DataManager.getInstance(context), preferences, object);
                    dismissProgressDialog(mProgDialog);
                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while trying to login the user: " + error.getLocalizedMessage(), error);
                dismissProgressAndShowAlert(context);
            }
        });
        // Setting request params and sending POST request
        resRequest.addParam(UrlData.PARAM_EMAIL, email);
        resRequest.addParam(UrlData.PARAM_DEVICE_ID, deviceId);
        resRequest.setAuthHeader(accessToken);
        if (!fbAccessToken.isEmpty())
            resRequest.addParam("fb_token", fbAccessToken);

        // Send to network
        mRequestQueue.add(resRequest);
    }

    /**
     * Method for contacting OAuth2 server for access token request;
     * NOTE: Shows progress dialog
     *
     * @param context MainActivity context used for dialog control and data entries of tokens, ids..
     * @param passwordString password string param
     * @param email email string param
     * @param fbAccessToken facebook access token if needed
     * @param listener the success callback to call when there was success response
     * @param errorListener the error callback listener to call when an error has occurred
     */
    private void sendAccessTokenReq(final MainActivity context,
                                    @NonNull String passwordString, @NonNull final String email, @NonNull String fbAccessToken, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        // First contacting OAuth2 server
        MateyRequest oauthRequest = new MateyRequest(Request.Method.POST, UrlData.OAUTH_LOGIN,
                listener, errorListener);

        // Contacting OAuth2 server with required params
        oauthRequest.addParam(UrlData.PARAM_CLIENT_ID, UrlData.PARAM_CLIENT_ID_VALUE);
        oauthRequest.addParam(UrlData.PARAM_CLIENT_SECRET, UrlData.PARAM_CLIENT_SECRET_VALUE);

        if (passwordString.isEmpty() && email.isEmpty()){
            oauthRequest.addParam(UrlData.PARAM_FBTOKEN, fbAccessToken);
            oauthRequest.addParam(UrlData.PARAM_GRANT_TYPE, UrlData.PARAM_GRANT_TYPE_SOCIAL);
        } else {
            oauthRequest.addParam(UrlData.PARAM_GRANT_TYPE, UrlData.PARAM_GRANT_TYPE_PASSWORD);
            oauthRequest.addParam(UrlData.PARAM_USERNAME, email);
            oauthRequest.addParam(UrlData.PARAM_PASSWORD, passwordString);
        }

        // Send to network
        mRequestQueue.add(oauthRequest);
    }

    /**
     * Parsing data and calling login process {@link #loggedIn(MainActivity)}
     * @see #parseUserData(DataManager, SharedPreferences, JSONObject)
     */
    private void parseUserDataAndLogin(MainActivity context, DataManager dataManager, SharedPreferences preferences, JSONObject object) throws JSONException{
        // Parse the data
        parseUserData( dataManager, preferences, object);

        // Check if the response object has suggested friends list
        if  (object.has(KEY_SUGGESTED_FRIENDS)) {
            LinkedList<UserProfile> list = new LinkedList<>();
            JSONArray array = object.getJSONArray(KEY_SUGGESTED_FRIENDS);
            UserProfile profile;
            for (int i = 0; i < array.length(); i++) {
                profile = new UserProfile();
                JSONObject profileObject = array.getJSONObject(i);
                profile.setUserId(profileObject.getLong(KEY_USER_ID));
                profile.setProfilePictureLink(profileObject.getString(KEY_PROFILE_PICTURE));
                profile.setFirstName(profileObject.getString(KEY_FIRST_NAME));
                profile.setLastName(profileObject.getString(KEY_LAST_NAME));
                list.add(profile);
            }
            // Add suggested friends list only in memory, not in db
            dataManager.setSuggestedFriends(list);
            // Login
            loggedInWithSuggestedFriends(context);
        } else
            loggedIn(context);
    }

    /** Method for parsing the user data retrieved from the server when trying to login
     *
     * @param dataManager the {@link DataManager} instance used to store data
     * @param preferences the {@link SharedPreferences} instance used to store data
     * @param object json object which contains {@link #KEY_FIRST_NAME}, {@link #KEY_LAST_NAME}, {@link #KEY_EMAIL}, {@link #KEY_PROFILE_PICTURE};
     * @throws JSONException the exception is thrown if json conversion fails
     */
    private void parseUserData( DataManager dataManager, SharedPreferences preferences, JSONObject object) throws JSONException{
        // Parsing user
        UserProfile userProfile = new UserProfile(object.getInt(KEY_USER_ID),
                object.getString(KEY_FIRST_NAME),
                object.getString(KEY_LAST_NAME),
                object.getString(KEY_EMAIL),
                object.getString(KEY_PROFILE_PICTURE));

        // Adding current user to the database
        dataManager.addUserProfile(userProfile);

        // Adding current user to the memory
        dataManager.setCurrentUserProfile(preferences, userProfile);
    }

    /** Method for retrieving error description message collected from the server
     *
     * @param errorData error string retrieved from the server
     * @return String[2] object, where String(0) = error type; String(1) = error description;
     * String(2) = error email;
     */
    private String[] parseJsonError(String errorData) throws JSONException {
        JSONObject jsonError = new JSONObject(errorData);

        return new String[] {jsonError.getString(MateyRequest.KEY_ERROR_TYPE),
                jsonError.getString(MateyRequest.KEY_ERROR_DESC),
                jsonError.getString(MateyRequest.KEY_ERROR_EMAIL)};
    }

    /** Method for parsing json response retrieved from the auth server;
     * Saves ACCESS_TOKEN to {@link SecurePreferences} and {@link MotherActivity#access_token}
     *
     * @param response string representation of json response
     */
    private void parseOAuthResponse(String response, SecurePreferences securePreferences) throws JSONException{
        JSONObject dataObj = new JSONObject(response);

        // Add data to secure prefs
        ArrayList<KVPair> list = new ArrayList<>();
        String accessToken = dataObj.getString(KEY_ACCESS_TOKEN);
        list.add(new KVPair(KEY_ACCESS_TOKEN, access_token));
        list.add(new KVPair(KEY_TOKEN_TYPE, dataObj.getString(KEY_TOKEN_TYPE)));

        securePreferences.putValues(list);
        MotherActivity.access_token = accessToken;
    }

    /** Helper method for dismissing progress dialog */
    private void dismissProgressDialog(ProgressDialog mProgDialog) {
        if (mProgDialog.isShowing())
            mProgDialog.dismiss();
    }

    /**
     * Helper method for dismissing progress dialog and showing alert on server error
     * @param context the context used for dialog control
     */
    private void dismissProgressAndShowAlert(MainActivity context){
        if (mProgDialog.isShowing())
            mProgDialog.dismiss();

        showServerAlert(context);
    }

    public void showServerAlert(MainActivity context){
        Util.showServerNotResponding(context,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * Helper method for dismissing progress dialog and showing alert on server error
     * @param context the context used for dialog control
     * @param message the message string
     */
    private void dismissProgressAndShowAlert(final MainActivity context, String message){
        if (mProgDialog.isShowing())
            mProgDialog.dismiss();

        Util.showAlertDialog(context, message, null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * Method to call when login process is finished;
     * If the user is already logged in, access_token, user_id and device_id are already stored in {@link MotherActivity}
     * @param context the context to use when starting new activity
     */
    private void loggedIn(MainActivity context) {
        startUploadService(context);

        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    /**
     * Method to call when login process is finished with the suggested friends list;
     * @param context the context used to start new activity
     */
    private void loggedInWithSuggestedFriends(MainActivity context) {
        startUploadService(context);
        Intent intent = new Intent(context, AddFriendsActivity.class);
        context.startActivity(intent);
        context.finish();
    }


    /**
     * Helper method for logging out from the app
     *
     * @param context           the {@link HomeActivity} context
     * @param securePreferences the SecuredPrefs user to clear user credentials
     */
    public void logout(HomeActivity context, SecurePreferences securePreferences) {

        clearUserCredentials(context, securePreferences);
        clearDatabase(context);
        sendLogOutReq(context, MotherActivity.device_id, MotherActivity.access_token);

        if (context.isDebug()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().remove("IS_DEBUG").remove("DATA_CREATED").apply();
        }

        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
        context.finish();
    }

    /**
     * Method for sending logout request on the server
     * @param context the context
     */
    private void sendLogOutReq(Context context, String deviceId, String accessToken) {
        MateyRequest logoutRequest = new MateyRequest(Request.Method.POST, UrlData.LOGOUT_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO - finish
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO - finish
                    }
                });
        // Setting request params and sending POST request
//        logoutRequest.addParam(UrlData.PARAM_EMAIL, email);
        logoutRequest.addParam(UrlData.PARAM_DEVICE_ID, deviceId);
        logoutRequest.setAuthHeader(accessToken);

        mRequestQueue.add(logoutRequest);
    }

    public static void clearUserCredentials(Context context, SecurePreferences securePreferences) {
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        DataManager dataManager = DataManager.getInstance(context);

        // Removing current user profile from db
        dataManager.removeUserProfile(preferences.getLong(DataManager.KEY_CUR_USER_ID, -1));

        // Removing current user  profile from prefs
        dataManager.setCurrentUserProfile(preferences, null);

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
        context.getContentResolver().delete(DataContract.NotUploadedEntry.CONTENT_URI, null, null);
    }



    ///////////////// DATA INTERNET METHODS ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /** Used for tagging network requests */
    private static volatile int TAG_COUNTER = 0;

    private static int DEFAULT_MAX_W = 512;
    private static int DEFAULT_MAX_H = 512;

    /**
     * Method for downloading and displaying image from provided link to the ImageView
     * @param ivProfilePic {@link ImageView} object to display downloaded image into
     * @param profilePictureLink the image url to download from
     * @return the newly created {@link Request} object with the tag {@link #TAG_COUNTER} incremented by 1;
     */
    public Request downloadImage(ImageView ivProfilePic, final String profilePictureLink) {

        int w = DEFAULT_MAX_W, h = DEFAULT_MAX_H;

        try {
            h = ivProfilePic.getHeight();
            w = ivProfilePic.getWidth();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        final WeakReference<ImageView> imageViewRef = new WeakReference<ImageView>(ivProfilePic);

        ImageRequest request = new ImageRequest(profilePictureLink,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        ImageView imageView = imageViewRef.get();
                        if (imageView != null)
                            imageView.setImageBitmap(bitmap);
                        TAG_COUNTER--;
                    }
                }, w, h, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error downloading image: " + error.getLocalizedMessage(), error);
                        TAG_COUNTER--;
                    }
                });
        addToRequestQueue(request);

        return request.setTag(TAG_COUNTER++);
    }


    /** Method for uploading failed data to the server
     *
     * @param context used to start the upload service if it isn't started
     */
    public void uploadFailedData(Context context) {
        Log.d(TAG, "Uploading failed data.");

        if (mUploadService != null && mConnection != null)
            mUploadService.uploadFailedData();
        else {
            startUploadService(context);
        }
    }

    /**
     * Helper method for uploading new bulletin to the server, also adds it to the database using dataManager
     * @param b the {@link Bulletin} to be uploaded
     * @param dataManager the {@link DataManager} instance used for adding bulletin to the database
     * @param context used to start the upload service if it isn't started
     * @param accessToken access token used to authorize with the server
     */
    public void uploadNewBulletin(Bulletin b, DataManager dataManager, String accessToken, Context context) {
        Log.d(TAG, "Posting new bulletin.");

        // First add the bulletin to the database then upload it to the server
        dataManager.addBulletin(b, STATUS_UPLOADING);

        if (mUploadService != null && mConnection != null)
            mUploadService.uploadBulletin(b, accessToken);
        else {
            dataManager.updateBulletinServerStatus(b, STATUS_RETRY_UPLOAD);
            startUploadService(context);
        }
    }

    /**
     * Helper method for uploading new reply to the server, also adds it to the database using dataManager
     * @param reply the {@link Reply} to be uploaded
     * @param dataManager the {@link DataManager} instance used for adding reply to the database
     * @param context used to start the upload service if it isn't started
     * @param accessToken access token used to authorize with the server
     */
    public void uploadNewReply(final Reply reply, final Bulletin currentBulletin, final DataManager dataManager, final String accessToken, final Context context) {
        Log.d(TAG, "Uploading new reply.");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                // First add the bulletin to the database then upload it to the server
                dataManager.addReply(reply, currentBulletin, STATUS_UPLOADING);

                if (mUploadService != null && mConnection != null)
                    mUploadService.uploadReply(reply, accessToken);
                else {
                    dataManager.updateReplyServerStatus(reply, STATUS_RETRY_UPLOAD);
                    // Update number of replies in bulletin db
                    dataManager.updateBulletinRepliesCount(currentBulletin.getPostID(), currentBulletin.getNumOfReplies()-1);
                }
            }
        };

        mExecutor.execute(r);
    }


    /**
     * Method for downloading and parsing news feed from the server, and all data around it
     * @param start the start position of the bulletin
     * @param count the total bulletin count that needs to be downloaded in a single burst
     * @param context the Context used for notifying when the parsing result is complete
     */
    public void downloadNewsFeed(int start, int count, Context context) {

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

        final WeakReference<Context> ref = new WeakReference<Context>(context);
        MateyRequest request = new MateyRequest(Request.Method.GET, url.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (ref.get() != null) {
                     new JSONParserAs(ref.get()).execute(response);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MateyRequest.TAG, error.getLocalizedMessage(), error);
            }
        });
        request.setAuthHeader(PARAM_AUTH_TYPE, String.format("Bearer %s", MotherActivity.access_token));

        mRequestQueue.add(request);
    }

    /**
     * Helper method for downloading news feed from the server to the database;
     * Downloads {@value DataManager#NUM_OF_BULLETINS_TO_DOWNLOAD} bulletins from the server;
     * Automatically determines from what bulletin position to download by calling {@link DataManager#getNumOfBulletinsInDb()}
     *
     * @param context the context of activity which is calling this method
     */
    public void downloadNewsFeed(final Context context) {
        int start = DataManager.getInstance(context).getNumOfBulletinsInDb();
        downloadNewsFeed(start, DataManager.NUM_OF_BULLETINS_TO_DOWNLOAD, context);
    }

    /**
     * Helper method used to upload followed friends list, by the current user, to the server;
     * @param addedFriends list of friends to be uploaded
     * @param accessToken access token used to authorise with the server
     * @param context used to start upload service if it isn't started
     */
    public void uploadFollowedFriends(ArrayList<UserProfile> addedFriends, String accessToken, Context context){
        Log.d(TAG, "Uploading added friends.");

        if (mUploadService != null && mConnection != null)
            mUploadService.uploadFollowedFriends(addedFriends, accessToken);
        else {
            // TODO - finish error nadling
            startUploadService(context);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////// GENERAL USE METHODS /////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method for starting {@link UploadService} used for uploading data to the server
     * @param c used to start the service
     */
    public void startUploadService(Context c) {
        if(!mIsBound) {
            Context context = c.getApplicationContext();
            Intent intent = new Intent(context, UploadService.class);
            context.startService(intent);
            mIsBound = context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else if (mUploadService == null){
            // If service gets destroyed in unusual way, just restart it
            Context context = c.getApplicationContext();
            Intent intent = new Intent(context, UploadService.class);
            context.startService(intent);
        }
    }

    /**
     * Method for stopping {@link UploadService} used for uploading data to the server
     * @param c used to stop the service
     */
    public void stopUploadService(Context c) {
        if (mIsBound) {
            Context context = c.getApplicationContext();
            context.unbindService(mConnection);
            Intent intent = new Intent(context, UploadService.class);
            context.stopService(intent);
            mIsBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
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
            // Before uploading anything, checking for mUploadService is needed;
            mUploadService = null;
            Log.e(TAG, "Service: " + arg0 + " - has been disconected");
        }
    };

    /** Method for stopping all pending and running downloads */
    public void stopAllNetworking(){
        mRequestQueue.cancelAll("");
    }

    /** Method for stopping all pending and running downloads */
    public void stopAllNetworking(Object tag){
        mRequestQueue.cancelAll(tag);
    }

    /**
     * Method for adding new {@link Request} to the current {@link RequestQueue} in use
     * @param req the request to be added
     */
    public void addToRequestQueue(Request req) {
        addToRequestQueue(req,"");
    }

    /**
     * Method for adding new {@link Request} to the current {@link RequestQueue} in use
     * @param req the request to be added
     * @param tag object to tag the network request
     */
    public void addToRequestQueue(Request req, Object tag) {
        req.setTag(tag);
        mRequestQueue.add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////// DEBUG/TEST //////////////////////////////////////////////////////////////////////////////////////

    public void debugLogin(SecurePreferences securePreferences, MainActivity context) {
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
        if (mProgDialog != null && mProgDialog.isShowing())
            mProgDialog.dismiss();

        // Close activity and proceed to HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        getDefaultSharedPreferences(context).edit().putBoolean("IS_DEBUG", true).commit();
        context.startActivity(intent);
        context.finish();
    }

    public void createDummyData(HomeActivity homeActivity) {
        DataManager dm = DataManager.getInstance(homeActivity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(homeActivity);

        if(!preferences.getBoolean("DATA_CREATED",false))
            dm.createDummyData();
    }


}

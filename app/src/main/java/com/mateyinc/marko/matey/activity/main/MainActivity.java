package com.mateyinc.marko.matey.activity.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.gcm.MateyGCMPreferences;
import com.mateyinc.marko.matey.gcm.RegistrationIntentService;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.MateyRequest;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.internet.procedures.FacebookLoginAs;
import com.mateyinc.marko.matey.internet.procedures.RegisterAs;
import com.mateyinc.marko.matey.model.KVPair;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mateyinc.marko.matey.gcm.MateyGCMPreferences.SENT_TOKEN_TO_SERVER;
import static com.mateyinc.marko.matey.internet.MateyRequest.KEY_ACCESS_TOKEN;
import static com.mateyinc.marko.matey.internet.MateyRequest.KEY_EXPIRES_IN;
import static com.mateyinc.marko.matey.internet.MateyRequest.KEY_REFRESH_TOKEN;
import static com.mateyinc.marko.matey.internet.MateyRequest.KEY_TOKEN_TYPE;
import static com.mateyinc.marko.matey.internet.MateyRequest.Method;
import static com.mateyinc.marko.matey.internet.MateyRequest.TOKEN_SAVED_TIME;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

    // device id, user id za login; registracija gcm id, username, pass, email..

    private static final int SHORT_ANIM_TIME = 150;
    private static final int MED_ANIM_TIME = 500;
    private static final int INTERMED_ANIM_TIME = 700;
    private static final int LONG_ANIM_TIME = 1000;
    private static final long SERVER_TIMER = 500;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = "com.matey.android";

    private ImageView ivLogoText, ivLoadingHead, ivLogoBubbleText, ivLogoClouds;
    private Button btnLogin, btnReg, btnFb;
    private RelativeLayout rlLoginButtons, rlMain;
    private LinearLayout llEmail, llPass, line1, line2;
    public EditText etPass;
    public AutoCompleteTextView etEmail;

    private float mLoginBtnMoveY;
    private float mRegBtnMoveY;
    private int mLoginBtnBotMargin;
    private boolean mLoginFormVisible;
    public boolean mRegFormVisible;
    private int mRegBtnBotMargin;
    private float mLogoMoveOnRegBtn;
    private float mLoadingHeadIntroTransl;

    private Resources resources;
    CallbackManager fbCallbackManager;

    // GCM
    private boolean isGcmReceiverRegistered;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String EXTRA_GCM_TOKEN = "extra_token";
    public static final String OLD_GCM_TOKEN = "old_token";
    public static final String NEW_GCM_TOKEN = "new_token";

    // Constants
    // The authority for the sync adapter's content provider
    public static String AUTHORITY;
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "proba";
    // The account name
    public static final String ACCOUNT = "email";
    // Instance fields
    Account mAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        fbCallbackManager = CallbackManager.Factory.create();
        facebookLogin();

        //TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        //Log.d("pnum", tMgr.getLine1Number());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        super.setSecurePreferences(this);

        // sceptic tommy starts checking everything
        initGCM();
//
//        super.startTommy();

        init();
    }

    /**
     * Initialize GCM broadcast receiver and register the device onto the server if it's not
     */
    private void initGCM() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                String token = intent.getStringExtra(EXTRA_GCM_TOKEN);


                // GCM complete with success
                if (token != null && token.length() != 0) {

                    // No old token found
                    if (sharedPreferences.getString(OLD_GCM_TOKEN, null) == null) {
                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        String url = UrlData.REGISTER_DEVICE;
                        // Request a string response from the provided URL.
                        MateyRequest stringRequest = new MateyRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    mServerReady = true;
                                    securePreferences.put(PREF_DEVICE_ID, object.getString(PREF_DEVICE_ID));
                                    Log.d(TAG, "Device id=" + object.getString(PREF_DEVICE_ID));
                                } catch (JSONException e) {
                                    sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                                    Log.e(TAG, e.getLocalizedMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                                Log.d(TAG, "Response error");
                                // TODO - error in response
                            }
                        });
                        stringRequest.addParam(UrlData.PARAM_NEW_GCM_ID, token);

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    } else {

                    }
                }
            }
        };

        registerGCMReceiver();
        registerDevice();
    }

    private void registerDevice() {

        if (checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);

            // TODO - rework the code on app upgrade to get new register
            if (!sentToken) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                intent.putExtra("device_id", securePreferences.getString("device_id"));
                startService(intent);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Registering BroadcastReceiver
        registerGCMReceiver();
    }

    private void registerGCMReceiver() {
        if (!isGcmReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(MateyGCMPreferences.REGISTRATION_COMPLETE));
            isGcmReceiverRegistered = true;
        }
    }


    private void init() {
        btnLogin = (ButtonLoginPage) findViewById(R.id.btnLogin);
        btnReg = (ButtonLoginPage) findViewById(R.id.btnReg);
        btnFb = (Button) findViewById(R.id.btnFacebook);
        ivLogoText = (ImageView) findViewById(R.id.ivLoginLogoText);
        llEmail = (LinearLayout) findViewById(R.id.llEmail);
        llPass = (LinearLayout) findViewById(R.id.llPass);
        line1 = (LinearLayout) findViewById(R.id.llLine1);
        line2 = (LinearLayout) findViewById(R.id.llLine2);
        rlLoginButtons = (RelativeLayout) findViewById(R.id.rlLoginButtons);
        ivLoadingHead = (ImageView) findViewById(R.id.ivLoadingHead);
        etEmail = (AutoCompleteTextView) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPass);
        ivLogoBubbleText = (ImageView) findViewById(R.id.ivLoginLogoBubble);
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        ivLogoClouds = (ImageView) findViewById(R.id.ivLoginLogoClouds);
        resources = getResources();
        AUTHORITY = getString(R.string.content_authority);
//        mAccount = createSyncAccount(this);

        startIntro();
        setOnClickListeners();
        setAutocompleteEmailValues();

    }

    private Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {

            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            return null;
        }

    }

    private void setOnClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mLoginFormVisible)
                    showLoginForm();
                else {

                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();

                    Bundle bundle = new Bundle();

                    if (!isValidEmailAddress(email)) {
                        bundle.putString("message", "Wrong email format. fon");
                        showDialog(1004, bundle);
                    } else if (!isValidPassword(pass)) {
                        bundle.putString("message", "Password needs to be at least 5 characters long.");
                        showDialog(1004, bundle);
                    } else {
                        loginWithVolley(email, pass);
                    }
                }
            }

            /**
             * Helper method for user login on to the server
             * @param email user's email address
             * @param pass user's password
             */
            private void loginWithVolley(final String email, String pass) {

                // Showing progress dialog
                final ProgressDialog mProgDialog = new ProgressDialog(MainActivity.this);
                mProgDialog.setMessage(MainActivity.this.getResources().getString(R.string.login_dialog_message));
                mProgDialog.show();

                // First contacting OAuth2 server
                final RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
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
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            preferences.edit().putLong(TOKEN_SAVED_TIME, System.currentTimeMillis()).apply();

                            // Immediately after contacting OAuth2 Server proceed to resource server for login
                            // Creating new request for the resource server
                            MateyRequest resRequest = new MateyRequest(Method.POST, UrlData.LOGIN_USER, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    JSONObject object;

                                    // Parsing response.data
                                    try {
                                        object = new JSONObject(response);

                                        // Adding current user to the database
                                        DataManager dataManager = DataManager.getInstance(MainActivity.this);
                                        dataManager.addUserProfile(new UserProfile(object.getInt(MateyRequest.KEY_USER_ID),
                                                object.getString(MateyRequest.KEY_FIRST_NAME),
                                                object.getString(MateyRequest.KEY_LAST_NAME),
                                                object.getString(MateyRequest.KEY_EMAIL),
                                                object.getString(MateyRequest.KEY_PROFILE_PICTURE)));

                                    } catch (JSONException e) {
                                        // TODO - finish error handling
                                        Log.e(TAG, e.getLocalizedMessage(), e);
                                        return;
                                    } catch (SecurityException se){
                                        Log.e(TAG, se.getLocalizedMessage(), se);
                                    }

                                    // Close progress dialog
                                    if (mProgDialog.isShowing())
                                        mProgDialog.dismiss();

                                    // Close activity and proceed to HomeActivity
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    MainActivity.this.startActivity(intent);
                                    MainActivity.this.finish();
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
                            resRequest.addParam(UrlData.PARAM_ACCESS_TOKEN, securePreferences.getString(PREF_DEVICE_ID));
                            queue.add(resRequest);

                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage(), e);
                            // TODO - finish error handling
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error while authenticating user.",error.getCause());
                        // TODO - finish method
                    }
                });

                // Contacting OAuth2 server with required params
                oauthRequest.addParam(UrlData.PARAM_GRANT_TYPE, UrlData.PARAM_GRANT_TYPE_PASSWORD);
                oauthRequest.addParam(UrlData.PARAM_CLIENT_ID, UrlData.PARAM_CLIENT_ID_VALUE);
                oauthRequest.addParam(UrlData.PARAM_CLIENT_SECRET, UrlData.PARAM_CLIENT_SECRET_VALUE);
                oauthRequest.addParam(UrlData.PARAM_USERNAME, email);
                oauthRequest.addParam(UrlData.PARAM_PASSWORD, pass);
                queue.add(oauthRequest);
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRegFormVisible)
                    showRegForm();
                else {
                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();

                    Bundle bundle = new Bundle();

                    if (!isValidEmailAddress(email)) {
                        bundle.putString("message", "Wrong email format. fon");
                        showDialog(1004, bundle);
                    } else if (!isValidPassword(pass)) {
                        bundle.putString("message", "Password needs to be at least 5 characters long.");
                        showDialog(1004, bundle);
                    } else {
                        registerWithVolley(email, pass);
                    }
                }
            }

            /**
             * Helper method for user registration on to the server
             * @param email user email address
             * @param pass user password
             */
            private void registerWithVolley(final String email, String pass) {
                // Showing progress dialog
                final ProgressDialog mProgDialog = new ProgressDialog(MainActivity.this);
                mProgDialog.setMessage(MainActivity.this.getResources().getString(R.string.registering_dialog_message));
                mProgDialog.show();

                // Making new request and contacting the server
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                MateyRequest request = new MateyRequest(Request.Method.POST, UrlData.REGISTER_USER, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, R.string.success_reg_message, Toast.LENGTH_SHORT).show();

                        if(mProgDialog.isShowing())
                            mProgDialog.dismiss();

                        // Adding new account to AM
                        AccountManager am = AccountManager.get(MainActivity.this);
                        Account account = new Account(email, getString(R.string.account_type));
                        am.addAccountExplicitly(account, null, null);

                        // Updating UI
                        startRegReverseAnim();
                        mRegFormVisible = false;
                        etEmail.setText("");
                        etPass.setText("");
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
                queue.add(request);
            }
        });

        btnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fbAnswerType = 0;
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

            }
        });

    }

    private void setAutocompleteEmailValues() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();

        Set<String> suggestedEmails = new HashSet<String>();

        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                suggestedEmails.add(account.name);
            }
        }

        etEmail.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(suggestedEmails)));
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidPassword(String password) {
        if (password.length() < 5) return false;
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    // when facebook login is done
    // this method will get on work
    protected void facebookLogin() {

        LoginManager.getInstance().registerCallback(fbCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        final AccessToken accessToken = loginResult.getAccessToken();
                        final Profile profile = Profile.getCurrentProfile();

                        // OVDE RADNJE NAKON LOGIN-A
                        if (fbAnswerType == 0) {

                            if (profile.getId() != null) {

                                FacebookLoginAs fbLogin = new FacebookLoginAs(MainActivity.this);
                                fbLogin.execute(accessToken.getToken(), profile.getId(), securePreferences.getString("device_id"));

                            }

                        } else {

                            String email = etEmail.getText().toString();
                            String pass = etPass.getText().toString();

                            // start asynchronous task to handle user registration
                            RegisterAs registerAs = new RegisterAs(MainActivity.this);
                            registerAs.execute(email, pass, "yes", accessToken.getToken());

                        }

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });

    }

    private void showLoginForm() {
        startFadeAnimation(btnReg, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(ivLogoBubbleText, 1, 0, SHORT_ANIM_TIME, false);

        showLoginFormAnim();
    }

    private void showRegForm() {
        startFadeAnimation(btnLogin, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(ivLogoBubbleText, 1, 0, SHORT_ANIM_TIME);
        showRegFormAnim();
    }

    // Animations
    //////////////////////////////////////////////////////////////////////////
    private void startIntro() {

        // Get view's h and w after its layout process then start anim
        final ViewTreeObserver viewTreeObserver = ivLoadingHead.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ivLoadingHead.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startIntroAnim(ivLoadingHead, 2, 1, 2, 1, 500);
                }
            });
        }


    }

    // Buttons anim
    private void showLoginFormAnim() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mLoginBtnMoveY = calculateLoginBtnTransl());
        animation.setDuration(500);
        animation.setFillAfter(false);
        animation.setAnimationListener(new LoginBtnDownAnimListener());
        mLoginBtnBotMargin = ((RelativeLayout.LayoutParams) btnLogin.getLayoutParams()).bottomMargin;
        btnLogin.startAnimation(animation);

        // For anim control, only animate once on button click if form is visible
        mLoginFormVisible = true;
    }

    private void startLoginReverseAnim() {
        startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

        // Return buttons to theirs original states
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mLoginBtnMoveY);
        animation.setDuration(500);
        animation.setFillAfter(false);
        animation.setAnimationListener(new LoginBtnUpAnimListener());

        btnLogin.startAnimation(animation);
    }

    private void showRegFormAnim() {
        btnReg.animate().setDuration(500).setListener(null)
                .yBy(mRegBtnMoveY = calculateRegBtnTransl());


        ivLoadingHead.animate().setInterpolator(new DecelerateInterpolator()).scaleX(0.75f).scaleY(0.75f).
                yBy(-resources.getDimension(R.dimen.anim_logo_transl_xy))
                .xBy(-resources.getDimension(R.dimen.anim_logo_transl_xy)).
                setDuration(MED_ANIM_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
                    }
                });

        // For anim control, only animate once on button click if form is visible
        mRegFormVisible = true;
    }


    public void startRegReverseAnim() {
        startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

        // Return buttons to theirs original states
        btnReg.animate().setDuration(500)
                .yBy(-mRegBtnMoveY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        btnFb.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);

                        startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(btnLogin, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(ivLogoBubbleText, 0, 1, SHORT_ANIM_TIME);

                    }
                });

        ivLoadingHead.animate().scaleX(1.25f).scaleY(1.25f)
                .yBy(resources.getDimension(R.dimen.anim_logo_transl_xy))
                .xBy(resources.getDimension(R.dimen.anim_logo_transl_xy))
                .setDuration(MED_ANIM_TIME)
                .setListener(null);

    }

    private float calculateLoginBtnTransl() {
        return btnFb.getTop() - btnLogin.getTop();
    }

    private float calculateRegBtnTransl() {
        return btnFb.getTop() - btnReg.getTop();
    }

    private float calculateLogoTransl() {
        return getResources().getDimension(R.dimen.anim_logo_transl_xy);
    }

    // Fade anim
    private void startFadeAnimation(final View view, final int from, final int to, int time) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new LinearInterpolator())
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (from > to)
                    view.setVisibility(View.GONE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startFadeAnimationWithDelay(final View view, final int from, final int to, int time) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new AccelerateInterpolator(1f))
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (from > to)
                    view.setVisibility(View.GONE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startFadeAnimation(final View view, final int from, final int to, int time, final boolean hide) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new LinearInterpolator())
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (hide)
                    view.setVisibility(View.GONE);
                else if (from > to)
                    view.setVisibility(View.INVISIBLE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    // Loading anim
    private void startIntroAnim(final View view, int fromX, int toX, int fromY, int toY, int time) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0);
        view.setScaleX(2f);
        view.setScaleY(2f);
//        AnimatorSet set = new AnimatorSet();
//        set
//                .play(ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1f))
//                .with(ObjectAnimator.ofFloat(view, View.SCALE_X, 2f, 1f))
//                .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 2f, 1f));
//        set.setInterpolator(new AccelerateInterpolator(2.5f));
//        set.setDuration(time);
//        set.start();

        view.animate().setDuration(time).setInterpolator(new AccelerateInterpolator(2.5f)).setListener(null)
                .alpha(1f).scaleX(1f).scaleY(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingHeadIntroTransl = ivLoadingHead.getTop();
                        goUpAnim(500);
                    }
                });

//        CountDownTimer timer = new CountDownTimer(time * 2, time) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//            }
//
//            @Override
//            public void onFinish() {
//                mLoadingHeadIntroTransl = ivLoadingHead.getTop();
//                goUpAnim(500);
//            }
//        };
//        timer.start();

    }

    private void goUpAnim(final int time) {
        ivLoadingHead.animate().setDuration(time).setInterpolator(new LinearInterpolator()).setStartDelay(100)
                .y(mLoadingHeadIntroTransl - 30)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivLoadingHead.clearAnimation();
                        if (!mServerReady) {
                            goDownAnim(700);
                        } else
                            endLoadingAnim();
                    }
                });
    }

    private void goDownAnim(final int time) {
        ivLoadingHead.animate().setDuration(time).setInterpolator(new BounceInterpolator()).setStartDelay(200)
                .y(mLoadingHeadIntroTransl + 60)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivLoadingHead.clearAnimation();
                        if (!mServerReady) {
                            goUpAnim(700);
                        } else
                            endLoadingAnim();
                    }
                });
    }

    private void endLoadingAnim() {
        // Return pic to center and  move layout
        // TODO - rework other anims via ViewPropertyAnimatior because it does not interfere with system transitions
        ivLoadingHead.animate().y(0)
                .setInterpolator(new OvershootInterpolator(2.2f))
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        startFadeAnimation(ivLogoClouds, 0, 1, MED_ANIM_TIME);
                        startFadeAnimation(ivLogoBubbleText, 0, 1, MED_ANIM_TIME);
                        startFadeAnimation(ivLogoText, 0, 1, MED_ANIM_TIME);

                        new CountDownTimer(900, 900) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                startFadeAnimation(rlLoginButtons, 0, 1, SHORT_ANIM_TIME);
                            }
                        }.start();

                    }
                });
    }
    //////////////////////////////////////////////////////////////////////////


    // Animation listeners
    //////////////////////////////////////////////////////////////////////////
    private class LoginBtnDownAnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            btnLogin.clearAnimation();

            // Set button position after anim
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
            layoutParams.setMargins(0, 0, 0, rlLoginButtons.getHeight() - btnFb.getBottom());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            btnLogin.setLayoutParams(layoutParams);

            startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

    }

    private class LoginBtnUpAnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            btnLogin.clearAnimation();
            btnFb.setVisibility(View.INVISIBLE);
            btnReg.setVisibility(View.INVISIBLE);

            // Set button position back to original layout position
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
            layoutParams.setMargins(0, 0, 0, mLoginBtnBotMargin);
            layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            btnLogin.setLayoutParams(layoutParams);

            startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(btnReg, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(ivLogoBubbleText, 0, 1, SHORT_ANIM_TIME);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
    //////////////////////////////////////////////////////////////////////////

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.error_tittle)
                        .setMessage(R.string.nogcm_message)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }
                        }).show();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tommy != null && tommy.getStatus() != AsyncTask.Status.FINISHED) {
            tommy.cancel(true);
        }

        unregisterGCMReceiver();
        Log.d(TAG, "entered onDestroy()");
        unbindDrawables(rlMain);
        System.gc();
    }

    private void unregisterGCMReceiver() {
        if (isGcmReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
            isGcmReceiverRegistered = false;
        }
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    // Controlling physical back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (llEmail.getVisibility() == View.VISIBLE && btnLogin.getVisibility() == View.VISIBLE) {
                startLoginReverseAnim();
                mLoginFormVisible = false;
                return true;
            } else if (llEmail.getVisibility() == View.VISIBLE && btnReg.getVisibility() == View.VISIBLE) {
                startRegReverseAnim();
                mRegFormVisible = false;
                return true;
            } else
                return super.onKeyDown(keyCode, event);

        }
        return super.onKeyDown(keyCode, event);
    }

}

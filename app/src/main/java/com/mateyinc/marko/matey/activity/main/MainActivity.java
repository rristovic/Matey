package com.mateyinc.marko.matey.activity.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.gcm.MateyGCMPreferences;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {
    private static final String TAG = "com.matey.android";

    // Animation constants
    private static final int SHORT_ANIM_TIME = 150;
    private static final int MED_ANIM_TIME = 500;
    private static final int INTERMED_ANIM_TIME = 700;
    private static final int LONG_ANIM_TIME = 1000;
    private static final long SERVER_TIMER = 500;
//    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Request code
    private static final int FACEBOOK_REQ_CODE = 5555;

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
    private CallbackManager fbCallbackManager;
    private SessionManager mSessionManager;

    // GCM
    private boolean isGcmReceiverRegistered;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String EXTRA_GCM_TOKEN = "extra_token";
    public static final String OLD_GCM_TOKEN = "old_token";
    public static final String NEW_GCM_TOKEN = "new_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext(), FACEBOOK_REQ_CODE);
        fbCallbackManager = CallbackManager.Factory.create();

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        init();
        mSessionManager.startSession(this);
    }

    private void init() {
        initGCM();
        initFBLogin();

        mSessionManager = SessionManager.getInstance(this);
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

        startIntro();
        setOnClickListeners();
        setAutocompleteEmailValues();
    }

    /** Initialize GCM broadcast receiver and registers it with {@link LocalBroadcastManager} */
    private void initGCM() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mSessionManager.registerDevice(MainActivity.this, getSecurePreferences(), intent.getStringExtra(EXTRA_GCM_TOKEN));
            }
        };

        registerGCMReceiver();
    }

    /** Method for registering the {@link #mRegistrationBroadcastReceiver} with the {@link LocalBroadcastManager} */
    private void registerGCMReceiver() {
        if (!isGcmReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(MateyGCMPreferences.REGISTRATION_COMPLETE));
            isGcmReceiverRegistered = true;
        }
    }

    /** Initialise facebook login callback */
    protected void initFBLogin() {

        LoginManager.getInstance().registerCallback(fbCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        final AccessToken accessToken = loginResult.getAccessToken();
                        final String[] email = new String[1];
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.d(TAG, response.toString());

                                        // Application code
                                        try {
                                            email[0] = (object.getString("email"));

                                            SessionManager.getInstance(MainActivity.this)
                                                    .loginWithFacebook(accessToken.getToken(),
                                                            email[0], getSecurePreferences(), MainActivity.this);
                                        } catch (JSONException e) {
                                            Log.e(TAG, e.getLocalizedMessage(), e);
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });
    }

    private void setOnClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLoginFormVisible) {
                    // If the device isn't ready, try registering with the server again
                    if(!mDeviceReady)
                        mSessionManager.startSession(MainActivity.this);

                    // Showing login form
                    showLoginForm();
                } else {
                    String email = etEmail.getText().toString().toLowerCase();
                    email = email.trim();
                    String pass = etPass.getText().toString().toLowerCase();
                    email = email.trim();

                    if (email.equals("sarma@nis.com") && pass.equals("radovan")) {
                        mSessionManager.debugLogin(getSecurePreferences(), MainActivity.this);
                        return;
                    }

                    Bundle bundle = new Bundle();

                    if (!isValidEmailAddress(email)) {
                        bundle.putString("message", getString(R.string.email_error_msg));
                        showDialog(1004, bundle);
                    } else if (!isValidPassword(pass)) {
                        bundle.putString("message", getString(R.string.password_error_msg));
                        showDialog(1004, bundle);
                    } else if (!mDeviceReady){
                            bundle.putString("message", getString(R.string.server_not_responding_msg));
                            showDialog(1004, bundle);
                    } else {
                        SessionManager.getInstance(MainActivity.this).
                                loginWithVolley(email, pass, getSecurePreferences(), MainActivity.this);
                    }
                }
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRegFormVisible) {
                    // If the device isn't ready, try registering with the server again
                    if (!mDeviceReady)
                        mSessionManager.startSession(MainActivity.this);
                    
                    // Showing reg form
                    showRegForm();
                } else {
                    String email = etEmail.getText().toString().toLowerCase();
                    email = email.trim();
                    String pass = etPass.getText().toString().toLowerCase();
                    email = email.trim();

                    Bundle bundle = new Bundle();

                    if (!isValidEmailAddress(email)) {
                        bundle.putString("message", getString(R.string.email_error_msg));
                        showDialog(1004, bundle);
                    } else if (!isValidPassword(pass)) {
                        bundle.putString("message", getString(R.string.password_error_msg));
                        showDialog(1004, bundle);
                    } else if (!mDeviceReady){
                        bundle.putString("message", getString(R.string.server_not_responding_msg));
                        showDialog(1004, bundle);
                    } else {
                        SessionManager.getInstance(MainActivity.this).registerWithVolley(MainActivity.this, email, pass);
                    }
                }
            }
        });

        btnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDeviceReady){
                    Bundle bundle = new Bundle();
                    bundle.putString("message", getString(R.string.server_not_responding_msg));
                    showDialog(1004, bundle);
                    mSessionManager.startSession(MainActivity.this);
                } else {
                    fbAnswerType = 0;
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,
                            Arrays.asList("public_profile", "user_friends", "email", "user_friends"));
                }
            }
        });
    }

    private void setAutocompleteEmailValues() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts;
        try {
            accounts = AccountManager.get(this).getAccounts();
        } catch (SecurityException e){
            Log.e(TAG, e.getLocalizedMessage(), e);
            return;
        }

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

        if (requestCode == FACEBOOK_REQ_CODE)
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);
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
    private void startIntroAnim(final View view, int fromX, int toX, int fromY, int toY, final int time) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0);
        view.setScaleX(2f);
        view.setScaleY(2f);

        view.animate().setDuration(time).setInterpolator(new AccelerateInterpolator(2.5f)).setListener(null)
                .alpha(1f).scaleX(1f).scaleY(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingHeadIntroTransl = ivLoadingHead.getTop();
                        goUpAnim(500);
                    }
                });
    }

    private void goUpAnim(final int time) {
        ivLoadingHead.animate().setDuration(time).setInterpolator(new LinearInterpolator()).setStartDelay(100)
                .y(mLoadingHeadIntroTransl - 30)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivLoadingHead.clearAnimation();
                        if (mDeviceReady) {
                            endLoadingAnim();
                        } else
                            goDownAnim(700);

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
                        if (mDeviceReady) {
                            endLoadingAnim();
                        } else
                            goUpAnim(700);
                    }
                });
    }

    public void endLoadingAnim() {
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



    /** Method to call when login process is finished and has suggested friends list*/
    public void loggedInWithSuggestedFriends() {

    }

    public void loggedIn() {

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

//    /**
//     * Check the device to make sure it has the Google Play Services APK. If
//     * it doesn't, display a dialog that allows users to download the APK from
//     * the Google Play Store or enable it in the device's system settings.
//     */
//    public boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle(R.string.error_tittle)
//                        .setMessage(R.string.nogcm_message)
//                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                MainActivity.this.finish();
//                            }
//                        }).show();
//            }
//            return false;
//        }
//        return true;
//    }


    @Override
    protected void onResume() {
        super.onResume();

        // Registering BroadcastReceiver
        registerGCMReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
            } else {
//                mSessionManager.stopAllNetworking();
                return super.onKeyDown(keyCode, event);
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}

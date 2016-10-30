package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.SessionManager;

public class HomeActivity extends MotherActivity implements View.OnTouchListener {

    private final static String TAG = HomeActivity.class.getSimpleName();

    private final static String BULLETIN_FRAG_TAG = "BULLETINS";
    private final static String NOTIF_FRAG_TAG = "NOTIFICATIONS";
    private final static String MESSAGES_FRAG_TAG = "MESSAGES";
    private final static String FRIENDS_FRAG_TAG = "FRIENDS";
    private final static String MENU_FRAG_TAG = "MENU";
    private final static String SEARCH_FRAG_TAG = "SEARCH";

    private SearchFragment mSearchFragment;
    private FragmentManager mFragmentManager;
    private BulletinsFragment mBulletinsFragment;
    private NotificationsFragment mNotificationsFragment;
    private MessagesFragment mMessagesFragment;
    private FriendsFragment mFriendsFragment;
    private MenuFragment mMenuFragment;
    private ImageButton ibHome, ibNotifications, ibMessages, ibFriends, ibMenu, ibSearch, ibProfile;
    private Toolbar toolbar;
    private SearchView searchView;
    private ImageView logo;

    /** Indicates if search view is visible or not */
    public boolean mSearchActive;

    /**
     * 0- Home; 1- Notifications; 2- Messages; 3- Friends; 4- Menu
     */
    private int mCurrentPage = 0;
    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "entered onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        getCurUser();
        getBulletins();
        uploadFailedData();
    }

    private void init() {
        // Test/debug
        ifTest();

        mSessionManager = SessionManager.getInstance(this);

        // Settings the app bar via custom toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ibHome = (ImageButton) findViewById(R.id.ibHome);
        // Change icon color for navigation
        ibHome.setColorFilter(getResources().getColor(R.color.app_bar_background));

        ibSearch = (ImageButton) findViewById(R.id.ibSearch);
        ibProfile = (ImageButton) findViewById(R.id.ibProfile);
        ibFriends = (ImageButton) findViewById(R.id.ibFriends);
        ibMenu = (ImageButton) findViewById(R.id.ibMenu);
        ibMessages = (ImageButton) findViewById(R.id.ibMessages);
        ibNotifications = (ImageButton) findViewById(R.id.ibNotifications);
        setListeners();

        // Adding Bulletins fragment to home layout on start
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mBulletinsFragment = new BulletinsFragment();
        fragmentTransaction.replace(R.id.homeContainer, mBulletinsFragment, BULLETIN_FRAG_TAG);
        fragmentTransaction.commit();

    }

    private void setListeners() {
        ibSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSearchActive = true;

                // Removing other views
                logo = (ImageView) toolbar.findViewById(R.id.ivHomeLogo);
                logo.setVisibility(View.GONE);
                ibSearch.setVisibility(View.GONE);
                ibProfile.setVisibility(View.GONE);

                // Adding search view
                searchView = new SearchView(HomeActivity.this);
                searchView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                searchView.setIconified(false);

                // Setting search view style
                try {
                    SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
                    searchAutoComplete.setHintTextColor(Color.BLACK);
                    searchAutoComplete.setBackgroundColor(Color.WHITE);
                    searchAutoComplete.setTextColor(Color.BLACK);
                    View searchplate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
                    searchplate.setBackgroundColor(Color.TRANSPARENT);
                    ImageView searchCloseIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                    searchCloseIcon.setColorFilter(Color.WHITE);
                    ImageView voiceIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_voice_btn);
                    voiceIcon.setColorFilter(Color.WHITE);
                    ImageView searchIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
                    searchIcon.setColorFilter(Color.WHITE);
                } catch (Exception e) {
                    searchView.setBackgroundColor(Color.WHITE);
                }

                toolbar.addView(searchView);

                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        closeSearchView();
                        return true;
                    }
                });

                // Adding search fragment
                if (mSearchFragment == null)
                    mSearchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.homeContainer, mSearchFragment).commit();

            }
        });

        ibProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(i);
            }
        });

        ibHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 0;
                changeNavIconColor();

                if (mBulletinsFragment == null) {
                    mBulletinsFragment = new BulletinsFragment();
                }
                mFragmentManager.beginTransaction().replace(
                        R.id.homeContainer, mBulletinsFragment, BULLETIN_FRAG_TAG
                ).commit();
            }
        });
        ibHome.setOnTouchListener(this);

        ibNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 1;
                changeNavIconColor();

                if (mNotificationsFragment == null) {
                    mNotificationsFragment = new NotificationsFragment();
                }
                mFragmentManager.beginTransaction().replace(
                        R.id.homeContainer, mNotificationsFragment, NOTIF_FRAG_TAG
                ).commit();
            }
        });
        ibNotifications.setOnTouchListener(this);

        ibMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 2;
                changeNavIconColor();

                if (mMessagesFragment == null) {
                    mMessagesFragment = new MessagesFragment();
                }
                mFragmentManager.beginTransaction().replace(
                        R.id.homeContainer, mMessagesFragment, MESSAGES_FRAG_TAG
                ).commit();
            }
        });
        ibMessages.setOnTouchListener(this);

        ibFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 3;
                changeNavIconColor();

                if (mFriendsFragment == null) {
                    mFriendsFragment = new FriendsFragment();
                }
                mFragmentManager.beginTransaction().replace(
                        R.id.homeContainer, mFriendsFragment, FRIENDS_FRAG_TAG
                ).commit();
            }
        });
        ibFriends.setOnTouchListener(this);

        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 4;
                changeNavIconColor();

                if (mMenuFragment == null) {
                    mMenuFragment = new MenuFragment();
                }
                mFragmentManager.beginTransaction().replace(
                        R.id.homeContainer, mMenuFragment, MENU_FRAG_TAG
                ).commit();
            }
        });
        ibMenu.setOnTouchListener(this);

    }

    /** Helper method for getting the current user profile in {@link DataManager#mCurrentUserProfile} */
    private void getCurUser() {
        if (!DataManager.getInstance(this).setCurrentUserProfile(PreferenceManager.getDefaultSharedPreferences(this))) {
            mSessionManager.logout(this, getSecurePreferences());
        }
    }

    /** Helper method for downloading bulletin news feed */
    private void getBulletins() {
        if (isDebug()) {
            mSessionManager.createDummyData(this);
        } else
            mSessionManager.getNewsFeed(this);
    }

    /** Helper method for uploading data that has failed to upload */
    private void uploadFailedData(){
        mSessionManager.uploadFailedData(HomeActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "entered onResume()");
        Log.d(TAG,"Access token:" + access_token);

        // Settings current user profile and access token in memory
        if (mSessionManager == null) {
            mSessionManager = SessionManager.getInstance(this);
        }
//            mSessionManager.setAccessToken(getSecurePreferences().getString(SessionManager.KEY_ACCESS_TOKEN));
//        } else if (mSessionManager.getAccessToken().isEmpty())
//            mSessionManager.setAccessToken(getSecurePreferences().getString(SessionManager.KEY_ACCESS_TOKEN));

        if (!DataManager.getInstance(this).setCurrentUserProfile(PreferenceManager.getDefaultSharedPreferences(this)))
            mSessionManager.logout(this, getSecurePreferences());
    }

    private void closeSearchView() {
        mSearchActive = false;
        toolbar.removeView(searchView);
        logo.setVisibility(View.VISIBLE);
        ibSearch.setVisibility(View.VISIBLE);
        ibProfile.setVisibility(View.VISIBLE);

        // Closing keyboard. Check if no view has focus:
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        // Close search fragment
        getSupportFragmentManager().popBackStack();
    }

    private void changeNavIconColor() {
        ibHome.setColorFilter(null);
        ibFriends.setColorFilter(null);
        ibMenu.setColorFilter(null);
        ibMessages.setColorFilter(null);
        ibNotifications.setColorFilter(null);

        switch (mCurrentPage) {
            case 0:
                ibHome.setColorFilter(getResources().getColor(R.color.app_bar_background));
                break;
            case 1:
                ibNotifications.setColorFilter(getResources().getColor(R.color.app_bar_background));
                break;
            case 2:
                ibMessages.setColorFilter(getResources().getColor(R.color.app_bar_background));
                break;
            case 3:
                ibFriends.setColorFilter(getResources().getColor(R.color.app_bar_background));
                break;
            case 4:
                ibMenu.setColorFilter(getResources().getColor(R.color.app_bar_background));
                break;
        }

        // Also close search view if active
        if (mSearchActive)
            closeSearchView();
    }


    // Controlling back button; Before v2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back to first tab if it's not selected, otherwise quit the app
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurrentPage != 0) {
            mCurrentPage = 0;
            changeNavIconColor();

            if (mBulletinsFragment == null) {
                mBulletinsFragment = new BulletinsFragment();
            }
            mFragmentManager.beginTransaction().replace(
                    R.id.homeContainer, mBulletinsFragment, BULLETIN_FRAG_TAG
            ).commit();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && mSearchActive) {
            closeSearchView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Coloring buttons programmatically instead of in XML
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageButton button;
        try {
            button = (ImageButton) v;
        } catch (ClassCastException e) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(getApplicationContext().getResources().getColor(R.color.colorAccent)); // White Tint
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                button.setColorFilter(getApplicationContext().getResources().getColor(R.color.light_gray)); // White Tint
                return false;

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        SessionManager.getInstance(this).stopUploadService(this);
        super.onDestroy();
    }

    //////////////////////////////////////////////////////////////////////
    private boolean mIsDebug;

    /**
     * Testing/debugging
     */
    private void ifTest() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("IS_DEBUG", false)) {
            mIsDebug = true;
        }
    }

    /**
     * Testing/debugging
     *
     * @return true if it's debug mode, false otherwise
     */
    public boolean isDebug() {
        return mIsDebug;
    }
}

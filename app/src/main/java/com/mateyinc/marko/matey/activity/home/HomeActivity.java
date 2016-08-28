package com.mateyinc.marko.matey.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.inall.InsideActivity;
import com.mateyinc.marko.matey.internet.home.BulletinAs;
import com.mateyinc.marko.matey.model.Bulletin;

public class HomeActivity extends InsideActivity implements BulletinsFragment.OnListFragmentInteractionListener {


    private FragmentManager mFragmentManager;
    private BulletinsFragment mBulletinsFragment;
    private NotificationsFragment mNotificationsFragment;
    private MessagesFragment mMessagesFragment;
    private FriendsFragment mFriendsFragment;
    private MenuFragment mMenuFragment;
    private ImageButton ibHome, ibNotifications, ibMessages, ibFriends, ibMenu;

    /**
     * 0- Home; 1- Notifications; 2- Messages; 3- Friends; 4- Menu
     */
    private int mCurrentPage = 0;

    private final static String BULLETIN_FRAG_TAG = "BULLETINS";
    private final static String NOTIF_FRAG_TAG = "NOTIFICATIONS";
    private final static String MESSAGES_FRAG_TAG = "MESSAGES";
    private final static String FRIENDS_FRAG_TAG = "FRIENDS";
    private final static String MENU_FRAG_TAG = "MENU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        super.setSecurePreferences(this);

        init();
        getBulletins();
    }

    private void getBulletins() {
        // Getting posts for the user
        BulletinAs bulletinsAs = new BulletinAs(this);
        bulletinsAs.execute(securePreferences.getString("user_id"),
                securePreferences.getString("uid"),
                securePreferences.getString("device_id"));
    }

    private void init() {
        // Settings the app bar via custom toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ibHome = (ImageButton) findViewById(R.id.ibHome);
        // Change icon color for navigation
        ibHome.setColorFilter(getResources().getColor(R.color.app_bar_background));

        ibFriends = (ImageButton) findViewById(R.id.ibFriends);
        ibMenu = (ImageButton) findViewById(R.id.ibMenu);
        ibMessages = (ImageButton) findViewById(R.id.ibMessages);
        ibNotifications = (ImageButton) findViewById(R.id.ibNotifications);
        setClickListeners();

        // Adding Bulletins fragment to home layout on start
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mBulletinsFragment = new BulletinsFragment();
        fragmentTransaction.replace(R.id.homeContainer, mBulletinsFragment, BULLETIN_FRAG_TAG);
        fragmentTransaction.commit();


    }

    private void setClickListeners() {
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
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onListFragmentInteraction(Bulletin item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
}

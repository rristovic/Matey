package com.mateyinc.marko.matey.activity.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.profile.UserProfileAs;
import com.mateyinc.marko.matey.model.UserProfile;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.lang.ref.WeakReference;

public class ProfileActivity extends MotherActivity {
    public static final String PROFILE_DOWNLOADED = "com.mateyinc.marko.matey.activity.profile.profile_downloaded";
    private TextView tvName;
    private ImageView ivProfilePic;
    private String mUserId;
    private ImageLoader mImageLoader;
    private UserProfile mUserProfile;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setSecurePreferences(this);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    private void init() {
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvName = (TextView) findViewById(R.id.ivName);
        mUserProfile = new UserProfile();
        initUIL();

        if (getIntent().hasExtra("user_id"))
            mUserId = getIntent().getExtras().getString("user_id");

        UserProfileAs userProfileAsAs = new UserProfileAs(this, new WeakReference<>(mUserProfile));
        userProfileAsAs.execute(securePreferences.getString("user_id"),
                securePreferences.getString("uid"),
                securePreferences.getString("device_id")
                , mUserId);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setData();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(PROFILE_DOWNLOADED));

    }

    @Override
    protected void onPause() {
        if (mBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void initUIL() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .diskCacheSize(50 * 1024 * 1024)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();
    }

    private void setData() {
        Log.d("ProfileActivity","Data is set.");
        mImageLoader.displayImage(mUserProfile.getProfilePictureLink(), ivProfilePic);
        tvName.setText(mUserProfile.getFirstName() + " " + mUserProfile.getLastName());
    }

}

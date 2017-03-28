package com.mateyinc.marko.matey.inall;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.storage.SecurePreferences;

abstract public class MotherActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    // Static params used for session management
    /**
     * Id of current user
     */
    public static long user_id;
    /**
     * Id of the device retrieved from the server
     */
    public static String device_id;
    /**
     * Access token used to authorise with the server
     */
    public static String access_token;
    /**
     * {@link UserProfile} of current user
     **/
    public static UserProfile mCurrentUserProfile;


    private final Object mLock = new Object();
    private SecurePreferences mSecurePreferences;

    /**
     * True if GCM_token and device_id are present on the the device thus
     * indicating if the device is registered on the server
     */
    public boolean mDeviceReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public SecurePreferences getSecurePreferences() {
        synchronized (mLock) {
            if (mSecurePreferences == null) {
                mSecurePreferences = ((MyApplication) getApplication()).getSecurePreferences();
            }
            return mSecurePreferences;
        }
    }

    protected void setSupportActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected void setChildSupportActionBar() {
        setChildSupportActionBar(false);
    }

    /**
     * For activities that uses childActionBar and that have back button
     */
    protected void setChildSupportActionBar(boolean transculentStatusBar) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (transculentStatusBar) {
            int result = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
            if (result > 0) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                toolbar.setPadding(0, result, 0, 0);
                ViewGroup.LayoutParams params = toolbar.getLayoutParams();
                params.height += result;
            }
        }
    }

    protected void setTranslucentStatusBar(Window window) {
        if (window == null) return;
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(window);
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setTranslucentStatusBarLollipop(Window window) {
        window.setStatusBarColor(
                window.getContext()
                        .getResources()
                        .getColor(android.R.color.transparent));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentStatusBarKiKat(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }



//    public void updateToolbarBehaviour(LinearLayoutManager layoutManager, AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout) {
//        if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
//            turnOffToolbarScrolling(appBarLayout, collapsingToolbarLayout);
//        } else {
//            turnOnToolbarScrolling(appBarLayout, collapsingToolbarLayout);
//        }
//    }
//
//    private void turnOffToolbarScrolling(AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout) {
//        //turn off scrolling
//        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
//        toolbarLayoutParams.setScrollFlags(0);
//        collapsingToolbarLayout.setLayoutParams(toolbarLayoutParams);
//
//        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
//        appBarLayoutParams.setBehavior(null);
//        appBarLayout.setLayoutParams(appBarLayoutParams);
//    }
//
//    private void turnOnToolbarScrolling(AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout) {
//        //turn on scrolling
//        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
//        toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
//        collapsingToolbarLayout.setLayoutParams(toolbarLayoutParams);
//
//        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
//        appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
//        appBarLayout.setLayoutParams(appBarLayoutParams);
//    }
}

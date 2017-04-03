package com.mateyinc.marko.matey.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.utils.EndlessScrollListener;
import com.mateyinc.marko.matey.adapters.ProfileFollowersAdapter;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadTempListEvent;
import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.UserProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FollowersActivity extends MotherActivity {
    private static final String TAG = FollowersActivity.class.getSimpleName();

    /**
     * Intent action - download and display followers list
     */
    public static final String ACTION_FOLLOWERS = "followers";
    /**
     * Intent action - download and display folliwing profiles list
     */
    public static final String ACTION_FOLLOWING = "following";
    public static final String EXTRA_PROFILE_ID = "user_id";

    private String mAction;
    private long mUserId;
    private EndlessScrollListener mScrollListener;
//    private ProgressBar mProgress;
    private RecyclerView rvList;
    private ProfileFollowersAdapter mAdapter;
    private TextView tvHeading;
    private OperationManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        setChildSupportActionBar();
        init();
        getData();
    }

    private void init() {
        setSupportActionBar();

        Intent i = getIntent();
        if (i.hasExtra(EXTRA_PROFILE_ID)) {
            mUserId = i.getLongExtra(EXTRA_PROFILE_ID, -1);
        } else {
            Log.e(TAG, "Failed to parse user id. Finishing activity..");
            finish();
            return;
        }
        mAction = i.getAction();

        mManager = OperationManager.getInstance(this);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        rvList = (RecyclerView) findViewById(R.id.rvList);
//        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FollowersActivity.this);
        rvList.setLayoutManager(layoutManager);
        mAdapter = new ProfileFollowersAdapter(this);
        rvList.setAdapter(mAdapter);

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (mAction.equals(ACTION_FOLLOWERS))
                    mManager.downloadUserFollowers(mUserId, false, FollowersActivity.this);
                else
                    mManager.downloadUserFollowingList(mUserId, false, FollowersActivity.this);
            }
        };
        rvList.addOnScrollListener(mScrollListener);
    }

    public void getData() {
        if (mAction.equals(ACTION_FOLLOWERS))
            mManager.downloadUserFollowers(mUserId, true, this);
        else
            mManager.downloadUserFollowingList(mUserId, true, this);

//        if (mScrollListener != null)
//            mScrollListener.mLoading = true;
//        int offset = 0;
//        int count = 20;

//        OperationManager manager = OperationManager.getInstance(FollowersActivity.this);
//        manager.addSuccessListener(new Response.Listener<String>() {
//            @Override
//            public void onResponse(final String response) {
//                if (mScrollListener != null)
////                    mScrollListener.mLoading = false;
//                OperationManager.getInstance(FollowersActivity.this).submitRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        final LinkedList<Object[]> list = parseData(response);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mProgress != null)
//                                    mProgress.setVisibility(View.GONE);
//                                setData(list);
//                            }
//                        });
//                    }
//                });
//            }
//        });
//        manager.addErrorListener(new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (mScrollListener != null)
//                    mScrollListener.mLoading = false;
//            }
//        });
//
//        manager.downloadFollowers(offset, count,
//                getIntent().getLongExtra(EXTRA_PROFILE_ID, -1), FollowersActivity.this);

//        Intent i = getIntent();
//        if (i.getAction().equals(ACTION_FOLLOWERS)) {
//            downloadFollowers.startDownloadAction(
//                    UserProfileOp.getFollowersListAction(i.getLongExtra(EXTRA_PROFILE_ID, -1), offset, limit)
//            );
//
//            // Setting header
//            char[] text = getString(R.string.followers_label).toCharArray();
//            text[0] = Character.toUpperCase(text[0]);
//            String label = new String(text);
//            tvHeading.setText(label);
//        }
//        else {
//            downloadFollowers.startDownloadAction(
//                    UserProfileOp.getFollowingListAction(i.getLongExtra(EXTRA_PROFILE_ID, -1), offset, limit));
//
//            // Setting header
//            char[] text = getString(R.string.following_label).toCharArray();
//            text[0] = Character.toUpperCase(text[0]);
//            String label = new String(text);
//            tvHeading.setText(label);
//        }
//
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListDownloaded(DownloadTempListEvent<UserProfile> event) {
        if (event.mEventType.equals(OperationType.DOWNLOAD_FOLLOWERS) && event.isSuccess){
            mAdapter.loadData(event.mDao);
        } else if (event.mEventType.equals(OperationType.DOWNLOAD_FOLLOWING) && event.isSuccess){
            mAdapter.loadData(event.mDao);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}

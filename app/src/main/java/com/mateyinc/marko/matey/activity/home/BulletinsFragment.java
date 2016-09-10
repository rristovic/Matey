package com.mateyinc.marko.matey.activity.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.BulletinRecyclerViewAdapter;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.internet.home.BulletinAs;

public class BulletinsFragment extends Fragment {

    private Context mContext;
    private BulletinRecyclerViewAdapter mAdapter;
    private BroadcastReceiver mDataDownloaded;
    private RecyclerView mRecycleView;
    private EndlessScrollListener mScrollListener;


    public static int updatedPos = -1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BulletinsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulletins, container, false);
        mAdapter = new BulletinRecyclerViewAdapter(mContext, (RecyclerView) view);

        // Set the adapter
        Context context = view.getContext();
        mRecycleView = (RecyclerView) view;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecycleView.setLayoutManager(layoutManager);
        if (HomeActivity.mListDownloaded && null == mRecycleView.getAdapter()) // If data is already downloaded, set the list, if not it will be set in broadcast receiver
            mRecycleView.setAdapter(mAdapter);

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Getting posts for the user
                BulletinAs bulletinsAs = new BulletinAs((HomeActivity) mContext);
                bulletinsAs.execute(Integer.toString(HomeActivity.mCurUser.getUserId()),
                        "securePreferences.getString(uid)",
                        "securePreferences.getString(device_id)",
                        Integer.toString(BulletinManager.mCurrentPage),
                        "false");
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BulletinsFragment", "onResume is called.");

        if (updatedPos != -1) { // If some item is updated, update UI based on position
            mAdapter.notifyItemChanged(updatedPos);
            updatedPos = -1;
        }

        if (HomeActivity.mListDownloaded && null == mRecycleView.getAdapter()) // If data is already downloaded, set the list, if not it will be set in broadcast receiver
            mRecycleView.setAdapter(mAdapter);

        mRecycleView.addOnScrollListener(mScrollListener); // Settings list scroll listener

        // Whole list downloaded broadcast
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDataDownloaded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (null == mRecycleView.getAdapter()) // Settings adapter if it wasn't set in initial state
                    mRecycleView.setAdapter(mAdapter);
                BulletinManager.mCurrentPage++; // Update curPage

                int itemDownloadedCount = intent.getIntExtra(BulletinManager.EXTRA_ITEM_DOWNLOADED_COUNT, 0);
                mAdapter.notifyItemRangeInserted(mAdapter.getItemCount() - itemDownloadedCount, itemDownloadedCount);
                mScrollListener.mLoading = false;

                Log.d("BulletinsFragment", "Bulletin downloaded broadcast received. Current page=" + BulletinManager.mCurrentPage);
            }
        }, new IntentFilter(BulletinManager.BULLETIN_LIST_LOADED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDataDownloaded);
        mRecycleView.removeOnScrollListener(mScrollListener); // Removing scroll listener

        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

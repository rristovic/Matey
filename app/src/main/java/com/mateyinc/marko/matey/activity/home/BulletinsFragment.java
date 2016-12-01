package com.mateyinc.marko.matey.activity.home;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.BulletinsAdapter;
import com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.SessionManager;

import java.util.ArrayList;
import java.util.Iterator;

import static com.mateyinc.marko.matey.data.DataManager.BULLETIN_COLUMNS;

public class BulletinsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private HomeActivity mContext;
    private BulletinsAdapter mAdapter;
    private BroadcastReceiver mDataDownloaded;
    private RecyclerView mRecycleView;
    private EndlessScrollListener mScrollListener;

    /**
     * The list which hold the updated bulletin positions;
     * Used to notify adapter about update;
     */
    public static ArrayList<Integer> mUpdatedPositions = new ArrayList<>();

    private SessionManager mSessionManager;
    public static final int BULLETINS_LOADER = 100;
    private LinearLayout llNoData;
    private LinearLayout rlNewPostView;
    private ProgressBar mProgressBar;
    private FrameLayout mMainFeedLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BulletinsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (HomeActivity) context;
        mSessionManager = SessionManager.getInstance(context);

        getLoaderManager().initLoader(BULLETINS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainFeedLayout = (FrameLayout) inflater.inflate(R.layout.fragment_bulletins, container, false);

        llNoData = (LinearLayout) mMainFeedLayout.findViewById(R.id.llNoData);
        rlNewPostView = (LinearLayout) mMainFeedLayout.findViewById(R.id.rlNewPostView);
        rlNewPostView.removeViewAt(1);
        mProgressBar = (ProgressBar) mMainFeedLayout.findViewById(R.id.pbDataLoading);

        mRecycleView = new RecyclerView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRecycleView.setLayoutParams(params);
        mAdapter = new BulletinsAdapter(mContext);
        // Set the adapter
        Context context = mMainFeedLayout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setAdapter(mAdapter);



        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mSessionManager.downloadNewsFeed(mContext);
            }
        };

        return mMainFeedLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BulletinsFragment", "onResume is called.");

        // If items are updated, notify adapter
        if (mUpdatedPositions.size() != 0) {
            Iterator i = mUpdatedPositions.iterator();
            while(i.hasNext()){
                int pos = (Integer)i.next();
                mAdapter.notifyItemChanged(pos);
                i.remove();
            }
        }

        // Settings list scroll listener
        mRecycleView.addOnScrollListener(mScrollListener);

        // Whole list downloaded broadcast
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDataDownloaded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DataManager.mCurrentPage++; // Update curPage
                mScrollListener.mLoading = false;
                Log.d("BulletinsFragment", "Bulletin downloaded broadcast received. Current page=" + DataManager.mCurrentPage);
            }
        }, new IntentFilter(DataManager.BULLETIN_LIST_LOADED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDataDownloaded);
        mRecycleView.removeOnScrollListener(mScrollListener); // Removing scroll listener

        super.onPause();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case BulletinsAdapter.COPYTEXT_ID:
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.clipboard_copiedtext_label), BulletinsAdapter.clickedText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, R.string.toast_textcopied, Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                BulletinEntry.CONTENT_URI,
                BULLETIN_COLUMNS,
                null,
                null,
                BulletinEntry.COLUMN_DATE + BulletinEntry.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateEmptyView(data.getCount());
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Show an 'empty' view when there's no data to display
     */
    private void updateEmptyView(int dataCount) {
        if (dataCount == 0) {
            if (mMainFeedLayout.getChildAt(0) instanceof RelativeLayout) {
                mMainFeedLayout.removeAllViews();

                mMainFeedLayout.addView(llNoData);
            }
            // TODO - finish error loading data
        } else {
            // There is data to display, show RecycleView instead of empty view
            if (mMainFeedLayout.getChildAt(0) instanceof LinearLayout) {
                mMainFeedLayout.removeAllViews();
                mMainFeedLayout.addView(mRecycleView);
            }
        }
    }
}

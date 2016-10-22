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
import android.widget.TextView;
import android.widget.Toast;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.BulletinsAdapter;
import com.mateyinc.marko.matey.data_and_managers.DataContract;
import com.mateyinc.marko.matey.data_and_managers.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.internet.home.BulletinAs;

import static com.mateyinc.marko.matey.data_and_managers.DataManager.BULLETINS_LOADER;
import static com.mateyinc.marko.matey.data_and_managers.DataManager.BULLETIN_COLUMNS;

public class BulletinsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private BulletinsAdapter mAdapter;
    private BroadcastReceiver mDataDownloaded;
    private RecyclerView mRecycleView;
    private EndlessScrollListener mScrollListener;
    private TextView mEmptyView;

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

        getLoaderManager().initLoader(BULLETINS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulletins, container, false);
        mRecycleView = (RecyclerView) view.findViewById(R.id.rvBulletinList);
        mEmptyView = (TextView) view.findViewById(R.id.tvEmptyView);
        mAdapter = new BulletinsAdapter(mContext, mEmptyView);

        // Set the adapter
        Context context = view.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecycleView.setLayoutManager(layoutManager);
//        if (HomeActivity.mListDownloaded && null == mRecycleView.getAdapter()) // If data is already downloaded, set the list, if not it will be set in broadcast receiver
//            mRecycleView.setAdapter(mAdapter);
        mRecycleView.setAdapter(mAdapter);

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Getting posts for the user
                BulletinAs bulletinsAs = new BulletinAs((HomeActivity) mContext);
                bulletinsAs.execute(Integer.toString(HomeActivity.mCurUser.getUserId()),
                        "securePreferences.getString(uid)",
                        "securePreferences.getString(device_id)",
                        Integer.toString(DataManager.mCurrentPage),
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

        // If data is already downloaded, set the list, if not it will be set in broadcast receiver
        if (HomeActivity.mListDownloaded && null == mRecycleView.getAdapter())
            mRecycleView.setAdapter(mAdapter);

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
                DataContract.BulletinEntry.CONTENT_URI,
                BULLETIN_COLUMNS,
                null,
                null,
                BulletinEntry.COLUMN_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}

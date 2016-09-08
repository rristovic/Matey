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

public class BulletinsFragment extends Fragment {

    private Context mContext;
    private BulletinRecyclerViewAdapter mAdapter;
    private BroadcastReceiver mListDownloadedReceiver, mItemChangedReceiver;

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
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BulletinsFragment", "onResume is called.");

        // Whole list downloaded broadcast
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mListDownloadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAdapter.notifyDataSetChanged();
                Log.d("BulletinsFragment", "Bulletin downloaded broadcast received.");
            }
        }, new IntentFilter(BulletinManager.BULLETIN_LIST_LOADED));

        if (updatedPos != -1) { // If some item is updated, update UI based on position
            mAdapter.notifyItemChanged(updatedPos);
            updatedPos = -1;
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mItemChangedReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mListDownloadedReceiver);

        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

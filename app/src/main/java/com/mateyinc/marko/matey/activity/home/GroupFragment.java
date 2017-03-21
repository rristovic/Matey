package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.adapters.GroupsAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.operations.Operations;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GroupFragment extends Fragment {

    private HomeActivity mContext;
    private OperationManager mOperationManager;
    private GroupsAdapter mAdapter;
    private DataAccess mDataAccess;
    private EndlessScrollListener mScrollListener;

    private SwipeRefreshLayout mMainRefreshLayout;

    public GroupFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (HomeActivity) context;
        mOperationManager = OperationManager.getInstance(context);
        mOperationManager.downloadGroupList(mContext);
        mDataAccess = DataAccess.getInstance(mContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_group_list, container, false);
        RecyclerView list = (RecyclerView) mMainRefreshLayout.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

        // Set the adapter
        mAdapter = new GroupsAdapter(mContext);
        mAdapter.setData(mDataAccess.getGroups());
        list.setAdapter(mAdapter);

        // Set listener
        mMainRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOperationManager.downloadGroupList(true, mContext);
            }
        });

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mOperationManager.downloadGroupList(false, mContext);
            }
        };
        list.addOnScrollListener(mScrollListener);

        return mMainRefreshLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(Operations.DownloadEvent event) {
        mScrollListener.onDownloadFinished();
        mMainRefreshLayout.setRefreshing(false);
        mAdapter.setData(mDataAccess.getGroups());
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


}

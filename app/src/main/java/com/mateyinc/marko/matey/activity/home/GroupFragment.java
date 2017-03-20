package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.adapters.GroupsAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.internet.DownloadListener;
import com.mateyinc.marko.matey.internet.OperationManager;


public class GroupFragment extends Fragment implements DownloadListener {

    private HomeActivity mContext;
    private OperationManager mOperationManager;
    private GroupsAdapter mAdapter;
    private DataAccess mDataAccess;

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
        SwipeRefreshLayout view = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_group_list, container, false);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        mAdapter = new  GroupsAdapter(mContext);
        mAdapter.setData(mDataAccess.getGroups());
        list.setAdapter(mAdapter);

        // Set listener
        view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOperationManager.downloadGroupList(true, mContext);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mOperationManager.setDownloadListener(this);
    }

    @Override
    public void onDownloadSuccess() {

    }

    @Override
    public void onDownloadFailed() {

    }
}

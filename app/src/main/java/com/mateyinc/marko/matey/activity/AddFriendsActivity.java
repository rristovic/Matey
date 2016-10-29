package com.mateyinc.marko.matey.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.AddFriendsAdapter;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

public class AddFriendsActivity extends MotherActivity {

    private RecyclerView mRecycleView;
    private AddFriendsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        init();
    }

    private void init() {
        mRecycleView = (RecyclerView)findViewById(R.id.rvSuggestedFriends);
        mAdapter = new AddFriendsAdapter(this);
        mRecycleView.setAdapter(mAdapter);

        mAdapter.setData(DataManager.getInstance(this).getSuggestedFriendsList());
    }


}

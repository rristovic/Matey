package com.mateyinc.marko.matey.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.adapters.AddFriendsAdapter;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.LinkedList;

public class AddFriendsActivity extends MotherActivity {

    private RecyclerView mRecycleView;
    private AddFriendsAdapter mAdapter;
    private Button btnAddAll, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        init();
    }

    private void init() {
        mRecycleView = (RecyclerView)findViewById(R.id.rvSuggestedFriends);
        btnAddAll = (Button)findViewById(R.id.btnAddAllFriends);
        btnFinish = (Button)findViewById(R.id.btnFinish);
        mAdapter = new AddFriendsAdapter(this);

        mRecycleView.setAdapter(mAdapter);
        mAdapter.setData(OperationManager.getInstance(this).getSuggestedFriendsList());

        setClickListeners();
    }

    private void setClickListeners() {
        btnAddAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList list = mAdapter.getAllFriends();
                // Add friends to database and to the server
                OperationManager.getInstance(AddFriendsActivity.this).
                        addUserProfiles(new ArrayList<UserProfile>(list), true);
                SessionManager.getInstance(AddFriendsActivity.this)
                        .uploadFollowedFriends(new ArrayList<UserProfile>(list), MotherActivity.access_token, AddFriendsActivity.this);

                Intent i = new Intent(AddFriendsActivity.this, HomeActivity.class);
                AddFriendsActivity.this.startActivity(i);
                AddFriendsActivity.this.finish();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList list = mAdapter.getAddedFriends();
                // Add friends to database and to the server

                if (list.size() != 0) {
                    OperationManager.getInstance(AddFriendsActivity.this).
                            addUserProfiles(new ArrayList<UserProfile>(list), true);
                    SessionManager.getInstance(AddFriendsActivity.this)
                            .uploadFollowedFriends(new ArrayList<UserProfile>(list), MotherActivity.access_token, AddFriendsActivity.this);
                }

                Intent i = new Intent(AddFriendsActivity.this, HomeActivity.class);
                AddFriendsActivity.this.startActivity(i);
                AddFriendsActivity.this.finish();
            }
        });
    }


}

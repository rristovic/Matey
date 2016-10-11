package com.mateyinc.marko.matey.activity.home;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.inall.InsideActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;


public class NewBulletinActivity extends InsideActivity {

    private EditText etNewPostMsg;
    private TextView tvPost;
    private ImageButton ibBack;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bulletin);

        init();
    }

    private void init() {
        // Settings the app bar via custom toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etNewPostMsg = (EditText)findViewById(R.id.etNewBulletinMsg);
        ibBack = (ImageButton)findViewById(R.id.ibBack);
        tvPost = (TextView)findViewById(R.id.tvPost);
        tvPost.setEnabled(false); // Can't post until something is typed in

        setClickListeners();
    }

    private void setClickListeners() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etNewPostMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s == null || s.length() == 0 ){
                    tvPost.setEnabled(false);
                }else{
                    tvPost.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile profile = Util.getCurrentUserProfile();
                Bulletin b = new Bulletin();
                b.setPostID((int)Util.getPostId());
                b.setUserID(Util.getCurrentUserProfileId());
                b.setDate(new Date());
                b.setFirstName(profile.getFirstName());
                b.setLastName(profile.getLastName());
                b.setMessage(etNewPostMsg.getText().toString());

                DataManager manager = DataManager.getInstance(NewBulletinActivity.this);
                manager.addBulletin(b);

                finish();
            }
        });


    }

}

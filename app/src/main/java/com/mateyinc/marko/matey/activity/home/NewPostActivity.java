package com.mateyinc.marko.matey.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;


public class NewPostActivity extends MotherActivity {

    private EditText etNewPostMsg, etNewPostSubject;
    private TextView tvPost, tvNewPostHeading;
    private ImageButton ibBack;
    private ImageView ivAddPhoto, ivAddLocation, ivAddFile, ivSend;
    private Toolbar mToolbar;

    /** Contains the id of the post that is being replied to **/
    public static final String EXTRA_POST_ID = "replied_postid";
    /** Contains text that is being replied to, thus indicating that this isn't new post **/
    public static final String EXTRA_REPLY_SUBJECT = "post_subject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        init();
        setUI();
    }

    private void init() {
        // Settings the app bar via custom toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etNewPostMsg = (EditText) findViewById(R.id.etNewPostMsg);
        etNewPostSubject = (EditText) findViewById(R.id.etNewPostSubject);
        ivAddFile = (ImageView) findViewById(R.id.ivAddFile);
        ivAddLocation = (ImageView) findViewById(R.id.ivAddLocation);
        ivAddPhoto = (ImageView) findViewById(R.id.ivAddPhoto);
        ivSend = (ImageView) findViewById(R.id.ivSend);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        tvPost = (TextView) findViewById(R.id.tvPost);
        tvNewPostHeading = (TextView) findViewById(R.id.tvNewPostHeading);
        tvPost.setEnabled(false); // Can't post until something is typed in

        setClickListeners();
    }

    private void setUI() {
        Intent i = getIntent();

        if (i.hasExtra(EXTRA_REPLY_SUBJECT)) {
            String text = i.getStringExtra(EXTRA_REPLY_SUBJECT);
            etNewPostSubject.setText(text);
            etNewPostSubject.setFocusable(false);
            etNewPostMsg.setHint(null);
            etNewPostMsg.requestFocus();
            tvNewPostHeading.setText(null);
        }
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
                enableButton(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableButton(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableButton(s);
            }

            private void enableButton(CharSequence s) {
                if (s == null || s.length() == 0) {
                    tvPost.setEnabled(false);
                } else {
                    tvPost.setEnabled(true);
                }
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperationManager operationManager = OperationManager.getInstance(NewPostActivity.this);

                UserProfile profile = MotherActivity.mCurrentUserProfile;
                Bulletin b = new Bulletin(
                        operationManager.createNewActivityId(),
                        profile.getUserId(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        etNewPostMsg.getText().toString(),
                        new Date()
                );

                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}

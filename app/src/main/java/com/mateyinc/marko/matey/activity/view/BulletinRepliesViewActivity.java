package com.mateyinc.marko.matey.activity.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.OnTouchInterface;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.adapters.BulletinRepliesAdapter;
import com.mateyinc.marko.matey.activity.home.BulletinsFragment;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;
import java.util.LinkedList;

public class BulletinRepliesViewActivity extends Activity {

    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_SHOW_REPLIES = "show_replies";

    private BulletinRepliesAdapter mAdapter;
    private RecyclerView rvList;
    private ImageButton ibBack;
    private TextView tvHeading, etReplyText;
    private ImageView ivReply;


    private LinkedList<Bulletin.Reply> mReplies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_replies_view);

        init();
    }

    private void init() {
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        rvList = (RecyclerView) findViewById(R.id.rvBulletinRepliesList);
        tvHeading = (TextView) findViewById(R.id.tvReplyViewHeading);
        mAdapter = new BulletinRepliesAdapter(this, rvList);
        ivReply = (ImageView) findViewById(R.id.ivSendReply);
        etReplyText = (TextView) findViewById(R.id.tvReply);

        // Laying out view from the last position
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.setAdapter(mAdapter);

        getReplies();
        setHeadingText();
        setListeners();
    }


    private void getReplies() {
        if (getIntent().hasExtra(EXTRA_POST_ID)) {
            mReplies = BulletinManager.getInstance(BulletinRepliesViewActivity.this).getBulletinByPostID(getIntent().getIntExtra(EXTRA_POST_ID, -1))
                    .getReplies();
            mAdapter.setData(mReplies);
        } else {
            finish();
        }
    }

    private void setHeadingText() {
        String text = "";
        int replyCount = mReplies.size();
        if (replyCount > 1) {
            text = String.format(getString(R.string.bulletin_reply_header), mReplies.get(0).userFirstName, --replyCount);
        } else if (replyCount == 1)
            text = String.format(getString(R.string.bulletin_onereply_header), mReplies.get(0).userFirstName, mReplies.get(0).userLastName);
        tvHeading.setText(text);
    }

    private void setListeners() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BulletinManager manager = BulletinManager.getInstance(BulletinRepliesViewActivity.this);
                Bulletin b = manager.getBulletinByPostID(getIntent().getIntExtra(EXTRA_POST_ID, -1));
                Bulletin.Reply r = b.getReplyInstance();
                UserProfile profile = Util.getCurrentUserProfile();

                // Create new reply
                r.userFirstName = profile.getFirstName();
                r.userLastName = profile.getLastName();
                r.replyDate = new Date().toString();
                r.userId = profile.getUserId();
                r.replyId = createReplyId();
                r.replyText = etReplyText.getText().toString();
                // Add reply to data
                mReplies.add(r);

                // Update UI
                mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                rvList.scrollToPosition(mAdapter.getItemCount() - 1);
                setHeadingText();
                BulletinsFragment.updatedPos = manager.getBulletinIndex(b);
            }
        });

        ivReply.setOnTouchListener(new OnTouchInterface(this));
    }

    private int createReplyId() {
        // TODO - finish method
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(EXTRA_SHOW_REPLIES)) {
            // Scroll list to the last pos
            rvList.smoothScrollToPosition(mReplies.size() - 1);

            // Get focus on edit text and show keyboard
            etReplyText.setFocusableInTouchMode(true);
            etReplyText.requestFocus();

            final InputMethodManager inputMethodManager = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(etReplyText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}

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
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;
import java.util.LinkedList;

public class BulletinRepliesViewActivity extends Activity {

    public static final String EXTRA_BULLETIN_POS = "post_id";
    public static final String EXTRA_NEW_REPLY = "show_replies";

    public static int mBulletinPos = -1;

    private BulletinRepliesAdapter mAdapter;
    private RecyclerView rvList;
    private ImageButton ibBack;
    private TextView tvHeading, etReplyText;
    private ImageView ivReply;
    private DataManager mManager;

    private LinkedList<Bulletin.Reply> mReplies;
    private Bulletin mCurBulletin = null;


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
        mManager = DataManager.getInstance(BulletinRepliesViewActivity.this);

        // Laying out view from the last position
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.setAdapter(mAdapter);

        getReplies();
        setHeadingText();
        setListeners();
    }

    private void getReplies() {
        if (getIntent().hasExtra(EXTRA_BULLETIN_POS)) {
            mBulletinPos = getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1);
            if (null == mCurBulletin)
                mCurBulletin = mManager.getBulletin(getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1));
            mReplies = mCurBulletin.getReplies();
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
                Bulletin.Reply r = mCurBulletin.getReplyInstance();
                UserProfile profile = Util.getCurrentUserProfile();

                // Create new reply
                r.userFirstName = profile.getFirstName();
                r.userLastName = profile.getLastName();
                r.replyDate = new Date().toString();
                r.userId = profile.getUserId();
                r.replyId = createReplyId();
                r.replyText = etReplyText.getText().toString();

                // Add reply to data and to database
                mReplies.addFirst(r);
                mManager.updateBulletinReplies(mCurBulletin);

                // Update UI
                mAdapter.notifyDataSetChanged();
                setHeadingText();
                BulletinsFragment.updatedPos = mBulletinPos;
            }
        });

        ivReply.setOnTouchListener(new OnTouchInterface(this));
    }

    /**
     * Creating ReplyId for the current bulletin
     *
     * @return the newly created reply id
     */
    private int createReplyId() {
        // TODO - finish method
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(EXTRA_NEW_REPLY)) {
            // Get focus on edit text and show keyboard
            etReplyText.setFocusableInTouchMode(true);
            etReplyText.requestFocus();

            final InputMethodManager inputMethodManager = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(etReplyText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}

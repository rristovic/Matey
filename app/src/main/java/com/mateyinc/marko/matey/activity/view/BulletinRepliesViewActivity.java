package com.mateyinc.marko.matey.activity.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.BulletinRepliesAdapter;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.LinkedList;

public class BulletinRepliesViewActivity extends Activity {

    public static final String POST_ID = "post_id";

    private BulletinRepliesAdapter mAdapter;
    private RecyclerView rvList;
    private ImageButton ibBack;
    private TextView tvHeading;
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
        rvList.setAdapter(mAdapter);

        getReplies();

        // Setting heading text
        String text = "";
        int replyCount = mReplies.size();
        if (replyCount > 1) {
            text = String.format(getString(R.string.bulletin_reply_header), mReplies.get(0).userFirstName, --replyCount);
        } else if (replyCount == 1)
            text = String.format(getString(R.string.bulletin_onereply_header), mReplies.get(0).userFirstName, mReplies.get(0).userLastName);
        tvHeading.setText(text);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getReplies() {
        if (getIntent().hasExtra(POST_ID)) {
            mReplies = BulletinManager.getInstance(BulletinRepliesViewActivity.this).getBulletinByPostID(getIntent().getIntExtra(POST_ID, -1))
                    .getReplies();
            mAdapter.setData(mReplies);
        } else {
            finish();
        }
    }

}

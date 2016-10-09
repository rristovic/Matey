package com.mateyinc.marko.matey.activity.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.mateyinc.marko.matey.data_and_managers.DataContract;
import com.mateyinc.marko.matey.data_and_managers.DataContract.ReplyEntry;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.inall.InsideActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;
import java.util.LinkedList;

public class BulletinRepliesViewActivity extends InsideActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_BULLETIN_ID = "post_id";
    public static final String EXTRA_BULLETIN_POS = "bulletin_adapter_pos";
    public static final String EXTRA_NEW_REPLY = "show_replies";

    public static final String[] REPLIES_COLUMNS = {
            ReplyEntry.TABLE_NAME + "." + ReplyEntry.COLUMN_REPLY_ID,
            ReplyEntry.COLUMN_USER_ID,
            ReplyEntry.COLUMN_FIRST_NAME,
            ReplyEntry.COLUMN_LAST_NAME,
            ReplyEntry.COLUMN_TEXT,
            ReplyEntry.COLUMN_DATE,
            ReplyEntry.COLUMN_NUM_OF_APPRVS,
            ReplyEntry.COLUMN_APPRVS,
            ReplyEntry.COLUMN_POST_ID
    };

    // These indices are tied to MSG_COLUMNS.  If MSG_COLUMNS changes, these
    // must change.
    public static final int COL_REPLY_ID = 0;
    public static final int COL_USER_ID = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_LAST_NAME = 3;
    public static final int COL_TEXT = 4;
    public static final int COL_DATE = 5;
    public static final int COL_NUM_OF_APPRVS = 6;
    public static final int COL_APPRVS = 7;
    public static final int COL_POST_ID = 8;


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
        Intent i = getIntent();
        mManager = DataManager.getInstance(BulletinRepliesViewActivity.this);

        if (i.hasExtra(EXTRA_BULLETIN_ID) && i.hasExtra(EXTRA_BULLETIN_POS)) {
            mBulletinPos = getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1);
            if (null == mCurBulletin)
                mCurBulletin = mManager.getBulletin(getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1));
        } else {
            finish();
        }

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        rvList = (RecyclerView) findViewById(R.id.rvBulletinRepliesList);
        tvHeading = (TextView) findViewById(R.id.tvReplyViewHeading);
        mAdapter = new BulletinRepliesAdapter(this, rvList);
        ivReply = (ImageView) findViewById(R.id.ivSendReply);
        etReplyText = (TextView) findViewById(R.id.tvReply);

        // Laying out view from the last position
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.setAdapter(mAdapter);

        // Init loader
        getSupportLoaderManager().initLoader(Util.REPLIES_LOADER, null, this);
        setListeners();
    }


    private void setHeadingText() {
        String text = "";
        int replyCount = mAdapter.getItemCount();
        Cursor c = mAdapter.getCursor();
        c.moveToFirst();
        if (replyCount > 1) {
            text = String.format(getString(R.string.bulletin_reply_header), c.getString(COL_FIRST_NAME), --replyCount);
        } else if (replyCount == 1)
            text = String.format(getString(R.string.bulletin_onereply_header), c.getString(COL_FIRST_NAME), c.getString(COL_LAST_NAME));
        tvHeading.setText(text);
        c = null;
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
                Bulletin.Reply r = new Bulletin().getReplyInstance();
                UserProfile profile = Util.getCurrentUserProfile();

                // Create new reply
                r.userFirstName = profile.getFirstName();
                r.userLastName = profile.getLastName();
                r.replyDate = new Date().toString();
                r.userId = profile.getUserId();
                r.postId = mCurBulletin.getPostID();
                String id = mCurBulletin.getPostID() + "" + mCurBulletin.getNumOfReplies();
                r.replyId = Integer.parseInt(id);
                r.replyText = etReplyText.getText().toString();

                // Add reply to database
                mManager.addReply(r);

                // Update UI
                mAdapter.notifyDataSetChanged();
                setHeadingText();
                ContentValues values = new ContentValues(1);
                int num = mCurBulletin.getNumOfReplies();
                values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, ++num);
                getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values, DataContract.BulletinEntry.COLUMN_POST_ID + " = " + mCurBulletin.getPostID(), null);
            }
        });

        ivReply.setOnTouchListener(new OnTouchInterface(this));
    }

    /**
     * If some change is made, notify bulletins fragment and its adapter
     */
    public void notifyBulletinFragment() {
        BulletinsFragment.updatedPos = mBulletinPos;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(BulletinRepliesViewActivity.this,
                DataContract.ReplyEntry.CONTENT_URI,
                REPLIES_COLUMNS,
                ReplyEntry.COLUMN_POST_ID + " = ?",
                new String[]{Integer.toString(getIntent().getIntExtra(EXTRA_BULLETIN_ID, -1))},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        setHeadingText();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}

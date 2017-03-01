package com.mateyinc.marko.matey.activity.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.OnTouchInterface;
import com.mateyinc.marko.matey.activity.adapters.BulletinRepliesAdapter;
import com.mateyinc.marko.matey.activity.home.BulletinsFragment;
import com.mateyinc.marko.matey.activity.home.NewPostActivity;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ReplyEntry;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;

import static com.mateyinc.marko.matey.data.OperationManager.REPLIES_ORDER_BY;

public class BulletinViewActivity extends MotherActivity implements LoaderManager.LoaderCallbacks<Cursor>, BulletinRepliesAdapter.RepliesPopupInterface {

    public static final String EXTRA_BULLETIN_ID = "post_id";
    public static final String EXTRA_BULLETIN_POS = "bulletin_adapter_pos";
    public static final String EXTRA_NEW_REPLY = "new_reply";

    /**
     * Indicates if this activity should not show main bulletin that is being replied to
     **/
    public static final String EXTRA_NO_MAIN_POST = "no_main_post";
    /**
     * Argument for loader to indicate what replies should load from db
     **/
    private static final String ARG_REPLY_ID = "reply_id";

    public static final String[] REPLIES_COLUMNS = {
            "replies.".concat(ReplyEntry._ID),
            "replies.".concat(ReplyEntry.COLUMN_USER_ID),
            "replies.".concat(ReplyEntry.COLUMN_FIRST_NAME),
            "replies.".concat(ReplyEntry.COLUMN_LAST_NAME),
            "replies.".concat(ReplyEntry.COLUMN_TEXT),
            "replies.".concat(ReplyEntry.COLUMN_DATE),
            "replies.".concat(ReplyEntry.COLUMN_NUM_OF_LIKES),
            "replies.".concat(ReplyEntry.COLUMN_POST_ID)
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
    public static final int COL_POST_ID = 7;

    public static int mBulletinPos = -1;

    public static final int REPLIES_LOADER = 200;
    public static final int REPLIES_OF_REPLIES_LOADER = 202;


    private BulletinRepliesAdapter mAdapter;
    private RecyclerView rvList, mRepliesOfRepliesRecyclerView;
    private ImageButton ibBack;
    private TextView tvHeading, etReplyText, mRepliesHeader;
    private ImageView ivReply, ivOpenReplyScreen;
    private PopupWindow popWindow;
    private LinearLayout mainlayout;

    private Bulletin mCurBulletin = null;
    private OperationManager mManager;
    private BulletinRepliesAdapter mRepliesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_view);

        init();
    }

    private void init() {
        Intent i = getIntent();
        mManager = OperationManager.getInstance(BulletinViewActivity.this);

        if (i.hasExtra(EXTRA_BULLETIN_ID) && i.hasExtra(EXTRA_BULLETIN_POS)) {
            mBulletinPos = getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1);
            if (null == mCurBulletin)
                updateCurBulletin();
        } else {
            finish();
        }

        mainlayout = (LinearLayout) findViewById(R.id.llmainFrame);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        rvList = (RecyclerView) findViewById(R.id.rvBulletinRepliesList);
        tvHeading = (TextView) findViewById(R.id.tvReplyViewHeading);
        mAdapter = new BulletinRepliesAdapter(this, rvList);
        ivOpenReplyScreen = (ImageView) findViewById(R.id.ivOpenReplyScreen);
        ivReply = (ImageView) findViewById(R.id.ivSendReply);
        ivReply.setEnabled(false);
        etReplyText = (TextView) findViewById(R.id.tvReply);
        etReplyText.addTextChangedListener(new TextWatcher() {
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
                    ivReply.setEnabled(false);
                } else {
                    ivReply.setEnabled(true);
                }
            }
        });

        mAdapter.setReplyPopupInterface(this);
        rvList.setAdapter(mAdapter);
        mAdapter.showMainPostInfo(mCurBulletin);

        // Init loader
        getSupportLoaderManager().initLoader(REPLIES_LOADER, null, this);
        setListeners();


    }

    private void updateCurBulletin() {
        mCurBulletin = mManager.getBulletin(mBulletinPos);
    }

    private void setListeners() {
        ivOpenReplyScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BulletinViewActivity.this, NewPostActivity.class);
                i.putExtra(NewPostActivity.EXTRA_REPLY_SUBJECT, mCurBulletin.getMessage());// TODO - finish instead of getMessage
                startActivity(i);
            }
        });
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperationManager dm = OperationManager.getInstance(BulletinViewActivity.this);
                Reply r = new Reply();
                UserProfile profile = MotherActivity.mCurrentUserProfile;

                // Create new reply
                r.userFirstName = profile.getFirstName();
                r.userLastName = profile.getLastName();
                r.replyDate = new Date();
                r.userId = profile.getUserId();
                r.postId = mCurBulletin.getPostID();
                r._id = dm.createNewActivityId();
                r.replyText = etReplyText.getText().toString();

                // Update UI
                mAdapter.notifyDataSetChanged();
                setHeadingText();
                etReplyText.setText(null);

//                dm.addOperation(new UploadOp(r)).performOperations();
//                SessionManager networkManager = SessionManager.getInstance(BulletinViewActivity.this);
//                networkManager.uploadNewReply(r, mCurBulletin, OperationManager.getInstance(BulletinViewActivity.this),
//                        MotherActivity.access_token, BulletinViewActivity.this);

//                // Update number of replies in bulletin db
//                ContentValues values = new ContentValues(1);
//                int num = mCurBulletin.getNumOfReplies();
//                values.put(DataContract.BulletinEntry.COLUMN_NUM_OF_REPLIES, ++num);
//                getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values, DataContract.BulletinEntry._ID + " = " + mCurBulletin.getPostID(), null);
            }
        });

        ivReply.setOnTouchListener(new OnTouchInterface(this));
    }

    /**
     * If some change is made, notify bulletins fragment and its adapter
     */
    public void notifyBulletinFragment() {
        BulletinsFragment.mUpdatedPositions.add(mBulletinPos);
    }

    private void setHeadingText() {
        String text = "";
        int replyCount = mAdapter.getItemCount();
        Cursor c = mAdapter.getCursor();
        c.moveToFirst();
        if (replyCount > 1) {
            text = String.format(getString(R.string.bulletin_reply_header), c.getString(COL_FIRST_NAME), Integer.toString(--replyCount));
        } else if (replyCount == 1)
            text = String.format(getString(R.string.bulletin_onereply_header), c.getString(COL_FIRST_NAME), c.getString(COL_LAST_NAME));
        tvHeading.setText(text);
        c = null;
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
    public void showPopupWindow(long replyId) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_REPLY_ID, replyId);
//        getSupportLoaderManager().initLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);
        getSupportLoaderManager().restartLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        View inflatedView = layoutInflater.inflate(R.layout.activity_replies_view, null, false);
        // find the ListView in the popup layout
        mRepliesOfRepliesRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rvReplies);
        mRepliesHeader = (TextView) inflatedView.findViewById(R.id.tvHeading);

        // Add adapter
        mRepliesAdapter = new BulletinRepliesAdapter(BulletinViewActivity.this, mRepliesOfRepliesRecyclerView);
        mRepliesOfRepliesRecyclerView.setAdapter(mRepliesAdapter);

        // Get device size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // set height depends on the device size
        popWindow = new PopupWindow(inflatedView, width, height - 50, true);
        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_bg));
        popWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popWindow.setAnimationStyle(R.style.PopupAnimation);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(mainlayout, Gravity.BOTTOM, 0, 200);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == REPLIES_LOADER)
            return new CursorLoader(BulletinViewActivity.this,
                    DataContract.ReplyEntry.CONTENT_URI,
                    REPLIES_COLUMNS,
                    "replies.".concat(ReplyEntry.COLUMN_POST_ID) + " = ?",
                    new String[]{Long.toString(getIntent().getLongExtra(EXTRA_BULLETIN_ID, -1))},
                    REPLIES_ORDER_BY);
        else
            return new CursorLoader(BulletinViewActivity.this,
                    ReplyEntry.CONTENT_URI,
                    REPLIES_COLUMNS,
                    "replies.".concat(ReplyEntry._ID) + " = ?",
                    new String[]{Long.toString(args.getLong(ARG_REPLY_ID))},
                    REPLIES_ORDER_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == REPLIES_LOADER) {
            mAdapter.swapCursor(data);
            setHeadingText();
        } else {
            mRepliesAdapter.swapCursor(data);
            // TODO - set heading text
        }
    }

//    private void setBulletinData() {
//        updateCurBulletin();
//        tvName.setText(mCurBulletin.getFirstName().concat(" ").concat(mCurBulletin.getLastName()));
//        tvDate.setText(mCurBulletin.getDate().toString());
//        tvMessage.setText(mCurBulletin.getMessage());
//
//        llReply.setOnTouchListener(new OnTouchInterface(BulletinViewActivity.this));
//        llReply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ivReply.performClick();
//            }
//        });
//    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == REPLIES_LOADER)
            mAdapter.swapCursor(null);
        else
            mRepliesAdapter.swapCursor(null);
    }
}

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.NewPostActivity;
import com.mateyinc.marko.matey.activity.OnTouchInterface;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.BulletinsFragment;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.adapters.RepliesAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.ReplyEntry;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;

public class BulletinViewActivity extends MotherActivity implements LoaderManager.LoaderCallbacks<Cursor>, RepliesAdapter.ReplyClickedInterface {

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
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry._ID),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_USER_ID),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_FIRST_NAME),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_LAST_NAME),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_TEXT),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_DATE),
            ReplyEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_POST_ID),
            "(SELECT COUNT(" + DataContract.ApproveEntry._ID +
                    ") FROM " + DataContract.ApproveEntry.TABLE_NAME + " WHERE " +
                    DataContract.ApproveEntry.TABLE_NAME + "." + DataContract.ApproveEntry.COLUMN_REPLY_ID + " = "
                    + DataContract.ReplyEntry.TABLE_NAME + "." + DataContract.ReplyEntry._ID + ")"

//            DataContract.ApproveEntry.TABLE_NAME.concat(".").concat(ReplyEntry.COLUMN_POST_ID),
    };

    // These indices are tied to MSG_COLUMNS.  If MSG_COLUMNS changes, these
    // must change.
    public static final int COL_REPLY_ID = 0;
    public static final int COL_USER_ID = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_LAST_NAME = 3;
    public static final int COL_TEXT = 4;
    public static final int COL_DATE = 5;
    public static final int COL_POST_ID = COL_DATE + 1;
//    public static final int COL_NUM_OF_REPLIES = COL_POST_ID + 1;
    public static final int COL_NUM_OF_APPRVS = COL_POST_ID + 1;

    /**
     * Cursor sort order
     */
    public static final String REPLIES_ORDER_BY = DataContract.ReplyEntry.COLUMN_DATE + " DESC";


    public static int mBulletinPos = -1;

    public static final int REPLIES_LOADER = 200;
    public static final int REPLIES_OF_REPLIES_LOADER = 202;

    private RepliesAdapter mAdapter;
    private RecyclerView rvList, mRepliesOfRepliesRecyclerView;
    private ImageButton ibBack;
    private TextView tvHeading, etReplyText, mRepliesHeader;
    private ImageView ivReply, ivOpenReplyScreen;
    private PopupWindow popWindow;
    private LinearLayout mainlayout;

    private Bulletin mCurBulletin = null;
    private OperationManager mManager;
    private RepliesAdapter mRepliesAdapter;
    private RelativeLayout rlContainer;
    private View bulletinView;


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

        rlContainer = (RelativeLayout) findViewById(R.id.container);
        mainlayout = (LinearLayout) findViewById(R.id.llmainFrame);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        rvList = (RecyclerView) findViewById(R.id.rvBulletinRepliesList);
        tvHeading = (TextView) findViewById(R.id.tvReplyViewHeading);
        mAdapter = new RepliesAdapter(this, rvList);
        ivOpenReplyScreen = (ImageView) findViewById(R.id.ivOpenReplyScreen);
        ivReply = (ImageView) findViewById(R.id.ivSendReply);
        ivReply.setEnabled(false);
        etReplyText = (TextView) findViewById(R.id.etNewReply);
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
        mCurBulletin = DataAccess.getBulletin(mBulletinPos, BulletinViewActivity.this);
    }

    private void setListeners() {
        ivOpenReplyScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BulletinViewActivity.this, NewPostActivity.class);
                i.putExtra(NewPostActivity.EXTRA_REPLY_SUBJECT, mCurBulletin.getSubject());
                i.putExtra(NewPostActivity.EXTRA_POST_ID, mCurBulletin.getPostID());
                i.putExtra(NewPostActivity.EXTRA_USER_NAME, mCurBulletin.getFirstName());
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
                Reply r = new Reply();
                UserProfile profile = MotherActivity.mCurrentUserProfile;

                // Create new reply
                r.setUserFirstName(profile.getFirstName());
                r.setUserLastName(profile.getLastName());
                r.setDate(new Date());
                r.setUserId(profile.getUserId());
                r.setPostId(mCurBulletin.getPostID());
                r.setReplyText(etReplyText.getText().toString());

                // Update UI
                etReplyText.setText(null);

                // Save and upload
                mManager.postNewReply(r, mCurBulletin.getPostID(), BulletinViewActivity.this);
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

        if (mAdapter.getItemCount() != 0) {
            Cursor c = mAdapter.getCursor();
            c.moveToFirst();
            int replyCount = mAdapter.getItemCount();
            if (replyCount == 1)
                text = String.format(getString(R.string.bulletin_onereply_header), c.getString(COL_FIRST_NAME), c.getString(COL_LAST_NAME));
            else {
                text = String.format(getString(R.string.bulletin_reply_header), c.getString(COL_FIRST_NAME), Integer.toString(--replyCount));
            }
        } else
            text = "No replies so far";

        tvHeading.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(EXTRA_NEW_REPLY)) {
            // Get focus on edit text and show keyboard
            showReplyKeyboard();
        }
    }

    @Override
    public void showReplyKeyboard() {
        etReplyText.setFocusableInTouchMode(true);
        etReplyText.requestFocus();

        final InputMethodManager inputMethodManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(etReplyText, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void showPopupWindow(final Reply reply) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_REPLY_ID, reply._id);
//        getSupportLoaderManager().initLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);
        getSupportLoaderManager().restartLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        View inflatedView = layoutInflater.inflate(R.layout.activity_replies_view, null, false);
        // find the ListView in the popup layout
        mRepliesOfRepliesRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rvReplies);
        mRepliesHeader = (TextView) inflatedView.findViewById(R.id.tvHeading);
        final EditText etNewReReply = (EditText) inflatedView.findViewById(R.id.etNewReply);
        final ImageView ivSendReply = (ImageView) inflatedView.findViewById(R.id.ivSendReply);

        etNewReReply.addTextChangedListener(new TextWatcher() {
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
                    ivSendReply.setEnabled(false);
                } else {
                    ivSendReply.setEnabled(true);
                }
            }
        });

        ivSendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reply r = new Reply();
                UserProfile profile = MotherActivity.mCurrentUserProfile;

                // Create new reply
                r.setUserFirstName(profile.getFirstName());
                r.setUserLastName(profile.getLastName());
                r.setDate(new Date());
                r.setUserId(profile.getUserId());
                r.setPostId(mCurBulletin.getPostID());
                r.setReReplyId(reply._id);
                r.setReplyText(etReplyText.getText().toString());
                r.isPostReply = false;

                // Update UI
                etNewReReply.setText(null);

                // Save and upload
                mManager.postNewReply(r, reply, BulletinViewActivity.this);
                // Notify so ui can update
                getContentResolver().notifyChange(ReplyEntry.CONTENT_URI, null);
            }
        });

        // Add adapter
        mRepliesAdapter = new RepliesAdapter(BulletinViewActivity.this, mRepliesOfRepliesRecyclerView);
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
                    ReplyEntry.CONTENT_URI,
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
            updateCurBulletin();
            mAdapter.setBulletin(mCurBulletin);
            mAdapter.swapCursor(data);
            updateUI();
        } else {
            mRepliesAdapter.swapCursor(data);
            // TODO - set heading text
        }
    }

    private static final String BULLETINVIEW_TAG = "bulletin_view_tag";

    private void updateUI() {
        setHeadingText();

        if (mAdapter.getItemCount() == 0) {

            // Initiate bulletin view
            if (bulletinView == null) {
                bulletinView = LayoutInflater.from(BulletinViewActivity.this).inflate(R.layout.bulletin_list_item, null);
                bulletinView.setTag(BULLETINVIEW_TAG);
                  LinearLayout llReply = (LinearLayout) bulletinView.findViewById(R.id.llBulletinReply);
                LinearLayout llBoost = (LinearLayout) bulletinView.findViewById(R.id.llBoost);

                llReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReplyKeyboard();
                    }
                });

                llBoost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mManager.newPostLike(mCurBulletin, BulletinViewActivity.this);
                        getContentResolver().notifyChange(DataContract.ReplyEntry.CONTENT_URI, null);
                        updateUI();
                    }
                });

                bulletinView.findViewById(R.id.ivBulletinProfilePic).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(BulletinViewActivity.this, ProfileActivity.class);
                        i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCurBulletin.getUserID());
                        BulletinViewActivity.this.startActivity(i);
                    }
                });
            }

            // Add bulletin view
            if (rlContainer.findViewWithTag(BULLETINVIEW_TAG) == null)
                rlContainer.addView(bulletinView);

            // Set data
            ((TextView) bulletinView.findViewById(R.id.tvBulletinUserName)).setText(mCurBulletin.getFirstName().concat(" ").concat(mCurBulletin.getLastName()));
            ((TextView) bulletinView.findViewById(R.id.tvBulletinDate)).setText(Util.getReadableDateText(mCurBulletin.getDate()));
            ((TextView) bulletinView.findViewById(R.id.tvBulletinSubject)).setText(mCurBulletin.getSubject());
            ((TextView) bulletinView.findViewById(R.id.tvBulletinMessage)).setText(mCurBulletin.getMessage());
            ((TextView) bulletinView.findViewById(R.id.tvBulletinStats)).setText(mCurBulletin.getStatistics(BulletinViewActivity.this));
        } else {
            if (bulletinView != null && rlContainer.findViewWithTag(BULLETINVIEW_TAG) != null)
                rlContainer.removeView(bulletinView);
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

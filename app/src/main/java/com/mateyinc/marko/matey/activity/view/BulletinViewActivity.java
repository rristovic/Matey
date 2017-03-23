package com.mateyinc.marko.matey.activity.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;

import java.util.Date;

public class BulletinViewActivity extends MotherActivity implements RepliesAdapter.ReplyClickedInterface {

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


    public static int mBulletinPos = -1;
    public static long mBulletinId = -1;

    private RepliesAdapter mAdapter;
    private RecyclerView rvList, mRepliesOfRepliesRecyclerView;
    private ImageButton ibBack;
    private TextView tvHeading, etReplyText, mRepliesHeader;
    private ImageView ivReply, ivOpenReplyScreen;
    private PopupWindow popWindow;
    private LinearLayout mainlayout;
    private RelativeLayout rlContainer;
    private View bulletinView;

    private Bulletin mCurBulletin = null;
    private OperationManager mManager;
    private RepliesAdapter mRepliesAdapter;
    // Indicates if popup window with reply list is visible
    private boolean mPopupShowing;
    // Holds reference to current reply which reply list is being displayed on popup window
    private Reply mCurReply;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_view);

        init();
    }

    private void init() {
        Intent i = getIntent();
        if (i.hasExtra(EXTRA_BULLETIN_ID) && i.hasExtra(EXTRA_BULLETIN_POS)) {
            mBulletinPos = getIntent().getIntExtra(EXTRA_BULLETIN_POS, -1);
            mBulletinId = getIntent().getLongExtra(EXTRA_BULLETIN_ID, -1);
        } else {
            finish();
        }

        mManager = OperationManager.getInstance(BulletinViewActivity.this);
        mManager.downloadBulletinInfo(mBulletinId, BulletinViewActivity.this);

        rlContainer = (RelativeLayout) findViewById(R.id.container);
        mainlayout = (LinearLayout) findViewById(R.id.llmainFrame);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        rvList = (RecyclerView) findViewById(R.id.rvBulletinRepliesList);
        tvHeading = (TextView) findViewById(R.id.tvReplyViewHeading);
        mAdapter = new RepliesAdapter(this);
        if (null == mCurBulletin)
            updateCurBulletin();
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

        setListeners();
    }

    /**
     * Helper method for updating field {@link #mCurBulletin} with new data and passing it to adapter.
     */
    private void updateCurBulletin() {
        mCurBulletin = DataAccess.getInstance(BulletinViewActivity.this).getBulletin(mBulletinPos);
        mAdapter.setBulletin(mCurBulletin);
        updateUI();
    }

    private void setListeners() {
        ivOpenReplyScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BulletinViewActivity.this, NewPostActivity.class);
                i.putExtra(NewPostActivity.EXTRA_REPLY_SUBJECT, mCurBulletin.getSubject());
                i.putExtra(NewPostActivity.EXTRA_POST_ID, mCurBulletin.getId());
                i.putExtra(NewPostActivity.EXTRA_USER_NAME, mCurBulletin.getUserProfile().getFirstName());
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
                // Create new reply
//                r.setUserFirstName(profile.getFirstName());
//                r.setUserLastName(profile.getLastName());
                r.setUserProfile(MotherActivity.mCurrentUserProfile);
                r.setDate(new Date());
//                r.setUserId(profile.getUserId());
                r.setPostId(mCurBulletin.getId());
                r.setReplyText(etReplyText.getText().toString());

                // Update UI
                etReplyText.setText(null);

                // Save and upload
                mManager.postNewReply(r, mCurBulletin, BulletinViewActivity.this);
                updateCurBulletin();

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
            int replyCount = mAdapter.getItemCount();
            if (replyCount == 1)
                text = String.format(getString(R.string.bulletin_onereply_header), mCurBulletin.getUserProfile().getFirstName(), mCurBulletin.getUserProfile().getLastName());
            else {
                text = String.format(getString(R.string.bulletin_reply_header), mCurBulletin.getUserProfile().getFirstName(), Integer.toString(--replyCount));
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

//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//
//        if (id == REPLIES_LOADER)
//            return new CursorLoader(BulletinViewActivity.this,
//                    ReplyEntry.CONTENT_URI,
//                    REPLIES_COLUMNS,
//                    "replies.".concat(ReplyEntry.COLUMN_POST_ID) + " = ?",
//                    new String[]{Long.toString(getIntent().getLongExtra(EXTRA_BULLETIN_ID, -1))},
//                    REPLIES_ORDER_BY);
//        else
//            return new CursorLoader(BulletinViewActivity.this,
//                    ReplyEntry.CONTENT_URI,
//                    REPLIES_COLUMNS,
//                    "replies.".concat(ReplyEntry._ID) + " = ?",
//                    new String[]{Long.toString(args.getLong(ARG_REPLY_ID))},
//                    REPLIES_ORDER_BY);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//
//        if (loader.getId() == REPLIES_LOADER) {
//            updateCurBulletin();
//            mAdapter.setBulletin(mCurBulletin);
//            mAdapter.swapCursor(data);
//            updateUI();
//        } else {
//            mRepliesAdapter.swapCursor(data);
//            // TODO - set heading text
//        }
//    }

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
                        mManager.boostPost(mCurBulletin, BulletinViewActivity.this);
                        updateUI();
                    }
                });

                bulletinView.findViewById(R.id.ivBulletinProfilePic).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(BulletinViewActivity.this, ProfileActivity.class);
                        i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCurBulletin.getUserProfile().getUserId());
                        BulletinViewActivity.this.startActivity(i);
                    }
                });
            }

            // Add bulletin view
            if (rlContainer.findViewWithTag(BULLETINVIEW_TAG) == null)
                rlContainer.addView(bulletinView);

            // Set data
            ((TextView) bulletinView.findViewById(R.id.tvBulletinUserName)).setText(mCurBulletin.getUserProfile().getFullName());
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

//    @Override
//    public void onDownloadSuccess() {
//        if (mPopupShowing && mCurReply != null) {
//            mRepliesAdapter.setData(mCurReply.getReplyList());
//            return;
//        }
//        updateCurBulletin();
//    }

//    @Override
//    public void onDownloadFailed() {
//        updateCurBulletin();
//        Toast.makeText(BulletinViewActivity.this, "Download failed.", Toast.LENGTH_SHORT).show();
//    }

//    @Override
//    public void onUploadSuccess() {
//        updateCurBulletin();
//    }

//    @Override
//    public void onUploadFailed() {
//        updateCurBulletin();
//    }

    @Override
    public void showPopupWindow(final Reply reply) {
        mPopupShowing = true;
        mCurReply = reply;
        // Start download process
        mManager.downloadReReplies(reply, BulletinViewActivity.this);

        Bundle bundle = new Bundle();
        bundle.putLong(ARG_REPLY_ID, reply._id);
//        getSupportLoaderManager().initLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);
//        getSupportLoaderManager().restartLoader(REPLIES_OF_REPLIES_LOADER, bundle, this);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        View inflatedView = layoutInflater.inflate(R.layout.activity_replies_view, null, false);
        // find the ListView in the popup layout
        mRepliesOfRepliesRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rvReReplies);
        mRepliesHeader = (TextView) inflatedView.findViewById(R.id.tvHeading);
        final EditText etNewReReply = (EditText) inflatedView.findViewById(R.id.etNewReReply);
        final ImageView ivSendReply = (ImageView) inflatedView.findViewById(R.id.ivSendReReply);

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
                // Save and upload
                mManager.postNewReReply(etNewReReply.getText().toString(), mCurBulletin.getId()
                        , reply, BulletinViewActivity.this);

                // Update UI
                etNewReReply.setText(null);
            }
        });

        // Add adapter
        mRepliesAdapter = new RepliesAdapter(BulletinViewActivity.this);
        mRepliesAdapter.setData(reply.getReplyList());
        mRepliesOfRepliesRecyclerView.setAdapter(mRepliesAdapter);

        // Get device size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // set height depends on the device size
        popWindow = new PopupWindow(inflatedView, width, height, true);
        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_bg));
        popWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
//        popWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
        popWindow.setAnimationStyle(R.style.PopupAnimation);
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(mainlayout, Gravity.BOTTOM, 0, 200);

        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopupShowing = false;
                mCurReply = null;
            }
        });
    }
}

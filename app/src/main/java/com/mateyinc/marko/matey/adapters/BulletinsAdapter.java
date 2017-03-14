package com.mateyinc.marko.matey.adapters;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.Bulletin;


public class BulletinsAdapter extends RecycleCursorAdapter {
    private static final String TAG = BulletinsAdapter.class.getSimpleName();

    // Maximum number of character in a post
    private static final int CHAR_LIM = 300;

    private static final int FIRST_ITEM = 1;
    private static final int ITEM = 2;

    private RecyclerView mRecycleView;
    /**
     * Used in long click to determine the position of clicked view
     */
    public static String clickedText = "";
    public static final int COPYTEXT_ID = 100;

    public BulletinsAdapter(HomeActivity context) {
        mContext = context;
        mManager = OperationManager.getInstance(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {

            if (mRecycleView == null)
                mRecycleView = (RecyclerView) parent;

            switch (viewType) {
                case ITEM:
                default: {
                    View view = LayoutInflater.from(mContext) //Inflate the view
                            .inflate(R.layout.bulletin_list_item, parent, false);

//
                    // Implementing ViewHolderClickListener and returning view holder
                    return new ViewHolder(view, getViewHolderListener());
                }
//                case FIRST_ITEM: {
//                    View view = LayoutInflater.from(mContext)
//                            .inflate(R.layout.bulletin_first_list_item, parent, false);
//
//                    return new ViewHolderFirst(view, getViewHolderListener());
//                }
            }
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /**
     * Helper method for creating ViewHolder listener
     *
     * @return newly created listener
     */
    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {

            @Override
            public void onReplyClick(View caller, int adapterViewPosition) {
                Intent i = new Intent(mContext, BulletinViewActivity.class);
                i.putExtra(BulletinViewActivity.EXTRA_NEW_REPLY, true);
                mCursor.moveToPosition(adapterViewPosition);
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getLong(DataAccess.COL_POST_ID));
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, adapterViewPosition);
                mContext.startActivity(i);
            }

            @Override
            public void onBodyClicked(View view, int adapterViewPosition) {
                Intent i = new Intent(mContext, BulletinViewActivity.class);
                mCursor.moveToPosition(adapterViewPosition);
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getLong(DataAccess.COL_POST_ID));
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, adapterViewPosition);
                mContext.startActivity(i);
            }

            @Override
            public void onNameClick(View view, int adapterViewPosition) {
                Intent i = new Intent(mContext, ProfileActivity.class);
                mCursor.moveToPosition(adapterViewPosition);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCursor.getLong(DataAccess.COL_USER_ID));
                mContext.startActivity(i);
            }

            @Override
            public void onBoostClick(View caller, int adapterViewPosition) {
                mManager.newPostLike(adapterViewPosition, mContext);
            }


//            @Override
//            public boolean onLongClick(View v, int adapterViewPosition) {
//                clickedText = ((TextView) v).getText().toString();
//                return false;
//            }
        };
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        switch (getItemViewType(position)) {

            case FIRST_ITEM:
            case ITEM: {
                BulletinsAdapter.ViewHolder holder = (ViewHolder) mHolder;
                final Bulletin bulletin = DataAccess.getBulletin(position, mCursor);

                try {
                    if (bulletin.getMessage().length() > CHAR_LIM) {
                        String text = bulletin.getMessage().substring(0, CHAR_LIM - 1).concat(" ... ")
                                .concat(mContext.getString(R.string.continue_reading_message));
                        holder.tvMessage.setText(text);
                    } else {
                        holder.tvMessage.setText(bulletin.getMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                    holder.tvMessage.setText(mContext.getString(R.string.error_message));
                }

                holder.tvSubject.setText(bulletin.getSubject());
                holder.tvStats.setText(bulletin.getStatistics(mContext));
                holder.tvName.setText(bulletin.getFirstName() + " " + bulletin.getLastName());
                holder.tvDate.setText(Util.getReadableDateText(bulletin.getDate()));

                switch (bulletin.getServerStatus()) {
                    case STATUS_UPLOADING: {
                        setViewToUploading(holder);
                        break;
                    }

                    case STATUS_RETRY_UPLOAD: {
                        setViewToUploadFailed(holder, bulletin);
                        break;
                    }

                    case STATUS_SUCCESS: {
                        setViewToNormal(holder);
                        break;
                    }
                }

                break;
            }
            default:
                break;
        }
    }

    private static final String TAG_UPLOADING = "uploading_textview";
    private static final String TAG_FAILED = "failed_textview";

    /**
     * Setting the view to the normal mode
     *
     * @param holder The view holder to be changed
     */
    private void setViewToNormal(ViewHolder holder) {
        // First update view based on past state
        switch (holder.stateFlag) {
            case ViewHolder.STATE_FAILED: {
                holder.llBottom.removeView(holder.llBottom.findViewWithTag(TAG_FAILED));
                break;
            }
            case ViewHolder.STATE_UPLOADING: { // view was in uploading, remove unnecessary views
                holder.llBottom.removeView(holder.llBottom.findViewWithTag(TAG_UPLOADING));
                break;
            }
            default:break;
        }

        if (holder.stateFlag != ViewHolder.STATE_UPLOADED) {
            holder.rlInfo.setClickable(true);
            holder.rlInfo.setAlpha(1f);
            holder.rlBody.setClickable(true);
            holder.rlBody.setAlpha(1f);
            holder.btnBoost.setVisibility(View.VISIBLE);
            holder.btnReply.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setting the view to the failed startUploadAction mode
     *
     * @param holder   the view holder which will be changed
     * @param bulletin the bulletin to startUploadAction it's {@link Bulletin#mServerStatus}
     */
    private void setViewToUploadFailed(ViewHolder holder, final Bulletin bulletin) {

        // First update view based on past state
        switch (holder.stateFlag) {
            case ViewHolder.STATE_UPLOADED: {
                holder.rlInfo.setClickable(false);
                holder.rlInfo.setAlpha(0.5f);
                holder.rlBody.setClickable(false);
                holder.rlBody.setAlpha(0.5f);
                holder.btnBoost.setVisibility(View.GONE);
                holder.btnReply.setVisibility(View.GONE);
                break;
            }
            case ViewHolder.STATE_UPLOADING: { // view was in uploading, remove unnecessary views
                View view = holder.llBottom.findViewWithTag(TAG_UPLOADING);
                holder.llBottom.removeView(view);
            }
        }

        // Update view to failed state only if not in failed mode already
        if (holder.stateFlag != ViewHolder.STATE_FAILED) {
            TextView textView = new TextView(mContext);
            ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(params);
            textView.setText("Failed to upload. Click to try again.");
            textView.setGravity(Gravity.CENTER);
            textView.setTag(TAG_FAILED);
            holder.llBottom.addView(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mManager.uploadFailedData(Bulletin.class.getSimpleName(), bulletin, mContext);
                }
            });
            holder.stateFlag = ViewHolder.STATE_FAILED;
        }
    }

    /**
     * Setting the view to the uploading mode
     *
     * @param holder the view holder to be changed
     */
    private void setViewToUploading(ViewHolder holder) {

        // First update view based on past state
        switch (holder.stateFlag) {
            case ViewHolder.STATE_UPLOADED: {
                holder.rlInfo.setClickable(false);
                holder.rlInfo.setAlpha(0.5f);
                holder.rlBody.setClickable(false);
                holder.rlBody.setAlpha(0.5f);
                holder.btnBoost.setVisibility(View.GONE);
                holder.btnReply.setVisibility(View.GONE);
                break;
            }
            case ViewHolder.STATE_FAILED: { // view was in failed, remove unnecessary views
                View view = holder.llBottom.findViewWithTag(TAG_FAILED);
                holder.llBottom.removeView(view);
            }
            default:
                break;
        }

        // Update view to uploading state only if not in uplading mode already
        if (holder.stateFlag != ViewHolder.STATE_UPLOADING) {
            TextView textView = new TextView(mContext);
            ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(params);
            textView.setText("Uploading");
            textView.setGravity(Gravity.CENTER);
            textView.setTag(TAG_UPLOADING);
            holder.llBottom.addView(textView);
            holder.stateFlag = ViewHolder.STATE_UPLOADING;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? FIRST_ITEM : ITEM;
    }

    public void swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

        private static final int STATE_UPLOADING = 0;
        private static final int STATE_FAILED = -1;
        private static final int STATE_UPLOADED = 1;

        private final View mView;
        private final TextView tvMessage;
        private final TextView tvName, tvDate, tvStats, tvSubject;
        private final RelativeLayout rlBody, rlInfo;
        private final LinearLayout btnReply, btnBoost, llBottom;
        private final ImageView ivBookmark, ivShare;
        private int stateFlag = STATE_UPLOADED;

        ViewHolderClickListener mListener;

        private static final String BTN_REPLY_TAG = "replytag";
        private static final String BTN_BOOST_TAG = "boosttag";
        private static final String TV_NAME_TAG = "nametag";
        private static final String RL_BODY_TAG = "bodytag";

        private ViewHolder(View view, ViewHolderClickListener listener) {
            super(view);
            mView = view;
            llBottom = (LinearLayout) view.findViewById(R.id.llBottomButtons);
            rlInfo = (RelativeLayout) view.findViewById(R.id.rlInfo);
            tvMessage = (TextView) view.findViewById(R.id.tvBulletinMessage);
            tvName = (TextView) view.findViewById(R.id.tvBulletinUserName);
            tvName.setTag(TV_NAME_TAG);
            tvSubject = (TextView) view.findViewById(R.id.tvBulletinSubject);
            tvDate = (TextView) view.findViewById(R.id.tvBulletinDate);
            tvStats = (TextView) view.findViewById(R.id.tvBulletinStats);
            rlBody = (RelativeLayout) view.findViewById(R.id.rlBody);
            rlBody.setTag(RL_BODY_TAG);
            btnReply = (LinearLayout) view.findViewById(R.id.llBulletinReply);
            btnReply.setTag(BTN_REPLY_TAG);
            btnBoost = (LinearLayout) view.findViewById(R.id.llBoost);
            btnBoost.setTag(BTN_BOOST_TAG);
            ivBookmark = (ImageView) view.findViewById(R.id.ivBulletinBookmark);
            ivShare = (ImageView) view.findViewById(R.id.ivBulletinShare);
            mListener = listener;

            rlBody.setOnClickListener(this);
//            btnReply.setOnTouchListener(new OnTouchInterface(mView.getContext()));
            btnReply.setOnClickListener(this);
            tvName.setOnClickListener(this);
            btnBoost.setOnClickListener(this
            );
//            tvMessage.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            int position = ViewHolder.this.getAdapterPosition();

            switch (tag) {
                case BTN_REPLY_TAG: {
                    mListener.onReplyClick(v, position);
                    break;
                }

                case BTN_BOOST_TAG: {
                    mListener.onBoostClick(v, position);
                    break;
                }

                case TV_NAME_TAG: {
                    mListener.onNameClick(v, position);
                    break;
                }

                case RL_BODY_TAG: {
                    mListener.onBodyClicked(v, position);
                    break;
                }
            }
        }


        @Override
        public boolean onLongClick(View v) {
//            int position = ViewHolder.this.getAdapterPosition();
//            return mListener.onLongClick(v, position);
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.setHeaderTitle(null);
//            menu.add(0, COPYTEXT_ID, 0, mView.getContext().getString(R.string.menu_item_copy_text));//groupId, itemId, order, title
        }


        protected interface ViewHolderClickListener {
            void onReplyClick(View caller, int adapterViewPosition);

            void onBodyClicked(View caller, int adapterViewPosition);

            void onNameClick(View caller, int adapterViewPosition);

//            boolean onLongClick(View caller, int adapterViewPosition);

            void onBoostClick(View caller, int adapterViewPosition);
        }
    }

//    private static class ViewHolderFirst extends ViewHolder {
//        private final View mView;
//        private final ImageView ivProfilePic;
//        private final TextView btnSendToSea;
//        private final ImageButton ibLocation;
//        private final ImageButton ibAttachment;
//        private final View mNewPostWrapper;
//
//        private ViewHolderFirst(View view, ViewHolderClickListener listener) {
//            super(view, listener);
//            mView = view;
//            mNewPostWrapper = view.findViewById(R.id.newPostWrapper);
//            ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
//            btnSendToSea = (TextView) view.findViewById(R.id.tvSendToSea);
//            ibLocation = (ImageButton) view.findViewById(R.id.ibLocation);
//            ibAttachment = (ImageButton) view.findViewById(R.id.ibAttachment);
//
//            mNewPostWrapper.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(mView.getContext(), NewPostActivity.class);
//                    mView.getContext().startActivity(i);
//                }
//            });
//        }
//    }
}

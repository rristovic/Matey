package com.mateyinc.marko.matey.activity.adapters;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.OnTouchInterface;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.model.Bulletin;

import static com.mateyinc.marko.matey.data.OperationManager.COL_POST_ID;
import static com.mateyinc.marko.matey.data.OperationManager.ServerStatus.STATUS_RETRY_UPLOAD;
import static com.mateyinc.marko.matey.data.OperationManager.ServerStatus.STATUS_SUCCESS;
import static com.mateyinc.marko.matey.data.OperationManager.ServerStatus.STATUS_UPLOADING;

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
                default:{
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
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getLong(COL_POST_ID));
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, adapterViewPosition);
                mContext.startActivity(i);
            }

            @Override
            public void onBodyClicked(View view, int adapterViewPosition) {
                Intent i = new Intent(mContext, BulletinViewActivity.class);
                mCursor.moveToPosition(adapterViewPosition);
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getLong(COL_POST_ID));
                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, adapterViewPosition);
                mContext.startActivity(i);
            }

            @Override
            public void onNameClick(View view, int adapterViewPosition) {
                Intent i = new Intent(mContext, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mManager.getBulletin(adapterViewPosition, mCursor).getUserID());
                mContext.startActivity(i);
            }

            @Override
            public void onBoostClick(View caller, int adapterViewPosition) {
                DataManager.getInstance(mContext).newPostLike(mCursor, adapterViewPosition);

//Bulletin b = mManager.getBulletin(adapterViewPosition, mCursor);
//                if (b.(mCurUserProfile.getUserId())) { // Unlike
//                    // Remove approve from data and from database
//                    for (UserProfile p : r.replyApproves) {
//                        if (p.getUserId() == mCurUserProfile.getUserId())
//                            r.replyApproves.remove(p);
//                    }
////                    mManager.addReply(r, mCurBulletin, );
//
//                    ((ImageView) caller).setColorFilter(mResources.getColor(light_gray)); // Changing the color of button
//                    ((BulletinViewActivity) mContext).notifyBulletinFragment();
//
//                } else { // Like
//                    // Add approve  to database
//                    r.replyApproves.add(mCurUserProfile); // adding Reply to bulletin
////                    mManager.addReply(r);
//
//                    ((BulletinViewActivity) mContext).notifyBulletinFragment();
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
//             Parsing data to views if available
//            case FIRST_ITEM: {
//                BulletinsAdapter.ViewHolderFirst holder = (ViewHolderFirst) mHolder;
//                holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent i = new Intent(mContext, ProfileActivity.class);
//                        mContext.startActivity(i);
//                    }
//                });
//                holder.ibAttachment.setOnTouchListener((HomeActivity) mContext);
//                holder.ibLocation.setOnTouchListener((HomeActivity) mContext);
//            }

            case FIRST_ITEM:
            case ITEM: {
                BulletinsAdapter.ViewHolder holder = (ViewHolder) mHolder;
                final Bulletin bulletin = mManager.getBulletin(position, mCursor);

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
                holder.tvStats.setText(String.format(mContext.getString(R.string.statistics),
                        bulletin.getNumOfLikes(), bulletin.getNumOfReplies()));
                holder.tvName.setText(bulletin.getFirstName() + " " + bulletin.getLastName());
                holder.tvDate.setText(Util.getReadableDateText(bulletin.getDate()));


//                ((ViewHolder) mHolder).llReplies.removeAllViews(); // First reset the layout then add new views

//                // Adding replies programmatically
//                try {
//                    Resources resources = mContext.getResources();
//                    int repliesCount = bulletin.getNumOfReplies();
//                    int margin = 0;
//                    int marginIncrease = Util.parseDp(15, resources);
//                    int height = Util.parseDp(24, resources);

//                    // Adding image view
//                    RoundedImageView imageView = null;
//                    for (int i = 0; i < repliesCount; i++) {
//                        if (i == 3) break; // Add no more than 3 views
//                        imageView = new RoundedImageView(mContext);
//
//                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height);
//                        params.addRule(RelativeLayout.CENTER_VERTICAL);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_START);
//                        params.leftMargin = margin;
//                        margin += marginIncrease; // increasing margin for next view
//
//                        imageView.setImageResource(R.drawable.empty_photo);
//                        imageView.setOval(true);
//                        imageView.setBorderWidth(0);
//                        imageView.setLayoutParams(params);
//                        imageView.setId(i);
//
////                        holder.llReplies.addView(imageView);
//                    }
//
//
//                    // Adding text view
//                    TextView textView = null;
//                    if (repliesCount > 3) {
//                        textView = new TextView(mContext);
//
//                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//                        layoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
//                        layoutParams.leftMargin = Util.parseDp(2, mContext.getResources());
//                        textView.setLayoutParams(layoutParams);
//
//                        textView.setGravity(Gravity.CENTER_VERTICAL);
//                        String text = String.format(mContext.getString(R.string.bulletin_reply_text), Integer.toString(repliesCount - 3));
//                        textView.setText(text);
//
////                        holder.llReplies.addView(textView);
//
//                    }
//
//                } catch (Exception e) {
//                    Log.e(TAG, e.getLocalizedMessage(), e);
//                }

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

    /**
     * Setting the view to the normal mode
     * @param holder The view holder to be changed
     */
    private void setViewToNormal(ViewHolder holder) {
        if (((LinearLayout) holder.mView).getChildAt(0).getTag() != null)
            ((LinearLayout) holder.mView).removeViewAt(0);

        for (int i = 0; i < ((LinearLayout) holder.mView).getChildCount(); i++) {
            ((LinearLayout) holder.mView).getChildAt(i).setAlpha(1f);
        }

    }

    /**
     * Setting the view to the failed startUploadAction mode
     * @param holder   the view holder which will be changed
     * @param bulletin the bulletin to startUploadAction it's {@link Bulletin#mServerStatus}
     */
    private void setViewToUploadFailed(ViewHolder holder, final Bulletin bulletin) {
        // If mView already has retry text view, don't add it again
        if (((LinearLayout) holder.mView).getChildAt(0).getTag() != null &&
                ((LinearLayout) holder.mView).getChildAt(0).getTag().equals("RetryTextView")) {
            return;
        }

        TextView textView = new TextView(mContext);
        ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)Util.parseDp(30, mContext.getResources()));
        textView.setLayoutParams(params);
        textView.setText("Retry");
        textView.setTag("RetryTextView");

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SessionManager.getInstance(mContext).uploadNewBulletin(bulletin, MotherActivity.access_token, mContext);
//                mManager.updateBulletinServerStatus(bulletin, STATUS_UPLOADING);
            }
        });
        ((LinearLayout) holder.mView).addView(textView, 0);

        for (int i = 1; i < ((LinearLayout) holder.mView).getChildCount(); i++) {
            ((LinearLayout) holder.mView).getChildAt(i).setAlpha(0.4f);
        }
    }

    /**
     * Setting the view to the uploading mode
     * @param holder the view holder to be changed
     */
    private void setViewToUploading(ViewHolder holder) {
        if (((LinearLayout) holder.mView).getChildAt(0).getTag() != null)
            ((LinearLayout) holder.mView).removeViewAt(0);

        for (int i = 0; i < ((LinearLayout) holder.mView).getChildCount(); i++) {
            ((LinearLayout) holder.mView).getChildAt(i).setAlpha(0.4f);
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

        private final View mView;
        private final TextView tvMessage;
        private final TextView tvName, tvDate, tvStats, tvSubject;
        private final RelativeLayout rlBody;
        private final LinearLayout btnReply, btnBoost;
        private final ImageView ivBookmark, ivShare;
        ViewHolderClickListener mListener;

        private static final String BTN_REPLY_TAG = "replytag";
        private static final String BTN_BOOST_TAG = "boosttag";
        private static final String TV_NAME_TAG = "nametag";
        private static final String RL_BODY_TAG = "bodytag";

        private ViewHolder(View view, ViewHolderClickListener listener) {
            super(view);
            mView = view;
            tvMessage = (TextView) view.findViewById(R.id.tvMessage);
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvName.setTag(TV_NAME_TAG);
            tvSubject = (TextView) view.findViewById(R.id.tvSubject);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvStats = (TextView) view.findViewById(R.id.tvStats);
            rlBody = (RelativeLayout) view.findViewById(R.id.rlBody);
            rlBody.setTag(RL_BODY_TAG);
            btnReply = (LinearLayout) view.findViewById(R.id.llReply);
            btnReply.setTag(BTN_REPLY_TAG);
            btnBoost = (LinearLayout) view.findViewById(R.id.llBoost);
            btnBoost.setTag(BTN_BOOST_TAG);
            ivBookmark = (ImageView) view.findViewById(R.id.ivBookmark);
            ivShare = (ImageView) view.findViewById(R.id.ivShare);
            mListener = listener;

            rlBody.setOnClickListener(this);
            btnReply.setOnTouchListener(new OnTouchInterface(mView.getContext()));
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

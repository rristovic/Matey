package com.mateyinc.marko.matey.adapters;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;


public class RepliesAdapter extends RecycleNoSQLAdapter {
    private final String TAG = RepliesAdapter.class.getSimpleName();

    public RepliesAdapter(MotherActivity context) {
        super(context);
    }

    public interface ReplyClickedInterface {
        void showPopupWindow(Reply reply);

        void showReplyKeyboard();
    }

    private RecyclerView mRecycleView;
    private UserProfile mCurUserProfile;
    private Resources mResources;
    private boolean mOnlyShowReplies = true;
    private Bulletin mCurBulletin;

    private int ITEM = 1;
    private int FIRST_ITEM = 0;

    /**
     * Used for showing popup windows for replying to reply
     **/
    private ReplyClickedInterface showPopupInterface = null;


    private void init() {
        mCurUserProfile = new UserProfile();
        mResources = mContext.getResources();
    }

    @Override
    public int getItemViewType(int position) {
        if (mOnlyShowReplies)
            return ITEM;
        return position == 0 ? FIRST_ITEM : ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.reply_replies_list_item, parent, false);

        if (viewType == FIRST_ITEM) {
            LinearLayout linearLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(layoutParams);

            // Bulletin view
            View topView = LayoutInflater.from(mContext)
                    .inflate(R.layout.bulletin_list_item, parent, false);

            // Divider view
            FrameLayout divider = new FrameLayout(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Util.parseDp(0.5f, mContext.getResources()));
            divider.setLayoutParams(params);
            divider.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));

            // Label view
            TextView textView = new TextView(mContext);
            textView.setTag("repliestext");
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, R.dimen.bulletin_textSize_message);
            textView.setText(R.string.bulletin_repliesView_repliesText);
            textView.setLayoutParams(params);

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                topView.setElevation(0f);
            }

            linearLayout.addView(topView);
            linearLayout.addView(divider);
            linearLayout.addView(textView);
            linearLayout.addView(view);

            return new ViewHolder(linearLayout, getViewHolderListener(), showPopupInterface != null);
        } else {
            return new ViewHolder(view, getViewHolderListener(), showPopupInterface != null);
        }
    }


    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {

            public void onRepliesClick(View caller, View rootView, boolean onlyShowReplies) {
//                int position = mRecycleView.getChildAdapterPosition(rootView);
//                if (onlyShowReplies) {
//                    Intent i = new Intent(mContext, BulletinViewActivity.class);
//                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, position);
//                    mContext.startActivity(i);
//                } else {
//                    Intent i = new Intent(mContext, BulletinViewActivity.class);
//                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, position);
//                    i.putExtra(BulletinViewActivity.EXTRA_NEW_REPLY, true);
//                    mContext.startActivity(i);
//                }
            }

            @Override
            public void onApproveClicked(View caller, int position) {
                mManager.newReplyLike(position, mContext);
            }

            @Override
            public void onShowApprovesClicked(View caller, View rootView) {
                // TODO - finish method
            }

            @Override
            public void onNameClicked(View caller, View rootView) {
                int position = mRecycleView.getChildAdapterPosition(rootView);
                Intent i = new Intent(mContext, ProfileActivity.class);
                Reply reply = (Reply) mData.get(position);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, reply.getUserId());
                mContext.startActivity(i);
            }

            @Override
            public void onReplyClick(View caller, int adapterViewPosition) {
                Reply r = (Reply) mData.get(adapterViewPosition);
                showPopupInterface.showPopupWindow(r);
            }
        };
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        if (getItemViewType(position) == FIRST_ITEM) {
            RepliesAdapter.ViewHolder holder = (ViewHolder) mHolder;
            ((TextView) holder.mView.findViewById(R.id.tvBulletinUserName)).setText(mCurBulletin.getFirstName().concat(" ").concat(mCurBulletin.getLastName()));
            ((TextView) holder.mView.findViewById(R.id.tvBulletinDate)).setText(Util.getReadableDateText(mCurBulletin.getDate()));
            ((TextView) holder.mView.findViewById(R.id.tvBulletinSubject)).setText(mCurBulletin.getSubject());
            ((TextView) holder.mView.findViewById(R.id.tvBulletinMessage)).setText(mCurBulletin.getMessage());
            ((TextView) holder.mView.findViewById(R.id.tvBulletinStats)).setText(mCurBulletin.getStatistics(mContext));
            LinearLayout llReply = (LinearLayout) holder.mView.findViewById(R.id.llBulletinReply);
            LinearLayout llBoost = (LinearLayout) holder.mView.findViewById(R.id.llBoost);

            llReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupInterface.showReplyKeyboard();
                }
            });

            llBoost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mManager.boostPost(mCurBulletin, mContext);
                    mContext.getContentResolver().notifyChange(DataContract.ReplyEntry.CONTENT_URI, null);
                }
            });

            holder.mView.findViewById(R.id.ivBulletinProfilePic).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCurBulletin.getUserID());
                    mContext.startActivity(i);
                }
            });
        }
        Reply reply = (Reply) mData.get(position);
        RepliesAdapter.ViewHolder holder = (ViewHolder) mHolder;

        String text = reply.getUserFirstName() + " " + reply.getUserLastName();
        holder.tvName.setText(text);
        holder.tvDate.setText(Util.getReadableDateText(reply.getReplyDate()));
        holder.tvMessage.setText(reply.getReplyText());
        holder.tvStats.setText(reply.getStatistics(mContext));
    }

    /**
     * Tells adapter to show main post of replies as first post.
     * For viewing bulletin this method should be called to show bulletin first,
     * but when viewing replies of reply this method should not be called.
     *
     * @param b {@link Bulletin} bulletin to show.
     */
    public void showMainPostInfo(Bulletin b) {
        mCurBulletin = b;
        mOnlyShowReplies = false;
    }

    /**
     * Method to call when new data has been downloaded
     *
     * @param b {@link Bulletin} object that contains parsed replies
     */
    public void setBulletin(Bulletin b) {
        mCurBulletin = b;
        mData = b.getReplies();
        notifyDataSetChanged();
    }

    /**
     * Setting communication interface so popup window and keyboard can show up when clicked in reply button.
     *
     * @param replyClickedInterface {@link ReplyClickedInterface} interface to setup in adapter
     */
    public void setReplyPopupInterface(ReplyClickedInterface replyClickedInterface) {
        showPopupInterface = replyClickedInterface;
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG_NAME = "name";
        private static final String TAG_SHOW_APPROVES = "showapprvs";

        private static final String TV_MESSAGE = "tvMessage";
        private static final String BTN_REPLY_TAG = "replytag";
        private static final String BTN_ARR_TAG = "arrtag";

        final LinearLayout mView;
        final TextView tvMessage;
        final TextView tvName, tvDate, tvStats;
        final ImageView ivProfilePic;
        final LinearLayout btnReply, btnArr;

        private final ViewHolderClickListener mListener;

        public ViewHolder(View view, ViewHolderClickListener listener, boolean showReplyButton) {
            super(view);
            mView = (LinearLayout) view;

            // Like button
            btnArr = (LinearLayout) view.findViewById(R.id.llArr);
            btnArr.setTag(BTN_ARR_TAG);
            // Reply button
            btnReply = (LinearLayout) view.findViewById(R.id.llReReply);
            btnReply.setTag(BTN_REPLY_TAG);
            // Stats
            tvStats = (TextView) view.findViewById(R.id.tvReplyStats);
            // Remove reply button when viewing replies of reply
            if (!showReplyButton) {
                ((RelativeLayout) mView.findViewById(R.id.rlReplyInfo)).removeView(btnReply);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvStats.getLayoutParams();
                params.addRule(RelativeLayout.RIGHT_OF, R.id.llArr);
                params.addRule(RelativeLayout.END_OF, R.id.llArr);
                tvStats.setLayoutParams(params);
            } else {
                btnReply.setTag(BTN_REPLY_TAG);
            }
            // Text view message
            tvMessage = (TextView) view.findViewById(R.id.tvReplyMessage);
            tvMessage.setTag(TV_MESSAGE);
            // text view name
            tvName = (TextView) view.findViewById(R.id.tvReplyName);
            tvName.setTag(TAG_NAME);
            // Date
            tvDate = (TextView) view.findViewById(R.id.tvReplyTime);
            // Profile pic
            ivProfilePic = (ImageView) view.findViewById(R.id.ivReplyProfilePic);
            ivProfilePic.setTag(TAG_NAME);

            mListener = listener;
            tvName.setOnClickListener(this);
            ivProfilePic.setOnClickListener(this);
            tvMessage.setOnClickListener(this);
            btnArr.setOnClickListener(this);
            if (showReplyButton)
                btnReply.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String tag = v.getTag().toString();
            if (tag.equals(TAG_NAME)) {
                mListener.onNameClicked(v, mView);
            } else if (tag.equals(BTN_ARR_TAG)) {
                mListener.onApproveClicked(v, getAdapterPosition());
            } else if (tag.equals(TAG_SHOW_APPROVES)) {
                mListener.onShowApprovesClicked(v, mView);
            } else if (tag.equals(BTN_REPLY_TAG)) {
                mListener.onReplyClick(v, getAdapterPosition());
            } else if (tag.equals(TV_MESSAGE)) {
                mListener.onReplyClick(v, getAdapterPosition());
            }
        }

        protected interface ViewHolderClickListener {
            void onApproveClicked(View caller, int adapterViewPosition);

            void onShowApprovesClicked(View caller, View rootView);

            void onNameClicked(View caller, View rootView);

            void onReplyClick(View caller, int adapterViewPosition);
        }
    }
}

package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by Sarma on 9/5/2016.
 */
public class BulletinRepliesAdapter extends RecycleCursorAdapter {
    private final String TAG = BulletinRepliesAdapter.class.getSimpleName();

    private RecyclerView mRecycleView;
    private UserProfile mCurUserProfile;
    private Resources mResources;


    public BulletinRepliesAdapter(Context context, RecyclerView view, LinkedList data) {
        mContext = context;
        mRecycleView = view;
        mManager = DataManager.getInstance(context);

        init();
    }

    public BulletinRepliesAdapter(Context context, RecyclerView view) {
        mContext = context;
        mRecycleView = view;
        mManager = DataManager.getInstance(context);

        init();
    }

    private void init() {
        mCurUserProfile = new UserProfile(Util.getCurrentUserProfileId());
        mResources = mContext.getResources();
    }


    @Override
    public BulletinRepliesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.bulletin_replies_list_item, parent, false);

        // Implemented ViewHolderClickListener interface from view holder to handle the clicks
        return new ViewHolder(view, getViewHolderListener());

    }


    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {
            public void onRepliesClick(View caller, View rootView, boolean onlyShowReplies) {
//                int position = mRecycleView.getChildAdapterPosition(rootView);
//                if (onlyShowReplies) {
//                    Intent i = new Intent(mContext, BulletinRepliesViewActivity.class);
//                    i.putExtra(BulletinRepliesViewActivity.EXTRA_BULLETIN_ID, position);
//                    mContext.startActivity(i);
//                } else {
//                    Intent i = new Intent(mContext, BulletinRepliesViewActivity.class);
//                    i.putExtra(BulletinRepliesViewActivity.EXTRA_BULLETIN_ID, position);
//                    i.putExtra(BulletinRepliesViewActivity.EXTRA_NEW_REPLY, true);
//                    mContext.startActivity(i);
//                }
            }

            @Override
            public void onApproveClicked(View caller, View rootView) {
//                int position = mRecycleView.indexOfChild(rootView); // Get child position in adapter
//                Bulletin.Reply r = mData.get(position);
//                if (r.hasReplyApproveWithId(mCurUserProfile.getUserId())) { // Unlike
//
//                    // Remove approve from data and from database
//                    r.replyApproves.remove(mCurUserProfile);
////                    mManager.removeReplyApprove(BulletinRepliesViewActivity.mBulletinPos, r.replyId);
//
////                    r.removeReplyApproveWithId(mCurUserProfile.getUserId());
//                    ((ImageView) caller).setColorFilter(mResources.getColor(R.color.light_gray)); // Changing the color of button
//                    BulletinRepliesAdapter.this.notifyItemChanged(position); // notify adapter of item changed
//                } else { // Like
//
//                    // Add approve to data and to database
//                    r.replyApproves.add(mCurUserProfile); // adding Reply to bulletin
////                    mManager.addReplyApprove(BulletinRepliesViewActivity.mBulletinPos, r.replyId);
//
//                    BulletinRepliesAdapter.this.notifyItemChanged(position); // notify adapter of item changed
//                }
            }

            @Override
            public void onShowApprovesClicked(View caller, View rootView) {
                // TODO - finish method
            }

            @Override
            public void onNameClicked(View caller, View rootView) {
//                int position = mRecycleView.getChildAdapterPosition(rootView);
//                Intent i = new Intent(mContext, ProfileActivity.class);
//                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mData.get(position).userId);
//                mContext.startActivity(i);
            }
        };
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        Bulletin.Reply reply = mManager.getReply(position, mCursor);
        BulletinRepliesAdapter.ViewHolder holder = (ViewHolder) mHolder;

        String text = reply.userFirstName + " " + reply.userLastName;
        holder.tvName.setText(text);
        holder.tvDate.setText(Util.getReadableDateText(reply.replyDate));
        holder.tvMessage.setText(reply.replyText);
        text = "%d "+ mContext.getString(R.string.reply_approve);
        text = String.format(Locale.US, text, reply.replyApproves.size());
        holder.tvApproves.setText(text);

        // Check if current user has liked the comment
        if (reply.hasReplyApproveWithId(mCurUserProfile.getUserId())) {
            holder.ivApprove.setColorFilter(mResources.getColor(R.color.blue));
        }

    }


    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG_NAME = "name";
        private static final String TAG_APPROVE = "approvebtn";
        private static final String TAG_SHOW_APPROVES = "showapprvs";

        public final View mView;
        public final TextView tvMessage;
        public final TextView tvName;
        public final TextView tvDate;
        public final TextView tvApproves;
        public final ImageView ivApprove;
        private final ViewHolderClickListener mListener;

        public ViewHolder(View view, ViewHolderClickListener listener) {
            super(view);
            mView = view;
            tvMessage = (TextView) view.findViewById(R.id.tvText);
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvDate = (TextView) view.findViewById(R.id.tvTime);
            tvApproves = (TextView) view.findViewById(R.id.tvApproves);
            ivApprove = (ImageView) view.findViewById(R.id.ivApprove);
            mListener = listener;

            ivApprove.setTag(TAG_APPROVE);
            tvApproves.setTag(TAG_SHOW_APPROVES);
            tvName.setTag(TAG_NAME);

            ivApprove.setOnClickListener(this);
            tvApproves.setOnClickListener(this);
            tvName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String tag = v.getTag().toString();
            if (tag.equals(TAG_NAME)) {
                mListener.onNameClicked(v, mView);
            } else if (tag.equals(TAG_APPROVE)) {
                mListener.onApproveClicked(v, mView);
            } else if (tag.equals(TAG_SHOW_APPROVES)) {
                mListener.onShowApprovesClicked(v, mView);
            }
        }

        protected interface ViewHolderClickListener {
            void onApproveClicked(View caller, View rootView);
            void onShowApprovesClicked(View caller, View rootView);
            void onNameClicked(View caller, View rootView);
        }
    }
}

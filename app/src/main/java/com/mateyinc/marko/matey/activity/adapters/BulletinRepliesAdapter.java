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
import com.mateyinc.marko.matey.activity.view.BulletinRepliesViewActivity;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by Sarma on 9/5/2016.
 */
public class BulletinRepliesAdapter extends RecyclerView.Adapter<BulletinRepliesAdapter.ViewHolder> {
    private final String TAG = BulletinRepliesAdapter.class.getSimpleName();

    private final DataManager mManager;
    private LinkedList<Bulletin.Reply> mData;
    private final Context mContext;
    private RecyclerView mRecycleView;
    private UserProfile mCurUserProfile;
    private Resources mResources;

    public BulletinRepliesAdapter(Context context, RecyclerView view, LinkedList data) {
        mContext = context;
        mData = data;
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

    public void setData(LinkedList data) {
        mData = data;
        this.notifyDataSetChanged();
    }

    @Override
    public BulletinRepliesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.bulletin_replies_list_item, parent, false);

        // Implemented ViewHolderClickListener interface from view holder to handle the clicks
        return new ViewHolder(view, new ViewHolder.ViewHolderClickListener() {
            public void onApproveClicked(View caller, View rootView) {
                    int position = mRecycleView.indexOfChild(rootView); // Get child position in adapter
                    Bulletin.Reply r = mData.get(position);
                    LinkedList list = r.replyApproves;
                    if (r.hasReplyApproveWithId(mCurUserProfile.getUserId())) { // Unlike
                        mManager.removeReplyApprove(BulletinRepliesViewActivity.mBulletinPos, r.replyId);

                        r.removeReplyApproveWithId(mCurUserProfile.getUserId());
                        ((ImageView) caller).setColorFilter(mResources.getColor(R.color.light_gray));
                        BulletinRepliesAdapter.this.notifyItemChanged(position); // notify adapter of item changed
                    } else { // Like
                        list.add(mCurUserProfile); // adding Reply to bulletin
                        mManager.addReplyApprove(BulletinRepliesViewActivity.mBulletinPos, r.replyId);
                        BulletinRepliesAdapter.this.notifyItemChanged(position); // notify adapter of item changed
                    }
            }
        });

    }


    @Override
    public void onBindViewHolder(final BulletinRepliesAdapter.ViewHolder mHolder, final int position) {
        Bulletin.Reply reply = mData.get(position);

        String text = reply.userFirstName + " " + reply.userLastName;
        mHolder.tvName.setText(text);
        mHolder.tvDate.setText(Util.getReadableDateText(reply.replyDate));
        mHolder.tvMessage.setText(reply.replyText);
        text = "%d "+ mContext.getString(R.string.reply_approve);
        text = String.format(Locale.US, text, reply.replyApproves.size());
        mHolder.tvApproves.setText(text);

        // Check if current user has liked the comment
        if (reply.hasReplyApproveWithId(mCurUserProfile.getUserId())) {
            mHolder.ivApprove.setColorFilter(mResources.getColor(R.color.blue));
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

            ivApprove.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            mListener.onApproveClicked(v, mView);
//            } else {
//                mListener.onPotato(v);
//            }
        }

        protected interface ViewHolderClickListener {
            void onApproveClicked(View caller, View rootView);
        }
    }
}

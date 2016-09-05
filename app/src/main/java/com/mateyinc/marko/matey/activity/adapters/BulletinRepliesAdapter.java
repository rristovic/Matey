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
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.LinkedList;

/**
 * Created by Sarma on 9/5/2016.
 */
public class BulletinRepliesAdapter extends RecyclerView.Adapter<BulletinRepliesAdapter.ViewHolder> {
    private final String TAG = BulletinRepliesAdapter.class.getSimpleName();

    private LinkedList<Bulletin.Reply> mData;
    private final Context mContext;
    private RecyclerView mRecycleView;
    private UserProfile mCurUserProfile;
    private Resources mResources;

    public BulletinRepliesAdapter(Context context, RecyclerView view, LinkedList data) {
        mContext = context;
        mData = data;
        mRecycleView = view;

        init();
    }

    public BulletinRepliesAdapter(Context context, RecyclerView view) {
        mContext = context;
        mRecycleView = view;

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

        return new ViewHolder(view, new ViewHolder.ViewHoledClicks() { // Implemented ViewHolderClicks interface from view holder to handle the clicks
            public void onApproveClicked(View caller, View rootView) {
                int position = mRecycleView.indexOfChild(rootView); // Get child position in adapter
                 LinkedList list = mData.get(position).replyApproves;
                if(list.contains(mCurUserProfile)){ // Unlike
                    list.remove(mCurUserProfile);
                    ((ImageView)caller).setColorFilter(mResources.getColor(R.color.light_gray));
                    BulletinRepliesAdapter.this.notifyItemChanged(position); // notify adapter of item changed
                }else { // Like
                    list.add(mCurUserProfile); // adding Reply to bulletin
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
        mHolder.tvDate.setText(reply.replyDate);
        mHolder.tvMessage.setText(reply.replyText);
        text = reply.replyApproves.size() + " approves";
        mHolder.tvApproves.setText(text);

        // Check if current user has liked the comment
        if(reply.replyApproves.contains(mCurUserProfile)){
            mHolder.ivApprove.setColorFilter(mResources.getColor(R.color.blue));
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView tvMessage;
        public final TextView tvName;
        public final TextView tvDate;
        public final TextView tvApproves;
        public final ImageView ivApprove;
        private final ViewHoledClicks mListener;

        public ViewHolder(View view, ViewHoledClicks listener) {
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

        public static interface ViewHoledClicks {
            public void onApproveClicked(View caller, View rootView);
        }
    }
}

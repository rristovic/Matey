package com.mateyinc.marko.matey.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.home.GroupActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.Group;

import java.util.ArrayList;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {


    private final Context mContext;
    private final OperationManager mManager;

    public ArrayList<Group> mData;

    public GroupsAdapter(Context context) {
        mData = new ArrayList<>();
        mContext = context;
        mManager = OperationManager.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view, new ViewHolder.ViewHolderClickListener() {
            @Override
            public void onClick(int adapterViewPosition) {
                Intent i = new Intent(mContext, GroupActivity.class);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Parsing data to views if available
//        try {
//            holder.mNotifText.setText("null");
//        } catch (Exception e) {
//            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//        }
    }

    @Override
    public int getItemCount() {
        return 200;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final View mView;
        final ImageView ivPicture;
        final TextView tvStats, tvName;
        final ViewHolderClickListener mListener;

        static final String TAG_STATS = "stats_tag";

        public ViewHolder(View view, ViewHolderClickListener listener) {
            super(view);
            mView = view;
            mListener = listener;
            ivPicture = (ImageView) mView.findViewById(R.id.ivPicture);
            tvStats = (TextView) mView.findViewById(R.id.tvGroupStats);
            tvName = (TextView) mView.findViewById(R.id.tvGroupName);

            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(getAdapterPosition());
        }

        protected interface ViewHolderClickListener {
            void onClick(int adapterViewPosition);
        }
    }
}


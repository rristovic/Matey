package com.mateyinc.marko.matey.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.Notification;

import java.util.ArrayList;

public class NotificationsAdapter extends RecycleNoSQLAdapter<Notification> {


    private final Context mContext;
    private final OperationManager mManager;

    public NotificationsAdapter(MotherActivity context) {
        super(context);
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
                Notification n = mData.get(adapterViewPosition);
                Intent i = n.buildIntent(mContext);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Notification n = mData.get(position);

        final ViewHolder view = (ViewHolder) holder;
        view.tvName.setText(n.getmMessage());
        view.tvStats.setVisibility(View.GONE);

        mManager.mImageLoader.get(n.getmPicUrl(),
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        view.ivPicture.setImageBitmap(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        view.ivPicture.setImageResource(R.drawable.empty_photo);
                    }
                }, view.ivPicture.getWidth(), view.ivPicture.getHeight());

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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


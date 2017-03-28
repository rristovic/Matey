package com.mateyinc.marko.matey.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Notification;

/**
 * Used in profile activity to list activities.
 */
public class ActivitiesAdapter extends TemporaryDataRecycleAdapter<Notification> {

    public ActivitiesAdapter(MotherActivity context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.group_list_item, parent, false);
        return new NotificationsAdapter.ViewHolder(view, new NotificationsAdapter.ViewHolder.ViewHolderClickListener() {
            @Override
            public void onClick(int adapterViewPosition) {
                Notification n = getItem(adapterViewPosition);
                Intent i = n.buildIntent(mContext);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Notification n = getItem(position);

        final NotificationsAdapter.ViewHolder view = (NotificationsAdapter.ViewHolder) holder;
        view.tvName.setText(Util.fromHtml(n.buildNotificationMessage(mContext)));
        view.tvStats.setVisibility(View.GONE);

        mManager.mImageLoader.get(n.buildIconUrl(),
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

}

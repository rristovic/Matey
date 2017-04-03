package com.mateyinc.marko.matey.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;


public class GroupActivityAdapter extends BulletinsAdapter {
    public GroupActivityAdapter(MotherActivity context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext) //Inflate the view
                .inflate(R.layout.bulletin_list_item, parent, false);

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = layoutParams.rightMargin = 0;
        view.setLayoutParams(layoutParams);

        // Implementing ViewHolderClickListener and returning view holder
        return new ViewHolder(view, getViewHolderListener());
    }
}

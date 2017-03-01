package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.OperationManager;

/**
 * Created by Sarma on 8/30/2016.
 */
public class GroupsRecyclerViewAdapter extends RecyclerView.Adapter<GroupsRecyclerViewAdapter.ViewHolder> {


    private final Context mContext;
    private final OperationManager mManager;

    public GroupsRecyclerViewAdapter(Context context) {
        mContext = context;
        mManager = OperationManager.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.groups_discover_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Parsing data to views if available
        try {
            holder.mNotifText.setText("null");
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public  ImageView mProfilePic;
        public final TextView mNotifText;
        public  TextView mNotifTime;

        public ViewHolder(View view) {
            super(view);
            mView = view;
//            mProfilePic = (ImageView) view.findViewById(R.id.ivNotifProfilePic);
            mNotifText = (TextView) view.findViewById(R.id.tvMessage);
//            mTime = (TextView) view.findViewById(R.id.tvNotifTime);
        }
    }
}


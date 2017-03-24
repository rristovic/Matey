package com.mateyinc.marko.matey.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Group;
import com.mateyinc.marko.matey.model.MModel;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter {

    private final Context mContext;
    private List<MModel> mData = new ArrayList<>(0);

    private final int FRAG_TOP = 0;
    private final int FRAG_USER = 1;
    private final int FRAG_GROUP = 2;
    private final int FRAG_BULLETIN = 3;
    // Indicates fragment position in view pager
    private final int mFragPos;
    private boolean mShowSection = false;

    public SearchAdapter(MotherActivity context, int fragPos) {
        mFragPos = fragPos;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_list_item, parent, false);

        return new ViewHolder(view, new ViewHolder.OnItemClickedListener() {
            @Override
            public void onClick(int position) {

            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder h = (ViewHolder) holder;
        String title;
        MModel model = mData.get(position);
        if (model instanceof UserProfile) {
            title = ((UserProfile) model).getFullName();
        } else if (model instanceof Group) {
            title = ((Group) model).getGroupName();
        } else {
            title = ((Bulletin) model).getSubject();
        }

        h.tvTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<MModel> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * Method to call when section should be displayed or not.
     *
     * @param show set to true if section should be displayed.
     */
    public void showSection(boolean show) {
        this.mShowSection = show;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final OnItemClickedListener mListener;
        private TextView tvTitle;

        interface OnItemClickedListener {
            void onClick(int position);
        }

        public ViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getAdapterPosition());
                }
            });
        }


    }

}

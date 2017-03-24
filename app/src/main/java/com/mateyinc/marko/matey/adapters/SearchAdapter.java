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
    private static final String TAG = "SearchAdapter";
    /**
     * State of ListView item that has never been determined.
     */
    private static final int STATE_UNKNOWN = 0;

    /**
     * State of a ListView item that is sectioned. A sectioned item must
     * display the separator.
     */
    private static final int STATE_SECTIONED_CELL = 1;

    /**
     * State of a ListView item that is not sectioned and therefore does not
     * display the separator.
     */
    private static final int STATE_REGULAR_CELL = 2;
    private int[] mCellStates;

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
        MModel model = mData.get(position);

        if(mShowSection) {
            boolean needSeparator = false;
            switch (mCellStates[position]) {
                case STATE_SECTIONED_CELL:
                    needSeparator = true;
                    break;

                case STATE_REGULAR_CELL:
                    needSeparator = false;
                    break;

                case STATE_UNKNOWN:
                default:
                    // A separator is needed if it's the first itemview of the
                    // ListView or if the group of the current cell is different
                    // from the previous itemview.
                    if (position == 0) {
                        needSeparator = true;
                    } else {
                        if (!model.getClass()
                                .equals(mData.get(position - 1).getClass()))
                            needSeparator = true;
                    }

                    // Cache the result
                    mCellStates[position] = needSeparator ? STATE_SECTIONED_CELL : STATE_REGULAR_CELL;
                    break;
            }

            if (needSeparator) {
                h.tvSectionTitle.setVisibility(View.VISIBLE);
                String modelClass = model.getClass().getSimpleName();
                if (modelClass.equals(UserProfile.class.getSimpleName())) {
                    h.tvSectionTitle.setText(R.string.search_sectionName_profiles);
                } else if (modelClass.equals(Group.class.getSimpleName())) {
                    h.tvSectionTitle.setText(R.string.search_sectionName_groups);
                } else if (modelClass.equals(Bulletin.class.getSimpleName())) {
                    h.tvSectionTitle.setText(R.string.search_sectionName_bulletins);
                }
            } else {
                h.tvSectionTitle.setVisibility(View.GONE);
            }
        }

        if (mFragPos == FRAG_TOP) {
            bindAll(h, model);
        } else if (mFragPos == FRAG_BULLETIN)
            bindBulletin(h, model);
        else if (mFragPos == FRAG_USER)
            bindUser(h, model);
        else if (mFragPos == FRAG_GROUP)
            bindGroup(h, model);
    }

    private void bindAll(ViewHolder holder, MModel model) {
        bindUser(holder, model);
        bindGroup(holder, model);
        bindBulletin(holder, model);
    }

    private void bindGroup(ViewHolder holder, MModel model) {
        if (model instanceof Group) {
            String title = ((Group) model).getGroupName();
            holder.tvTitle.setText(title);
        }
    }

    private void bindUser(ViewHolder holder, MModel model) {
        if (model instanceof UserProfile) {
            String title = ((UserProfile) model).getFullName();
            holder.tvTitle.setText(title);
        }
    }

    private void bindBulletin(ViewHolder holder, MModel model) {
        if (model instanceof Bulletin) {
            String title = ((Bulletin) model).getSubject();
            holder.tvTitle.setText(title);
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<MModel> data) {
        this.mData = data;
        mCellStates = data == null ? null : new int[data.size()];
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
        private TextView tvSectionTitle;

        interface OnItemClickedListener {
            void onClick(int position);
        }

        public ViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvSectionTitle = (TextView) itemView.findViewById(R.id.tvSectionName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getAdapterPosition());
                }
            });
        }


    }

}

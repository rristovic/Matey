package com.mateyinc.marko.matey.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.activity.view.GroupActivity;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
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
    private final DataAccess mDataAccess;
    private final OperationManager mManager;

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
        mDataAccess = DataAccess.getInstance(context);
        mManager = OperationManager.getInstance(context);
        setData();
    }

    private void setData() {
        mData.clear();
        switch (mFragPos) {
            default:
            case FRAG_TOP:
                mData.addAll(mDataAccess.mUserSearchList);
                mData.addAll(mDataAccess.mGroupSearchList);
                mData.addAll(mDataAccess.mBulletinSearchList);
                break;
            case FRAG_USER:
                mData.addAll(mDataAccess.mUserSearchList);
                break;
            case FRAG_GROUP:
                mData.addAll(mDataAccess.mGroupSearchList);
                break;
            case FRAG_BULLETIN:
                mData.addAll(mDataAccess.mBulletinSearchList);
                break;
        }
        mCellStates = mData == null ? null : new int[mData.size()];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_list_item, parent, false);

        return new ViewHolder(view, new ViewHolder.OnItemClickedListener() {
            @Override
            public void onClick(int position) {
                MModel model = mData.get(position);
                if (model instanceof UserProfile) {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, model.getId());
                    mContext.startActivity(i);
                } else if (model instanceof Group) {
                    Intent i = new Intent(mContext, GroupActivity.class);
                    i.putExtra(GroupActivity.EXTRA_GROUP_ID, model.getId());
                    mContext.startActivity(i);
                } else {
                    Intent i = new Intent(mContext, BulletinViewActivity.class);
                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, model.getId());
                    mContext.startActivity(i);
                }
            }

            @Override
            public void onToggleButton(int position, boolean isChecked) {
                MModel model = mData.get(position);
                if (model instanceof UserProfile) {
                    if (isChecked) {
                        mManager.followNewUser((UserProfile) model, mContext);
                    } else {
                        mManager.unfollowUser((UserProfile) model, mContext);
                    }
                } else {
                    if (isChecked) {
                        mManager.followGroup((Group) model, mContext);
                    } else {
                        mManager.unfollowGroup((Group) model, mContext);
                    }
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder h = (ViewHolder) holder;
        MModel model = mData.get(position);
        resetView(h);

        boolean needSeparator = false;
        if (mShowSection) {
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

        // try to bind view, if fail remove view from ui
        boolean binded = false;
        if (mFragPos == FRAG_TOP) {
            binded = bindAll(h, model);
        } else if (mFragPos == FRAG_BULLETIN)
            binded = bindBulletin(h, model);
        else if (mFragPos == FRAG_USER)
            binded = bindUser(h, model);
        else if (mFragPos == FRAG_GROUP)
            binded = bindGroup(h, model);
        if (!binded)
            h.itemView.setVisibility(View.GONE);
        else
            h.itemView.setVisibility(View.VISIBLE);
    }

    private void resetView(ViewHolder h) {
        h.tvTitle.setText(null);
        h.ivPicture.setVisibility(View.VISIBLE);
        h.btnSail.setVisibility(View.VISIBLE);
    }


    private boolean bindAll(ViewHolder holder, MModel model) {
        return bindUser(holder, model) ||
                bindGroup(holder, model) ||
                bindBulletin(holder, model);
    }

    private boolean bindGroup(ViewHolder holder, MModel model) {
        if (model instanceof Group) {
            Group g = (Group) model;
            String title = g.getGroupName();
            holder.btnSail.setChecked(g.isFollowed());
            holder.tvTitle.setText(title);
            holder.tvInfo.setText(String.format(mContext.getString(R.string.groups_statistics), g.getNumOfFollowers()));
            return true;
        } else
            return false;
    }

    private boolean bindUser(ViewHolder holder, MModel model) {
        if (model instanceof UserProfile) {
            UserProfile u = (UserProfile) model;
            String title = u.getFullName();
            holder.btnSail.setChecked(u.isFollowed());
            holder.tvTitle.setText(title);
            holder.tvInfo.setText(u.getLocation());
            return true;
        } else
            return false;
    }

    private boolean bindBulletin(ViewHolder holder, MModel model) {
        if (model instanceof Bulletin) {
            Bulletin b = (Bulletin) model;
            String title = b.getSubject();
            holder.tvTitle.setText(title);
            holder.tvInfo.setText(b.getStatistics(mContext));
            holder.btnSail.setVisibility(View.GONE);
            holder.ivPicture.setVisibility(View.GONE);
            return true;
        } else
            return false;
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    public void notifyDataChanged() {
        setData();
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

        final OnItemClickedListener mListener;
        TextView tvTitle;
        TextView tvSectionTitle;
        TextView tvInfo;
        ImageView ivPicture;
        ToggleButton btnSail;

        interface OnItemClickedListener {
            void onClick(int position);

            void onToggleButton(int position, boolean isChecked);
        }

        public ViewHolder(View itemView, OnItemClickedListener listener) {
            super(itemView);
            mListener = listener;
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvSectionTitle = (TextView) itemView.findViewById(R.id.tvSectionName);
            tvInfo = (TextView) itemView.findViewById(R.id.tvInfo);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivSearchPicture);
            btnSail = (ToggleButton) itemView.findViewById(R.id.btnSail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getAdapterPosition());
                }
            });

            btnSail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mListener.onToggleButton(getAdapterPosition(), isChecked);
                }
            });
        }
    }
}

package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.ArrayList;
import java.util.Date;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Bulletin} and makes a call to the
 * specified {@link BulletinsFragment.OnListFragmentInteractionListener}.
 */
public class BulletinRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Bulletin> mData;
    private final BulletinsFragment.OnListFragmentInteractionListener mListener;
    private final Context mContext;
    private final BulletinManager mManager;
    private static final int FIRST_ITEM = 1;
    private static final int ITEM = 2;

    public BulletinRecyclerViewAdapter(Context context, BulletinsFragment.OnListFragmentInteractionListener listener) {
        mContext = context;
        mManager = BulletinManager.getInstance(context);
        mData = mManager.getBulletinList();
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM: {
                View view = LayoutInflater.from(mContext)
                        .inflate(R.layout.bulletin_list_item, parent, false);
                return new ViewHolder(view);
            }
            case FIRST_ITEM: {
                View view = LayoutInflater.from(mContext)
                        .inflate(R.layout.bulletin_first_list_item, parent, false);
                return new ViewHolderFirst(view);
            }
            default:
                return null;
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        switch (getItemViewType(position)) {
            // Parsing data to views if available
            case ITEM: {
                BulletinRecyclerViewAdapter.ViewHolder holder = (ViewHolder) mHolder;
                try {
                    holder.mMessage.setText(mManager.getBulletin(position).getMessage());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mMessage.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mName.setText(mManager.getBulletin(position).getFirstName() + " " + mManager.getBulletin(position).getLastName());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mName.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mDate.setText(Util.getReadableDateText(mManager.getBulletin(position).getDate()));
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mDate.setText(Util.getReadableDateText(new Date()));
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onListFragmentInteraction(mManager.getBulletin(position));
                        }
                    }
                });
                break;
            }
            case FIRST_ITEM:{
                BulletinRecyclerViewAdapter.ViewHolderFirst holder = (ViewHolderFirst) mHolder;
                holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(i);
                    }
                });
                holder.ibAttachment.setOnTouchListener((HomeActivity)mContext);
                holder.ibLocation.setOnTouchListener((HomeActivity)mContext);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? FIRST_ITEM : ITEM;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessage;
        public final TextView mName;
        public final TextView mDate;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMessage = (TextView) view.findViewById(R.id.tvMessage);
            mName = (TextView) view.findViewById(R.id.tvName);
            mDate = (TextView) view.findViewById(R.id.tvDate);
        }
    }

    public class ViewHolderFirst extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView ivProfilePic;
        public final TextView btnSendToSea;
        public final ImageButton ibLocation;
        public final ImageButton ibAttachment;

        public ViewHolderFirst(View view) {
            super(view);
            mView = view;
            ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
            btnSendToSea = (TextView) view.findViewById(R.id.tvSendToSea);
            ibLocation = (ImageButton) view.findViewById(R.id.ibLocation);
            ibAttachment = (ImageButton) view.findViewById(R.id.ibAttachment);
        }
    }
}

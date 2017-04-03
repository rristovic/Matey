package com.mateyinc.marko.matey.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.UserProfile;


public class ProfileFollowersAdapter extends TemporaryDataRecycleAdapter<UserProfile> {

    public ProfileFollowersAdapter(MotherActivity context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_item, parent, false);
        return new SearchAdapter.ViewHolder(view, new SearchAdapter.ViewHolder.OnItemClickedListener() {
            @Override
            public void onClick(int position) {
                Intent i = new Intent(mContext, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, getItem(position).getUserId());
                mContext.startActivity(i);
            }

            @Override
            public void onToggleButton(int position, boolean isChecked) {
                UserProfile profile = getItem(position);
                if (isChecked && !profile.isFollowed()) {
                    mManager.followNewUser(profile, mContext);
                } else if (!isChecked && profile.isFollowed()) {
                    mManager.unfollowUser(profile, mContext);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        SearchAdapter.ViewHolder holder = (SearchAdapter.ViewHolder) h;
        UserProfile profile = getItem(position);
//        OperationManager.getInstance(mContext).mImageLoader.get(getItem(position).getProfilePictureLink(),
//                new ImageLoader.ImageListener() {
//                    @Override
//                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
//                        holder.mImage.setImageBitmap(response.getBitmap());
//                    }
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, error.getLocalizedMessage(), error);
//                    }
//                }, holder.ivPicture.getWidth(), holder.ivPicture.getHeight());
//        );

        holder.tvSectionTitle.setVisibility(View.GONE);
        holder.tvTitle.setText(profile.getFullName());
        holder.tvInfo.setText("Peder");
        holder.btnSail.setChecked(profile.isFollowed());
    }


//    static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
//        View mView;
//        int mPosition;
//        TextView tvName;
//        ToggleButton mButton;
//        ImageView mImage;
//        ViewHolderListener mListener;
//
//        interface ViewHolderListener {
//            void onCheckedChanged(CompoundButton compoundButton, boolean bm, int position);
//
//            void onProfileClicked(int position);
//        }
//
//        ViewHolder(View itemView, ViewHolderListener listener) {
//            super(itemView);
//
//            mListener = listener;
//            mView = itemView;
//            tvName = (TextView) itemView.findViewById(R.id.tvName);
//            mPosition = this.getAdapterPosition();
//            mButton = (ToggleButton) itemView.findViewById(R.id.tBtnSailWith);
//            mImage = (ImageView) itemView.findViewById(R.id.ivProfilePic);
//
//            mButton.setOnCheckedChangeListener(this);
//            mImage.setOnClickListener(this);
//            tvName.setOnClickListener(this);
//        }
//
//        @Override
//        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//            mListener.onCheckedChanged(compoundButton, b, getAdapterPosition());
//        }
//
//        @Override
//        public void onClick(View view) {
//            mListener.onProfileClicked(getAdapterPosition());
//        }
//
//    }
}
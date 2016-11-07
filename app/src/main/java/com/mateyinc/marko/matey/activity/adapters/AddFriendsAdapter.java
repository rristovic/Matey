package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.rounded_image_view.RoundedImageView;
import com.mateyinc.marko.matey.internet.NetworkManager;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.LinkedList;

public class AddFriendsAdapter extends RecycleCursorAdapter {

    private final NetworkManager mNetworkManager;
    private Context mContext;
    private RecyclerView mRecycleView;
    private LinkedList mFriendsAdded;
    private LinkedList<UserProfile> mSuggestedFriends;

    public AddFriendsAdapter(Context context) {
        mContext = context;
        mFriendsAdded = new LinkedList();
        mNetworkManager = NetworkManager.getInstance(mContext);
    }

    public void setData(LinkedList<UserProfile> list){
        mSuggestedFriends = list;
        notifyDataSetChanged();
    }

    public LinkedList<UserProfile> getAddedFriends(){
        return mFriendsAdded;
    }

    public LinkedList<UserProfile> getAllFriends(){
        return new LinkedList<UserProfile>(mSuggestedFriends);
    }

    @Override
    public int getItemCount() {
        return mSuggestedFriends.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mRecycleView == null)
            mRecycleView = (RecyclerView) parent;

        View view = LayoutInflater.from(mContext) //Inflate the view
                .inflate(R.layout.add_friend_list_item, parent, false);

        return new ViewHolder(view, getViewHolderListener());
    }

    private ViewHolder.ViewHolderClickCallback getViewHolderListener() {
        return new ViewHolder.ViewHolderClickCallback(){

            @Override
            public void onAddFriendClicked(Button caller, View rootView) {
                int pos = mRecycleView.getChildAdapterPosition(rootView);
                UserProfile profile = mSuggestedFriends.get(pos);
                mFriendsAdded.add(profile);
            }

            @Override
            public void onNameClicked(TextView caller, View rootView) {
                int pos = mRecycleView.getChildAdapterPosition(rootView);
                UserProfile profile = mSuggestedFriends.get(pos);
                Intent i = new Intent(mContext, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, profile.getUserId());
                mContext.startActivity(i);
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        UserProfile profile = mSuggestedFriends.get(position);

        viewHolder.tvName.setText(profile.getFirstName().concat(" ").concat(profile.getLastName()));

        if(profile.getProfilePictureLink() != null && !profile.getProfilePictureLink().isEmpty())
        mNetworkManager.downloadImage(viewHolder.ivProfilePic, profile.getProfilePictureLink());
    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ViewHolderClickCallback mListener;
        private final View mView;
        private final TextView tvName;
        private final Button btnAddFriend;
        private final RoundedImageView ivProfilePic;

        public ViewHolder(View view, ViewHolderClickCallback listener) {
            super(view);

            mView = view;
            mListener = listener;
            tvName = (TextView) view.findViewById(R.id.tvName);
            btnAddFriend = (Button) view.findViewById(R.id.btnAddFriend);
            ivProfilePic = (RoundedImageView)view.findViewById(R.id.ivProfilePic);

            tvName.setOnClickListener(this);
            btnAddFriend.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof TextView)
                mListener.onNameClicked(tvName, mView);
            else if (v instanceof Button)
                mListener.onAddFriendClicked(btnAddFriend, mView);
        }

        interface ViewHolderClickCallback{
            void onAddFriendClicked(Button caller, View rootView);
            void onNameClicked(TextView caller, View rootView);
        }
    }
}

package com.mateyinc.marko.matey.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.home.EndlessScrollListener;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class FollowersActivity extends MotherActivity {
    private static final String TAG = FollowersActivity.class.getSimpleName();

    /**
     * Intent action - download and display followers list
     */
    public static final String ACTION_FOLLOWERS = "followers";
    /**
     * Intent action - download and display folliwing profiles list
     */
    public static final String ACTION_FOLLOWING = "following";
    public static final String EXTRA_PROFILE_ID = "user_id";

    private EndlessScrollListener mScrollListener;
    private ProgressBar mProgress;
    private RecyclerView rvList;
    private ListAdapter mAdapter;
    private TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        init();
        getData();
    }

    private void init() {
        setSupportActionBar();

        tvHeading = (TextView) findViewById(R.id.tvHeading);
        rvList = (RecyclerView) findViewById(R.id.rvList);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mAdapter = new ListAdapter(this);
        rvList.setAdapter(mAdapter);

        toolbar.findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void getData() {
//        if (mScrollListener != null)
//            mScrollListener.mLoading = true;
//        int offset = 0;
//        int count = 20;

//        OperationManager manager = OperationManager.getInstance(FollowersActivity.this);
//        manager.addSuccessListener(new Response.Listener<String>() {
//            @Override
//            public void onResponse(final String response) {
//                if (mScrollListener != null)
////                    mScrollListener.mLoading = false;
//                OperationManager.getInstance(FollowersActivity.this).submitRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        final LinkedList<Object[]> list = parseData(response);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mProgress != null)
//                                    mProgress.setVisibility(View.GONE);
//                                setData(list);
//                            }
//                        });
//                    }
//                });
//            }
//        });
//        manager.addErrorListener(new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (mScrollListener != null)
//                    mScrollListener.mLoading = false;
//            }
//        });
//
//        manager.downloadFollowers(offset, count,
//                getIntent().getLongExtra(EXTRA_PROFILE_ID, -1), FollowersActivity.this);

//        Intent i = getIntent();
//        if (i.getAction().equals(ACTION_FOLLOWERS)) {
//            downloadFollowers.startDownloadAction(
//                    UserProfileOp.getFollowersListAction(i.getLongExtra(EXTRA_PROFILE_ID, -1), offset, limit)
//            );
//
//            // Setting header
//            char[] text = getString(R.string.followers_label).toCharArray();
//            text[0] = Character.toUpperCase(text[0]);
//            String label = new String(text);
//            tvHeading.setText(label);
//        }
//        else {
//            downloadFollowers.startDownloadAction(
//                    UserProfileOp.getFollowingListAction(i.getLongExtra(EXTRA_PROFILE_ID, -1), offset, limit));
//
//            // Setting header
//            char[] text = getString(R.string.following_label).toCharArray();
//            text[0] = Character.toUpperCase(text[0]);
//            String label = new String(text);
//            tvHeading.setText(label);
//        }
//
    }

    /**
     * Helper method for settings the data in adapter
     *
     * @param list list of data
     */
    private void setData(LinkedList<Object[]> list) {
        mAdapter.setData(list);
        if (mAdapter.getItemCount() == 20) {
            if (mScrollListener == null)
                mScrollListener = new EndlessScrollListener((LinearLayoutManager) rvList.getLayoutManager()) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount) {
                    }
                };
        }
    }

    /**
     * Helper method for parsing followers list from the server
     *
     * @param response raw data response from server
     * @return {@link LinkedList} with parsed data
     */
    private LinkedList<Object[]> parseData(String response) {
        LinkedList<Object[]> list = new LinkedList<>();

        try {
            JSONObject object = new JSONObject(response);
            JSONArray followers = object.getJSONArray(KEY_ARRAY_NAME);

            int count = followers.length();
            for (int i = 0; i < count; i++) {
                JSONObject follower = followers.getJSONObject(i);

                list.add(new Object[]{
                        follower.getLong(KEY_USER_ID),
                        follower.getString(KEY_FIRST_NAME),
                        follower.getString(KEY_LAST_NAME),
                        follower.getString(KEY_FULL_NAME),
                        follower.getString(KEY_PICTURE_URL),
                        follower.getString(KEY_COVER_URL),
                        follower.getBoolean(KEY_FOLLOWING)
                });
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }

        return list;
    }

    // Keys for json retrieved from the server
    private static final String KEY_ARRAY_NAME = "data";
    private static final String KEY_USER_ID = UserProfile.KEY_ID;
    private static final String KEY_FIRST_NAME = UserProfile.KEY_FIRST_NAME;
    private static final String KEY_LAST_NAME = UserProfile.KEY_LAST_NAME;
    private static final String KEY_FULL_NAME = UserProfile.KEY_FULL_NAME;
    private static final String KEY_PICTURE_URL = UserProfile.KEY_PROFILE_PIC;
    private static final String KEY_COVER_URL = UserProfile.KEY_COVER_PIC;
    private static final String KEY_FOLLOWING = UserProfile.KEY_FOLLOWING;
    private static final String KEY_SIZE = "size";
    private static final String KEY_OFFSET = "offset";
    private static final String KEY_LIMIT = "limit";
    // Indices for data position that will be saved
    private static final int COL_ID = 0;
    private static final int COL_NAME = COL_ID + 1;
    private static final int COL_LASTNAME = COL_NAME + 1;
    private static final int COL_FULLNAME = COL_LASTNAME + 1;
    private static final int COL_PIC = COL_FULLNAME + 1;
    private static final int COL_COVER = COL_PIC + 1;
    private static final int COL_FOLLOWING = COL_COVER + 1;


    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LinkedList<Object[]> data;
        private MotherActivity mContext;

        ListAdapter(MotherActivity context) {
            setHasStableIds(true);
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_followers_list_item, parent, false);
            return new ViewHolder(view, new ViewHolder.ViewHolderListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked, int position) {
//                    // Because button will be set to checked state programmatically to update UI,
//                    // first check if the user is already being followed so that it doesn't upload
                    if (isChecked && !(boolean)data.get(position)[COL_FOLLOWING]) {
                        // Follow current user only when checked and user is not being followed
                        OperationManager.getInstance(mContext).followNewUser((long)data.get(position)[COL_ID], FollowersActivity.this);
                        // Update data
                        data.get(position)[COL_FOLLOWING] = true;
                    } else if(!isChecked && (boolean)data.get(position)[COL_FOLLOWING]) {
                        // unfollow cur user only when unchecked and user is followed
                        OperationManager.getInstance(mContext).unfollowUser((long)data.get(position)[COL_ID], FollowersActivity.this);
                        // Update data
                        data.get(position)[COL_FOLLOWING] = false;
                    }
                }

                @Override
                public void onProfileClicked(int position) {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, (long)data.get(position)[COL_ID]);
                    mContext.startActivity(i);
                }
            });
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Object[] currentData = data.get(position);

            OperationManager.getInstance(FollowersActivity.this).mImageLoader.get((String)currentData[COL_PIC],
                    new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            holder.mImage.setImageBitmap(response.getBitmap());
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.getLocalizedMessage(), error);
                        }
                    }, holder.mImage.getWidth(), holder.mImage.getHeight()
            );

            holder.mButton.setChecked((boolean)currentData[COL_FOLLOWING]);
            holder.tvName.setText((String) currentData[COL_FULLNAME]);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setData(LinkedList<Object[]> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{
        View mView;
        int mPosition;
        TextView tvName;
        ToggleButton mButton;
        ImageView mImage;
        ViewHolderListener mListener;

        interface ViewHolderListener{
            void onCheckedChanged(CompoundButton compoundButton, boolean bm, int position);
            void onProfileClicked(int position);
        }

        ViewHolder(View itemView, ViewHolderListener listener) {
            super(itemView);

            mListener = listener;
            mView = itemView;
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            mPosition = this.getAdapterPosition();
            mButton = (ToggleButton) itemView.findViewById(R.id.tBtnSailWith);
            mImage = (ImageView) itemView.findViewById(R.id.ivProfilePic);

            mButton.setOnCheckedChangeListener(this);
            mImage.setOnClickListener(this);
            tvName.setOnClickListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mListener.onCheckedChanged(compoundButton, b, getAdapterPosition());
        }

        @Override
        public void onClick(View view) {
            mListener.onProfileClicked(getAdapterPosition());
        }

    }
}

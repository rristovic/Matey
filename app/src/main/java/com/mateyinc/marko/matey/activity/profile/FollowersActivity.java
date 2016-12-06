package com.mateyinc.marko.matey.activity.profile;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Response;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.OperationFactory;
import com.mateyinc.marko.matey.data.operations.Operations;
import com.mateyinc.marko.matey.data.operations.UserProfileOp;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class FollowersActivity extends MotherActivity {
    private static final String TAG = FollowersActivity.class.getSimpleName();

    private ProgressBar mProgress;
    private RecyclerView rvList;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        init();
        getData();
    }

    private void init() {
        rvList = (RecyclerView) findViewById(R.id.rvList);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mAdapter = new ListAdapter();

    }

    public void getData() {
        int position = 0;
        int offset = 0;
        int limit = 0;

        OperationFactory factory = OperationFactory.getInstance(this);
        Operations downloadFollowers = factory.getOperation(OperationFactory.OperationType.USER_PROFILE_OP);
        downloadFollowers.addSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                DataManager.getInstance(FollowersActivity.this).submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        final LinkedList<Object[]> list = parseData(response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.setVisibility(View.GONE);
                                mAdapter.setData(list);
                            }
                        });
                    }
                });
            }
        });
        downloadFollowers.startDownloadAction(
                UserProfileOp.getFollowersListAction(position, offset, limit)
        );
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
    private static final String KEY_USER_ID = UserProfileOp.KEY_ID;
    private static final String KEY_FIRST_NAME = UserProfileOp.KEY_FIRST_NAME;
    private static final String KEY_LAST_NAME = UserProfileOp.KEY_LAST_NAME;
    private static final String KEY_FULL_NAME = UserProfileOp.KEY_FULL_NAME;
    private static final String KEY_PICTURE_URL = UserProfileOp.KEY_PROFILE_PIC;
    private static final String KEY_COVER_URL = UserProfileOp.KEY_COVER_PIC;
    private static final String KEY_FOLLOWING = UserProfileOp.KEY_FOLLOWING;
    // Indices for data position that will be saved
    private static final int COL_ID = 1;
    private static final int COL_NAME = COL_ID + 1;
    private static final int COL_LASTNAME = COL_NAME + 1;
    private static final int COL_FULLNAME = COL_LASTNAME + 1;
    private static final int COL_PIC = COL_FULLNAME + 1;
    private static final int COL_COVER = COL_PIC + 1;
    private static final int COL_FOLLOWING = COL_COVER + 1;


    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LinkedList<Object[]> data;

        ListAdapter() {
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.followers_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Object[] currentData = data.get(position);

            holder.tvName.setText((String)currentData[COL_FULLNAME]);
        }

        @Override
        public int getItemCount() {
            return data.size();
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        int mPosition;
        TextView tvName;
        ToggleButton mButton;
        ImageView mImage;

        ViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvName);
            mPosition = this.getAdapterPosition();
            mButton = (ToggleButton) itemView.findViewById(R.id.tBtnSailWith);
            mImage = (ImageView) itemView.findViewById(R.id.ivProfilePic);
        }
    }
}

package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data_and_managers.DataManager;

import java.util.Date;

/**
 * Created by Sarma on 8/27/2016.
 */
public class NotificationsFragment extends Fragment {

    private NotificationAdapter mAdapter;
    private Context mContext;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * NotificationsFragment callback for when an item has been selected.
         */
        public void onItemSelected();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new NotificationAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_msg, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationsFragment.NotificationAdapter.ViewHolder>{

        private final Context mContext;
        private final DataManager mManager;

        public NotificationAdapter(Context context){
            mContext = context;
            mManager = DataManager.getInstance(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.notif_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Parsing data to views if available
//            try {
//                holder.mProfilePic.setImageBitmap(null);
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                holder.mProfilePic.setImageBitmap(null);
//            }
            try {
                holder.mNotifText.setText(mManager.mNotificationList.get(position).getMessage());
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.mNotifText.setText(mContext.getString(R.string.bulletin_post_error));
            }
            try {
                holder.mNotifTime.setText(getReadableDateText(mManager.mNotificationList.get(position).getDate()));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.mNotifTime.setText(new Date().toString());
            }

//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (null != mListener) {
//                        // Notify the active callbacks interface (the activity, if the
//                        // fragment is attached to one) that an item has been selected.
//                        mListener.onListFragmentInteraction(mManager.getBulletin(position));
//                    }
//                }
//            });
        }

        private String getReadableDateText(Date date) {
            // TODO - set the timezone to server timezone
            //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

            // TODO - Get correct day with timezone
            Date now = new Date();
            int hour = Math.round(now.getTime() - date.getTime())/(1000*60*60);
            if(hour > 24)
                return hour / 24 + " days ago";
            else if(hour >= 1)
                return hour + " hours ago";
            else{
                return (now.getTime()-date.getTime())/(1000*60) + " mins ago";
            }
        }

        @Override
        public int getItemCount() {
            return mManager.mNotificationList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mProfilePic;
            public final TextView mNotifText;
            public final TextView mNotifTime;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mProfilePic = (ImageView) view.findViewById(R.id.ivNotifProfilePic);
                mNotifText = (TextView) view.findViewById(R.id.tvNotifText);
                mNotifTime = (TextView) view.findViewById(R.id.tvNotifTime);
            }
        }
    }
}

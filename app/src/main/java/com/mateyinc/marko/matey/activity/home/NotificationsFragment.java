package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.EndlessScrollListener;
import com.mateyinc.marko.matey.adapters.NotificationsAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class NotificationsFragment extends Fragment {

    private static final String TAG = NotificationsFragment.class.getSimpleName();

    private NotificationsAdapter mAdapter;
    private MotherActivity mContext;
    private OperationManager mManager;
    private OperationManager mOperationManager;
    private DataAccess mDataAccess;
    private SwipeRefreshLayout mMainRefreshLayout;
    private EndlessScrollListener mScrollListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (MotherActivity) context;
        mOperationManager = OperationManager.getInstance(context);
        mOperationManager.downloadNotificationList(true, mContext);
        mDataAccess = DataAccess.getInstance(mContext);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_group_list, container, false);
        RecyclerView list = (RecyclerView) mMainRefreshLayout.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

        // Set the adapter
        mAdapter = new NotificationsAdapter(mContext);
        mAdapter.setData(mDataAccess.mNotificationList);
        list.setAdapter(mAdapter);

        // Set listener
        mMainRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOperationManager.downloadNotificationList(true, mContext);
            }
        });

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mOperationManager.downloadNotificationList(false, mContext);
            }
        };
        list.addOnScrollListener(mScrollListener);

        return mMainRefreshLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mAdapter.setData(mDataAccess.mNotificationList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent event) {
        mScrollListener.onDownloadFinished();
        mMainRefreshLayout.setRefreshing(false);
        mAdapter.setData(mDataAccess.mNotificationList);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

//    private class NotificationsAdapter extends CursorAdapter {
//
//        public NotificationsAdapter(Context context, Cursor c, int flags) {
//            super(context, c, flags);
//        }
//
//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            View view = LayoutInflater.from(context).inflate(R.layout.notif_list_item, parent, false);
//            ViewHolder viewHolder = new ViewHolder(view);
//            view.setTag(viewHolder);
//
//            return view;
//        }
//
//        @Override
//        public void bindView(View view, Context context, Cursor cursor) {
//            ViewHolder viewHolder = (ViewHolder) view.getTag();
//
//            try {
//                Spanned text = Html.fromHtml("<b>" + cursor.getString(COL_NOTIF_SENDER_NAME) + "</b>" + " " +
//                        cursor.getString(COL_NOTIF_BODY) + " " +
//                        "<b>" + cursor.getString(COL_NOTIF_LINK_ID) + "</b>");
//                viewHolder.tvNotifText.setText(text);
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                viewHolder.tvNotifText.setText(mContext.getString(R.string.error_message) + " ");
//            }
//
//            Date date = new Date();
//            try {
//                try {
//                    date = new Date(cursor.getString(COL_NOTIF_TIME));
//                } catch (Exception e) {
//                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                }
//                viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
//            }
//        }
//
//
//
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            public final View mView;
//            public final ImageView ivProfilePic;
//            public final TextView tvNotifText;
//            public final TextView tvNotifTime;
//
//            public ViewHolder(View view) {
//                super(view);
//                mView = view;
//                ivProfilePic = (ImageView) view.findViewById(R.id.ivNotifProfilePic);
//                tvNotifText = (TextView) view.findViewById(R.id.tvNotifText);
//                tvNotifTime = (TextView) view.findViewById(R.id.tvNotifTime);
//            }
//        }
//    }


//    private class NotificationAdapter extends RecyclerView.Adapter<NotificationsFragment.NotificationAdapter.ViewHolder>{
//
//        private final Context mContext;
//        private final OperationManager mManager;
//
//        public NotificationAdapter(Context context){
//            mContext = context;
//            mManager = OperationManager.getInstance(context);
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(mContext)
//                    .inflate(R.layout.notif_list_item, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            // Parsing data to views if available
////            try {
////                holder.mProfilePic.setImageBitmap(null);
////            } catch (Exception e) {
////                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
////                holder.mProfilePic.setImageBitmap(null);
////            }
//            try {
//                holder.mText.setText(mManager.mNotificationList.get(position).getMessage());
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                holder.mText.setText(mContext.getString(R.string.bulletin_post_error));
//            }
//            try {
//                holder.mTime.setText(getReadableDateText(mManager.mNotificationList.get(position).getDate()));
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                holder.mTime.setText(new Date().toString());
//            }
//
////            holder.mView.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    if (null != mListener) {
////                        // Notify the active callbacks interface (the activity, if the
////                        // fragment is attached to one) that an item has been selected.
////                        mListener.onListFragmentInteraction(mManager.getBulletin(position));
////                    }
////                }
////            });
//        }
//
//        private String getReadableDateText(Date date) {
//            // TODO - set the timezone to server timezone
//            //TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
//
//            // TODO - Get correct day with timezone
//            Date now = new Date();
//            int hour = Math.round(now.getTime() - date.getTime())/(1000*60*60);
//            if(hour > 24)
//                return hour / 24 + " days ago";
//            else if(hour >= 1)
//                return hour + " hours ago";
//            else{
//                return (now.getTime()-date.getTime())/(1000*60) + " mins ago";
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return mManager.mNotificationList.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            public final View mView;
//            public final ImageView mProfilePic;
//            public final TextView mText;
//            public final TextView mTime;
//
//            public ViewHolder(View view) {
//                super(view);
//                mView = view;
//                mProfilePic = (ImageView) view.findViewById(R.id.ivNotifProfilePic);
//                mText = (TextView) view.findViewById(R.id.tvNotifText);
//                mTime = (TextView) view.findViewById(R.id.tvNotifTime);
//            }
//        }
//    }

}

package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataContract.NotificationEntry;
import com.mateyinc.marko.matey.data.OperationManager;

import java.util.Date;


public class NotificationsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = NotificationsFragment.class.getSimpleName();

    private NotificationsAdapter mAdapter;
    private Context mContext;
    private OperationManager mManager;

    private static final int NOTIF_LOADER = 1;

    // For the data view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] NOTIF_COLUMNS = {
            NotificationEntry.TABLE_NAME + "." + NotificationEntry._ID,
            NotificationEntry.COLUMN_SENDER_ID,
            NotificationEntry.COLUMN_SENDER_NAME,
            NotificationEntry.COLUMN_NOTIF_TEXT,
            NotificationEntry.COLUMN_NOTIF_TIME,
            NotificationEntry.COLUMN_NOTIF_LINK_ID

    };

    // These indices are tied to NOTIF_COUMNS.  If NOTIF_COLUMNS changes, these
    // must change.
    public static final int COL_NOTIF_ID = 0;
    public static final int COL_NOTIF_SENDER_ID = 1;
    public static final int COL_NOTIF_SENDER_NAME = 2;
    public static final int COL_NOTIF_BODY = 3;
    public static final int COL_NOTIF_TIME = 4;
    public static final int COL_NOTIF_LINK_ID = 5;

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
        mAdapter = new NotificationsAdapter(mContext, null, 0);
        mManager = OperationManager.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_msg, container, false);

        // Set the adapter
        ListView listView = (ListView) view;
        listView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NOTIF_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by time.
//        String sortOrder = DataContract.MessageEntry.COLUMN_TIME + " ASC";

        return new CursorLoader(getActivity(),
                NotificationEntry.CONTENT_URI,
                NOTIF_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        Log.d(TAG, "entered onLoadFinished(..)");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        Log.d(TAG, "entered onLoaderReset(..)");
    }

    private class NotificationsAdapter extends CursorAdapter {

        public NotificationsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.notif_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            try {
                Spanned text = Html.fromHtml("<b>" + cursor.getString(COL_NOTIF_SENDER_NAME) + "</b>" + " " +
                        cursor.getString(COL_NOTIF_BODY) + " " +
                        "<b>" + cursor.getString(COL_NOTIF_LINK_ID) + "</b>");
                viewHolder.tvNotifText.setText(text);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                viewHolder.tvNotifText.setText(mContext.getString(R.string.error_message) + " ");
            }

            Date date = new Date();
            try {
                try {
                    date = new Date(cursor.getString(COL_NOTIF_TIME));
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                }
                viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
            }
        }




        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView ivProfilePic;
            public final TextView tvNotifText;
            public final TextView tvNotifTime;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                ivProfilePic = (ImageView) view.findViewById(R.id.ivNotifProfilePic);
                tvNotifText = (TextView) view.findViewById(R.id.tvNotifText);
                tvNotifTime = (TextView) view.findViewById(R.id.tvNotifTime);
            }
        }
    }


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

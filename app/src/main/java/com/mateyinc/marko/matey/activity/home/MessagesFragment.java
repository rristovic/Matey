package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.DataContract.MessageEntry;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.OperationManager;

/**
 * Created by Sarma on 8/27/2016.
 */
public class MessagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MessageAdapter mAdapter;
    private Context mContext;
    private OperationManager mManager;

    private static final int MESSAGE_LOADER = 0;

    // For the data view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] MESSAGE_COLUMNS = {
            MessageEntry.TABLE_NAME + "." + DataContract.MessageEntry._ID,
            MessageEntry.COLUMN_SENDER_NAME,
            MessageEntry.COLUMN_MSG_BODY,
            MessageEntry.COLUMN_MSG_TIME,
            MessageEntry.COLUMN_IS_READ
    };

    // These indices are tied to MSG_COLUMNS.  If MSG_COLUMNS changes, these
    // must change.
    public static final int COL_MSG_ID = 0;
    public static final int COL_MSG_SENDER_NAME = 1;
    public static final int COL_MSG_BODY = 2;
    public static final int COL_MSG_TIME = 3;
    public static final int COL_MSG_IS_READ = 4;

    public static final String[] PROFILE_COLUMNS = {
            ProfileEntry.TABLE_NAME + "." + ProfileEntry._ID,
            ProfileEntry.COLUMN_NAME,
            ProfileEntry.COLUMN_LAST_MSG_ID
    };

    public static final int COL_PROF_ID = 0;
    public static final int COL_PROF_NAME = 1;
    public static final int COL_PROF_LAST_MSG_ID = 2;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessagesFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mAdapter = new MessageAdapter(mContext, null, 0);
        mManager = OperationManager.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_msg, container, false);
//
//        // Set the adapter
//        ListView listView = (ListView) view;
//        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
//        params.leftMargin = getPx(0);
//        params.rightMargin = getPx(0);
//        listView.setLayoutParams(params);
//
//        listView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        getLoaderManager().initLoader(MESSAGE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by time.
//        String sortOrder = DataContract.MessageEntry.COLUMN_TIME + " ASC";

        return new CursorLoader(getActivity(),
                ProfileEntry.CONTENT_URI,
                PROFILE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private int getPx(int dp) {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
    }

    private class MessageAdapter extends CursorAdapter {

        private final Context mContext;

        public MessageAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.msg_list_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

//            Glide.with(mContext)
//                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
//                    .error(fallbackIconId)
//                    .crossFade()
//                    .into(viewHolder.iconView);
            Cursor msgCursor = mContext.getContentResolver().query(MessageEntry.CONTENT_URI,
                    MESSAGE_COLUMNS, MessageEntry._ID + " = ?",
                    new String[]{Integer.toString(cursor.getInt(COL_PROF_LAST_MSG_ID))}, null);


            if (msgCursor != null)
                msgCursor.moveToFirst();

            // Last message text view
            try {
                holder.tvLastMsg.setText(msgCursor.getString(COL_MSG_BODY));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.tvLastMsg.setText(mContext.getString(R.string.error_message));
            }

            // Sender name text view
            try {
                holder.tvSenderName.setText(msgCursor.getString(COL_MSG_SENDER_NAME));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.tvSenderName.setText(mContext.getString(R.string.error_message));
            }

            // Last message time
            try {
                holder.tvTime.setText(Util.getReadableDateText(msgCursor.getString(MessagesFragment.COL_MSG_TIME)));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.tvTime.setText(mContext.getString(R.string.error_message));
            }

            // Unread messages count
            try {
                int unreadCount = msgCursor.getInt(COL_MSG_IS_READ);
                if (unreadCount == 0) {// last message is not read, count unread messages
                    msgCursor = mContext.getContentResolver().query(MessageEntry.CONTENT_URI,
                            MESSAGE_COLUMNS,
                            MessageEntry.COLUMN_SENDER_ID + " = ? AND " + MessageEntry.COLUMN_IS_READ + " = 0",
                            new String[]{Integer.toString(cursor.getInt(COL_PROF_ID))}, null);
                    holder.tvUnreadCount.setText(Integer.toString(msgCursor.getCount()));
                } else {
                    holder.tvUnreadCount.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.tvUnreadCount.setVisibility(View.GONE);
            } finally {
                if (msgCursor != null)
                    msgCursor.close();
            }
            holder.ivActiveState.setColorFilter(getActiveStateColor());

        }

        private int getActiveStateColor() {
            return mContext.getResources().getColor(R.color.active_state);
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView ivProfilePic;
            public final ImageView ivActiveState;
            public final TextView tvTime;
            public final TextView tvSenderName;
            public final TextView tvLastMsg;
            public final TextView tvUnreadCount;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                ivProfilePic = (ImageView) view.findViewById(R.id.ivMsgProfilePic);
                ivActiveState = (ImageView) view.findViewById(R.id.activeState);
                tvTime = (TextView) view.findViewById(R.id.tvMsgTime);
                tvSenderName = (TextView) view.findViewById(R.id.tvMsgSenderName);
                tvLastMsg = (TextView) view.findViewById(R.id.tvLastMsg);
                tvUnreadCount = (TextView) view.findViewById(R.id.tvUnreadCount);
            }
        }
    }
}


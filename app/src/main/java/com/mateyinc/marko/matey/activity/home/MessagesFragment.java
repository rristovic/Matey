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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data_and_managers.DataContract;
import com.mateyinc.marko.matey.data_and_managers.DataManager;

/**
 * Created by Sarma on 8/27/2016.
 */
public class MessagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private MessageAdapter mAdapter;
    private Context mContext;
    private DataManager mManager;

    private static final int MESSAGE_LOADER = 0;

    // For the data view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MESSAGE_COLUMNS = {
            DataContract.MessageEntry.TABLE_NAME + "." + DataContract.MessageEntry._ID,
            DataContract.MessageEntry.COLUMN_MSG_BODY
    };

    // These indices are tied to MSG_COLUMNS.  If MSG_COLUMNS changes, these
    // must change.
    static final int COL_MSG_ID = 0;
    static final int COL_MSG_BODY = 1;

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
        mAdapter = new MessageAdapter(mContext, null,0);
        mManager = DataManager.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_msg, container, false);

        // Set the adapter
        Context context = view.getContext();
        ListView listView = (ListView) view;
        listView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MESSAGE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),
                DataContract.MessageEntry.CONTENT_URI,
                MESSAGE_COLUMNS,
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
            int msgId = cursor.getInt(MessagesFragment.COL_MSG_ID);

//            Glide.with(mContext)
//                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
//                    .error(fallbackIconId)
//                    .crossFade()
//                    .into(viewHolder.iconView);

            try {
                holder.mNotifText.setText(cursor.getString(MessagesFragment.COL_MSG_BODY));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                holder.mNotifText.setText(mContext.getString(R.string.bulletin_post_error));
            }
//            try {
//                holder.mNotifTime.setText(getReadableDateText(mManager.mNotificationList.get(position).getDate()));
//            } catch (Exception e) {
//                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
//                holder.mNotifTime.setText(new Date().toString());
//            }
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


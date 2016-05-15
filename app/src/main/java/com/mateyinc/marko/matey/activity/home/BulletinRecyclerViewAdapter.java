package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Bulletin} and makes a call to the
 * specified {@link BulletinFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BulletinRecyclerViewAdapter extends RecyclerView.Adapter<BulletinRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Bulletin> mData;
    private final BulletinFragment.OnListFragmentInteractionListener mListener;
    private final Context mContext;
    private final BulletinManager mManager;

    public BulletinRecyclerViewAdapter(Context context, BulletinFragment.OnListFragmentInteractionListener listener) {
        mContext = context;
        mManager = BulletinManager.getInstance(context);
        mData = mManager.getBulletinList();
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_bulletin_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mMessage.setText(mManager.getBulletin(position).getMessage());
        holder.mName.setText(mManager.getBulletin(position).getFirstName() + " " + mManager.getBulletin(position).getLastName());
        holder.mDate.setText(mManager.getBulletin(position).getDate().toString());

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
}

package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.mateyinc.marko.matey.data_and_managers.DataManager;

/**
 * Created by Sarma on 10/9/2016.
 */
public abstract class RecycleCursorAdapter extends RecyclerView.Adapter {
    protected Cursor mCursor;
    protected  Context mContext;
    protected  DataManager mManager;

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

}

package com.mateyinc.marko.matey.activity.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

public abstract class RecycleCursorAdapter extends RecyclerView.Adapter {
    protected Cursor mCursor;
    protected MotherActivity mContext;
    protected OperationManager mManager;

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

package com.mateyinc.marko.matey.adapters;

import android.support.v7.widget.RecyclerView;

import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;

import java.util.ArrayList;
import java.util.List;

public abstract class RecycleNoSQLAdapter<T> extends RecyclerView.Adapter {
    protected MotherActivity mContext;
    protected OperationManager mManager;
    protected List<T> mData;

    public RecycleNoSQLAdapter(MotherActivity context){
        mContext = context;
        mData = new ArrayList<>();
        mManager = OperationManager.getInstance(context);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addData(List<T> list) {
        int pos = mData.size();
        mData.addAll(list);
        notifyItemRangeInserted(pos, list.size());
    }

    public void setData(List<T> list) {
        mData = list;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

//    abstract public void refreshData();
}

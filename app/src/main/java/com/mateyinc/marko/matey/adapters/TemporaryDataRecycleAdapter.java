package com.mateyinc.marko.matey.adapters;


import android.support.v7.widget.RecyclerView;

import com.mateyinc.marko.matey.data.TemporaryDataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.model.MModel;

import java.util.ArrayList;

public abstract class TemporaryDataRecycleAdapter<T extends MModel> extends RecyclerView.Adapter {
    protected MotherActivity mContext;
    protected OperationManager mManager;
    private TemporaryDataAccess<T> mDataAccess;

    public TemporaryDataRecycleAdapter(MotherActivity context){
        mContext = context;
        mDataAccess = new TemporaryDataAccess<>(new ArrayList<T>(0), true);
        mManager = OperationManager.getInstance(context);
    }

    @Override
    public int getItemCount() {
        return mDataAccess.getItemCount();
    }

    public void loadData(TemporaryDataAccess<T> dao) {
        int startPos = mDataAccess.getItemCount();
        this.mDataAccess.loadDataFrom(dao);
        notifyItemRangeInserted(startPos, dao.getItemCount());
    }

    public void addItem(T item){
        int startPos = mDataAccess.getItemCount();
        mDataAccess.addItem(item);
        notifyItemRangeInserted(startPos, 1);
    }

//    public void setData(List<T> list) {
//        mData = list;
//        notifyDataSetChanged();
//    }

    protected T getItem(int position){
        return mDataAccess.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

//    abstract public void refreshData();
}

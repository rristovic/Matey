package com.mateyinc.marko.matey.activity.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mateyinc.marko.matey.internet.OperationManager;

import java.util.ArrayList;
import java.util.List;


public class SearchHintAdapter extends BaseAdapter implements Filterable {

    private final OperationManager mManager;
    private final Context mContext;
    private List<String> mData = new ArrayList<>();

    public SearchHintAdapter(Context context) {
        mContext = context;
        mManager = OperationManager.getInstance(context);
    }

    public void setData(List<String> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ((TextView) convertView).setText(mData.get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint.length() > 0)
                    mManager.onSearchAutocomplete(constraint.toString(), mContext);
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
        return filter;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
}

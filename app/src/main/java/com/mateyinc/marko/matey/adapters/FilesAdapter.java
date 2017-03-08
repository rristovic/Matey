package com.mateyinc.marko.matey.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mateyinc.marko.matey.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileHolder> {

    private static final String TAG = FilesAdapter.class.getSimpleName();
    List<String> mData;
    Context mContext;

    public FilesAdapter(Context context) {
        mData = new ArrayList<>();
        mContext = context;
    }

    public void addData(String filePath) {
        mData.add(filePath);
        notifyDataSetChanged();
    }

    public List<String> getData() {
        return mData;
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context c = parent.getContext();
        View view = LayoutInflater.from(c).inflate(R.layout.file_list_view, parent, false);

        FileHolder holder = new FileHolder(view, new FileHolder.ItemClickListener() {
            @Override
            public void onDeleteClicked(int position) {
                mData.remove(position);
                notifyDataSetChanged();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        holder.title.setText(null);
        holder.title.setVisibility(View.INVISIBLE);

        String path = mData.get(position);
        if (isPicture(path))
            try {
                Glide.with(mContext)
                        .load(new File(mData.get(position)))
                        .centerCrop()
                        .placeholder(android.R.color.holo_blue_light)
                        .crossFade()
                        .into(holder.image);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
            holder.title.setText(path.substring(path.lastIndexOf("/") + 1));
        }
    }

    private boolean isPicture(String path) {
        path = path.toLowerCase();
        return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final View mView;
        final ImageView image;
        final TextView title;
        final ItemClickListener mListener;

        @Override
        public void onClick(View v) {
            mListener.onDeleteClicked(getAdapterPosition());
        }

        interface ItemClickListener {
            void onDeleteClicked(int position);
        }

        public FileHolder(View itemView, ItemClickListener listener) {
            super(itemView);

            mListener = listener;
            mView = itemView;
            image = (ImageView) itemView.findViewById(R.id.ivImage);
            title = (TextView) itemView.findViewById(R.id.tvTitle);

            mView.setOnClickListener(this);
        }
    }
}

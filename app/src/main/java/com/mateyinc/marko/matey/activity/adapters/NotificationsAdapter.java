package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.NotificationsFragment;

import java.util.Date;

/**
 * Created by Sarma on 9/5/2016.
 */
public class NotificationsAdapter extends CursorAdapter {

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
            Spanned text = Html.fromHtml("<b>" + cursor.getString(NotificationsFragment.COL_NOTIF_SENDER_NAME) + "</b>" + " " +
                    cursor.getString(NotificationsFragment.COL_NOTIF_BODY) + " " +
                    "<b>" + cursor.getString(NotificationsFragment.COL_NOTIF_LINK_ID) + "</b>");
            viewHolder.tvNotifText.setText(text);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
            viewHolder.tvNotifText.setText(mContext.getString(R.string.error_message) + " ");
        }

        Date date = new Date();
        try {
            try {
                date = new Date(cursor.getString(NotificationsFragment.COL_NOTIF_TIME));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
            }
            viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
            viewHolder.tvNotifTime.setText(Util.getReadableDateText(date));
        }
    }




    public static class ViewHolder extends RecyclerView.ViewHolder {
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
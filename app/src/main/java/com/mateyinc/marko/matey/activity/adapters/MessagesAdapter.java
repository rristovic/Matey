package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.MessagesFragment;
import com.mateyinc.marko.matey.data.DataContract;

public class MessagesAdapter extends CursorAdapter {

    private final Context mContext;

    public MessagesAdapter(Context context, Cursor c, int flags) {
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
        Cursor msgCursor = mContext.getContentResolver().query(DataContract.MessageEntry.CONTENT_URI,
                MessagesFragment.MESSAGE_COLUMNS, DataContract.MessageEntry._ID + " = ?",
                new String[]{Integer.toString(cursor.getInt(MessagesFragment.COL_PROF_LAST_MSG_ID))}, null);


        if (msgCursor != null)
            msgCursor.moveToFirst();

        // Last message text view
        try {
            holder.tvLastMsg.setText(msgCursor.getString(MessagesFragment.COL_MSG_BODY));
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
            holder.tvLastMsg.setText(mContext.getString(R.string.error_message));
        }

        // Sender name text view
        try {
            holder.tvSenderName.setText(msgCursor.getString(MessagesFragment.COL_MSG_SENDER_NAME));
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
            int unreadCount = msgCursor.getInt(MessagesFragment.COL_MSG_IS_READ);
            if (unreadCount == 0) {// last message is not read, count unread messages
                msgCursor = mContext.getContentResolver().query(DataContract.MessageEntry.CONTENT_URI,
                        MessagesFragment.MESSAGE_COLUMNS,
                        DataContract.MessageEntry.COLUMN_SENDER_ID + " = ? AND " + DataContract.MessageEntry.COLUMN_IS_READ + " = 0",
                        new String[]{Integer.toString(cursor.getInt(MessagesFragment.COL_PROF_ID))}, null);
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

    public int getActiveStateColor() {
        return mContext.getResources().getColor(R.color.active_state);
    }

    // TODO - change cursoradapter to recycle adapter with these methods
//    @override
//    public int getItemCount(){
//        if( null == mCursor)return 0;
//        return mCursor.getCount()
//    }
//    public void swapCursor(Cursor newCursor){
//        mCursor = newCursor;
//        notifyDataSetChanged();
//    }
//
//    public Cursor getCursor(){
//        return mCursor;
//    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
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
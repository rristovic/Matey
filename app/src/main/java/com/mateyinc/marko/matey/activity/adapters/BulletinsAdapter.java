package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.rounded_image_view.RoundedImageView;
import com.mateyinc.marko.matey.activity.view.BulletinRepliesViewActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.Date;

public class BulletinsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FIRST_ITEM = 1;
    private static final int ITEM = 2;

    private final Context mContext;
    private final BulletinManager mManager;
    private View mEmptyView;
    private Cursor mCursor;
    private RecyclerView mRecycleView;

    public BulletinsAdapter(Context context, TextView emptyView) {
        mContext = context;
        mManager = BulletinManager.getInstance(context);
        mEmptyView = emptyView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {

            if (mRecycleView == null)
                mRecycleView = (RecyclerView)parent;

            switch (viewType) {
                case ITEM: {
                    View view = LayoutInflater.from(mContext) //Inflate the view
                            .inflate(R.layout.bulletin_list_item, parent, false);

                    // Implementing ViewHolderClickListener and returning view holder
                    return new ViewHolder(view, getViewHolderListener());
                }
                case FIRST_ITEM: {
                    View view = LayoutInflater.from(mContext)
                            .inflate(R.layout.bulletin_first_list_item, parent, false);
                    return new ViewHolderFirst(view);
                }
                default:
                    return null;
            }
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }

    }

    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {
            public void onRepliesClick(View caller, View rootView, boolean onlyShowReplies) {
                int position = mRecycleView.getChildAdapterPosition(rootView);
                if (onlyShowReplies) {
                    Intent i = new Intent(mContext, BulletinRepliesViewActivity.class);
                    i.putExtra(BulletinRepliesViewActivity.EXTRA_BULLETIN_POS, position);
                    mContext.startActivity(i);
                } else {
                    Intent i = new Intent(mContext, BulletinRepliesViewActivity.class);
                    i.putExtra(BulletinRepliesViewActivity.EXTRA_BULLETIN_POS, position);
                    i.putExtra(BulletinRepliesViewActivity.EXTRA_NEW_REPLY, true);
                    mContext.startActivity(i);
                }
            }

            @Override
            public void onMsgClick(TextView mMessage, View rootView) {
                // TODO - finish method
            }

            @Override
            public void onNameClick(TextView mName, View rootView) {
                int position = mRecycleView.getChildAdapterPosition(rootView);
                Intent i = new Intent(mContext, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mManager.getBulletin(position,mCursor).getUserID());
                mContext.startActivity(i);
            }
        };
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        switch (getItemViewType(position)) {
//             Parsing data to views if available
            case ITEM: {
                BulletinsAdapter.ViewHolder holder = (ViewHolder) mHolder;
                Bulletin bulletin = mManager.getBulletin(position, mCursor);
                try {
                    holder.mMessage.setText(bulletin.getMessage());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mMessage.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mName.setText(bulletin.getFirstName() + " " + bulletin.getLastName());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mName.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mDate.setText(Util.getReadableDateText(bulletin.getDate()));
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mDate.setText(Util.getReadableDateText(new Date()));
                }

                ((ViewHolder) mHolder).rlReplies.removeAllViews(); // First reset the layout then add new views

                // Adding replies programmatically
                try {
                    Resources resources = mContext.getResources();
                    int repliesCount = bulletin.getReplies().size();
                    int margin = 0;
                    int marginIncrease = Util.getDp(15, resources);
                    int height = Util.getDp(24, resources);

                    // Adding image view
                    RoundedImageView imageView = null;
                    for (int i = 0; i < repliesCount; i++) {
                        if (i == 3) break; // Add no more than 3 views
                        imageView = new RoundedImageView(mContext);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height);
                        params.addRule(RelativeLayout.CENTER_VERTICAL);
                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        params.addRule(RelativeLayout.ALIGN_PARENT_START);
                        params.leftMargin = margin;
                        margin += marginIncrease; // increasing margin for next view

                        imageView.setImageResource(R.drawable.empty_photo);
                        imageView.setOval(true);
                        imageView.setBorderWidth(0);
                        imageView.setLayoutParams(params);
                        imageView.setId(i);

                        holder.rlReplies.addView(imageView);
                    }


                    // Adding text view
                    TextView textView = null;
                    if (repliesCount > 3) {
                        textView = new TextView(mContext);

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        layoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
                        layoutParams.leftMargin = Util.getDp(2, mContext.getResources());
                        textView.setLayoutParams(layoutParams);

                        textView.setGravity(Gravity.CENTER_VERTICAL);
                        String text = String.format(mContext.getString(R.string.bulletin_reply_text), repliesCount - 3);
                        textView.setText(text);

                        holder.rlReplies.addView(textView);

                    }

                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                }
                break;
            }
            case FIRST_ITEM: {
                BulletinsAdapter.ViewHolderFirst holder = (ViewHolderFirst) mHolder;
                holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(i);
                    }
                });
                holder.ibAttachment.setOnTouchListener((HomeActivity) mContext);
                holder.ibLocation.setOnTouchListener((HomeActivity) mContext);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? FIRST_ITEM : ITEM;
    }

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
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mMessage;
        public final TextView mName;
        public final TextView mDate;
        public final RelativeLayout rlReplies;
        public final LinearLayout btnReply;
        public ViewHolderClickListener mListener;

        public ViewHolder(View view, ViewHolderClickListener listener) {
            super(view);
            mView = view;
            mMessage = (TextView) view.findViewById(R.id.tvMessage);
            mMessage.setTag("msg");
            mName = (TextView) view.findViewById(R.id.tvName);
            mName.setTag("name");
            mDate = (TextView) view.findViewById(R.id.tvDate);
            rlReplies = (RelativeLayout) view.findViewById(R.id.rlReplies);
            btnReply = (LinearLayout) view.findViewById(R.id.llReply);
            mListener = listener;

            rlReplies.setOnClickListener(this);
            btnReply.setOnClickListener(this);
            mMessage.setOnClickListener(this);
            mName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof RelativeLayout) {
                int c = ((RelativeLayout) v).getChildCount();
                if (c > 0) // Only open if there are comments present
                    mListener.onRepliesClick(rlReplies, mView, true);
                return;
            } else if (v instanceof LinearLayout) {
                mListener.onRepliesClick(btnReply, mView, false);
                return;
            }

            String tag = v.getTag().toString();
            if (tag.equals("msg")) {
                mListener.onMsgClick(mMessage, mView);
            } else if (tag.equals("name")) {
                mListener.onNameClick(mName, mView);
                Log.d(BulletinsAdapter.class.getSimpleName(), "onNameClick();");
            }


        }

        protected interface ViewHolderClickListener {
            void onRepliesClick(View caller, View rootView, boolean onlyShowReplies);

            void onMsgClick(TextView mMessage, View mView);

            void onNameClick(TextView mName, View mView);
        }
    }

    protected static class ViewHolderFirst extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView ivProfilePic;
        public final TextView btnSendToSea;
        public final ImageButton ibLocation;
        public final ImageButton ibAttachment;

        public ViewHolderFirst(View view) {
            super(view);
            mView = view;
            ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
            btnSendToSea = (TextView) view.findViewById(R.id.tvSendToSea);
            ibLocation = (ImageButton) view.findViewById(R.id.ibLocation);
            ibAttachment = (ImageButton) view.findViewById(R.id.ibAttachment);
        }


    }
}

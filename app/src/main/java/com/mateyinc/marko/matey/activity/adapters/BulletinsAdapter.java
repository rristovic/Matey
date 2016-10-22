package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ContextMenu;
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
import com.mateyinc.marko.matey.activity.OnTouchInterface;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.home.NewBulletinActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.rounded_image_view.RoundedImageView;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data_and_managers.DataManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.Date;

import static com.mateyinc.marko.matey.data_and_managers.DataManager.COL_POST_ID;

public class BulletinsAdapter extends RecycleCursorAdapter {
    private static final String TAG = BulletinsAdapter.class.getSimpleName();

    // Maximum number of character in a post
    private static final int CHAR_LIM = 600;

    private static final int FIRST_ITEM = 1;
    private static final int ITEM = 2;

    private View mEmptyView;
    private RecyclerView mRecycleView;

    /**
     * Used in long click to determine the position of clicked view
     */
    public static int clickedPosition = -1;
    public static String clickedText = "";
    public static final int COPYTEXT_ID = 100;

    public BulletinsAdapter(Context context, TextView emptyView) {
        mContext = context;
        mManager = DataManager.getInstance(context);
        mEmptyView = emptyView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {

            if (mRecycleView == null)
                mRecycleView = (RecyclerView) parent;

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

    /**
     * Helper method for creating ViewHolder listener
     *
     * @return newly created listener
     */
    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {
            public void onRepliesClick(View caller, View rootView, boolean onlyShowReplies) {
                int position = mRecycleView.getChildAdapterPosition(rootView);
                if (onlyShowReplies) {
                    Intent i = new Intent(mContext, BulletinViewActivity.class);
                    mCursor.moveToPosition(position);
                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getInt(COL_POST_ID));
                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, position);
                    mContext.startActivity(i);
                } else {
                    Intent i = new Intent(mContext, BulletinViewActivity.class);
                    i.putExtra(BulletinViewActivity.EXTRA_NEW_REPLY, true);
                    mCursor.moveToPosition(position);
                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, mCursor.getInt(COL_POST_ID));
                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, position);
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
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mManager.getBulletin(position, mCursor).getUserID());
                mContext.startActivity(i);
            }

            @Override
            public boolean onLongClick(View v, View rootView) {
                clickedPosition = mRecycleView.getChildAdapterPosition(rootView);
                clickedText = ((TextView) v).getText().toString();
                return false;
            }
        };
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        switch (getItemViewType(position)) {
//             Parsing data to views if available

            case ITEM: {
                BulletinsAdapter.ViewHolder holder = (ViewHolder) mHolder;
                final Bulletin bulletin = mManager.getBulletin(position, mCursor);
                try {
                    if (bulletin.getMessage().length() > CHAR_LIM) {
                        // Setting text view message
                        SpannableString ss = new SpannableString(bulletin.getMessage().substring(0, CHAR_LIM - 1).concat(" ... ")
                                .concat(mContext.getString(R.string.continue_reading_message)));

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Intent i = new Intent(mContext, BulletinViewActivity.class);
                                i.putExtra(BulletinViewActivity.EXTRA_SHOW_BULLETIN, true);
                                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, bulletin.getPostID());
                                i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_POS, position);
                                mContext.startActivity(i);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(false);
                            }
                        };
                        ss.setSpan(clickableSpan, CHAR_LIM + 4, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the message
                        holder.mMessage.setMovementMethod(LinkMovementMethod.getInstance());
                        holder.mMessage.setHighlightColor(Color.TRANSPARENT);
                        holder.mMessage.setText(ss);
                    } else {
                        holder.mMessage.setText(bulletin.getMessage());
                    }
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
                    int repliesCount = bulletin.getNumOfReplies();
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
                        String text = String.format(mContext.getString(R.string.bulletin_reply_text), Integer.toString(repliesCount - 3));
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


    public void swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

        private final View mView;
        private final TextView mMessage;
        private final TextView mName;
        private final TextView mDate;
        private final RelativeLayout rlReplies;
        private final LinearLayout btnReply;
        ViewHolderClickListener mListener;

        private ViewHolder(View view, ViewHolderClickListener listener) {
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
            btnReply.setOnTouchListener(new OnTouchInterface(mView.getContext()));
            btnReply.setOnClickListener(this);
            mMessage.setOnClickListener(this);
            mName.setOnClickListener(this);
            mMessage.setOnCreateContextMenuListener(this);
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

        @Override
        public boolean onLongClick(View v) {
            return mListener.onLongClick(v, mView);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(null);
            menu.add(0, COPYTEXT_ID, 0, mView.getContext().getString(R.string.menu_item_copy_text));//groupId, itemId, order, title
        }


        private interface ViewHolderClickListener {
            void onRepliesClick(View caller, View rootView, boolean onlyShowReplies);

            void onMsgClick(TextView mMessage, View mView);

            void onNameClick(TextView mName, View mView);

            boolean onLongClick(View v, View mView);
        }
    }

    private static class ViewHolderFirst extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView ivProfilePic;
        private final TextView btnSendToSea;
        private final ImageButton ibLocation;
        private final ImageButton ibAttachment;

        private ViewHolderFirst(View view) {
            super(view);
            mView = view;
            ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
            btnSendToSea = (TextView) view.findViewById(R.id.tvSendToSea);
            ibLocation = (ImageButton) view.findViewById(R.id.ibLocation);
            ibAttachment = (ImageButton) view.findViewById(R.id.ibAttachment);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mView.getContext(), NewBulletinActivity.class);
                    mView.getContext().startActivity(i);
                }
            });
        }


    }
}

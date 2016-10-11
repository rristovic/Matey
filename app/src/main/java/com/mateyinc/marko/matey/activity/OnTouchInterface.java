package com.mateyinc.marko.matey.activity;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;

/**
 * Created by Sarma on 9/5/2016.
 */
public class OnTouchInterface implements View.OnTouchListener {

    private final Context mContext;

    public OnTouchInterface(Context context) {
        mContext = context;
    }

    // Coloring buttons programmatically instead of in XML
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ImageButton)
            onButtonTouch(v, event);
        else if (v instanceof ImageView)
            onImageViewTouch(v, event);
        else if (v instanceof LinearLayout)
            onLinearLayoutTouch(v, event);
        return false;
    }

    private void onLinearLayoutTouch(View v, MotionEvent event) {
        LinearLayout view = (LinearLayout) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < view.getChildCount(); i++) {
                    if (view.getChildAt(i) instanceof ImageView) {
                        ((ImageView) view.getChildAt(i)).setColorFilter(mContext.getResources().getColor(R.color.colorAccent));
                    } else if (view.getChildAt(i) instanceof TextView) {
                        ((TextView) view.getChildAt(i)).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < view.getChildCount(); i++) {
                    if (view.getChildAt(i) instanceof ImageView) {
                        ((ImageView) view.getChildAt(i)).setColorFilter(mContext.getResources().getColor(R.color.light_gray));
                    } else if (view.getChildAt(i) instanceof TextView) {
                        ((TextView) view.getChildAt(i)).setTextColor(mContext.getResources().getColor(R.color.light_gray));
                    }
                }
                break;
        }
    }

    private void onImageViewTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setColorFilter(mContext.getResources().getColor(R.color.colorAccent)); // White Tint
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.setColorFilter(mContext.getResources().getColor(R.color.light_gray)); // White Tint
                break;
        }
    }

    private void onButtonTouch(View v, MotionEvent event) {
        ImageButton button = (ImageButton) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(mContext.getResources().getColor(R.color.colorAccent)); // White Tint
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                button.setColorFilter(mContext.getResources().getColor(R.color.light_gray)); // White Tint
                break;

        }
    }
}

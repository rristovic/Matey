package com.mateyinc.marko.matey.activity;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

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
        return false;
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

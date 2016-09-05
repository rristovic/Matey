package com.mateyinc.marko.matey.activity;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.mateyinc.marko.matey.R;

/**
 * Created by Sarma on 9/5/2016.
 */
public class OnTouchInterface implements View.OnTouchListener {

    private final Context mContext;

    public OnTouchInterface(Context context){
        mContext = context;
    }

    // Coloring buttons programmatically instead of in XML
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageButton button;
        try {
            button = (ImageButton) v;
        } catch (ClassCastException e) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(mContext.getResources().getColor(R.color.colorAccent)); // White Tint
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                button.setColorFilter(mContext.getResources().getColor(R.color.light_gray)); // White Tint
                return false;

        }
        return false;
    }
}

package com.mateyinc.marko.matey.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by M4rk0 on 5/4/2016.
 */
public class LoginTextViewBtn extends TextView {

    public LoginTextViewBtn(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LoginTextViewBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoginTextViewBtn(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Fonts/Roboto-Light.ttf");
            setTypeface(tf);
        }
    }

}

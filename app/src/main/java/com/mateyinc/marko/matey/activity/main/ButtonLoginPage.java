package com.mateyinc.marko.matey.activity.main;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class ButtonLoginPage extends Button {

    public ButtonLoginPage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ButtonLoginPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonLoginPage(Context context) {
        super(context);
        init();
    }

    private void init() {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Fonts/Roboto-Light.ttf");
            setTypeface(tf);
    }

}
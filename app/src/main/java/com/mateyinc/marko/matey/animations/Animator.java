package com.mateyinc.marko.matey.animations;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by M4rk0 on 3/8/2016.
 */
@SuppressLint("NewApi")
public class Animator {

    public void animateLogin (
            final AppCompatActivity activity,
            final RelativeLayout mainLayout, final ImageView icon, final Button login_btn,
            final Button register_btn,final Button facebook_btn) {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                LayoutTransition layoutTransition = mainLayout.getLayoutTransition();
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                //TransitionManager.beginDelayedTransition(mainLayout);

                DisplayMetrics display = activity.getResources().getDisplayMetrics();

                int width = display.widthPixels;
                int height = display.heightPixels;
                LinearLayout.LayoutParams linRules;

                Log.d("wii", String.valueOf(width));

                if(width > 1200) {
                    linRules = new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT);
                } else {
                    linRules = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                }
                linRules.gravity = Gravity.CENTER;
                icon.setLayoutParams(linRules);

                        //TransitionManager.beginDelayedTransition(mainLayout);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                login_btn.setVisibility(LinearLayout.VISIBLE);

                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        register_btn.setVisibility(LinearLayout.VISIBLE);

                                        new Handler().postDelayed(new Runnable() {
                                            public void run() {
                                                facebook_btn.setVisibility(LinearLayout.VISIBLE);

                                            }
                                        }, 200);
                                    }
                                }, 200);

                            }
                        }, 200);


            }
        }, 800);

    }

}

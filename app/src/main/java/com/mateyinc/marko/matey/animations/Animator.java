package com.mateyinc.marko.matey.animations;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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
            final RelativeLayout mainLayout, final ImageView icon, final ImageView login_btn,
            final ImageView register_btn,final ImageView facebook_btn) {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                LayoutTransition layoutTransition = mainLayout.getLayoutTransition();
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                //TransitionManager.beginDelayedTransition(mainLayout);
                LinearLayout.LayoutParams linRules = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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

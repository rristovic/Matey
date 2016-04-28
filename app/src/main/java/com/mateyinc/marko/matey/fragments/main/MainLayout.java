package com.mateyinc.marko.matey.fragments.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.login.LoginManager;
import com.mateyinc.marko.matey.R;

import java.util.Arrays;

/**
 * Created by M4rk0 on 4/27/2016.
 */
public class MainLayout extends Fragment {

    RelativeLayout mainLayout;
    LinearLayout inputLayout;
    ImageView icon;
    Button login_btn;
    Button register_btn;
    Button facebook_btn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_activity, container, false);

        mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
        icon = (ImageView) view.findViewById(R.id.title_image);

        // REGISTER BUTTON and it's listener
        register_btn = (Button) view.findViewById(R.id.register_btn);
        //register_btn.setTypeface(Typeface.createFromAsset(view.getAssets(), "Roboto-Light.ttf"));
        register_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              // Add your code in here!

            }
        });

        facebook_btn = (Button) view.findViewById(R.id.facebook_btn);
        //facebook_btn.setTypeface(Typeface.createFromAsset(view.getAssets(), "Roboto-Light.ttf"));
        facebook_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "user_friends", "email"));
            }
        });

        // LOG IN BUTTON and it's listener
        login_btn = (Button) view.findViewById(R.id.login_btn);
        //login_btn.setTypeface(Typeface.createFromAsset(view.getAssets(), "Roboto-Light.ttf"));
        login_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Add your code in here!
                //Intent goHome = new Intent(MainActivity.this, Home.class);
                //startActivity(goHome);
                facebook_btn.setVisibility(View.GONE);
                register_btn.setVisibility(View.GONE);

            }
        });

        return view;
    }

}

package com.mateyinc.marko.matey.fragments.main;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.internet.procedures.FacebookLoginAs;

import org.json.JSONException;
import org.json.JSONObject;

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
    CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set facebook dependencies
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager= CallbackManager.Factory.create();
        facebookLogin();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_activity, container, false);

        mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
        icon = (ImageView) view.findViewById(R.id.title_image);

        // REGISTER BUTTON and it's listener
        register_btn = (Button) view.findViewById(R.id.register_btn);
        register_btn.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf"));
        register_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Add your code in here!

            }
        });

        facebook_btn = (Button) view.findViewById(R.id.facebook_btn);
        facebook_btn.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf"));
        facebook_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "user_friends", "email"));
            }
        });

        // LOG IN BUTTON and it's listener
        login_btn = (Button) view.findViewById(R.id.login_btn);
        login_btn.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf"));
        login_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        return view;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // when facebook login is done
    // this method will get on work
    protected void facebookLogin() {

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        final AccessToken token = loginResult.getAccessToken();
                        final Profile profile = Profile.getCurrentProfile();
                        // OVDE RADNJE NAKON LOGIN-A
                        //if (profile.getId() != null && profile.getFirstName() != null && profile.getLastName() != null) {
                        //	FacebookLoginAs fbLogin = new FacebookLoginAs(por);
                        //	fbLogin.execute(token.getToken(), profile.getId(), profile.getFirstName(), profile.getLastName());
                        //}


                        GraphRequest request = GraphRequest.newMeRequest(
                                token,
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        // Application code
                                        try {

                                            String email = object.getString("email");

                                            // OVDE RADNJE NAKON LOGIN-A
                                            if (profile.getId() != null && profile.getFirstName() != null && profile.getLastName() != null) {
                                                FacebookLoginAs fbLogin = new FacebookLoginAs();
                                                fbLogin.execute(token.getToken(), profile.getId(), profile.getFirstName(), profile.getLastName(), email);
                                            }

                                        } catch (JSONException e) {

                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "email");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });

    }

}

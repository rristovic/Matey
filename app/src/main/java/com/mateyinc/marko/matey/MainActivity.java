package com.mateyinc.marko.matey;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.procedures.FacebookLoginAs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

	RelativeLayout mainLayout;
	LinearLayout inputLayout;
	ImageView icon;
	Button login_btn;
	Button register_btn;
	Button facebook_btn;
	CallbackManager callbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set facebook dependencies
		//FacebookSdk.sdkInitialize(this.getApplicationContext());
		//callbackManager= CallbackManager.Factory.create();

		// status bar and onCreate

		super.onCreate(savedInstanceState);
		super.setStatusBarColor();
		super.setSecurePreferences(this);
		setContentView(R.layout.fragments_test);

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.fragment, waitingScreen);
		fragmentTransaction.commit();

		super.startTommy();

	}

	public void setLoginScreen () {

		mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
		icon = (ImageView) findViewById(R.id.title_image);

		// REGISTER BUTTON and it's listener
		register_btn = (Button) findViewById(R.id.register_btn);
		register_btn.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));
		register_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Add your code in here!

			}
		});

		facebook_btn = (Button) findViewById(R.id.facebook_btn);
		facebook_btn.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));
		facebook_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
			}
		});

		// LOG IN BUTTON and it's listener
		login_btn = (Button) findViewById(R.id.login_btn);
		login_btn.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));
		login_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Add your code in here!
				//Intent goHome = new Intent(MainActivity.this, Home.class);
				//startActivity(goHome);
				facebook_btn.setVisibility(View.GONE);
				register_btn.setVisibility(View.GONE);

			}
		});

		// Adding login manager
		//this.facebookLogin();

		// animating the activity
		//Animator animator = new Animator();
		//animator.animateLogin(this, mainLayout, icon, login_btn, register_btn, facebook_btn);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tommy.cancel(true);
	}
}

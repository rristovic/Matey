package com.mateyinc.marko.matey;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mateyinc.marko.matey.helpers.MotherActivity;
import com.mateyinc.marko.matey.internet.login.FbLoginAs;

import java.util.Arrays;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

	ImageView login_btn;
	ImageView register_btn;
	ImageView facebook_btn;
	CallbackManager callbackManager;
	TextView por;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager= CallbackManager.Factory.create();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		super.setStatusBarColor();

		// LOG IN BUTTON and it's listener
		login_btn = (ImageView) findViewById(R.id.login_btn);
		login_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Add your code in here!
				Intent goHome = new Intent(MainActivity.this, Home.class);
				startActivity(goHome);
			}
		});

		// REGISTER BUTTON and it's listener
		register_btn = (ImageView) findViewById(R.id.register_btn);
		register_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Add your code in here!
				Intent goHome = new Intent(MainActivity.this, Home.class);
				startActivity(goHome);
			}
		});

		facebook_btn = (ImageView) findViewById(R.id.facebook_btn);
		facebook_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
			}
		});

		por=(TextView) findViewById(R.id.por);
		LoginManager.getInstance().registerCallback(callbackManager,
				new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {

						AccessToken token = loginResult.getAccessToken();
						Profile profile = Profile.getCurrentProfile();
						// OVDE RADNJE NAKON LOGIN-A
						if (profile.getId() != null && profile.getFirstName() != null && profile.getLastName() != null) {
							FbLoginAs fbLogin = new FbLoginAs();
							fbLogin.execute(token.getToken(), profile.getId(), profile.getFirstName(), profile.getLastName());
						}


						/*GraphRequest request = GraphRequest.newMeRequest(
								token,
								new GraphRequest.GraphJSONObjectCallback() {
									@Override
									public void onCompleted(JSONObject object, GraphResponse response) {
										// Application code
										try {

											String email = object.getString("email");
											String birthday = object.getString("birthday"); // 01/31/1980 format
											String gender = object.getString("gender");

											// OVDE RADNJE NAKON LOGIN-A
											if (profile.getId() != null && profile.getFirstName() != null && profile.getLastName() != null) {
												FbLoginAs fbLogin = new FbLoginAs();
												fbLogin.execute(token.getToken(), profile.getId(), profile.getFirstName(), profile.getLastName());
											}

										} catch (JSONException e) {}

									}
								});
						Bundle parameters = new Bundle();
						parameters.putString("fields", "email, gender, birthday");
						request.setParameters(parameters);
						request.executeAsync();*/

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}
}

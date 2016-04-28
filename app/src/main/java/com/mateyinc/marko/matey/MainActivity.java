package com.mateyinc.marko.matey;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.mateyinc.marko.matey.fragments.main.MainLayout;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.procedures.FacebookLoginAs;

import org.json.JSONException;
import org.json.JSONObject;

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
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager= CallbackManager.Factory.create();

		// status bar and onCreate

		super.onCreate(savedInstanceState);
		super.setStatusBarColor();
		super.setSecurePreferences(this);
		setContentView(R.layout.fragments_test);
		desiredScreen = new MainLayout();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.fragment, waitingScreen);
		fragmentTransaction.commit();

		super.startTommy();

		// Adding login manager
		this.facebookLogin();

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

		if(tommy.getStatus() != AsyncTask.Status.FINISHED) {
			tommy.cancel(true);
		}

	}
}

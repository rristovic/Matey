package com.mateyinc.marko.matey.inall;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.facebook.login.LoginManager;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.util.Arrays;

@SuppressLint("NewApi")
public class MotherActivity extends AppCompatActivity {

	protected ScepticTommy tommy;
	public static SecurePreferences securePreferences;
	protected boolean mServerReady = false;

	public int fbAnswerType = 0;

	public void startTommy () {

		tommy = new ScepticTommy(this);
		tommy.execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setSecurePreferences (AppCompatActivity activity) {

		securePreferences = new SecurePreferences(activity, "credentials", "1checkMate1717", true);

	}

	public SecurePreferences getSecurePreferences() {

		return securePreferences;

	}

	public void clearUserCredentials() {

		securePreferences.removeValue("user_id");
		securePreferences.removeValue("uid");
		securePreferences.removeValue("username");
		securePreferences.removeValue("firstname");
		securePreferences.removeValue("lastname");

	}

	public boolean isInternetConnected () {

		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) return true;
		else return false;

	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {

		switch(id) {

			case 0: return new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle("Hey mate, there's a problem!")
					.setMessage("Some error occurred.")
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startTommy();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.create();

			case 2: return new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle("WOOHOO")
					.setMessage("You are logged in with email: "+securePreferences.getString("email")+"!")
					.setPositiveButton("Log Out",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

									LogoutAs logoutAs = new LogoutAs((MainActivity) MotherActivity.this);
									logoutAs.execute(securePreferences.getString("email"),
											securePreferences.getString("uid"),
											securePreferences.getString("device_id"));
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.create();

			case 3: return new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle("Hey " + bundle.getString("name") + ", you are already here!")
					.setMessage("You have connected earlier with your facebook account. We recommend you to merge these two accounts!")
					.setPositiveButton("Merge Accounts",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

									fbAnswerType = 1;
									LoginManager.getInstance().logInWithReadPermissions(MotherActivity.this, Arrays.asList("public_profile"));

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.create();

			case 4: return new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle("Hey mate, there's a problem!")
					.setMessage(bundle.getString("message"))
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.create();
		}

		return super.onCreateDialog(id);

	}


}

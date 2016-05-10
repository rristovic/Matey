package com.mateyinc.marko.matey.inall;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import org.json.JSONObject;

@SuppressLint("NewApi")
public class MotherActivity extends AppCompatActivity {

	protected ScepticTommy tommy;
	public SecurePreferences securePreferences;
	protected boolean mServerReady = false;

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

	@Override
	protected Dialog onCreateDialog(int id) {

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
			case 1: return new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle("Hey mate, there's a problem!")
					.setMessage("There is no internet connection! Please connect and try again.")
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
					.setMessage("You are logged in!")
					.setPositiveButton("Log Out",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

									LogoutAs logoutAs = new LogoutAs();
									try{

										String result = logoutAs.execute(securePreferences.getString("email"),
												securePreferences.getString("uid"),
												securePreferences.getString("device_id")).get();

										// if there is some result check if successful
										if (result != null) {

											JSONObject jsonObject = new JSONObject(result);

											// if successful, set everything to SecurePreferences
											if (jsonObject.getBoolean("success")) {

												clearUserCredentials();
												startTommy();

											} else throw new Exception();

										} else throw new Exception();

									} catch (Exception e) {
										// if there was an error, show corresponding alert dialog
										showDialog(0);
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.create();
		}

		return super.onCreateDialog(id);

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


}

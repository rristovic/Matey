package com.mateyinc.marko.matey.inall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.storage.SecurePreferences;

@SuppressLint("NewApi")
public class MotherActivity extends AppCompatActivity {

	protected ScepticTommy tommy;
	protected SecurePreferences securePreferences;
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

	public void setSecurePreferences (AppCompatActivity activity) {

		securePreferences = new SecurePreferences(activity, "credentials", "1checkMate1717", true);

	}

	public SecurePreferences getSecurePreferences() {

		return securePreferences;

	}

	public void putToPreferences(String key, String value) {

		this.securePreferences.put(key, value);

	}

	public String getFromPreferences(String key) {

		return this.securePreferences.getString(key);

	}

	public void clearPreferences() {

		this.securePreferences.clear();

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

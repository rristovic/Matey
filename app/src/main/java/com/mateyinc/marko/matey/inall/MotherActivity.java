package com.mateyinc.marko.matey.inall;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.askhelp.AskHelp;
import com.mateyinc.marko.matey.fragments.standard.ErrorScreen;
import com.mateyinc.marko.matey.fragments.standard.WaitingScreen;
import com.mateyinc.marko.matey.storage.SecurePreferences;

@SuppressLint("NewApi")
public class MotherActivity extends AppCompatActivity {

	protected ScepticTommy tommy;

	protected Toolbar toolbar;
	protected SecurePreferences securePreferences;

	public WaitingScreen waitingScreen = new WaitingScreen();
	public ErrorScreen errorScreen = new ErrorScreen();

	public void startTommy () {

		tommy = new ScepticTommy(this);
		tommy.execute();

	}

	public void setFragment (int fragmentPlaceId, Fragment fragment) {

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(fragmentPlaceId, fragment);
		fragmentTransaction.commit();

	}

	public void setStatusBarColor () {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(this.getResources().getColor(R.color.statusBar));
		}
		
	}
	
	public void setCustomToolbar () {
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_makenew) {
			Intent makeNew = new Intent (this, AskHelp.class);
			startActivity(makeNew);
			
			return true;
		}
		if (id == R.id.action_settings) {
			return true;
		}
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

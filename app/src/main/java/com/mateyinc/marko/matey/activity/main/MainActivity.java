package com.mateyinc.marko.matey.activity.main;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// status bar and onCreate
		super.onCreate(savedInstanceState);
		super.setStatusBarColor();
		super.setSecurePreferences(this);
		setContentView(R.layout.fragment_test);

		setFragment(R.id.fragment, waitingScreen);

		super.startTommy();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(tommy.getStatus() != AsyncTask.Status.FINISHED) {
			tommy.cancel(true);
		}

	}

}

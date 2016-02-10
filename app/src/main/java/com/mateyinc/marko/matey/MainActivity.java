package com.mateyinc.marko.matey;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mateyinc.marko.matey.helpers.MotherActivity;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

	Button login_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.main_activity);
		super.setStatusBarColor("loginPage");
		
		// Text font for "NOTIFINDA" title
		TextView app_title = (TextView) findViewById(R.id.app_title);
		//Typeface typeFace=Typeface.createFromAsset(getAssets(),"res/assets/fonts/Aller_BdIt.ttf");
		//app_title.setTypeface(typeFace);
		
		// listener for Log In button
		login_btn = (Button) findViewById(R.id.login_button);
		login_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		        // Add your code in here!
		        Intent goHome = new Intent(MainActivity.this, Home.class);
		        startActivity(goHome);
		      }
		});
		
	}
	
}

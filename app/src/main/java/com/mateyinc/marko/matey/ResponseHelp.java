package com.mateyinc.marko.matey;

import android.os.Bundle;

import com.mateyinc.marko.matey.helpers.MotherActivity;

public class ResponseHelp extends MotherActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.response_help_page);
		super.setStatusBarColor();
		super.setCustomToolbar();
	}
	
}

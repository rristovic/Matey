package com.mateyinc.marko.matey.activity.responsehelp;

import android.os.Bundle;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;

public class ResponseHelp extends MotherActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.response_help_page);
		super.setStatusBarColor();
		super.setCustomToolbar();
	}
	
}

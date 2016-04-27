package com.mateyinc.marko.matey.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.responsehelp.ResponseHelp;

public class HomeFragment extends Fragment {

	ImageView helpResponseBtn;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		helpResponseBtn = (ImageView) getActivity().findViewById(R.id.helpResponseBtn);
		helpResponseBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent respAct = new Intent(getActivity(), ResponseHelp.class);
				startActivity(respAct);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.home_page, container, false);
	}
	
}

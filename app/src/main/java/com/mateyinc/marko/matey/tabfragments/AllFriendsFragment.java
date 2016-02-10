package com.mateyinc.marko.matey.tabfragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mateyinc.marko.matey.R;

import java.util.Arrays;

public class AllFriendsFragment extends Fragment {
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		// Variables needed for taking phonebook
        String nameCsv = "";
        String[] nameArr;   
        
        // List that will contain all contacts from phonebook
        ListView friendsList = (ListView) getActivity().findViewById (R.id.friendsList);
        
        // Query for fetching all contacts
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        
        // While there is more contacts to be fetch, do
        while (phones.moveToNext()) {
        	// Store the name of a contact into this variable
        	String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        	
        	// if taken name isn't null, do
        	if(name!=null) {
        		nameCsv += name +",";
        	}
        	
        }
        // closing phones variable
        phones.close();
        
        // storing data into an string array and sorting alphabetically
        nameArr = nameCsv.split(",");
        Arrays.sort(nameArr, String.CASE_INSENSITIVE_ORDER);
        
        // creating ArrayAdapter and setting it on the list
        ArrayAdapter<String> arrAdapter = new ArrayAdapter<String> (getActivity(), R.layout.friends_list_item, R.id.friendName, nameArr);
        friendsList.setAdapter(arrAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub	
		return inflater.inflate(R.layout.all_friends_page, container, false);
	}
	
}

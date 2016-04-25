package com.mateyinc.marko.matey.activity.askhelp;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

@SuppressLint("NewApi")
public class AskHelp extends MotherActivity {
	
	// All variables that will serve keywords input
	EditText helpKeywords;
	ListView keywordsList;
	ArrayAdapter<String> keywordsAdapter;
	ArrayList<String> keywordsArray = new ArrayList <String> ();
	Button keywordsOkBtn;
	
	// All variables that will serve uploading image
	Button uploadedImageBtn;
	private static int RESULT_LOAD_IMAGE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.ask_help_page);
		super.setStatusBarColor();
		super.setCustomToolbar();
		
		// finding button for upload image action
		uploadedImageBtn = (Button) findViewById(R.id.buttonLoadImage);
		// on click listener for that button
		uploadedImageBtn.setOnClickListener(new OnClickListener () {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// starting gallery activity for result
				Intent gall = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(gall, RESULT_LOAD_IMAGE);
			}
			
		});
		
		// finding list that will store the keywords
		// and also setting ArrayAdapter for that list
		keywordsList = (ListView) findViewById(R.id.keywordsList);
		keywordsAdapter = new ArrayAdapter<String> (this, R.layout.keyword_list_item, R.id.keyword, keywordsArray);
		keywordsList.setAdapter(keywordsAdapter);
		
		// finding EditText and Button for keywords form
		helpKeywords = (EditText) findViewById (R.id.ask_help_keywords);
		keywordsOkBtn = (Button) findViewById (R.id.keywordsOkBtn);
		
		// on click action for "ok" button
		keywordsOkBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// Getting all the text from EditText for keywords
				// checking if it is empty string or null, if not proceed
				String text = helpKeywords.getText().toString();
				if(text == "" || text == null) return;
				// spliting taken text on @ character
				String[] keywords = text.split("@");
				// clear keywordsArray to make new one
				keywordsArray.clear();
				
				// algorithm that makes a new list and automatically push it in ListView
				for(int i=0; i<keywords.length; i++) {

						for(int j=0; j<keywords[i].length(); j++) {
							if(keywords[i].charAt(j)!= ' ') {
								keywordsArray.add( keywords[i].replaceAll("\\s", "") );
								keywordsAdapter.notifyDataSetChanged();
								setListViewHeightBasedOnChildren(keywordsList);
								break;
							}
						}
					
				}
				
				
			}
			
		});
		
	}
	
	public static void setListViewHeightBasedOnChildren(ListView listView) {
	    ListAdapter listAdapter = listView.getAdapter();
	    if (listAdapter == null)
	        return;

	    int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
	    int totalHeight = 0;
	    View view = null;
	    for (int i = 0; i < listAdapter.getCount(); i++) {
	        view = listAdapter.getView(i, view, listView);
	        if (i == 0)
	            view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

	        view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
	        totalHeight += view.getMeasuredHeight();
	    }
	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	    listView.setLayoutParams(params);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
 
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
             
            ImageView imageView = (ImageView) findViewById(R.id.uploadedImage);

            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);
         
        }
     
    }
	
	private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}

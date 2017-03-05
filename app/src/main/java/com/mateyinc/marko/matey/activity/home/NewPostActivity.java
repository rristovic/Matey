package com.mateyinc.marko.matey.activity.home;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.FilePath;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import static com.mateyinc.marko.matey.inall.MyApplication.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;


public class NewPostActivity extends MotherActivity {

    private static final String TAG = NewPostActivity.class.getSimpleName();



    private EditText etNewPostMsg, etNewPostSubject;
    private TextView tvPost, tvNewPostHeading;
    private ImageButton ibBack;
    private ImageView ivAddPhoto, ivAddLocation, ivAddFile, ivSend;
    private Toolbar mToolbar;

    private ArrayList<String> mFilePaths = new ArrayList<>();

    /**
     * Contains the id of the post that is being replied to
     **/
    public static final String EXTRA_POST_ID = "replied_postid";
    /**
     * Contains text that is being replied to, thus indicating that this isn't new post
     **/
    public static final String EXTRA_REPLY_SUBJECT = "post_subject";

    private static final int IMAGE_CAPTURE_REQ_CODE = 1002;
    private static final int PICK_FILE_REQ_CODE = 1000;
    private static final int GALLERY_REQ_CODE = 1001;

    private String selectedFilePath;
    private File mImageFile;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        init();
        setUI();
    }

    private void init() {
        // Settings the app bar via custom toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etNewPostMsg = (EditText) findViewById(R.id.etNewPostMsg);
        etNewPostSubject = (EditText) findViewById(R.id.etNewPostSubject);
        ivAddFile = (ImageView) findViewById(R.id.ivAddFile);
        ivAddLocation = (ImageView) findViewById(R.id.ivAddLocation);
        ivAddPhoto = (ImageView) findViewById(R.id.ivAddPhoto);
        ivSend = (ImageView) findViewById(R.id.ivSend);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        tvPost = (TextView) findViewById(R.id.tvPost);
        tvNewPostHeading = (TextView) findViewById(R.id.tvNewPostHeading);
        tvPost.setEnabled(false); // Can't post until something is typed in
        // First place in list will be reserved for text

        setClickListeners();
    }

    private void setUI() {
        Intent i = getIntent();

        if (i.hasExtra(EXTRA_REPLY_SUBJECT)) {
            String text = i.getStringExtra(EXTRA_REPLY_SUBJECT);
            etNewPostSubject.setText(text);
            etNewPostSubject.setFocusable(false);
            etNewPostMsg.setHint(null);
            etNewPostMsg.requestFocus();
            tvNewPostHeading.setText(null);
        }
    }


    private void setClickListeners() {
        ivAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMarshMallow = Build.VERSION.SDK_INT >= 23;
                if (isMarshMallow && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    return;
                }


                Intent intent = new Intent();
                //sets the select file to all types of files
                intent.setType("*/*");
                //allows to select data and return it
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //starts new activity to select file and return data
                startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQ_CODE);

            }
        });

        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMarshMallow = Build.VERSION.SDK_INT >= 23;
                if (isMarshMallow && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
                builder.setPositiveButton(R.string.chose_from_gal_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choseImage();
                    }
                });
                builder.setNegativeButton(getString(R.string.take_a_pic_label), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        captureImage();
                    }
                });
                builder.setTitle(R.string.choosing_new_pic_title);
                builder.setMessage(R.string.choosing_new_pic_msg);
                builder.create().show();
            }

        });


        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etNewPostMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableButton(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableButton(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableButton(s);
            }

            private void enableButton(CharSequence s) {
                if (s == null || s.length() == 0) {
                    tvPost.setEnabled(false);
                } else {
                    tvPost.setEnabled(true);
                }
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNewBulletin();
            }
        });


    }

    /**
     * Helper method for posting new bulletin.
     */
    private void postNewBulletin() {
        OperationManager operationManager = OperationManager.getInstance(NewPostActivity.this);
        operationManager.postNewBulletin(etNewPostSubject.getText().toString()
                , etNewPostMsg.getText().toString(), mFilePaths, NewPostActivity.this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                uploadFile();
//
//            }
//        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && (requestCode == PICK_FILE_REQ_CODE || requestCode == GALLERY_REQ_CODE
                || requestCode == IMAGE_CAPTURE_REQ_CODE)) {

            if (data == null) {
                //no data present
                return;
            }

            Uri selectedFileUri = data.getData();
            selectedFilePath = FilePath.getPath(this, selectedFileUri);
            Log.i(TAG, "Selected File Path:" + selectedFilePath);

            if (selectedFilePath != null && !selectedFilePath.equals("")) {
                mFilePaths.add(selectedFilePath);
            } else {
                Toast.makeText(this, R.string.file_not_supported, Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Method for sending intent for user to chose an image file
     */
    private void choseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_REQ_CODE);
        } else {
            Toast.makeText(this, R.string.no_image_picker, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method for sending intent for user to capture new image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                mImageFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }

            // Continue only if the File was successfully created
            if (mImageFile != null) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(mImageFile));
                startActivityForResult(intent, IMAGE_CAPTURE_REQ_CODE);
            } else {
                Toast.makeText(this, R.string.error_capture_image, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_cam_app, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // TODO - move picture path to internal
        File filepath = Environment.getExternalStorageDirectory();
        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath() + "/Matey/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        // Create a file for the image
        String mImageName = "profilePic_" + (new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())) + ".jpg";
        File image = File.createTempFile(
                mImageName,  /* prefix */
                ".jpg",         /* suffix */
                dir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }
}

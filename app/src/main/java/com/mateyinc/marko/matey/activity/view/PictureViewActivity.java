package com.mateyinc.marko.matey.activity.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.UploadNewPictureActivity;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;


public class PictureViewActivity extends MotherActivity {
    private static final String TAG = PictureViewActivity.class.getSimpleName();
    public static final String EXTRA_PIC_LINK = "picture_link";

    private PhotoView mImageView;
    private Button btnChangePic;

    // Request code for image capture
    private static final int IMAGE_CAPTURE_REQ_CODE = 100;
    // Request code for gallery image choosing
    private static final int GALLERY_REQ_CODE = 101;
    // Request code for changing the profile image
    private static final int CHANGE_PIC_REQ_CODE = 102;

    // If user capture new image it is saved here
    private String mCurrentPhotoPath;
    private File mImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        init();
        setListeners();
    }

    private void init() {
        mImageView = (PhotoView) findViewById(R.id.ivMain);
        btnChangePic = (Button) findViewById(R.id.btnChangePic);

        String picLink;
        if (getIntent().hasExtra(EXTRA_PIC_LINK))
            picLink = getIntent().getStringExtra(EXTRA_PIC_LINK);
        else {
            finish();
            return;
        }

        DataManager.getInstance(this).mImageLoader.get(picLink,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        mImageView.setImageBitmap(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }, mImageView.getWidth(), mImageView.getHeight());
    }

    private void setListeners() {
        btnChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PictureViewActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GALLERY_REQ_CODE:
            case IMAGE_CAPTURE_REQ_CODE:
                if (resultCode == RESULT_OK) {
                    Uri fullPhotoUri = data.getData();
                    Intent i = new Intent(PictureViewActivity.this, UploadNewPictureActivity.class);
                    i.putExtra(UploadNewPictureActivity.EXTRA_PIC_URI, fullPhotoUri);
                    startActivity(i);
                    break;
                }
        }
    }

    /**
     * Returns scaled bitmap with required params
     *
     * @param uri     image uri path
     * @param reqSize a size that the image should be reduced to
     * @return newly created bitmap object
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Bitmap getThumbnail(Uri uri, int reqSize) throws FileNotFoundException, IOException {
        InputStream input = PictureViewActivity.this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = calculateInSampleSize(onlyBoundsOptions, reqSize, reqSize);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    || (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}

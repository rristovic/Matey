package com.mateyinc.marko.matey.activity.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.UploadNewPictureActivity;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import uk.co.senab.photoview.PhotoView;


public class PictureViewActivity extends MotherActivity {
    private static final String TAG = PictureViewActivity.class.getSimpleName();
    public static final String EXTRA_PIC_LINK = "picture_link";

    private PhotoView mImageView;
    private Button btnChangePic;

    // Request code for image capture
    private int IMAGE_CAPTURE_REQ_CODE;
    // Request code for gallery image choosing
    private int GALLERY_REQ_CODE;
    // Request code for changing the profile image
    private int CHANGE_PIC_REQ_CODE;

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
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//
//                        if (intent.resolveActivity(getPackageManager()) != null) {
//                            try {
//                                mImageFile = createImageFile();
//                            } catch (IOException ex) {
//                                Log.e(AppConstant.TAG, ex.getStackTrace().toString());
//                            }
//
//                            // Continue only if the File was successfully created
//                            if (mImageFile != null) {
//                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                                        Uri.fromFile(mImageFile));
//                                startActivityForResult(intent, IMAGE_CAPTURE_REQ_CODE);
//                            }
//                        }
                    }
                });
                builder.setTitle(R.string.choosing_new_pic_title);
                builder.setMessage(R.string.choosing_new_pic_msg);
                builder.create().show();
            }
        });
    }

    private void choseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {

//            try {

            Uri fullPhotoUri = data.getData();
            Intent i = new Intent(PictureViewActivity.this, UploadNewPictureActivity.class);
            i.putExtra(UploadNewPictureActivity.EXTRA_PIC_URI, fullPhotoUri);
            startActivity(i);

            return;
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

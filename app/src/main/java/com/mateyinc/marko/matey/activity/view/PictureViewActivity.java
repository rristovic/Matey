package com.mateyinc.marko.matey.activity.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.UploadNewPictureActivity;
import com.mateyinc.marko.matey.activity.view.gestures.MoveGestureDetector;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.MultipartRequest;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class PictureViewActivity extends MotherActivity implements View.OnTouchListener {
    private static final String TAG = PictureViewActivity.class.getSimpleName();
    public static final String EXTRA_PIC_LINK = "picture_link";

    // Fields for scaling/panning with touch
    private float mScaleFactor = 1f;
    private float mFocusX, mFocusY, mInitialFocusX, mInitialFocusY;
    private Matrix mMatrix = new Matrix();// The Matrix class holds a 3x3 matrix to move the coordinates.
    private Matrix mInitialMatrix = new Matrix();

    // Listeners
    private ScaleGestureDetector mScaleDetector;
    private MoveGestureDetector mMoveDetector;

    private ImageView mImageView;
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
        mImageView = (ImageView) findViewById(R.id.ivMain);
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
//        scaleImage();
    }

    private void setListeners() {
        mImageView.setOnTouchListener(this);
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

        // Set gesture detectors
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());
        mMoveDetector = new MoveGestureDetector(getApplication(), new MoveListener());
    }

    private void choseImage() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, GALLERY_REQ_CODE);
            }
    }

    private void scaleImage() {
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // do your stuff
                Matrix m = mImageView.getImageMatrix();
                RectF drawableRect = new RectF(0, 0, mImageView.getDrawable().getIntrinsicWidth(), mImageView.getDrawable().getIntrinsicHeight());
                RectF viewRect = new RectF(0, 0, mImageView.getWidth(), mImageView.getHeight());
                m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
                mImageView.setImageMatrix(m);
                mInitialMatrix.set(mImageView.getImageMatrix());

                // Setting the focus point used for translating to image center
                mInitialFocusX = (mImageView.getWidth() * mScaleFactor) / 2;
                mInitialFocusY = (mImageView.getHeight() * mScaleFactor) / 2;
                mFocusX = mInitialFocusX;
                mFocusY = mInitialFocusY;

                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
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
//                String[] filePathColumn = { MediaStore.Images.Media.DATA,
//                        MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE};
//
//                Cursor cursor = getContentResolver().query(fullPhotoUri,
//                        filePathColumn, null, null, null);
//                cursor.moveToFirst();
//
//                String title = cursor.getString(1);
//                String mimeType = cursor.getString(2);
//                cursor.close();


//                uploadImage(getThumbnail(fullPhotoUri, ), title, mimeType);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            mImageView.setImageBitmap(thumbnail);
        }
    }


    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;
    /** Image will be compressed with this amount of quality; goes from 0 - 100 */
    private static final int JPG_QUALITY = 70;

    /**
     * Helper method for uploading image to the server
     * @param bitmap image that needs to be uploaded
     * @param title title that will be associated with the file
     * @param mimeType file mime type
     * @throws FileNotFoundException
     */
    private void uploadImage(Bitmap bitmap, String title, String mimeType) throws FileNotFoundException{
//        byte[] fileData1 = getFileDataFromImage(imageUri);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            buildPicPart(dos, bitmap, title, mimeType);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // pass to multipart body
            multipartBody = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = UrlData.POST_PICTURE;
        MultipartRequest multipartRequest = new MultipartRequest(url, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(PictureViewActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                Toast.makeText(PictureViewActivity.this, "Upload failed!\r\n" + volleyError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        multipartRequest.setAuthHeader(MotherActivity.access_token);
        DataManager.getInstance(this).submitRequest(multipartRequest);

    }

    /**
     * Method for building one part of multipart http body
     * @param dataOutputStream stream to write data to
     * @param imageBitmap building image part from this bitmap
     * @param title title of the part that is being added
     * @param mimeType mime type of the file
     * @throws IOException
     */
    private void buildPicPart(DataOutputStream dataOutputStream, Bitmap imageBitmap, String title, String mimeType) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"picture\"; filename=\""
                + title + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: " + mimeType + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        // Compress bitmap and write
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, JPG_QUALITY, dataOutputStream);

        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * Returns scaled bitmap with required params
     * @param uri image uri path
     * @param reqSize a size that the image should be reduced to
     * @return newly created bitmap object
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Bitmap getThumbnail(Uri uri, int reqSize) throws FileNotFoundException, IOException{
        InputStream input = PictureViewActivity.this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = calculateInSampleSize(onlyBoundsOptions, reqSize, reqSize);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float scaleImageCenterX = (mImageView.getWidth() * mScaleFactor) / 2;
        float scaleImageCenterY = (mImageView.getHeight() * mScaleFactor) / 2;

        // Process events
        mScaleDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);

        // Resetting view
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                // Reset view to initial state
                mImageView.setImageMatrix(mInitialMatrix);
                // Resets the focus points used for translating
                mFocusX = mInitialFocusX;
                mFocusY = mInitialFocusY;
                // Resets scale factor
                mScaleFactor = 1f;
                return true;
        }

        // Setting transformations
        mMatrix.set(mInitialMatrix); // Set matrix for calculation via initial state of image matrix
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postTranslate(mFocusX - scaleImageCenterX, mFocusY - scaleImageCenterY);

        // Applying transformation
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        view.setImageMatrix(mMatrix);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 1.0f));
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            float deltaThreshold = 1.5f;
            PointF d = detector.getFocusDelta();
            if ((d.x > deltaThreshold || d.x < -deltaThreshold) &&
                    d.y > deltaThreshold || d.y < -deltaThreshold) {
                mFocusX += d.x;
                mFocusY += d.y;
            }

            return true;
        }
    }
}

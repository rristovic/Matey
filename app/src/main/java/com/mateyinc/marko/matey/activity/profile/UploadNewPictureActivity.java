package com.mateyinc.marko.matey.activity.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.internet.MultipartRequest;
import com.mateyinc.marko.matey.data.internet.UrlData;
import com.mateyinc.marko.matey.inall.MotherActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;

import static android.R.attr.angle;
import static android.R.attr.pivotX;
import static android.R.attr.pivotY;
import static com.mateyinc.marko.matey.R.id.imageView;
import static com.mateyinc.marko.matey.activity.view.PictureViewActivity.calculateInSampleSize;


public class UploadNewPictureActivity extends MotherActivity {
    private static final String TAG = UploadNewPictureActivity.class.getSimpleName();

    public static final String EXTRA_PIC_URI = "picture_url";

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;

    /**
     * Image will be compressed with this amount of quality; goes from 0 - 100
     */
    private static final int JPG_QUALITY = 70;

    private ImageView ivRotateLeft, ivRotateRight;
    private PhotoView mImageView;
    private Button btnCancel, btnSave;
    private Bitmap mBitmap;
    private Uri mPicUri;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_new);

        init();
        setListeners();
    }

    private void init() {
        mImageView = (PhotoView) findViewById(R.id.ivMain);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);
        ivRotateLeft = (ImageView) findViewById(R.id.ivLeftRotate);
        ivRotateRight = (ImageView) findViewById(R.id.ivRightRotate);

        if (getIntent().hasExtra(EXTRA_PIC_URI))
            mPicUri = getIntent().getParcelableExtra(EXTRA_PIC_URI);
        else {
            finish();
            return;
        }

        try {
            // Load bitmap into memory
            mBitmap = getThumbnail(mPicUri, getMaxImageSize());
            // Filling up image view
            setLoadedPic(mBitmap);
        } catch (IOException e) {
            mBitmap = null;
            e.printStackTrace();
        }
    }

    private void setListeners() {
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Just making image view a square box
                int size = mImageView.getWidth();
                ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
                layoutParams.height = size;
                mImageView.setLayoutParams(layoutParams);

                // Adding frame view
                FrameView view = new FrameView(UploadNewPictureActivity.this, size);
                view.setLayoutParams(layoutParams);
                RelativeLayout main = (RelativeLayout) findViewById(R.id.rlMain);
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mImageView.onTouchEvent(event);
                        return false;
                    }
                });
                main.addView(view);

                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                saveImage();
                String[] filePathColumn = { MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE};

                Cursor cursor = getContentResolver().query(mPicUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                mImagePath = cursor.getString(0);
                String title = cursor.getString(1);
                String mimeType = cursor.getString(2);
                cursor.close();

                uploadImage(getImageForUpload(), title, mimeType);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFailed();
            }
        });

        ivRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setRotationBy(-90f);
            }
        });

        ivRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setRotationBy(90f);
            }

        });
    }

    /**
     * Retrieves the cropped bitmap from image view that is visible on the ui
     * @return cropped bitmap for upload
     */
    private Bitmap getImageForUpload() {
        final Bitmap imageBitmap = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(imageBitmap);
        mImageView.draw(c);

        return imageBitmap;
    }

    /**
     * Helper method for uploading image to the server
     *
     * @param bitmap   image that needs to be uploaded
     * @param title    title that will be associated with the file
     * @param mimeType file mime type
     */
    private void uploadImage(final Bitmap bitmap, String title, String mimeType) {
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
                Toast.makeText(UploadNewPictureActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                uploadSuccess(bitmap);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                Toast.makeText(UploadNewPictureActivity.this, "Upload failed!\r\n" + volleyError.toString(), Toast.LENGTH_SHORT).show();
                uploadFailed();
            }
        });

        multipartRequest.setAuthHeader(MotherActivity.access_token);
        DataManager.getInstance(this).submitRequest(multipartRequest);
    }

    /**
     * Method for building one part of multipart http body
     *
     * @param dataOutputStream stream to write data to
     * @param imageBitmap      building image part from this bitmap
     * @param title            title of the part that is being added
     * @param mimeType         mime type of the file
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
     * Call when image upload has success
     */
    private void uploadSuccess(Bitmap bitmap){
//        Intent i = new Intent();
//        Bundle bundle = new Bundle(bitmap.getByteCount());
//        bundle.putByteArray("picture", );
        setResult(RESULT_OK );
        finish();
    }

    /**
     * Call when image upload has failed
     */
    private void uploadFailed(){
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveImage() {
        final Bitmap imageBitmap = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(imageBitmap);
        mImageView.draw(c);

        Toast.makeText(this.getApplicationContext(), "Posting image..", Toast.LENGTH_SHORT).show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream output = null;
                try {
                    // Find the SD Card path
                    // TODO - move picture path to internal
                    File filepath = Environment.getExternalStorageDirectory();
                    // Create a new folder in SD Card
                    File dir = new File(filepath.getAbsolutePath() + "/Matey/");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    // Create a file for the image
                    String mImageName = "profilePic_" + (new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())) + ".png";
                    File file = new File(dir, mImageName);

                    output = new FileOutputStream(file);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.flush();
                    output.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    verifyStoragePermissions(UploadNewPictureActivity.this);
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
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
    public Bitmap getThumbnail(Uri uri, int reqSize) throws IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

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
        Log.d(TAG, String.format(Locale.US, "Bitmap (%d MB) decoded with %d sample size; %d original width; %d original height; %d required size",
                bitmap.getByteCount() / (1024 * 1024),
                bitmapOptions.inSampleSize, onlyBoundsOptions.outWidth, onlyBoundsOptions.outHeight, reqSize));
        input.close();

        return bitmap;
    }

    public static int getMaxImageSize() {
        int imageSize = 2048;
        long size = imageSize * imageSize * 4; // because ARGB_8888 is used for decoding
        long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
        boolean sizeFound = false;

        while (true) {
            final long heapPad = (long) Math.max(4 * 1024 * 1024, Runtime.getRuntime().maxMemory() * 0.1);
            if ((size + allocNativeHeap + heapPad) >= Runtime.getRuntime().maxMemory()) {
                // Image can't fit, reduce size, try again
                imageSize -= 128;
                continue;
            }

            return imageSize;
        }
    }

    private void setLoadedPic(Bitmap bitmap) {
        if (mBitmap != null)
            mImageView.setImageBitmap(bitmap);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private class FrameView extends ImageView {

        private Paint circlePaint;
        private Bitmap backgroundBitmap;
        private Bitmap circleMaskBitmap;
        private Canvas bgCanvas, circleCanvas;
        private Rect backRect;
        private Paint antiAPaint;
        private int size;

        public FrameView(Context context, int size) {
            super(context);
            this.size = size;

            backgroundBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            circleMaskBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
            bgCanvas = new Canvas(backgroundBitmap);
            circleCanvas = new Canvas(circleMaskBitmap);
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            circlePaint.setAntiAlias(true);
            backRect = new Rect(0, 0, size, size);
            antiAPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw the background
            circlePaint.setColor(Color.DKGRAY);
            circlePaint.setAlpha(50);
            bgCanvas.drawRect(backRect, circlePaint);

            // Draw the circle mask
            circlePaint.setColor(Color.RED);
            circlePaint.setAlpha(255);
            circleCanvas.drawCircle(size / 2, size / 2, size / 2 - 2, circlePaint);

            setLayerType(LAYER_TYPE_HARDWARE, antiAPaint);
            canvas.drawBitmap(backgroundBitmap, 0, 0, antiAPaint);
            antiAPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawBitmap(circleMaskBitmap, 0, 0, antiAPaint);
            antiAPaint.setXfermode(null);
        }
    }



//    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {
//
//        @Override
//        public void onPhotoTap(View view, float x, float y) {
//            float xPercentage = x * 100f;
//            float yPercentage = y * 100f;
//
////            showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
//        }
//
//        @Override
//        public void onOutsidePhotoTap() {
////            showToast("You have a tap event on the place where out of the photo.");
//        }
//    }
//
//    private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {
//
//        @Override
//        public void onMatrixChanged(RectF rect) {
////            mCurrMatrixTv.setText(rect.toString());
//        }
//    }
//
//    private class SingleFlingListener implements PhotoViewAttacher.OnSingleFlingListener {
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
////            if (BuildConfig.DEBUG) {
////                Log.d("PhotoView", String.format(FLING_LOG_STRING, velocityX, velocityY));
////            }
//            return true;
//        }
//    }
}

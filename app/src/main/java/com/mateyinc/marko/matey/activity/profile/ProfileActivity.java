package com.mateyinc.marko.matey.activity.profile;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.OperationFactory;
import com.mateyinc.marko.matey.data.operations.Operations;
import com.mateyinc.marko.matey.data.operations.UserProfileOp;
import com.mateyinc.marko.matey.inall.MotherActivity;


public class ProfileActivity extends MotherActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private String TAG = ProfileActivity.class.getSimpleName();

    public static final String PROFILE_DOWNLOADED = "com.mateyinc.marko.matey.activity.profile.profile_downloaded";

    /** Intent extra param which has user id
     * used to startDownloadAction data from the server or from database; */
    public static final String EXTRA_PROFILE_ID = "com.mateyinc.marko.matey.activity.profile.user_id";

    // Projection for profile query
    private static final String[] PROFILE_DATA_PROJECTION = new String[]{
            ProfileEntry.COLUMN_FULL_NAME,
            ProfileEntry.COLUMN_PROF_PIC,
            ProfileEntry.COLUMN_COVER_PIC,
            ProfileEntry.COLUMN_FOLLOWERS_NUM,
            ProfileEntry.COLUMN_FOLLOWING_NUM
    };
    // Indices going with above projection
    private static final int COL_FULL_NAME = 0;
    private static final int COL_PROFILE_PIC = COL_FULL_NAME + 1;
    private static final int COL_COVER_PIC = COL_PROFILE_PIC + 1;
    private static final int COL_FOLLOWERS_NUM = COL_COVER_PIC + 1;
    private static final int COL_FOLLOWING_NUM = COL_FOLLOWERS_NUM + 1;

    private TextView tvName, tvFollowersNum, tvFollowingNum;
    private ImageView ivProfilePic, ivCoverPic;
    private ToggleButton tBtnSailWith;
    private Button btnSendMsg;
    private long mUserId;
    private TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        init();
        setClickListeners();
        readData();
    }

    private void init() {
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        ivCoverPic = (ImageView) findViewById(R.id.ivCoverPic);
        tvName = (TextView) findViewById(R.id.tvName);
        tvFollowersNum = (TextView) findViewById(R.id.tvFollowersNum);
        tvFollowingNum = (TextView) findViewById(R.id.tvFollowingNum);
        tBtnSailWith = (ToggleButton) findViewById(R.id.tBtnSailWith);
        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);

        // If intent doesn't have extra profile id, then ProfileActivity is called for the current user profile
        if (getIntent().hasExtra(EXTRA_PROFILE_ID))
            mUserId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);
        else{
            mUserId = DataManager.getInstance(ProfileActivity.this).getCurrentUserProfile().getUserId();
        }
    }

    private void setClickListeners() {
        tBtnSailWith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                OperationFactory factory = OperationFactory.getInstance(ProfileActivity.this);
                Operations userProfileOp = factory.getOperation(OperationFactory.OperationType.USER_PROFILE_OP);
                if (isChecked) {
                    // Follow/following current user
                    userProfileOp.startUploadAction(
                            UserProfileOp.followNewUserAction(mUserId)
                    );
                } else {
                    // unfollow/not following cur user
                    userProfileOp.startUploadAction(
                            UserProfileOp.unfollowUserAction(mUserId)
                    );
                }
            }
        });

        tvFollowersNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
                i.setAction(FollowersActivity.ACTION_FOLLOWERS);
                i.putExtra(FollowersActivity.EXTRA_PROFILE_ID, mUserId);
                startActivity(i);
            }
        });

        tvFollowingNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProfileActivity.this, FollowersActivity.class);
                i.putExtra(FollowersActivity.EXTRA_PROFILE_ID, mUserId);
                i.setAction(FollowersActivity.ACTION_FOLLOWING);
                startActivity(i);
            }
        });

        final View parent = (View) tvFollowersNum.getParent();  // button: the view you want to enlarge hit area
        parent.post( new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                tvFollowersNum.getHitRect(rect);
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect , tvFollowersNum));

                tvFollowingNum.getHitRect(rect);
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect , tvFollowingNum));
            }
        });
    }

    /**
     * Helper method for downloading and saving data to database
     */
    private void readData() {
        // Initialise LoaderManager
        getSupportLoaderManager().initLoader(0, null, this);
        // Download new data
        OperationFactory factory = OperationFactory.getInstance(this);
        Operations operations = factory.getOperation(
                OperationFactory.OperationType.USER_PROFILE_OP);
        operations.startDownloadAction(
                UserProfileOp.getUserProfileAction(mUserId));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ProfileEntry.CONTENT_URI,
                PROFILE_DATA_PROJECTION,
                ProfileEntry._ID + " = ?",
                new String[]{Long.toString(mUserId)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst())
            return;

        DataManager.getInstance(this).mImageLoader.get(data.getString(COL_PROFILE_PIC),
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        ivProfilePic.setImageBitmap(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }, ivProfilePic.getWidth(), ivProfilePic.getHeight());
        DataManager.getInstance(this).mImageLoader.get(data.getString(COL_COVER_PIC),
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        ivCoverPic.setImageBitmap(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }, ivCoverPic.getWidth(), ivCoverPic.getHeight());

        tvName.setText(data.getString(COL_FULL_NAME));
        tvFollowersNum.setText(Integer.toString(data.getInt(COL_FOLLOWERS_NUM)));
        tvFollowingNum.setText(Integer.toString(data.getInt(COL_FOLLOWING_NUM)));

        Log.d("ProfileActivity", "Data is set.");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

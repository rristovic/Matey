package com.mateyinc.marko.matey.activity.profile;

import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.data.OperationFactory;
import com.mateyinc.marko.matey.data.internet.NetworkManager;
import com.mateyinc.marko.matey.data.internet.profile.UserProfileAs;
import com.mateyinc.marko.matey.data.operations.Operations;
import com.mateyinc.marko.matey.inall.MotherActivity;

public class ProfileActivity extends MotherActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private String TAG = ProfileActivity.class.getSimpleName();

    public static final String PROFILE_DOWNLOADED = "com.mateyinc.marko.matey.activity.profile.profile_downloaded";

    /** Intent extra param which has user id
     * used to download data from the server or from database; */
    public static final String EXTRA_PROFILE_ID = "com.mateyinc.marko.matey.activity.profile.user_id";

    private TextView tvName,tvNumberOfMates;
    private ImageView ivProfilePic;
    private long mUserId;
    private com.mateyinc.marko.matey.model.UserProfile mUserProfile;
    private BroadcastReceiver mBroadcastReceiver;
    private UserProfileAs mUserProfileAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);

        init();
        readData();
    }


    private void init() {
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvName = (TextView) findViewById(R.id.tvName);
        tvNumberOfMates = (TextView)findViewById(R.id.tvNumOfMates);
        mUserProfile = new com.mateyinc.marko.matey.model.UserProfile();

        // If intent doesn't have extra profile id, then ProfileActivity is called for the current user profile
        if (getIntent().hasExtra(EXTRA_PROFILE_ID))
            mUserId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);
        else{
            mUserId = DataManager.getInstance(ProfileActivity.this).getCurrentUserProfile().getUserId();
        }
    }

    private void setData() {
        Log.d("ProfileActivity", "Data is set.");
        //mImageLoader.displayImage(mUserProfile.getProfilePictureLink(), ivProfilePic);
        NetworkManager.getInstance(this).downloadImage(ivProfilePic, mUserProfile.getProfilePictureLink());
        tvName.setText(mUserProfile.getFirstName() + " " + mUserProfile.getLastName());
        tvNumberOfMates.setText(mUserProfile.getNumOfFriends());
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
        operations.download(mUserId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

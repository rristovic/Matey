package com.mateyinc.marko.matey.activity.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.view.PictureViewActivity;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

import static com.mateyinc.marko.matey.activity.profile.UploadNewPictureActivity.KEY_COVER_PATH;
import static com.mateyinc.marko.matey.activity.profile.UploadNewPictureActivity.KEY_PROF_PIC_PATH;


public class ProfileActivity extends MotherActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static final String PROFILE_DOWNLOADED = "com.mateyinc.marko.matey.activity.profile.profile_downloaded";

    /**
     * Intent extra param which has user id
     * used to startDownloadAction data from the server or from database;
     */
    public static final String EXTRA_PROFILE_ID = "com.mateyinc.marko.matey.activity.profile.user_id";
    /**
     * When uploading new pic, this is how it will be delivered
     */
    public static final String EXTRA_PIC_BITMAP = TAG + ".picture_bitmap";

    // Flag for database to indicate that the picture has changed, and is currently being uploaded
    public static final String FLAG_CHANGED = TAG + ".changed";
    private static final String FLAG_PROFILE_CHANGED = "profile_changed";
    private static final String FLAG_COVER_CHANGED = "cover_changed";

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
    private ImageButton ibSettings;
    private ToggleButton tBtnSailWith;
    private RecyclerView rvActivities;
    private RelativeLayout rlBadges;
    private Button btnSendMsg;
    private TextView tvHeading;
    private ScrollView svScrollFrame;

    private String mPicLink = "";
    private String mCoverLink = "";
    private long mUserId;
    private boolean isCurUser = false;

    // Position used for scroll control
    private float mBadgesPos;

    private OperationManager mOpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Entered onCreate()");
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setChildSupportActionBar();
        init();
        setListeners();
        readData();
    }

    private void init() {
        // If intent doesn't have extra profile id, then ProfileActivity is called for the current user profile
        if (getIntent().hasExtra(EXTRA_PROFILE_ID))
            mUserId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);
        else {
            mUserId = OperationManager.getInstance(ProfileActivity.this).getCurrentUserProfile().getUserId();
            isCurUser = true;
        }

         mOpManager = OperationManager.getInstance(this);

        // UI bounding
        svScrollFrame = (ScrollView) findViewById(R.id.svScrollFrame);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        ivCoverPic = (ImageView) findViewById(R.id.ivCoverPic);
        tvName = (TextView) findViewById(R.id.tvName);
        tvFollowersNum = (TextView) findViewById(R.id.tvFollowersNum);
        tvFollowingNum = (TextView) findViewById(R.id.tvFollowingNum);
        tBtnSailWith = (ToggleButton) findViewById(R.id.tBtnSailWith);
        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        rlBadges = (RelativeLayout) findViewById(R.id.llBadges);
        rvActivities = (RecyclerView) findViewById(R.id.rvActivities);
        ibSettings = (ImageButton) findViewById(R.id.ibSettings);
        if (isCurUser) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.llMainButtons);
            layout.removeAllViews();
            layout.setVisibility(View.GONE);
            ibSettings.setVisibility(View.GONE);
        }

        rvActivities.setAdapter(new Adatpter());
        final LayoutManager layoutManager = new LayoutManager(ProfileActivity.this);
        rvActivities.setLayoutManager(layoutManager);
    }

    private class LayoutManager extends LinearLayoutManager {

        boolean isScrollEnabled = true;

        public LayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return isScrollEnabled && super.canScrollVertically();
        }
    }

    private void setListeners() {
        if (!isCurUser) {
            tBtnSailWith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Follow/following current user
                        mOpManager.followNewUser(mUserId, ProfileActivity.this);
                    } else {
                        // unfollow/not following cur user
                        mOpManager.unfollowUser(mUserId, ProfileActivity.this);
                    }
                }
            });

            ibSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileActivity.this);
                    dialog.setAdapter(new ArrayAdapter<String>(ProfileActivity.this,
                                    android.R.layout.simple_list_item_1, new String[]{getString(R.string.action_block)}),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO - finish block feature
                                }
                            })
                            .setTitle(null).create().show();
                }
            });
        }


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
        parent.post(new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                tvFollowersNum.getHitRect(rect);
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, tvFollowersNum));

                tvFollowingNum.getHitRect(rect);
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, tvFollowingNum));
            }
        });

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileActivity.this.startActivity(
                        getViewIntent(PictureViewActivity.ACTION_PROFILE_PIC, mPicLink));
            }
        });

        ivCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.this.startActivity(
                        getViewIntent(PictureViewActivity.ACTION_COVER_PIC, mCoverLink)
                );
            }
        });

        tvFollowersNum.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] ints = new int[2];
                tvFollowersNum.getLocationOnScreen(ints);
                mBadgesPos = ints[1];

                //Remove the listener before proceeding
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tvFollowingNum.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    tvFollowingNum.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

//        svScrollFrame.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                int scrollY = svScrollFrame.getScrollY(); // For ScrollView
////                int scrollX = rootScrollView.getScrollX(); // For HorizontalScrollView
//                // DO SOMETHING WITH THE SCROLL COORDINATES
//            }
//        });
    }

    /**
     * Create intent to view the image
     *
     * @param intentAction action can be <br>{@link PictureViewActivity#ACTION_COVER_PIC}, {@link PictureViewActivity#ACTION_PROFILE_PIC}
     * @param picLink      string picture link
     * @return newly create intent
     */
    private Intent getViewIntent(String intentAction, String picLink) {
        Intent i = new Intent(ProfileActivity.this, PictureViewActivity.class);
        i.putExtra(PictureViewActivity.EXTRA_PIC_LINK, picLink);
        i.putExtra(PictureViewActivity.EXTRA_IS_CUR_USER, isCurUser);
        i.putExtra(PictureViewActivity.EXTRA_USER_ID, mUserId);
        i.setAction(intentAction);

        return i;
    }

    /**
     * Helper method for downloading and saving data to database
     */
    private void readData() {
        // Initialise LoaderManager
        getSupportLoaderManager().initLoader(0, null, this);
        // Download new data
        mOpManager.downloadUserProfile(mUserId, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        ivProfilePic.setImageBitmap(((Bitmap) intent.getParcelableExtra(EXTRA_PIC_BITMAP)));
//        ivProfilePic.setColorFilter(Color.argb(125, 0, 0, 0));
//        OperationManager.getInstance(this).mImageLoader.isCached("",1,1);
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
        Log.d(TAG, "Entered onLoadFinished()");
        if (data == null || !data.moveToFirst())
            return;

        mPicLink = data.getString(COL_PROFILE_PIC);
        mCoverLink = data.getString(COL_COVER_PIC);

        if (mPicLink.equals(FLAG_CHANGED)) {
            mPicLink = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).
                    getString(UploadNewPictureActivity.KEY_PROF_PIC_URI, "");
            loadTempPic(FLAG_PROFILE_CHANGED);
        } else
            OperationManager.getInstance(this).mImageLoader.get(mPicLink,
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

        if (mCoverLink.equals(FLAG_CHANGED)) {
            mCoverLink = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).
                    getString(UploadNewPictureActivity.KEY_COVER_URI, "");
            loadTempPic(FLAG_COVER_CHANGED);
        } else
            OperationManager.getInstance(this).mImageLoader.get(mCoverLink,
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

    /**
     * Method for loading temporary picture, until picture is uploaded
     */
    private void loadTempPic(final String flagTypeChanged) {
        // TODO - finish loading temp pic and saving it
        ImageLoader loader = new ImageLoader(Volley.newRequestQueue(getApplicationContext()), new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
                    cache = new LruCache<String, Bitmap>(2);

            @Override
            public Bitmap getBitmap(String url) {
                // Get image path
                if (flagTypeChanged.equals(FLAG_COVER_CHANGED)) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String path = pref.getString(KEY_COVER_PATH, "");
                    return BitmapFactory.decodeFile(path);
                } else if (flagTypeChanged.equals(FLAG_PROFILE_CHANGED)) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String path = pref.getString(KEY_PROF_PIC_PATH, "");
                    return BitmapFactory.decodeFile(path);
                }

                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

        int width, height;


        if (flagTypeChanged.equals(FLAG_COVER_CHANGED)) {
            width = ivCoverPic.getWidth();
            height = ivCoverPic.getHeight();
        } else {
            width = ivProfilePic.getWidth();
            height = ivProfilePic.getHeight();
        }

        loader.get(flagTypeChanged, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (flagTypeChanged.equals(FLAG_COVER_CHANGED))
                    ivCoverPic.setImageBitmap(response.getBitmap());
                else
                    ivProfilePic.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }, width, height);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
    }

    private class Adatpter extends RecyclerView.Adapter<ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            view.setLayoutParams(layoutParams);
            view.setText("Lorem Ipsum");

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 20;
        }


    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

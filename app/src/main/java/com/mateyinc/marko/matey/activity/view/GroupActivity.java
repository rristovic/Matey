package com.mateyinc.marko.matey.activity.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.NewPostActivity;
import com.mateyinc.marko.matey.adapters.BulletinsAdapter;
import com.mateyinc.marko.matey.adapters.GroupActivityAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.DownloadTempListEvent;
import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Group;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GroupActivity extends MotherActivity {
    public static final String TAG = "GroupActivity";

    public static final String EXTRA_MODEL_POSITION = "group_position";
    public static final String EXTRA_GROUP_ID = "group_id";

    private TextView tvGroupName, tvGroupDesc, tvGroupStats, tvGroupCrewNum;
    private ImageView ivGroupPic, ivShader;
    private ToggleButton btnSail;
    private RecyclerView rvBulletinList;
    private NestedScrollView svMainScrollFrame;
    private FloatingActionButton mFab;
    private Drawable mActionBarBackgroundDrawable;

    private int mGroupIndex;
    private long mGroupId;
    private Group mCurGroup;
    private BulletinsAdapter mAdapter;
    private OperationManager mManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        setChildSupportActionBar(true);
        init();
        setListeners();
        updateCurGroup();
        downloadData();
    }

    private void init() {
        mManager = OperationManager.getInstance(this);
        mFab = (FloatingActionButton) findViewById(R.id.fabNewGroupPost);
        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvGroupDesc = (TextView) findViewById(R.id.tvGroupInfo);
        tvGroupStats = (TextView) findViewById(R.id.tvGroupStats);
        tvGroupCrewNum = (TextView) findViewById(R.id.tvGroupCrewNum);
        ivGroupPic = (ImageView) findViewById(R.id.ivGroupPic);
        ivShader = (ImageView) findViewById(R.id.ivShader);
        svMainScrollFrame = (NestedScrollView) findViewById(R.id.svMainScrollFrame);
        btnSail = (ToggleButton) findViewById(R.id.btnSail);
        rvBulletinList = (RecyclerView) findViewById(R.id.rvGroupBulletins);
        mAdapter = new GroupActivityAdapter(this);
        rvBulletinList.setAdapter(mAdapter);
        rvBulletinList.setNestedScrollingEnabled(false);

        // Style toolbar
        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.actionbar_profile_drawable);
        mActionBarBackgroundDrawable.setAlpha(0);
        getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
    }

    private void setListeners() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupActivity.this, NewPostActivity.class);
                i.putExtra(NewPostActivity.EXTRA_GROUP_ID, mCurGroup.getId());
                startActivity(i);
            }
        });

        svMainScrollFrame.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            GradientDrawable shape = new GradientDrawable();

            String hexColor = String.format("%08X", (ContextCompat.getColor(GroupActivity.this, R.color.colorPrimary))).substring(2);
            boolean fullColorOn = false;

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                final int headerHeight = ivGroupPic.getHeight() - getSupportActionBar().getHeight();
                final float ratio = (float) Math.min(Math.max(Math.abs(scrollY), 0), headerHeight) / headerHeight;
                final int newAlpha = (int) (ratio * 255);


                if (headerHeight <= Math.abs(scrollY)) {
//                    ivProfilePic.animate().alpha(0f).setDuration(100).start();
                    mActionBarBackgroundDrawable.setAlpha(newAlpha);
                } else {
                    mActionBarBackgroundDrawable.setAlpha(0);
//                    ivProfilePic.animate().alpha(1f).setDuration(100).start();
                }


                int color;
                try {
                    color = Color.parseColor("#".concat(String.format("%02X", newAlpha)).concat(hexColor));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Faield to parse color value.", e);
                    color = Color.TRANSPARENT;
                }

                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(color);

                ivShader.setBackground(shape);
            }
        });

        btnSail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCurGroup != null) {
                    if (isChecked && !mCurGroup.isFollowed()) {
                        mManager.followGroup(mCurGroup, GroupActivity.this);
                    } else if (!isChecked && mCurGroup.isFollowed()) {
                        mManager.unfollowGroup(mCurGroup, GroupActivity.this);
                    }
                }
            }
        });
    }

    /**
     * Updates current group model and refresh UI if update was successful
     */
    private void updateCurGroup() {
        Intent i = getIntent();
        if (i.hasExtra(EXTRA_MODEL_POSITION)) {
            mGroupIndex = i.getIntExtra(EXTRA_MODEL_POSITION, -1);
            mCurGroup = DataAccess.getInstance(this).getGroups().get(mGroupIndex);
        } else if (i.hasExtra(EXTRA_GROUP_ID)) {
            mGroupId = i.getLongExtra(EXTRA_GROUP_ID, -1);
            mCurGroup = DataAccess.getInstance(this).getGroupById(mGroupId);
        } else {
            finish();
            Log.e(TAG, "No group info provided into GroupActivity.");
        }

        if (mCurGroup != null)
            setupUI();
        else {
            mCurGroup = new Group(mGroupId);
            setupUI();
        }
    }

    private void setupUI() {
        // Setup image
        OperationManager.getInstance(this).mImageLoader.get(mCurGroup.getPicUrl(),
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        ivGroupPic.setImageBitmap(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getLocalizedMessage(), error);
                    }
                }, ivGroupPic.getWidth(), ivGroupPic.getHeight());
        // Setup data
        tvGroupName.setText(mCurGroup.getGroupName());
        tvGroupDesc.setText(mCurGroup.getDescription());
        tvGroupCrewNum.setText(Integer.toString(mCurGroup.getNumOfFollowers()));

        btnSail.setChecked(mCurGroup.isFollowed());
//        mAdapter.setData(mCurGroup.getBulletinList());
    }

    /**
     * Helper method for downloading group data.
     */
    private void downloadData() {
        OperationManager.getInstance(this).downloadGroupInfo(this.mCurGroup.getId(), this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent<Group> event) {
        if (event.mEventType.equals(OperationType.DOWNLOAD_GROUP_INFO) && event.isSuccess) {
            mCurGroup = event.getModel();
            setupUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempListDownloaded(DownloadTempListEvent<Bulletin> event) {
        if (event.mEventType.equals(OperationType.DOWNLOAD_GROUP_ACTIVITY_LIST) && event.isSuccess)
            if (event.mDao.isFreshData())
                mAdapter.setData(event.mDao.getItems());
            else
                mAdapter.addData(event.mDao.getItems());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNewPostAdded(DownloadTempListEvent<Bulletin> event) {
        if (event.isSuccess) {
            mAdapter.addData(event.mDao.getItems());
            EventBus.getDefault().removeStickyEvent(event);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

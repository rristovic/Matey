package com.mateyinc.marko.matey.activity.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.model.Group;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GroupActivity extends MotherActivity {
    public static final String TAG = "GroupActivity";

    public static final String KEY_MODEL_POSITION = "group_position";

    private TextView tvGroupName, tvGroupDesc, tvGroupStats, tvGroupCrewNum;
    private ImageView ivGroupPic;
    private Button btnSail;

    private int mGroupIndex;
    private Group mCurGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        init();
        setupUI();
        downloadData();
    }

    private void init() {
        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvGroupDesc = (TextView) findViewById(R.id.tvGroupInfo);
        tvGroupStats = (TextView) findViewById(R.id.tvGroupStats);
        tvGroupCrewNum = (TextView) findViewById(R.id.tvGroupCrewNum);
        ivGroupPic = (ImageView) findViewById(R.id.ivGroupPic);
        btnSail = (Button) findViewById(R.id.btnGroupSail);

        if (getIntent().hasExtra(KEY_MODEL_POSITION)) {
            mGroupIndex = getIntent().getIntExtra(KEY_MODEL_POSITION, -1);
            mCurGroup = DataAccess.getInstance(this).getGroups().get(mGroupIndex);
        } else
            finish();
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
    }

    private void downloadData() {
        OperationManager.getInstance(this).downloadGroupInfo(this.mCurGroup, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent event) {
        setupUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

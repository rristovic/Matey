package com.mateyinc.marko.matey.inall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.facebook.login.LoginManager;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.util.Arrays;

abstract public class MotherActivity extends AppCompatActivity {

    public static long user_id;
    public static String device_id;

    protected Toolbar toolbar;

    /** Access token used to authorise with the server */
    public static String access_token;


    private final Object mLock = new Object();
    private SecurePreferences mSecurePreferences;

    /**
     * True if GCM_token and device_id are present on the the device thus
     * indicating if the device is registered on the server
     */
    public boolean mDeviceReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSecurePreferences == null) {
            mSecurePreferences = ((MyApplication) getApplication()).getSecurePreferences();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public SecurePreferences getSecurePreferences() {
        synchronized (mLock) {
            return mSecurePreferences;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {

        switch (id) {

            case 1000:
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Hey mate, there's a problem!")
                        .setMessage("Some error occurred.")
                        .setPositiveButton("Try Again",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        startTommy();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create();

            case 1002:
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("WOOHOO")
                        .setMessage("You are logged in with email: " + mSecurePreferences.getString("email") + "!")
                        .setPositiveButton("Log Out",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        LogoutAs logoutAs = new LogoutAs(MotherActivity.this);
                                        logoutAs.execute(mSecurePreferences.getString("email"),
                                                mSecurePreferences.getString("uid"),
                                                mSecurePreferences.getString("device_id"));
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create();

            case 1003:
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Hey " + bundle.getString("name") + ", you are already here!")
                        .setMessage("You have connected earlier with your facebook account. We recommend you to merge these two accounts!")
                        .setPositiveButton("Merge Accounts",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

//                                        FB_REQUEST = 1;
                                        LoginManager.getInstance().logInWithReadPermissions(MotherActivity.this, Arrays.asList("public_profile"));

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create();

            case 1004:
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Hey mate, there's a problem!")
                        .setMessage(bundle.getString("message"))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create();
        }

        return super.onCreateDialog(id);

    }

    protected void setSupportActionBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * For activities that uses childActionBar and that have back button
     */
    protected void setChildSupportActionBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}

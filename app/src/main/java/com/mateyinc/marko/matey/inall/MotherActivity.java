package com.mateyinc.marko.matey.inall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.facebook.login.LoginManager;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.internet.procedures.LogoutAs;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.util.Arrays;

abstract public class MotherActivity extends AppCompatActivity {


    protected ScepticTommy tommy;
    public SecurePreferences securePreferences;
    public boolean mServerReady = false;
    public boolean mDeviceReady = false;
    public boolean mLoggedIn = false;


    public int fbAnswerType = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setSecurePreferences(AppCompatActivity activity) {
        if (securePreferences == null)
            securePreferences = new SecurePreferences(activity, "credentials", "1checkMate1717", true);
    }

    public SecurePreferences getSecurePreferences() {
        return securePreferences;
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
                        .setMessage("You are logged in with email: " + securePreferences.getString("email") + "!")
                        .setPositiveButton("Log Out",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        LogoutAs logoutAs = new LogoutAs(MotherActivity.this);
                                        logoutAs.execute(securePreferences.getString("email"),
                                                securePreferences.getString("uid"),
                                                securePreferences.getString("device_id"));
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

                                        fbAnswerType = 1;
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

}

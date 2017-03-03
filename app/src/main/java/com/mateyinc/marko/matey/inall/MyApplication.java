package com.mateyinc.marko.matey.inall;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.internet.SessionManager;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyApplication extends Application {

    private SecurePreferences mSecurePreferences;
    private Object mLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (mLock) {
            if (mSecurePreferences == null)
                mSecurePreferences = new SecurePreferences(MyApplication.this, "credentials", "1checkMate1717", true);
        }

        MotherActivity.access_token = mSecurePreferences.getString(SessionManager.KEY_ACCESS_TOKEN);
        MotherActivity.device_id = mSecurePreferences.getString(SessionManager.KEY_DEVICE_ID);
        MotherActivity.user_id = PreferenceManager.getDefaultSharedPreferences(MyApplication.this).getLong(DataAccess.KEY_CUR_USER_ID, Long.MIN_VALUE);
    }

    public SecurePreferences getSecurePreferences() {
        synchronized (mLock) {
            return mSecurePreferences;
        }
    }

    public void printHash(Context context) {
        // Add code to print out the key hash
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.mateyinc.marko.matey",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (NullPointerException e) {
            Log.e("HASH", e.getLocalizedMessage());
        }
    }

}

package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;

import com.mateyinc.marko.matey.model.Bulletin;

import java.util.ArrayList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class BulletinManager {
    private final ArrayList<Bulletin> mData;
    private final Context mAppContext;
    private boolean mBulletinsLoaded;

    private static final Object mLock = new Object();
    private static BulletinManager mInstance;

    public static BulletinManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new BulletinManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }


    private BulletinManager(Context context) {
        mAppContext = context;
        mData = new ArrayList<>();
    }

    public void addBulletin(Bulletin b) {
        synchronized (mLock) {
            mData.add(b);
        }
    }

    public ArrayList<Bulletin> getBulletinList() {
        synchronized (mLock) {
            return mData;
        }
    }


    public Bulletin getBulletin(int index) {
        synchronized (mLock) {
            return mData.get(index);
        }
    }

    public void setBulletinsLoaded(boolean isLoaded){
        mBulletinsLoaded = isLoaded;
    }

    public boolean isBulletinsLoaded(){
        return mBulletinsLoaded;
    }
}

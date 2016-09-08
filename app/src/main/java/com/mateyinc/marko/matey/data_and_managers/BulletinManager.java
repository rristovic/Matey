package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;

import com.mateyinc.marko.matey.model.Bulletin;

import java.util.ArrayList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class BulletinManager {
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String BULLETIN_ITEM_UPDATED = "com.mateyinc.marko.matey.internet.home.bulletin_item_updated";
    public static final String EXTRA_ITEM_CHANGED_POS = "item_changed_position"; // extra for the bulletin_item_updated intent

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

    public Bulletin getBulletinByPostID(int id) {
        synchronized (mLock) {
            for (Bulletin b :
                    mData) {
                if (b != null && b.getPostID() == id) // Can be null because of bulletins first item
                    return b;
            }
        }
        return null;
    }

    public int getBulletinIndex(Bulletin b){
        synchronized (mLock){
            return mData.indexOf(b);
        }
    }

    public int getBulletinID(int index) {
        synchronized (mLock) {
            return mData.indexOf(getBulletin(index));
        }
    }

    public int getSize() {
        return mData.size();
    }

    public void setBulletinsLoaded(boolean isLoaded) {
        mBulletinsLoaded = isLoaded;
    }

    public boolean isBulletinsLoaded() {
        return mBulletinsLoaded;
    }


}

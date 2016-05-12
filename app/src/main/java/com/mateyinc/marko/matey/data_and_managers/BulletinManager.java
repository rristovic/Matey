package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;

import com.mateyinc.marko.matey.model.Bulletin;

import java.util.LinkedList;

/**
 * Created by Sarma on 5/12/2016.
 */
public class BulletinManager {
    private final LinkedList<Bulletin> mData;
    private final Context mAppContext;

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
        mData = new LinkedList<>();
    }

    public LinkedList<Bulletin> getBulletinList() {
        return mData;
    }

   public Bulletin getBulletin(int index){
       return mData.get(index);
   }
}

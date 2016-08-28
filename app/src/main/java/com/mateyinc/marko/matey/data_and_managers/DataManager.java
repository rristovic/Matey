package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;

import com.mateyinc.marko.matey.model.Message;
import com.mateyinc.marko.matey.model.Notification;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Sarma on 8/28/2016.
 */
public class DataManager {

    public final ArrayList<Notification> mNotificationList = new ArrayList<>();
    public final ArrayList<Message> mMessageList;
    public final ArrayList<UserProfile> mFriendsList;

    private final Context mAppContext;
    private static final Object mLock = new Object();
    private static DataManager mInstance;

    public static DataManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                mInstance.createDummyData();
            }
            return mInstance;
        }
    }

    private DataManager(Context context) {
        mAppContext = context;
        mFriendsList = new ArrayList<>();
        mMessageList = new ArrayList<>();
    }

    private void createDummyData(){
        Random r = new Random();
        for (int i = 0; i <50; i++){
            Notification n = new Notification("Proba "+i,new Date(new Date().getTime() - r.nextInt(86400000)));
            mNotificationList.add(n);
        }
    }

    // TODO - add data management system
}

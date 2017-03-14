package com.mateyinc.marko.matey.data;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Class used for generating temporary IDs for models
 */
public class IdGenerator {
    private static final String TAG = IdGenerator.class.getSimpleName();

    private long mLastGeneratedId = -1;
    private Object mLock = new Object();

    public IdGenerator(Context context) {
        init(context);
    }

    private void init(final Context context) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    Cursor c = context.getContentResolver().query(DataContract.NotUploadedEntry.CONTENT_URI, null, null, null,
                            DataContract.NotUploadedEntry._ID + " ASC");

                    if (c != null && c.moveToFirst())
                        mLastGeneratedId = c.getLong(c.getColumnIndex(DataContract.NotUploadedEntry._ID));
                    else
                        mLastGeneratedId = -1;

                    try {
                        c.close();
                    } catch (NullPointerException e) {
                    }
                }
            }
        });
        t.start();
    }

    /**
     * Method to call when new _ID is needed.
     *
     * @return newly generated unique id
     */
    public long generateId() {
        long newId;
        synchronized (mLock) {
            newId = --mLastGeneratedId;
        }
        Log.d(TAG, "Generated new id: " + newId);
        return newId;
    }
}

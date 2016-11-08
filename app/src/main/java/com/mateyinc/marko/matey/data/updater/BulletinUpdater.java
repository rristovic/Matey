package com.mateyinc.marko.matey.data.updater;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;

import java.util.Date;

import static com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_SUCCESS;

public class BulletinUpdater extends DataUpdater {
    private static final String TAG = BulletinUpdater.class.getSimpleName();

    public BulletinUpdater(int level){
        this.level = level;
    }

    @Override
    void updateServerStatus(final int serverStatus, final long id, final Context context) {
        // Add to not uploaded table if needed
        if(STATUS_RETRY_UPLOAD == serverStatus){
            BulletinUpdater.this.addNotUploadedActivity(id, BULLETIN_CLASS, context);
        }
        // Update status
        ContentValues values = new ContentValues();
        values.put(DataContract.BulletinEntry.COLUMN_SERVER_STATUS, serverStatus);
        int numOfUpdatedRows = context.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry._ID + " = ?", new String[]{Long.toString(id)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, String.format("Error updating bulletin (with ID=%d) server status to %s.", id, getServerStatusName(serverStatus)));
        } else {
            Log.d(TAG, String.format("Bulletin (with ID=%d) server status updated to %s.", id, getServerStatusName(serverStatus)));
        }
    }


    @Override
    void updateIdAndDate(final long oldId, final long newId, final Date date, final Context context) {
        ContentValues values = new ContentValues(2);
        values.put(BulletinEntry._ID, newId);
        values.put(BulletinEntry.COLUMN_SERVER_STATUS, STATUS_SUCCESS);
        values.put(BulletinEntry.COLUMN_DATE, date.getTime());

        int numOfRows = context.getContentResolver().update(DataContract.ReplyEntry.CONTENT_URI,
                values, BulletinEntry._ID + " = ?", new String[]{Long.toString(oldId)});

        if (numOfRows == 1) {
            Log.d(TAG, String.format("Bulletin (ID=%d) updated with new id=%d", oldId, newId));
        } else {
            Log.e(TAG, String.format("Failed to update the bulletin (ID=%d) with the new id=%d", oldId, newId));
        }
    }
}


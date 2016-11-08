package com.mateyinc.marko.matey.data.updater;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.Date;

import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_RETRY_UPLOAD;
import static com.mateyinc.marko.matey.data.DataManager.ServerStatus.STATUS_SUCCESS;

public class ReplyUpdater extends DataUpdater {
    private static final String TAG = DataUpdater.class.getSimpleName();

    public ReplyUpdater(int level){
        this.level = level;
    }

    @Override
    void updateServerStatus(int serverStatus, long id, Context context) {
        // Add to not uploaded table if needed
        if(STATUS_RETRY_UPLOAD == serverStatus){
            ReplyUpdater.this.addNotUploadedActivity(id, REPLY_CLASS, context);
//            Bulletin.decrementRepliesCount(id, context);
        }
        // Update status
        ContentValues values = new ContentValues();
        values.put(DataContract.ReplyEntry.COLUMN_SERVER_STATUS, serverStatus);

        int numOfUpdatedRows = context.getContentResolver().update(DataContract.ReplyEntry.CONTENT_URI, values,
                DataContract.ReplyEntry._ID + " = ?", new String[]{Long.toString(id)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, String.format("Error updating reply (with ID=%d) server status to %s.", id, getServerStatusName(serverStatus)));
        } else {
            Log.d(TAG, String.format("Reply (with ID=%d) server status updated to %s.", id, getServerStatusName(serverStatus)));
        }
    }

    @Override
    void updateIdAndDate(long oldId, long newId, Date date, Context context) {
        ContentValues values = new ContentValues(2);
        values.put(DataContract.ReplyEntry._ID, newId);
        values.put(DataContract.ReplyEntry.COLUMN_SERVER_STATUS, STATUS_SUCCESS);
        values.put(DataContract.ReplyEntry.COLUMN_DATE, date.getTime());

        int numOfRows = context.getContentResolver().update(DataContract.ReplyEntry.CONTENT_URI,
                values, DataContract.ReplyEntry._ID + " = ?", new String[]{Long.toString(oldId)});

        if (numOfRows == 1) {
            Log.d(TAG, String.format("Reply (ID=%d) updated with new id=%d", oldId, newId));
            // If success, update UI with replies count
            Bulletin.incrementRepliesCount(newId, context);
        } else {
            Log.e(TAG, String.format("Failed to update the reply (ID=%d) with the new id=%d", oldId, newId));
        }
    }
}

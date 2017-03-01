package com.mateyinc.marko.matey.data;

/**
 * Created by Sarma on 10/29/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;

import java.text.ParseException;

/**
 * AsyncTask class used for parsing downloaded data to the database
 */
public class JSONParserAs extends AsyncTask<String, Void, Void> {
    private static final String TAG = JSONParserAs.class.getSimpleName();
    Context mContext;

    public JSONParserAs(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);

        try {
            OperationManager.getInstance(mContext).parseBulletins(params[0]);
        } catch (JSONException | ParseException e) {
            Log.e(TAG, e.getLocalizedMessage(),e);
            Intent i = new Intent(OperationManager.BULLETIN_LIST_LOAD_FAILED);
            broadcastManager.sendBroadcast(i);
            return null;
        }

        Intent i = new Intent(OperationManager.BULLETIN_LIST_LOADED);
        broadcastManager.sendBroadcast(i);

        return null;
    }
}

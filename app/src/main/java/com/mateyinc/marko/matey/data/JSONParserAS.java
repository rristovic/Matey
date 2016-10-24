package com.mateyinc.marko.matey.data;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;

import java.text.ParseException;

/**
 * Created by Sarma on 10/23/2016.
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
            DataManager.getInstance(mContext).parseBulletins(params[0]);
        } catch (JSONException | ParseException e) {
            Log.e(TAG, e.getLocalizedMessage(),e);
            Intent i = new Intent(DataManager.BULLETIN_LIST_LOAD_FAILED);
            broadcastManager.sendBroadcast(i);
            return null;
        }

        Intent i = new Intent(DataManager.BULLETIN_LIST_LOADED);
        broadcastManager.sendBroadcast(i);

        return null;
    }
}

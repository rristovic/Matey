package com.mateyinc.marko.matey.inall;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by M4rk0 on 4/25/2016.
 */
abstract public class MotherAs extends AsyncTask<String,Void,String> {

    protected Context context;
    protected MotherActivity activity;

    public MotherAs (Context context) {

        this.context = context;

        if(context instanceof MotherActivity) {

            activity = (MotherActivity) context;

        } else cancel(true);

    }

}

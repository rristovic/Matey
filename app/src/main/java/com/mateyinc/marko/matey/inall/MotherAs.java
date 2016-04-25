package com.mateyinc.marko.matey.inall;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by M4rk0 on 4/25/2016.
 */
abstract public class MotherAs extends AsyncTask<String,Void,String> {

    protected Context context;
    protected MotherActivity activity;
    protected int DESIRED_LAYOUT, WAITING_LAYOUT, ERROR_LAYOUT;

    public MotherAs (Context context, int DESIRED_LAYOUT, int WAITING_LAYOUT, int ERROR_LAYOUT) {

        this.context = context;

        if(context instanceof MotherActivity) {

            activity = (MotherActivity) context;
            this.DESIRED_LAYOUT = DESIRED_LAYOUT;
            this.WAITING_LAYOUT = WAITING_LAYOUT;
            this.ERROR_LAYOUT = ERROR_LAYOUT;

        } else cancel(true);

    }

}

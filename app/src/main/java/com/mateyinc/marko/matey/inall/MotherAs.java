package com.mateyinc.marko.matey.inall;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by M4rk0 on 4/25/2016.
 */
abstract public class MotherAs extends AsyncTask<String,Void,String> {

    protected MotherActivity activity;

    public MotherAs (AppCompatActivity activity) {

        if(activity instanceof MotherActivity) {

            this.activity = (MotherActivity) activity;

        } else cancel(true);

    }

}

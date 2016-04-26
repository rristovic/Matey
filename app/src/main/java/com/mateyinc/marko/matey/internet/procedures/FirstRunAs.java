package com.mateyinc.marko.matey.internet.procedures;

import android.content.Context;

import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.inall.MotherAs;
import com.mateyinc.marko.matey.inall.ScepticTommy;
import com.mateyinc.marko.matey.internet.http.HTTP;

/**
 * Created by M4rk0 on 3/10/2016.
 */
public class FirstRunAs extends MotherAs {

    public FirstRunAs(Context context) {
        super(context);
    }

    @Override
    protected void onPreExecute() {

        if(!isCancelled()) activity.setContentView(ScepticTommy.WAITING_LAYOUT);

    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {
            // get data from server
            // return null if something went wrong
            try {

                HTTP http = new HTTP (UrlData.FIRST_RUN_URL, "GET");

                return http.getData();

            } catch (Exception e) {

                return null;

            }

        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

}

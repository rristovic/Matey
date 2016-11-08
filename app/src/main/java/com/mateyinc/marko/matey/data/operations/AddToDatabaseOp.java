package com.mateyinc.marko.matey.data.operations;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.mateyinc.marko.matey.model.MModel;

public class AddToDatabaseOp implements Operation{
    private MModel model;

    public AddToDatabaseOp(MModel model){
        this.model = model;
    }

    @Override
    public void execute(final Context context, RequestQueue queue, String token) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                model.addToDb(context);
            }
        });
        t.start();
    }
}

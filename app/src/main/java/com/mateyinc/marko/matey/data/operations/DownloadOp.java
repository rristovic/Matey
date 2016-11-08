package com.mateyinc.marko.matey.data.operations;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.mateyinc.marko.matey.model.MModel;

public class DownloadOp implements Operation {

    private MModel model;

    public DownloadOp(MModel model){
        this.model = model;
    }

    @Override
    public void execute(Context context, RequestQueue queue, String token) {
        model.download(context, queue, token);
    }
}

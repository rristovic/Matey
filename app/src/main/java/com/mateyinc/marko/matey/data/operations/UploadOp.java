package com.mateyinc.marko.matey.data.operations;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.mateyinc.marko.matey.model.MModel;

public class UploadOp implements Operation {

    private MModel model;

    public UploadOp(MModel model){
        this.model = model;
    }

    @Override
    public void execute(Context context, RequestQueue queue, String accessToken){
        model.upload(context, queue, accessToken);
    }
}

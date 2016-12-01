package com.mateyinc.marko.matey.data.operations;

import android.content.Context;

import com.android.volley.RequestQueue;

public interface Operation {
    void execute(Context context, RequestQueue queue, String token);
}

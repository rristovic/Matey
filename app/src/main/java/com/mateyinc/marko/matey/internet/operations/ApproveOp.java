package com.mateyinc.marko.matey.internet.operations;


import android.content.Context;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.model.Approve;

public class ApproveOp extends Operations {

    Approve approve;

    public ApproveOp(Context context, Approve approve) {
        super(context);
        this.approve = approve;
    }

    @Override
    public void startDownloadAction() {

    }

    @Override
    protected void onDownloadSuccess(String response) {
        Context c = mContextRef.get();

        if (c != null) {
            approve.onDownloadSuccess(response, c);
        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {
        Context c = mContextRef.get();

        if (c != null) {
            approve.onDownloadFailed(new String(error.networkResponse.data), c);
        }
    }

    @Override
    public void startUploadAction() {

    }

    @Override
    protected void onUploadSuccess(String response) {
        Context c = mContextRef.get();

        if (c != null) {
            approve.onUploadSuccess(response, c);
        }
    }

    @Override
    protected void onUploadFailed(VolleyError error) {
        Context c = mContextRef.get();

        if (c != null) {
            approve.onUploadFailed(new String(error.networkResponse.data), c);
        }
    }

    @Override
    protected String getTag() {
        return null;
    }
}

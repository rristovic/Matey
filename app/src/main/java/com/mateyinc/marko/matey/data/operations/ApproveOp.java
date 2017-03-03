package com.mateyinc.marko.matey.data.operations;


import android.content.Context;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.model.Approve;

public class ApproveOp extends Operations {

    Approve approve;

    public ApproveOp(Context context, Approve approve) {
        super(context);
        this.approve = approve;
    }

    public ApproveOp(Context context, OperationType operationType) {
        super(context, operationType);
    }

    public ApproveOp(OperationProvider provider, Context context, OperationType operationType) {
        super(provider, context, operationType);
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
            approve.onDownloadFailed(error, c);
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
            approve.onUploadFailed(error, c);
        }
    }

    @Override
    protected String getTag() {
        return null;
    }
}

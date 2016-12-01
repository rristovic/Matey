package com.mateyinc.marko.matey.data.operations;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.inall.MotherActivity;

public class BulletinOp extends Operations {

    BulletinOp(OperationProvider provider, MotherActivity context) {
        super(provider, context);
    }

    @Override
    public void download(long id) {
    }

    @Override
    protected void onDownloadSuccess(String response) {

    }

    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public <T> void upload(T object) {

    }

    @Override
    protected <T> void onUploadSuccess(T object) {

    }

    @Override
    protected <T> void onUploadFailed(T object) {

    }

    @Override
    protected String getTag() {
        return null;
    }
}

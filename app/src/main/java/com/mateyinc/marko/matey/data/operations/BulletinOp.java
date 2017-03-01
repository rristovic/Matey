package com.mateyinc.marko.matey.data.operations;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.inall.MotherActivity;

public class BulletinOp extends Operations {

    BulletinOp(OperationProvider provider, MotherActivity context) {
        super(provider, context, OperationType.BULLETIN_OP);
    }


    @Override
    public void startDownloadAction() {

    }

    @Override
    protected void onDownloadSuccess(String response) {

    }

    @Override
    protected void onDownloadFailed(VolleyError error) {

    }

    @Override
    public void startUploadAction() {

    }

    @Override
    protected String getTag() {
        return null;
    }
}

package com.mateyinc.marko.matey.internet.operations;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Bulletin;

public class ApproveOp extends Operations {

    final Bulletin b;

    public ApproveOp(Context context, Bulletin b) {
        super(context);
        this.b = b;
    }

    @Override
    public void startDownloadAction() {

    }

    @Override
    protected void onDownloadSuccess(String response) {
        Context c = mContextRef.get();
//
//        if (c != null) {
//            approve.onDownloadSuccess(response, c);
//        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {
        Context c = mContextRef.get();

//        if (c != null) {
//            approve.onDownloadFailed(new String(error.networkResponse.data), c);
//        }
    }

    @Override
    public void startUploadAction() {
        String url;
        int method;

        switch (mOpType) {
            case POST_LIKED: {
                url = UrlData.buildBulletinBoostUrl(b.getId());
                method = Request.Method.PUT;
                break;
            }
            case POST_UNLIKED: {
                url = UrlData.buildBulletinBoostUrl(b.getId());
                method = Request.Method.DELETE;
                break;
            }
            default:
                url = "#";
                method = Request.Method.GET;
        }

        createNewUploadReq(url, method);
        startUpload();
    }

    @Override
    protected void onUploadSuccess(String response) {
        Context c = mContextRef.get();
    }

    @Override
    protected void onUploadFailed(VolleyError error) {
        Context c = mContextRef.get();

        b.boost(); // If failed, revert back to pass state
    }

    @Override
    protected String getTag() {
        return ApproveOp.class.getSimpleName();
    }
}

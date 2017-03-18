package com.mateyinc.marko.matey.internet.operations;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;

public class ApproveOp extends Operations {

    Bulletin b;
    Reply r;

    public ApproveOp(Context context, Bulletin b) {
        super(context);
        this.b = b;
    }

    public ApproveOp(Context context, Reply r) {
        super(context);
        this.r = r;
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
            case REPLY_LIKED: {
                if (this.r.isPostReply())
                    url = UrlData.buildReplyLikeUrl(r.getId());
                else
                    url = UrlData.buildReReplyLikeUrl(this.r.getReReplyId());
                method = Request.Method.PUT;
                break;
            }
            case REPLY_UNLIKED: {
                if (this.r.isPostReply())
                    url = UrlData.buildReplyLikeUrl(r.getId());
                else
                    url = UrlData.buildReReplyLikeUrl(this.r.getReReplyId());

                method = Request.Method.DELETE;
                break;
            }
            case RE_REPLY_LIKED: {
                url = UrlData.buildReReplyLikeUrl(r.getReReplyId());
                method = Request.Method.PUT;
                break;
            }
            case RE_REPLY_UNLIKED: {
                url = UrlData.buildReReplyLikeUrl(r.getReReplyId());
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

        // If failed, revert back to pass state
        switch (mOpType) {
            case POST_LIKED:
            case POST_UNLIKED:
                b.boost();
                break;
            case REPLY_LIKED:
            case REPLY_UNLIKED:
            case RE_REPLY_LIKED:
            case RE_REPLY_UNLIKED:
                r.like();
                break;
            default:
                break;
        }
    }

    @Override
    protected String getTag() {
        return ApproveOp.class.getSimpleName();
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
}

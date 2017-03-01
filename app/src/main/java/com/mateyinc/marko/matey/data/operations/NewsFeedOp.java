package com.mateyinc.marko.matey.data.operations;

import android.net.Uri;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.data.OperationProvider;
import com.mateyinc.marko.matey.inall.MotherActivity;

import static com.mateyinc.marko.matey.data.internet.UrlData.GET_NEWSFEED_ROUTE;
import static com.mateyinc.marko.matey.data.internet.UrlData.PARAM_COUNT;
import static com.mateyinc.marko.matey.data.internet.UrlData.PARAM_START_POS;
import static com.mateyinc.marko.matey.data.operations.OperationType.DOWNLOAD_NEWS_FEED;


public class NewsfeedOp extends Operations{

    private static final String TAG = NewsfeedOp.class.getSimpleName();

    private int mCount, mStartPos;

    public NewsfeedOp(MotherActivity context) {
        super(context, DOWNLOAD_NEWS_FEED);
    }

    public NewsfeedOp(OperationProvider provider, MotherActivity context) {
        super(provider, context, DOWNLOAD_NEWS_FEED);
    }

    @Override
    public void startDownloadAction() {
        String url;

        switch (mOpType) {
            case DOWNLOAD_NEWS_FEED: {
                Log.d(TAG, "Downloading news feed. Start position=".concat(Integer.toString(mStartPos))
                  .concat("; Count=").concat(Integer.toString(mCount)));

                Uri.Builder builder = Uri.parse(GET_NEWSFEED_ROUTE).buildUpon();
                builder.appendQueryParameter(PARAM_START_POS, Integer.toString(mStartPos))
                        .appendQueryParameter(PARAM_COUNT, Integer.toString(mCount));
                url = builder.build().toString();
                break;
            }
            default:
                url = "";
        }

        createNewDownloadReq(url);
        startDownload();
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

    public NewsfeedOp setCount(int count){
        this.mCount = count;
        return this;
    }
    public NewsfeedOp setStartPos(int startPos){
        this.mStartPos = startPos;
        return this;
    }
}

package com.mateyinc.marko.matey.internet;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sarma on 10/20/2016.
 */

public class MateyRequest extends StringRequest {
    private static final String TAG = MateyRequest.class.getSimpleName();

    private Map<String, String> mParams = new LinkedHashMap<>();
    private Map<String, String> authHeader = new LinkedHashMap<>();

    public MateyRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public void addParam(String key, String value) {
        mParams.put(key, value);
    }

    public void setAuthHeader(String key, String value){
        authHeader.put(key,value);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return authHeader;
    }
}

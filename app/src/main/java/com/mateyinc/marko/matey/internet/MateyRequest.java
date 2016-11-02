package com.mateyinc.marko.matey.internet;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.mateyinc.marko.matey.internet.UrlData.PARAM_AUTH_TYPE;

public class MateyRequest extends StringRequest {
    protected static final String TAG = MateyRequest.class.getSimpleName();

    private Map<String, String> mParams;
    private Map<String, String> authHeader = new LinkedHashMap<>();
    private String mBody;

    public MateyRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public void addParam(String key, String value) {
        if (mParams == null)
            mParams = new LinkedHashMap<>();
        mParams.put(key, value);
    }

    public void setAuthHeader(String key, String value) {
        authHeader.put(key, value);
    }

    public void setBody(String body){
        mBody = body;
    }

    public void setBodyFromFollowedProfiles(ArrayList<UserProfile> list){
        JSONArray jsonArray  = new JSONArray();

        try {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                JSONObject object = new JSONObject();
                object.put(UrlData.PARAM_FOLLOWED_USER_ID, ((UserProfile) i.next()).getUserId());
                jsonArray.put(object);
            }
        } catch (JSONException e){
            Log.e(TAG, e.getLocalizedMessage(), e);
            mBody = null;
        }

        mBody = jsonArray.toString();
    }

    public void setAuthHeader(String value) {
        authHeader.put(PARAM_AUTH_TYPE, String.format("Bearer %s", value));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return authHeader;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mBody == null ? super.getBody() : mBody.getBytes();
    }
}

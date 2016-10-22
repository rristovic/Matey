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

    // Fields downloaded from OAuth2 Server
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    /**
     * SharedPref name for data that indicates when is the ACCESS_TOKEN saved in db
     */
    public static final String TOKEN_SAVED_TIME = "tst";

    // Fields downloaded from Resource Server
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_VERIFIED = "verified";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_PROFILE_PICTURE = "profile_picture";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";

    private Map<String, String> mParams = new LinkedHashMap<>();

    public MateyRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public void addParam(String key, String value) {
        mParams.put(key, value);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }
}

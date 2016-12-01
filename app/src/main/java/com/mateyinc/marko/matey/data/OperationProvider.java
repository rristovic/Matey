package com.mateyinc.marko.matey.data;

import com.android.volley.Request;

/**
 * Created by Sarma on 11/30/2016.
 */

/**
 * Interface for operations to provide {@link com.android.volley.RequestQueue}
 * and {@link java.util.concurrent.ExecutorService} to their action on;
 */
public interface OperationProvider {
    void submitRequest(Request request);
    void submitRunnable(Runnable runnable);
    String getAccessToken();
}

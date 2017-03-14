package com.mateyinc.marko.matey.internet;

import com.android.volley.Request;


/**
 * Interface for operations to provide {@link com.android.volley.RequestQueue}
 * and {@link java.util.concurrent.ExecutorService} to their action on;
 */
public interface OperationProvider {
    void submitRequest(Request request);
    void submitRunnable(Runnable runnable);
    String getAccessToken();
    long generateId();
}

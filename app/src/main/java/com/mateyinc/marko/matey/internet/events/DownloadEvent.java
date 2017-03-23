package com.mateyinc.marko.matey.internet.events;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class DownloadEvent {
    public DownloadEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }public DownloadEvent(boolean isSuccess, String modelType) {
        this.isSuccess = isSuccess;
        this.modelType = modelType;
    }

    public String modelType = "";

    boolean isSuccess;
}


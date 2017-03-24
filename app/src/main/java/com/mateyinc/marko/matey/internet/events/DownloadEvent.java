package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class DownloadEvent {
    public DownloadEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }public DownloadEvent(boolean isSuccess, OperationType operationType) {
        this.isSuccess = isSuccess;
        this.operationType = operationType;
    }

    public OperationType operationType = OperationType.NO_OPERATION;

    boolean isSuccess;
}


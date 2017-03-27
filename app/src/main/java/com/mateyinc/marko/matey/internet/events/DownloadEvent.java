package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class DownloadEvent extends Event {

    public DownloadEvent(boolean isSuccess) {
        super(isSuccess, OperationType.NO_OPERATION);
    }

    public DownloadEvent(boolean isSuccess, OperationType operationType) {
        super(isSuccess, operationType);
    }

}


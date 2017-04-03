package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.MModel;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class DownloadEvent<T extends MModel> extends Event {

    private T mModel;

    public DownloadEvent(boolean isSuccess) {
        super(isSuccess, OperationType.NO_OPERATION);
    }

    public DownloadEvent(boolean isSuccess, OperationType operationType) {
        super(isSuccess, operationType);
    }

    public DownloadEvent(boolean isSuccess, T model, OperationType operationType) {
        super(isSuccess, operationType);
        this.mModel = model;
    }

    public T getModel(){
        return mModel;
    }
}


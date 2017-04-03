package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.MModel;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class UploadEvent<T extends MModel> extends Event {

    private T mModel;

    public UploadEvent(boolean isSuccess) {
        super(isSuccess, OperationType.NO_OPERATION);
    }

    public UploadEvent(boolean isSuccess, OperationType operationType) {
        super(isSuccess, operationType);
    }

    public UploadEvent(boolean isSuccess, T model, OperationType operationType) {
        super(isSuccess, operationType);
        this.mModel = model;
    }

    public T getModel(){
        return mModel;
    }
}


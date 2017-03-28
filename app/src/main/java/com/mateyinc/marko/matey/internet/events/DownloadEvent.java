package com.mateyinc.marko.matey.internet.events;

import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.model.MModel;

/**
 * Static class used for event bus. Event that indicates that data has been downloaded or failed to download.
 */
public class DownloadEvent extends Event {

    private MModel mModel;

    public DownloadEvent(boolean isSuccess) {
        super(isSuccess, OperationType.NO_OPERATION);
    }

    public DownloadEvent(boolean isSuccess, OperationType operationType) {
        super(isSuccess, operationType);
    }

    public DownloadEvent(boolean isSuccess, MModel model, OperationType operationType) {
        super(isSuccess, operationType);
        this.mModel = model;
    }

    public MModel getModel(){
        return mModel;
    }
}


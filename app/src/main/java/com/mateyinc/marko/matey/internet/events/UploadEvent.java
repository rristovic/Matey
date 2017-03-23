package com.mateyinc.marko.matey.internet.events;



/**
 * Static class used for event bus. Event that indicates that upload has been success of failed.
 */
public class UploadEvent {
    public UploadEvent(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
    public UploadEvent(boolean isSuccess, String modelType){
        this.isSuccess = isSuccess;
        this.modelType = modelType;
    }

    public String modelType = "";
    boolean isSuccess;
}
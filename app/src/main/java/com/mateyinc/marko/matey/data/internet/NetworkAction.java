package com.mateyinc.marko.matey.data.internet;


import java.util.LinkedList;

/**
 * Network operation/action that will be performed with UserProfile data; U is startUploadAction action, D is startDownloadAction action that can be performed
 */
public abstract class NetworkAction<U, D> {

    public long userId;

    /**
     * Method for returning startUploadAction action contained in this class
     * @return the startUploadAction action
     */
    protected abstract U getUploadAction();

    /**
     * Method for returning startDownloadAction action contained in this class
     * @return the startDownloadAction action
     */
    protected abstract D getDownloadAction();

    protected abstract void setDownloadAction(D action);
    protected abstract void setUploadAction(U action);
}

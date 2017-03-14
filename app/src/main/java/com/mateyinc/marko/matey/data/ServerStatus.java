package com.mateyinc.marko.matey.data;


/**
 * Model server status that is saved in database, and used for UI control
 */
public enum ServerStatus {
    /**
     * Indicates that data upload to the server has failed
     */
    STATUS_RETRY_UPLOAD,
    /**
     * Indicates that the data is currently being uploaded to the server
     */
    STATUS_UPLOADING,
    /**
     * Indicates that data is successfully uploaded to the server
     */
    STATUS_SUCCESS;

    public static ServerStatus fromInteger(int i) {
        switch(i) {
            case 0:
                return STATUS_RETRY_UPLOAD;
            case 1:
                return STATUS_UPLOADING;
            case 2:
                return STATUS_SUCCESS;
            default:
                return STATUS_UPLOADING;
        }
    }
}

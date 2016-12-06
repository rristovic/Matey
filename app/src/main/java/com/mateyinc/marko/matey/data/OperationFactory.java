package com.mateyinc.marko.matey.data;

import com.mateyinc.marko.matey.data.operations.Operations;
import com.mateyinc.marko.matey.data.operations.UserProfileOp;
import com.mateyinc.marko.matey.inall.MotherActivity;

/**
 * Factory class for creating {@link Operations} which is used for data startDownloadAction/startUploadAction control and saving to the database;
 */
public class OperationFactory {

    /**
     * Operation type that can be created with {@link OperationFactory};
     */
    public enum OperationType{
        BULLETIN_OP, USER_PROFILE_OP
    }

    /** Used for db control **/
    private final MotherActivity mContext;
    /** @see com.mateyinc.marko.matey.data.OperationProvider **/
    private final OperationProvider mProvider;

    /**
     * Private constructor initialising {@link #mProvider} and {@link #mContext}
     * @param context {@link android.content.Context} of the calling activity
     */
    private OperationFactory(MotherActivity context){
        mContext = context;
        mProvider = DataManager.getInstance(context);
    }

    /**
     * Creates an instance of this class used for creating new operations
     * @param mContext {@link android.content.Context} of the calling activity used for db control
     * @return newly created instance of this class
     */
    public static OperationFactory getInstance(MotherActivity mContext){
        return new OperationFactory(mContext);
    }

    /**
     * Create the desired type of {@link Operations}
     * @param opType type of the operations. Can be a value from {@link OperationType}
     * @return newly created instance of operation class;
     */
    public Operations getOperation(OperationType opType){

        if(opType == OperationType.USER_PROFILE_OP){
            return new UserProfileOp(mProvider, mContext, opType);
        }

        return null;
    }
}

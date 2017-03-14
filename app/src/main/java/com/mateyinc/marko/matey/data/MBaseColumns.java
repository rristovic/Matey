package com.mateyinc.marko.matey.data;

import android.provider.BaseColumns;

/**
 * Created by Sarma on 10/17/2016.
 */

public interface MBaseColumns extends BaseColumns {

    /**
     * Indicates if current row is uploaded to server
     */
     String COLUMN_SERVER_STATUS = "is_on_server";
}

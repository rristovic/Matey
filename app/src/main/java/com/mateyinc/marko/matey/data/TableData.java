package com.mateyinc.marko.matey.data;

import android.provider.BaseColumns;

/**
 * Created by M4rk0 on 3/7/2016.
 */
public class TableData {

    public TableData () {}

    public static abstract class TableInfo implements BaseColumns {

        public static final String DEVICE_ID = "installation_id";
        public static final String DATABASE_NAME = "matey_db";
        public static final String TABLE_NAME = "installation_info";

    }

}

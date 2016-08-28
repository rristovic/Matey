/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mateyinc.marko.matey.data_and_managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mateyinc.marko.matey.data_and_managers.DataContract.*;

/**
 * Manages a local database for data.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "matey.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // TODO - Update sql command after creating more columns in DataCotntract

        final String SQL_CREATE_MSG_TABLE = "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MessageEntry.COLUMN_MSG_BODY + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_NOTIF_TABLE = "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MSG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_NOTIF_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

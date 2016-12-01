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
package com.mateyinc.marko.matey.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mateyinc.marko.matey.data.DataContract.ApproveEntry;
import com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.data.DataContract.MessageEntry;
import com.mateyinc.marko.matey.data.DataContract.NotUploadedEntry;
import com.mateyinc.marko.matey.data.DataContract.NotificationEntry;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.DataContract.ReplyEntry;

/**
 * Manages a local database for data.
 */
class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "matey.db";

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MSG_TABLE = "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_SENDER_ID + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_SENDER_NAME + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MSG_BODY + " TEXT, " +
                MessageEntry.COLUMN_MSG_TIME + " TEXT, " +
                MessageEntry.COLUMN_IS_READ + " BIT NOT NULL, " +

                // Set up the sender_id column as a foreign key to profile table.
                "FOREIGN KEY (" + MessageEntry.COLUMN_SENDER_ID + ") REFERENCES " +
                ProfileEntry.TABLE_NAME + " (" + ProfileEntry._ID +
                ") );";


        final String SQL_CREATE_NOTIF_TABLE = "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NotificationEntry.COLUMN_SENDER_NAME + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_SENDER_ID + " INTEGER NOT NULL, " +
                NotificationEntry.COLUMN_NOTIF_TEXT + " TEXT, " +
                NotificationEntry.COLUMN_NOTIF_TIME + " TEXT, " +
                NotificationEntry.COLUMN_NOTIF_LINK_ID + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + ProfileEntry.TABLE_NAME + " (" +
                ProfileEntry._ID + " INTEGER NOT NULL, " +
                ProfileEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_PROF_PIC + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_COVER_PIC + " TEXT NOT NULL, " +
                ProfileEntry.COLUMN_FOLLOWERS_NUM + " INTEGER DEFAULT 0, " +
                ProfileEntry.COLUMN_FOLLOWING_NUM + " INTEGER DEFAULT 0, " +
                ProfileEntry.COLUMN_IS_FRIEND + " BOOLEAN DEFAULT FALSE, " +
                ProfileEntry.COLUMN_LAST_MSG_ID + " INTEGER, " +
                BulletinEntry.COLUMN_SERVER_STATUS + " INTEGER DEFAULT 0 " +
                ");";

        final String SQL_CREATE_BULLETIN_TABLE = "CREATE TABLE " + BulletinEntry.TABLE_NAME + " (" +
                BulletinEntry._ID + " INTEGER NOT NULL, " +
                BulletinEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                BulletinEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_DATE + " INT, " +
                BulletinEntry.COLUMN_NUM_OF_REPLIES + " INTEGER DEFAULT 0, " +
                BulletinEntry.COLUMN_SERVER_STATUS + " INTEGER DEFAULT 0, " +
                BulletinEntry.COLUMN_ATTACHMENTS + " TEXT, " +
                // Set up the sender_id column as a foreign key to profile table.
                "FOREIGN KEY (" + BulletinEntry.COLUMN_USER_ID + ") REFERENCES " +
                ProfileEntry.TABLE_NAME + " (" + ProfileEntry._ID + "), " +
                " UNIQUE (" + BulletinEntry._ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REPLY_TABLE = "CREATE TABLE " + ReplyEntry.TABLE_NAME + " (" +
                ReplyEntry._ID + " INTEGER NOT NULL, " +
                ReplyEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                ReplyEntry.COLUMN_POST_ID + " INTEGER NOT NULL, " +
                ReplyEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                ReplyEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                ReplyEntry.COLUMN_DATE + " INT NOT NULL, " +
                ReplyEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_SERVER_STATUS + " INTEGER DEFAULT 0, " +
                ReplyEntry.COLUMN_NUM_OF_APPRVS + " INTEGER DEFAULT 0," +
                "FOREIGN KEY (" + ReplyEntry.COLUMN_USER_ID + ") REFERENCES " +
                ProfileEntry.TABLE_NAME + " (" + ProfileEntry._ID + "), " +
                "FOREIGN KEY (" + ReplyEntry.COLUMN_POST_ID + ") REFERENCES " +
                BulletinEntry.TABLE_NAME + " (" + BulletinEntry._ID + "));";

        final String SQL_CREATE_APPROVES_TABLE = "CREATE TABLE " + ApproveEntry.TABLE_NAME + " (" +
                ApproveEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ApproveEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                ApproveEntry.COLUMN_POST_ID + " INTEGER NOT NULL, " +
                ApproveEntry.COLUMN_REPLY_ID + " INTEGER NOT NULL, " +
                BulletinEntry.COLUMN_SERVER_STATUS + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY (" + ApproveEntry.COLUMN_USER_ID + ") REFERENCES " +
                ProfileEntry.TABLE_NAME + " (" + ProfileEntry._ID + "), " +
                "FOREIGN KEY (" + ApproveEntry.COLUMN_REPLY_ID + ") REFERENCES " +
                ReplyEntry.TABLE_NAME + " (" + ReplyEntry._ID + "), " +
                "FOREIGN KEY (" + ApproveEntry.COLUMN_POST_ID + ") REFERENCES " +
                BulletinEntry.TABLE_NAME + " (" + BulletinEntry._ID + "));";

        final String SQL_CREATE_NOT_UPLOADED_TABLE = "CREATE TABLE " + NotUploadedEntry.TABLE_NAME + " (" +
                NotUploadedEntry._ID + " INTEGER NOT NULL, " +
                NotUploadedEntry.COLUMN_ENTRY_TYPE + " INTEGER NOT NULL );";

        sqLiteDatabase.execSQL(SQL_CREATE_MSG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROFILE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_NOTIF_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BULLETIN_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REPLY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_NOT_UPLOADED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_APPROVES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BulletinEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReplyEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotUploadedEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ApproveEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProfileEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BulletinEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReplyEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ApproveEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotUploadedEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class DataProvider extends ContentProvider {

    // The URI Matcher used by this content mProvider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private DbHelper mOpenHelper;

    static final int MESSAGES = 100;
    static final int MESSAGE_WITH_ID = 101;
    static final int NOTIFICATIONS = 200;
    static final int NOTIFICATION_WITH_ID = 201;
    static final int PROFILES = 300;
    static final int PROFILE_WITH_ID = 301;
    static final int BULLETINS = 400;
    static final int BULLETIN_WITH_ID = 401;
    static final int BULLETIN_WITH_REPLIES_COUNT = 402;
    static final int BULLETIN_REPLIES = 500;
    static final int REPLY_REPLIES = 501;
    static final int APPROVES = 600;
    static final int APPROVE_WITH_ID = 601;
    static final int NOT_UPLOADED_ITEMS = 700;

    private static final SQLiteQueryBuilder sRepliesWithBulletin;
    private static final SQLiteQueryBuilder sBulletinReplies;

    static{
        sRepliesWithBulletin = new SQLiteQueryBuilder();
        sBulletinReplies = new SQLiteQueryBuilder();

        sRepliesWithBulletin.setTables(
                DataContract.BulletinEntry.TABLE_NAME + " LEFT OUTER JOIN " + DataContract.ReplyEntry.TABLE_NAME +
                        " ON " + DataContract.BulletinEntry.TABLE_NAME + "." + DataContract.BulletinEntry._ID +
                        " = " + DataContract.ReplyEntry.TABLE_NAME + "." + DataContract.ReplyEntry.COLUMN_POST_ID);

    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Using the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MESSAGES:
                return DataContract.MessageEntry.CONTENT_TYPE;
            case MESSAGE_WITH_ID:
                return DataContract.MessageEntry.CONTENT_ITEM_TYPE;
            case NOTIFICATIONS:
                return DataContract.NotificationEntry.CONTENT_TYPE;
            case NOTIFICATION_WITH_ID:
                return DataContract.NotificationEntry.CONTENT_ITEM_TYPE;
            case PROFILES:
                return DataContract.ProfileEntry.CONTENT_TYPE;
            case PROFILE_WITH_ID:
                return DataContract.ProfileEntry.CONTENT_ITEM_TYPE;
            case BULLETINS:
                return DataContract.BulletinEntry.CONTENT_TYPE;
            case BULLETIN_WITH_ID:
                return DataContract.BulletinEntry.CONTENT_ITEM_TYPE;
            case BULLETIN_WITH_REPLIES_COUNT:
                return DataContract.BulletinEntry.CONTENT_TYPE;
            case BULLETIN_REPLIES:
                return DataContract.ReplyEntry.CONTENT_TYPE;
            case REPLY_REPLIES:
                return DataContract.ReplyEntry.CONTENT_TYPE;
            case APPROVES:
                return DataContract.ApproveEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "messages"
            case MESSAGES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.MessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "messages/*"
            case MESSAGE_WITH_ID: {
                retCursor = getMessageByID(uri, projection, sortOrder);
                break;
            }
            // "notifications"
            case NOTIFICATIONS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.NotificationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "notifications/*"
            case NOTIFICATION_WITH_ID: {
                retCursor = getNotificationByID(uri, projection, sortOrder);
                break;
            }
            // "profile"
            case PROFILES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ProfileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "profile/*"
            case PROFILE_WITH_ID: {
                retCursor = getProfileByID(uri, projection, sortOrder);
                break;
            } // "bulletins"
            case BULLETINS: {
//                retCursor = sRepliesWithBulletin.query(mOpenHelper.getReadableDatabase(),
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
//                retCursor =  mOpenHelper.getReadableDatabase().rawQuery(
//                        "SELECT bulletins.*,(SELECT COUNT(post_id) FROM replies WHERE replies.post_id = bulletins._id GROUP BY replies.post_id) AS kurac from bulletins"
//                        ,null,null);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.BulletinEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "bulletins/*"
            case BULLETIN_WITH_ID: {
//                retCursor = getBulletinByID(uri, projection, sortOrder);
                retCursor = sRepliesWithBulletin.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "replies"
            case BULLETIN_REPLIES: {
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        DataContract.ReplyEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
                retCursor = sRepliesWithBulletin.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        DataContract.ReplyEntry.TABLE_NAME.concat(".").concat(DataContract.ReplyEntry._ID),
                        null,
                        sortOrder);
                break;
            }
            case REPLY_REPLIES: {
                retCursor = getReplyByID(uri, projection, sortOrder);
                break;
            }
            case APPROVES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ApproveEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "not_uploaded_items"
            case NOT_UPLOADED_ITEMS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.NotUploadedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getReplyByID(Uri uri, String[] projection, String sortOrder) {
        return null;
    }

    private Cursor getProfileByID(Uri uri, String[] projection, String sortOrder) {
        // TODO - finish function by ID
        return null;
    }

    private Cursor getNotificationByID(Uri uri, String[] projection, String sortOrder) {
        // TODO - finish function by ID
        return null;
    }

    private Cursor getMessageByID(Uri uri, String[] projection, String sortOrder) {
        // TODO - finish function by ID
        return null;
    }

    private Cursor getBulletinByID(Uri uri, String[] projection, String sortOrder) {
        // TODO - finish function by ID
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MESSAGES: {
                long _id = db.insert(DataContract.MessageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.MessageEntry.buildMessageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case NOTIFICATIONS: {
                long _id = db.insert(DataContract.NotificationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.NotificationEntry.buildNotifUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PROFILES: {
                long _id = db.insert(DataContract.ProfileEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.ProfileEntry.buildProfileUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case BULLETINS: {
                long _id = db.insert(DataContract.BulletinEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.BulletinEntry.buildBulletinUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case BULLETIN_REPLIES: {
                long _id = db.insert(DataContract.ReplyEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.ReplyEntry.buildReplyUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case APPROVES: {
                long _id = db.insert(DataContract.ApproveEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.ApproveEntry.buildApproveUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case NOT_UPLOADED_ITEMS: {
                long _id = db.insert(DataContract.NotUploadedEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.NotUploadedEntry.buildNotUploadedUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MESSAGES:
                rowsDeleted = db.delete(
                        DataContract.MessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTIFICATIONS:
                rowsDeleted = db.delete(
                        DataContract.NotificationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILES:
                rowsDeleted = db.delete(
                        DataContract.ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BULLETINS:
                rowsDeleted = db.delete(
                        DataContract.BulletinEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BULLETIN_REPLIES:
                rowsDeleted = db.delete(
                        DataContract.ReplyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case APPROVES:
                rowsDeleted = db.delete(
                        DataContract.ApproveEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOT_UPLOADED_ITEMS:
                rowsDeleted = db.delete(
                        DataContract.NotUploadedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MESSAGES:
                rowsUpdated = db.update(DataContract.MessageEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case NOTIFICATIONS:
                rowsUpdated = db.update(DataContract.NotificationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PROFILES:
                rowsUpdated = db.update(DataContract.ProfileEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case BULLETINS:
                rowsUpdated = db.update(DataContract.BulletinEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case BULLETIN_REPLIES:
                rowsUpdated = db.update(DataContract.ReplyEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case APPROVES:
                rowsUpdated = db.update(DataContract.ApproveEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case NOT_UPLOADED_ITEMS:
                rowsUpdated = db.update(DataContract.NotUploadedEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MESSAGES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.MessageEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case NOTIFICATIONS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.NotificationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case PROFILES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.ProfileEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case BULLETINS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.BulletinEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case BULLETIN_REPLIES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.ReplyEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case APPROVES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.ApproveEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case NOT_UPLOADED_ITEMS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.NotUploadedEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DataContract.PATH_MESSAGES, MESSAGES);
        matcher.addURI(authority, DataContract.PATH_MESSAGES + "/#", MESSAGE_WITH_ID);
        matcher.addURI(authority, DataContract.PATH_NOTIFICATIONS, NOTIFICATIONS);
        matcher.addURI(authority, DataContract.PATH_NOTIFICATIONS + "/#", NOTIFICATION_WITH_ID);
        matcher.addURI(authority, DataContract.PATH_PROFILES, PROFILES);
        matcher.addURI(authority, DataContract.PATH_PROFILES + "/#", PROFILE_WITH_ID);
        matcher.addURI(authority, DataContract.PATH_BULLETINS, BULLETINS);
        matcher.addURI(authority, DataContract.PATH_BULLETINS + "/rcount/#", BULLETIN_WITH_REPLIES_COUNT);
        matcher.addURI(authority, DataContract.PATH_BULLETIN_REPLIES, BULLETIN_REPLIES);
        matcher.addURI(authority, DataContract.PATH_BULLETIN_REPLIES + "/#", BULLETIN_REPLIES);
        matcher.addURI(authority, DataContract.PATH_APPROVES, APPROVES);
        matcher.addURI(authority, DataContract.PATH_APPROVES + "/#", APPROVE_WITH_ID);
        matcher.addURI(authority, DataContract.PATH_NOT_UPLOADED, NOT_UPLOADED_ITEMS);


        return matcher;
    }
}
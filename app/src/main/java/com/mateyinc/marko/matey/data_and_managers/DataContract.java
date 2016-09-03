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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the  database.
 */
public class DataContract {

    // Name for the content provider
    public static final String CONTENT_AUTHORITY = "com.mateyinc.marko.matey";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.mateyinc.marko.matey/messages/ is a valid path for
    // looking at messages data.
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_NOTIFICATIONS = "notification";
    public static final String PATH_PROFILE = "profile";


    /* Inner class that defines the table contents of the messages table */
    public static final class MessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGES;

        // Table name
        public static final String TABLE_NAME = "messages";

        // Columns
        public static final String COLUMN_SENDER_ID = "msg_sender_id";
        public static final String COLUMN_SENDER_NAME = "msg_sender_name";
        public static final String COLUMN_MSG_BODY = "msg_body";
        public static final String COLUMN_MSG_TIME = "msg_time";
        public static final String COLUMN_IS_READ = "msg_isread";
        // TODO - add required columns

        public static Uri buildMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }

    /* Inner class that defines the table contents of the notifications table */
    public static final class NotificationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFICATIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;

        public static final String TABLE_NAME = "notifications";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_SENDER_ID = "notif_sender_id";
        public static final String COLUMN_SENDER_NAME = "notif_sender_name";
        public static final String COLUMN_NOTIF_TEXT = "notif_text";
        public static final String COLUMN_NOTIF_TIME = "notif_time";
        public static final String COLUMN_NOTIF_LINK_ID = "notif_link_id";



        public static Uri buildNotifUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class ProfileEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        public static final String TABLE_NAME = "profiles";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_NAME = "profile_name";
        public static final String COLUMN_LAST_MSG_ID = "profile_last_msg_id";
//        public static final String _ID = "id"



        public static Uri buildPorfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }


}

package com.mateyinc.marko.matey.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by M4rk0 on 3/7/2016.
 */
public class Device extends SQLiteOpenHelper {

    public static final int database_version = 1;
    public String createQuery = "CREATE TABLE " + TableData.TableInfo.TABLE_NAME + "(" + TableData.TableInfo.DEVICE_ID + " BIGINT);";

    public Device (Context context) {
        super(context, TableData.TableInfo.DATABASE_NAME, null, database_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void putInformation (Device dev, String device_id) {
        SQLiteDatabase sql = dev.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableData.TableInfo.DEVICE_ID, device_id);
        sql.insert(TableData.TableInfo.TABLE_NAME, null, cv);
        sql.close();
    }

    public ArrayList<String> retreveInformation (Device dev) {
        ArrayList<String> values = new ArrayList<String>();
        SQLiteDatabase sql = dev.getReadableDatabase();

        String[] columns = {TableData.TableInfo.DEVICE_ID};
        Cursor CR = sql.rawQuery("SELECT " + TableData.TableInfo.DEVICE_ID + " FROM " + TableData.TableInfo.TABLE_NAME, null);
        if(CR.moveToFirst()) {
            do {
                values.add(CR.getString(CR.getColumnIndex(TableData.TableInfo.DEVICE_ID)));
            }while(CR.moveToNext());
            CR.close();
            return values;
        }
        else return null;
    }

    public void deleteAll (Device dev) {
        SQLiteDatabase sql = dev.getWritableDatabase();
        sql.execSQL("DELETE FROM " + TableData.TableInfo.TABLE_NAME + ";");
    }

}

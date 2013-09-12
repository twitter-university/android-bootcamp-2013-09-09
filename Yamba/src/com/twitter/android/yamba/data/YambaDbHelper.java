package com.twitter.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class YambaDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB";

    public static final String DATABASE = "yamba.db";
    public static final int VERSION = 1;

    public static final String TABLE_TIMELINE = "timeline";
    public static final String COL_ID = "id";
    public static final String COL_TIMESTAMP = "timestmp";
    public static final String COL_USER = "user";
    public static final String COL_STATUS = "status";

    public YambaDbHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "create db");
        db.execSQL(
                "CREATE TABLE " + TABLE_TIMELINE + "("
                        + COL_ID + " INTEGER PRIMARY KEY,"
                        + COL_TIMESTAMP + " INTEGER NOT NULL,"
                        + COL_USER + " STRING NOT NULL,"
                        + COL_STATUS + " STRING" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "update db");
        db.execSQL("DROP TABLE " + TABLE_TIMELINE);
        onCreate(db);
    }
}

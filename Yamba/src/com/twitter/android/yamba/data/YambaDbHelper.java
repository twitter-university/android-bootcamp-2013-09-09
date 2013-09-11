package com.twitter.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class YambaDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "HELPER";

    public static final String TABLE_TIMELINE = "timeline";
    public static final String COL_ID = "id";

    public YambaDbHelper(Context context) {
        super(context, null, null, 0);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "creating db");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }
}

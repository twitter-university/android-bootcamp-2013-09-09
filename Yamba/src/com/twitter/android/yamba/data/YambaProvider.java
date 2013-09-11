package com.twitter.android.yamba.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class YambaProvider extends ContentProvider {
    private static final String TAG = "PROVIDER";

    //  scheme            authority             path    [id]
    // content://com.marakana.android.yamba/contactphone/7
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    }

    private static final ColumnMap COL_MAP_TIMELINE = new ColumnMap.Builder()
        .build();

    private static final ProjectionMap PROJ_MAP_TIMELINE = new ProjectionMap.Builder()
        .build();


    private YambaDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "provider created");
        dbHelper = new YambaDbHelper(getContext());
        return null != dbHelper;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {
        Log.d(TAG, "query");
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] rows) {
        Log.d(TAG, "insert: " + rows.length);
        return 0;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        throw new UnsupportedOperationException("insert not supported");
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException("update not supported");
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException("delete not supported");
    }

    private SQLiteDatabase getDb() { return dbHelper.getWritableDatabase(); }
}

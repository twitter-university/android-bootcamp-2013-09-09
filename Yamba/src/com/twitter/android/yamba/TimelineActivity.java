
package com.twitter.android.yamba;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;

public class TimelineActivity extends ListActivity implements LoaderCallbacks<Cursor>{
    private static final int TIMELINE_LOADER = 666;

    private static final String[] PROJ = new String[] {
        YambaContract.Timeline.Columns.ID,
        YambaContract.Timeline.Columns.USER,
        YambaContract.Timeline.Columns.TIMESTAMP,
        YambaContract.Timeline.Columns.STATUS
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); }

    private static final int[] TO = new int[] {
        R.id.timeline_row_user,
        R.id.timeline_row_time,
        R.id.timeline_row_status
    };

    static class TimelineBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View v, Cursor c, int col) {
            if (R.id.timeline_row_time != v.getId()) { return false; }

            CharSequence s = "long ago";
            long t = c.getLong(col);
            if (0 < t) { s = DateUtils.getRelativeTimeSpanString(t); }
            ((TextView) v).setText(s);
            return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(
                this,
                YambaContract.Timeline.URI,
                PROJ,
                null,
                null,
                YambaContract.Timeline.Columns.TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> l) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int p, long id) {
        Cursor c = (Cursor) l.getItemAtPosition(p);

        TimelineDetailActivity.showDetails(
                this,
                c.getLong(c.getColumnIndex(YambaContract.Timeline.Columns.TIMESTAMP)),
                c.getString(c.getColumnIndex(YambaContract.Timeline.Columns.USER)),
                c.getString(c.getColumnIndex(YambaContract.Timeline.Columns.STATUS)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.timeline_row,
                null,
                FROM,
                TO,
                0);

        adapter.setViewBinder(new TimelineBinder());
        setListAdapter(adapter);

        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);
    }
}

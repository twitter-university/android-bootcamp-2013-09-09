package com.twitter.android.yamba;

import android.content.Context;
import android.content.Intent;


public class TimelineDetailActivity extends YambaActivity {
    private static final String TAG = "DETAILS_ACT";

    public static void showDetails(Context ctxt, long ts, String user, String status) {
        Intent i = new Intent(ctxt, TimelineDetailActivity.class);
        i.putExtra(YambaContract.Timeline.Columns.TIMESTAMP, ts);
        i.putExtra(YambaContract.Timeline.Columns.USER, user);
        i.putExtra(YambaContract.Timeline.Columns.STATUS, status);
        ctxt.startActivity(i);
    }

    public TimelineDetailActivity() {
        super(TAG, R.layout.activity_timeline_detail);
    }
}

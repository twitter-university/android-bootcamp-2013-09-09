
package com.twitter.android.yamba;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;


public class TimelineActivity extends Activity {
    private static final String TAG = "TIMELINEACT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
        setContentView(R.layout.activity_timeline);
    }
}


package com.twitter.android.yamba;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;


public class StatusActivity extends Activity {
    public static final String TAG = "STATUSACT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
        setContentView(R.layout.activity_status);
    }
}

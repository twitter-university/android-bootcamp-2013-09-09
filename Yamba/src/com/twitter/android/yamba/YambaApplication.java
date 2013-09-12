package com.twitter.android.yamba;

import android.app.Application;
import android.util.Log;

import com.twitter.android.yamba.svc.YambaService;


public class YambaApplication extends Application {
    private static final String TAG = "APP";

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "yamba up!"); }

        YambaService.startPoller(this);
    }
}

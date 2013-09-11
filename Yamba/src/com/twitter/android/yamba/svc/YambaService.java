/* $Id: $
   Copyright 2013, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.twitter.android.yamba.svc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;
import com.twitter.android.yamba.BuildConfig;
import com.twitter.android.yamba.R;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    static final int OP_POST_COMPLETE = -1;

    private static final String PARAM_STATUS = "YambaService.STATUS";

    private static final int POLLER = 666;


    private static class Hdlr extends Handler {
        private final  YambaService svc;

        public Hdlr(YambaService svc) { this.svc = svc; }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OP_POST_COMPLETE:
                    svc.postComplete(msg.arg1);
                    break;
                default:
                    if (BuildConfig.DEBUG) { Log.d(TAG, "unexpected message: " + msg.what); }
            }
        }
    }

    public static void post(Context ctxt, String status) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "posting: " + status); }
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_STATUS, status);
        ctxt.startService(i);
    }

    public static void startPoller(Context ctxt) {
        AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
         mgr.setInexactRepeating(
                 AlarmManager.RTC,
                 System.currentTimeMillis() + 100,
                 30 * 1000,
                createPollingIntent(ctxt));
    }

    private static PendingIntent createPollingIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaService.class);
       return PendingIntent.getService(
                ctxt,
                POLLER,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private volatile YambaClient client;
    private volatile Hdlr hdlr;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "service created"); }

        hdlr = new Hdlr(this);

        client = new YambaClient("student", "password");

        startPoller(this);
    }

    public YambaService() { super(TAG); }

    void postComplete(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(Intent i) {
        String status = i.getStringExtra(PARAM_STATUS);
        int msg = R.string.fail;
        try {
            client.postStatus(status);
            msg = R.string.success;
            if (BuildConfig.DEBUG) { Log.d(TAG, "post  succeeded"); }
        }
        catch (YambaClientException e) {
            Log.e(TAG, "Post failed");
        }

        Message.obtain(hdlr, OP_POST_COMPLETE, msg, 0).sendToTarget();
    }

    private void poll() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "poll"); }
        try { client.getTimeline(20); }
        catch (YambaClientException e) {
        }
    }
}

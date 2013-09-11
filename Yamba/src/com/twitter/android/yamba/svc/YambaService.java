package com.twitter.android.yamba.svc;

import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;
import com.twitter.android.yamba.BuildConfig;
import com.twitter.android.yamba.R;
import com.twitter.android.yamba.data.YambaDbHelper;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    private static final String PARAM_STATUS = "YambaService.STATUS";
    private static final String PARAM_OP = "YambaService.OP";

    static final int OP_POST_COMPLETE = -1;
    static final int OP_POST = -2;
    static final int OP_POLL = -3;

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
        i.putExtra(PARAM_OP, OP_POST);
        i.putExtra(PARAM_STATUS, status);
        ctxt.startService(i);
    }

    public static void startPoller(Context ctxt) {
        long interval = getPollIntervalMs(ctxt);
        if (0 >= interval) { return; }
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                interval,
                createPollingIntent(ctxt));
    }

    private static PendingIntent createPollingIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POLL);
        return PendingIntent.getService(
                ctxt,
                POLLER,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static synchronized long getPollIntervalMs(Context ctxt) {
        if (0 >= pollInterval) {
            long i = ctxt.getResources().getInteger(R.integer.poll_interval);
            if (0 < i) { pollInterval = i * 60 * 1000; }
            else { Log.e(TAG, "failed retreiving poll interval");  }
        }
        return pollInterval;
    }

    // LAZILY INITIALIZED!  Use getPollInterval().
    private static long pollInterval;


    private volatile YambaClient client;
    private volatile YambaDbHelper db;
    private volatile int pollSize;
    private volatile Hdlr hdlr;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "service created"); }

        pollSize = getResources().getInteger(R.integer.poll_size);

        client = new YambaClient("student", "password");

        db = new YambaDbHelper(this);

        hdlr = new Hdlr(this);
   }

    public YambaService() { super(TAG); }

    void postComplete(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(Intent i) {
        int op = i.getIntExtra(PARAM_OP, 0);
        switch (op) {
            case OP_POST:
                doPost(i.getStringExtra(PARAM_STATUS));
                break;

            case OP_POLL:
                doPoll();
                break;

            default:
                Log.e(TAG, "Unexpected op: " + op);
        }
    }

    private void doPost(String status) {
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

    private void doPoll() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "poll"); }
        try { parseTimeline(client.getTimeline(pollSize)); }
        catch (YambaClientException e) {
            Log.e(TAG, "Poll failed");
        }
    }

    private void parseTimeline(List<Status> timeline) {
        SQLiteDatabase database = db.getWritableDatabase();
        for (Status status: timeline) {
            ContentValues cv = new ContentValues();
            cv.put(YambaDbHelper.COL_ID, Long.valueOf(status.getId()));
            cv.put("timestamp", Long.valueOf(status.getCreatedAt().getTime()));
            database.insert(YambaDbHelper.TABLE_TIMELINE, null, cv);
        }
    }
}

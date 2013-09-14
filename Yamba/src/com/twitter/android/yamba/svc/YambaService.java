package com.twitter.android.yamba.svc;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;
import com.twitter.android.yamba.BuildConfig;
import com.twitter.android.yamba.R;
import com.twitter.android.yamba.TimelineActivity;
import com.twitter.android.yamba.YambaApplication;
import com.twitter.android.yamba.YambaContract;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    private static final int NOTIFICATION_ID = 7;
    private static final int NOTIFICATION_INTENT_ID = 13;

    private static final int POLLER = 666;

    private static final String PARAM_STATUS = "YambaService.STATUS";
    private static final String PARAM_OP = "YambaService.OP";

    static final int OP_POST_COMPLETE = -1;
    private static final int OP_POST = -2;
    private static final int OP_POLL = -3;

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

    public static void stopPoller(Context ctxt) {
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .cancel(createPollingIntent(ctxt));
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


    private volatile Hdlr hdlr;
    private volatile int pollSize;
    private volatile String notifyTitle;
    private volatile String notifyMessage;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }

        Resources rez = getResources();

        pollSize = rez.getInteger(R.integer.poll_size);
        notifyTitle = rez.getString(R.string.notify_title);
        notifyMessage = rez.getString(R.string.notify_message);

        hdlr = new Hdlr(this);
   }

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
            getClient().postStatus(status);
            msg = R.string.success;
            if (BuildConfig.DEBUG) { Log.d(TAG, "post succeeded"); }
        }
        catch (YambaClientException e) {
            Log.e(TAG, "Post failed");
        }

        Message.obtain(hdlr, OP_POST_COMPLETE, msg, 0).sendToTarget();
    }

    private void doPoll() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "poll"); }

        int n = 0;
        try { n = parseTimeline(getClient().getTimeline(pollSize)); }
        catch (YambaClientException e) {
            Log.e(TAG, "Poll failed");
        }

        if (0 < n) { notify(n); }
    }

    private int parseTimeline(List<Status> timeline) {
        long latest = getLatestStatusTime();
        if (BuildConfig.DEBUG) { Log.d(TAG, "latest: " + latest); }

        List<ContentValues> vals = new ArrayList<ContentValues>();

        for (Status status: timeline) {
            long t = status.getCreatedAt().getTime();
            if (t <= latest) { continue; }

            ContentValues cv = new ContentValues();
            cv.put(YambaContract.Timeline.Columns.ID, Long.valueOf(status.getId()));
            cv.put(YambaContract.Timeline.Columns.TIMESTAMP, Long.valueOf(t));
            cv.put(YambaContract.Timeline.Columns.USER, status.getUser());
            cv.put(YambaContract.Timeline.Columns.STATUS, status.getMessage());
            vals.add(cv);
        }

        int n = vals.size();
        if (0 >= n) { return 0; }
        n = getContentResolver().bulkInsert(
                YambaContract.Timeline.URI,
                vals.toArray(new ContentValues[n]));

        if (BuildConfig.DEBUG) { Log.d(TAG, "inserted: " + n); }
        return n;
    }

    private long getLatestStatusTime() {
        Cursor c = null;
        try {
            c = getContentResolver().query(
                    YambaContract.Timeline.URI,
                    new String[] { YambaContract.Timeline.Columns.MAX_TIMESTAMP },
                    null,
                    null,
                    null);
            return ((null == c) || (!c.moveToNext()))
                    ? Long.MIN_VALUE
                    : c.getLong(0);
        }
        finally {
            if (null != c) { c.close(); }
        }
    }

    private void notify(int count) {
        PendingIntent pi = PendingIntent.getActivity(
                this,
                NOTIFICATION_INTENT_ID,
                new Intent(this, TimelineActivity.class),
                0);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(
                NOTIFICATION_ID,
                new Notification.Builder(this)
                .setContentTitle(notifyTitle)
                .setContentText(count + " " + notifyMessage)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .build());   // works as of version 16
    }

    private YambaClient getClient() throws YambaClientException {
        return ((YambaApplication) getApplication()).getYambaClient();
    }
}

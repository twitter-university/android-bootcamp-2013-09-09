package com.twitter.android.yamba;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class TimelineActivity extends YambaActivity {
    public static final String TAG = "TIMELINE_ACT";

    private static final String DETAILS_TAG = "TimelineActivity.DETAILS";

    public TimelineActivity() {
        super(TAG, R.layout.activity_timeline);
    }

    public void showDetails(long ts, String user, String status) {
        Bundle args = new Bundle();
        args.putLong(YambaContract.Timeline.Columns.TIMESTAMP, ts);
        args.putString(YambaContract.Timeline.Columns.USER, user);
        args.putString(YambaContract.Timeline.Columns.STATUS, status);

        FragmentTransaction xact = getFragmentManager().beginTransaction();
        xact.replace(
                R.id.fragment_timeline_details,
                TimelineDetailFragment.newInstance(args),
                DETAILS_TAG);
        xact.addToBackStack(null);
        xact.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        xact.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addDetailFragment();
    }

    private void addDetailFragment() {
        FragmentManager mgr = getFragmentManager();

        if (null != mgr.findFragmentByTag(DETAILS_TAG)) { return; }

        FragmentTransaction xact = mgr.beginTransaction();
        xact.add(
                R.id.fragment_timeline_details,
                TimelineDetailFragment.newInstance(null),
                DETAILS_TAG);
        xact.commit();
    }
}

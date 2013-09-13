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
package com.twitter.android.yamba;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class TimelineDetailActivity extends Activity {
    private static final String TAG = "DETAILSACT";

    public static void showDetails(Context ctxt, long ts, String user, String status) {
        Intent i = new Intent(ctxt, TimelineDetailActivity.class);
        i.putExtra(YambaContract.Timeline.Columns.TIMESTAMP, ts);
        i.putExtra(YambaContract.Timeline.Columns.USER, user);
        i.putExtra(YambaContract.Timeline.Columns.STATUS, status);
        ctxt.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }

        setContentView(R.layout.activity_timeline_detail);
     }
}

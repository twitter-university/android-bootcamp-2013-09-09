
package com.twitter.android.yamba;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.twitter.android.yamba.svc.YambaService;

public class StatusActivity extends Activity {
    public static final String TAG = "STATUS";

    private int okColor;
    private int warnColor;
    private int errColor;

    private int statusLenMax;
    private int warnMax;
    private int errMax;

    private EditText statusView;
    private TextView countView;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources rez = getResources();
        okColor = rez.getColor(R.color.green);
        statusLenMax = rez.getInteger(R.integer.status_limit);
        warnColor = rez.getColor(R.color.yellow);
        warnMax = rez.getInteger(R.integer.warn_limit);
        errColor = rez.getColor(R.color.red);
        errMax = rez.getInteger(R.integer.err_limit);

        setContentView(R.layout.activity_status);

        countView = (TextView) findViewById(R.id.status_count);

        submitButton = (Button) findViewById(R.id.status_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { post(); }
        });

        statusView = (EditText) findViewById(R.id.status_status);
        statusView.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) { updateCount(); }

                    @Override
                    public void beforeTextChanged(CharSequence s, int b, int n, int a) { }

                    @Override
                    public void onTextChanged(CharSequence s, int b, int p, int n) { }
                });
    }

    void updateCount() {
        int n = statusView.getText().length();

        submitButton.setEnabled(checkStatusLen(n));

        n = statusLenMax - n;

        int color;
        if (n > warnMax) { color = okColor; }
        else if (n > errMax) { color = warnColor; }
        else  { color = errColor; }

        countView.setText(String.valueOf(n));
        countView.setTextColor(color);
    }

    void post() {
        String status = statusView.getText().toString();
        if (BuildConfig.DEBUG) { Log.d(TAG, "posting: " + status); }
        if (!checkStatusLen(status.length())) { return; }

        statusView.setText("");
        YambaService.post(this, status);
    }

    private boolean checkStatusLen(int n) {
        return (errMax < n) && (statusLenMax > n);
    }
}

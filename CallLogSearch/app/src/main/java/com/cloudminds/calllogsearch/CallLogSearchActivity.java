package com.cloudminds.calllogsearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class CallLogSearchActivity extends Activity {
    public static final String TAG = "CallLogSearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                //intent.setClass(this, BrowserActivity.class);
                Log.d(TAG, "intent=" + intent);
                startActivity(intent);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 500);
        finish();
    }
}

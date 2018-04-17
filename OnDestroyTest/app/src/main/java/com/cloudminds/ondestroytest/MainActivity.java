package com.cloudminds.ondestroytest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    private static final String TAG = "OnDestroyTest";

    Button mButtonFinish;
    Button mButtonCrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ====>>>>");
        setContentView(R.layout.activity_main);

        mButtonFinish = (Button)findViewById(R.id.button_finish);
        mButtonFinish.setOnClickListener(this);
        mButtonCrash = (Button)findViewById(R.id.button_crash);
        mButtonCrash.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy ====>>>>");
        Intent intent = new Intent(Intent.ACTION_DEFAULT);
        intent.setClassName("com.cloudminds.ondestroytest", "com.cloudminds.ondestroytest.MainActivity");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_finish:
                finish();
                break;
            case R.id.button_crash:
                View view = null;
                view.getId();
                break;
        }
    }
}

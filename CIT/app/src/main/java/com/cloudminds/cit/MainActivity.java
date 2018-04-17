package com.cloudminds.cit;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cloudminds.cit.com.cloudminds.cit.cases.CaseFragment;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity implements CaseFragment.OnFragmentInteractionListener {
    private static final String TAG = "CIT";
    private static final int PERMISSIONS_REQUEST = 0;

    private View mFragmentContainerView;

    private CaseFragment mCaseFragment;
    private Iterator<String> mCaseKeySetIterator;
    private String mCurrentCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mFragmentContainerView = findViewById(R.id.fragment_container);
        mCaseKeySetIterator = Utils.sCaseTable.keySet().iterator();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ensurePermissions();
        } else {
            startFirstCase();
        }
    }

    private void ensurePermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        for (String permission : Utils.sUsePermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSIONS_REQUEST);
        } else {
            startFirstCase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        finish();
                        return;
                    }
                }

                startFirstCase();
                break;
            }
            default:
                break;
        }
    }

    private void startFirstCase() {
        Log.d(TAG, "startFirstCase()");
        mCurrentCase = mCaseKeySetIterator.next();
        Log.d(TAG, "mCurrentCase=" + mCurrentCase);
        Class clazz = Utils.sCaseTable.get(mCurrentCase);

        try {
            mCaseFragment = (CaseFragment)(clazz.newInstance());
        } catch (InstantiationException e) {
            Log.w(TAG, "startFirstCase InstantiationException:", e);
        } catch (IllegalAccessException e) {
            Log.w(TAG, "startFirstCase IllegalAccessException:", e);
        }

        mCaseFragment.setOnFragmentInteractionListener(this);
        getFragmentManager().beginTransaction().
                add(R.id.fragment_container, mCaseFragment, "CaseFragment")
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFragmentContainerView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCaseFragment.onKeyDown(keyCode, event)) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onTestFinish(String caseName, int result) {
        Log.d(TAG, "onTestFinish caseName:" + caseName + ", result:" + result);
        if (!caseName.equals(mCurrentCase)) {
            Log.e(TAG, "Test case name not match!!! expect:" + mCurrentCase + ", result:" + caseName);
            return;
        }

        Utils.saveTestResult(caseName, result);

        if (!mCaseKeySetIterator.hasNext()) {
            Log.d(TAG, "All test case finished.");
            // TODO: show test result of all cases
            finish();
            return;
        }

        mCurrentCase = mCaseKeySetIterator.next();
        Log.d(TAG, "mCurrentCase=" +mCurrentCase);
        Class clazz = Utils.sCaseTable.get(mCurrentCase);

        CaseFragment newFragment = null;
        try {
            newFragment = (CaseFragment)(clazz.newInstance());
        } catch (InstantiationException e) {
            Log.w(TAG, "onTestFinish InstantiationException:", e);
        } catch (IllegalAccessException e) {
            Log.w(TAG, "onTestFinish IllegalAccessException:", e);
        }

        if (newFragment == null) {
            Log.e(TAG, "ERROR!!! new fragment is null, case name:" + mCurrentCase);
            return;
        }
        newFragment.setOnFragmentInteractionListener(this);
        getFragmentManager().beginTransaction()
                .remove(mCaseFragment)
                .add(R.id.fragment_container, newFragment, "CaseFragment")
                .commit();

        mCaseFragment = newFragment;
    }
}

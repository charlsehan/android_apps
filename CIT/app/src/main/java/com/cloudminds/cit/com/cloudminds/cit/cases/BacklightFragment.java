package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class BacklightFragment extends CaseFragment implements View.OnClickListener {
    private float mBrightness;

    private static final int BRIGHTNESS_CHANGE_DELAY_MILLIS = 300;
    private static final float BRIGHTNESS_DELTA_VALUE = 0.2f;

    private Handler mHandler = new Handler();
    private final Runnable mBrightnessChangeRunnable = new Runnable() {
        @Override
        public void run() {
            mBrightness -= BRIGHTNESS_DELTA_VALUE;
            if(mBrightness < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF) {
                mBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            setBrightness(mBrightness);
            mHandler.postDelayed(mBrightnessChangeRunnable, BRIGHTNESS_CHANGE_DELAY_MILLIS);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_backlight, container, false);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        startTest();

        return fragmentView;
    }

    private void startTest() {
        mBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        setBrightness(mBrightness);
        mHandler.postDelayed(mBrightnessChangeRunnable, BRIGHTNESS_CHANGE_DELAY_MILLIS);
    }

    private void stopTest() {
        mHandler.removeCallbacks(mBrightnessChangeRunnable);
        setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    private void setBrightness(float value) {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = value;
        window.setAttributes(lp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTest();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fail:
                stopTest();
                onTestFail();
                break;
            case R.id.button_pass:
                stopTest();
                onTestPass();
                break;
            default:
                break;
        }
    }

    @Override
    public String getCaseName() {
        return Utils.CASE_BACKLIGHT;
    }
}

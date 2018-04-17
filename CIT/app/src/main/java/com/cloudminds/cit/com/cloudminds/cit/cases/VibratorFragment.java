package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class VibratorFragment extends CaseFragment implements View.OnClickListener {
    private TextView mMessageView;
    private View mActionButtonContainer;

    private Vibrator mVibrator;

    private static final int BUTTON_SHOW_DELAY_MILLIS = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_vibrator, container, false);
        mMessageView = (TextView)fragmentView.findViewById(R.id.message);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        startTest();

        return fragmentView;
    }

    private final Runnable mButtonShowRunnable = new Runnable() {
        @Override
        public void run() {
            mActionButtonContainer.setVisibility(View.VISIBLE);
        }
    };

    private void startTest() {
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {300, 1000, 300, 200, 100, 200};
        mVibrator.vibrate(pattern, 0);
        new Handler().postDelayed(mButtonShowRunnable, BUTTON_SHOW_DELAY_MILLIS);
    }

    private void stopTest() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
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
        return Utils.CASE_VIBRATOR;
    }
}

package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;


public class LCDFragment extends CaseFragment implements View.OnClickListener {

    private View mColorView;
    private View mActionButtonContainer;
    private static final int COLOR_CHANGE_DELAY_MILLIS = 1000;

    private int mCurrentColorId = 0;
    private final int[] COLORS = new int[] {
            Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.WHITE };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_lcd, container, false);
        mColorView = fragmentView.findViewById(R.id.color_view);
        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);
        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setOnClickListener(this);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        startTest();

        return fragmentView;
    }

    private Handler mHandler = new Handler();
    private final Runnable mColorChangeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentColorId < COLORS.length) {
                mColorView.setBackgroundColor(COLORS[mCurrentColorId]);
                mCurrentColorId++;
                mHandler.removeCallbacks(mColorChangeRunnable);
                mHandler.postDelayed(mColorChangeRunnable, COLOR_CHANGE_DELAY_MILLIS);
            } else {
                mActionButtonContainer.setVisibility(View.VISIBLE);
            }
        }
    };

    private void startTest() {
        mCurrentColorId = 0;
        mActionButtonContainer.setVisibility(View.GONE);
        mHandler.post(mColorChangeRunnable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_retest:
                startTest();
                break;
            case R.id.button_fail:
                onTestFail();
                break;
            case R.id.button_pass:
                onTestPass();
                break;
            default:
                break;
        }
    }

    @Override
    public String getCaseName() {
        return Utils.CASE_LCD;
    }
}

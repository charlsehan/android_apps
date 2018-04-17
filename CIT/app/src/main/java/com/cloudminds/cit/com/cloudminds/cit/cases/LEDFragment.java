package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class LEDFragment extends CaseFragment implements View.OnClickListener {
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    private View mActionButtonContainer;

    private int mCurrentColorId = 0;
    private static final int COLOR_CHANGE_DELAY_MILLIS = 1000;
    private final int[] COLORS = new int[] {
            Color.RED, Color.GREEN, Color.BLUE, Color.WHITE };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_led, container, false);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        mNotificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        startTest();

        return fragmentView;
    }

    private Handler mHandler = new Handler();
    private final Runnable mColorChangeRunnable = new Runnable() {
        @Override
        public void run() {
            setNotification(COLORS[mCurrentColorId]);
            mCurrentColorId++;
            if (mCurrentColorId >= COLORS.length) {
                mCurrentColorId = 0;
                mActionButtonContainer.setVisibility(View.VISIBLE);
            }
            mHandler.removeCallbacks(mColorChangeRunnable);
            mHandler.postDelayed(mColorChangeRunnable, COLOR_CHANGE_DELAY_MILLIS);
        }
    };

    private void startTest() {
        mCurrentColorId = 0;
        mActionButtonContainer.setVisibility(View.GONE);
        mHandler.post(mColorChangeRunnable);
    }

    private void stopTest() {
        mHandler.removeCallbacks(mColorChangeRunnable);
        cancelNotification();
    }

    private void setNotification(int color) {
        Notification.Builder builder = new Notification.Builder(getContext());
        builder.setSmallIcon(Icon.createWithResource(getContext(), R.mipmap.ic_launcher));
        builder.setLights(color, 2000, 0);
        Bundle extras = new Bundle();
        extras.putInt("com.cloudminds.cit.led.color", color);
        builder.setExtras(extras);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
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
        return Utils.CASE_LED;
    }
}

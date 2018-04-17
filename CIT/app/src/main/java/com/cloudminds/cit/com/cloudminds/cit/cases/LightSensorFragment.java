package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class LightSensorFragment extends CaseFragment
        implements View.OnClickListener, SensorEventListener {
    private static final int THRESHOLD_VALUE = 5;
    private static final int TEST_TIMES = 2;

    private SensorManager mSensorManager;

    private TextView mLabel;
    private float mValue;
    private int mTestCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_light_sensor, container, false);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);
        buttonPass.setVisibility(View.GONE);

        mLabel = (TextView)fragmentView.findViewById(R.id.label);

        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        registerSensor();

        return fragmentView;
    }

    protected void registerSensor() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                String valueX = getString(R.string.light_sensor_value);

                //int darkColor = ContextCompat.getColor(getActivity(), R.color.pass_button_color);
                //int lightColor = ContextCompat.getColor(getActivity(), R.color.key_label_color);
                int darkColor = getContext().getColor(R.color.pass_button_color);
                int lightColor = getContext().getColor(R.color.key_label_color);

                mLabel.setText(String.format("%s %.1f lux", valueX, event.values[0]));
                if (event.values[0] <= THRESHOLD_VALUE) {
                    mLabel.setBackgroundColor(darkColor);
                    if (mValue > THRESHOLD_VALUE) {
                        mTestCount++;
                    }
                } else {
                    mLabel.setBackgroundColor(lightColor);
                    if (mTestCount >= TEST_TIMES) {
                        onTestPass();
                    }
                }
                mValue = event.values[0];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public String getCaseName() {
        return Utils.CASE_SENSOR_LIGHT;
    }
}

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

public class ProximitySensorFragment extends CaseFragment
        implements View.OnClickListener, SensorEventListener {
    private static final int TEST_TIMES = 2;

    private SensorManager mSensorManager;

    private TextView mLabel;
    private int mNearCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_proximity_sensor, container, false);

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
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
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
            case Sensor.TYPE_PROXIMITY:
                String valueX = getString(R.string.proximity_sensor_value);

                //int nearColor = ContextCompat.getColor(getActivity(), R.color.pass_button_color);
                //int farColor = ContextCompat.getColor(getActivity(), R.color.key_label_color);
                int nearColor = getContext().getColor(R.color.pass_button_color);
                int farColor = getContext().getColor(R.color.key_label_color);

                mLabel.setText(String.format("%s %.1f", valueX, event.values[0]));
                if (event.values[0] == 0) {
                    mLabel.setBackgroundColor(nearColor);
                    mNearCount ++;
                } else {
                    mLabel.setBackgroundColor(farColor);
                    if (mNearCount >= TEST_TIMES) {
                        onTestPass();
                    }
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public String getCaseName() {
        return Utils.CASE_SENSOR_PROXIMITY;
    }
}

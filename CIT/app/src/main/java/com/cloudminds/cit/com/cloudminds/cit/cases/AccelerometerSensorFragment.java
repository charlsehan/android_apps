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

public class AccelerometerSensorFragment extends CaseFragment
        implements View.OnClickListener, SensorEventListener {
    private static final int THRESHOLD_LOW = 8;
    private static final int THRESHOLD_HIGH = 12;

    private SensorManager mSensorManager;

    private TextView mLabelX;
    private TextView mLabelY;
    private TextView mLabelZ;

    private boolean mXOK = false;
    private boolean mYOK = false;
    private boolean mZOK = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_accelerometer_sensor, container, false);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);
        buttonPass.setVisibility(View.GONE);

        mLabelX = (TextView)fragmentView.findViewById(R.id.label_x);
        mLabelY = (TextView)fragmentView.findViewById(R.id.label_y);
        mLabelZ = (TextView)fragmentView.findViewById(R.id.label_z);

        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        registerSensor();

        return fragmentView;
    }

    protected void registerSensor() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
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
            case Sensor.TYPE_ACCELEROMETER:
                String valueX = getString(R.string.sensor_value_x);
                String valueY = getString(R.string.sensor_value_y);
                String valueZ = getString(R.string.sensor_value_z);
                //int passColor = ContextCompat.getColor(getActivity(), R.color.pass_button_color);
                int passColor = getContext().getColor(R.color.pass_button_color);

                mLabelX.setText(String.format("%s %+06.2f (m/s^2)", valueX, event.values[0]));
                mLabelY.setText(String.format("%s %+06.2f (m/s^2)", valueY, event.values[1]));
                mLabelZ.setText(String.format("%s %+06.2f (m/s^2)", valueZ, event.values[2]));

                if (Math.abs(event.values[0]) > THRESHOLD_LOW && Math.abs(event.values[0]) < THRESHOLD_HIGH) {
                    mXOK = true;
                    mLabelX.setBackgroundColor(passColor);
                }
                if (event.values[1] > THRESHOLD_LOW && event.values[1] < THRESHOLD_HIGH) {
                    mYOK = true;
                    mLabelY.setBackgroundColor(passColor);
                }
                if (event.values[2] > THRESHOLD_LOW && event.values[2] < THRESHOLD_HIGH) {
                    mZOK = true;
                    mLabelZ.setBackgroundColor(passColor);
                }

                if (mXOK && mYOK && mZOK) {
                    onTestPass();
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public String getCaseName() {
        return Utils.CASE_SENSOR_ACCELEROMETER;
    }
}

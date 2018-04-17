package com.cloudminds.cit;

import android.Manifest;

import com.cloudminds.cit.com.cloudminds.cit.cases.AccelerometerSensorFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.BacklightFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.EarphoneFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.FlashlightFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.FrontCameraFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.KeyFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.LEDFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.LightSensorFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.MainCameraFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.LCDFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.ProximitySensorFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.ReceiverFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.RecorderFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.SpeakerFragment;
import com.cloudminds.cit.com.cloudminds.cit.cases.VibratorFragment;

import java.util.LinkedHashMap;

public class Utils {
    public static final String[] sUsePermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String CASE_LCD = "LCD";
    public static final String CASE_VIBRATOR = "Vibrator";
    public static final String CASE_SPEAKER = "Speaker";
    public static final String CASE_RECEIVER = "Receiver";
    public static final String CASE_MAIN_CAMERA = "MainCamera";
    public static final String CASE_FRONT_CAMERA = "FrontCamera";
    public static final String CASE_RECORDER = "Recorder";
    public static final String CASE_EARPHONE = "Earphone";
    public static final String CASE_KEY = "Key";
    public static final String CASE_BACKLIGHT = "Backlight";
    public static final String CASE_SENSOR_ACCELEROMETER = "AccelerometerSensor";
    public static final String CASE_SENSOR_PROXIMITY = "ProximitySensor";
    public static final String CASE_SENSOR_LIGHT = "LightSensor";
    public static final String CASE_FLASHLIGHT = "Flashlight";
    public static final String CASE_LED = "LED";

    public static final LinkedHashMap<String, Class> sCaseTable = new LinkedHashMap<>();
    static {
        sCaseTable.put(CASE_LED, LEDFragment.class);
        sCaseTable.put(CASE_FLASHLIGHT, FlashlightFragment.class);
        sCaseTable.put(CASE_SENSOR_LIGHT, LightSensorFragment.class);
        sCaseTable.put(CASE_SENSOR_PROXIMITY, ProximitySensorFragment.class);
        sCaseTable.put(CASE_SENSOR_ACCELEROMETER, AccelerometerSensorFragment.class);
        sCaseTable.put(CASE_VIBRATOR, VibratorFragment.class);
        sCaseTable.put(CASE_KEY, KeyFragment.class);
        sCaseTable.put(CASE_BACKLIGHT, BacklightFragment.class);
        sCaseTable.put(CASE_LCD, LCDFragment.class);
        sCaseTable.put(CASE_MAIN_CAMERA, MainCameraFragment.class);
        sCaseTable.put(CASE_FRONT_CAMERA, FrontCameraFragment.class);
        sCaseTable.put(CASE_SPEAKER, SpeakerFragment.class);
        sCaseTable.put(CASE_RECEIVER, ReceiverFragment.class);
        sCaseTable.put(CASE_RECORDER, RecorderFragment.class);
        sCaseTable.put(CASE_EARPHONE, EarphoneFragment.class);
    }

    public static final int RESULT_PASS = 1;
    public static final int RESULT_FAIL = 2;

    public static void saveTestResult(String caseName, int result) {
        //TODO: complete test result saving function
    }
}

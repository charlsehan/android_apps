package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class FlashlightFragment extends CaseFragment implements View.OnClickListener {
    private static final String TAG = "CIT";

    private View mActionButtonContainer;
    private CameraManager mCameraManager;
    private String mCameraId;

    private static final int FLASHLIGHT_ON_MILLIS = 2000;
    private static final int FLASHLIGHT_OFF_MILLIS = 500;
    private static final int TEST_TIMES = 2;

    private int mTestCount = 0;
    private Handler mHandler = new Handler();

    private final Runnable mLightOnRunnable = new Runnable() {
        @Override
        public void run() {
            lightOn();
            mHandler.postDelayed(mLightOffRunnable, FLASHLIGHT_ON_MILLIS);
        }
    };

    private final Runnable mLightOffRunnable = new Runnable() {
        @Override
        public void run() {
            lightOff();
            mTestCount ++;
            if (mTestCount >= TEST_TIMES) {
                mActionButtonContainer.setVisibility(View.VISIBLE);
            } else {
                mHandler.postDelayed(mLightOnRunnable, FLASHLIGHT_OFF_MILLIS);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_flashlight, container, false);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setOnClickListener(this);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        initial();
        startTest();

        return fragmentView;
    }

    private void startTest() {
        mTestCount = 0;
        mActionButtonContainer.setVisibility(View.GONE);
        mHandler.post(mLightOnRunnable);
    }

    private void stopTest() {
        lightOff();
    }

    private void initial() {
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = getCameraId();
        } catch (Throwable e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        } finally {
            mCameraId = cameraId;
        }
    }

    private void lightOn() {
        try {
            mCameraManager.setTorchMode(mCameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void lightOff() {
        try {
            mCameraManager.setTorchMode(mCameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTest();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_retest:
                startTest();
                break;
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
        return Utils.CASE_FLASHLIGHT;
    }
}

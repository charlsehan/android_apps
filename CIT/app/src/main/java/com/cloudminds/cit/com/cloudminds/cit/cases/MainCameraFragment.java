package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

import java.io.IOException;

public class MainCameraFragment extends CaseFragment implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "CIT";

    protected static final int CAMERA_LENS_BACK_ID = 0;
    protected static final int CAMERA_LENS_FRONT_ID = 1;

    private Camera mCamera;
    protected int mCameraLensId;

    private View mActionButtonContainer;
    protected View mAutofocusButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_camera, container, false);

        SurfaceView surfaceView = (SurfaceView) fragmentView.findViewById(R.id.surface_camera);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);

        mCameraLensId = CAMERA_LENS_BACK_ID;

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);
        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setOnClickListener(this);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        mAutofocusButton = fragmentView.findViewById(R.id.button_autofocus);
        mAutofocusButton.setOnClickListener(this);

        return fragmentView;
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraLensId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(mCameraLensId == CAMERA_LENS_BACK_ID) {
                Log.d(TAG, "surfaceCreated Camera.open(0)");
                mCamera = Camera.open(0);
            } else {
                Log.d(TAG, "surfaceCreated Camera.open(1)");
                mCamera = Camera.open(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int  format, int w, int h) {

        try {
            Log.d(TAG, "w=" + w + ", h=" + h);
            Camera.Parameters p = mCamera.getParameters();
            p.setPreviewSize(1280, 720);
            p.setPictureSize(640, 480);

            setCameraDisplayOrientation();
            mCamera.setParameters(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    Camera.PictureCallback mPictureCallback = new  Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {
            //not need to do anything
        }
    };

    Camera.ShutterCallback mShutterCallback = new  Camera.ShutterCallback() {
        public void onShutter() {
            //not need to do anything
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_autofocus:
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mCamera.takePicture(mShutterCallback, mPictureCallback, mPictureCallback);
                        mAutofocusButton.setVisibility(View.GONE);
                        mActionButtonContainer.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.button_retest:
                mCamera.startPreview();
                mAutofocusButton.setVisibility(View.VISIBLE);
                mActionButtonContainer.setVisibility(View.GONE);
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
        return Utils.CASE_MAIN_CAMERA;
    }
}

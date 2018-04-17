package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class FrontCameraFragment extends MainCameraFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = super.onCreateView(inflater, container, savedInstanceState);
        mCameraLensId = CAMERA_LENS_FRONT_ID;
        ((TextView)mAutofocusButton).setText(R.string.camera_take_picture);
        return fragmentView;
    }

    @Override
    public String getCaseName() {
        return Utils.CASE_FRONT_CAMERA;
    }
}

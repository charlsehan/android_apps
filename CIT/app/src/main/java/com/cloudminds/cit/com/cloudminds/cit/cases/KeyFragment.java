package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

import java.util.HashMap;

public class KeyFragment extends CaseFragment implements View.OnClickListener {
    private static final String TAG = "CIT";

    protected TextView mMessageView;
    private View mButtonPass;

    private HashMap<Integer, View> mLabelViewMap = new HashMap<>();
    private HashMap<Integer, Boolean> mTestResultMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_key, container, false);
        mMessageView = (TextView)fragmentView.findViewById(R.id.message);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setOnClickListener(this);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        mButtonPass = fragmentView.findViewById(R.id.button_pass);
        mButtonPass.setOnClickListener(this);
        mButtonPass.setVisibility(View.GONE);

        View keyLabelVolumeUp = fragmentView.findViewById(R.id.key_label_volume_up);
        mLabelViewMap.put(KeyEvent.KEYCODE_VOLUME_UP, keyLabelVolumeUp);
        mTestResultMap.put(KeyEvent.KEYCODE_VOLUME_UP, false);

        View keyLabelVolumeDown = fragmentView.findViewById(R.id.key_label_vollume_down);
        mLabelViewMap.put(KeyEvent.KEYCODE_VOLUME_DOWN, keyLabelVolumeDown);
        mTestResultMap.put(KeyEvent.KEYCODE_VOLUME_DOWN, false);

        startTest();

        return fragmentView;
    }

    private void startTest() {
        for (int keyCode : mTestResultMap.keySet()) {
            mTestResultMap.put(keyCode, false);
            mLabelViewMap.get(keyCode).setVisibility(View.VISIBLE);
        }
        mButtonPass.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mTestResultMap.containsKey(keyCode)) {
            mTestResultMap.put(keyCode, true);
            mLabelViewMap.get(keyCode).setVisibility(View.INVISIBLE);

            boolean allKeyOK = true;
            for (boolean ok : mTestResultMap.values()) {
                allKeyOK = allKeyOK && ok;
            }
            if (allKeyOK) {
                onTestPass();
            }
            return true;
        }
        return false;
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
        return Utils.CASE_KEY;
    }
}

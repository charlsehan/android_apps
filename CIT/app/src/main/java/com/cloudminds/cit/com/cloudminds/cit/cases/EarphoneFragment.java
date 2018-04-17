package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

public class EarphoneFragment extends RecorderFragment {

    private View mButtonRetest;
    private View mButtonFail;
    private View mButtonPass;

    private boolean mEarphoneKeyTestEnable;

    private AudioManager mAudioManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_recorder, container, false);
        mMessageView = (TextView)fragmentView.findViewById(R.id.message);
        mMessageView.setText(R.string.earphone_test);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);

        mButtonRetest = fragmentView.findViewById(R.id.button_retest);
        mButtonRetest.setOnClickListener(this);
        ((TextView)mButtonRetest).setText(R.string.start);

        mButtonFail = fragmentView.findViewById(R.id.button_fail);
        mButtonFail.setOnClickListener(this);

        mButtonPass = fragmentView.findViewById(R.id.button_pass);
        mButtonPass.setOnClickListener(this);
        mButtonPass.setVisibility(View.GONE);

        mEarphoneKeyTestEnable = false;
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (!mAudioManager.isWiredHeadsetOn()) {
            mMessageView.setText(R.string.plug_in_earphone);
        }
        return fragmentView;
    }

    @Override
    protected void startTest() {
        if (mAudioManager.isWiredHeadsetOn()) {
            setupViewsOnStartTest();
            mHandler.post(mStartRecordRunnable);
        } else {
            mMessageView.setText(R.string.plug_in_earphone);
        }
    }

    @Override
    protected void setupViewsOnStartTest() {
        mButtonRetest.setVisibility(View.GONE);
        mButtonFail.setVisibility(View.GONE);
        mButtonPass.setVisibility(View.GONE);
    }

    @Override
    protected void setupViewsOnStopPlay() {
        mMessageView.setText(R.string.press_earphone_key);
        mEarphoneKeyTestEnable = true;
        mButtonRetest.setVisibility(View.VISIBLE);
        ((TextView)mButtonRetest).setText(R.string.retest);
        mButtonFail.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK && mEarphoneKeyTestEnable) {
            mMessageView.setText(R.string.earphone_key_pressed);
            mButtonPass.setVisibility(View.VISIBLE);
            mEarphoneKeyTestEnable = false;
            return true;
        }
        return false;
    }

    @Override
    public String getCaseName() {
        return Utils.CASE_EARPHONE;
    }
}

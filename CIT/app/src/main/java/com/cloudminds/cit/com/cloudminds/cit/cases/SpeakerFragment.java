package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

import java.io.IOException;

public class SpeakerFragment extends CaseFragment implements View.OnClickListener {
    private static final String TAG = "CIT";

    protected TextView mMessageView;
    private View mActionButtonContainer;

    protected MediaPlayer mPlayer;

    protected static final int BUTTON_SHOW_DELAY_MILLIS = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_speaker, container, false);
        mMessageView = (TextView)fragmentView.findViewById(R.id.message);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setVisibility(View.GONE);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        startTest();

        return fragmentView;
    }

    protected final Runnable mButtonShowRunnable = new Runnable() {
        @Override
        public void run() {
            mActionButtonContainer.setVisibility(View.VISIBLE);
        }
    };

    protected void startTest() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = new MediaPlayer();

        try {
            AssetFileDescriptor fd = getActivity().getResources().openRawResourceFd(R.raw.speaker);
            if (fd != null) {
                mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                fd.close();
                mPlayer.prepare();
            }
        } catch (IOException e) {
            Log.w(TAG, "startTest IOException:", e);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "startTest IllegalArgumentException:", e);
        } catch (IllegalStateException e) {
            Log.w(TAG, "startTest IllegalStateException:", e);
        }

        try {
            mPlayer.start();
        } catch (IllegalStateException e) {
            Log.w(TAG, "mPlayer.start(), IllegalStateException:", e);
        }

        new Handler().postDelayed(mButtonShowRunnable, BUTTON_SHOW_DELAY_MILLIS);
    }

    private void stopTest() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    //TODO: Set MUSIC volume to max before play
    private void setMaxMusicVolume() {

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
        return Utils.CASE_SPEAKER;
    }
}

package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

import java.io.IOException;

public class ReceiverFragment extends SpeakerFragment {
    private static final String TAG = "CIT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = super.onCreateView(inflater, container, savedInstanceState);
        mMessageView.setText(R.string.receiver_test);
        return fragmentView;
    }

    @Override
    protected void startTest() {
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = new MediaPlayer();

        try {
            AssetFileDescriptor fd = getActivity().getResources().openRawResourceFd(R.raw.receiver);
            if (fd != null) {
                mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                fd.close();
                mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
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

    @Override
    public String getCaseName() {
        return Utils.CASE_RECEIVER;
    }
}

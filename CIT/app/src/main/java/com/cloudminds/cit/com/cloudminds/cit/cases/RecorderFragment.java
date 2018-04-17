package com.cloudminds.cit.com.cloudminds.cit.cases;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudminds.cit.R;
import com.cloudminds.cit.Utils;

import java.io.File;
import java.io.IOException;

public class RecorderFragment extends CaseFragment implements View.OnClickListener {
    private static final String TAG = "CIT";

    protected TextView mMessageView;
    protected View mActionButtonContainer;

    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    private static final String RECORD_FILE =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.amr";

    private static final int STOP_RECORD_DELAY_MILLIS = 3000;
    private static final int START_PLAY_DELAY_MILLIS = 500;
    private static final int STOP_PLAY_DELAY_MILLIS = 3000;

    protected Handler mHandler = new Handler();

    protected final Runnable mStartRecordRunnable = new Runnable() {
        @Override
        public void run() {
            startRecording();
            setupViewsOnStartRecording();
            mHandler.postDelayed(mStopRecordRunnable, STOP_RECORD_DELAY_MILLIS);
        }
    };

    private final Runnable mStopRecordRunnable = new Runnable() {
        @Override
        public void run() {
            stopRecording();
            setupViewsOnStopRecording();
            mHandler.postDelayed(mStartPlayRunnable, START_PLAY_DELAY_MILLIS);
        }
    };

    private final Runnable mStartPlayRunnable = new Runnable() {
        @Override
        public void run() {
            startPlay();
            setupViewsOnStartPlay();
            mHandler.postDelayed(mStopPlayRunnable, STOP_PLAY_DELAY_MILLIS);
        }
    };

    private final Runnable mStopPlayRunnable = new Runnable() {
        @Override
        public void run() {
            stopPlay();
            setupViewsOnStopPlay();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_recorder, container, false);
        mMessageView = (TextView)fragmentView.findViewById(R.id.message);

        mActionButtonContainer = fragmentView.findViewById(R.id.action_button_container);
        mActionButtonContainer.setVisibility(View.GONE);

        View buttonRetest = fragmentView.findViewById(R.id.button_retest);
        buttonRetest.setOnClickListener(this);
        View buttonFail = fragmentView.findViewById(R.id.button_fail);
        buttonFail.setOnClickListener(this);
        View buttonPass = fragmentView.findViewById(R.id.button_pass);
        buttonPass.setOnClickListener(this);

        startTest();

        return fragmentView;
    }

    protected void startTest() {
        setupViewsOnStartTest();
        mHandler.post(mStartRecordRunnable);
    }

    protected void setupViewsOnStartTest() {
        mActionButtonContainer.setVisibility(View.GONE);
    }

    private void setupViewsOnStartRecording() {
        mMessageView.setText(R.string.recording);
    }

    private void setupViewsOnStopRecording() {
        mMessageView.setText(R.string.record_finish);
    }

    private void setupViewsOnStartPlay() {
        mMessageView.setText(R.string.playing);
    }

    protected void setupViewsOnStopPlay() {
        mMessageView.setText(R.string.recorder_test);
        mActionButtonContainer.setVisibility(View.VISIBLE);
    }

    private void startRecording() {
        File recordFile = new File(RECORD_FILE);
        if(recordFile.exists()) {
            recordFile.delete();
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(RECORD_FILE);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void startPlay() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stopPlay();
                File recordFile = new File(RECORD_FILE);
                if (recordFile.exists()) {
                    recordFile.delete();
                }
            }
        });

        try {
            mPlayer.setDataSource(RECORD_FILE);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.start();
    }

    protected void stopPlay() {
        File recordFile = new File(RECORD_FILE);
        if(recordFile.exists()) {
            recordFile.delete();
        }

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    //TODO: Set MUSIC volume to max before play
    private void setMaxMusicVolume() {

    }

    private void cleanUp() {
        stopRecording();
        stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
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
        return Utils.CASE_RECORDER;
    }
}

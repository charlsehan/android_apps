
package com.cloudminds.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenState {
    public interface OnScreenStateListener {
        void onScreenState(boolean state);
    }

    private Context mContext = null;
    private OnScreenStateListener mOnScreenStateListener = null;
    private volatile Boolean mStateLast = null;

    private IntentFilter mFilter = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState(intent);
        }
    };

    public ScreenState() {
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_SCREEN_ON);
        mFilter.addAction(Intent.ACTION_SCREEN_OFF);
    }

    private void updateState(Intent intent) {
        if (mOnScreenStateListener != null) {
            Boolean state = null;
            if (intent != null) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
                    state = true;
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()))
                    state = false;
            }
            if (state == null) {
                state = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE))
                        .isScreenOn();
            }

            if ((mStateLast == null) || (mStateLast != state)) {
                mStateLast = state;
                mOnScreenStateListener.onScreenState(state);
            }
        }
    }

    public boolean start(Context context, OnScreenStateListener onScreenStateListener) {
        if (mContext == null) {
            mContext = context;
            mOnScreenStateListener = onScreenStateListener;
            mContext.registerReceiver(mReceiver, mFilter);
            updateState(null);
            return true;
        }
        return false;
    }

    public boolean stop() {
        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
            mOnScreenStateListener = null;
            mContext = null;
            return true;
        }
        return false;
    }

    public Boolean getState() {
        if (mStateLast == null)
            return false;
        return mStateLast.booleanValue();
    }
}

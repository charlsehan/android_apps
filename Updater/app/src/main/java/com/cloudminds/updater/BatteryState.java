
package com.cloudminds.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryState {
    public interface OnBatteryStateListener {
        void onBatteryState(boolean state);
    }

    private Context mContext = null;
    private OnBatteryStateListener mOnBatteryStateListener = null;
    private volatile Boolean mStateLast = null;

    private int mMinLevel = 50;
    private boolean mChargeOnly = false;

    private IntentFilter mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            updateState(
                    level,
                    (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                            (status == BatteryManager.BATTERY_STATUS_FULL)
            );
        }
    };

    private void updateState(int level, boolean charging) {
        if (mOnBatteryStateListener != null) {
            boolean state = (
                    (charging && mChargeOnly) ||
                    ((level >= mMinLevel) && (!mChargeOnly))
            );

            if ((mStateLast == null) || (mStateLast != state)) {
                mStateLast = state;
                mOnBatteryStateListener.onBatteryState(state);
            }
        }
    }

    public boolean start(Context context, OnBatteryStateListener onBatteryStateListener,
            int minLevel, boolean chargeOnly) {
        if (mContext == null) {
            mContext = context;
            mOnBatteryStateListener = onBatteryStateListener;
            mMinLevel = minLevel;
            mChargeOnly = chargeOnly;
            mContext.registerReceiver(mReceiver, mFilter);
            return true;
        }
        return false;
    }

    public boolean stop() {
        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
            mOnBatteryStateListener = null;
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

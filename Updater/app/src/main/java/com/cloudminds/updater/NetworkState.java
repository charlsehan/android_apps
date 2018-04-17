
package com.cloudminds.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkState {
    public interface OnNetworkStateListener {
        void onNetworkState(boolean state);
    }

    public static final int ALLOW_UNKNOWN = 1;
    public static final int ALLOW_2G = 2;
    public static final int ALLOW_3G = 4;
    public static final int ALLOW_4G = 8;
    public static final int ALLOW_WIFI = 16;
    public static final int ALLOW_ETHERNET = 32;

    private Context mContext = null;
    private OnNetworkStateListener mOnNetworkStateListener = null;
    private ConnectivityManager mConnectivityManager = null;
    private volatile Boolean mStateLast = null;
    private boolean mConnected;

    private int flags = ALLOW_WIFI | ALLOW_ETHERNET;

    private IntentFilter mFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

    private boolean haveFlag(int flag) {
        return ((flags & flag) == flag);
    }

    private void updateState() {
        if (mOnNetworkStateListener != null) {
            NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

            boolean state = false;
            mConnected = (info != null) && info.isConnected();
            if (mConnected) {
                // My definitions of 2G/3G/4G may not match yours... :)
                // Speed estimates courtesy (c) 2013 the internets
                switch (info.getType()) {
                    case ConnectivityManager.TYPE_MOBILE:
                        switch (info.getSubtype()) {
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                                // 2G ~ 50-100 kbps
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                                // 2G ~ 14-64 kbps
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                                // 2G ~ 50-100 kbps
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                                // 2G ~ 100 kbps *
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                // 2G ~ 25 kbps
                                state = haveFlag(ALLOW_2G);
                                break;
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                                // 3G ~ 1-2 Mbps
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                                // 3G ~ 400-1000 kbps
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                                // 3G ~ 600-1400 kbps
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                // 3G ~ 5 Mbps
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                                // 3G ~ 2-14 Mbps
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                                // 3G ~ 700-1700 kbps
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                                // 3G ~ 1-23 Mbps *
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                                // 3G ~ 400-7000 kbps
                                state = haveFlag(ALLOW_3G);
                                break;
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                                // 4G ~ 10-20 Mbps
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                // 4G ~ 10+ Mbps
                                state = haveFlag(ALLOW_4G);
                                break;
                            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            default:
                                state = haveFlag(ALLOW_UNKNOWN);
                                break;
                        }
                        break;
                    case ConnectivityManager.TYPE_WIFI:
                        state = haveFlag(ALLOW_WIFI);
                        break;
                    case ConnectivityManager.TYPE_ETHERNET:
                        state = haveFlag(ALLOW_ETHERNET);
                        break;
                    case ConnectivityManager.TYPE_WIMAX:
                        // 4G
                        state = haveFlag(ALLOW_4G);
                        break;
                    default:
                        state = haveFlag(ALLOW_UNKNOWN);
                        break;
                }
            }

            if ((mStateLast == null) || (mStateLast != state)) {
                mStateLast = state;
                mOnNetworkStateListener.onNetworkState(state);
            }
        }
    }

    public boolean start(Context context, OnNetworkStateListener onNetworkStateListener, int flags) {
        if (mContext == null) {
            mContext = context;
            mOnNetworkStateListener = onNetworkStateListener;
            updateFlags(flags);
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            mContext.registerReceiver(mReceiver, mFilter);
            updateState();
            return true;
        }
        return false;
    }

    public boolean stop() {
        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
            mOnNetworkStateListener = null;
            mConnectivityManager = null;
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

    public void updateFlags(int newFlags) {
        flags = newFlags;
        Logger.d("networkstate flags --> %d", newFlags);
        if (mConnectivityManager != null)
            updateState();
    }

    public boolean isConnected() {
        return mConnected;
    }
}

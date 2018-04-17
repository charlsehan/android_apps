
package com.cloudminds.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.cloudminds.updater.ScreenState.OnScreenStateListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Scheduler
        implements
        OnScreenStateListener
{
    public interface OnWantUpdateCheckListener {
        boolean onWantUpdateCheck(boolean checkOnly);
    }

    private static final String PREF_LAST_CHECK_ATTEMPT_TIME_NAME = "last_check_attempt_time";
    private static final long PREF_LAST_CHECK_ATTEMPT_TIME_DEFAULT = 0L;

    private static final long CHECK_THRESHOLD_MS = 6 * AlarmManager.INTERVAL_HOUR;
    private static final long ALARM_INTERVAL_START = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long ALARM_INTERVAL_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;
    private static final long ALARM_SECONDARY_WAKEUP_TIME = 3 * AlarmManager.INTERVAL_HOUR;
    private static final long ALARM_DETECT_SLEEP_TIME = (5 * AlarmManager.INTERVAL_HOUR) + AlarmManager.INTERVAL_HALF_HOUR;

    private OnWantUpdateCheckListener mOnWantUpdateCheckListener = null;
    private AlarmManager mAlarmManager = null;
    private SharedPreferences mPrefs = null;

    private PendingIntent mAlarmInterval = null;
    private PendingIntent mAlarmSecondaryWake = null;
    private PendingIntent mAlarmDetectSleep = null;

    private SimpleDateFormat mSdfLog = (new SimpleDateFormat("HH:mm", Locale.ENGLISH));

    public Scheduler(Context context, OnWantUpdateCheckListener onWantUpdateCheckListener) {
        mOnWantUpdateCheckListener = onWantUpdateCheckListener;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        mAlarmInterval = UpdateService.alarmPending(context, 1);
        mAlarmSecondaryWake = UpdateService.alarmPending(context, 2);
        mAlarmDetectSleep = UpdateService.alarmPending(context, 3);

        Logger.i("Setting repeating alarm (inexact) for %s",
                mSdfLog.format(new Date(System.currentTimeMillis() + ALARM_INTERVAL_START)));
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + ALARM_INTERVAL_START,
                ALARM_INTERVAL_INTERVAL,
                mAlarmInterval
        );

        setSecondaryWakeAlarm();
    }

    private void setSecondaryWakeAlarm() {
        Logger.i("Setting secondary wakeup alarm (inexact) for %s",
                mSdfLog.format(new Date(System.currentTimeMillis() + ALARM_SECONDARY_WAKEUP_TIME)));
        mAlarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + ALARM_SECONDARY_WAKEUP_TIME,
                mAlarmSecondaryWake
        );
    }

    private void cancelSecondaryWakeAlarm() {
        Logger.i("Cancelling secondary wakeup alarm");
        mAlarmManager.cancel(mAlarmSecondaryWake);
    }

    private void setDetectSleepAlarm() {
        Logger.i("Setting sleep detection alarm (exact) for %s",
                mSdfLog.format(new Date(System.currentTimeMillis() + ALARM_DETECT_SLEEP_TIME)));
        mAlarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + ALARM_DETECT_SLEEP_TIME,
                mAlarmDetectSleep
        );
    }

    private void cancelDetectSleepAlarm() {
        Logger.i("Cancelling sleep detection alarm");
        mAlarmManager.cancel(mAlarmDetectSleep);
    }

    @Override
    public void onScreenState(boolean state) {
        if (!state) {
            setDetectSleepAlarm();
        } else {
            cancelDetectSleepAlarm();
        }
    }

    private boolean checkForUpdates(boolean force) {
        // Using abs here in case user changes date/time
        long lastCheckMillis = mPrefs.getLong(PREF_LAST_CHECK_ATTEMPT_TIME_NAME, PREF_LAST_CHECK_ATTEMPT_TIME_DEFAULT);
        Logger.i("Last check time %s", mSdfLog.format(new Date(lastCheckMillis)));
        if (force || (Math.abs(System.currentTimeMillis() - lastCheckMillis) > CHECK_THRESHOLD_MS)) {
            if (mOnWantUpdateCheckListener != null) {
                if (mOnWantUpdateCheckListener.onWantUpdateCheck(!force/*checkOnly*/)) {
                    mPrefs.edit().putLong(PREF_LAST_CHECK_ATTEMPT_TIME_NAME, System.currentTimeMillis()).commit();
                    return true;
                }
            }
        }
        return false;
    }

    public void alarm(int id) {
        switch (id) {
            case 1:
                // This is the interval alarm, called only if the device is
                // already awake for some reason. Might as well see if
                // conditions match to check for updates, right ?
                Logger.i("Interval alarm fired");
                checkForUpdates(false);
                break;

            case 2:
                // Fallback alarm. Our interval alarm has not been called for
                // several hours. The device might have been woken up just
                // for us. Let's see if conditions are good to check for
                // updates.
                Logger.i("Secondary alarm fired");
                checkForUpdates(false);
                break;

            case 3:
                // The screen has been off for 5:30 hours, with luck we've
                // caught the user asleep and we'll have a fresh build waiting
                // when (s)he wakes!
                Logger.i("Sleep detection alarm fired");
                checkForUpdates(true);
                break;
        }

        // Reset fallback wakeup command, we don't need to be called for another
        // few hours
        cancelSecondaryWakeAlarm();
        setSecondaryWakeAlarm();
    }
}

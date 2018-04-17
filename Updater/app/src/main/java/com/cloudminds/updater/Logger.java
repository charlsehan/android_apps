
package com.cloudminds.updater;

import java.util.Locale;

public class Logger {
    private final static String LOG_TAG = "CloudmindsUpdater";
    
    private static boolean mDebug = false;

    public static void setDebugLogging(boolean enabled) {
        mDebug = enabled;
    }

    public static void d(String message, Object... args) {
        if (mDebug)
            android.util.Log.d(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }

    public static void ex(Exception e) {
        if (mDebug)
            android.util.Log.w(LOG_TAG, e);
    }

    public static void i(String message, Object... args) {
        android.util.Log.i(LOG_TAG, String.format(Locale.ENGLISH, message, args));
    }
}

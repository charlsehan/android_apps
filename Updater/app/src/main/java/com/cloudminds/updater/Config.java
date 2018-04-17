
package com.cloudminds.updater;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

public class Config {
    private static Config sInstance = null;

    public static Config getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Config(context.getApplicationContext());
        }
        return sInstance;
    }

    private Context mContext;

    private final String mCurrentVersion;
    private final String mDeviceName;
    private final String mPathBase;
    private final String mDataInfoUrl;
    private final boolean mKeepScreenOn;

    /*
     * Using reflection voodoo instead calling the hidden class directly, to
     * dev/test outside of AOSP tree
     */
    private String getProperty(Context context, String key, String defValue) {
        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(
                    "android.os.SystemProperties");
            Method get = SystemProperties.getMethod("get", new Class[] {
                    String.class, String.class
            });
            return (String) get.invoke(null, new Object[] {
                    key, defValue
            });
        } catch (Exception e) {
            // A lot of voodoo could go wrong here, return failure instead of
            // crash
            Logger.ex(e);
        }
        return null;
    }

    private Config(Context context) {
        mContext = context;
        Resources res = context.getResources();

        mCurrentVersion = "E601.1.73_V03.03_r2.1.04026.1_G3061_160421_1049";//getProperty(context, res.getString(R.string.property_version), "");
        mDeviceName = getProperty(context, res.getString(R.string.property_device), "");

        mPathBase = String.format(Locale.ENGLISH, "%s%s%s%s",
                Environment.getExternalStorageDirectory().getAbsolutePath(),
                File.separator,
                res.getString(R.string.path_base),
                File.separator);
        mDataInfoUrl = String.format(Locale.ENGLISH, res.getString(R.string.data_info_url), mCurrentVersion);

        boolean keepOn = false;
        try {
            String[] devices = res.getStringArray(R.array.keep_screen_on_devices);
            if (devices != null) {
                for (String device : devices) {
                    if (mDeviceName.equals(device)) {
                        keepOn = true;
                        break;
                    }
                }
            }
        } catch (Resources.NotFoundException e) {
        }
        mKeepScreenOn = keepOn;

        Logger.d("propertyVersion: %s", mCurrentVersion);
        Logger.d("propertyDevice: %s", mDeviceName);
        Logger.d("mPathBase: %s", mPathBase);
        Logger.d("mDataInfoUrl: %s", mDataInfoUrl);
        Logger.d("mKeepScreenOn: %d", mKeepScreenOn ? 1 : 0);
    }

    public String getCurrentVersion() {
        return mCurrentVersion;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getPathBase() {
        return mPathBase;
    }

    public String getDataInfoUrl() {
        return mDataInfoUrl;
    }

    public String getPackageUrl(String token, String path) {
        String packageUrl = String.format(Locale.ENGLISH, mContext.getString(R.string.package_url),
                token, path);
        return packageUrl;
    }

    public boolean getKeepScreenOn() {
        return mKeepScreenOn;
    }
}
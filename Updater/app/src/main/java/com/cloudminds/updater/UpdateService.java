
package com.cloudminds.updater;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cloudminds.updater.BatteryState.OnBatteryStateListener;
import com.cloudminds.updater.NetworkState.OnNetworkStateListener;
import com.cloudminds.updater.Scheduler.OnWantUpdateCheckListener;
import com.cloudminds.updater.ScreenState.OnScreenStateListener;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class UpdateService extends Service implements
        OnNetworkStateListener,
        OnBatteryStateListener,
        OnScreenStateListener,
        OnWantUpdateCheckListener,
        OnSharedPreferenceChangeListener {
    private static final int HTTP_READ_TIMEOUT = 30000;
    private static final int HTTP_CONNECTION_TIMEOUT = 30000;

    public static void start(Context context) {
        start(context, null);
    }

    public static void startCheck(Context context) {
        start(context, ACTION_CHECK);
    }

    public static void startDownload(Context context) {
        start(context, ACTION_DOWNLOAD);
    }

    public static void startFlash(Context context) {
        start(context, ACTION_FLASH);
    }

    private static void start(Context context, String action) {
        Intent i = new Intent(context, UpdateService.class);
        i.setAction(action);
        context.startService(i);
    }

    public static PendingIntent alarmPending(Context context, int id) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtra(EXTRA_ALARM_ID, id);
        return PendingIntent.getService(context, id, intent, 0);
    }

    public static final String ACTION_SYSTEM_UPDATE_SETTINGS = "android.settings.SYSTEM_UPDATE_SETTINGS";
    public static final String PERMISSION_ACCESS_CACHE_FILESYSTEM = "android.permission.ACCESS_CACHE_FILESYSTEM";
    public static final String PERMISSION_REBOOT = "android.permission.REBOOT";

    public static final String BROADCAST_INTENT = "com.cloudminds.updater.intent.BROADCAST_STATE";
    public static final String EXTRA_STATE = "com.cloudminds.updater.extra.ACTION_STATE";
    public static final String EXTRA_PROGRESS = "com.cloudminds.updater.extra.PROGRESS";
    public static final String EXTRA_CURRENT = "com.cloudminds.updater.extra.CURRENT";
    public static final String EXTRA_TOTAL = "com.cloudminds.updater.extra.TOTAL";
    public static final String EXTRA_FILE_NAME = "com.cloudminds.updater.extra.FILE_NAME";
    public static final String EXTRA_MS = "com.cloudminds.updater.extra.MS";

    public static final String STATE_ACTION_NONE = "action_none";
    public static final String STATE_ACTION_CHECKING = "action_checking";
    public static final String STATE_ACTION_AVAILABEL = "action_available";
    public static final String STATE_ACTION_DOWNLOADING = "action_downloading";
    public static final String STATE_ACTION_CHECKING_MD5 = "action_checking_md5";
    public static final String STATE_ACTION_READY = "action_ready";
    public static final String STATE_ACTION_INSTALLING = "action_installing";
    public static final String STATE_ERROR_DISK_SPACE = "error_disk_space";
    public static final String STATE_ERROR_DOWNLOAD = "error_download";
    public static final String STATE_ERROR_CONNECTION = "error_connection";
    public static final String STATE_ERROR_FILE_NOT_EXIST = "error_file_not_exist";
    public static final String STATE_ERROR_VERIFICATION_FAIL = "error_verification_fail";
    public static final String STATE_ERROR_UNKNOWN = "error_unknown";

    private static final String ACTION_CHECK = "com.cloudminds.updater.action.CHECK";
    private static final String ACTION_DOWNLOAD = "com.cloudminds.updater.action.DOWNLOAD";
    private static final String ACTION_FLASH = "com.cloudminds.updater.action.FLASH";
    private static final String ACTION_ALARM = "com.cloudminds.updater.action.ALARM";
    private static final String EXTRA_ALARM_ID = "com.cloudminds.updater.extra.ALARM_ID";
    private static final String ACTION_NOTIFICATION_DELETED = "com.cloudminds.updater.action.NOTIFICATION_DELETED";

    private static final int NOTIFICATION_BUSY = 1;
    private static final int NOTIFICATION_UPDATE = 2;
    private static final int NOTIFICATION_ERROR = 3;

    public static final String PREF_AVAILABEL_VERSION_NAME = "availabel_version_name";
    public static final String PREF_AVAILABEL_VERSION_NAME_DEFAULT = null;

    public static final String PREF_AVAILABEL_VERSION_INFO = "availabel_version_info";
    public static final String PREF_AVAILABEL_VERSION_INFO_DEFAULT = null;

    public static final String PREF_AVAILABEL_FILE_SIZE = "availabel_file_size";
    public static final long PREF_AVAILABEL_FILE_SIZE_DEFAULT = -1L;

    private static final String PREF_READY_FILE_NAME = "ready_file_name";
    private static final String PREF_READY_FILE_NAME_DEFAULT = null;

    private static final String PREF_LAST_CHECK_TIME = "last_check_time";
    private static final long PREF_LAST_CHECK_TIME_DEFAULT = 0L;

    private static final String PREF_LAST_SNOOZE_TIME = "last_snooze_time";
    private static final long PREF_LAST_SNOOZE_TIME_DEFAULT = 0L;

    private static final long SNOOZE_MS = 24 * AlarmManager.INTERVAL_HOUR;

    public static final String PREF_AUTO_UPDATE_NETWORKS_NAME = "auto_update_networks";
    public static final int PREF_AUTO_UPDATE_NETWORKS_DEFAULT = NetworkState.ALLOW_WIFI
            | NetworkState.ALLOW_ETHERNET;

    public static final String PREF_STOP_DOWNLOAD = "stop_download";

    public static boolean isProgressState(String state) {
        return state.equals(STATE_ACTION_CHECKING) ||
                state.equals(STATE_ACTION_DOWNLOADING) ||
                state.equals(STATE_ACTION_CHECKING_MD5) ||
                state.equals(STATE_ACTION_INSTALLING);
    }

    public static boolean isErrorState(String state) {
        if (state.equals(STATE_ERROR_DISK_SPACE) ||
                state.equals(STATE_ERROR_DOWNLOAD) ||
                state.equals(STATE_ERROR_CONNECTION) ||
                state.equals(STATE_ERROR_FILE_NOT_EXIST) ||
                state.equals(STATE_ERROR_VERIFICATION_FAIL) ||
                state.equals(STATE_ERROR_UNKNOWN)) {
            return true;
        }
        return false;
    }
    private Config mConfig;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private String mState = STATE_ACTION_NONE;

    private NetworkState mNetworkState = null;
    private BatteryState mBatteryState = null;
    private ScreenState mScreenState = null;

    private Scheduler mScheduler = null;

    private PowerManager.WakeLock mWakeLock = null;
    private WifiManager.WifiLock mWifiLock = null;

    private NotificationManager mNotificationManager = null;
    private SharedPreferences mPrefs = null;
    private boolean mStopDownload;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        mConfig = Config.getInstance(this);

        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                mConfig.getKeepScreenOn() ?
                        PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP :
                        PowerManager.PARTIAL_WAKE_LOCK, "Updater WakeLock");
        mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(
                WifiManager.WIFI_MODE_FULL, "Updater WifiLock");

        mHandlerThread = new HandlerThread("Updater Service Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mScheduler = new Scheduler(this, this);

        mNetworkState = new NetworkState();
        mNetworkState.start(this, this,
                mPrefs.getInt(PREF_AUTO_UPDATE_NETWORKS_NAME, PREF_AUTO_UPDATE_NETWORKS_DEFAULT));

        mBatteryState = new BatteryState();
        mBatteryState.start(this, this, 50, true);

        mScreenState = new ScreenState();
        mScreenState.start(this, this);

        mPrefs.registerOnSharedPreferenceChangeListener(this);

        autoState(false);
    }

    @Override
    public void onDestroy() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        mNetworkState.stop();
        mBatteryState.stop();
        mScreenState.stop();
        mHandlerThread.quitSafely();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_CHECK.equals(intent.getAction())) {
                checkForUpdates(true, true);
            } else if (ACTION_DOWNLOAD.equals(intent.getAction())) {
                checkForUpdates(true, false);
            } else if (ACTION_FLASH.equals(intent.getAction())) {
                flashUpdate();
            } else if (ACTION_ALARM.equals(intent.getAction())) {
                mScheduler.alarm(intent.getIntExtra(EXTRA_ALARM_ID, -1));
            } else if (ACTION_NOTIFICATION_DELETED.equals(intent.getAction())) {
                Logger.i("Snoozing for 24 hours");
                mPrefs.edit().putLong(PREF_LAST_SNOOZE_TIME, System.currentTimeMillis())
                        .commit();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onNetworkState(boolean state) {
        Logger.d("network state --> %d", state ? 1 : 0);
    }

    @Override
    public void onBatteryState(boolean state) {
        Logger.d("battery state --> %d", state ? 1 : 0);
    }

    @Override
    public void onScreenState(boolean state) {
        Logger.d("screen state --> %d", state ? 1 : 0);
        mScheduler.onScreenState(state);
    }

    @Override
    public boolean onWantUpdateCheck(boolean checkOnly) {
        Logger.i("Scheduler requests check for updates");
        return checkForUpdates(false, checkOnly);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_AUTO_UPDATE_NETWORKS_NAME.equals(key)) {
            mNetworkState.updateFlags(sharedPreferences.getInt(PREF_AUTO_UPDATE_NETWORKS_NAME,
                    PREF_AUTO_UPDATE_NETWORKS_DEFAULT));
        } else if (PREF_STOP_DOWNLOAD.equals(key)) {
            mStopDownload = true;
        }
    }

    private boolean checkForUpdates(boolean userInitiated, boolean checkOnly) {
        /*
         * Unless the user is specifically asking to check for updates, we only
         * check for them if we have a connection matching the user's set
         * preferences, we're charging and/or have juice aplenty, and the screen
         * is off
         */

        if ((mNetworkState == null) ||
                (mBatteryState == null) ||
                (mScreenState == null))
            return false;

        if (!mNetworkState.isConnected()) {
            updateState(STATE_ERROR_CONNECTION);
            Logger.i("Ignoring request to check for updates - no data connection");
            return false;
        }

        if (!isProgressState(mState) &&
                (userInitiated || (mNetworkState.getState() && mBatteryState.getState() && !mScreenState.getState()))) {
            Logger.i("Starting check for updates");
            checkForUpdatesAsync(userInitiated, checkOnly);
            return true;
        } else {
            Logger.i("Ignoring request to check for updates");
        }
        return false;
    }

    private void checkForUpdatesAsync(final boolean userInitiated, final boolean checkOnly) {
        updateState(STATE_ACTION_CHECKING);
        mWakeLock.acquire();
        mWifiLock.acquire();

        stopNotification();
        stopErrorNotification();

        Notification notification = (new Notification.Builder(this)).
                setSmallIcon(R.drawable.stat_notify_update).
                setContentTitle(getString(R.string.title)).
                setContentText(getString(checkOnly ? R.string.notify_checking : R.string.state_action_downloading)).
                setShowWhen(false).
                setContentIntent(getNotificationIntent(false)).
                build();
        startForeground(NOTIFICATION_BUSY, notification);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean force = userInitiated;
                mStopDownload = false;

                try {
                    mPrefs.edit().putLong(PREF_LAST_CHECK_TIME, System.currentTimeMillis()).commit();
                    clearInfoPrefs();

                    DataInfo info = getUpdatePackageInfo(mConfig.getDataInfoUrl());
                    if (info == null) {
                        return;
                    }
                    if (!TextUtils.isEmpty(info.getErrorCode())) {
                        Logger.d("ERROR %s: %s", info.getErrorCode(), info.getErrorDescription());
                        return;
                    }

                    saveAvailabelVersionPrefs(info.getVersionName(), info.getVersionDescription(), info.getPackageFileSize());
                    if (checkOnly) {
                        return;
                    }

                    final File folder = new File(mConfig.getPathBase());
                    folder.mkdirs();

                    final String fetchUrl = mConfig.getPackageUrl(info.getToken(), info.getPackageFilePath());
                    final String saveFileName = mConfig.getPathBase() + info.getPackageFileName();
                    final File f = new File(saveFileName);

                    final boolean checkMd5WhenDownload = true;
                    final boolean fileAlreadyExist = f.exists();

                    if (fileAlreadyExist) {
                        Logger.d("file already exist: %s", saveFileName);
                    } else {
                        for (File tempFile : folder.listFiles()) {
                            tempFile.delete();
                        }
                        if (!downloadUpdatePackageFile(fetchUrl, f, info, force, checkMd5WhenDownload)) {
                            return;
                        }
                    }

                    if (fileAlreadyExist || !checkMd5WhenDownload) {
                        Logger.d("start md5 verification");
                        DataInfo.ProgressListener progressListener =
                                getProgressListener(STATE_ACTION_CHECKING_MD5, info.getPackageFileName());
                        if (!info.check(f, progressListener)) {
                            f.delete();
                            updateState(STATE_ERROR_VERIFICATION_FAIL);
                            Logger.d("md5 verification error");
                            return;
                        }
                    }

                    saveReadyFilePref(saveFileName);
                } finally {
                    stopForeground(true);
                    if (mWifiLock.isHeld()) mWifiLock.release();
                    if (mWakeLock.isHeld()) mWakeLock.release();
                    if (isErrorState(mState)) {
                        clearInfoPrefs();
                        if (!userInitiated) {
                            startErrorNotification();
                        }
                    } else {
                        autoState(userInitiated);
                    }
                }
            }
        });
    }

    private DataInfo getUpdatePackageInfo(String url) {
        DataInfo info = null;
        try {
            info = new DataInfo(downloadUrlMemoryAsString(url));
        } catch (JSONException e) {
            // There's an error in the JSON. Could be bad JSON,
            // could be a 404 text, etc
            Logger.ex(e);
        } catch (NullPointerException e) {
            // Download failed
            Logger.ex(e);
        }
        return info;
    }

    private boolean downloadUpdatePackageFile(String url, File f, DataInfo info, boolean force, boolean checkMD5) {
        updateState(STATE_ACTION_DOWNLOADING, 0f, 0L, 100L, null, null);
        Logger.d("download: %s --> %s", url, f.getAbsolutePath());

        long requiredSpace = sizeOnDisk(info.getPackageFileSize());
        long freeSpace = (new StatFs(mConfig.getPathBase())).getAvailableBytes();
        if (freeSpace < requiredSpace) {
            updateState(STATE_ERROR_DISK_SPACE, null, freeSpace, requiredSpace, null, null);
            Logger.d("not enough space!");
            return false;
        }

        DataInfo.ProgressListener progressListener =
                getProgressListener(STATE_ACTION_DOWNLOADING, info.getPackageFileName());
        if (!downloadUrlFile(url, f, info.getPackageFileSize(), checkMD5 ? info.getPackageFileMd5() : null, progressListener)) {
            Logger.d("download failed");
            f.delete();
            updateState(STATE_ERROR_DOWNLOAD);
            return false;
        }

        Logger.d("download success");
        updateState(STATE_ACTION_DOWNLOADING, 100f, 100L, 100L, null, null);

        return true;
    }

    private HttpURLConnection setupHttpRequest(String urlStr){
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                Logger.d("response: %d", code);
                return null;
            }
            return urlConnection;
        } catch (Exception e) {
            Logger.i("Failed to connect to server");
            return null;
        }
    }

    private byte[] downloadUrlMemory(String url) {
        Logger.d("download: %s", url);

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = setupHttpRequest(url);
            if(urlConnection == null) {
                return null;
            }

            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            int byteInt;

            while((byteInt = is.read()) >= 0){
                byteArray.write(byteInt);
            }

            return byteArray.toByteArray();

        } catch (Exception e) {
            // Download failed for any number of reasons, timeouts, connection
            // drops, etc. Just log it in debugging mode.
            Logger.ex(e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String downloadUrlMemoryAsString(String url) {
        try {
            byte[] bytes = downloadUrlMemory(url);
            if(bytes == null){
                return null;
            }
            String responseBody = new String(bytes, StandardCharsets.UTF_8);
            return responseBody;
        } catch (Exception e) {
            Logger.ex(e);
            return null;
        }
    }

    private boolean downloadUrlFile(String url, File f, long fileSize, String matchMD5,
                                    DataInfo.ProgressListener progressListener) {
        Logger.d("download: %s", url);

        HttpURLConnection urlConnection = null;
        MessageDigest digest = null;
        if (matchMD5 != null) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                // No MD5 algorithm support
                Logger.ex(e);
            }
        }

        if (f.exists())
            f.delete();

        try {
            urlConnection = setupHttpRequest(url);
            if(urlConnection == null){
                return false;
            }
            long len = urlConnection.getContentLength();
            Logger.d("response content len: %d", len);
            Logger.d("info fileSize: %d", fileSize);
            if (len < 0) {
                len = fileSize;
            }
            long recv = 0;
            if ((len > 0) && (len < 4L * 1024L * 1024L * 1024L)) {
                byte[] buffer = new byte[262144];

                InputStream is = urlConnection.getInputStream();
                FileOutputStream os = new FileOutputStream(f, false);
                try {
                    int r;
                    while ((r = is.read(buffer)) > 0) {
                        if (mStopDownload) {
                            return false;
                        }
                        os.write(buffer, 0, r);
                        if (digest != null)
                            digest.update(buffer, 0, r);

                        recv += (long) r;
                        if (progressListener != null)
                            progressListener.onProgress(
                                    ((float) recv / (float) len) * 100f, recv,
                                    len);
                    }
                } finally {
                    os.close();
                }
                Logger.d("download file done, recv: %d", recv);

                if (digest != null) {
                    String MD5 = new BigInteger(1, digest.digest())
                            .toString(16).toLowerCase(Locale.ENGLISH);
                    while (MD5.length() < 32)
                        MD5 = "0" + MD5;
                    Logger.d("check md5, expected: %s, reality: %s", matchMD5, MD5);
                    boolean md5Check = MD5.equals(matchMD5);
                    if (!md5Check) {
                        Logger.i("MD5 check failed for " + url);
                    }
                    return md5Check;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            // Download failed for any number of reasons, timeouts, connection
            // drops, etc. Just log it in debugging mode.
            Logger.ex(e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void flashUpdate() {
        updateState(STATE_ACTION_INSTALLING);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getPackageManager().checkPermission(PERMISSION_ACCESS_CACHE_FILESYSTEM,
                            getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                        Logger.d("[%s] required beyond this point", PERMISSION_ACCESS_CACHE_FILESYSTEM);
                        return;
                    }

                    if (getPackageManager().checkPermission(PERMISSION_REBOOT, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                        Logger.d("[%s] required beyond this point", PERMISSION_REBOOT);
                        return;
                    }

                    String flashFilename = mPrefs.getString(PREF_READY_FILE_NAME, PREF_READY_FILE_NAME_DEFAULT);
                    clearInfoPrefs();

                    Logger.d("flashFilename: %s", flashFilename);
                    if ((flashFilename == null) || !flashFilename.startsWith(mConfig.getPathBase()))
                        return;

                    File f = new File(flashFilename);
                    if (!f.exists()) {
                        Logger.d("file not exist!");
                        updateState(STATE_ERROR_FILE_NOT_EXIST);
                        return;
                    }

                    try {
                        String newPath = flashFilename;
                        if (newPath.contains("/storage/sdcard1/")) {
                            newPath = newPath.replace("/storage/sdcard1/", "/sdcard/");
                        } else {
                            newPath = newPath.replace("/storage/emulated/0/", "/data/media/0/");
                        }
                        Logger.d("newPath: %s", newPath);

                        Method newInstallPackage = RecoverySystem.class.getMethod("newInstallPackage", new Class[] {
                                Context.class, File.class
                        });
                        newInstallPackage.invoke(null, new Object[] {
                                getApplicationContext(), new File(newPath)
                        });
                    } catch (Exception e) {
                        Logger.ex(e);
                    }
                } finally {
                    if (!isErrorState(mState)) {
                        autoState(true);
                    }
                }
            }
        });
    }

    private void autoState(boolean userInitiated) {
        Logger.d("autoState mState = " + mState + ", userInitiated = " + userInitiated);

        if (isErrorState(mState)) {
            return;
        }

        String filename = mPrefs.getString(PREF_READY_FILE_NAME, PREF_READY_FILE_NAME_DEFAULT);
        String versionName = mPrefs.getString(PREF_AVAILABEL_VERSION_NAME, PREF_AVAILABEL_VERSION_NAME_DEFAULT);
        Logger.i("file name: %s", filename);
        Logger.i("version name: %s", versionName);

        if (filename != null) {
            if (!(new File(filename)).exists()) {
                filename = null;
            }
        }

        if (filename == null) {
            if (versionName == null) {
                Logger.i("System up to date");
                updateState(STATE_ACTION_NONE, null, null, null, null,
                        mPrefs.getLong(PREF_LAST_CHECK_TIME, PREF_LAST_CHECK_TIME_DEFAULT));
            } else {
                Logger.i("New version is available: %s", versionName);
                updateState(STATE_ACTION_AVAILABEL, null, null, null, null,
                        mPrefs.getLong(PREF_LAST_CHECK_TIME, PREF_LAST_CHECK_TIME_DEFAULT));

                if (!userInitiated) {
                    // check if we're snoozed, using abs for clock changes
                    if (Math.abs(System.currentTimeMillis()
                            - mPrefs.getLong(PREF_LAST_SNOOZE_TIME, PREF_LAST_SNOOZE_TIME_DEFAULT)) > SNOOZE_MS) {
                        startNotification();
                    } else {
                        Logger.d("notification snoozed");
                    }
                }
            }
        } else {
            Logger.i("Update found: %s", filename);
            updateState(STATE_ACTION_READY, null, null, null, (new File(filename)).getName(),
                    mPrefs.getLong(PREF_LAST_CHECK_TIME, PREF_LAST_CHECK_TIME_DEFAULT));

            if (!userInitiated) {
                // check if we're snoozed, using abs for clock changes
                if (Math.abs(System.currentTimeMillis()
                        - mPrefs.getLong(PREF_LAST_SNOOZE_TIME, PREF_LAST_SNOOZE_TIME_DEFAULT)) > SNOOZE_MS) {
                    startNotification();
                } else {
                    Logger.d("notification snoozed");
                }
            }
        }
    }

    private synchronized void updateState(String state) {
        updateState(state, null, null, null, null, null);
    }

    private synchronized void updateState(String state, Float progress, Long current, Long total, String filename, Long ms) {
        mState = state;

        Intent i = new Intent(BROADCAST_INTENT);
        i.putExtra(EXTRA_STATE, state);
        if (progress != null)
            i.putExtra(EXTRA_PROGRESS, progress);
        if (current != null)
            i.putExtra(EXTRA_CURRENT, current);
        if (total != null)
            i.putExtra(EXTRA_TOTAL, total);
        if (filename != null)
            i.putExtra(EXTRA_FILE_NAME, filename);
        if (ms != null)
            i.putExtra(EXTRA_MS, ms);
        sendStickyBroadcast(i);
    }

    private void clearInfoPrefs() {
        mPrefs.edit().putString(PREF_AVAILABEL_VERSION_NAME, PREF_AVAILABEL_VERSION_NAME_DEFAULT).commit();
        mPrefs.edit().putString(PREF_AVAILABEL_VERSION_INFO, PREF_AVAILABEL_VERSION_INFO_DEFAULT).commit();
        mPrefs.edit().putLong(PREF_AVAILABEL_FILE_SIZE, PREF_AVAILABEL_FILE_SIZE_DEFAULT).commit();
        mPrefs.edit().putString(PREF_READY_FILE_NAME, PREF_READY_FILE_NAME_DEFAULT).commit();
    }

    private void saveAvailabelVersionPrefs(String versionName, String versionInfo, long fileSize) {
        mPrefs.edit().putString(PREF_AVAILABEL_VERSION_NAME, versionName).commit();
        mPrefs.edit().putString(PREF_AVAILABEL_VERSION_INFO, versionInfo).commit();
        mPrefs.edit().putLong(PREF_AVAILABEL_FILE_SIZE, fileSize).commit();
    }

    private void saveReadyFilePref(String fileName) {
        mPrefs.edit().putString(PREF_READY_FILE_NAME, fileName).commit();
    }

    private DataInfo.ProgressListener getProgressListener(String state, String fileName) {
        final long[] last = new long[] {
                0, SystemClock.elapsedRealtime()
        };
        final String _state = state;
        final String _filename = fileName;

        return new DataInfo.ProgressListener() {
            @Override
            public void onProgress(float progress, long current, long total) {
                long now = SystemClock.elapsedRealtime();
                if (now >= last[0] + 16L) {
                    updateState(_state, progress, current, total, _filename,
                            SystemClock.elapsedRealtime() - last[1]);
                    last[0] = now;
                }
            }
        };
    }

    private PendingIntent getNotificationIntent(boolean delete) {
        if (delete) {
            Intent notificationIntent = new Intent(this, UpdateService.class);
            notificationIntent.setAction(ACTION_NOTIFICATION_DELETED);
            return PendingIntent.getService(this, 0, notificationIntent, 0);
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(ACTION_SYSTEM_UPDATE_SETTINGS);
            return PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }
    }

    private void startNotification() {
        String flashFilename = mPrefs.getString(PREF_READY_FILE_NAME, PREF_READY_FILE_NAME_DEFAULT);
        String newVersionName = mPrefs.getString(PREF_AVAILABEL_VERSION_NAME, PREF_AVAILABEL_VERSION_NAME_DEFAULT);

        final boolean readyToFlash = flashFilename != PREF_READY_FILE_NAME_DEFAULT;
        if (readyToFlash) {
            flashFilename = new File(flashFilename).getName();
            flashFilename = flashFilename.substring(0, flashFilename.lastIndexOf('.'));
        }

        mNotificationManager.notify(
                NOTIFICATION_UPDATE,
                (new Notification.Builder(this)).
                setSmallIcon(R.drawable.stat_notify_update).
                setContentTitle(readyToFlash ? getString(R.string.notify_title_flash) : getString(R.string.notify_title_download)).
                setContentText(readyToFlash ? flashFilename : newVersionName).
                setShowWhen(true).
                setContentIntent(getNotificationIntent(false)).
                setDeleteIntent(getNotificationIntent(true)).
                build());
    }

    private void stopNotification() {
        mNotificationManager.cancel(NOTIFICATION_UPDATE);
    }

    private void startErrorNotification() {
        String errorStateString = null;
        try {
            errorStateString = getString(getResources().getIdentifier(
                    "state_" + mState, "string", getPackageName()));
        } catch (Exception e) {
            // String for this state could not be found (displays empty string)
            Logger.ex(e);
        }
        if (errorStateString != null) {
            mNotificationManager.notify(
                    NOTIFICATION_ERROR,
                    (new Notification.Builder(this))
                    .setSmallIcon(R.drawable.stat_notify_error)
                    .setContentTitle(getString(R.string.notify_title_error))
                    .setContentText(errorStateString)
                    .setShowWhen(true)
                    .setContentIntent(getNotificationIntent(false)).build());
        }
    }

    private void stopErrorNotification() {
        mNotificationManager.cancel(NOTIFICATION_ERROR);
    }

    private long sizeOnDisk(long size) {
        // Assuming 256k block size here, should be future proof for a little
        // bit
        long blocks = (size + 262143L) / 262144L;
        return blocks * 262144L;
    }
}

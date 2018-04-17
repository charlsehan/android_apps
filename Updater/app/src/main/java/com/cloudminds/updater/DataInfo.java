
package com.cloudminds.updater;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class DataInfo {
    public interface ProgressListener {
        void onProgress(float progress, long current, long total);
    }

    private final String mVersionName;
    private final String mVersionDescription;
    private final String mPackageFileName;
    private final String mPackageFileMd5;
    private final long   mPackageFileSize;
    private final String mPackageFilePath;
    private final String mToken;
    private final String mErrorCode;
    private final String mErrorDescription;

    public DataInfo(String json) throws JSONException,
            NullPointerException {
        JSONObject object = new JSONObject(json);

        mErrorCode = object.getString("error");
        if (TextUtils.isEmpty(mErrorCode)) {
            mVersionName        = object.getString("name");
            mVersionDescription = object.getString("info");
            mPackageFileName    = object.getString("packageName");
            mPackageFileMd5     = object.getString("md5");
            mPackageFileSize    = object.getLong("size");
            mPackageFilePath    = object.getString("path");
            mToken              = object.getString("token");
            mErrorDescription   = null;
        } else {
            mVersionName        = null;
            mVersionDescription = null;
            mPackageFileName    = null;
            mPackageFileMd5     = null;
            mPackageFileSize    = 0L;
            mPackageFilePath    = null;
            mToken              = null;
            mErrorDescription   = object.getString("errordescription");
        }

        Logger.d("mVersionName       : %s", mVersionName       );
        Logger.d("mVersionDescription: %s", mVersionDescription != null ?
                                            mVersionDescription.substring(0, 10) : null);
        Logger.d("mPackageFileName   : %s", mPackageFileName   );
        Logger.d("mPackageFileMd5    : %s", mPackageFileMd5    );
        Logger.d("mPackageFileSize   : %d", mPackageFileSize   );
        Logger.d("mPackageFilePath   : %s", mPackageFilePath   );
        Logger.d("mToken             : %s", mToken             );
        Logger.d("mErrorCode         : %s", mErrorCode         );
        Logger.d("mErrorDescription  : %s", mErrorDescription  );
    }

    public String getVersionName() {
        return mVersionName;
    }

    public String getVersionDescription() {
        return mVersionDescription;
    }

    public String getPackageFileName() {
        return mPackageFileName;
    }

    public String getPackageFileMd5() {
        return mPackageFileMd5;
    }

    public long getPackageFileSize() {
        return mPackageFileSize;
    }

    public String getPackageFilePath() {
        return mPackageFilePath;
    }

    public String getToken() {
        return mToken;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public String getErrorDescription() {
        return mErrorDescription;
    }

    public boolean check(File f, ProgressListener progressListener) {
        return f.exists() && f.length() == getPackageFileSize() &&
                getPackageFileMd5().equals(generateFileMD5(f, progressListener));
    }

    private float getProgress(long current, long total) {
        if (total == 0)
            return 0f;
        return ((float) current / (float) total) * 100f;
    }

    private String generateFileMD5(File file, ProgressListener progressListener) {
        String ret = null;

        long current = 0;
        long total = file.length();
        if (progressListener != null)
            progressListener.onProgress(getProgress(current, total), current, total);

        try {
            FileInputStream is = new FileInputStream(file);
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] buffer = new byte[256 * 1024];
                int r;

                while ((r = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, r);
                    current += (long) r;
                    if (progressListener != null)
                        progressListener.onProgress(getProgress(current, total), current, total);
                }

                String MD5 = new BigInteger(1, digest.digest()).
                        toString(16).toLowerCase(Locale.ENGLISH);
                while (MD5.length() < 32)
                    MD5 = "0" + MD5;
                ret = MD5;
            } finally {
                is.close();
            }
        } catch (NoSuchAlgorithmException e) {
            // No MD5 support (returns null)
            Logger.ex(e);
        } catch (FileNotFoundException e) {
            // The MD5 of a non-existing file is null
            Logger.ex(e);
        } catch (IOException e) {
            // Read or close error (returns null)
            Logger.ex(e);
        }

        if (progressListener != null)
            progressListener.onProgress(getProgress(total, total), total, total);

        return ret;
    }
}

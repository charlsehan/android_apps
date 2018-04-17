
package com.cloudminds.updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView mTitle = null;
    private TextView mHeader1 = null;
    private TextView mContent1 = null;
    private TextView mHeader2 = null;
    private TextView mContent2 = null;
    private TextView mHeader3 = null;
    private TextView mContent3 = null;
    private View mProgressContainer = null;
    private ProgressBar mProgressBar = null;
    private TextView mProgressText = null;
    private TextView mProgressText2 = null;
    private TextView mProgressPercent = null;
    private Button mCheckNow = null;
    private Button mDownloadNow = null;
    private Button mFlashNow = null;
    private ImageButton mStopNow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getActionBar().setIcon(getPackageManager().getApplicationIcon("com.android.settings"));
        } catch (NameNotFoundException e) {
            // The standard Settings package is not present, so we can't snatch its icon
            Logger.ex(e);
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);

        UpdateService.start(this);

        setContentView(R.layout.activity_main);

        mTitle = (TextView) findViewById(R.id.text_title);

        mHeader1 = (TextView) findViewById(R.id.text_header1);
        mContent1 = (TextView) findViewById(R.id.text_content1);
        mHeader2 = (TextView) findViewById(R.id.text_header2);
        mContent2 = (TextView) findViewById(R.id.text_content2);

        mHeader3 = (TextView) findViewById(R.id.text_header3);
        mContent3 = (TextView) findViewById(R.id.text_content3);

        mProgressContainer = findViewById(R.id.progress_container);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressText = (TextView) findViewById(R.id.progress_text);
        mProgressText2 = (TextView) findViewById(R.id.progress_text2);
        mProgressPercent = (TextView) findViewById(R.id.progress_percent);

        mCheckNow = (Button) findViewById(R.id.button_check_now);
        mDownloadNow = (Button) findViewById(R.id.button_download_now);
        mFlashNow = (Button) findViewById(R.id.button_flash_now);
        mStopNow = (ImageButton) findViewById(R.id.button_stop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showNetworks() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int flags = prefs.getInt(UpdateService.PREF_AUTO_UPDATE_NETWORKS_NAME,
                UpdateService.PREF_AUTO_UPDATE_NETWORKS_DEFAULT);
        final boolean[] checkedItems = new boolean[] {
                (flags & NetworkState.ALLOW_2G) == NetworkState.ALLOW_2G,
                (flags & NetworkState.ALLOW_3G) == NetworkState.ALLOW_3G,
                (flags & NetworkState.ALLOW_4G) == NetworkState.ALLOW_4G,
                (flags & NetworkState.ALLOW_WIFI) == NetworkState.ALLOW_WIFI,
                (flags & NetworkState.ALLOW_ETHERNET) == NetworkState.ALLOW_ETHERNET,
                (flags & NetworkState.ALLOW_UNKNOWN) == NetworkState.ALLOW_UNKNOWN
        };

        (new AlertDialog.Builder(this)).
                setTitle(R.string.title_networks).
                setMultiChoiceItems(new CharSequence[] {
                        getString(R.string.network_2g),
                        getString(R.string.network_3g),
                        getString(R.string.network_4g),
                        getString(R.string.network_wifi),
                        getString(R.string.network_ethernet),
                        getString(R.string.network_unknown),
                }, checkedItems, new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                }).
                setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int flags = 0;
                        if (checkedItems[0])
                            flags += NetworkState.ALLOW_2G;
                        if (checkedItems[1])
                            flags += NetworkState.ALLOW_3G;
                        if (checkedItems[2])
                            flags += NetworkState.ALLOW_4G;
                        if (checkedItems[3])
                            flags += NetworkState.ALLOW_WIFI;
                        if (checkedItems[4])
                            flags += NetworkState.ALLOW_ETHERNET;
                        if (checkedItems[5])
                            flags += NetworkState.ALLOW_UNKNOWN;
                        prefs.edit().putInt(UpdateService.PREF_AUTO_UPDATE_NETWORKS_NAME, flags).commit();
                    }
                }).
                setNegativeButton(android.R.string.cancel, null).
                setCancelable(true).
                show();
    }

    private void showAbout() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        String time = (thisYear == 2015) ? "2015" : "2015-" + String.valueOf(thisYear);

        AlertDialog dialog = (new AlertDialog.Builder(this)).
                setTitle(R.string.app_name).
                setMessage(Html.fromHtml(getString(R.string.about_content).replace("_COPYRIGHT_", time))).
                setNeutralButton(android.R.string.ok, null).
                setCancelable(true).
                show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        if (textView != null)
            textView.setTypeface(mTitle.getTypeface());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_networks:
                showNetworks();
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private IntentFilter updateFilter = new IntentFilter(UpdateService.BROADCAST_INTENT);
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        private String formatLastChecked(long ms) {
            Date date = new Date(ms);
            if (ms == 0) {
                return getString(R.string.last_checked_never);
            } else {
                return String.format("%s %s",
                        DateFormat.getDateFormat(MainActivity.this).format(date),
                        DateFormat.getTimeFormat(MainActivity.this).format(date)
                );
            }
        }

        private String formatDownloadSpeed(long current, long total, long ms, boolean progressInK) {
            String str = "";
            if ((ms > 500) && (current > 0) && (total > 0)) {
                float kbps = ((float) current / 1024f) / ((float) ms / 1000f);
                if (progressInK) {
                    kbps *= 1024f;
                }
                int sec = (int) (((((float) total / (float) current) * (float) ms) - ms) / 1000f);

                if (kbps < 10000) {
                    str = String.format(Locale.ENGLISH, "%.0f KB/s, %02d:%02d", kbps, sec / 60, sec % 60);
                } else {
                    str = String.format(Locale.ENGLISH, "%.0f MB/s, %02d:%02d", kbps / 1024f, sec / 60, sec % 60);
                }
            }

            return str;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = "";
            String header1 = "";
            String content1 = "";
            String header2 = "";
            String content2 = "";
            String header3 = "";
            String content3 = "";
            String progressText = "";
            String progressText2 = "";
            String progressPercent = "";
            long current = 0L;
            long total = 1L;
            boolean enableProgerss = false;
            boolean enableCheck = false;
            boolean enableDownload = false;
            boolean enableFlash = false;
            boolean enableStop = false;
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);

            String state = intent.getStringExtra(UpdateService.EXTRA_STATE);
            // don't try this at home
            if (state != null) {
                try {
                    title = getString(getResources().getIdentifier(
                            "state_" + state, "string", getPackageName()));
                } catch (Exception e) {
                    // String for this state could not be found (displays empty string)
                    Logger.ex(e);
                }
            }

            if (UpdateService.STATE_ERROR_DISK_SPACE.equals(state)) {
                current = intent.getLongExtra(UpdateService.EXTRA_CURRENT, current);
                total = intent.getLongExtra(UpdateService.EXTRA_TOTAL, total);
                current /= 1024L * 1024L;
                total /= 1024L * 1024L;
                header1 = getString(R.string.header_current_version);
                content1 = Config.getInstance(MainActivity.this).getCurrentVersion();
                content2 = getString(R.string.error_disk_space_sub, current, total);

            } else if (UpdateService.STATE_ERROR_FILE_NOT_EXIST.equals(state) ||
                    UpdateService.STATE_ERROR_DOWNLOAD.equals(state) ||
                    UpdateService.STATE_ERROR_CONNECTION.equals(state) ||
                    UpdateService.STATE_ERROR_VERIFICATION_FAIL.equals(state) ||
                    UpdateService.STATE_ERROR_UNKNOWN.equals(state)) {
                enableCheck = true;
                header1 = getString(R.string.header_current_version);
                content1 = Config.getInstance(MainActivity.this).getCurrentVersion();
                content2 = getString(R.string.check_again);

            } else if (UpdateService.STATE_ACTION_CHECKING.equals(state)) {
                header1 = getString(R.string.header_current_version);
                content1 = Config.getInstance(MainActivity.this).getCurrentVersion();

            } else if (UpdateService.STATE_ACTION_NONE.equals(state)) {
                enableCheck = true;
                header1 = getString(R.string.header_current_version);
                content1 = Config.getInstance(MainActivity.this).getCurrentVersion();
                header2 = getString(R.string.header_last_checked);
                content2 = formatLastChecked(intent.getLongExtra(UpdateService.EXTRA_MS, 0));

            } else if (UpdateService.STATE_ACTION_AVAILABEL.equals(state) ||
                    UpdateService.STATE_ACTION_READY.equals(state) ||
                    UpdateService.STATE_ACTION_INSTALLING.equals(state)) {

                if (UpdateService.STATE_ACTION_AVAILABEL.equals(state)) {
                    enableDownload = true;
                } else if (UpdateService.STATE_ACTION_READY.equals(state)) {
                    enableFlash = true;
                }

                header1 = getString(R.string.header_update_version);
                content1 = prefs.getString(UpdateService.PREF_AVAILABEL_VERSION_NAME,
                        UpdateService.PREF_AVAILABEL_VERSION_NAME_DEFAULT);
                header2 = getString(R.string.header_download_size);
                long downloadSize = prefs.getLong(UpdateService.PREF_AVAILABEL_FILE_SIZE,
                        UpdateService.PREF_AVAILABEL_FILE_SIZE_DEFAULT);
                if(downloadSize == -1) {
                    content2 = "";
                } else if (downloadSize == 0) {
                    content2 = getString(R.string.text_download_size_unknown);
                } else {
                    content2 = Formatter.formatFileSize(context, downloadSize);
                }
                header3 = getString(R.string.header_update_information);
                content3 = prefs.getString(UpdateService.PREF_AVAILABEL_VERSION_INFO,
                        UpdateService.PREF_AVAILABEL_VERSION_INFO_DEFAULT);

            } else {
                enableProgerss = true;
                if (UpdateService.STATE_ACTION_DOWNLOADING.equals(state)) {
                    enableStop = true;
                }
                current = intent.getLongExtra(UpdateService.EXTRA_CURRENT, current);
                total = intent.getLongExtra(UpdateService.EXTRA_TOTAL, total);
                // long --> int overflows FTL (progress.setXXX)
                boolean progressInK = false;
                if (total > 1024L * 1024L * 1024L) {
                    progressInK = true;
                    current /= 1024L;
                    total /= 1024L;
                }

                header1 = getString(R.string.header_update_version);
                content1 = prefs.getString(UpdateService.PREF_AVAILABEL_VERSION_NAME,
                        UpdateService.PREF_AVAILABEL_VERSION_NAME_DEFAULT);
                header2 = getString(R.string.header_download_size);
                long downloadSize = prefs.getLong(UpdateService.PREF_AVAILABEL_FILE_SIZE,
                        UpdateService.PREF_AVAILABEL_FILE_SIZE_DEFAULT);
                if(downloadSize == -1) {
                    content2 = "";
                } else if (downloadSize == 0) {
                    content2 = getString(R.string.text_download_size_unknown);
                } else {
                    content2 = Formatter.formatFileSize(context, downloadSize);
                }
                header3 = getString(R.string.header_update_information);
                content3 = prefs.getString(UpdateService.PREF_AVAILABEL_VERSION_INFO,
                        UpdateService.PREF_AVAILABEL_VERSION_INFO_DEFAULT);

                progressText = intent.getStringExtra(UpdateService.EXTRA_FILE_NAME);
                progressPercent = String.format(Locale.ENGLISH, "%.0f %%",
                        intent.getFloatExtra(UpdateService.EXTRA_PROGRESS, 0));
                progressText2 = formatDownloadSpeed(current, total,
                        intent.getLongExtra(UpdateService.EXTRA_MS, 0), progressInK);
            }

            mTitle.setText(title);
            mHeader1.setText(header1);
            mContent1.setText(content1);
            mHeader2.setText(header2);
            mContent2.setText(content2);
            mHeader3.setText(header3);
            mContent3.setText(content3);

            mProgressContainer.setVisibility(enableProgerss ? View.VISIBLE : View.GONE);
            if (enableProgerss) {
                mProgressBar.setProgress((int) current);
                mProgressBar.setMax((int) total);
                mProgressText.setText(progressText);
                mProgressText2.setText(progressText2);
                mProgressPercent.setText(progressPercent);
            }

            mCheckNow.setVisibility(enableCheck ? View.VISIBLE : View.GONE);
            mDownloadNow.setVisibility(enableDownload ? View.VISIBLE : View.GONE);
            mFlashNow.setVisibility(enableFlash ? View.VISIBLE : View.GONE);
            mStopNow.setVisibility(enableStop ? View.VISIBLE : View.GONE);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(updateReceiver, updateFilter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(updateReceiver);
        super.onStop();
    }

    public void onButtonCheckNowClick(View v) {
        UpdateService.startCheck(this);
    }

    public void onButtonDownloadNowClick(View v) {
        UpdateService.startDownload(this);
    }

    public void onButtonStopClick(View v) {
        stopDownload();
    }

    public void onButtonFlashNowClick(View v) {
        UpdateService.startFlash(MainActivity.this);
    }

    private void stopDownload() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(UpdateService.PREF_STOP_DOWNLOAD,
                !prefs.getBoolean(UpdateService.PREF_STOP_DOWNLOAD, false)
                ).commit();
    }
}

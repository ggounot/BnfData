package eu.gounot.bnfdata;

import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import eu.gounot.bnfdata.database.DatabaseInstallationListener;
import eu.gounot.bnfdata.database.DatabaseInstallationTask;
import eu.gounot.bnfdata.dialog.ChangelogDialogFragment;
import eu.gounot.bnfdata.util.Constants;

public class MainActivity extends BnfDataBaseActivity implements DatabaseInstallationListener {

    private static final String TAG = "MainActivity";

    private DatabaseInstallationTask mDatabaseInstallationTask;

    private boolean mDatabaseInstallationMode = false;

    private SharedPreferences mPreferences;

    private View mHomeView;
    private View mDbInstallationView;
    private View mInsufficientFreeSpaceView;
    private ProgressBar mProgressBar;
    private TextView mPercentageView;

    private FreeDiskSpaceMonitor mFreeDiskSpaceMonitor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        // Disable the Home/Up button as this is already the root activity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(Constants.PREFS_FILE_NAME, Context.MODE_PRIVATE);

        if (changelogShowingIsNeeded()) {
            showChangelog();
        }

        handleDatabaseInstallation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Disable search menu item when in database installation mode.
        if (mDatabaseInstallationMode) {
            getSearchMenuItem().setEnabled(false);
        }

        return true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onRetainCustomNonConfigurationInstance()");
        }

        if (mDatabaseInstallationTask != null) {
            mDatabaseInstallationTask.detach();
        }

        return mDatabaseInstallationTask;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroy()");
        }

        if (mFreeDiskSpaceMonitor != null) {
            mFreeDiskSpaceMonitor.cancel(true);
        }

        if (mDatabaseInstallationTask != null) {
            mDatabaseInstallationTask.detach();
        }
    }

    @Override
    public boolean onSearchRequested() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onSearchRequested()");
        }

        // Disable search when in database install mode.
        if (!mDatabaseInstallationMode) {
            return super.onSearchRequested();
        }

        return false;
    }

    private boolean changelogShowingIsNeeded() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "changelogShowingIsNeeded()");
        }

        // Showing the changelog is needed if a new version of the app was installed and if this is
        // the first launch of this new version.

        // Retrieve the stored version code of the app.
        int storedVersionCode = mPreferences.getInt(Constants.PREF_CHANGELOG_APP_VERSION_KEY, 0);

        // Retrieve the current version code of the app.
        int currentVersionCode;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.toString(), e);

            // If this exception is raised, it might be raised at every launch, so we don't want to
            // show the changelog to not bother the user.
            return false;
        }

        return (currentVersionCode > storedVersionCode);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void showChangelog() {
        // Show the changelog dialog.
        new ChangelogDialogFragment().show(getSupportFragmentManager(), "changelog");

        // Retrieve the current version code of the app.
        int currentVersionCode;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.toString(), e);
            return;
        }

        // Store the current version code of the app.
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(Constants.PREF_CHANGELOG_APP_VERSION_KEY, currentVersionCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            preferencesEditor.apply();
        } else {
            preferencesEditor.commit();
        }
    }

    public void handleDatabaseInstallation() {
        // Check the database setup status and install it if necessary.
        mDatabaseInstallationTask = (DatabaseInstallationTask) getLastCustomNonConfigurationInstance();
        if (mDatabaseInstallationTask != null) {
            // A configuration change occurred while the database installation task was running, so
            // we just check its status and re-attach the activity to it if it is not yet completed.
            int progress = mDatabaseInstallationTask.getProgress();
            if (progress < Constants.PROGRESS_MAX) {
                mDatabaseInstallationMode = true;
                showDatabaseInstallationView();
                onDatabaseInstallationProgressUpdate(progress);
                mDatabaseInstallationTask.attach(this);
            }
        } else if (databaseInstallationIsNeeded()) {
            // We need to install the database.
            mDatabaseInstallationMode = true;
            if (getFreeDiskSpace() > Constants.REQUIRED_FREE_SPACE) {
                // The required disk space is available, so we immediately process the database
                // installation.
                startDatabaseInstallation();
            } else {
                // The required disk space is not available, so we show a view asking the user to
                // uninstall some applications to free up some disk space; and we start a
                // FreeDiskSpaceMonitor to regularly monitor the available disk space.
                showInsufficientFreeSpaceView(Constants.REQUIRED_FREE_SPACE - getFreeDiskSpace());
                mFreeDiskSpaceMonitor = new FreeDiskSpaceMonitor();
                mFreeDiskSpaceMonitor.setListener(new OnFreeDiskSpaceListener() {

                    @Override
                    public void onFreeDiskSpaceChange(long freeDiskSpace) {
                        // The available disk space has changed.
                        long deltaRequiredFreeSpace = Constants.REQUIRED_FREE_SPACE - freeDiskSpace;
                        if (deltaRequiredFreeSpace > 0) {
                            // There is still not enough free disk space, so we update the view with
                            // the new delta required free space.
                            updateInsufficientFreeSpaceView(deltaRequiredFreeSpace);
                        } else {
                            // There is now enough free disk space, so we stop the monitor and
                            // process the database installation.
                            mFreeDiskSpaceMonitor.cancel(true);
                            mFreeDiskSpaceMonitor = null;
                            mInsufficientFreeSpaceView.setVisibility(View.GONE);
                            startDatabaseInstallation();
                        }
                    }
                });
                mFreeDiskSpaceMonitor.execute();
            }
        }
    }

    public boolean databaseInstallationIsNeeded() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "databaseInstallationIsNeeded()");
        }

        // Database installation is needed if it was not already fully installed
        // or if the already installed version doesn't match the current one.
        int dbState = mPreferences.getInt(Constants.PREF_DB_STATE_KEY, Constants.DB_NOT_INSTALLED);
        if (dbState == Constants.DB_INSTALLED) {
            int dbVersion = mPreferences.getInt(Constants.PREF_DB_VERSION_KEY, 0);
            return (dbVersion != Constants.DB_VERSION);
        } else {
            return true;
        }
    }

    private void startDatabaseInstallation() {
        showDatabaseInstallationView();
        mDatabaseInstallationTask = new DatabaseInstallationTask(getApplicationContext(), this);
        mDatabaseInstallationTask.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void showDatabaseInstallationView() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "showDatabaseInstallationView()");
        }

        // Hide the home view.
        mHomeView = findViewById(R.id.home_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mHomeView.setAlpha(0);
        } else {
            mHomeView.setVisibility(View.GONE);
        }

        // Show the database installation view.
        ViewStub dbInstallViewStub = (ViewStub) findViewById(R.id.database_installation_viewstub);
        mDbInstallationView = dbInstallViewStub.inflate();

        // Reference the progress bar for progress updates.
        mProgressBar = (ProgressBar) mDbInstallationView
                .findViewById(R.id.db_installation_progressbar);
        mProgressBar.setMax(Constants.PROGRESS_MAX);

        // Reference the percentage view for progress updates.
        mPercentageView = (TextView) mDbInstallationView
                .findViewById(R.id.db_installation_percentage);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private long getFreeDiskSpace() {
        // Compute the free bytes amount in the data directory.
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        } else {
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void showInsufficientFreeSpaceView(long requiredFreeSpace) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "showInsufficientFreeSpaceView()");
        }

        // Hide the home view.
        mHomeView = findViewById(R.id.home_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mHomeView.setAlpha(0);
        } else {
            mHomeView.setVisibility(View.GONE);
        }

        // Show the insufficient free space view.
        ViewStub insufficientFreeSpaceViewStub = (ViewStub) findViewById(R.id.insufficient_free_space_viewstub);
        mInsufficientFreeSpaceView = insufficientFreeSpaceViewStub.inflate();

        updateInsufficientFreeSpaceView(requiredFreeSpace);
    }

    private void updateInsufficientFreeSpaceView(long requiredFreeSpace) {
        // Format the message with the human readable required free space amount.
        String text = getResources().getString(R.string.insufficient_free_space_explanation1,
                humanReadableSize(getResources(), requiredFreeSpace));
        CharSequence formattedLine = Html.fromHtml(text);
        TextView textView = (TextView) findViewById(R.id.insufficient_free_space_explanation1);
        if (textView != null) {
            textView.setText(formattedLine);
        }
    }

    @Override
    public void onDatabaseInstallationProgressUpdate(int progress) {
        // Update the progress bar and percentage view.
        mProgressBar.setProgress(progress);
        mPercentageView.setText(Integer.toString(progress));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onDatabaseInstallationComplete() {
        // No longer in database installation mode. Enable the search menu item.
        mDatabaseInstallationMode = false;
        getSearchMenuItem().setEnabled(true);

        // Hide the database installation view and show the home view.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mHomeView.animate().alpha(1);
            mDbInstallationView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mDbInstallationView.setVisibility(View.GONE);
                }
            });
        } else {
            mDbInstallationView.setVisibility(View.GONE);
            mHomeView.setVisibility(View.VISIBLE);
        }
    }

    private static String humanReadableSize(Resources res, long bytes) {
        // Convert bytes amount into a French human readable amount in ko or Mo.
        float size;
        String unit;
        if (bytes >= 1000000) {
            size = bytes / 1000000.0f;
            unit = res.getString(R.string.unit_mb);
        } else {
            size = bytes / 1000.0f;
            unit = res.getString(R.string.unit_kb);
        }
        return String.format(Locale.FRENCH, "%.1f%s", size, unit);
    }

    /*
     * FreeDiskSpaceMonitor checks the amount of available disk space every 500ms and notify any
     * listener whenever this amount has changed.
     */
    private class FreeDiskSpaceMonitor extends AsyncTask<Void, Long, Void> {

        private OnFreeDiskSpaceListener mListener = null;
        private long mFreeDiskSpace = -1;

        public void setListener(OnFreeDiskSpaceListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                long freeDiskSpace = getFreeDiskSpace();
                if (freeDiskSpace != mFreeDiskSpace) {
                    publishProgress(freeDiskSpace);
                    mFreeDiskSpace = freeDiskSpace;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            if (mListener != null) {
                mListener.onFreeDiskSpaceChange(values[0]);
            }
        }

    }

    /*
     * Listener interface for FreeDiskSpaceMonitor.
     */
    private interface OnFreeDiskSpaceListener {

        public void onFreeDiskSpaceChange(long freeBytes);
    }

}

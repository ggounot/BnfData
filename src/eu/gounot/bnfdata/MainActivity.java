package eu.gounot.bnfdata;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import eu.gounot.bnfdata.database.DatabaseInstallationListener;
import eu.gounot.bnfdata.database.DatabaseInstallationTask;
import eu.gounot.bnfdata.util.Constants;

public class MainActivity extends BnfDataBaseActivity implements DatabaseInstallationListener {

    private static final String TAG = "MainActivity";

    private DatabaseInstallationTask mDatabaseInstallationTask;

    private boolean mDatabaseInstallationMode = false;

    private View mHomeView;
    private View mDbInstallationView;
    private ProgressBar mProgressBar;
    private TextView mPercentageView;

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

    public void handleDatabaseInstallation() {
        // Check the database setup status and install it if necessary.
        mDatabaseInstallationTask = (DatabaseInstallationTask) getLastCustomNonConfigurationInstance();
        if (mDatabaseInstallationTask != null) {
            int progress = mDatabaseInstallationTask.getProgress();
            if (progress < Constants.PROGRESS_MAX) {
                showDatabaseInstallationView();
                onDatabaseInstallationProgressUpdate(progress);
                mDatabaseInstallationTask.attach(this);
            }
        } else if (databaseInstallationIsNeeded()) {
            showDatabaseInstallationView();
            mDatabaseInstallationTask = new DatabaseInstallationTask(getApplicationContext(), this);
            mDatabaseInstallationTask.execute();
        }
    }

    public boolean databaseInstallationIsNeeded() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "databaseInstallationIsNeeded()");
        }

        // Database installation is needed if it was not already fully installed
        // or if the already installed version doesn't match the current one.
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_FILE_NAME,
                Context.MODE_PRIVATE);
        int dbState = preferences.getInt(Constants.PREF_DB_STATE_KEY, Constants.DB_NOT_INSTALLED);
        if (dbState == Constants.DB_INSTALLED) {
            int dbVersion = preferences.getInt(Constants.PREF_DB_VERSION_KEY, 0);
            return (dbVersion != Constants.DB_VERSION);
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void showDatabaseInstallationView() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "showDatabaseInstallationView()");
        }

        // Enter database installation mode.
        mDatabaseInstallationMode = true;

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

}

package eu.gounot.bnfdata.database;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;

public class DatabaseInstallationTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "DatabaseInstallationTask";

    private Context mApplicationContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPrefsEditor;
    private String mDatabaseDir;
    private String mDatabasePath;
    private DatabaseInstallationListener mDbInstallationListener = null;
    private int mProgress = 0;

    public int getProgress() {
        return mProgress;
    }

    public DatabaseInstallationTask(Context context,
            DatabaseInstallationListener dbInstallationListener) {
        mApplicationContext = context.getApplicationContext();
        mPreferences = mApplicationContext.getSharedPreferences(DatabaseConstants.PREFS_FILE_NAME,
                Context.MODE_PRIVATE);
        mPrefsEditor = mPreferences.edit();
        mDatabaseDir = mApplicationContext.getApplicationInfo().dataDir + "/databases";
        mDatabasePath = mDatabaseDir + "/" + DatabaseConstants.DB_FILENAME;
        attach(dbInstallationListener);
    }

    public void attach(DatabaseInstallationListener databaseInstallListener) {
        // Attach the listener to this task.
        mDbInstallationListener = databaseInstallListener;
        if (mProgress >= DatabaseConstants.PROGRESS_MAX) {
            mDbInstallationListener.onDatabaseInstallationComplete();
        }
    }

    public void detach() {
        // Detach the listener from this task.
        mDbInstallationListener = null;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "doInBackground()");
        }

        InputStream is = null;
        GZIPInputStream gzis = null;
        OutputStream os = null;

        try {
            // Create the database directory if it doesn't exist.
            File databaseDir = new File(mDatabaseDir);
            if (!databaseDir.exists()) {
                databaseDir.mkdir();
            }

            // Open the i/o streams.
            is = mApplicationContext.getAssets().open(
                    DatabaseConstants.ASSETS_COMPRESSED_DB_FILENAME);
            gzis = new GZIPInputStream(new BufferedInputStream(is));
            os = new FileOutputStream(mDatabasePath);

            // Process the copy.

            byte[] buffer = new byte[65536]; // 64 KiB
            int length;
            int copiedBytes = 0;
            int oldProgress = 0;
            int progress = 0;

            while ((length = gzis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                copiedBytes += length;
                // Compute the percentage of copied bytes as progress.
                progress = (int) ((copiedBytes / (float) DatabaseConstants.UNCOMPRESSED_DB_SIZE) * DatabaseConstants.PROGRESS_MAX);
                // Publish the progress.
                if (progress != oldProgress) {
                    publishProgress(progress);
                    oldProgress = progress;
                }
                // Escape early if cancel() is called.
                if (isCancelled()) {
                    return false;
                }
            }
            os.flush();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
            return false;
        } finally {
            // Close the i/o streams.
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                }
            }
            if (gzis != null) {
                try {
                    gzis.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                }
            }
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onPreExecute() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPreExecute()");
        }

        // Mark the database installation as aborted before the copy in the event that the copy is
        // actually aborted.
        mPrefsEditor.putInt(DatabaseConstants.PREF_DB_STATE_KEY,
                DatabaseConstants.DB_INSTALL_ABORTED);

        // Mark the database version number that will be copied.
        mPrefsEditor.putInt(DatabaseConstants.PREF_DB_VERSION_KEY, DatabaseConstants.DB_VERSION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mPrefsEditor.apply();
        } else {
            mPrefsEditor.commit();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onProgressUpdate()");
        }

        // Save the progress state.
        mProgress = values[0];

        if (mDbInstallationListener != null) {
            // Notify the database installation progress to the listener.
            mDbInstallationListener.onDatabaseInstallationProgressUpdate(mProgress);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onPostExecute(Boolean result) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPostExecute()");
        }

        // Mark the database installation as completed.
        mPrefsEditor.putInt(DatabaseConstants.PREF_DB_STATE_KEY, DatabaseConstants.DB_INSTALLED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mPrefsEditor.apply();
        } else {
            mPrefsEditor.commit();
        }

        if (mDbInstallationListener != null) {
            // Notify the listener that the database installation is completed.
            mDbInstallationListener.onDatabaseInstallationComplete();
        }
    }

}

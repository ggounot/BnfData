package eu.gounot.bnfdata.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseOpenHelper";

    public DatabaseOpenHelper(Context context) {
        super(context.getApplicationContext(), DatabaseConstants.DB_FILENAME, null,
                DatabaseConstants.DB_VERSION);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DatabaseOpenHelper()");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Nothing to do as the database was pre-created and copied from assets.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do as no database where used in the previous version of the app.
    }

}

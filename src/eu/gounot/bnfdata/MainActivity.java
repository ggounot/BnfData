package eu.gounot.bnfdata;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends BnfDataBaseActivity {

    private static final String TAG = "MainActivity";

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
    }

}

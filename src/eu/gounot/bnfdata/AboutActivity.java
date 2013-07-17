package eu.gounot.bnfdata;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

public class AboutActivity extends BnfDataBaseActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_about);

        TextView sourceCodeTextView = (TextView) findViewById(R.id.source_code);
        sourceCodeTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateOptionsMenu()");
        }

        // Set a menu that does not include the About item.
        getSupportMenuInflater().inflate(R.menu.about, menu);
        configureSearchView(this, menu);

        return true;
    }

}

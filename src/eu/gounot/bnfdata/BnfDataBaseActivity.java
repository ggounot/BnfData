package eu.gounot.bnfdata;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public abstract class BnfDataBaseActivity extends SherlockFragmentActivity implements
        OnQueryTextListener {

    private static final String TAG = "BnfDataBaseActivity";

    private MenuItem mSearchMenuItem;

    protected MenuItem getSearchMenuItem() {
        return mSearchMenuItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable the Home/Up button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateOptionsMenu()");
        }

        getSupportMenuInflater().inflate(R.menu.default_menu, menu);

        configureSearchView(this, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onOptionsItemSelected()");
        }

        switch (item.getItemId()) {
        case android.R.id.home:
            // Go back to MainActivity when the Up button is pressed.
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivityIntent);
            finish();
            return true;
        case R.id.menu_about:
            // Show the About screen.
            Intent aboutActivityIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutActivityIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        // Expand the SearchView when a search is requested
        // (when the Search button is pressed for instance).
        if (mSearchMenuItem != null) {
            mSearchMenuItem.expandActionView();
        }

        return super.onSearchRequested();
    }

    public SearchView configureSearchView(Context context, Menu menu) {
        // Get the SearchView and set the searchable configuration.
        SearchManager searchManager = (SearchManager) context
                .getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ComponentName componentName = new ComponentName("eu.gounot.bnfdata",
                "eu.gounot.bnfdata.SearchActivity");
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);

        // Set the OnQueryTextListener to check whether the query is
        // long enough to be submitted in onQueryTextSubmit().
        searchView.setOnQueryTextListener(this);

        // Reference the search menu item to expand it when
        // a search is requested in onSearchRequested().
        mSearchMenuItem = menu.findItem(R.id.menu_search);

        return searchView;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Inhibit the query submission if it is less than 3 characters
        // and show an explanatory message.
        if (query.length() < 3) {
            Toast toast = Toast.makeText(this, R.string.query_too_short, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, toast.getYOffset());
            toast.show();
            return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Nothing to do.
        return false;
    }

}

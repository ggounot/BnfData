package eu.gounot.bnfdata;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import eu.gounot.bnfdata.provider.SuggestionsProvider;
import eu.gounot.bnfdata.util.NetworkState;

public class SearchActivity extends BnfDataBaseActivity implements OnItemClickListener,
        OnClickListener, LoaderCallbacks<Cursor> {

    private static final String TAG = "SearchableActivity";

    // Loader's ID.
    private static final int SEARCH_RESULTS_LOADER = 0;

    // Views.
    private View mProgressBar;
    private View mNoResultView;
    private View mNetworkErrorView;
    private ListView mListView;

    private SimpleCursorAdapter mAdapter;
    private String mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        Intent intent = getIntent();
        String intentAction = intent.getAction();

        // Check the intent action.
        if (Intent.ACTION_SEARCH.equalsIgnoreCase(intentAction)) {
            // The user has initiated a search.

            setContentView(R.layout.activity_search);

            // Reference the views.
            mProgressBar = findViewById(R.id.progress_bar);
            mNoResultView = findViewById(R.id.no_result);
            mListView = (ListView) findViewById(R.id.list);

            mListView.setOnItemClickListener(this);

            // Get the query and process the search.
            String query = intent.getStringExtra(SearchManager.QUERY);
            processSearch(query);
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(intentAction)) {
            // The user has selected a suggestion.

            // Get the type and the URL of the selected object.
            int object_type = Integer.parseInt(intent.getExtras().getString(
                    SearchManager.EXTRA_DATA_KEY));
            String url = intent.getDataString();

            // Start the appropriate activity to view the object and finish the search activity.
            startAppropriateActivity(object_type, url);
            finish();
        } else {
            // The intent action received is not valid.
            throw new RuntimeException(
                    "The intent action is not valid. Should be ACTION_SEARCH or ACTION_VIEW.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onItemClick() position=" + position);
        }

        // Get the type and the URL of the selected object.
        Cursor cursor = mAdapter.getCursor();
        int object_type = cursor.getInt(SuggestionsProvider.CURSOR_COL_OBJECT_TYPE);
        String url = cursor.getString(SuggestionsProvider.CURSOR_COL_ARK_NAME);

        // Start the appropriate activity to view the object.
        startAppropriateActivity(object_type, url);
    }

    private void startAppropriateActivity(int object_type, String url) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startAppropriateActivity() object_type=" + object_type + " url=" + url);
        }

        // Select an appropriate activity to view the selected object.
        Class<?> intentClass;
        switch (object_type) {
        case SuggestionsProvider.PERSON:
            intentClass = ViewAuthorActivity.class;
            break;
        case SuggestionsProvider.WORK:
            intentClass = ViewWorkActivity.class;
            break;
        case SuggestionsProvider.ORGANIZATION:
            intentClass = ViewOrganizationActivity.class;
            break;
        default:
            Log.e(TAG, "Object type '" + object_type + "' is not valid.");
            return;
        }

        // Make the intent and start the appropriate activity to view the selected object.
        Intent viewObjectIntent = new Intent(this, intentClass);
        viewObjectIntent.setData(Uri.parse(url));
        startActivity(viewObjectIntent);
    }

    private void processSearch(String filter) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processSearch() filter=" + filter);
        }

        mFilter = filter;

        // Create an empty adapter for the ListView. The search results
        // will be put in when they are fully loaded.
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2 }, new int[] { android.R.id.text1,
                        android.R.id.text2 }, 0);

        mListView.setAdapter(mAdapter);

        // Initialize the search results loader.
        getSupportLoaderManager().initLoader(SEARCH_RESULTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        String selection = getResources().getString(R.string.suggest_selection);
        String[] selectionArgs = new String[] { mFilter };

        // Load and return the cursor.
        return new CursorLoader(getApplicationContext(), SuggestionsProvider.CONTENT_SEARCH_URI,
                null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        // Put the results in the adapter.
        mAdapter.swapCursor(cursor);

        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);

        // Hide the network error view if it has already been inflated.
        if (mNetworkErrorView != null) {
            mNetworkErrorView.setVisibility(View.GONE);
        }

        if (cursor != null) {
            // Show the results list or the no result view if there is no result.
            if (cursor.getCount() > 0) {
                mListView.setVisibility(View.VISIBLE);
            } else {
                mNoResultView.setVisibility(View.VISIBLE);
            }
        } else {
            // A network error occurred while trying to retrieve the data.

            // Load the network error ViewStub if it was not already loaded.
            if (mNetworkErrorView == null) {
                // Inflate the network error view and reference it for further use.
                ViewStub viewStub = (ViewStub) findViewById(R.id.network_error_stub);
                viewStub.setLayoutResource(R.layout.network_error);
                mNetworkErrorView = viewStub.inflate();

                // Set the OnClickListener to the Retry button.
                Button retryButton = (Button) mNetworkErrorView.findViewById(R.id.retry_button);
                retryButton.setOnClickListener(this);
            }

            int errMsgResId;

            // Select an appropriate error message.
            if (!NetworkState.isNetworkAvailable(this)) {
                errMsgResId = R.string.errmsg_no_network_connection;
            } else {
                errMsgResId = R.string.errmsg_data_retrieval_failed;
            }

            // Set the error message to the network error TextView.
            TextView errorMessageTextView = (TextView) mNetworkErrorView
                    .findViewById(R.id.error_message);
            errorMessageTextView.setText(errMsgResId);

            // Show the network error view.
            mNetworkErrorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        // Following a network error, the user has clicked the Retry button,
        // so we restart the loader to try loading the data again.
        mNetworkErrorView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(SEARCH_RESULTS_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoaderReset()");
        }

        mAdapter.swapCursor(null);
    }

}

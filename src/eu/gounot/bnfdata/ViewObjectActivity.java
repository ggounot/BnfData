package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import eu.gounot.bnfdata.loadercallbacks.DataLoaderCallbacks;
import eu.gounot.bnfdata.util.NetworkState;

public abstract class ViewObjectActivity extends BnfDataBaseActivity implements OnClickListener {

    private static final String TAG = "ViewObjectActivity";

    // Loaders' IDs.
    public static final int DATA_LOADER = 0;
    public static final int IMAGE_LOADER = 1;

    private View mProgressBar;
    private View mNetworkErrorView;

    private DataLoaderCallbacks mDataLoaderCallbacks;

    private View getProgressBar() {
        if (mProgressBar == null) {
            mProgressBar = findViewById(R.id.progress_bar);
        }
        return mProgressBar;
    }

    protected void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public abstract int getObjectType();

    public abstract void onDataLoaded(JSONObject jsonObject);

    public abstract void onImageLoaded(Bitmap bitmap);

    public void onNetworkError() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onNetworkError()");
        }

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
        if (!NetworkState.isNetworkAvailable(getApplicationContext())) {
            errMsgResId = R.string.errmsg_no_network_connection;
        } else {
            errMsgResId = R.string.errmsg_data_retrieval_failed;
        }

        // Set the error message to the network error TextView.
        TextView errorMessageTextView = (TextView) mNetworkErrorView
                .findViewById(R.id.error_message);
        errorMessageTextView.setText(errMsgResId);

        // Hide the progress bar.
        getProgressBar().setVisibility(View.GONE);

        // Show the network error view.
        mNetworkErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        // Following a network error, the user has clicked the Retry button,
        // so we restart the loader to try loading the data again.
        mNetworkErrorView.setVisibility(View.INVISIBLE);
        getProgressBar().setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(DATA_LOADER, null, mDataLoaderCallbacks);
    }

    protected void hideNetworkError() {
        // Hide the network error view if it has already been inflated.
        if (mNetworkErrorView != null) {
            mNetworkErrorView.setVisibility(View.GONE);
        }
    }

    protected void loadData(String arkName) {
        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new DataLoaderCallbacks(this, arkName);
        getSupportLoaderManager().initLoader(DATA_LOADER, null, mDataLoaderCallbacks);
    }

}

package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import eu.gounot.bnfdata.loadercallbacks.OrganizationDataLoaderCallbacks;

public class ViewOrganizationActivity extends BnfDataBaseActivity implements OnClickListener {

    private static final String TAG = "ViewOrganizationActivity";

    // Loaders' IDs.
    public static final int DATA_LOADER = 0;
    public static final int IMAGE_LOADER = 1;

    // Views.
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mStartdateLabelTextView;
    private TextView mStopdateLabelTextView;
    private TextView mNationalitiesLabelTextView;
    private TextView mNotesLabelTextView;
    private TextView mStartdateTextView;
    private TextView mStopdateTextView;
    private TextView mNationalitiesTextView;
    private TextView mNotesTextView;
    private ProgressBar mProgressBar;
    private ProgressBar mImageProgressBar;
    private View mNetworkErrorView;
    private View mScrollView;

    private OrganizationDataLoaderCallbacks mDataLoaderCallbacks;

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ProgressBar getImageProgressBar() {
        return mImageProgressBar;
    }

    public View getScrollView() {
        return mScrollView;
    }

    public void setNetworkErrorView(View networkErrorView) {
        mNetworkErrorView = networkErrorView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_view_organization);

        // Reference the views.
        mImageView = (ImageView) findViewById(R.id.image);
        mNameTextView = (TextView) findViewById(R.id.name);
        mStartdateLabelTextView = (TextView) findViewById(R.id.startdate_label);
        mStopdateLabelTextView = (TextView) findViewById(R.id.stopdate_label);
        mNationalitiesLabelTextView = (TextView) findViewById(R.id.nationalities_label);
        mNotesLabelTextView = (TextView) findViewById(R.id.notes_label);
        mStartdateTextView = (TextView) findViewById(R.id.startdate);
        mStopdateTextView = (TextView) findViewById(R.id.stopdate);
        mNationalitiesTextView = (TextView) findViewById(R.id.nationalities);
        mNotesTextView = (TextView) findViewById(R.id.notes);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mImageProgressBar = (ProgressBar) findViewById(R.id.image_progress_bar);
        mScrollView = findViewById(R.id.scrollview);

        // Get the organization's URL from the intent.
        String organizationUrl = getIntent().getDataString();

        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new OrganizationDataLoaderCallbacks(this, organizationUrl);
        getSupportLoaderManager().initLoader(DATA_LOADER, null, mDataLoaderCallbacks);
    }

    @Override
    public void onClick(View v) {
        // Following a network error, the user has clicked the Retry button,
        // so we restart the loader to try loading the data again.
        mNetworkErrorView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(DATA_LOADER, null, mDataLoaderCallbacks);
    }

    // These methods below are used by the onLoadFinished() callback
    // to update the UI with the loaded data.

    public void setImage(Bitmap image) {
        if (image != null) {
            mImageView.setImageBitmap(image);
        } else {
            mImageView.setImageResource(R.drawable.no_author_or_organization_image);
        }
        mImageProgressBar.setVisibility(View.GONE);
    }

    public void setName(String name) {
        if (name != null) {
            mNameTextView.setText(name);
        } else {
            mNameTextView.setVisibility(View.GONE);
        }
    }

    public void setStartdate(String startdate) {
        if (startdate != null) {
            mStartdateTextView.setText(startdate);
        } else {
            mStartdateLabelTextView.setVisibility(View.GONE);
            mStartdateTextView.setVisibility(View.GONE);
        }
    }

    public void setStopdate(String stopdate) {
        if (stopdate != null) {
            mStopdateTextView.setText(stopdate);
        } else {
            mStopdateLabelTextView.setVisibility(View.GONE);
            mStopdateTextView.setVisibility(View.GONE);
        }
    }

    public void setNationalities(String nationalities) {
        if (nationalities != null) {
            mNationalitiesTextView.setText(nationalities);
        } else {
            mNationalitiesLabelTextView.setVisibility(View.GONE);
            mNationalitiesTextView.setVisibility(View.GONE);
        }
    }

    public void setNotes(String notes) {
        if (notes != null) {
            mNotesTextView.setText(notes);
        } else {
            mNotesLabelTextView.setVisibility(View.GONE);
            mNotesTextView.setVisibility(View.GONE);
        }
    }

}

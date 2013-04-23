package eu.gounot.bnfdata;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import eu.gounot.bnfdata.loadercallbacks.AuthorDataLoaderCallbacks;

public class ViewAuthorActivity extends BnfDataBaseActivity implements OnItemClickListener,
        OnClickListener {

    private static final String TAG = "ViewAuthorActivity";

    // Loaders' IDs.
    public static final int DATA_LOADER = 0;
    public static final int IMAGE_LOADER = 1;

    // Views.
    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mDatesTextView;
    private TextView mBirthdateLabelTextView;
    private TextView mDeathdateLabelTextView;
    private TextView mNationalitiesLabelTextView;
    private TextView mNotesLabelTextView;
    private TextView mBirthdateTextView;
    private TextView mDeathdateTextView;
    private TextView mNationalitiesTextView;
    private TextView mNotesTextView;
    private ProgressBar mProgressBar;
    private ProgressBar mImageProgressBar;
    private View mNetworkErrorView;
    private ListView mListView;

    private AuthorDataLoaderCallbacks mDataLoaderCallbacks;

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ProgressBar getImageProgressBar() {
        return mImageProgressBar;
    }

    public ListView getListView() {
        return mListView;
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

        setContentView(R.layout.activity_view_author);

        mListView = (ListView) findViewById(R.id.list);

        // Set up the GridLayout (in which all the TextViews are)
        // as the ListView's header.
        View header = View.inflate(this, R.layout.activity_view_author_header, null);
        mListView.addHeaderView(header, null, false);

        // Set up a transparent footer that serves as a padding between
        // the ListView's items and the bottom of the screen.
        View footer = View.inflate(this, R.layout.listview_footer, null);
        mListView.addFooterView(footer, null, false);

        mListView.setOnItemClickListener(this);

        // Reference the views.
        mImageView = (ImageView) findViewById(R.id.image);
        mNameTextView = (TextView) findViewById(R.id.name);
        mDatesTextView = (TextView) findViewById(R.id.dates);
        mBirthdateLabelTextView = (TextView) findViewById(R.id.birthdate_label);
        mDeathdateLabelTextView = (TextView) findViewById(R.id.deathdate_label);
        mNationalitiesLabelTextView = (TextView) findViewById(R.id.nationalities_label);
        mNotesLabelTextView = (TextView) findViewById(R.id.notes_label);
        mBirthdateTextView = (TextView) findViewById(R.id.birthdate);
        mDeathdateTextView = (TextView) findViewById(R.id.deathdate);
        mNationalitiesTextView = (TextView) findViewById(R.id.nationalities);
        mNotesTextView = (TextView) findViewById(R.id.notes);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mImageProgressBar = (ProgressBar) findViewById(R.id.image_progress_bar);

        // Get the author's URL from the intent.
        String authorUrl = getIntent().getDataString();

        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new AuthorDataLoaderCallbacks(this, authorUrl);
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

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onItemClick() position=" + position);
        }

        // Create an intent, put in the selected work's URL,
        // and start the ViewWorkActivity to view this work in detail.
        Intent intent = new Intent(this, ViewWorkActivity.class);
        Work work = (Work) mListView.getAdapter().getItem(position);
        intent.setData(Uri.parse(work.getUrl()));
        startActivity(intent);
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

    public void setDates(String dates) {
        if (dates != null) {
            mDatesTextView.setText(dates);
        } else {
            mDatesTextView.setVisibility(View.GONE);
        }
    }

    public void setBirthdate(String birthdate) {
        if (birthdate != null) {
            mBirthdateTextView.setText(birthdate);
        } else {
            mBirthdateLabelTextView.setVisibility(View.GONE);
            mBirthdateTextView.setVisibility(View.GONE);
        }
    }

    public void setDeathdate(String deathdate) {
        if (deathdate != null) {
            mDeathdateTextView.setText(deathdate);
        } else {
            mDeathdateLabelTextView.setVisibility(View.GONE);
            mDeathdateTextView.setVisibility(View.GONE);
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

    // This class gathers a work's title and URL.
    public static class Work {

        private String mTitle;
        private String mUrl;

        public String toString() {
            return mTitle;
        }

        public String getUrl() {
            return mUrl;
        }

        public Work(String title, String url) {
            mTitle = title;
            mUrl = url;
        }

    }

}

package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import eu.gounot.bnfdata.loadercallbacks.WorkDataLoaderCallbacks;
import eu.gounot.bnfdata.util.Constants;

public class ViewWorkActivity extends BnfDataBaseActivity implements OnClickListener {

    private static final String TAG = "ViewWorkActivity";

    // Loaders' IDs.
    public static final int DATA_LOADER = 0;
    public static final int IMAGE_LOADER = 1;

    // Views.
    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mLanguagesLabelTextView;
    private TextView mNotesLabelTextView;
    private TextView mLanguagesTextView;
    private TextView mNotesTextView;
    private ProgressBar mProgressBar;
    private ProgressBar mImageProgressBar;
    private View mNetworkErrorView;
    private ScrollView mScrollView;

    private WorkDataLoaderCallbacks mDataLoaderCallbacks;

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ProgressBar getImageProgressBar() {
        return mImageProgressBar;
    }

    public ScrollView getScrollView() {
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

        setContentView(R.layout.activity_view_work);

        mScrollView = (ScrollView) findViewById(R.id.scrollview);

        // Reference the views.
        mImageView = (ImageView) findViewById(R.id.image);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mAuthorTextView = (TextView) findViewById(R.id.author);
        mLanguagesLabelTextView = (TextView) findViewById(R.id.languages_label);
        mNotesLabelTextView = (TextView) findViewById(R.id.notes_label);
        mLanguagesTextView = (TextView) findViewById(R.id.languages);
        mNotesTextView = (TextView) findViewById(R.id.notes);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mImageProgressBar = (ProgressBar) findViewById(R.id.image_progress_bar);

        // Get the ARK name from the intent.
        String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);

        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new WorkDataLoaderCallbacks(this, arkName);
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
            mImageView.setImageResource(R.drawable.no_work_image);
        }
        mImageProgressBar.setVisibility(View.GONE);
    }

    public void setWorkTitle(String title) {
        if (title != null) {
            mTitleTextView.setText(title);
        } else {
            mTitleTextView.setVisibility(View.GONE);
        }
    }

    public void setAuthor(String author) {
        if (author != null) {
            mAuthorTextView.setText(author);
        } else {
            mAuthorTextView.setVisibility(View.GONE);
        }
    }

    public void setLanguages(String languages) {
        if (languages != null) {
            mLanguagesTextView.setText(languages);
        } else {
            mLanguagesLabelTextView.setVisibility(View.GONE);
            mLanguagesTextView.setVisibility(View.GONE);
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

    // This class gathers a contributor's name and URL.
    public static class Contributor {

        private String mName;
        private String mUrl;

        public String toString() {
            return mName;
        }

        public String getUrl() {
            return mUrl;
        }

        public Contributor(String title, String url) {
            mName = title;
            mUrl = url;
        }

    }

}

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

import eu.gounot.bnfdata.loadercallbacks.WorkDataLoaderCallbacks;

public class ViewWorkActivity extends BnfDataBaseActivity implements OnItemClickListener,
        OnClickListener {

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
    private ListView mListView;

    private WorkDataLoaderCallbacks mDataLoaderCallbacks;

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

        setContentView(R.layout.activity_view_work);

        mListView = (ListView) findViewById(R.id.list);

        // Set up the GridLayout (in which all the TextViews are)
        // as the ListView's header.
        View header = View.inflate(this, R.layout.activity_view_work_header, null);
        mListView.addHeaderView(header, null, false);

        // Set up a transparent footer that serves as a padding between
        // the ListView's items and the bottom of the screen.
        View footer = View.inflate(this, R.layout.listview_footer, null);
        mListView.addFooterView(footer, null, false);

        mListView.setOnItemClickListener(this);

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

        // Get the work's URL from the intent.
        String workUrl = getIntent().getDataString();

        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new WorkDataLoaderCallbacks(this, workUrl);
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

        // Create an intent, put in the selected contributor's URL,
        // and start the ViewAuthorActivity to view this contributor in detail.
        Intent intent = new Intent(this, ViewAuthorActivity.class);
        Contributor contributor = (Contributor) mListView.getAdapter().getItem(position);
        intent.setData(Uri.parse(contributor.getUrl()));
        startActivity(intent);
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

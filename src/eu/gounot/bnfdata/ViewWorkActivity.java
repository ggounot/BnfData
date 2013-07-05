package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import eu.gounot.bnfdata.data.Work;
import eu.gounot.bnfdata.loadercallbacks.WorkDataLoaderCallbacks;
import eu.gounot.bnfdata.loadercallbacks.WorkImageLoaderCallbacks;
import eu.gounot.bnfdata.util.Constants;
import eu.gounot.bnfdata.util.NetworkState;

public class ViewWorkActivity extends BnfDataBaseActivity implements OnClickListener {

    private static final String TAG = "ViewWorkActivity";

    // Loaders' IDs.
    public static final int DATA_LOADER = 0;
    public static final int IMAGE_LOADER = 1;

    // Views.
    private ProgressBar mProgressBar;
    private View mNetworkErrorView;
    private ScrollView mScrollView;

    private WorkDataLoaderCallbacks mDataLoaderCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_view_work);

        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Get the ARK name from the intent.
        String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);

        // Initialize the data loader with its callbacks.
        mDataLoaderCallbacks = new WorkDataLoaderCallbacks(this, arkName);
        getSupportLoaderManager().initLoader(DATA_LOADER, null, mDataLoaderCallbacks);
    }

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
        mProgressBar.setVisibility(View.GONE);

        // Show the network error view.
        mNetworkErrorView.setVisibility(View.VISIBLE);
    }

    public void onDataLoaded(Work work) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDataLoaded()");
        }

        setTitle(work.getTitle());
        setCreator(work.getCreatorLabel());
        setDate(work.getDate());
        setLanguage(work.getLanguageName());
        setDate(work.getDate());
        setDescription(work.getDescription());
        setSubjects(work.getSubjects());
        setAltForms(work.getAltForms());
        setEditorialNotes(work.getEditorialNotes());
        setExternalLinks(work.getCatalogueUrl(), work.getWikipediaUrl());

        String imageUrl = work.getImageUrl();
        if (imageUrl != null) {
            WorkImageLoaderCallbacks callbacks = new WorkImageLoaderCallbacks(this, imageUrl);
            getSupportLoaderManager().initLoader(IMAGE_LOADER, null, callbacks);
        } else {
            setImage(null);
        }

        // Hide the network error view if it has already been inflated.
        if (mNetworkErrorView != null) {
            mNetworkErrorView.setVisibility(View.GONE);
        }

        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);

        // Show the ScrollView.
        mScrollView.setVisibility(View.VISIBLE);
    }

    public void onImageLoaded(Bitmap bitmap) {
        setImage(bitmap);
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
        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            imageView.setImageResource(R.drawable.no_work_image);
        }
        findViewById(R.id.image_progress_bar).setVisibility(View.GONE);
    }

    public void setTitle(String title) {
        TextView titleTextView = (TextView) findViewById(R.id.title);
        if (title != null) {
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
    }

    public void setCreator(String creatorLabel) {
        TextView authorTextView = (TextView) findViewById(R.id.author);
        if (creatorLabel != null) {
            authorTextView.setText(creatorLabel);
        } else {
            authorTextView.setVisibility(View.GONE);
        }
    }

    public void setLanguage(String language) {
        TextView languageLabelTextView = (TextView) findViewById(R.id.language_label);
        TextView languageTextView = (TextView) findViewById(R.id.language);
        if (language != null) {
            languageTextView.setText(language);
        } else {
            languageLabelTextView.setVisibility(View.GONE);
            languageTextView.setVisibility(View.GONE);
        }
    }

    public void setDate(String date) {
        TextView dateLabelTextView = (TextView) findViewById(R.id.date_label);
        TextView dateTextView = (TextView) findViewById(R.id.date);
        if (date != null) {
            dateTextView.setText(date);
        } else {
            dateLabelTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
        }
    }

    public void setDescription(String[] description) {
        TextView descriptionLabelTextView = (TextView) findViewById(R.id.description_label);
        TextView descriptionTextView = (TextView) findViewById(R.id.description);
        if (description != null) {
            descriptionTextView.setText(TextUtils.join("\n", description));
        } else {
            descriptionLabelTextView.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.GONE);
        }
    }

    public void setSubjects(String[] subjects) {
        TextView subjectsLabelTextView = (TextView) findViewById(R.id.subjects_label);
        TextView subjectsTextView = (TextView) findViewById(R.id.subjects);
        if (subjects != null) {
            subjectsTextView.setText(TextUtils.join("\n", subjects));
        } else {
            subjectsLabelTextView.setVisibility(View.GONE);
            subjectsTextView.setVisibility(View.GONE);
        }
    }

    public void setAltForms(String[] altForms) {
        TextView altFormsLabelTextView = (TextView) findViewById(R.id.alt_forms_label);
        TextView altFormsTextView = (TextView) findViewById(R.id.alt_forms);
        if (altForms != null) {
            altFormsTextView.setText(TextUtils.join("\n", altForms));
        } else {
            altFormsLabelTextView.setVisibility(View.GONE);
            altFormsTextView.setVisibility(View.GONE);
        }
    }

    public void setEditorialNotes(String[][] editorialNotes) {
        TextView editorialNotesLabelTextView = (TextView) findViewById(R.id.editorial_notes_label);
        TextView editorialNotesTextView = (TextView) findViewById(R.id.editorial_notes);
        if (editorialNotes != null) {
            String[] noteGroups = new String[editorialNotes.length];
            for (int i = 0; i < editorialNotes.length; i++) {
                noteGroups[i] = TextUtils.join("\n", editorialNotes[i]);
            }
            editorialNotesTextView.setText(TextUtils.join("\n\n", noteGroups));
        } else {
            editorialNotesLabelTextView.setVisibility(View.GONE);
            editorialNotesTextView.setVisibility(View.GONE);
        }
    }

    public void setExternalLinks(String catalogueUrl, String wikipediaUrl) {
        TextView externalLinksLabelTextView = (TextView) findViewById(R.id.external_links_label);
        TextView externalLinksTextView = (TextView) findViewById(R.id.external_links);
        String urls = "";
        if (catalogueUrl != null) {
            urls += "<a href=\"" + catalogueUrl + "\">"
                    + getResources().getString(R.string.catalogue_link_text) + "</a>";
        }
        if (wikipediaUrl != null) {
            if (!urls.equals("")) {
                urls += "<br />";
            }
            urls += "<a href=\"" + wikipediaUrl + "\">"
                    + getResources().getString(R.string.wikipedia_link_text) + "</a>";
        }
        if (!urls.equals("")) {
            externalLinksTextView.setText(Html.fromHtml(urls));
            externalLinksTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            externalLinksLabelTextView.setVisibility(View.GONE);
            externalLinksTextView.setVisibility(View.GONE);
        }
    }

}

package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import eu.gounot.bnfdata.data.Work;
import eu.gounot.bnfdata.loadercallbacks.ImageLoaderCallbacks;
import eu.gounot.bnfdata.util.Constants;

public class ViewWorkActivity extends ViewObjectActivity {

    private static final String TAG = "ViewWorkActivity";

    // Views.
    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    @Override
    public int getObjectType() {
        return Constants.OBJECT_TYPE_WORK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_view_work);

        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        setProgressBar(mProgressBar);

        // Get the ARK name from the intent and load the data.
        String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);
        loadData(arkName);
    }

    @Override
    public void onDataLoaded(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDataLoaded()");
        }

        Work work = new Work(jsonObject);

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
            ImageLoaderCallbacks callbacks = new ImageLoaderCallbacks(this, imageUrl);
            getSupportLoaderManager().initLoader(IMAGE_LOADER, null, callbacks);
        } else {
            setImage(null);
        }

        hideNetworkError();

        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);

        // Show the ScrollView.
        mScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        setImage(bitmap);
    }

    // These methods below are used to update the UI with the loaded data.

    private void setImage(Bitmap image) {
        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            imageView.setImageResource(R.drawable.no_work_image);
        }
        findViewById(R.id.image_progress_bar).setVisibility(View.GONE);
    }

    private void setTitle(String title) {
        TextView titleTextView = (TextView) findViewById(R.id.title);
        if (title != null) {
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
    }

    private void setCreator(String creatorLabel) {
        TextView authorTextView = (TextView) findViewById(R.id.author);
        if (creatorLabel != null) {
            authorTextView.setText(creatorLabel);
        } else {
            authorTextView.setVisibility(View.GONE);
        }
    }

    private void setLanguage(String language) {
        TextView languageLabelTextView = (TextView) findViewById(R.id.language_label);
        TextView languageTextView = (TextView) findViewById(R.id.language);
        if (language != null) {
            languageTextView.setText(language);
        } else {
            languageLabelTextView.setVisibility(View.GONE);
            languageTextView.setVisibility(View.GONE);
        }
    }

    private void setDate(String date) {
        TextView dateLabelTextView = (TextView) findViewById(R.id.date_label);
        TextView dateTextView = (TextView) findViewById(R.id.date);
        if (date != null) {
            dateTextView.setText(date);
        } else {
            dateLabelTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
        }
    }

    private void setDescription(String[] description) {
        TextView descriptionLabelTextView = (TextView) findViewById(R.id.description_label);
        TextView descriptionTextView = (TextView) findViewById(R.id.description);
        if (description != null) {
            descriptionTextView.setText(TextUtils.join("\n", description));
        } else {
            descriptionLabelTextView.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.GONE);
        }
    }

    private void setSubjects(String[] subjects) {
        TextView subjectsLabelTextView = (TextView) findViewById(R.id.subjects_label);
        TextView subjectsTextView = (TextView) findViewById(R.id.subjects);
        if (subjects != null) {
            subjectsTextView.setText(TextUtils.join("\n", subjects));
        } else {
            subjectsLabelTextView.setVisibility(View.GONE);
            subjectsTextView.setVisibility(View.GONE);
        }
    }

    private void setAltForms(String[] altForms) {
        TextView altFormsLabelTextView = (TextView) findViewById(R.id.alt_forms_label);
        TextView altFormsTextView = (TextView) findViewById(R.id.alt_forms);
        if (altForms != null) {
            altFormsTextView.setText(TextUtils.join("\n", altForms));
        } else {
            altFormsLabelTextView.setVisibility(View.GONE);
            altFormsTextView.setVisibility(View.GONE);
        }
    }

    private void setEditorialNotes(String[][] editorialNotes) {
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

    private void setExternalLinks(String catalogueUrl, String wikipediaUrl) {
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

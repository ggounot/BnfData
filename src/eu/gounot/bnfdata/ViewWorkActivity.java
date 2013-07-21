package eu.gounot.bnfdata;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import eu.gounot.bnfdata.data.Work;
import eu.gounot.bnfdata.util.Constants;
import eu.gounot.bnfdata.view.ExpandableTextView;
import eu.gounot.bnfdata.view.ExpandableTextView.OnExpandListener;

public class ViewWorkActivity extends ViewObjectActivity {

    private static final String TAG = "ViewWorkActivity";

    private static final String EDIT_NOTES_EXPANDED_KEY = "editorialNotesExpanded";
    private static final String ALT_FORMS_EXPANDED_KEY = "altFormsExpanded";

    // Views.
    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    // ExpandableTextViews states.
    private boolean mAltFormsExpanded = false;
    private boolean mEditorialNotesExpanded = false;

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

        if (savedInstanceState != null) {
            mAltFormsExpanded = savedInstanceState.getBoolean(ALT_FORMS_EXPANDED_KEY);
            mEditorialNotesExpanded = savedInstanceState.getBoolean(EDIT_NOTES_EXPANDED_KEY);
        }

        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        setProgressBar(mProgressBar);

        // Get the ARK name from the intent and load the data.
        String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);
        loadData(arkName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ALT_FORMS_EXPANDED_KEY, mAltFormsExpanded);
        outState.putBoolean(EDIT_NOTES_EXPANDED_KEY, mEditorialNotesExpanded);
    }

    @Override
    public void onDataLoaded(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDataLoaded()");
        }

        Work work = new Work(jsonObject);

        setTitle(work.getTitle());
        setCreator(work.getCreatorLabel(), work.getCreatorArkName());
        setDate(work.getDate());
        setLanguage(work.getLanguageName());
        setDate(work.getDate());
        setDescription(work.getDescription());
        setSubjects(work.getSubjects());
        setAltForms(work.getAltForms());
        setEditorialNotes(work.getEditorialNotes());
        setExternalLinks(work.getCatalogueUrl(), work.getWikipediaUrl());

        // This piece of code below should ideally be used to load the image from a URL provided in
        // the JSON data, but since for now it is not possible to obtain the URL of the selected
        // images that are used on the data.bnf.fr pages from the RDF data (The bnf-onto:depiction
        // property is not yet implemented), we bypass this issue by downloading the JSON data from
        // data.bnf.fr to get the image URL and then we download the image, all this using the
        // DataBnfFrImageLoader. If in the future the bnf-onto:depiction property is eventually
        // implemented, we would be able to remove all this shame code and use the below piece of
        // code instead.
        // String imageUrl = work.getImageUrl();
        // if (imageUrl != null) {
        //     ImageLoaderCallbacks callbacks = new ImageLoaderCallbacks(this, imageUrl);
        //     getSupportLoaderManager().initLoader(IMAGE_LOADER, null, callbacks);
        // } else {
        //     setImage(null);
        // }

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

    private void setCreator(String creatorLabel, final String creatorArkName) {
        TextView authorTextView = (TextView) findViewById(R.id.author);
        if (creatorLabel != null) {
            authorTextView.setText(creatorLabel);
            authorTextView.setMovementMethod(LinkMovementMethod.getInstance());
            Spannable spannableCreatorLabel = (Spannable) authorTextView.getText();
            ClickableSpan clickableSpan = new ClickableSpan() {

                @Override
                public void onClick(View widget) {
                    Intent viewAuthorIntent = new Intent(getApplicationContext(),
                            ViewAuthorActivity.class);
                    viewAuthorIntent.putExtra(Constants.INTENT_ARK_NAME_KEY, creatorArkName);
                    startActivity(viewAuthorIntent);
                }
            };
            spannableCreatorLabel.setSpan(clickableSpan, 0, spannableCreatorLabel.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        ExpandableTextView altFormsTextView = (ExpandableTextView) findViewById(R.id.alt_forms);
        if (altForms != null) {
            altFormsTextView.setText(TextUtils.join("\n", altForms));
            altFormsTextView.setMustStartExpanded(mAltFormsExpanded);
            altFormsTextView.setOnExpandListener(new OnExpandListener() {

                @Override
                public void onMeasureComplete(boolean expandable) {
                }

                @Override
                public void onExpand() {
                    mAltFormsExpanded = true;
                }

                @Override
                public void onCollapse() {
                    mAltFormsExpanded = false;
                }
            });
        } else {
            altFormsLabelTextView.setVisibility(View.GONE);
            altFormsTextView.setVisibility(View.GONE);
        }
    }

    private void setEditorialNotes(String[][] editorialNotes) {
        TextView editorialNotesLabelTextView = (TextView) findViewById(R.id.editorial_notes_label);
        final ExpandableTextView editorialNotesTextView = (ExpandableTextView) findViewById(R.id.editorial_notes);
        if (editorialNotes != null) {
            String[] noteGroups = new String[editorialNotes.length];
            for (int i = 0; i < editorialNotes.length; i++) {
                noteGroups[i] = TextUtils.join("\n", editorialNotes[i]);
            }
            final String notes = TextUtils.join("\n\n", noteGroups);
            editorialNotesTextView.setText(notes);
            editorialNotesTextView.setMustStartExpanded(mEditorialNotesExpanded);
            if (mEditorialNotesExpanded) {
                Linkify.addLinks(editorialNotesTextView, Linkify.WEB_URLS);
            }
            editorialNotesTextView.setOnExpandListener(new OnExpandListener() {

                @Override
                public void onMeasureComplete(boolean expandable) {
                    if (!expandable) {
                        Linkify.addLinks(editorialNotesTextView, Linkify.WEB_URLS);
                    }
                }

                @Override
                public void onExpand() {
                    Linkify.addLinks(editorialNotesTextView, Linkify.WEB_URLS);
                    mEditorialNotesExpanded = true;
                }

                @Override
                public void onCollapse() {
                    editorialNotesTextView.setText(notes); // unlinkify
                    mEditorialNotesExpanded = false;
                }
            });
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

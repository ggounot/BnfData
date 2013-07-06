package eu.gounot.bnfdata;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import eu.gounot.bnfdata.data.DataObject;
import eu.gounot.bnfdata.data.Organization;
import eu.gounot.bnfdata.loadercallbacks.OrganizationImageLoaderCallbacks;
import eu.gounot.bnfdata.util.Constants;

public class ViewOrganizationActivity extends ViewObjectActivity {

    private static final String TAG = "ViewOrganizationActivity";

    // Views.
    private View mProgressBar;
    private View mScrollView;

    @Override
    public int getObjectType() {
        return Constants.OBJECT_TYPE_ORGANIZATION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_view_organization);

        mProgressBar = findViewById(R.id.progress_bar);
        mScrollView = findViewById(R.id.scrollview);

        // Get the ARK name from the intent and load the data.
        String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);
        loadData(arkName);
    }

    @Override
    public void onDataLoaded(DataObject dataObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDataLoaded()");
        }

        Organization organization = (Organization) dataObject;

        setName(organization.getName());
        setCountry(organization.getCountry());
        setLanguage(organization.getLanguageName());
        setCreationDate(organization.getCreationDate());
        setTerminationDate(organization.getTerminationDate());
        setHomePage(organization.getHomePage());
        setHistory(organization.getHistory());
        setFieldsOfActivity(organization.getFieldsOfActivity());
        setAltForms(organization.getAltForms());
        setEditorialNotes(organization.getEditorialNotes());
        setExternalLinks(organization.getCatalogueUrl(), organization.getWikipediaUrl());

        String imageUrl = organization.getImageUrl();
        if (imageUrl != null) {
            OrganizationImageLoaderCallbacks callbacks = new OrganizationImageLoaderCallbacks(this,
                    imageUrl);
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

    private void setName(String name) {
        TextView nameTextView = (TextView) findViewById(R.id.name);
        if (name != null) {
            nameTextView.setText(name);
        } else {
            nameTextView.setVisibility(View.GONE);
        }
    }

    private void setCountry(String country) {
        TextView countryLabelTextView = (TextView) findViewById(R.id.country_label);
        TextView countryTextView = (TextView) findViewById(R.id.country);
        if (country != null) {
            countryTextView.setText(country);
        } else {
            countryLabelTextView.setVisibility(View.GONE);
            countryTextView.setVisibility(View.GONE);
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

    private void setCreationDate(String creationDate) {
        TextView creationDateLabelTextView = (TextView) findViewById(R.id.creation_date_label);
        TextView creationDateTextView = (TextView) findViewById(R.id.creation_date);
        if (creationDate != null) {
            creationDateTextView.setText(creationDate);
        } else {
            creationDateLabelTextView.setVisibility(View.GONE);
            creationDateTextView.setVisibility(View.GONE);
        }
    }

    private void setTerminationDate(String terminationDate) {
        TextView terminationDateLabelTextView = (TextView) findViewById(R.id.termination_date_label);
        TextView terminationDateTextView = (TextView) findViewById(R.id.termination_date);
        if (terminationDate != null) {
            terminationDateTextView.setText(terminationDate);
        } else {
            terminationDateLabelTextView.setVisibility(View.GONE);
            terminationDateTextView.setVisibility(View.GONE);
        }
    }

    private void setHomePage(String homePage) {
        TextView homePageDateLabelTextView = (TextView) findViewById(R.id.home_page_label);
        TextView homePageDateTextView = (TextView) findViewById(R.id.home_page);
        if (homePage != null) {
            homePageDateTextView.setText(homePage);
        } else {
            homePageDateLabelTextView.setVisibility(View.GONE);
            homePageDateTextView.setVisibility(View.GONE);
        }
    }

    private void setHistory(String[] history) {
        TextView historyLabelTextView = (TextView) findViewById(R.id.history_label);
        TextView historyTextView = (TextView) findViewById(R.id.history);
        if (history != null) {
            historyTextView.setText(TextUtils.join("\n", history));
        } else {
            historyLabelTextView.setVisibility(View.GONE);
            historyTextView.setVisibility(View.GONE);
        }
    }

    private void setFieldsOfActivity(String[] fieldsOfActivity) {
        TextView fieldsOfActivityLabelTextView = (TextView) findViewById(R.id.fields_of_activity_label);
        TextView fieldsOfActivityTextView = (TextView) findViewById(R.id.fields_of_activity);
        if (fieldsOfActivity != null) {
            fieldsOfActivityTextView.setText(TextUtils.join("\n", fieldsOfActivity));
        } else {
            fieldsOfActivityLabelTextView.setVisibility(View.GONE);
            fieldsOfActivityTextView.setVisibility(View.GONE);
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

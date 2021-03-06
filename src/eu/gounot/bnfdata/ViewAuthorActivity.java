package eu.gounot.bnfdata;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import eu.gounot.bnfdata.adapter.WorkAdapter;
import eu.gounot.bnfdata.data.Author;
import eu.gounot.bnfdata.util.Constants;
import eu.gounot.bnfdata.view.ExpandableTextView;
import eu.gounot.bnfdata.view.ExpandableTextView.OnExpandListener;

public class ViewAuthorActivity extends ViewObjectActivity implements OnItemClickListener {

    private static final String TAG = "ViewAuthorActivity";

    private static final String EDIT_NOTES_EXPANDED_KEY = "editorialNotesExpanded";
    private static final String ALT_FORMS_EXPANDED_KEY = "altFormsExpanded";

    // Views.
    private ProgressBar mProgressBar;
    private ListView mListView;

    // ExpandableTextViews states.
    private boolean mAltFormsExpanded = false;
    private boolean mEditorialNotesExpanded = false;

    @Override
    public int getObjectType() {
        return Constants.OBJECT_TYPE_PERSON;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_view_author);

        if (savedInstanceState != null) {
            mAltFormsExpanded = savedInstanceState.getBoolean(ALT_FORMS_EXPANDED_KEY);
            mEditorialNotesExpanded = savedInstanceState.getBoolean(EDIT_NOTES_EXPANDED_KEY);
        }

        mListView = (ListView) findViewById(R.id.list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        setProgressBar(mProgressBar);

        // Set up the GridLayout (in which all the TextViews are)
        // as the ListView's header.
        View header = View.inflate(this, R.layout.activity_view_author_header, null);
        mListView.addHeaderView(header, null, false);

        // Set up a transparent footer that serves as a padding between
        // the ListView's items and the bottom of the screen.
        View footer = View.inflate(this, R.layout.listview_footer, null);
        mListView.addFooterView(footer, null, false);

        mListView.setOnItemClickListener(this);

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

        Author author = new Author(jsonObject);

        setName(author.getGivenName(), author.getFamilyName());
        setDates(author.getDates());
        setCountry(author.getCountry());
        setLanguage(author.getLanguageName());
        setGender(author.getGender());
        setDateOfBirth(author.getDateOfBirth(), author.getPlaceOfBirth());
        setDateOfDeath(author.getDateOfDeath(), author.getPlaceOfDeath());
        setBiographicalInfos(author.getBiographicalInfos());
        setFieldsOfActivity(author.getFieldsOfActivity());
        setAltForms(author.getAltForms());
        setEditorialNotes(author.getEditorialNotes());
        setExternalLinks(author.getCatalogueUrl(), author.getWikipediaUrl());
        setWorks(author.getWorks());

        // This piece of code below should ideally be used to load the image from a URL provided in
        // the JSON data, but since for now it is not possible to obtain the URL of the selected
        // images that are used on the data.bnf.fr pages from the RDF data (The bnf-onto:depiction
        // property is not yet implemented), we bypass this issue by downloading the JSON data from
        // data.bnf.fr to get the image URL and then we download the image, all this using the
        // DataBnfFrImageLoader. If in the future the bnf-onto:depiction property is eventually
        // implemented, we would be able to remove all this shame code and use the below piece of
        // code instead.
        // String imageUrl = author.getImageUrl();
        // if (imageUrl != null) {
        //     ImageLoaderCallbacks callbacks = new ImageLoaderCallbacks(this, imageUrl);
        //     getSupportLoaderManager().initLoader(IMAGE_LOADER, null, callbacks);
        // } else {
        //     setImage(null);
        // }

        hideNetworkError();

        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);

        // Show the ListView.
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        setImage(bitmap);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onItemClick() position=" + position);
        }

        // Create an intent, put in the selected work's URL,
        // and start the ViewWorkActivity to view this work in detail.
        Intent intent = new Intent(this, ViewWorkActivity.class);
        Author.Work work = (Author.Work) mListView.getAdapter().getItem(position);
        intent.putExtra(Constants.INTENT_ARK_NAME_KEY, work.getArkName());
        startActivity(intent);
    }

    // These methods below are used to update the UI with the loaded data.

    private void setImage(Bitmap image) {
        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            imageView.setImageResource(R.drawable.no_author_or_organization_image);
        }
        findViewById(R.id.image_progress_bar).setVisibility(View.GONE);
    }

    private void setName(String givenName, String familyName) {
        TextView nameTextView = (TextView) findViewById(R.id.name);
        String name = "";
        if (givenName != null) {
            name = givenName + " ";
        }
        if (familyName != null) {
            name += familyName;
        }
        if (!name.equals("")) {
            nameTextView.setText(name);
        } else {
            nameTextView.setVisibility(View.GONE);
        }
    }

    private void setDates(String dates) {
        TextView datesTextView = (TextView) findViewById(R.id.dates);
        if (dates != null) {
            datesTextView.setText(dates);
        } else {
            datesTextView.setVisibility(View.GONE);
        }
    }

    private void setCountry(String nationalities) {
        TextView countryLabelTextView = (TextView) findViewById(R.id.country_label);
        TextView countryTextView = (TextView) findViewById(R.id.country);
        if (nationalities != null) {
            countryTextView.setText(nationalities);
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

    private void setGender(String gender) {
        TextView genderLabelTextView = (TextView) findViewById(R.id.gender_label);
        TextView genderTextView = (TextView) findViewById(R.id.gender);
        if (gender != null) {
            genderTextView.setText(gender);
        } else {
            genderLabelTextView.setVisibility(View.GONE);
            genderTextView.setVisibility(View.GONE);
        }
    }

    private void setDateOfBirth(String dateOfBirth, String placeOfBirth) {
        TextView dateOfBirthLabelTextView = (TextView) findViewById(R.id.date_of_birth_label);
        TextView dateOfBirthTextView = (TextView) findViewById(R.id.date_of_birth);
        String birth = "";
        if (dateOfBirth != null) {
            birth = dateOfBirth;
        }
        if (placeOfBirth != null) {
            if (!birth.equals("")) {
                birth += ", ";
            }
            birth += placeOfBirth;
        }
        if (!birth.equals("")) {
            dateOfBirthTextView.setText(birth);
        } else {
            dateOfBirthLabelTextView.setVisibility(View.GONE);
            dateOfBirthTextView.setVisibility(View.GONE);
        }
    }

    private void setDateOfDeath(String dateOfDeath, String placeOfDeath) {
        TextView dateOfDeathLabelTextView = (TextView) findViewById(R.id.date_of_death_label);
        TextView dateOfDeathTextView = (TextView) findViewById(R.id.date_of_death);
        String death = "";
        if (dateOfDeath != null) {
            death = dateOfDeath;
        }
        if (placeOfDeath != null) {
            if (!death.equals("")) {
                death += ", ";
            }
            death += placeOfDeath;
        }
        if (!death.equals("")) {
            dateOfDeathTextView.setText(death);
        } else {
            dateOfDeathLabelTextView.setVisibility(View.GONE);
            dateOfDeathTextView.setVisibility(View.GONE);
        }
    }

    private void setBiographicalInfos(String[] biographicalInfos) {
        TextView biographicalInfosLabelTextView = (TextView) findViewById(R.id.notes_label);
        TextView biographicalInfosTextView = (TextView) findViewById(R.id.notes);
        if (biographicalInfos != null) {
            biographicalInfosTextView.setText(TextUtils.join("\n", biographicalInfos));
        } else {
            biographicalInfosLabelTextView.setVisibility(View.GONE);
            biographicalInfosTextView.setVisibility(View.GONE);
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

    private void setWorks(Author.Work[] works) {
        WorkAdapter adapter = new WorkAdapter(this, works);
        mListView.setAdapter(adapter);
    }

}

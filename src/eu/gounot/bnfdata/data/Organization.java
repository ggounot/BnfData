package eu.gounot.bnfdata.data;

import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.JsonHelper;

public class Organization {

    private static final String TAG = "Work";

    private static final String JSON_IMAGE_URL = "imageUrl";
    private static final String JSON_NAME = "name";
    private static final String JSON_COUNTRY = "country";
    private static final String JSON_LANGUAGE_NAME = "languageName";
    private static final String JSON_LANGUAGE_ARK_NAME = "languageId";
    private static final String JSON_CREATION_DATE = "creationDate";
    private static final String JSON_TERMINATION_DATE = "terminationDate";
    private static final String JSON_HOME_PAGE = "homePage";
    private static final String JSON_HISTORY = "history";
    private static final String JSON_FIELDS_OF_ACTIVITY = "fieldsOfActivity";
    private static final String JSON_ALT_FORMS = "altLabels";
    private static final String JSON_EDITORIAL_NOTES = "editorialNotes";
    private static final String JSON_CATALOGUE_URL = "catalogueUrl";
    private static final String JSON_WIKIPEDIA_URL = "wikipediaUrl";

    private String mImageUrl;
    private String mName;
    private String mCountry;
    private String mLanguageName;
    private String mLanguageArkName;
    private String mCreationDate;
    private String mTerminationDate;
    private String mHomePage;
    private String[] mHistory;
    private String[] mFieldsOfActivity;
    private String[] mAltForms;
    private String[][] mEditorialNotes;
    private String mCatalogueUrl;
    private String mWikipediaUrl;

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getName() {
        return mName;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getLanguageName() {
        return mLanguageName;
    }

    public String getLanguageArkName() {
        return mLanguageArkName;
    }

    public String getCreationDate() {
        return mCreationDate;
    }

    public String getTerminationDate() {
        return mTerminationDate;
    }

    public String getHomePage() {
        return mHomePage;
    }

    public String[] getHistory() {
        return mHistory;
    }

    public String[] getFieldsOfActivity() {
        return mFieldsOfActivity;
    }

    public String[] getAltForms() {
        return mAltForms;
    }

    public String[][] getEditorialNotes() {
        return mEditorialNotes;
    }

    public String getCatalogueUrl() {
        return mCatalogueUrl;
    }

    public String getWikipediaUrl() {
        return mWikipediaUrl;
    }

    public Organization(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Organization()");
        }

        loadJsonData(jsonObject);
    }

    private void loadJsonData(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadJsonData()");
        }

        mImageUrl = JsonHelper.getStringOrNull(jsonObject, JSON_IMAGE_URL);
        mName = JsonHelper.getStringOrNull(jsonObject, JSON_NAME);
        mCountry = JsonHelper.getStringOrNull(jsonObject, JSON_COUNTRY);
        mLanguageName = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_NAME);
        mLanguageArkName = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_ARK_NAME);
        mCreationDate = JsonHelper.getStringOrNull(jsonObject, JSON_CREATION_DATE);
        mTerminationDate = JsonHelper.getStringOrNull(jsonObject, JSON_TERMINATION_DATE);
        mHomePage = JsonHelper.getStringOrNull(jsonObject, JSON_HOME_PAGE);
        mHistory = JsonHelper.getStringArrayOrNull(jsonObject, JSON_HISTORY);
        mFieldsOfActivity = JsonHelper.getStringArrayOrNull(jsonObject, JSON_FIELDS_OF_ACTIVITY);
        mAltForms = JsonHelper.getStringArrayOrNull(jsonObject, JSON_ALT_FORMS);
        mEditorialNotes = JsonHelper.get2DStringArrayOrNull(jsonObject, JSON_EDITORIAL_NOTES);
        mCatalogueUrl = JsonHelper.getStringOrNull(jsonObject, JSON_CATALOGUE_URL);
        mWikipediaUrl = JsonHelper.getStringOrNull(jsonObject, JSON_WIKIPEDIA_URL);
    }

}

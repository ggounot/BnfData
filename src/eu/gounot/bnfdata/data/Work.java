package eu.gounot.bnfdata.data;

import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.JsonHelper;

public class Work {

    private static final String TAG = "Work";

    private static final String JSON_IMAGE_URL = "imageUrl";
    private static final String JSON_TITLE = "prefLabel";
    private static final String JSON_CREATOR_LABEL = "creatorLabel";
    private static final String JSON_CREATOR_TYPE = "creatorType";
    private static final String JSON_CREATOR_ARK_NAME = "creatorId";
    private static final String JSON_LANGUAGE_NAME = "languageName";
    private static final String JSON_LANGUAGE_ARK_NAME = "languageId";
    private static final String JSON_DATE = "date";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_SUBJECTS = "subjects";
    private static final String JSON_ALT_FORMS = "altLabels";
    private static final String JSON_EDITORIAL_NOTES = "editorialNotes";
    private static final String JSON_CATALOGUE_URL = "catalogueUrl";
    private static final String JSON_WIKIPEDIA_URL = "wikipediaUrl";

    private String mImageUrl;
    private String mTitle;
    private String mCreatorLabel;
    private String mCreatorType;
    private String mCreatorArkName;
    private String mLanguageName;
    private String mLanguageArkName;
    private String mDate;
    private String[] mDescription;
    private String[] mSubjects;
    private String[] mAltForms;
    private String[][] mEditorialNotes;
    private String mCatalogueUrl;
    private String mWikipediaUrl;

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getCreatorLabel() {
        return mCreatorLabel;
    }

    public String getCreatorType() {
        return mCreatorType;
    }

    public String getCreatorArkName() {
        return mCreatorArkName;
    }

    public String getLanguageName() {
        return mLanguageName;
    }

    public String getLanguageArkName() {
        return mLanguageArkName;
    }

    public String getDate() {
        return mDate;
    }

    public String[] getDescription() {
        return mDescription;
    }

    public String[] getSubjects() {
        return mSubjects;
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

    public Work(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Work()");
        }

        loadJsonData(jsonObject);
    }

    private void loadJsonData(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadJsonData()");
        }

        mImageUrl = JsonHelper.getStringOrNull(jsonObject, JSON_IMAGE_URL);
        mTitle = JsonHelper.getStringOrNull(jsonObject, JSON_TITLE);
        mCreatorLabel = JsonHelper.getStringOrNull(jsonObject, JSON_CREATOR_LABEL);
        mCreatorType = JsonHelper.getStringOrNull(jsonObject, JSON_CREATOR_TYPE);
        mCreatorArkName = JsonHelper.getStringOrNull(jsonObject, JSON_CREATOR_ARK_NAME);
        mLanguageName = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_NAME);
        mLanguageArkName = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_ARK_NAME);
        mDate = JsonHelper.getStringOrNull(jsonObject, JSON_DATE);
        mDescription = JsonHelper.getStringArrayOrNull(jsonObject, JSON_DESCRIPTION);
        mSubjects = JsonHelper.getStringArrayOrNull(jsonObject, JSON_SUBJECTS);
        mAltForms = JsonHelper.getStringArrayOrNull(jsonObject, JSON_ALT_FORMS);
        mEditorialNotes = JsonHelper.get2DStringArrayOrNull(jsonObject, JSON_EDITORIAL_NOTES);
        mCatalogueUrl = JsonHelper.getStringOrNull(jsonObject, JSON_CATALOGUE_URL);
        mWikipediaUrl = JsonHelper.getStringOrNull(jsonObject, JSON_WIKIPEDIA_URL);
    }

}

package eu.gounot.bnfdata.data;

import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.JsonHelper;

public class Author {

    private static final String TAG = "Author";

    private static final String JSON_IMAGE_URL = "imageUrl";
    private static final String JSON_GIVEN_NAME = "givenName";
    private static final String JSON_FAMILY_NAME = "familyName";
    private static final String JSON_DATES = "dates";
    private static final String JSON_COUNTRY = "country";
    private static final String JSON_LANGUAGE_NAME = "languageName";
    private static final String JSON_LANGUAGE_URI = "languageUri";
    private static final String JSON_GENDER = "gender";
    private static final String JSON_DATE_OF_BIRTH = "dateOfBirth";
    private static final String JSON_PLACE_OF_BIRTH = "placeOfBirth";
    private static final String JSON_DATE_OF_DEATH = "dateOfDeath";
    private static final String JSON_PLACE_OF_DEATH = "placeOfDeath";
    private static final String JSON_BIOGRAPHICAL_INFOS = "biographicalInformation";
    private static final String JSON_FIELDS_OF_ACTIVITY = "fieldOfActivity";
    private static final String JSON_ALT_FORMS = "altLabels";
    private static final String JSON_EDITORIAL_NOTES = "editorialNotes";
    private static final String JSON_CATALOGUE_URL = "catalogueUrl";
    private static final String JSON_WIKIPEDIA_URL = "wikipediaUrl";
    private static final String JSON_WORKS = "works";
    public static final String JSON_WORK_TITLE = "workTitle";
    public static final String JSON_WORK_DESCRIPTION = "workDescription";
    public static final String JSON_WORK_ARK_NAME = "workId";

    private String mImageUrl;
    private String mGivenName;
    private String mFamilyName;
    private String mDates;
    private String mCountry;
    private String mLanguageName;
    private String mLanguageUri;
    private String mGender;
    private String mDateOfBirth;
    private String mPlaceOfBirth;
    private String mDateOfDeath;
    private String mPlaceOfDeath;
    private String[] mBiographicalInfos;
    private String[] mFieldsOfActivity;
    private String[] mAltForms;
    private String[][] mEditorialNotes;
    private String mCatalogueUrl;
    private String mWikipediaUrl;
    private Work[] mWorks;

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getGivenName() {
        return mGivenName;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public String getDates() {
        return mDates;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getLanguageName() {
        return mLanguageName;
    }

    public String getLanguageUri() {
        return mLanguageUri;
    }

    public String getGender() {
        return mGender;
    }

    public String getDateOfBirth() {
        return mDateOfBirth;
    }

    public String getPlaceOfBirth() {
        return mPlaceOfBirth;
    }

    public String getDateOfDeath() {
        return mDateOfDeath;
    }

    public String getPlaceOfDeath() {
        return mPlaceOfDeath;
    }

    public String[] getBiographicalInfos() {
        return mBiographicalInfos;
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

    public Work[] getWorks() {
        return mWorks;
    }

    public Author(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Author()");
        }

        loadJsonData(jsonObject);
    }

    private void loadJsonData(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadJsonData()");
        }

        mImageUrl = JsonHelper.getStringOrNull(jsonObject, JSON_IMAGE_URL);
        mGivenName = JsonHelper.getStringOrNull(jsonObject, JSON_GIVEN_NAME);
        mFamilyName = JsonHelper.getStringOrNull(jsonObject, JSON_FAMILY_NAME);
        mDates = JsonHelper.getStringOrNull(jsonObject, JSON_DATES);
        mCountry = JsonHelper.getStringOrNull(jsonObject, JSON_COUNTRY);
        mLanguageName = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_NAME);
        mLanguageUri = JsonHelper.getStringOrNull(jsonObject, JSON_LANGUAGE_URI);
        mGender = JsonHelper.getStringOrNull(jsonObject, JSON_GENDER);
        mDateOfBirth = JsonHelper.getStringOrNull(jsonObject, JSON_DATE_OF_BIRTH);
        mPlaceOfBirth = JsonHelper.getStringOrNull(jsonObject, JSON_PLACE_OF_BIRTH);
        mDateOfDeath = JsonHelper.getStringOrNull(jsonObject, JSON_DATE_OF_DEATH);
        mPlaceOfDeath = JsonHelper.getStringOrNull(jsonObject, JSON_PLACE_OF_DEATH);
        mBiographicalInfos = JsonHelper.getStringArrayOrNull(jsonObject, JSON_BIOGRAPHICAL_INFOS);
        mFieldsOfActivity = JsonHelper.getStringArrayOrNull(jsonObject, JSON_FIELDS_OF_ACTIVITY);
        mAltForms = JsonHelper.getStringArrayOrNull(jsonObject, JSON_ALT_FORMS);
        mEditorialNotes = JsonHelper.get2DStringArrayOrNull(jsonObject, JSON_EDITORIAL_NOTES);
        mCatalogueUrl = JsonHelper.getStringOrNull(jsonObject, JSON_CATALOGUE_URL);
        mWikipediaUrl = JsonHelper.getStringOrNull(jsonObject, JSON_WIKIPEDIA_URL);
        mWorks = JsonHelper.getWorkArray(jsonObject, JSON_WORKS);
    }

    public static class Work {

        private String mTitle;
        private String mDescription;
        private String mArkName;

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getArkName() {
            return mArkName;
        }

        public Work(String title, String description, String arkName) {
            mTitle = title;
            mDescription = description;
            mArkName = arkName;
        }

    }

}

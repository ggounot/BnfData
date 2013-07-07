package eu.gounot.bnfdata.util;

public class Constants {

    public static final String ASSETS_COMPRESSED_DB_FILENAME = "suggestions.jpg";
    public static final String DB_FILENAME = "suggestions.db";

    public static final int UNCOMPRESSED_DB_SIZE = 26257408;
    public static final int PROGRESS_MAX = 100;
    public static final int DB_VERSION = 1;

    public static final String PREFS_FILE_NAME = "prefs";
    public static final String PREF_DB_STATE_KEY = "dbstate";
    public static final String PREF_DB_VERSION_KEY = "dbversion";

    public static final int DB_NOT_INSTALLED = 0;
    public static final int DB_INSTALL_ABORTED = 1;
    public static final int DB_INSTALLED = 2;

    public static final String JSON_OBJECT_URL_FORMAT = "http://bnfdata.gounot.eu/data/2.0/%s/%s.json";

    public static final int OBJECT_TYPE_PERSON = 0;
    public static final int OBJECT_TYPE_WORK = 1;
    public static final int OBJECT_TYPE_ORGANIZATION = 2;
    public static final int OBJECT_TYPE_THEME = 3;

    public static final String[] OBJECT_TYPE_DIR = { "person", "work", "organization", "theme" };

    public static final String INTENT_ARK_NAME_KEY = "arkname";

    public static final String USER_AGENT = "BnfData-Android-App/2.0";
}

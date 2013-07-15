package eu.gounot.bnfdata.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.database.DatabaseOpenHelper;

public class SuggestionsProvider extends ContentProvider {

    private static final String TAG = "SuggestionsProvider";

    // SQL query.
    private static final String SQL_QUERY = "SELECT "
            + "s.rowid AS " + BaseColumns._ID
            + ", MIN(s.form) AS " + SearchManager.SUGGEST_COLUMN_TEXT_1
            + ", ot.label AS " + SearchManager.SUGGEST_COLUMN_TEXT_2
            + ", s.ark_name AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
            + ", s.object_type_id AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            + " FROM ("
                + "SELECT ark_name, "
                + "MIN(form_type_id) AS min_form_type_id "
                + "FROM suggestions "
                + "WHERE form MATCH ? "
                + "GROUP BY ark_name"
            + ") AS an "
            + "INNER JOIN suggestions AS s "
            + "ON s.ark_name = an.ark_name "
            + "AND s.form_type_id = an.min_form_type_id "
            + "INNER JOIN object_types AS ot "
            + "ON s.object_type_id = ot.id "
            + "WHERE s.form MATCH ? "
            + "GROUP BY s.ark_name "
            + "ORDER BY s.object_type_id ASC, s.form ASC";

    // Cursor's columns.
    public static final int CURSOR_COL_ID = 0; // _ID
    public static final int CURSOR_COL_FORM = 1; // SUGGEST_COLUMN_TEXT_1
    public static final int CURSOR_COL_OBJECT_TYPE_LABEL = 2; // SUGGEST_COLUMN_TEXT_2
    public static final int CURSOR_COL_ARK_NAME = 3; // SUGGEST_COLUMN_INTENT_DATA
    public static final int CURSOR_COL_OBJECT_TYPE = 4; // SUGGEST_COLUMN_INTENT_EXTRA_DATA

    // Content provider settings.
    public static final String AUTHORITY = "eu.gounot.bnfdata.provider.SuggestionsProvider";
    public static final String SEARCH_URI_PATH_QUERY = "search_query";
    public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SEARCH_URI_PATH_QUERY);

    // URI codes.
    private static final int SUGGEST_URI = 0; // Suggestion URI.
    private static final int SEARCH_URI = 1; // Search URI.

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Matchable URIs.
    static {
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGEST_URI);
        sUriMatcher.addURI(AUTHORITY, SEARCH_URI_PATH_QUERY, SEARCH_URI);
    }

    private DatabaseOpenHelper mDatabaseOpenHelper;

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        mDatabaseOpenHelper = new DatabaseOpenHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "query() uri=" + uri + " selection=" + selection + " selectionArgs[0]="
                    + selectionArgs[0]);
        }

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
        case SUGGEST_URI:
        case SEARCH_URI:
            String filter = selectionArgs[0].trim();
            // Search only if the filter is at least 3 characters.
            return (filter.length() < 3) ? null : getSuggestionsCursor(selection, filter);
        default:
            Log.e(TAG, "Invalid content URI.");
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getType() uri=" + uri);
        }

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
        case SUGGEST_URI:
        case SEARCH_URI:
            return SearchManager.SUGGEST_MIME_TYPE;
        default:
            throw new IllegalArgumentException("Invalid content URI");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private Cursor getSuggestionsCursor(String selection, String filter) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getSuggestionsCursor()");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();

        filter = appendWildcards(filter);
        String[] args = { filter, filter };

        return db.rawQuery(SQL_QUERY, args);
    }

    private String appendWildcards(String filter) {
        String[] words = filter.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            builder.append(word).append('*').append(' ');
        }

        return builder.toString();
    }

}

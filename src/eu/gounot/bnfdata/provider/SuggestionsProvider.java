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

    // SQLite tables.
    private static final String TABLE_SUGGESTIONS = "suggestions";
    private static final String TABLE_OBJECT_TYPES = "object_types";

    // Columns.
    private static final String COL_ID = TABLE_SUGGESTIONS + ".rowid";
    private static final String COL_FORM = TABLE_SUGGESTIONS + ".form";
    private static final String COL_FORM_TYPE = TABLE_SUGGESTIONS + ".form_type_id";
    private static final String COL_OBJECT_TYPE = TABLE_SUGGESTIONS + ".object_type_id";
    private static final String COL_ARK_NAME = TABLE_SUGGESTIONS + ".ark_name";
    private static final String COL_OBJECT_TYPE_LABEL = TABLE_OBJECT_TYPES + ".label";

    // Query elements.
    private static final String TABLES = TABLE_SUGGESTIONS + ", " + TABLE_OBJECT_TYPES;
    private static final String[] PROJECTION = { COL_ID + " AS " + BaseColumns._ID,
            COL_FORM + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
            COL_OBJECT_TYPE_LABEL + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2,
            COL_ARK_NAME + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            COL_OBJECT_TYPE + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA };
    private static final String GROUP_BY = COL_ARK_NAME;
    private static final String HAVING = COL_FORM_TYPE + " = MIN(" + COL_FORM_TYPE + ")";
    private static final String ORDER_BY = COL_OBJECT_TYPE + " ASC, " + COL_FORM + " ASC";

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
            return (filter.length() < 3) ? null : getSuggestionsCursor(selection, selectionArgs);
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

    private Cursor getSuggestionsCursor(String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getSuggestionsCursor()");
        }

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();

        selectionArgs[0] = appendWildcards(selectionArgs[0]);

        return db.query(TABLES, PROJECTION, selection, selectionArgs, GROUP_BY, HAVING, ORDER_BY);
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

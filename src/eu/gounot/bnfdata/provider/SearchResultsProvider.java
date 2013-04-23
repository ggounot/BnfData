package eu.gounot.bnfdata.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.R;
import eu.gounot.bnfdata.util.BnfDataWebsite;

public class SearchResultsProvider extends ContentProvider {

    private static final String TAG = "SearchResultsProvider";

    // Keys in a JSON object.
    public static final String LABEL = "label";
    public static final String CATEGORY = "category";
    public static final String RAW_CATEGORY = "raw_category";
    public static final String URL = "value";

    // Values associated to the raw_category key in a JSON object.
    public static final String AUTHOR = "Person";
    public static final String WORK = "Work";
    public static final String ORGANIZATION = "Org";
    public static final String THEME = "Rameau";

    public static final String AUTHORITY = "eu.gounot.bnfdata.provider.SearchResultsProvider";
    public static final String SEARCH_URI_PATH_QUERY = "search_query";
    public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SEARCH_URI_PATH_QUERY);

    // URI codes.
    private static final int SUGGEST = 0; // Suggestion URI without filter.
    private static final int SUGGEST_FILTER = 1; // Suggestion URI with filter.
    private static final int SEARCH = 2; // Search URI without filter.
    private static final int SEARCH_FILTER = 3; // Search URI with filter.

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Matchable URIs.
    static {
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGEST_FILTER);
        sUriMatcher.addURI(AUTHORITY, SEARCH_URI_PATH_QUERY, SEARCH);
        sUriMatcher.addURI(AUTHORITY, SEARCH_URI_PATH_QUERY + "/*", SEARCH_FILTER);
    }

    public static final String[] CURSOR_COLUMNS = { "_ID", SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA };

    // Cursor's columns indices.
    public static final int CURSOR_COLUMN_ID = 0; // _ID
    public static final int CURSOR_COLUMN_LABEL = 1; // SUGGEST_COLUMN_TEXT_1
    public static final int CURSOR_COLUMN_CATEGORY = 2; // SUGGEST_COLUMN_TEXT_2
    public static final int CURSOR_COLUMN_URL = 3; // SUGGEST_COLUMN_INTENT_DATA
    public static final int CURSOR_COLUMN_RAW_CATEGORY = 4; // SUGGEST_COLUMN_INTENT_EXTRA_DATA

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "query() uri=" + uri);
        }

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
        case SUGGEST:
        case SEARCH:
            // Cannot search because there is no filter provided in the URI.
            return null;
        case SUGGEST_FILTER:
        case SEARCH_FILTER:
            String filter = uri.getLastPathSegment();
            // Search only if the filter is at least 4 characters.
            return (filter.length() < 4) ? null : getSearchResultsCursor(filter);
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
        case SUGGEST:
        case SEARCH:
            return null;
        case SUGGEST_FILTER:
        case SEARCH_FILTER:
            String filter = uri.getLastPathSegment();
            return (filter.length() < 4) ? null : SearchManager.SUGGEST_MIME_TYPE;
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

    private Cursor getSearchResultsCursor(String filter) {
        Context context = getContext();
        JSONArray jsonArray;

        try {
            // Try to get the search results in JSON format from the BnF data website.
            jsonArray = BnfDataWebsite.getSearchResults(filter);
        } catch (UnsupportedEncodingException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(CURSOR_COLUMNS);
        String label;
        String raw_category;
        String category;
        String url;

        // Iterate through the JSON array to copy the results into the cursor.
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonRow;
            Object[] cursorRow = new Object[CURSOR_COLUMNS.length];
            cursorRow[CURSOR_COLUMN_ID] = i;

            try {
                jsonRow = jsonArray.getJSONObject(i);

                raw_category = jsonRow.getString(RAW_CATEGORY);

                // Themes (Rameau) are not supported so we skip them.
                if (raw_category.equalsIgnoreCase(THEME)) {
                    continue;
                }

                label = jsonRow.getString(LABEL);
                category = getDisplayableCategoryName(context, raw_category);
                url = jsonRow.getString(URL);

                cursorRow[CURSOR_COLUMN_LABEL] = label;
                cursorRow[CURSOR_COLUMN_RAW_CATEGORY] = raw_category;
                cursorRow[CURSOR_COLUMN_CATEGORY] = category;
                cursorRow[CURSOR_COLUMN_URL] = url;

                cursor.addRow(cursorRow);
            } catch (JSONException e) {
                cursor.close();
                Log.e(TAG, e.toString(), e);
                throw new RuntimeException(e);
            }
        }

        return cursor;
    }

    private String getDisplayableCategoryName(Context context, String category) {
        // Return a displayable category name for the given raw category name.
        if (category.equalsIgnoreCase(AUTHOR)) {
            return context.getString(R.string.category_person);
        } else if (category.equalsIgnoreCase(WORK)) {
            return context.getString(R.string.category_work);
        } else if (category.equalsIgnoreCase(ORGANIZATION)) {
            return context.getString(R.string.category_organization);
        } else if (category.equalsIgnoreCase(THEME)) {
            return context.getString(R.string.category_theme);
        } else {
            Log.e(TAG, "Unexpected category: " + category);
            return context.getString(R.string.category_unknown);
        }
    }

}

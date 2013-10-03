package eu.gounot.bnfdata.provider;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.Constants;

public class SuggestionsProvider extends ContentProvider {

    private static final String TAG = "SuggestionsProvider";

    private static final Collator FRENCH_COLLATOR = Collator.getInstance(Locale.FRENCH);

    // SQL query.
    private static final String SQL_QUERY = "SELECT "
            + "suggestions.rowid AS " + BaseColumns._ID
            + ", form AS " + SearchManager.SUGGEST_COLUMN_TEXT_1
            + ", ot.label AS " + SearchManager.SUGGEST_COLUMN_TEXT_2
            + ", ark_name AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
            + ", object_type_id AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            + " FROM suggestions"
            + " INNER JOIN object_types AS ot"
            + " ON suggestions.object_type_id = ot.id"
            + " WHERE form MATCH ?"
            + " ORDER BY object_type_id ASC, form_type_id ASC, form ASC";

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

    private static native boolean nativeInit();

    private native Cursor sqliteU61Query(String filename, String sql, String[] values);

    static {
        System.loadLibrary("sqlite-u61-jni");
        nativeInit();
    }

    String mDatabasePath = null;

    @Override
    public boolean onCreate() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        mDatabasePath = getContext().getApplicationInfo().dataDir + "/databases/"
                + Constants.DB_FILENAME;

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

        if (mDatabasePath == null) {
            return null;
        }

        String[] values = { appendWildcards(filter) };

        Cursor cursor = sqliteU61Query(mDatabasePath, SQL_QUERY, values);

        return new UniqueArkNameCursor(cursor);
    }

    private String appendWildcards(String filter) {
        String[] words = filter.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            builder.append(word).append('*').append(' ');
        }

        return builder.toString();
    }

    private class UniqueArkNameCursor extends CursorWrapper {

        private int[] mPositionMap;
        private int mCount = 0;
        private int mPos = 0;

        public UniqueArkNameCursor(Cursor cursor) {
            super(cursor);

            int cursorRowsCount = super.getCount();

            // While iterating the cursor, we will add the ARK names in this HashSet. This way we
            // will know whether an ARK name was already added so we can skip the duplicates. Since
            // the query has returned the rows in good order, we will keep a row when its ARK name
            // is encountered for the first time and reject the following rows that has the same ARK
            // name.
            HashSet<String> arkNames = new HashSet<String>(cursorRowsCount);

            // This array will serve to map the positions of the CursorWrapper with those of the
            // actual cursor.
            mPositionMap = new int[cursorRowsCount];

            // This variable will hold the currently treated object type.
            int curObjectType = -1;

            // This variable will hold the index of the currently treated form over the currently
            // treated object type.
            int curObjectTypeFormIndex = 0;

            // These arrays will respectively hold the positions and the forms of the currently
            // treated form over the currently treated object type.
            Integer[] curObjectTypeFormsPos = new Integer[cursorRowsCount];
            String[] curObjectTypeForms = new String[cursorRowsCount];

            for (int i = 0; i < cursorRowsCount; i++) {
                super.moveToPosition(i);

                if (!arkNames.add(getString(CURSOR_COL_ARK_NAME))) {
                    // This current row's ARK name was already encountered in a preceding row so we
                    // skip this current row.
                    continue;
                }

                int objectType = getInt(CURSOR_COL_OBJECT_TYPE);

                if (objectType != curObjectType && curObjectType != -1) {
                    // The object type changes from this row on, so we can sort the forms and map
                    // the positions of the rows of the previous object type.
                    sortFormsAndMapPositions(curObjectTypeFormsPos, curObjectTypeForms,
                            curObjectTypeFormIndex);
                }

                if (objectType != curObjectType) {
                    // The object type changes from this row on, or this is the first row, so we
                    // initialize variables according to this new object type.
                    curObjectType = objectType;
                    curObjectTypeFormIndex = 0;

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Treating object type " + curObjectType);
                    }
                }

                // Save the position and form of the current row.
                curObjectTypeFormsPos[curObjectTypeFormIndex] = i;
                curObjectTypeForms[curObjectTypeFormIndex] = getString(CURSOR_COL_FORM);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Form " + curObjectTypeForms[curObjectTypeFormIndex] + " position="
                            + curObjectTypeFormsPos[curObjectTypeFormIndex]);
                }

                curObjectTypeFormIndex++;
            }

            // The iteration is over but the last rows corresponding to the last object type were
            // not sorted nor mapped yet. So we do it now.
            sortFormsAndMapPositions(curObjectTypeFormsPos, curObjectTypeForms,
                    curObjectTypeFormIndex);

            super.moveToFirst();
        }

        private void sortFormsAndMapPositions(Integer[] positions, String[] forms, int count) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Mapping positions of " + count + " forms");
            }

            // This comparator will serve to sort the indexes of the forms according to the forms.
            FormPositionComparator comparator = new FormPositionComparator(forms, count);

            // Get an array of the forms' indexes.
            Integer[] formsIndexes = comparator.createIndexArray();

            // Sort the indexes.
            Arrays.sort(formsIndexes, comparator);

            for (int j = 0; j < count; j++) {
                // Map the actual positions according to the sorted forms' indexes.
                mPositionMap[mCount++] = positions[formsIndexes[j]];
            }
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public int getPosition() {
            return mPos;
        }

        @Override
        public boolean move(int offset) {
            return moveToPosition(mPos + offset);
        }

        @Override
        public boolean moveToFirst() {
            return moveToPosition(0);
        }

        @Override
        public boolean moveToLast() {
            return moveToPosition(mCount - 1);
        }

        @Override
        public boolean moveToPrevious() {
            return moveToPosition(mPos - 1);
        }

        @Override
        public boolean moveToNext() {
            return moveToPosition(mPos + 1);
        }

        @Override
        public boolean moveToPosition(int position) {
            if (position < 0 || position >= mCount) {
                return false;
            }

            super.moveToPosition(mPositionMap[position]);
            mPos = position;

            return true;
        }

        private class FormPositionComparator implements Comparator<Integer> {

            private String[] mForms;
            private int mCount;

            public FormPositionComparator(String[] forms, int count) {
                mForms = forms;
                mCount = count;
            }

            public Integer[] createIndexArray() {
                Integer[] indexes = new Integer[mCount];

                for (int i = 0; i < mCount; i++) {
                    indexes[i] = i;
                }

                return indexes;
            }

            @Override
            public int compare(Integer lhs, Integer rhs) {
                return FRENCH_COLLATOR.compare(mForms[lhs], mForms[rhs]);
            }

        }

    }

}

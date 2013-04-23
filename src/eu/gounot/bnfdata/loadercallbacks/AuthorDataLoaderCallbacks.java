package eu.gounot.bnfdata.loadercallbacks;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.R;
import eu.gounot.bnfdata.ViewAuthorActivity;
import eu.gounot.bnfdata.ViewAuthorActivity.Work;
import eu.gounot.bnfdata.loader.DataLoader;
import eu.gounot.bnfdata.util.NetworkState;

public class AuthorDataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "AuthorDataLoaderCallbacks";

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+)-(\\d{1,2})-(\\d{1,2})");

    private ViewAuthorActivity mActivity;
    private View mProgressBar;
    private View mNetworkErrorView;
    private ListView mListView;
    private String mAuthorUrl;

    public AuthorDataLoaderCallbacks(ViewAuthorActivity activity, String authorUrl) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AuthorDataLoaderCallbacks() authorUrl=" + authorUrl);
        }

        mActivity = activity;
        mProgressBar = activity.getProgressBar();
        mListView = activity.getListView();
        mAuthorUrl = authorUrl;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, mAuthorUrl);
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        if (jsonObject == null) {
            // A network error occurred while trying to retrieve the data.

            // Load the network error ViewStub if it was not already loaded.
            if (mNetworkErrorView == null) {
                // Inflate the network error view and reference it for further use.
                ViewStub viewStub = (ViewStub) mActivity.findViewById(R.id.network_error_stub);
                viewStub.setLayoutResource(R.layout.network_error);
                mNetworkErrorView = viewStub.inflate();
                mActivity.setNetworkErrorView(mNetworkErrorView);

                // Set the OnClickListener to the Retry button.
                Button retryButton = (Button) mNetworkErrorView.findViewById(R.id.retry_button);
                retryButton.setOnClickListener(mActivity);
            }

            int errMsgResId;

            // Select an appropriate error message.
            if (!NetworkState.isNetworkAvailable(mActivity)) {
                errMsgResId = R.string.errmsg_no_network_connection;
            } else {
                errMsgResId = R.string.errmsg_data_retrieval_failed;
            }

            // Set the error message to the network error TextView.
            TextView errorMessageTextView = (TextView) mNetworkErrorView
                    .findViewById(R.id.error_message);
            errorMessageTextView.setText(errMsgResId);

            // Hide the progress bar.
            mProgressBar.setVisibility(View.GONE);

            // Show the network error view.
            mNetworkErrorView.setVisibility(View.VISIBLE);

            return;
        }

        // Get the author's name if it is provided and set it to the activity.
        if (jsonObject.has("label") && !jsonObject.isNull("label")) {
            try {
                String name = jsonObject.getString("label");
                mActivity.setName(name);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setName(null);
        }

        Matcher matcher;
        String birthdateYear = null;
        String deathdateYear = null;

        // Get the author's birth date if it is provided and set it to the activity.
        if (jsonObject.has("birthdate") && !jsonObject.isNull("birthdate")) {
            try {
                String birthdate = jsonObject.getString("birthdate");
                birthdateYear = birthdate;
                matcher = DATE_PATTERN.matcher(birthdate);
                if (matcher.matches()) {
                    birthdate = matcher.group(3) + "/" + matcher.group(2) + "/" + matcher.group(1);
                    birthdateYear = matcher.group(1);
                }
                mActivity.setBirthdate(birthdate);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setBirthdate(null);
        }

        // Get the author's death date if it is provided and set it to the activity.
        if (jsonObject.has("deathdate") && !jsonObject.isNull("deathdate")) {
            try {
                String deathdate = jsonObject.getString("deathdate");
                deathdateYear = deathdate;
                matcher = DATE_PATTERN.matcher(deathdate);
                if (matcher.matches()) {
                    deathdate = matcher.group(3) + "/" + matcher.group(2) + "/" + matcher.group(1);
                    deathdateYear = matcher.group(1);
                }
                mActivity.setDeathdate(deathdate);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setDeathdate(null);
        }

        // Set the author's dates to the activity if at least one date is provided.
        if (birthdateYear != null || deathdateYear != null) {
            String authorDates = "";
            if (birthdateYear != null) {
                authorDates += birthdateYear;
            }
            authorDates += " – ";
            if (deathdateYear != null) {
                authorDates += deathdateYear;
            }
            mActivity.setDates(authorDates);
        } else {
            mActivity.setDates(null);
        }

        // Get the author's nationalities list if it is provided and set it to the activity.
        if (jsonObject.has("nationality") && !jsonObject.isNull("nationality")) {
            try {
                JSONArray nationalitiesJSON = jsonObject.getJSONArray("nationality");
                if (nationalitiesJSON.length() > 0) {
                    ArrayList<String> nationalities = new ArrayList<String>();
                    for (int i = 0; i < nationalitiesJSON.length(); i++) {
                        nationalities.add(nationalitiesJSON.getString(i));
                    }
                    mActivity.setNationalities(TextUtils.join(", ", nationalities));
                } else {
                    mActivity.setNationalities(null);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setNationalities(null);
        }

        // Get the author's notes list if it is provided and set it to the activity.
        if (jsonObject.has("notes") && !jsonObject.isNull("notes")) {
            try {
                JSONArray notesJSON = jsonObject.getJSONArray("notes");
                if (notesJSON.length() > 0) {
                    ArrayList<String> notes = new ArrayList<String>();
                    for (int i = 0; i < notesJSON.length(); i++) {
                        notes.add(notesJSON.getString(i));
                    }
                    mActivity.setNotes(TextUtils.join("\n", notes));
                } else {
                    mActivity.setNotes(null);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setNotes(null);
        }

        // Create an adapter for the works list.
        ArrayAdapter<Work> adapter = new ArrayAdapter<Work>(mActivity,
                android.R.layout.simple_list_item_1);

        // Get the author's works list if it is provided and put them into the adapter.
        if (jsonObject.has("works") && !jsonObject.isNull("works")) {
            try {
                JSONArray worksJSON = jsonObject.getJSONArray("works");
                for (int i = 0; i < worksJSON.length(); i++) {
                    JSONObject workJson = worksJSON.getJSONObject(i);
                    if (workJson.has("title") && !workJson.isNull("title")) {
                        String title = workJson.getString("title");
                        String url = workJson.getString("url");
                        adapter.add(new Work(title, url));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        // Set the adapter to the ListView.
        mListView.setAdapter(adapter);

        // Get the image URL if it is provided and initialize the image loader.
        if (jsonObject.has("image") && !jsonObject.isNull("image")) {
            try {
                String imageURL = jsonObject.getString("image");
                AuthorImageLoaderCallbacks imageLoaderCallbacks = new AuthorImageLoaderCallbacks(
                        mActivity, imageURL);
                mActivity.getSupportLoaderManager().initLoader(ViewAuthorActivity.IMAGE_LOADER,
                        null, imageLoaderCallbacks);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setImage(null);
        }

        // Hide the network error view if it has already been inflated.
        if (mNetworkErrorView != null) {
            mNetworkErrorView.setVisibility(View.GONE);
        }

        // Hide the progress bar.
        mProgressBar.setVisibility(View.GONE);

        // Show the ListView.
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // Nothing to do as the loaded data is no more referenced at this point.
    }

}

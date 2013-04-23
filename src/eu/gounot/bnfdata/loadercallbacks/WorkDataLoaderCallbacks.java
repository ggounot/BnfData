package eu.gounot.bnfdata.loadercallbacks;

import java.util.ArrayList;

import android.content.res.Resources;
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
import eu.gounot.bnfdata.ViewWorkActivity;
import eu.gounot.bnfdata.ViewWorkActivity.Contributor;
import eu.gounot.bnfdata.loader.DataLoader;
import eu.gounot.bnfdata.util.NetworkState;

public class WorkDataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "WorkObjectLoaderCallbacks";

    private ViewWorkActivity mActivity;
    private Resources mResources;
    private View mProgressBar;
    private View mNetworkErrorView;
    private ListView mListView;
    private String mWorkUrl;

    public WorkDataLoaderCallbacks(ViewWorkActivity activity, String workUrl) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "WorkDataLoaderCallbacks() workUrl=" + workUrl);
        }

        mActivity = activity;
        mResources = activity.getResources();
        mProgressBar = activity.getProgressBar();
        mListView = activity.getListView();
        mWorkUrl = workUrl;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, mWorkUrl);
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
                // Inflate the network error view
                // and reference it for further use.
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

        // Get the work's title if it is provided and set it to the activity.
        if (jsonObject.has("label") && !jsonObject.isNull("label")) {
            try {
                String title = jsonObject.getString("label");
                mActivity.setWorkTitle(title);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setWorkTitle(null);
        }

        // Get the work's author if it is provided and set it to the activity.
        if (jsonObject.has("author") && !jsonObject.isNull("author")) {
            try {
                String author = jsonObject.getString("author");
                mActivity.setAuthor(author);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setAuthor(null);
        }

        // Get the work's languages list if it is provided and set it to the activity.
        if (jsonObject.has("lang") && !jsonObject.isNull("lang")) {
            try {
                JSONArray languagesJSON = jsonObject.getJSONArray("lang");
                if (languagesJSON.length() > 0) {
                    ArrayList<String> languages = new ArrayList<String>();
                    for (int i = 0; i < languagesJSON.length(); i++) {
                        languages.add(getLanguageName(languagesJSON.getString(i)));
                    }
                    mActivity.setLanguages(TextUtils.join(", ", languages));
                } else {
                    mActivity.setLanguages(null);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setLanguages(null);
        }

        // Get the work's notes list if it is provided and set it to the activity.
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

        // Create an adapter for the contributors list.
        ArrayAdapter<Contributor> adapter = new ArrayAdapter<Contributor>(mActivity,
                android.R.layout.simple_list_item_1);

        // Get the work's contributors list if it is provided and put them into the adapter.
        if (jsonObject.has("contributors") && !jsonObject.isNull("contributors")) {
            try {
                JSONArray contributorsJSON = jsonObject.getJSONArray("contributors");
                for (int i = 0; i < contributorsJSON.length(); i++) {
                    JSONObject contributorJSON = contributorsJSON.getJSONObject(i);
                    if (contributorJSON.has("name") && !contributorJSON.isNull("name")) {
                        String name = contributorJSON.getString("name");
                        String url = contributorJSON.getString("url");
                        adapter.add(new Contributor(name, url));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        // Set the adapter to the ListView.
        mListView.setAdapter(adapter);

        // // Get the image URL if it is provided and initialize the image loader.
        if (jsonObject.has("image") && !jsonObject.isNull("image")) {
            try {
                String imageURL = jsonObject.getString("image");
                WorkImageLoaderCallbacks imageLoaderCallbacks = new WorkImageLoaderCallbacks(
                        mActivity, imageURL);
                mActivity.getSupportLoaderManager().initLoader(ViewWorkActivity.IMAGE_LOADER, null,
                        imageLoaderCallbacks);
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

    private String getLanguageName(String languageCode) {
        // Get and return the full name of the language associated to the ISO 639 code.

        int resId = mResources.getIdentifier(languageCode, "string", mActivity.getPackageName());

        return (resId == 0) ? languageCode : mActivity.getString(resId);
    }

}

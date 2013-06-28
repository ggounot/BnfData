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
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.R;
import eu.gounot.bnfdata.ViewOrganizationActivity;
import eu.gounot.bnfdata.loader.DataLoader;
import eu.gounot.bnfdata.util.Constants;
import eu.gounot.bnfdata.util.NetworkState;

public class OrganizationDataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "OrganizationDataLoaderCallbacks";

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+)-(\\d{1,2})-(\\d{1,2})");

    private ViewOrganizationActivity mActivity;
    private View mProgressBar;
    private View mNetworkErrorView;
    private View mScrollView;
    private String mArkName;

    public OrganizationDataLoaderCallbacks(ViewOrganizationActivity activity, String arkName) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "OrganizationDataLoaderCallbacks() arkName=" + arkName);
        }

        mActivity = activity;
        mProgressBar = activity.getProgressBar();
        mScrollView = activity.getScrollView();
        mArkName = arkName;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, Constants.OBJECT_TYPE_ORGANIZATION, mArkName);
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

        // Get the organization's name if it is provided and set it to the activity.
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

        // Get the organization's nationalities list if they are provided
        // and set it to the activity.
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

        Matcher matcher;

        // Get the organization's start date if it is provided and set it to the activity.
        if (jsonObject.has("startdate") && !jsonObject.isNull("startdate")) {
            try {
                String startdate = jsonObject.getString("startdate");
                matcher = DATE_PATTERN.matcher(startdate);
                if (matcher.matches()) {
                    startdate = matcher.group(3) + "/" + matcher.group(2) + "/" + matcher.group(1);
                }
                mActivity.setStartdate(startdate);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setStartdate(null);
        }

        // Get the organization's stop date if it is provided and set it to the activity.
        if (jsonObject.has("stopdate") && !jsonObject.isNull("stopdate")) {
            try {
                String stopdate = jsonObject.getString("stopdate");
                matcher = DATE_PATTERN.matcher(stopdate);
                if (matcher.matches()) {
                    stopdate = matcher.group(3) + "/" + matcher.group(2) + "/" + matcher.group(1);
                }
                mActivity.setStopdate(stopdate);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
            }
        } else {
            mActivity.setStopdate(null);
        }

        // Get the organization's notes list if they are provided and set it to the activity.
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

        // Get the image URL if it is provided and initialize the image loader.
        if (jsonObject.has("image") && !jsonObject.isNull("image")) {
            try {
                String imageURL = jsonObject.getString("image");
                OrganizationImageLoaderCallbacks imageLoaderCallbacks = new OrganizationImageLoaderCallbacks(
                        mActivity, imageURL);
                mActivity.getSupportLoaderManager().initLoader(
                        ViewOrganizationActivity.IMAGE_LOADER, null, imageLoaderCallbacks);
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

        // Show the ScrollView.
        mScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // Nothing to do as the loaded data is no more referenced at this point.
    }

}

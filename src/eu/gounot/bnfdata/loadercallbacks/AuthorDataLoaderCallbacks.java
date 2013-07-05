package eu.gounot.bnfdata.loadercallbacks;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewAuthorActivity;
import eu.gounot.bnfdata.data.Author;
import eu.gounot.bnfdata.loader.DataLoader;
import eu.gounot.bnfdata.util.Constants;

public class AuthorDataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "AuthorDataLoaderCallbacks";

    private ViewAuthorActivity mActivity;
    private String mArkName;

    public AuthorDataLoaderCallbacks(ViewAuthorActivity activity, String arkName) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AuthorDataLoaderCallbacks() arkName=" + arkName);
        }

        mActivity = activity;
        mArkName = arkName;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, Constants.OBJECT_TYPE_PERSON, mArkName);
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        if (jsonObject != null) {
            mActivity.onDataLoaded(new Author(jsonObject));
        } else {
            mActivity.onNetworkError();
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // Nothing to do as the loaded data is no more referenced at this point.
    }

}

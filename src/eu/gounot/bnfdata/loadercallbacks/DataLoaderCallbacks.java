package eu.gounot.bnfdata.loadercallbacks;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewObjectActivity;
import eu.gounot.bnfdata.data.Author;
import eu.gounot.bnfdata.loader.DataLoader;

public class DataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "DataLoaderCallbacks";

    private ViewObjectActivity mActivity;
    private String mArkName;

    public DataLoaderCallbacks(ViewObjectActivity activity, String arkName) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DataLoaderCallbacks() arkName=" + arkName);
        }

        mActivity = activity;
        mArkName = arkName;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, mArkName);
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

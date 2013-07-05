package eu.gounot.bnfdata.loadercallbacks;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewWorkActivity;
import eu.gounot.bnfdata.data.Work;
import eu.gounot.bnfdata.loader.DataLoader;
import eu.gounot.bnfdata.util.Constants;

public class WorkDataLoaderCallbacks implements LoaderCallbacks<JSONObject> {

    private static final String TAG = "WorkObjectLoaderCallbacks";

    private ViewWorkActivity mActivity;
    private String mArkName;

    public WorkDataLoaderCallbacks(ViewWorkActivity activity, String arkName) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "WorkDataLoaderCallbacks() arkName=" + arkName);
        }

        mActivity = activity;
        mArkName = arkName;
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataLoader(mActivity, Constants.OBJECT_TYPE_WORK, mArkName);
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        if (jsonObject != null) {
            mActivity.onDataLoaded(new Work(jsonObject));
        } else {
            mActivity.onNetworkError();
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        // Nothing to do as the loaded data is no more referenced at this point.
    }

}

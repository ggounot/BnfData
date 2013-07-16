package eu.gounot.bnfdata.loadercallbacks;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewObjectActivity;
import eu.gounot.bnfdata.loader.DataBnfFrImageLoader;

public class DataBnfFrImageLoaderCallbacks implements LoaderCallbacks<Bitmap> {

    private static final String TAG = "DataBnfFrImageLoaderCallbacks";

    ViewObjectActivity mActivity;
    String mObjectArkName;

    public DataBnfFrImageLoaderCallbacks(ViewObjectActivity activity, String objectArkName) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DataBnfFrImageLoaderCallbacks() objectArkName=" + objectArkName);
        }

        mActivity = activity;
        mObjectArkName = objectArkName;
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new DataBnfFrImageLoader(mActivity, mObjectArkName);
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        mActivity.onImageLoaded(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        // Nothing to do as the loaded image is no more referenced at this point.
    }

}

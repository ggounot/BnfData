package eu.gounot.bnfdata.loadercallbacks;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewWorkActivity;
import eu.gounot.bnfdata.loader.ImageLoader;

public class WorkImageLoaderCallbacks implements LoaderCallbacks<Bitmap> {

    private static final String TAG = "WorkImageLoaderCallbacks";

    ViewWorkActivity mActivity;
    String mImageUrl;

    public WorkImageLoaderCallbacks(ViewWorkActivity activity, String imageUrl) {
        mActivity = activity;
        mImageUrl = imageUrl;
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateLoader()");
        }

        return new ImageLoader(mActivity, mImageUrl);
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadFinished()");
        }

        mActivity.setImage(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> arg0) {
        // Nothing to do as the loaded data is no more referenced at this point.
    }

}

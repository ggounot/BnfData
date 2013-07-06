package eu.gounot.bnfdata.loadercallbacks;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewObjectActivity;
import eu.gounot.bnfdata.loader.ImageLoader;

public class ImageLoaderCallbacks implements LoaderCallbacks<Bitmap> {

    private static final String TAG = "ImageLoaderCallbacks";

    ViewObjectActivity mActivity;
    String mImageUrl;

    public ImageLoaderCallbacks(ViewObjectActivity activity, String imageUrl) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ImageLoaderCallbacks() imageUrl=" + imageUrl);
        }

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

        mActivity.onImageLoaded(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        // Nothing to do as the loaded image is no more referenced at this point.
    }

}

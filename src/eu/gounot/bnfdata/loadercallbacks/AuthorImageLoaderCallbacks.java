package eu.gounot.bnfdata.loadercallbacks;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewAuthorActivity;
import eu.gounot.bnfdata.loader.ImageLoader;

public class AuthorImageLoaderCallbacks implements LoaderCallbacks<Bitmap> {

    private static final String TAG = "AuthorImageLoaderCallbacks";

    ViewAuthorActivity mActivity;
    String mImageUrl;

    public AuthorImageLoaderCallbacks(ViewAuthorActivity activity, String imageUrl) {
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
    public void onLoaderReset(Loader<Bitmap> loader) {
        // Nothing to do as the loaded image is no more referenced at this point.
    }

}

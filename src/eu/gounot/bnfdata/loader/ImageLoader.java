package eu.gounot.bnfdata.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.ImageHelper;

public class ImageLoader extends AsyncTaskLoader<Bitmap> {

    private static final String TAG = "ImageLoader";

    private String mImageUrl;
    private Bitmap mBitmap;

    public ImageLoader(Context context, String imageUrl) {
        super(context);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ImageLoader() imageUrl=" + imageUrl);
        }

        mImageUrl = imageUrl;
    }

    @Override
    public Bitmap loadInBackground() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadInBackground()");
        }

        return ImageHelper.getResizedImage(getContext(), mImageUrl);
    }

    @Override
    public void deliverResult(Bitmap bitmap) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "deliverResult()");
        }

        // Retain the loaded image.
        mBitmap = bitmap;

        // Deliver the image only if the loader is in the started state.
        if (isStarted()) {
            super.deliverResult(bitmap);
        }
    }

    @Override
    protected void onStartLoading() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStartLoading() image already exists? " + (mBitmap != null));
        }

        if (mBitmap != null) {
            // The image has already been loaded so we immediately deliver it.
            deliverResult(mBitmap);
        } else {
            // The image hasn't been loaded so we ask to load it.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStopLoading()");
        }

        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onReset()");
        }

        // Ensure the loader is stopped.
        onStopLoading();

        // Release the loaded bitmap.
        mBitmap = null;
    }

}

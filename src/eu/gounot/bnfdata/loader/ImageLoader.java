package eu.gounot.bnfdata.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.R;

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

        Bitmap bitmap = null;
        Resources res = getContext().getResources();

        // Retrieve the maximum dimensions that the image should have.
        int maxWidth = res.getDimensionPixelSize(R.dimen.object_image_max_width);
        int maxHeight = res.getDimensionPixelSize(R.dimen.object_image_max_height);

        try {
            // Try to download the image.
            InputStream in = new URL(mImageUrl).openStream();
            bitmap = BitmapFactory.decodeStream(in);

            // Get the original bitmap's width and height.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Calculate the ratio to apply to resize the bitmap
            // so it doesn't exceed the maximum dimensions.
            float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

            // Apply the ratio to the original dimensions to get the new dimensions.
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);

            // Resize the bitmap with the new dimensions.
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        } catch (FileNotFoundException e) {
            // 404 happens. bitmap remains null and the default image will be displayed.
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString(), e);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }

        return bitmap;
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

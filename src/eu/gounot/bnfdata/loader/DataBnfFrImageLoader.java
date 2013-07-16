package eu.gounot.bnfdata.loader;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.DataServer;
import eu.gounot.bnfdata.util.ImageHelper;

public class DataBnfFrImageLoader extends AsyncTaskLoader<Bitmap> {

    private static final String TAG = "DataBnfFrImageLoader";

    private String mObjectArkName;
    private Bitmap mBitmap;

    public DataBnfFrImageLoader(Context context, String objectArkName) {
        super(context);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DataBnfFrImageLoader() objectArkName=" + objectArkName);
        }

        mObjectArkName = objectArkName;
    }

    @Override
    public Bitmap loadInBackground() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadInBackground()");
        }

        Bitmap bitmap = null;

        try {
            JSONArray jsonArray = DataServer.getDataBnfFrJsonArray(mObjectArkName);
            String imageUrl = jsonArray.getJSONObject(0).getString("image");
            bitmap = ImageHelper.getResizedImage(getContext(), imageUrl);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        } catch (JSONException e) {
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

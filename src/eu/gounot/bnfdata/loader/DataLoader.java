package eu.gounot.bnfdata.loader;

import java.io.IOException;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.util.BnfDataWebsite;

public class DataLoader extends AsyncTaskLoader<JSONObject> {

    private static final String TAG = "DataLoader";

    private String mJsonUrl;
    private JSONObject mJsonObject;

    public DataLoader(Context context, String objectUrl) {
        super(context);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DataLoader() objectUrl=" + objectUrl);
        }

        // Remove the potential trailing '/' and add the .json extension
        // to obtain the URL of the JSON.
        if (objectUrl.length() > 0 && objectUrl.charAt(objectUrl.length() - 1) == '/') {
            objectUrl = objectUrl.substring(0, objectUrl.length() - 1);
        }
        mJsonUrl = objectUrl + ".json";
    }

    @Override
    public JSONObject loadInBackground() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadInBackground()");
        }

        JSONObject jsonObject = null;

        try {
            // Try to download the JSON object from the BnF data website.
            jsonObject = BnfDataWebsite.getObject(mJsonUrl);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.toString(), e);
        }

        return jsonObject;
    }

    @Override
    public void deliverResult(JSONObject jsonObject) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "deliverResult()");
        }

        // Retain the loaded data.
        mJsonObject = jsonObject;

        // Deliver the data only if the loader is in the started state.
        if (isStarted()) {
            super.deliverResult(jsonObject);
        }
    }

    @Override
    protected void onStartLoading() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStartLoading() data already exists? " + (mJsonObject != null));
        }

        if (mJsonObject != null) {
            // The data has already been loaded so we immediately deliver it.
            deliverResult(mJsonObject);
        } else {
            // The data hasn't been loaded so we ask to load it.
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

        // Release the loaded data.
        mJsonObject = null;
    }

}

package eu.gounot.bnfdata.loader;

import java.io.IOException;

import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;
import eu.gounot.bnfdata.ViewObjectActivity;
import eu.gounot.bnfdata.util.DataServer;

public class DataLoader extends AsyncTaskLoader<JSONObject> {

    private static final String TAG = "DataLoader";

    private int mObjectType;
    private String mArkName;
    private JSONObject mJsonObject;

    public DataLoader(ViewObjectActivity activity, String arkName) {
        super(activity);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "DataLoader() arkName=" + arkName);
        }

        mObjectType = activity.getObjectType();
        mArkName = arkName;
    }

    @Override
    public JSONObject loadInBackground() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadInBackground()");
        }

        JSONObject jsonObject = null;

        try {
            // Try to download the JSON object from the data server.
            jsonObject = DataServer.getJsonObject(mObjectType, mArkName);
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

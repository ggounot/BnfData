package eu.gounot.bnfdata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;

public class DataServer {

    private static final String TAG = "DataServer";

    private static HttpClient mHttpClient = new DefaultHttpClient();
    static {
        HttpProtocolParams.setUserAgent(mHttpClient.getParams(), Constants.USER_AGENT);
    }

    public static JSONObject getJsonObject(int objectType, String arkName) throws IOException,
            JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getJsonObject() objectType=" + objectType + " arkName=" + arkName);
        }

        String jsonObjectUrl = buildJsonObjectUrl(objectType, arkName);
        String jsonText = downloadJsonText(jsonObjectUrl);
        JSONObject jsonObject = new JSONObject(jsonText);

        return jsonObject;
    }

    private static String buildJsonObjectUrl(int objectType, String arkName) {
        String objectTypeDir = Constants.OBJECT_TYPE_DIR[objectType];
        String url = String.format(Constants.JSON_OBJECT_URL_FORMAT, objectTypeDir, arkName);

        return url;
    }

    private static String downloadJsonText(String url) throws IOException, JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "downloadJsonText() url=" + url);
        }

        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = mHttpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();

        return stringBuilder.toString();
    }

}

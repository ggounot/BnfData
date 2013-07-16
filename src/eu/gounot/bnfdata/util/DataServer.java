package eu.gounot.bnfdata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;

public class DataServer {

    private static final String TAG = "DataServer";

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

    public static JSONArray getDataBnfFrJsonArray(String arkName) throws IOException, JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getDataBnfFrJsonArray() arkName=" + arkName);
        }

        String jsonArrayUrl = getDataBnfFrJsonUrl(arkName);
        String jsonText = downloadJsonText(jsonArrayUrl);
        JSONArray jsonArray = new JSONArray(jsonText);

        return jsonArray;
    }

    private static String getDataBnfFrJsonUrl(String arkName) throws MalformedURLException,
            IOException {
        String pageUrl = Constants.DATA_BNF_FR_PAGE_URL_PREFIX + arkName;
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(pageUrl).openConnection();
        urlConnection.setInstanceFollowRedirects(false);
        String redirectUrl = urlConnection.getHeaderField("Location");
        if (redirectUrl.charAt(redirectUrl.length() - 1) == '/') {
            redirectUrl = redirectUrl.substring(0, redirectUrl.length() - 1);
        }
        String jsonUrl = redirectUrl + ".json";

        return jsonUrl;
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

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        InputStream inputStream = urlConnection.getInputStream();
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

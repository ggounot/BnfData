package eu.gounot.bnfdata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.BuildConfig;

public class BnfDataWebsite {

    private static final String TAG = "BnfDataWebsite";

    private static final String SEARCH_URL = "http://data.bnf.fr/search-letter/?term=";

    public static JSONArray getSearchResults(String filter) throws UnsupportedEncodingException,
            IOException, JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getSearchResults() filter=" + filter);
        }

        String url = SEARCH_URL + URLEncoder.encode(filter, "UTF-8");

        return downloadJsonArray(url);
    }

    public static JSONObject getObject(String url) throws IOException, JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getObject() url=" + url);
        }

        JSONArray jsonArray = downloadJsonArray(url);
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        return jsonObject;
    }

    private static JSONArray downloadJsonArray(String url) throws IOException, JSONException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "downloadJSONArray() url=" + url);
        }

        JSONArray jsonArray = null;
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        jsonArray = new JSONArray(stringBuilder.toString());

        return jsonArray;
    }

}

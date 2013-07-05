package eu.gounot.bnfdata.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gounot.bnfdata.data.Author;

public class JsonHelper {

    private static final String TAG = "JsonHelper";

    public static String getStringOrNull(JSONObject jsonObject, String key) {
        if (!jsonObject.isNull(key)) {
            try {
                return jsonObject.getString(key);
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String[] getStringArrayOrNull(JSONObject jsonObject, String key) {
        if (!jsonObject.isNull(key)) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                int length = jsonArray.length();
                String[] stringArray = new String[length];
                for (int i = 0; i < length; i++) {
                    stringArray[i] = jsonArray.getString(i);
                }
                return stringArray;
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String[][] get2DStringArrayOrNull(JSONObject jsonObject, String key) {
        if (!jsonObject.isNull(key)) {
            try {
                JSONArray jsonArray1 = jsonObject.getJSONArray(key);
                int length1 = jsonArray1.length();
                String[][] stringArray1 = new String[length1][];
                for (int i = 0; i < length1; i++) {
                    JSONArray jsonArray2 = jsonArray1.getJSONArray(i);
                    int length2 = jsonArray2.length();
                    String[] stringArray2 = new String[length2];
                    for (int j = 0; j < length2; j++) {
                        stringArray2[j] = jsonArray2.getString(j);
                    }
                    stringArray1[i] = stringArray2;
                }
                return stringArray1;
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static Author.Work[] getWorkArray(JSONObject jsonObject, String key) {
        if (!jsonObject.isNull(key)) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                int length = jsonArray.length();
                Author.Work[] workArray = new Author.Work[length];
                for (int i = 0; i < length; i++) {
                    JSONObject jsonWork = jsonArray.getJSONObject(i);
                    String workTitle = getStringOrNull(jsonWork, Author.JSON_WORK_TITLE);
                    String workDescription = getStringOrNull(jsonWork, Author.JSON_WORK_DESCRIPTION);
                    String workArkName = getStringOrNull(jsonWork, Author.JSON_WORK_ARK_NAME);
                    workArray[i] = new Author.Work(workTitle, workDescription, workArkName);
                }
                return workArray;
            } catch (JSONException e) {
                Log.e(TAG, e.toString(), e);
                return new Author.Work[0];
            }
        } else {
            return new Author.Work[0];
        }
    }
}

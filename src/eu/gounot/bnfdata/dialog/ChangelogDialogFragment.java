package eu.gounot.bnfdata.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.webkit.WebView;

import eu.gounot.bnfdata.R;

public class ChangelogDialogFragment extends DialogFragment {

    private static final String TAG = "ChangelogDialogFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Initialize a WebView displaying the changelog.
        WebView webView = new WebView(getActivity());
        webView.loadDataWithBaseURL(null, getHtmlChangelog(), "text/html", "UTF-8", null);

        // Build an AlertDialog displaying the WebView.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.changelog_dialog_title)
                .setView(webView)
                .setPositiveButton(R.string.changelog_dialog_button_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        });

        return dialogBuilder.create();
    }

    private String getHtmlChangelog() {
        // Get the content of res/raw/changelog.html and return it in a String.

        InputStream is = getResources().openRawResource(R.raw.changelog);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }

        return sb.toString();
    }

}

package eu.gounot.bnfdata;

import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import eu.gounot.bnfdata.dialog.ChangelogDialogFragment;

public class AboutActivity extends BnfDataBaseActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate()");
        }

        setContentView(R.layout.activity_about);

        TextView generalites2CodeTextView = (TextView) findViewById(R.id.generalities_2);
        generalites2CodeTextView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView sourceCodeTextView = (TextView) findViewById(R.id.source_code);
        sourceCodeTextView.setMovementMethod(LinkMovementMethod.getInstance());

        makeLinkToChangelogDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateOptionsMenu()");
        }

        // Set a menu that does not include the About item.
        getSupportMenuInflater().inflate(R.menu.about, menu);
        configureSearchView(this, menu);

        return true;
    }

    private void makeLinkToChangelogDialog() {
        TextView seeChangelogTextView = (TextView) findViewById(R.id.changelog);
        seeChangelogTextView.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable seeChangelogSpannable = (Spannable) seeChangelogTextView.getText();

        // Here we should normally use a ClickableSpan but it causes ChangelogDialogFragment.show()
        // to raise an exception when it is called a second time after a screen rotation. This issue
        // doesn't happen with an OnClickListener so we use this instead and give a hyperlink style
        // to the text to mimic a real hyperlink.

        // Give a hyperlink style to the text.
        CharacterStyle style = new CharacterStyle() {

            @Override
            public void updateDrawState(TextPaint tp) {
                tp.setColor(tp.linkColor);
                tp.setUnderlineText(true);
            }
        };
        seeChangelogSpannable.setSpan(style, 0, seeChangelogSpannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Attach an OnClickListener that shows the ChangelogDialogFragment when the TextView is
        // clicked.
        seeChangelogTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new ChangelogDialogFragment().show(getSupportFragmentManager(), "changelog");
            }
        });
    }

}

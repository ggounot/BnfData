package eu.gounot.bnfdata.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import eu.gounot.bnfdata.R;

public class ExpandableTextView extends TextView implements OnClickListener {

    private int mCollapsedHeight;
    private int mExpandedHeight = 0;
    private boolean mExpandable = true;
    private boolean mCollapsed = true;
    private LinearGradient mTextGradient = null;
    private int mArrowHeight;

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mArrowHeight = getResources().getDrawable(R.drawable.navigation_collapse)
                .getIntrinsicHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // When onMeasure() is called for the first time with a positive width, we can obtain the
        // actual height of the TextView (not yet collapsed).
        if (mExpandedHeight == 0 && MeasureSpec.getSize(widthMeasureSpec) > 0) {
            // Call super.onMeasure() a first time to measure the TextView's width and height.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // Add the height of the TextView with the height of the expand arrow to
            // get the full height of the expanded TextView.
            mExpandedHeight = getMeasuredHeight() + mArrowHeight;
            // Get the height of the collapsed TextView from the resources.
            mCollapsedHeight = getResources().getDimensionPixelSize(
                    R.dimen.collapsed_textview_height);
            // If the expanded height is higher than the collapsed height, we make the TextView
            // expandable. Otherwise, it remains like a regular TextView.
            if (mExpandedHeight > mCollapsedHeight) {
                makeExpandable();
            } else {
                mExpandable = false;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void makeExpandable() {
        setHeight(mCollapsedHeight);
        setBackgroundResource(R.drawable.navigation_expand);
        mTextGradient = makeTextGradient();
        getPaint().setShader(mTextGradient);
        setOnClickListener(this);
    }

    private LinearGradient makeTextGradient() {
        int textHeight = mCollapsedHeight - mArrowHeight;
        int gradientHeight = getResources().getDimensionPixelSize(
                R.dimen.collapsed_textview_gradient_height);
        int textColor = getTextColors().getDefaultColor();
        int backgroundColor = Color.TRANSPARENT;
        int gradientYStart = textHeight - gradientHeight;
        return new LinearGradient(0, gradientYStart, 0, gradientYStart + gradientHeight, textColor,
                backgroundColor, TileMode.CLAMP);
    }

    @Override
    public void onClick(View v) {
        if (mExpandable) {
            if (mCollapsed) {
                expand();
            } else {
                collapse();
            }
            mCollapsed = !mCollapsed;
        }
    }

    public void collapse() {
        setHeight(mCollapsedHeight);
        setBackgroundResource(R.drawable.navigation_expand);
        getPaint().setShader(mTextGradient);
    }

    public void expand() {
        setHeight(mExpandedHeight);
        setBackgroundResource(R.drawable.navigation_collapse);
        getPaint().setShader(null);
    }

}

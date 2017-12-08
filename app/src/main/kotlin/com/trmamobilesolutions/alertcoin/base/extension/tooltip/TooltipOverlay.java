package com.trmamobilesolutions.alertcoin.base.extension.tooltip;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.trmamobilesolutions.alertcoin.R;


public class TooltipOverlay extends android.support.v7.widget.AppCompatImageView {
    private int margins;

    public TooltipOverlay(Context context) {
        this(context, null);
    }

    public TooltipOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.ToolTipOverlayDefaultStyle);
    }

    public TooltipOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, R.style.ToolTipLayoutDefaultStyle);
    }

    public TooltipOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleResId) {
        super(context, attrs, defStyleAttr);
        init(context, defStyleResId);
    }

    private void init(final Context context, final int defStyleResId) {
        TooltipOverlayDrawable drawable = new TooltipOverlayDrawable(context, defStyleResId);
        setImageDrawable(drawable);

        final TypedArray array =
                context.getTheme().obtainStyledAttributes(defStyleResId, R.styleable.TooltipOverlay);
        margins = array.getDimensionPixelSize(R.styleable.TooltipOverlay_android_layout_margin, 0);
        array.recycle();

    }

    public int getLayoutMargins() {
        return margins;
    }
}
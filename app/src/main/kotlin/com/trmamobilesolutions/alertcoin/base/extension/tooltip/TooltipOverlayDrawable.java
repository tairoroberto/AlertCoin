package com.trmamobilesolutions.alertcoin.base.extension.tooltip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.trmamobilesolutions.alertcoin.R;


/**
 * Created by alessandro on 12/12/15.
 */
public class TooltipOverlayDrawable extends Drawable {
    @SuppressWarnings("unused")
    public static final String TAG = TooltipOverlay.class.getSimpleName();
    private static final float ALPHA_MAX = 255f;
    private static final double FADEOUT_START_DELAY = 0.55;
    private static final double FADEIN_DURATION = 0.3;
    private static final double SECOND_ANIM_START_DELAY = 0.25;
    private Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float outerRadius;
    private float innerRadius = 0;
    private AnimatorSet firstAnimatorSet;
    private AnimatorSet secondAnimatorSet;
    private ValueAnimator firstAnimator;
    private ValueAnimator secondAnimator;
    private int repeatIndex;
    private boolean started;
    private int outerAlpha;
    private int innerAlpha;
    private int repeatCount = 1;
    private long duration = 400;

    TooltipOverlayDrawable(Context context, int defStyleResId) {
        outerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setStyle(Paint.Style.FILL);

        final TypedArray array =
                context.getTheme().obtainStyledAttributes(defStyleResId, R.styleable.TooltipOverlay);

        for (int i = 0; i < array.getIndexCount(); i++) {
            setPainter(array, i);
        }

        array.recycle();
        setAnimation();
        setAnimatorSetFirst();
        setAnimatorSetSecond();
    }

    private void setAnimatorSetFirst() {
        firstAnimatorSet.addListener(new AnimatorListenerAdapter() {
            boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && isVisible() && ++repeatIndex < repeatCount) {
                    firstAnimatorSet.start();
                }
            }
        });
    }

    private void setAnimatorSetSecond() {
        secondAnimatorSet.addListener(new AnimatorListenerAdapter() {
            boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && isVisible() && repeatIndex < repeatCount) {
                    secondAnimatorSet.setStartDelay(0);
                    secondAnimatorSet.start();
                }
            }
        });
    }

    private void setPainter(TypedArray array, int i) {
        int index = array.getIndex(i);

        if (index == R.styleable.TooltipOverlay_android_color) {
            int color = array.getColor(index, 0);
            outerPaint.setColor(color);
            innerPaint.setColor(color);

        } else if (index == R.styleable.TooltipOverlay_ttlm_repeatCount) {
            repeatCount = array.getInt(index, 1);

        } else if (index == R.styleable.TooltipOverlay_android_alpha) {
            int alpha = (int) (array.getFloat(index, innerPaint.getAlpha() / ALPHA_MAX) * 255);
            innerPaint.setAlpha(alpha);
            outerPaint.setAlpha(alpha);

        } else if (index == R.styleable.TooltipOverlay_ttlm_duration) {
            duration = array.getInt(index, 400);
        }
    }

    private void setAnimation() {
        outerAlpha = getOuterAlpha();
        innerAlpha = getInnerAlpha();

        /**first*/
        Animator fadeIn = ObjectAnimator.ofInt(this, "outerAlpha", 0, outerAlpha);
        fadeIn.setDuration((long) (duration * FADEIN_DURATION));

        Animator fadeOut = ObjectAnimator.ofInt(this, "outerAlpha", outerAlpha, 0, 0);
        fadeOut.setStartDelay((long) (duration * FADEOUT_START_DELAY));
        fadeOut.setDuration((long) (duration * (1.0 - FADEOUT_START_DELAY)));

        firstAnimator = ObjectAnimator.ofFloat(this, "outerRadius", 0, 1);
        firstAnimator.setDuration(duration);

        firstAnimatorSet = new AnimatorSet();
        firstAnimatorSet.playTogether(fadeIn, firstAnimator, fadeOut);
        firstAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        firstAnimatorSet.setDuration(duration);

        /**second*/
        fadeIn = ObjectAnimator.ofInt(this, "innerAlpha", 0, innerAlpha);
        fadeIn.setDuration((long) (duration * FADEIN_DURATION));

        fadeOut = ObjectAnimator.ofInt(this, "innerAlpha", innerAlpha, 0, 0);
        fadeOut.setStartDelay((long) (duration * FADEOUT_START_DELAY));
        fadeOut.setDuration((long) (duration * (1.0 - FADEOUT_START_DELAY)));

        secondAnimator = ObjectAnimator.ofFloat(this, "innerRadius", 0, 1);
        secondAnimator.setDuration(duration);

        secondAnimatorSet = new AnimatorSet();
        secondAnimatorSet.playTogether(fadeIn, secondAnimator, fadeOut);
        secondAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        secondAnimatorSet.setStartDelay((long) (duration * SECOND_ANIM_START_DELAY));
        secondAnimatorSet.setDuration(duration);
    }

    public int getOuterAlpha() {
        return outerPaint.getAlpha();
    }

    @SuppressWarnings("unused")
    public void setOuterAlpha(final int value) {
        outerPaint.setAlpha(value);
        invalidateSelf();
    }

    public int getInnerAlpha() {
        return innerPaint.getAlpha();
    }

    @SuppressWarnings("unused")
    public void setInnerAlpha(final int value) {
        innerPaint.setAlpha(value);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int centerX = bounds.width() / 2;
        int centerY = bounds.height() / 2;
        canvas.drawCircle(centerX, centerY, outerRadius, outerPaint);
        canvas.drawCircle(centerX, centerY, innerRadius, innerPaint);

    }

    @Override
    public void setAlpha(int i) { /*unused*/ }

    @Override
    public void setColorFilter(ColorFilter colorFilter) { /*unused*/ }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = isVisible() != visible;

        if (visible) {
            if (restart || !started) {
                replay();
            }
        } else {
            stop();
        }
        return changed;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        outerRadius = (float) Math.min(bounds.width(), bounds.height()) / 2;
        firstAnimator.setFloatValues(0, outerRadius);
        secondAnimator.setFloatValues(0, outerRadius);
    }

    @Override
    public int getIntrinsicWidth() {
        return 96;
    }

    @Override
    public int getIntrinsicHeight() {
        return 96;
    }

    public void play() {
        repeatIndex = 0;
        started = true;
        firstAnimatorSet.start();
        secondAnimatorSet.setStartDelay((long) (duration * SECOND_ANIM_START_DELAY));
        secondAnimatorSet.start();
    }

    private void replay() {
        stop();
        play();
    }

    private void stop() {
        firstAnimatorSet.cancel();
        secondAnimatorSet.cancel();
        repeatIndex = 0;
        started = false;
        setInnerRadius(0);
        setOuterRadius(0);
    }

    @SuppressWarnings("unused")
    public float getInnerRadius() {
        return innerRadius;
    }

    @SuppressWarnings("unused")
    public void setInnerRadius(final float rippleRadius) {
        innerRadius = rippleRadius;
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public float getOuterRadius() {
        return outerRadius;
    }

    @SuppressWarnings("unused")
    public void setOuterRadius(final float value) {
        outerRadius = value;
        invalidateSelf();
    }
}

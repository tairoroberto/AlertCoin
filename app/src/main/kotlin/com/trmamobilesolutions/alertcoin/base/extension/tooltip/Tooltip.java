package com.trmamobilesolutions.alertcoin.base.extension.tooltip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trmamobilesolutions.alertcoin.R;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.trmamobilesolutions.alertcoin.base.extension.tooltip.Utils.dpToPx;

/**
 * Created by Alessandro Crugnola on 12/12/15.
 * alessandro.crugnola@gmail.com
 */
public final class Tooltip {
    static final boolean DBG = false;

    private Tooltip() { /*unused*/ }

    @SuppressWarnings("unused")
    public static TooltipView make(Context context, Builder builder) {
        return new TooltipViewImpl(context, builder);
    }

    @SuppressWarnings("unused")
    public static boolean remove(Context context, final int tooltipId) {
        final Activity act = Utils.getActivity(context);
        if (act != null) {
            ViewGroup rootView;
            rootView = (ViewGroup) (act.getWindow().getDecorView());
            for (int i = 0; i < rootView.getChildCount(); i++) {
                final View child = rootView.getChildAt(i);
                if (child instanceof TooltipView && ((TooltipView) child).getTooltipId() == tooltipId) {
                    Utils.log("Tooltip", VERBOSE, "removing: %d", ((TooltipView) child).getTooltipId());
                    ((TooltipView) child).remove();
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static boolean removeAll(Context context) {
        final Activity act = Utils.getActivity(context);
        if (act != null) {
            ViewGroup rootView;
            rootView = (ViewGroup) (act.getWindow().getDecorView());
            for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
                final View child = rootView.getChildAt(i);
                if (child instanceof TooltipView) {
                    Utils.log("Tooltip", VERBOSE, "removing: %d", ((TooltipView) child).getTooltipId());
                    ((TooltipView) child).remove();
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static class ClosePolicy {
        static final int NONE = 0;
        static final int TOUCH_INSIDE = 1 << 1;
        static final int TOUCH_OUTSIDE = 1 << 2;
        static final int CONSUME_INSIDE = 1 << 3;
        static final int CONSUME_OUTSIDE = 1 << 4;
        private int policy;
        public static final ClosePolicy TOUCH_NONE = new ClosePolicy(NONE);
        public static final ClosePolicy TOUCH_INSIDE_CONSUME = new ClosePolicy(TOUCH_INSIDE | CONSUME_INSIDE);
        public static final ClosePolicy TOUCH_INSIDE_NO_CONSUME = new ClosePolicy(TOUCH_INSIDE);
        public static final ClosePolicy TOUCH_OUTSIDE_CONSUME = new ClosePolicy(TOUCH_OUTSIDE | CONSUME_OUTSIDE);
        public static final ClosePolicy TOUCH_OUTSIDE_NO_CONSUME = new ClosePolicy(TOUCH_OUTSIDE);
        public static final ClosePolicy TOUCH_ANYWHERE_NO_CONSUME = new ClosePolicy(TOUCH_INSIDE | TOUCH_OUTSIDE);
        public static final ClosePolicy TOUCH_ANYWHERE_CONSUME =
                new ClosePolicy(TOUCH_INSIDE | TOUCH_OUTSIDE | CONSUME_INSIDE | CONSUME_OUTSIDE);

        public ClosePolicy() {
            policy = NONE;
        }

        ClosePolicy(final int policy) {
            this.policy = policy;
        }

        public ClosePolicy insidePolicy(boolean close, boolean consume) {
            policy = close ? policy | TOUCH_INSIDE : policy & ~TOUCH_INSIDE;
            policy = consume ? policy | CONSUME_INSIDE : policy & ~CONSUME_INSIDE;
            return this;
        }

        public ClosePolicy outsidePolicy(boolean close, boolean consume) {
            policy = close ? policy | TOUCH_OUTSIDE : policy & ~TOUCH_OUTSIDE;
            policy = consume ? policy | CONSUME_OUTSIDE : policy & ~CONSUME_OUTSIDE;
            return this;
        }

        public ClosePolicy clear() {
            policy = NONE;
            return this;
        }

        public int build() {
            return policy;
        }

        public int getPolicy() {
            return policy;
        }

        static boolean touchInside(final int value) {
            return (value & TOUCH_INSIDE) == TOUCH_INSIDE;
        }

        static boolean touchOutside(final int value) {
            return (value & TOUCH_OUTSIDE) == TOUCH_OUTSIDE;
        }

        static boolean consumeInside(final int value) {
            return (value & CONSUME_INSIDE) == CONSUME_INSIDE;
        }

        static boolean consumeOutside(final int value) {
            return (value & CONSUME_OUTSIDE) == CONSUME_OUTSIDE;
        }

    }

    public enum Gravity {
        LEFT, RIGHT, TOP, BOTTOM, CENTER
    }

    @SuppressWarnings("unused")
    public interface TooltipView {
        void show();

        void hide();

        void remove();

        int getTooltipId();

        void offsetTo(int x, int y);

        void offsetBy(int x, int y);

        void offsetXBy(float x);

        void offsetXTo(float x);

        boolean isAttached();

        boolean isShown();

        void setText(CharSequence text);

        void setText(@StringRes int resId);

        void setTextColor(int color);

        void setTextColor(ColorStateList color);

        void requestLayout();
    }

    public interface Callback {
        /**
         * Tooltip is being closed
         *
         * @param tooltip       the tooltip being closed
         * @param fromUser      true if the close operation started from a user click
         * @param containsTouch true if the original touch came from inside the tooltip
         */
        void onTooltipClose(TooltipView tooltip, boolean fromUser, boolean containsTouch);

        /**
         * Tooltip failed to show (not enough space)
         */
        void onTooltipFailed(TooltipView view);

        void onTooltipShown(TooltipView view);

        void onTooltipHidden(TooltipView view);
    }

    @SuppressLint("ViewConstructor")
    static class TooltipViewImpl extends ViewGroup implements TooltipView {
        public static final int TOLERANCE_VALUE = 10;
        private static final String TAG = "TooltipView";
        private static final List<Gravity> GRAVITY_LIST = new ArrayList<>(
                Arrays.asList(Gravity.LEFT, Gravity.RIGHT, Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER));
        private final List<Gravity> viewGravities = new ArrayList<>(GRAVITY_LIST);
        private final long showDelay;
        private final int textAppearance;
        private final int textGravity;
        private final int toolTipId;
        private final Rect drawRect;
        private final long showDuration;
        private final int closePolicy;
        private final Point point;
        private final int textResId;
        private final int topRule;
        private final int maxWidth;
        private final boolean hideArrow;
        private final long activateDelay;
        private final boolean restrict;
        private final long fadeDuration;
        private final TooltipTextDrawable drawable;
        private final Rect tempRect = new Rect();
        private final int[] tempLocation = new int[2];
        private final Handler handler = new Handler();
        private final Rect screenRect = new Rect();
        private final Point tmpPoint = new Point();
        private final Rect hitRect = new Rect();
        private final float textViewElevation;
        private final boolean alignAnchorToLeft;
        private final int color;
        private final int textColor;
        private int margin = (int) dpToPx(20);
        private Callback callback;
        private int[] oldLocation;
        private Gravity gravity;
        private Animator showAnimation;
        private boolean showing;
        private WeakReference<View> viewAnchor;
        private boolean attached;
        private Runnable activateRunnable = new Runnable() {
            @Override
            public void run() {
                activated = true;
            }
        };
        private Runnable hideRunnable = new Runnable() {
            @Override

            public void run() {
                onClose(false, false, false);
            }
        };
        private boolean initialized;
        private boolean activated;
        private int padding;
        private CharSequence text;
        private Rect viewRect;
        private View view;
        private TooltipOverlay viewOverlay;
        private TextView textView;
        private Typeface typeface;
        private int sizeTolerance;
        private ValueAnimator animator;
        private AnimationBuilder floatingAnimation;
        private boolean alreadyCheck;
        private boolean isCustomView;
        private CunstomOnAttachStateChangeListener changeListener = new CunstomOnAttachStateChangeListener();
        private final OnAttachStateChangeListener attachedStateListener = changeListener;

        private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!attached) {
                    removePreDrawObserver(null);
                    return true;
                }
                if (null != viewAnchor) {
                    setAnchorView();
                }
                return true;
            }
        };

        private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!attached) {
                            removeGlobalLayoutObserver(null);
                            return;
                        }

                        if (null != viewAnchor) {
                            setAnchorViewFirst();
                        }
                    }
                };
        private ShowAnimationListener animationListener = new ShowAnimationListener();

        TooltipViewImpl(Context context, final Builder builder) {
            super(context);

            TypedArray theme = context.getTheme().obtainStyledAttributes(null, R.styleable.TooltipLayout, builder.defStyleAttr, builder.defStyleRes);
            this.padding = theme.getDimensionPixelSize(R.styleable.TooltipLayout_ttlm_padding, (int) dpToPx(20));
            this.textAppearance = theme.getResourceId(R.styleable.TooltipLayout_android_textAppearance, 0);
            this.textGravity = theme.getInt(R.styleable.TooltipLayout_android_gravity, android.view.Gravity.TOP | android.view.Gravity.START);
            this.textViewElevation = theme.getDimension(R.styleable.TooltipLayout_ttlm_elevation, 0);
            int overlayStyle = theme.getResourceId(R.styleable.TooltipLayout_ttlm_overlayStyle, R.style.ToolTipOverlayDefaultStyle);

            String font = theme.getString(R.styleable.TooltipLayout_ttlm_font);

            theme.recycle();

            this.toolTipId = builder.id;
            this.text = builder.text;
            this.gravity = builder.gravity;
            this.textResId = builder.textResId;
            this.maxWidth = builder.maxWidth;
            this.topRule = builder.actionbarSize;
            this.closePolicy = builder.closePolicy;
            this.showDuration = builder.showDuration;
            this.showDelay = builder.showDelay;
            this.hideArrow = builder.hideArrow;
            this.activateDelay = builder.activateDelay;
            this.restrict = builder.restrictToScreenEdges;
            this.fadeDuration = builder.fadeDuration;
            this.callback = builder.closeCallback;
            this.floatingAnimation = builder.floatingAnimation;
            this.sizeTolerance = (int) (context.getResources().getDisplayMetrics().density * TOLERANCE_VALUE);
            this.margin = builder.margin;
            this.alignAnchorToLeft = builder.alignAnchorToLeft;
            this.color = builder.color;
            this.textColor = builder.textColor;

            if (builder.typeface != null) {
                typeface = builder.typeface;
            } else if (!TextUtils.isEmpty(font)) {
                typeface = Typefaces.get(context, font);
            }

            setClipChildren(false);
            setClipToPadding(false);

            if (null != builder.point) {
                this.point = new Point(builder.point);
                this.point.y += topRule;
            } else {
                this.point = null;
            }

            this.drawRect = new Rect();

            if (null != builder.view) {
                viewRect = new Rect();

                builder.view.getHitRect(hitRect);
                builder.view.getLocationOnScreen(tempLocation);

                viewRect.set(hitRect);
                viewRect.offsetTo(tempLocation[0], tempLocation[1]);

                viewAnchor = new WeakReference<>(builder.view);

                if (builder.view.getViewTreeObserver().isAlive()) {
                    builder.view.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
                    builder.view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
                    builder.view.addOnAttachStateChangeListener(attachedStateListener);
                }
            }

            if (builder.overlay) {
                viewOverlay = new TooltipOverlay(getContext(), null, 0, overlayStyle);
                viewOverlay.setAdjustViewBounds(true);
                viewOverlay.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            }

            if (!builder.isCustomView) {
                this.drawable = new TooltipTextDrawable(context, builder);
            } else {
                this.drawable = null;
                this.isCustomView = true;
            }
            setVisibility(INVISIBLE);
        }

        private class CunstomOnAttachStateChangeListener implements OnAttachStateChangeListener {

            @Override
            public void onViewAttachedToWindow(View v) { /*unused*/ }

            @Override
            @TargetApi(17)
            public void onViewDetachedFromWindow(View v) {
                removeViewListeners(v);
                if (!attached) {
                    return;
                }
                Activity activity = Utils.getActivity(getContext());
                if (null != activity) {
                    if (activity.isFinishing()) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
                        return;
                    }
                    onClose(false, false, true);
                }
            }
        }

        private void setAnchorView() {

            View view = viewAnchor.get();
            if (null != view) {
                view.getLocationOnScreen(tempLocation);

                if (oldLocation == null) {
                    oldLocation = new int[]{tempLocation[0], tempLocation[1]};
                }

                if (oldLocation[0] != tempLocation[0] || oldLocation[1] != tempLocation[1]) {
                    this.view.setTranslationX(tempLocation[0] - oldLocation[0] + this.view.getTranslationX());
                    this.view.setTranslationY(tempLocation[1] - oldLocation[1] + this.view.getTranslationY());

                    if (null != viewOverlay) {
                        viewOverlay.setTranslationX(tempLocation[0] - oldLocation[0] + viewOverlay.getTranslationX());
                        viewOverlay.setTranslationY(tempLocation[1] - oldLocation[1] + viewOverlay.getTranslationY());
                    }
                }

                oldLocation[0] = tempLocation[0];
                oldLocation[1] = tempLocation[1];
            }
        }

        private void setAnchorViewFirst() {
            View view = viewAnchor.get();
            if (null != view) {
                view.getHitRect(tempRect);
                view.getLocationOnScreen(tempLocation);

                if (DBG) {
                    Utils.log(TAG, INFO, "[%d] onGlobalLayout(dirty: %b)", toolTipId, view.isDirty());
                    Utils.log(TAG, VERBOSE, "[%d] hitRect: %s, old: %s", toolTipId, tempRect, hitRect);
                }

                if (!tempRect.equals(hitRect)) {
                    hitRect.set(tempRect);
                    tempRect.offsetTo(tempLocation[0], tempLocation[1]);
                    viewRect.set(tempRect);
                    calculatePositions();
                }
            } else {
                if (DBG) {
                    Utils.log(TAG, WARN, "[%d] view is null", toolTipId);
                }
            }
        }

        @Override
        public void show() {
            if (getParent() == null) {
                final Activity act = Utils.getActivity(getContext());
                LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
                if (act != null) {
                    ViewGroup rootView;
                    rootView = (ViewGroup) (act.getWindow().getDecorView());
                    rootView.addView(this, params);
                }
            }
        }

        @Override
        public void hide() {
            hide(fadeDuration);
        }

        private void hide(long fadeDuration) {
            Utils.log(TAG, INFO, "[%d] hide(%d)", toolTipId, fadeDuration);
            if (!isAttached()) {
                return;
            }
            fadeOut(fadeDuration);
        }

        protected void fadeOut(long fadeDuration) {
            if (!isAttached() || !showing) {
                return;
            }
            if (null != showAnimation) {
                showAnimation.cancel();
            }
            showing = false;
            if (fadeDuration > 0) {
                float alpha = getAlpha();
                showAnimation = ObjectAnimator.ofFloat(this, "alpha", alpha, 0);
                showAnimation.setDuration(fadeDuration);
                setAnimationView();
                showAnimation.start();
            } else {
                setVisibility(View.INVISIBLE);
                remove();
            }
        }

        private void setAnimationView() {
            showAnimation.addListener(animationListener);
        }

        private class ShowAnimationListener implements Animator.AnimatorListener {
            boolean cancelled;

            @Override
            public void onAnimationStart(final Animator animation) {
                cancelled = false;
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                if (cancelled) {
                    return;
                }
                // hide completed
                if (null != callback) {
                    callback.onTooltipHidden(TooltipViewImpl.this);
                }
                remove();
                showAnimation = null;
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) { /*unused*/ }
        }

        void removeFromParent() {
            Utils.log(TAG, INFO, "[%d] removeFromParent", toolTipId);
            ViewParent parent = getParent();
            removeCallbacks();

            if (null != parent) {
                ((ViewGroup) parent).removeView(TooltipViewImpl.this);

                if (null != showAnimation && showAnimation.isStarted()) {
                    showAnimation.cancel();
                }
            }
        }

        private void removeCallbacks() {
            handler.removeCallbacks(hideRunnable);
            handler.removeCallbacks(activateRunnable);
        }

        @Override
        public void remove() {
            Utils.log(TAG, INFO, "[%d] remove()", toolTipId);
            if (isAttached()) {
                removeFromParent();
            }
        }

        @Override
        public int getTooltipId() {
            return toolTipId;
        }

        @Override
        public void offsetTo(final int x, final int y) {
            view.setTranslationX(x + drawRect.left);
            view.setTranslationY(y + drawRect.top);
        }

        @Override
        public void offsetBy(final int x, final int y) {
            view.setTranslationX(x + view.getTranslationX());
            view.setTranslationY(y + view.getTranslationY());
        }

        @Override
        public void offsetXBy(final float x) {
            view.setTranslationX(x + view.getTranslationX());
        }

        @Override
        public void offsetXTo(final float x) {
            view.setTranslationX(x + drawRect.left);
        }

        @Override
        public void setText(@StringRes final int resId) {
            if (null != view) {
                setText(getResources().getString(resId));
            }
        }

        @Override
        public void setTextColor(final int color) {
            if (null != textView) {
                textView.setTextColor(color);
            }
        }

        @Override
        public void setTextColor(final ColorStateList color) {
            if (null != textView) {
                textView.setTextColor(color);
            }
        }

        @Override
        public boolean isAttached() {
            return attached;
        }

        @SuppressWarnings("unused")
        public boolean isShowing() {
            return showing;
        }

        @Override
        protected void onAttachedToWindow() {
            Utils.log(TAG, INFO, "[%d] onAttachedToWindow", toolTipId);
            super.onAttachedToWindow();
            attached = true;
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            android.view.Display display = wm.getDefaultDisplay();
            display.getRectSize(screenRect);
            initializeView();
            showInternal();
        }

        @Override
        protected void onDetachedFromWindow() {
            Utils.log(TAG, INFO, "[%d] onDetachedFromWindow", toolTipId);
            removeListeners();
            stopFloatingAnimations();
            attached = false;
            viewAnchor = null;
            super.onDetachedFromWindow();
        }

        @Override
        protected void onVisibilityChanged(@NonNull final View changedView, final int visibility) {
            super.onVisibilityChanged(changedView, visibility);

            if (null != animator) {
                if (visibility == VISIBLE) {
                    animator.start();
                } else {
                    animator.cancel();
                }
            }
        }

        @Override
        protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
            if (null != view) {
                view.layout(view.getLeft(), view.getTop(), view.getMeasuredWidth(), view.getMeasuredHeight());
            }

            if (null != viewOverlay) {
                viewOverlay.layout(
                        viewOverlay.getLeft(),
                        viewOverlay.getTop(),
                        viewOverlay.getMeasuredWidth(),
                        viewOverlay.getMeasuredHeight()
                );
            }

            if (changed) {
                if (viewAnchor != null) {
                    View view = viewAnchor.get();
                    if (null != view) {
                        view.getHitRect(tempRect);
                        view.getLocationOnScreen(tempLocation);
                        tempRect.offsetTo(tempLocation[0], tempLocation[1]);
                        viewRect.set(tempRect);
                    }
                }
                calculatePositions();
            }
        }

        private void removeListeners() {
            callback = null;

            if (null != viewAnchor) {
                View view = viewAnchor.get();
                removeViewListeners(view);
            }
        }

        private void stopFloatingAnimations() {
            if (null != animator) {
                animator.cancel();
                animator = null;
            }
        }

        private void removeViewListeners(final View view) {
            Utils.log(TAG, INFO, "[%d] removeListeners", toolTipId);
            removeGlobalLayoutObserver(view);
            removePreDrawObserver(view);
            removeOnAttachStateObserver(view);
        }

        @SuppressWarnings("deprecation")
        private void removeGlobalLayoutObserver(@Nullable View v) {
            View view = v;
            if (null == v && null != viewAnchor) {
                view = viewAnchor.get();
            }
            if (null != view && view.getViewTreeObserver().isAlive()) {
                if (Build.VERSION.SDK_INT >= 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
                }
            } else {
                Utils.log(TAG, ERROR, "[%d] removeGlobalLayoutObserver failed", toolTipId);
            }
        }

        private void removePreDrawObserver(@Nullable View v) {
            View view = v;
            if (null == v && null != viewAnchor) {
                view = viewAnchor.get();
            }
            if (null != view && view.getViewTreeObserver().isAlive()) {
                view.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            } else {
                Utils.log(TAG, ERROR, "[%d] removePreDrawObserver failed", toolTipId);
            }
        }

        private void removeOnAttachStateObserver(@Nullable View v) {
            View view = v;
            if (null == v && null != viewAnchor) {
                view = viewAnchor.get();
            }
            if (null != view) {
                view.removeOnAttachStateChangeListener(attachedStateListener);
            } else {
                Utils.log(TAG, ERROR, "[%d] removeOnAttachStateObserver failed", toolTipId);
            }
        }

        @SuppressWarnings("deprecation")
        private void initializeView() {
            if (!isAttached() || initialized) {
                return;
            }
            initializeViewParams();
            this.addView(view);

            if (null != viewOverlay) {
                this.addView(viewOverlay);
            }

            if (!isCustomView && textViewElevation > 0 && Build.VERSION.SDK_INT >= 21) {
                setupElevation();
            }
        }

        private void initializeViewParams() {
            initialized = true;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.setMargins(margin, 0, margin, 0);

            view = LayoutInflater.from(getContext()).inflate(textResId, this, false);
            view.setLayoutParams(params);

            textView = (TextView) view.findViewById(android.R.id.text1);

            SpannableString content = new SpannableString(this.text);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

            textView.setText(content);
            if (maxWidth > -1) {
                textView.setMaxWidth(maxWidth);
            }

            if (0 != textAppearance) {
                TextViewCompat.setTextAppearance(textView, textAppearance);
            }
            if (0 != textColor) {
                textView.setTextColor(textColor);
            }

            textView.setGravity(textGravity);

            if (typeface != null) {
                textView.setTypeface(typeface);
            }

            if (null != drawable) {
                if (this.color != 0) {
                    drawable.setColor(ContextCompat.getColor(textView.getContext(), color));
                }
                textView.setBackgroundDrawable(drawable);
                if (!hideArrow) {
                    params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                    if (margin > 0) {
                        params.setMargins(margin, 0, margin, 0);
                    }
                    textView.setPadding(padding, padding, padding, padding);
                    textView.setLayoutParams(params);
                }
            }
        }

        private void showInternal() {
            Utils.log(TAG, INFO, "[%d] show", toolTipId);
            if (!isAttached()) {
                Utils.log(TAG, ERROR, "[%d] not attached!", toolTipId);
                return;
            }
            fadeIn(fadeDuration);
        }

        @SuppressLint("NewApi")
        private void setupElevation() {
            textView.setElevation(textViewElevation);
            textView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }

        protected void fadeIn(final long fadeDuration) {
            if (showing) {
                return;
            }

            if (null != showAnimation) {
                showAnimation.cancel();
            }

            Utils.log(TAG, INFO, "[%d] fadeIn", toolTipId);

            showing = true;

            setAnimationViewFadein(fadeDuration);

            if (showDuration > 0) {
                handler.removeCallbacks(hideRunnable);
                handler.postDelayed(hideRunnable, showDuration);
            }
        }

        private void setAnimationViewFadein(long fadeDuration) {
            if (fadeDuration > 0) {
                showAnimation = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
                showAnimation.setDuration(fadeDuration);
                if (this.showDelay > 0) {
                    showAnimation.setStartDelay(this.showDelay);
                }

                AnimationListener animationListener = new AnimationListener();

                showAnimation.addListener(animationListener);
                showAnimation.start();
            } else {
                setVisibility(View.VISIBLE);
                if (!activated) {
                    postActivate(activateDelay);
                }
            }
        }

        void postActivate(long ms) {
            Utils.log(TAG, VERBOSE, "[%d] postActivate: %d", toolTipId, ms);
            if (ms > 0) {
                if (isAttached()) {
                    handler.postDelayed(activateRunnable, ms);
                }
            } else {
                activated = true;
            }
        }

        class AnimationListener implements Animator.AnimatorListener {

            boolean cancelled;

            @Override
            public void onAnimationStart(final Animator animation) {
                setVisibility(View.VISIBLE);
                cancelled = false;
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                if (!cancelled) {
                    if (null != callback) {
                        callback.onTooltipShown(TooltipViewImpl.this);
                    }
                    postActivate(activateDelay);
                }
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) { /*unused*/ }
        }

        private void calculatePositions() {
            calculatePositions(restrict);
        }

        private void calculatePositions(boolean restrict) {
            viewGravities.clear();
            viewGravities.addAll(GRAVITY_LIST);
            viewGravities.remove(gravity);
            viewGravities.add(0, gravity);
            calculatePositions(viewGravities, restrict);
        }

        @SuppressWarnings("checkstyle:cyclomaticcomplexity")
        private void calculatePositions(List<Gravity> gravities, final boolean checkEdges) {
            if (!isAttached()) {
                return;
            }

            // failed to display the tooltip due to
            // something wrong with its dimensions or
            // the target position..
            if (gravities.size() < 1) {
                if (null != callback) {
                    callback.onTooltipFailed(this);
                }
                setVisibility(View.GONE);
                return;
            }

            Gravity gravity = gravities.remove(0);
            int statusbarHeight = screenRect.top;
            int overlayWidth = 0;
            int overlayHeight = 0;

            if (null != viewOverlay && gravity != Gravity.CENTER) {
                int margin = viewOverlay.getLayoutMargins();
                overlayWidth = (viewOverlay.getWidth() / 2) + margin;
                overlayHeight = (viewOverlay.getHeight() / 2) + margin;
            }

            if (viewRect == null) {
                viewRect = new Rect();
                viewRect.set(point.x, point.y + statusbarHeight, point.x, point.y + statusbarHeight);
            }

            setGravity(gravities, gravity, checkEdges, overlayWidth, overlayHeight);
            setGravityCenter(gravity);
            setDrawableTooltip(gravity);
        }

        private void setDrawableTooltip(Gravity gravity) {
            if (null != drawable) {
                getAnchorPoint(gravity, tmpPoint);
                drawable.setAnchor(gravity, hideArrow ? 0 : padding / 2, hideArrow ? null : tmpPoint);
            }

            if (!alreadyCheck) {
                alreadyCheck = true;
                startFloatingAnimations();
            }
        }

        private void setGravityCenter(Gravity gravity) {
            if (gravity != this.gravity) {
                this.gravity = gravity;

                if (gravity == Gravity.CENTER && null != viewOverlay) {
                    removeView(viewOverlay);
                    viewOverlay = null;
                }
            }

            if (null != viewOverlay) {
                viewOverlay.setTranslationX(viewRect.centerX() - viewOverlay.getWidth() / 2);
                viewOverlay.setTranslationY(viewRect.centerY() - viewOverlay.getHeight() / 2);
            }

            view.setTranslationX(drawRect.left);
            view.setTranslationY(drawRect.top);
        }

        private void setGravity(List<Gravity> gravities, Gravity gravity, final boolean checkEdges, int overlayWidth,
                                int overlayHeight) {
            final int screenTop = screenRect.top + topRule;

            int width = view.getWidth();
            int height = view.getHeight();
            if (gravity == Gravity.BOTTOM) {
                if (calculatePositionBottom(checkEdges, overlayHeight, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                }
            } else if (gravity == Gravity.TOP) {
                if (calculatePositionTop(checkEdges, overlayHeight, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                }
            } else if (gravity == Gravity.RIGHT) {
                if (calculatePositionRight(checkEdges, overlayWidth, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                }
            } else if (gravity == Gravity.LEFT) {
                if (calculatePositionLeft(checkEdges, overlayWidth, screenTop, width, height)) {
                    calculatePositions(gravities, checkEdges);
                }
            } else if (gravity == Gravity.CENTER) {
                calculatePositionCenter(checkEdges, screenTop, width, height);
            }
        }

        private void calculatePositionCenter(final boolean checkEdges, final int screenTop, final int width, final int height) {
            drawRect.set(
                    viewRect.centerX() - width / 2,
                    viewRect.centerY() - height / 2,
                    viewRect.centerX() + width / 2,
                    viewRect.centerY() + height / 2
            );

            if (checkEdges && !Utils.rectContainsRectWithTolerance(screenRect, drawRect, sizeTolerance)) {
                if (drawRect.bottom > screenRect.bottom) {
                    drawRect.offset(0, screenRect.bottom - drawRect.bottom);
                } else if (drawRect.top < screenTop) {
                    drawRect.offset(0, screenTop - drawRect.top);
                }
                if (drawRect.right > screenRect.right) {
                    drawRect.offset(screenRect.right - drawRect.right, 0);
                } else if (drawRect.left < screenRect.left) {
                    drawRect.offset(screenRect.left - drawRect.left, 0);
                }
            }
        }

        private boolean calculatePositionLeft(
                final boolean checkEdges, final int overlayWidth, final int screenTop,
                final int width, final int height) {
            drawRect.set(
                    viewRect.left - width,
                    viewRect.centerY() - height / 2,
                    viewRect.left,
                    viewRect.centerY() + height / 2
            );

            if ((viewRect.width() / 2) < overlayWidth) {
                drawRect.offset(-(overlayWidth - (viewRect.width() / 2)), 0);
            }

            if (checkEdges && !Utils.rectContainsRectWithTolerance(screenRect, drawRect, sizeTolerance)) {
                if (drawRect.bottom > screenRect.bottom) {
                    drawRect.offset(0, screenRect.bottom - drawRect.bottom);
                } else if (drawRect.top < screenTop) {
                    drawRect.offset(0, screenTop - drawRect.top);
                }
                if (drawRect.left < screenRect.left) {
                    // this means there's no enough space!
                    return true;
                } else if (drawRect.right > screenRect.right) {
                    drawRect.offset(screenRect.right - drawRect.right, 0);
                }
            }
            return false;
        }

        private boolean calculatePositionRight(
                final boolean checkEdges, final int overlayWidth, final int screenTop,
                final int width, final int height) {
            drawRect.set(
                    viewRect.right,
                    viewRect.centerY() - height / 2,
                    viewRect.right + width,
                    viewRect.centerY() + height / 2
            );

            if ((viewRect.width() / 2) < overlayWidth) {
                drawRect.offset(overlayWidth - viewRect.width() / 2, 0);
            }

            if (checkEdges && !Utils.rectContainsRectWithTolerance(screenRect, drawRect, sizeTolerance)) {
                if (drawRect.bottom > screenRect.bottom) {
                    drawRect.offset(0, screenRect.bottom - drawRect.bottom);
                } else if (drawRect.top < screenTop) {
                    drawRect.offset(0, screenTop - drawRect.top);
                }
                if (drawRect.right > screenRect.right) {
                    // this means there's no enough space!
                    return true;
                } else if (drawRect.left < screenRect.left) {
                    drawRect.offset(screenRect.left - drawRect.left, 0);
                }
            }
            return false;
        }

        private boolean calculatePositionTop(
                final boolean checkEdges, final int overlayHeight, final int screenTop,
                final int width, final int height) {
            drawRect.set(
                    viewRect.centerX() - width / 2,
                    viewRect.top - height,
                    viewRect.centerX() + width / 2,
                    viewRect.top
            );

            if ((viewRect.height() / 2) < overlayHeight) {
                drawRect.offset(0, -(overlayHeight - (viewRect.height() / 2)));
            }

            if (checkEdges && !Utils.rectContainsRectWithTolerance(screenRect, drawRect, sizeTolerance)) {
                if (drawRect.right > screenRect.right) {
                    drawRect.offset(screenRect.right - drawRect.right, 0);
                } else if (drawRect.left < screenRect.left) {
                    drawRect.offset(-drawRect.left, 0);
                }
                if (drawRect.top < screenTop) {
                    // this means there's no enough space!
                    return true;
                } else if (drawRect.bottom > screenRect.bottom) {
                    drawRect.offset(0, screenRect.bottom - drawRect.bottom);
                }
            }
            return false;
        }

        private boolean calculatePositionBottom(
                final boolean checkEdges, final int overlayHeight, final int screenTop,
                final int width, final int height) {
            drawRect.set(
                    viewRect.centerX() - width / 2,
                    viewRect.bottom,
                    viewRect.centerX() + width / 2,
                    viewRect.bottom + height
            );

            if (viewRect.height() / 2 < overlayHeight) {
                drawRect.offset(0, overlayHeight - viewRect.height() / 2);
            }

            if (checkEdges && !Utils.rectContainsRectWithTolerance(screenRect, drawRect, sizeTolerance)) {
                if (drawRect.right > screenRect.right) {
                    drawRect.offset(screenRect.right - drawRect.right, 0);
                } else if (drawRect.left < screenRect.left) {
                    drawRect.offset(-drawRect.left, 0);
                }
                if (drawRect.bottom > screenRect.bottom) {
                    // this means there's no enough space!
                    return true;
                } else if (drawRect.top < screenTop) {
                    drawRect.offset(0, screenTop - drawRect.top);
                }
            }
            return false;
        }

        private void startFloatingAnimations() {
            if (textView == view || null == floatingAnimation) {
                return;
            }

            final float endValue = floatingAnimation.radius;
            final long duration = floatingAnimation.duration;

            final int direction;

            if (floatingAnimation.direction == 0) {
                direction = gravity == Gravity.TOP || gravity == Gravity.BOTTOM ? 2 : 1;
            } else {
                direction = floatingAnimation.direction;
            }

            final String property = direction == 2 ? "translationY" : "translationX";
            animator = ObjectAnimator.ofFloat(textView, property, -endValue, endValue);
            animator.setDuration(duration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());

            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);

            animator.start();
        }

        void getAnchorPoint(final Gravity gravity, Point outPoint) {
            Point point = validatePositionAnchorPoint(gravity, outPoint);

            point.x -= drawRect.left;
            point.y -= drawRect.top;

            if (!hideArrow) {
                if (gravity == Gravity.LEFT || gravity == Gravity.RIGHT) {
                    point.y -= padding / 2;
                } else if (gravity == Gravity.TOP || gravity == Gravity.BOTTOM) {
                    point.x -= padding;
                    /** Ancora a 20dp da esquerda */
                    point.x = alignAnchorToLeft ? (int) dpToPx(20) : point.x;
                    /** point.x -= padding / 2 ;*/
                }
            }
        }

        private Point validatePositionAnchorPoint(final Gravity gravity, Point outPoint) {
            if (gravity == Gravity.BOTTOM) {
                outPoint.x = viewRect.centerX();
                outPoint.y = viewRect.bottom;
            } else if (gravity == Gravity.TOP) {
                outPoint.x = viewRect.centerX();
                outPoint.y = viewRect.top;
            } else if (gravity == Gravity.RIGHT) {
                outPoint.x = viewRect.right;
                outPoint.y = viewRect.centerY();
            } else if (gravity == Gravity.LEFT) {
                outPoint.x = viewRect.left;
                outPoint.y = viewRect.centerY();
            } else if (this.gravity == Gravity.CENTER) {
                outPoint.x = viewRect.centerX();
                outPoint.y = viewRect.centerY();
            }
            return outPoint;
        }

        @Override
        public void setText(final CharSequence text) {
            this.text = text;
            if (null != textView) {
                textView.setText(Html.fromHtml((String) text));
            }
        }

        @Override
        public boolean onTouchEvent(@NonNull final MotionEvent event) {
            final int action = event.getActionMasked();

            if (!attached || !showing || !isShown() || closePolicy == ClosePolicy.NONE || (!activated && activateDelay > 0)) {
                return false;
            }

            return action == MotionEvent.ACTION_DOWN && verifyContainsTouch(event);
        }

        private boolean verifyContainsTouch(MotionEvent event) {
            Rect outRect = new Rect();
            view.getGlobalVisibleRect(outRect);
            boolean containsTouch = outRect.contains((int) event.getX(), (int) event.getY());

            if (null != viewOverlay) {
                viewOverlay.getGlobalVisibleRect(outRect);
                containsTouch |= outRect.contains((int) event.getX(), (int) event.getY());
            }

            if (containsTouch) {
                if (ClosePolicy.touchInside(closePolicy)) {
                    onClose(true, true, false);
                }
                return ClosePolicy.consumeInside(closePolicy);
            }

            if (ClosePolicy.touchOutside(closePolicy)) {
                onClose(true, false, false);
            }
            return ClosePolicy.consumeOutside(closePolicy);
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            if (!attached) {
                return;
            }
            super.onDraw(canvas);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int myWidth = 0;
            int myHeight = 0;

            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthMode != MeasureSpec.UNSPECIFIED) {
                /** Add padding de 20dp */
                myWidth = widthSize;
            }

            if (heightMode != MeasureSpec.UNSPECIFIED) {
                myHeight = heightSize;
            }

            setViewAndOverlayVisibility(myWidth, myHeight, widthSize, heightSize);
            setMeasuredDimension(myWidth, myHeight);
        }

        private void setViewAndOverlayVisibility(int myWidth, int myHeight, int widthSize, int heightSize) {
            if (null != view && view.getVisibility() != GONE) {
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.AT_MOST);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(myHeight, MeasureSpec.AT_MOST);
                view.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }

            if (null != viewOverlay && viewOverlay.getVisibility() != GONE) {
                final int childWidthMeasureSpec;
                final int childHeightMeasureSpec;
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
                viewOverlay.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }

        private void onClose(boolean fromUser, boolean containsTouch, boolean immediate) {

            if (!isAttached()) {
                return;
            }

            if (null != callback) {
                callback.onTooltipClose(this, fromUser, containsTouch);
            }
            hide(immediate ? 0 : fadeDuration);
        }
    }

    public static final class AnimationBuilder {
        int radius;
        int direction;
        long duration;
        boolean completed;
        @SuppressWarnings("unused")
        public static final AnimationBuilder DEFAULT = new AnimationBuilder().build();
        @SuppressWarnings("unused")
        public static final AnimationBuilder SLOW = new AnimationBuilder().setDuration(600).setRadius(4).build();

        public AnimationBuilder() {
            radius = 8;
            direction = 0;
            duration = 400;
        }

        public AnimationBuilder setRadius(int value) {
            throwIfCompleted();
            this.radius = value;
            return this;
        }

        private void throwIfCompleted() {
            if (completed) {
                throw new IllegalStateException("Builder cannot be modified");
            }
        }

        /**
         * @param value 0 for auto, 1 horizontal, 2 vertical
         */
        @SuppressWarnings("unused")
        public AnimationBuilder setDirection(int value) {
            throwIfCompleted();
            this.direction = value;
            return this;
        }

        public AnimationBuilder setDuration(long value) {
            throwIfCompleted();
            this.duration = value;
            return this;
        }

        public AnimationBuilder build() {
            throwIfCompleted();
            completed = true;
            return this;
        }
    }

    public static final class Builder {
        private static int sNextId = 0;
        int id;
        CharSequence text;
        View view;
        Gravity gravity;
        int actionbarSize = 0;
        int textResId = R.layout.tooltip_textview;
        int closePolicy = ClosePolicy.NONE;
        long showDuration;
        Point point;
        long showDelay = 0;
        boolean hideArrow;
        int maxWidth = -1;
        int defStyleRes = R.style.ToolTipLayoutDefaultStyle;
        int defStyleAttr = R.attr.ttlm_defaultStyle;
        long activateDelay = 0;
        boolean isCustomView;
        boolean restrictToScreenEdges = true;
        long fadeDuration = 200;
        Callback closeCallback;
        boolean completed;
        boolean overlay = true;
        AnimationBuilder floatingAnimation;
        Typeface typeface;
        int margin;
        boolean alignAnchorToLeft;
        private int color;
        private int textColor;

        public Builder(int id) {
            this.id = id;
        }

        @SuppressWarnings("unused")
        public Builder() {
            this.id = sNextId++;
        }

        @SuppressWarnings("unused")
        public Builder withCustomView(int resId) {
            throwIfCompleted();
            return withCustomView(resId, true);
        }

        private void throwIfCompleted() {
            if (completed) {
                throw new IllegalStateException("Builder cannot be modified");
            }
        }

        /**
         * Use a custom View for the tooltip. Note that the custom view
         * must include a TextView which id is `@android:id/text1`.<br />
         * Moreover, when using a custom view, the anchor arrow will not be shown
         *
         * @param resId             the custom layout view.
         * @param replaceBackground if true the custom view's background won't be replaced
         * @return the builder for chaining.
         */
        public Builder withCustomView(int resId, boolean replaceBackground) {
            this.textResId = resId;
            this.isCustomView = replaceBackground;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder withStyleId(int styleId) {
            throwIfCompleted();
            this.defStyleAttr = 0;
            this.defStyleRes = styleId;
            return this;
        }

        public Builder fitToScreen(boolean value) {
            throwIfCompleted();
            restrictToScreenEdges = value;
            return this;
        }

        public Builder fadeDuration(long ms) {
            throwIfCompleted();
            fadeDuration = ms;
            return this;
        }

        public Builder withCallback(Callback callback) {
            throwIfCompleted();
            this.closeCallback = callback;
            return this;
        }

        public Builder text(Resources res, @StringRes int resId) {
            return text(res.getString(resId));
        }

        public Builder text(CharSequence text) {
            throwIfCompleted();
            this.text = text;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder typeface(Typeface typeface) {
            throwIfCompleted();
            this.typeface = typeface;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder maxWidth(Resources res, @DimenRes int dimension) {
            return maxWidth(res.getDimensionPixelSize(dimension));
        }

        public Builder maxWidth(int maxWidth) {
            throwIfCompleted();
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder floatingAnimation(AnimationBuilder builder) {
            throwIfCompleted();
            this.floatingAnimation = builder;
            return this;
        }

        /**
         * Enable/disable the default overlay view
         *
         * @param value false to disable the overlay view. True by default
         */
        public Builder withOverlay(boolean value) {
            throwIfCompleted();
            this.overlay = value;
            return this;
        }

        public Builder anchor(View view, Gravity gravity) {
            throwIfCompleted();
            this.point = null;
            this.view = view;
            this.gravity = gravity;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder anchor(final Point point, final Gravity gravity) {
            throwIfCompleted();
            this.view = null;
            this.point = new Point(point);
            this.gravity = gravity;
            return this;
        }

        /**
         return withArrow(show);
         }*/

        /**
         * Hide/Show the tooltip arrow (trueby default)
         * <p>
         * show true to show the arrow, false to hide it
         *
         * @return the builder for chaining.
         * <p>
         * public Builder withArrow(boolean show) {
         * throwIfCompleted();
         * this.hideArrow = !show;
         * return this;
         * }
         */

        public Builder actionBarSize(Resources resources, int resId) {
            return actionBarSize(resources.getDimensionPixelSize(resId));
        }

        public Builder actionBarSize(final int actionBarSize) {
            throwIfCompleted();
            this.actionbarSize = actionBarSize;
            return this;
        }

        public Builder closePolicy(ClosePolicy policy, long milliseconds) {
            throwIfCompleted();
            this.closePolicy = policy.build();
            this.showDuration = milliseconds;
            return this;
        }

        public Builder activateDelay(long ms) {
            throwIfCompleted();
            this.activateDelay = ms;
            return this;
        }

        public Builder showDelay(long ms) {
            throwIfCompleted();
            this.showDelay = ms;
            return this;
        }

        public Builder build() {
            throwIfCompleted();
            if (floatingAnimation != null && !floatingAnimation.completed) {
                throw new IllegalStateException("Builder not closed");
            }
            completed = true;
            overlay = overlay && gravity != Gravity.CENTER;
            return this;
        }

        public Builder marginRightAndLeft(int margin) {
            throwIfCompleted();
            this.margin = margin;
            return this;
        }

        public Builder alignAnchorToLeft(boolean alignAnchorToLeft) {
            throwIfCompleted();
            this.alignAnchorToLeft = alignAnchorToLeft;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder textColor(int color) {
            this.textColor = color;
            return this;
        }
    }
}

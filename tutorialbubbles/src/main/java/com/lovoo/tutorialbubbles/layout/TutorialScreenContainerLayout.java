package com.lovoo.tutorialbubbles.layout;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lovoo.tutorialbubbles.R;
import com.lovoo.tutorialbubbles.TutorialScreen;
import com.lovoo.tutorialbubbles.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * this ViewGroup serves as container layout for a tutorial layout and supplies this functionality.
 * <ul>
 * <li>positioning tutorial layout within its container</li>
 * <li>dimming background</li>
 * <li>delegating touch events</li>
 * <li>shows highlight views by not dimming an area where an underlying view is</li>
 * </ul>
 *
 * @author Johannes Braun
 */
public class TutorialScreenContainerLayout extends ViewGroup {

    private static final boolean DEBUG = false;

    private static final int DEFAULT_FUNNEL_WIDTH = 25;
    private static final int DEFAULT_FUNNEL_LENGTH = 20;
    private static final int DEFAULT_BUBBLE_CORNER_RADIUS = 5;
    private static final int DEFAULT_OFFSET_FROM_ANCHOR = 5;

    @CheckForNull
    private View mAnchor;

    @Nonnull
    private Rect mAnchorBounds;

    private LinkedHashMap<Integer, DisplayBox> mDisplayAreas;
    private int mDesiredTutorialScreenWidth;
    private int mDesiredTutorialScreenHeight;
    private int mHalfTutorialScreenWidth;
    private int mHalfTutroialScreenHeight;

    private int mFunnelLength;
    private int mFunnelWidth;

    private HashMap<Integer, Paint> mDebugPaints;
    private Paint mAnchourDebugPaint;
    private Paint mClearPaint;

    private int mOffestFromAnchor;
    private int[] mInitialTutorialPadding;
    private int mTutorialBackgroundColor;
    private int mBubbleCornerRadius;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mStatusbarHeight;
    private boolean mIsWindowTranslucent;
    private boolean mIsWindowManaged;

    @Nonnull
    private ArrayList<HighlightEntry> mHightlightViews;
    private ChildPos mChildPos;

    private OnAttachStateChangeListener mAnchorDetachListener;
    private boolean mAnchorIsDetached;

    public TutorialScreenContainerLayout ( Context context ) {
        this(context, null);
    }

    public TutorialScreenContainerLayout ( Context context, AttributeSet attrs ) {
        this(context, attrs, 0);
    }

    public TutorialScreenContainerLayout ( Context context, AttributeSet attrs, int defStyleAttr ) {
        super(context, attrs, defStyleAttr);
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(android.R.color.black));
        colorDrawable.setAlpha(127);
        if (Build.VERSION.SDK_INT > 15) {
            setBackground(colorDrawable);
        } else {
            setBackgroundDrawable(colorDrawable);
        }

        mStatusbarHeight = Utils.getSystemStatusBarHeight(context);
        mDisplayWidth = Utils.getDisplayWidth(context);
        mDisplayHeight = Utils.getDisplayHeight(context);
        mIsWindowTranslucent = Utils.isWindowTranslucent(context);

        mDisplayAreas = new LinkedHashMap<>();
        mAnchorBounds = new Rect();

        mFunnelLength = Utils.dpToPx(context, DEFAULT_FUNNEL_LENGTH);
        mFunnelWidth = Utils.dpToPx(context, DEFAULT_FUNNEL_WIDTH);
        mTutorialBackgroundColor = getResources().getColor(R.color.tooltip_background);
        mBubbleCornerRadius = Utils.dpToPx(context, DEFAULT_BUBBLE_CORNER_RADIUS);
        mOffestFromAnchor = Utils.dpToPx(context, DEFAULT_OFFSET_FROM_ANCHOR);

        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint.setStyle(Paint.Style.FILL);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mHightlightViews = new ArrayList<>();

        if (DEBUG) {
            mDebugPaints = new HashMap<>();

            Paint leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            leftPaint.setStyle(Paint.Style.FILL);
            leftPaint.setColor(getResources().getColor(R.color.transparentYellow));
            mDebugPaints.put(Gravity.LEFT, leftPaint);

            Paint topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            topPaint.setStyle(Paint.Style.FILL);
            topPaint.setColor(getResources().getColor(R.color.transparentRed));
            mDebugPaints.put(Gravity.TOP, topPaint);

//            Paint rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            rightPaint.setStyle(Paint.Style.FILL);
//            rightPaint.setColor(getResources().getColor(R.color.transparentBlue));
//            mDebugPaints.put(Gravity.RIGHT, rightPaint);

            Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bottomPaint.setStyle(Paint.Style.FILL);
            bottomPaint.setColor(getResources().getColor(R.color.transparentGreen));
            mDebugPaints.put(Gravity.BOTTOM, bottomPaint);

            mAnchourDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mAnchourDebugPaint.setStyle(Paint.Style.FILL);
            mAnchourDebugPaint.setColor(getResources().getColor(R.color.notification_bubble_female));
        }

        setLayoutTransition(new LayoutTransition());

    }

    /**
     * inits the view.
     *
     * @param tutorial an inflated view that will be added to the screen
     * @param anchor   a anchor view
     */
    public void init ( View tutorial, View anchor, TutorialScreenDimension dimensions ) {
        this.mAnchor = anchor;

        calcDisplayableAreas();

        mAnchorDetachListener = new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow ( View v ) {
                mAnchorIsDetached = false;
            }

            @Override
            public void onViewDetachedFromWindow ( View v ) {
                // joba: workaround if anchor is an optionsMenuItem
                // in this case, every time onCreateOptionsMenu is called, a new view is inflated
                // for that menu item, however we still have the old reference. For now we indicate with
                // a flag that we lost the current ref in onDetach. In measurement pass we don't update
                // the bounds as they should not have changed (except menu items change in runtime within the same fragment)
                mAnchorIsDetached = true;
//                mAnchor.removeOnAttachStateChangeListener(mAnchorDetachListener);
            }
        };
        this.mAnchor.addOnAttachStateChangeListener(mAnchorDetachListener);

        this.mIsWindowManaged = dimensions.isWindowManaged;

        mInitialTutorialPadding = new int[]{tutorial.getPaddingLeft(), tutorial.getPaddingTop(),
                tutorial.getPaddingRight(), tutorial.getPaddingBottom()};


        mDesiredTutorialScreenWidth = dimensions.width;
        if (mIsWindowTranslucent || !dimensions.isWindowManaged) {
            mDesiredTutorialScreenHeight = dimensions.height;
        } else {
            mDesiredTutorialScreenHeight = dimensions.height - mStatusbarHeight;
        }

        mHalfTutorialScreenWidth = mDesiredTutorialScreenWidth / 2;
        mHalfTutroialScreenHeight = mDesiredTutorialScreenHeight / 2;


        tutorial.setClickable(true);


        addView(tutorial);
        tutorial.setVisibility(INVISIBLE);
    }

    /**
     * sets width of the funnel (schnippbatz) in px.
     *
     * @param funnelWidth width in px
     */
    public void setFunnelWidth ( int funnelWidth ) {
        this.mFunnelWidth = funnelWidth;
    }

    /**
     * sets the legth of the funnel (schnippbatz) in px.
     *
     * @param funnelLength length in px
     */
    public void setFunnelLength ( Integer funnelLength ) {
        this.mFunnelLength = funnelLength;
    }

    /**
     * sets the color of the tutorial bubble.
     *
     * @param tutorialBackgroundColor color resource
     */
    public void setTutorialBackgroundColor ( int tutorialBackgroundColor ) {
        this.mTutorialBackgroundColor = tutorialBackgroundColor;
    }

    /**
     * sets the highlightviews.
     *
     * @param hightlightViews collection of views
     */
    public void setHighlightViews ( @Nonnull ArrayList<TutorialScreen.HighlightView> hightlightViews ) {
        boolean resetCache = false;
        boolean resetBackground = false;
        View view;
        for (TutorialScreen.HighlightView e : hightlightViews) {
            view = e.mView;
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            if (!mIsWindowTranslucent && mIsWindowManaged) {
                rect.offset(0, -Utils.getSystemStatusBarHeight(getContext()));
            }

            if (!mIsWindowManaged) {
                // adjust global anchor view position for viewgroups that are smaller than the display
                rect.offset(-(mDisplayWidth - mDesiredTutorialScreenWidth), -(mDisplayHeight - mDesiredTutorialScreenHeight));
            }

            if (!e.mUseViewBoundsAsMask && (view.getMeasuredHeight() > 0 && view.getMeasuredWidth() > 0)) {

                if (view.getBackground() == null) {
                    view.setBackgroundColor(Color.WHITE);
                    resetBackground = true;
                }

                if (!view.isDrawingCacheEnabled()) {
                    view.setDrawingCacheEnabled(true);
                    resetCache = true;
                }
                view.buildDrawingCache();
                Bitmap b = view.getDrawingCache();
                if (b != null) {
                    Bitmap cache = Bitmap.createBitmap(b);
                    mHightlightViews.add(new HighlightEntry(view, rect, cache, e.mUseViewBoundsAsMask));
                    view.destroyDrawingCache();
                } else {
                    b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(b);
                    view.draw(c);
                    mHightlightViews.add(new HighlightEntry(view, rect, b, e.mUseViewBoundsAsMask));
                }
            } else {
                mHightlightViews.add(new HighlightEntry(view, rect, null, e.mUseViewBoundsAsMask));
            }

            if (resetCache) {
                view.setDrawingCacheEnabled(false);
                resetCache = false;
            }
            if (resetBackground) {
                view.setBackgroundResource(0);
                resetBackground = false;
            }
        }
    }

    public void setOffestFromAnchor ( Integer offestFromAnchor ) {
        this.mOffestFromAnchor = offestFromAnchor;
    }

    @Override
    protected void onMeasure ( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        if (childCount > 1) {
            throw new IllegalStateException("this view can only handle one child in layout");
        }

        // 1. calc boxes
        calcDisplayableAreas();

        // 2. measure the popup
        View tutorial = getChildAt(0);

        LayoutParams params = tutorial.getLayoutParams();
        int specHeight, specWidth;

        if (params.width > 0) {
            specWidth = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY);
        } else {
            specWidth = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        if (params.height > 0) {
            specHeight = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
        } else {
            specHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        // get how big this view want to be
        tutorial.measure(specWidth, specHeight);

        measureTutorialInLargestBox(tutorial);
    }

    @Override
    protected void onLayout ( boolean changed, int l, int t, int r, int b ) {
        View tutorial = getChildAt(0);

        // finally layout tutorial at calculated position
        if (mChildPos != null) {
            tutorial.layout(mChildPos.left, mChildPos.top, mChildPos.left + tutorial.getMeasuredWidth(), mChildPos.top + tutorial.getMeasuredHeight());
            tutorial.setVisibility(VISIBLE);
        }
    }

    private void measureTutorialInLargestBox ( @Nonnull View tutorial ) {
        int bestGravity = 0;
        float bestValue = Float.MAX_VALUE;

        for (Map.Entry<Integer, DisplayBox> entry : mDisplayAreas.entrySet()) {
            Rect entryRect = entry.getValue().rect;
            float w = Math.abs(tutorial.getMeasuredWidth() / (float) entryRect.width());
            float h = Math.abs(tutorial.getMeasuredHeight() / (float) entryRect.height());

            if (w + h < bestValue) {
                bestValue = w + h;
                bestGravity = entry.getKey();
            }
        }

        DisplayBox bestFittingBox = mDisplayAreas.get(bestGravity);
        if (bestFittingBox != null) {

            BubbleDrawable.BubbleBuilder bubbleBuilder = BubbleDrawable.createBubbleBuilder();
            bubbleBuilder.setBubbleCorner(mBubbleCornerRadius)
                    .setBubbleColor(mTutorialBackgroundColor)
                    .setEdgeThickness(0f)
                    .setFunnelWidth(mFunnelWidth);

            // configure bubble and tutorial views padding
            switch (bestFittingBox.gravity) {
                case Gravity.LEFT:
                    bubbleBuilder.setFunnelGravity(Gravity.RIGHT);
                    bubbleBuilder.setFunnelVector(mFunnelLength, 0);
                    tutorial.setPadding(mInitialTutorialPadding[0], mInitialTutorialPadding[1],
                            mInitialTutorialPadding[2] + mFunnelLength, mInitialTutorialPadding[3]);
                    break;
                case Gravity.TOP:
                    bubbleBuilder.setFunnelGravity(Gravity.BOTTOM);
                    bubbleBuilder.setFunnelVector(0, mFunnelLength);
                    tutorial.setPadding(mInitialTutorialPadding[0], mInitialTutorialPadding[1], mInitialTutorialPadding[2],
                            mInitialTutorialPadding[3] + mFunnelLength);
                    break;
                case Gravity.RIGHT:
                    bubbleBuilder.setFunnelGravity(Gravity.LEFT);
                    bubbleBuilder.setFunnelVector(mFunnelLength, 0);
                    tutorial.setPadding(mInitialTutorialPadding[0] + mFunnelLength, mInitialTutorialPadding[1],
                            mInitialTutorialPadding[2], mInitialTutorialPadding[3]);
                    break;
                case Gravity.BOTTOM:
                    bubbleBuilder.setFunnelGravity(Gravity.TOP);
                    bubbleBuilder.setFunnelVector(0, mFunnelLength);
                    tutorial.setPadding(mInitialTutorialPadding[0], mInitialTutorialPadding[1] + mFunnelLength,
                            mInitialTutorialPadding[2], mInitialTutorialPadding[3]);
                    break;
                default:
            }

            tutorial.measure(MeasureSpec.makeMeasureSpec(bestFittingBox.rect.width(), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(bestFittingBox.rect.height(), MeasureSpec.AT_MOST));

            // calculate position within the display box according to anchor
            mChildPos = calcInnerBoxPosition(bestFittingBox, tutorial);

            float funnelPosition = calcFunnelPosition(bestFittingBox.gravity, mChildPos, tutorial);
            bubbleBuilder.setFunnelPointRelative(funnelPosition);
            BubbleDrawable drawable = bubbleBuilder.build();

            if (Build.VERSION.SDK_INT < 16) {
                tutorial.setBackgroundDrawable(drawable);
            } else {
                tutorial.setBackground(drawable);
            }
        }
    }

    private float calcFunnelPosition ( int gravity, ChildPos childPos, View tutorial ) {
        float relativePos = 0.5f;

        switch (gravity) {
            case Gravity.TOP:
            case Gravity.BOTTOM:
                if (mAnchorBounds.width() < tutorial.getMeasuredWidth()) {
                    relativePos = (mAnchorBounds.exactCenterX() - childPos.left) / (tutorial.getMeasuredWidth() - mBubbleCornerRadius);

                    float funnelOverflow = (tutorial.getMeasuredWidth() * relativePos) + (mFunnelWidth / 2);
                    if (funnelOverflow > tutorial.getMeasuredWidth()) {
                        relativePos = relativePos - (funnelOverflow / tutorial.getMeasuredWidth() - 1);
                    }
                }
                break;
            case Gravity.LEFT:
            case Gravity.RIGHT:
                if (mAnchorBounds.height() < tutorial.getMeasuredHeight()) {
                    relativePos = (mAnchorBounds.exactCenterY() - childPos.top) / (tutorial.getMeasuredHeight() - mBubbleCornerRadius);

                    float funnelOverflow = (tutorial.getMeasuredHeight() * relativePos) + (mFunnelWidth / 2);
                    if (funnelOverflow > tutorial.getMeasuredHeight()) {
                        relativePos = relativePos - (funnelOverflow / tutorial.getMeasuredHeight() - 1);
                    }
                }
                break;
            default:
        }
        return relativePos;
    }

    private ChildPos calcInnerBoxPosition ( @Nonnull DisplayBox displayBox, @Nonnull View tutorial ) {
        ChildPos childPos = new ChildPos(displayBox.rect.left, displayBox.rect.top);

        switch (displayBox.gravity) {
            case Gravity.LEFT:
                if (mAnchorBounds.bottom < mHalfTutroialScreenHeight) {
                    // top
                    childPos.top = displayBox.rect.top;
                    if (childPos.top + tutorial.getMeasuredHeight() < mAnchorBounds.bottom) {
                        childPos.top = mAnchorBounds.bottom - tutorial.getMeasuredHeight();
                    }
                } else if (mAnchorBounds.top > mHalfTutroialScreenHeight) {
                    // bottom
                    childPos.top = displayBox.rect.bottom - tutorial.getMeasuredHeight();
                    if (childPos.top > mAnchorBounds.top) {
                        childPos.top = mAnchorBounds.top;
                    }
                } else {
                    // center
                    childPos.top = mHalfTutroialScreenHeight - tutorial.getMeasuredHeight() / 2;
                }
                childPos.left = displayBox.rect.right - tutorial.getMeasuredWidth();
                break;
            case Gravity.TOP:
                if (mAnchorBounds.right < mHalfTutorialScreenWidth) {
                    // left
                    childPos.left = displayBox.rect.left;
                    if (childPos.left + tutorial.getMeasuredWidth() < mAnchorBounds.right) {
                        childPos.left = mAnchorBounds.right - tutorial.getMeasuredWidth();
                    }
                } else if (mAnchorBounds.left > mHalfTutorialScreenWidth) {
                    // right
                    childPos.left = displayBox.rect.right - tutorial.getMeasuredWidth();
                    if (childPos.left > mAnchorBounds.left) {
                        childPos.left = mAnchorBounds.left;
                    }
                } else {
                    // center
                    childPos.left = mHalfTutorialScreenWidth - tutorial.getMeasuredWidth() / 2;
                }
                childPos.top = displayBox.rect.bottom - tutorial.getMeasuredHeight();
                break;
            case Gravity.RIGHT:
                if (mAnchorBounds.bottom < mHalfTutroialScreenHeight) {
                    // top
                    childPos.top = displayBox.rect.top;
                    if (childPos.top + tutorial.getMeasuredHeight() < mAnchorBounds.bottom) {
                        childPos.top = mAnchorBounds.bottom - tutorial.getMeasuredHeight();
                    }
                } else if (mAnchorBounds.top > mHalfTutroialScreenHeight) {
                    // bottom
                    childPos.top = displayBox.rect.bottom - tutorial.getMeasuredHeight();
                    if (childPos.top > mAnchorBounds.top) {
                        childPos.top = mAnchorBounds.top;
                    }
                } else {
                    // center
                    childPos.top = mHalfTutroialScreenHeight - tutorial.getMeasuredHeight() / 2;
                }
                break;
            case Gravity.BOTTOM:
                if (mAnchorBounds.right < mHalfTutorialScreenWidth) {
                    // left
                    childPos.left = displayBox.rect.left;
                    if (childPos.left + tutorial.getMeasuredWidth() < mAnchorBounds.right) {
                        childPos.left = mAnchorBounds.right - tutorial.getMeasuredWidth();
                    }
                } else if (mAnchorBounds.left > mHalfTutorialScreenWidth) {
                    // right
                    childPos.left = displayBox.rect.right - tutorial.getMeasuredWidth();
                    if (childPos.left > mAnchorBounds.left) {
                        childPos.left = mAnchorBounds.left;
                    }
                } else {
                    // center
                    childPos.left = mHalfTutorialScreenWidth - tutorial.getMeasuredWidth() / 2;
                }
                break;
            default:
        }

        return childPos;
    }

    private void calcDisplayableAreas () {
        if (mAnchor == null || mAnchorIsDetached) {
            return;
        }

        mAnchor.getGlobalVisibleRect(mAnchorBounds);

        if (!mIsWindowManaged) {
            mAnchorBounds.offset(-(mDisplayWidth - mDesiredTutorialScreenWidth), -(mDisplayHeight - mDesiredTutorialScreenHeight));
        }

        if (!mIsWindowTranslucent && mIsWindowManaged) {
            mAnchorBounds.offset(0, -mStatusbarHeight);
        }

        // left
        Rect left = new Rect();
        left.left = getPaddingLeft();
        left.top = getPaddingTop();
        left.right = mAnchorBounds.left - mOffestFromAnchor;
        left.bottom = mDesiredTutorialScreenHeight - getPaddingBottom();
        mDisplayAreas.put(Gravity.LEFT, new DisplayBox(left, Gravity.LEFT));

        // top
        Rect top = new Rect();
        top.left = getPaddingLeft();
        top.top = getPaddingTop();
        top.right = mDesiredTutorialScreenWidth - getPaddingRight();
        top.bottom = mAnchorBounds.top - mOffestFromAnchor;
        mDisplayAreas.put(Gravity.TOP, new DisplayBox(top, Gravity.TOP));

        // right
        Rect right = new Rect();
        right.left = mAnchorBounds.right + mOffestFromAnchor;
        right.top = getPaddingTop();
        right.right = mDesiredTutorialScreenWidth - getPaddingRight();
        right.bottom = mDesiredTutorialScreenHeight - getPaddingBottom();
        mDisplayAreas.put(Gravity.RIGHT, new DisplayBox(right, Gravity.RIGHT));

        // botton
        Rect bottom = new Rect();
        bottom.left = getPaddingLeft();
        bottom.top = mAnchorBounds.bottom + mOffestFromAnchor;
        bottom.right = mDesiredTutorialScreenWidth - getPaddingRight();
        bottom.bottom = mDesiredTutorialScreenHeight - getPaddingBottom();
        mDisplayAreas.put(Gravity.BOTTOM, new DisplayBox(bottom, Gravity.BOTTOM));

        mDisplayAreas = (LinkedHashMap<Integer, DisplayBox>) sortByValue(mDisplayAreas);

    }

    @Override
    protected void onDraw ( Canvas canvas ) {
        super.onDraw(canvas);

        for (HighlightEntry entry : mHightlightViews) {
            if (entry.useBoundsAsmask && entry.rect != null) {
                canvas.drawRect(entry.rect, mClearPaint);
            } else if (entry.drawingCache != null && entry.rect != null) {
                canvas.drawBitmap(entry.drawingCache, null, entry.rect, mClearPaint);
            }
        }

        if (DEBUG) {
            // display the displayable areas as colored boxes while debugging
            for (Map.Entry<Integer, Paint> paints : mDebugPaints.entrySet()) {
                DisplayBox box = mDisplayAreas.get(paints.getKey());
                if (box != null) {
                    canvas.drawRect(box.rect, paints.getValue());
                }
            }

            if (mAnchourDebugPaint != null && mAnchorBounds != null) {
                canvas.drawRect(mAnchorBounds, mAnchourDebugPaint);
            }

            canvas.drawRect(0, 0, 30, 30, mAnchourDebugPaint);
        }
    }

    @Override
    public boolean onInterceptTouchEvent ( MotionEvent ev ) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            for (HighlightEntry entry : mHightlightViews) {
                if (entry.rect.contains((int) ev.getX(), (int) ev.getY())) {
                    entry.view.performClick();
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }


    private Map sortByValue ( Map unsortMap ) {
        List list = new LinkedList(unsortMap.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare ( Object o1, Object o2 ) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }


    /**
     * an quadrant area inside the layout based upon an anchor.
     */
    private static class DisplayBox implements Comparable<DisplayBox> {
        @Nonnull
        Rect rect;
        int gravity;
        int area;

        public DisplayBox ( @Nonnull Rect rect, int gravity ) {
            this.rect = rect;
            this.area = rect.width() * rect.height();
            this.gravity = gravity;
        }

        @Override
        public int compareTo ( DisplayBox another ) {
            return another.area - this.area;
        }

        @Override
        public String toString () {
            return rect + "area: " + area;
        }
    }

    //region inner classes
    private static class ChildPos {
        int left;
        int top;

        public ChildPos ( int left, int top ) {
            this.left = left;
            this.top = top;
        }
    }

    private static class HighlightEntry {
        Rect rect;
        Bitmap drawingCache;
        View view;
        boolean useBoundsAsmask;

        public HighlightEntry ( @Nonnull View view, @Nonnull Rect rect, @Nullable Bitmap cache, boolean useBoundsAsmask ) {
            this.view = view;
            this.useBoundsAsmask = useBoundsAsmask;
            this.rect = rect;
            this.drawingCache = cache;
        }
    }

    public static class TutorialScreenDimension {
        public final int width;
        public final int height;
        public final boolean isWindowManaged;

        /**
         * constructs dimensions class.
         *
         * @param mWidth          the width, the overlaying layout should have
         * @param mHeight         the height, the overlaying layout should have
         * @param isWindowManaged flag that shows whether the layout is windowManaged or not
         */
        public TutorialScreenDimension ( int mWidth, int mHeight, boolean isWindowManaged ) {
            this.width = mWidth;
            this.height = mHeight;
            this.isWindowManaged = isWindowManaged;
        }
    }
    //endregion
}

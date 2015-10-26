package com.lovoo.tutorialbubbles;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.lovoo.tutorialbubbles.layout.TutorialScreenContainerLayout;
import com.lovoo.tutorialbubbles.utils.Utils;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstract class that handles creation and display state of a tutorial.
 * supports to internal strategies:
 * <ul>
 * <li>uses {@link WindowManager} if SYSTEM_ALERT_WINDOW permission is set</li>
 * <li>uses layout hierarchy if {@link TutorialBuilder#setParentLayout} is set with a proper viewgroup</li>
 * </ul>
 * if SYSTEM_ALERT_WINDOW is set and no parent layout is defined, {@link WindowManager} is used.
 * In that case {@link #onPause()} and {@link #onResume()} must be called from the outside android mContext in order to correctly show and dismiss
 * the tutorial when the app moves from foreground to background
 *
 * @author Johannes Braun
 */
public abstract class TutorialScreen {

    //region members
    @Nonnull
    protected Context mContext;
    protected boolean mShouldShow = false;
    //endregion

    private TutorialScreen () {
    }

    TutorialScreen ( TutorialBuilder builder ) {
        mContext = builder.mContext;
    }

    /**
     * Creates and inits the tutorial.
     *
     * @param builder the builder to init this instance
     */
    protected abstract void init ( TutorialBuilder builder );

    protected final View createContainerLayoutWithTutorial ( TutorialBuilder builder ) {
        View tutorialLayout = LayoutInflater.from(mContext).inflate(builder.mTutorialLayoutRes, null);

        // run callback for inflated layout, if set
        if (builder.mTutorialLayoutInflatedListener != null) {
            builder.mTutorialLayoutInflatedListener.onLayoutInflated(tutorialLayout);
        }

        TutorialScreenContainerLayout containerLayout = new TutorialScreenContainerLayout(mContext);
        containerLayout.init(tutorialLayout, builder.mAnchorView, getTutorialDimensions());
        if (builder.mFunnelWidth != null) {
            containerLayout.setFunnelWidth(builder.mFunnelWidth);
        }
        if (builder.mFunnelLength != null) {
            containerLayout.setFunnelLength(builder.mFunnelLength);
        }
        if (builder.mBackgroundColor != null) {
            containerLayout.setTutorialBackgroundColor(builder.mBackgroundColor);
        }

        if (builder.mOffset != null) {
            containerLayout.setOffestFromAnchor(builder.mOffset);
        }

        containerLayout.setHighlightViews(builder.mHighlightViews);

        int padding = Utils.dpToPx(mContext, 15);
        containerLayout.setPadding(padding, padding, padding, padding);

        if (builder.mDismissible != null && builder.mDismissible) {
            containerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick ( View v ) {
                    dismissTutorial();
                }
            });
        }

        return containerLayout;
    }

    //region public methods

    /**
     * displays the tutorial on the screen.
     */
    public void showTutorial () {
        mShouldShow = true;
    }

    /**
     * dismisses the {@code TutorialScreen} by cleaning up the {@code WindowManager}.
     */
    public void dismissTutorial () {
        mShouldShow = false;
    }

    /**
     * callback that should be called from Android {@code Activity} or {@code Fragment}.
     * removes {@code TutorialScreen}
     */
    public abstract void onPause ();

    /**
     * callback that should be called from Android {@code Activity} or {@code Fragment}.
     * readdes {@code TutorialScreen}
     */
    public abstract void onResume ();

    /**
     * alter dismiss status after creation.
     *
     * @param dismissible true if dismissible by pressing anywhere outside of the layout, false otherwise
     */
    public abstract void setDismissible ( boolean dismissible );

    /**
     * returns visible state of the popup.
     *
     * @return true if currently showing, false otherwise
     */
    public abstract boolean isShowing ();
    //endregion

    //region inner classes

    /**
     * implement this interface if you want to react to layout inflation.
     */
    public interface OnTutorialLayoutInflatedListener {
        /**
         * is invoked right after layout inflation.
         *
         * @param view the inflated view
         */
        void onLayoutInflated ( View view );
    }

    protected abstract TutorialScreenContainerLayout.TutorialScreenDimension getTutorialDimensions ();

    /**
     * builder class that composes an TutorialScreen Instance.
     */
    public static class TutorialBuilder {

        @Nonnull
        protected Context mContext;
        @Nonnull
        protected final Integer mTutorialLayoutRes;
        @Nonnull
        protected final View mAnchorView;
        @CheckForNull
        protected OnTutorialLayoutInflatedListener mTutorialLayoutInflatedListener;
        @CheckForNull
        protected Integer mFunnelWidth;
        @CheckForNull
        protected Integer mFunnelLength;
        @CheckForNull
        protected Integer mBackgroundColor;
        @CheckForNull
        protected Boolean mDismissible;
        @Nonnull
        protected ArrayList<HighlightView> mHighlightViews;
        @CheckForNull
        protected View mParentContainer;
        @CheckForNull
        Integer mOffset;

        /**
         * creates a builder to config and return a {@link TutorialScreen}.
         *
         * @param tutorialLayoutRes a layout resource
         * @param anchorView        a view at which the layout resource will be displayed to
         */
        public TutorialBuilder ( @Nonnull Integer tutorialLayoutRes, @Nonnull View anchorView ) {
            this.mContext = anchorView.getContext();
            this.mTutorialLayoutRes = tutorialLayoutRes;
            this.mAnchorView = anchorView;
            this.mHighlightViews = new ArrayList<>();
        }

        /**
         * called after all configuation is done.
         *
         * @return TutorialScreen Instance
         */
        @CheckForNull
        public TutorialScreen build () {
            if (mParentContainer != null) {
                return new LayoutManagedTutorialScreen(this);
            } else if (hasAppWindowManagerPermission()) {
                return new WindowManagedTutorialScreen(this);
            }
            return null;
        }

        /**
         * set a listener that will be called each time the layout resource is inflated.
         *
         * @param listener the listener
         * @return this builder
         */
        public TutorialBuilder setOnTutorialLayoutInflatedListener ( OnTutorialLayoutInflatedListener listener ) {
            this.mTutorialLayoutInflatedListener = listener;
            return this;
        }

        /**
         * sets the width of the funnel.
         *
         * @param funnelWidth width in px
         * @return this builder
         */
        public TutorialBuilder setFunnelWidth ( int funnelWidth ) {
            this.mFunnelWidth = funnelWidth;
            return this;
        }

        /**
         * sets the width of the funnel.
         *
         * @param funnelLength length in px
         * @return this builder
         */
        public TutorialBuilder setFunnelLength ( int funnelLength ) {
            this.mFunnelLength = funnelLength;
            return this;
        }

        /**
         * sets the color of the tutorial as a color resource.
         *
         * @param color resource int
         * @return this builder
         */
        public TutorialBuilder setTutorialBackgroundColor ( int color ) {
            this.mBackgroundColor = color;
            return this;
        }

        /**
         * sets wether the tutorial popup can be dismissed by pressing somewhere next to the layout.
         *
         * @param dismissible true if dismissible, false otherwise
         * @return this builder
         */
        public TutorialBuilder setDismissible ( Boolean dismissible ) {
            this.mDismissible = dismissible;
            return this;
        }

        /**
         * adds a view that wont be dimmed by the background.
         *
         * @param view                the highlighted view
         * @param useViewBoundsAsMask true if the rectangular bounds of the view can be used, false if drawing cache should be used
         *                            (slower but non rectangular views will be supported)
         * @return this builder
         */
        public TutorialBuilder addHighlightView ( View view, boolean useViewBoundsAsMask ) {
            this.mHighlightViews.add(new HighlightView(view, useViewBoundsAsMask));
            return this;
        }

        /**
         * sets the parent layout, at which the {@code TutorialScreen} will be added to.
         *
         * @param parent should be a viewgroup
         * @return this builder
         */
        public TutorialBuilder setParentLayout ( View parent ) {
            mParentContainer = parent;
            return this;
        }

        /**
         * sets the offset from the anchor view.
         *
         * @param offset offset in px
         * @return this builder
         */
        public TutorialBuilder setTutorialOffsetFromAnchor ( int offset ) {
            this.mOffset = offset;
            return this;
        }

        private boolean hasAppWindowManagerPermission () {
            try {
                PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
                if (info.requestedPermissions != null) {
                    for (String p : info.requestedPermissions) {
                        if (p.equals("android.permission.SYSTEM_ALERT_WINDOW")) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class HighlightView {
        public final View mView;
        public final boolean mUseViewBoundsAsMask;

        public HighlightView ( View mView, boolean mUseViewBoundsAsMask ) {
            this.mView = mView;
            this.mUseViewBoundsAsMask = mUseViewBoundsAsMask;
        }
    }

    //endregion
}

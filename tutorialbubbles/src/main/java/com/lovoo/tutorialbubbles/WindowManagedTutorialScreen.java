package com.lovoo.tutorialbubbles;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import com.lovoo.tutorialbubbles.utils.Utils;
import com.lovoo.tutorialbubbles.layout.TutorialScreenContainerLayout;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This implementation of TutorialScreen uses androids {@link WindowManager} to display a tutorial popup.
 * It requires SYSTEM_ALERT_WINDOW permission in order to work.
 *
 * @author Johannes Braun
 */
public class WindowManagedTutorialScreen extends TutorialScreen {

    public static final String TAG = WindowManagedTutorialScreen.class.getSimpleName();

    @Nonnull
    private WindowManager mWindowManager;
    @CheckForNull
    private WindowEntry mAddedView;

    protected WindowManagedTutorialScreen ( @Nonnull TutorialBuilder builder ) {
        super(builder);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        init(builder);
    }

    @Override
    protected void init ( TutorialBuilder builder ) {
        View containerLayout = createContainerLayoutWithTutorial(builder);

        int flags;
        if (Utils.isWindowTranslucent(mContext)) {
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        } else {
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                flags,
                PixelFormat.TRANSLUCENT);

        mAddedView = new WindowEntry(containerLayout, params, false);
    }

    @Override
    protected TutorialScreenContainerLayout.TutorialScreenDimension getTutorialDimensions () {
        return new TutorialScreenContainerLayout.TutorialScreenDimension(Utils.getDisplayWidth(mContext), Utils.getDisplayHeight(mContext), true);
    }

    @Override
    public void showTutorial () {
        super.showTutorial();
        addViewsToWindow();
    }

    @Override
    public void dismissTutorial () {
        removeViewsFromWindow();
        super.dismissTutorial();
    }

    @Override
    public boolean isShowing () {
        if (mAddedView == null) {
            return false;
        }
        return mAddedView.isAdded;
    }

    @Override
    public void onPause () {
        removeViewsFromWindow();
    }

    @Override
    public void onResume () {
        addViewsToWindow();
    }

    @Override
    public void setDismissible ( boolean dismissible ) {
        if (mAddedView != null) {
            if (dismissible) {
                mAddedView.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick ( View v ) {
                        dismissTutorial();
                    }
                });
            } else {
                mAddedView.view.setOnClickListener(null);
            }
        }
    }

    //region privat and protected internal methods
    private void addViewsToWindow () {
        if (!mShouldShow || mAddedView == null) {
            return;
        }

        if (!mAddedView.isAdded) {
            mWindowManager.addView(mAddedView.view, mAddedView.layoutParams);
            mAddedView.isAdded = true;
        }
    }

    private void removeViewsFromWindow () {
        if (mAddedView != null) {
            if (mAddedView.isAdded) {
                try {
                    mWindowManager.removeView(mAddedView.view);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            mAddedView.isAdded = false;
        }
    }

    /**
     * encapsulates entry, that can be added to or removed from the WindowManager.
     */
    protected static class WindowEntry {
        View view;
        WindowManager.LayoutParams layoutParams;
        boolean isAdded;

        public WindowEntry ( View view, WindowManager.LayoutParams layoutParams, boolean isAdded ) {
            this.view = view;
            this.layoutParams = layoutParams;
            this.isAdded = isAdded;
        }
    }
}

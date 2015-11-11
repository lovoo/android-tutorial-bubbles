package com.lovoo.tutorialbubbles;

import android.view.View;
import android.view.ViewGroup;

import com.lovoo.tutorialbubbles.layout.TutorialScreenContainerLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This implementation of TutorialScreen uses the layout hierarchy to show the tutorial.
 * In order to work a mParent {@code ViewGroup} must be supplied, in which the tutorial screen will be added to.
 *
 * @author Johannes Braun
 */
class LayoutManagedTutorialScreen extends TutorialScreen {

    private static final String TAG = LayoutManagedTutorialScreen.class.getSimpleName();

    @Nullable
    private final View mParent;
    @Nonnull
    private View mContainerLayout;
    private boolean mIsShowing;

    public LayoutManagedTutorialScreen ( @Nonnull TutorialBuilder builder ) {
        super(builder);
        mParent = builder.mParentContainer;
        init(builder);
    }

    @Override
    protected void init ( TutorialBuilder builder ) {
        mContainerLayout = createContainerLayoutWithTutorial(builder);
        mContainerLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mContainerLayout.setClickable(true);
    }

    @Override
    protected TutorialScreenContainerLayout.TutorialScreenDimension getTutorialDimensions () {
        if(mParent != null) {
            return new TutorialScreenContainerLayout.TutorialScreenDimension(mParent.getMeasuredWidth(), mParent.getMeasuredHeight(), false);
        } else {
            return new TutorialScreenContainerLayout.TutorialScreenDimension(0, 0, false);
        }
    }

    @Override
    public void showTutorial () {
        super.showTutorial();
        addLayout();
    }

    @Override
    public void dismissTutorial () {
        super.dismissTutorial();
        removeLayout();
    }

    @Override
    public void setDismissible ( boolean dismissible ) {
        if (dismissible) {
            mContainerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick ( View v ) {
                    dismissTutorial();
                }
            });
        } else {
            mContainerLayout.setOnClickListener(null);
            mContainerLayout.setClickable(true);
        }
    }

    @Override
    public boolean isShowing () {
        return mIsShowing;
    }

    @Override
    public void onPause () {
        removeLayout();
    }

    @Override
    public void onResume () {
        addLayout();
    }

    private void addLayout () {
        if (mParent instanceof ViewGroup) {
            if(mContainerLayout.getParent() != null){
                return;
            }
            if(mShouldShow) {
                mIsShowing = true;
                ViewGroup viewGroup = (ViewGroup) mParent;
                viewGroup.addView(mContainerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    }

    private void removeLayout () {
        if (mParent instanceof ViewGroup) {
            mIsShowing = false;
            ViewGroup viewGroup = (ViewGroup) mParent;
            viewGroup.removeView(mContainerLayout);
        }
    }
}

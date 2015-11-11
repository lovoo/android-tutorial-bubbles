package com.lovoo.tutorialbubbles.layout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import com.lovoo.tutorialbubbles.utils.Vector2D;

import javax.annotation.Nonnull;

/**
 * Drawable that draws an Comic-Style-Bubble. You have to config this Drawable by code with
 * {@code BubbleDrawable.build()} or {@code reBuild()}. All configs can be chained.
 * <p></p>
 * Created by mariokreussel on 02.06.14.
 */
public class BubbleDrawable extends Drawable {

    /**
     * Configuration class for {@link BubbleDrawable}.
     * Provides all setter as chains. You have to call
     * {@code finish()} to get your created Drawable instance.
     */
    public static class BubbleBuilder {

        private BubbleDrawable mDrawable;

        private BubbleBuilder ( BubbleDrawable drawable ) {
            mDrawable = drawable;
            mDrawable.initPaint();
        }

        /**
         * Set Bubble background color.
         * @param color target value
         * @return current instance
         */
        public BubbleBuilder setBubbleColor ( int color ) {
            mDrawable.mBubblePaint.setColor(color);
            return this;
        }

        /**
         * Set Edge color.
         * @param color target value
         * @return current instance
         */
        public BubbleBuilder setEdgeColor ( int color ) {
            mDrawable.mEdgePaint.setColor(color);
            return this;
        }

        /**
         * Set Edge stroke thickness.
         * @param thickness target value, {@code 0 or less} for no edge
         * @return current instance
         */
        public BubbleBuilder setEdgeThickness ( float thickness ) {
            mDrawable.mEdgePaint.setStrokeWidth(thickness);
            return this;
        }

        /**
         * Set Bubble round corner values in pixel.
         * @param corner target value
         * @return current instance
         */
        public BubbleBuilder setBubbleCorner ( int corner ) {
            mDrawable.mBubbleCorner = corner;
            return this;
        }

        /**
         * Set width of the Bubble Funnel.
         * @param width target value
         * @return current instance
         */
        public BubbleBuilder setFunnelWidth ( int width ) {
            mDrawable.mFunnelWidth = width;
            return this;
        }

        /**
         * Set relative middle point for the Funnel.
         * Value should be between {@code [0, 1]}.
         * @param middlePointRelative target value
         * @return current instance
         */
        public BubbleBuilder setFunnelPointRelative ( float middlePointRelative ) {
            mDrawable.mFunnelStartRelative = middlePointRelative;
            return this;
        }

        /**
         * Set gravity of the Bubble Funnel.
         * @param gravity target value
         * @return current instance
         */
        public BubbleBuilder setFunnelGravity ( int gravity ) {
            mDrawable.mFunnelGravity = gravity;
            return this;
        }

        /**
         * Set the Bubble Funnel Vector.
         * Vector starts at funnel middle point and create an triangle with the funnel width.
         * @param x target value for x
         * @param y target value for y
         * @return current instance
         */
        public BubbleBuilder setFunnelVector ( int x, int y ) {
            mDrawable.mFunnelVector = new Vector2D(x, y);
            return this;
        }

        /**
         * Finish current configuration.
         * @return created or re-configured Drawable
         */
        public BubbleDrawable build () {
            mDrawable.initPath();
            return mDrawable;
        }
    }

    //region members
    private Vector2D mFunnelVector;
    private int mFunnelWidth;
    private float mFunnelStartRelative;
    private int mFunnelGravity;

    private int mBubbleCorner;

    private Path mBubblePath;
    private Paint mBubblePaint;
    private Paint mEdgePaint;
    //endregion

    /**
     * Create a new default instance of an BubbleDrawable.
     * Only for private usage. Please use {@code BubbleDrawable.createBubbleBuilder()}.
     */
    private BubbleDrawable () {
        super();
        mFunnelGravity = Gravity.CENTER;
        initPaint();
        initPath();
    }

    //region init
    private void initPaint () {
        if (mBubblePaint == null) {
            mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBubblePaint.setStyle(Paint.Style.FILL);
            mBubblePaint.setColor(Color.WHITE);
            mBubbleCorner = 6;
        }

        if (mEdgePaint == null) {
            mEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEdgePaint.setStyle(Paint.Style.STROKE);
            mEdgePaint.setColor(Color.BLACK);
            mEdgePaint.setStrokeWidth(2);
        }
    }

    private void initPath () {

        mBubblePath = new Path();

        Rect r = new Rect(getBounds());

        if (mEdgePaint != null) {
            int strokeHalfSize = (int) (mEdgePaint.getStrokeWidth() / 2f);
            r.left += strokeHalfSize;
            r.top += strokeHalfSize;
            r.right -= strokeHalfSize;
            r.bottom -= strokeHalfSize;
        }

        switch (mFunnelGravity) {
            case Gravity.LEFT:
                r.left += Math.abs(mFunnelVector.x);
                break;
            case Gravity.RIGHT:
                r.right -= Math.abs(mFunnelVector.x);
                break;
            case Gravity.TOP:
                r.top += Math.abs(mFunnelVector.y);
                break;
            case Gravity.BOTTOM:
                r.bottom -= Math.abs(mFunnelVector.y);
                break;
            default:
                break;
        }

        //start
        mBubblePath.moveTo(r.left + mBubbleCorner, r.top);

        intersectFunnel(r, Gravity.TOP);

        //top horizontal line.
        mBubblePath.lineTo(r.right - mBubbleCorner, r.top);

        //top right arc
        int arc = mBubbleCorner * 2;
        mBubblePath.arcTo(new RectF(r.right - arc, r.top, r.right, r.top + arc), 270, 90);

        intersectFunnel(r, Gravity.RIGHT);

        //right vertical line.
        mBubblePath.lineTo(r.right, r.bottom - mBubbleCorner);

        //bottom right arc.
        mBubblePath.arcTo(new RectF(r.right - arc, r.bottom - arc, r.right, r.bottom), 0, 90);

        intersectFunnel(r, Gravity.BOTTOM);

        //bottom horizontal line.
        mBubblePath.lineTo(r.left + mBubbleCorner, r.bottom);

        //bottom left arc.
        mBubblePath.arcTo(new RectF(r.left, r.bottom - arc, r.left + arc, r.bottom), 90, 90);

        intersectFunnel(r, Gravity.LEFT);

        //left horizontal line.
        mBubblePath.lineTo(r.left, r.top + mBubbleCorner);

        //top right arc.
        mBubblePath.arcTo(new RectF(r.left, r.top, r.left + arc, r.top + arc), 180, 90);

        mBubblePath.close();
    }

    private void intersectFunnel ( Rect r, int gravity ) {
        if (gravity != mFunnelGravity) {
            return;
        }

        float lineToX;
        float lineToY;
        float halfFunnelSize = mFunnelWidth / 2f;

        switch (gravity) {
            case Gravity.LEFT:
                lineToX = r.left;
                lineToY = ((r.bottom - (mBubbleCorner * 2)) * mFunnelStartRelative) + halfFunnelSize + mBubbleCorner;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX -= Math.abs(mFunnelVector.x);
                lineToY += mFunnelVector.y - halfFunnelSize;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX = r.left;
                lineToY = lineToY - mFunnelVector.y - halfFunnelSize;
                mBubblePath.lineTo(lineToX, lineToY);
                break;

            case Gravity.RIGHT:
                lineToX = r.right;
                lineToY = ((r.bottom - (mBubbleCorner * 2)) * mFunnelStartRelative) - halfFunnelSize + mBubbleCorner;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX += Math.abs(mFunnelVector.x);
                lineToY += mFunnelVector.y + halfFunnelSize;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX = r.right;
                lineToY = lineToY - mFunnelVector.y + halfFunnelSize;
                mBubblePath.lineTo(lineToX, lineToY);
                break;

            case Gravity.TOP:
                lineToX = ((r.right - (mBubbleCorner * 2)) * mFunnelStartRelative) - halfFunnelSize + mBubbleCorner;
                lineToY = r.top;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX += mFunnelVector.x + halfFunnelSize;
                lineToY -= Math.abs(mFunnelVector.y);
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX = lineToX - mFunnelVector.x + halfFunnelSize;
                lineToY = r.top;
                mBubblePath.lineTo(lineToX, lineToY);
                break;

            case Gravity.BOTTOM:
                lineToX = ((r.right - (mBubbleCorner * 2)) * mFunnelStartRelative) + halfFunnelSize + mBubbleCorner;
                lineToY = r.bottom;
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX += mFunnelVector.x - halfFunnelSize;
                lineToY += Math.abs(mFunnelVector.y);
                mBubblePath.lineTo(lineToX, lineToY);

                lineToX = lineToX - mFunnelVector.x - halfFunnelSize;
                lineToY = r.bottom;
                mBubblePath.lineTo(lineToX, lineToY);
                break;
            default:
                break;
        }
    }
    //endregion

    //region getter
    public static BubbleBuilder createBubbleBuilder () {
        return new BubbleBuilder(new BubbleDrawable());
    }

    public BubbleBuilder getBubbleBuilder () {
        return new BubbleBuilder(this);
    }

    public int getBubbleColor () {
        initPaint();
        return mBubblePaint.getColor();
    }

    public int getEdgeColor () {
        initPaint();
        return mEdgePaint.getColor();
    }

    public float getEdgeThickness () {
        initPaint();
        return mEdgePaint.getStrokeWidth();
    }

    public int getBubbleCorner () {
        return mBubbleCorner;
    }

    public int getFunnelWidth () {
        return mFunnelWidth;
    }

    public float getFunnelStart () {
        return mFunnelStartRelative;
    }

    public int getFunnelGravity () {
        return mFunnelGravity;
    }

    public Vector2D getFunnelVector () {
        return mFunnelVector;
    }
    //endregion

    //region implemented methods
    @Override
    protected void onBoundsChange ( Rect bounds ) {
        super.onBoundsChange(bounds);
        initPath();
    }

    @Override
    public void setAlpha ( int alpha ) {
        initPaint();

        mBubblePaint.setAlpha(alpha);
        mEdgePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter ( ColorFilter cf ) {
        initPaint();

        mBubblePaint.setColorFilter(cf);
        mEdgePaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity () {
        initPaint();

        if (mBubblePaint.getColorFilter() != null || mEdgePaint.getColorFilter() != null) {
            // can not define result color
            return PixelFormat.TRANSLUCENT;
        }

        int alphaBackground = mBubblePaint.getColor() >>> 24;
        int alphaEdge = mEdgePaint.getColor() >>> 24;

        if (alphaBackground + alphaEdge == 0) {
            // both colors ar transparent
            return PixelFormat.TRANSPARENT;
        }

        // drawable always draws opaque, translucent and transparent parts
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void draw ( @Nonnull Canvas canvas ) {
        if (mBubblePath == null || mBubblePaint == null) {
            return;
        }

        canvas.drawPath(mBubblePath, mBubblePaint);

        if (mEdgePaint != null && mEdgePaint.getStrokeWidth() > 0f) {
            canvas.drawPath(mBubblePath, mEdgePaint);
        }
    }
    //endregion
}

package com.lovoo.tutorialbubbles.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Johannes Braun
 */
public class Utils {

    private static int mScreenWidth = -1;
    private static int mScreenHeight = -1;

    public static int dpToPx ( Context context, int dp ) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getSystemStatusBarHeight ( Context context ) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getDisplayWidth ( Context context ) {
        if (mScreenWidth == -1) {
            DisplayMetrics displayMetrics = context.getResources()
                    .getDisplayMetrics();
            mScreenWidth = displayMetrics.widthPixels;
        }
        return mScreenWidth;
    }

    public static int getDisplayHeight ( Context context ) {
        if (mScreenHeight == -1) {
            DisplayMetrics displayMetrics = context.getResources()
                    .getDisplayMetrics();
            mScreenHeight = displayMetrics.heightPixels;
        }
        return mScreenHeight;
    }

    /**
     * Determine whether the current context has translucent mode (KITKAT and above).
     * @param context should be some context deriving from activity, NO application context
     * @return true if API-Level is KITKAT and above and translucent flag is enabled, false in any other cases
     */
    @TargetApi(19)
    public static boolean isWindowTranslucent( Context context ){
        if(Build.VERSION.SDK_INT < 19){
            return false;
        }

        if(context == null || !(context instanceof Activity)){
            return false;
        }

        Window window = ((Activity) context).getWindow();
        if(window == null){
            return false;
        }

        return (window.getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }
}

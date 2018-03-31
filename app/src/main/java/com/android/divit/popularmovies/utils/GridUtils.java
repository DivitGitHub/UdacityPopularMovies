package com.android.divit.popularmovies.utils;


import android.content.Context;
import android.util.DisplayMetrics;

public class GridUtils {

    private GridUtils() {
    }

    public static int calculateNumberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns < 2 ? 2 : noOfColumns; // To keep grid aspect
    }
}

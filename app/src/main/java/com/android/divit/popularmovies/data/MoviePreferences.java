package com.android.divit.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.divit.popularmovies.R;

public class MoviePreferences {

    private MoviePreferences() {}

    public static String getSortBy(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String keyForSortBy = context.getString(R.string.pref_list_key);
        String defaultSortBy = context.getString(R.string.pref_sort_by_default_value);

        return sharedPreferences.getString(keyForSortBy, defaultSortBy);
    }

}

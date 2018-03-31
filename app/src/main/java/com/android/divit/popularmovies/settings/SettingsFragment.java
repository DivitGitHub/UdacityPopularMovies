package com.android.divit.popularmovies.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.android.divit.popularmovies.R;

/**
 * Created by Divit on 01/03/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_movies);
    }
}

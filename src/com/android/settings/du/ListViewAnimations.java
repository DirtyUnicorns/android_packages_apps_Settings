/*
 *  Copyright (C) 2013 The Dirty Unicorns Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.du;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class ListViewAnimations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ListViewAnimations";

    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";

    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.listview_animations);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
        String listViewAnimation = Settings.System.getString(resolver, Settings.System.LISTVIEW_ANIMATION);
        if (listViewAnimation != null) {
             mListViewAnimation.setValue(listViewAnimation);
        }
        mListViewAnimation.setOnPreferenceChangeListener(this);

        mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
        String listViewInterpolator = Settings.System.getString(resolver, Settings.System.LISTVIEW_INTERPOLATOR);
        if (listViewInterpolator != null) {
             mListViewInterpolator.setValue(listViewInterpolator);
        }
        mListViewInterpolator.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mListViewAnimation) {
            String value = (String) objValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_ANIMATION, value);
        } else if (preference == mListViewInterpolator) {
            String value = (String) objValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_INTERPOLATOR, value);
        } else {
            return false;
        }

        return true;
    }
}

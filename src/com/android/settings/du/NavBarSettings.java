/*
 * Copyright (C) 2014 The Dirty Unicorns project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.du;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.provider.Settings;

import com.android.internal.util.slim.DeviceUtils;
import com.android.settings.widget.SeekBarPreferenceGlow;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class NavBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NavBarSettings";

    private final Configuration mCurConfig = new Configuration();

    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String NAVIGATION_BUTTON_GLOW_TIME = "navigation_button_glow_time";

    CheckBoxPreference mNavigationBarLeftPref;
    SeekBarPreferenceGlow mNavigationButtonGlowTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navbarsettings);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        mNavigationButtonGlowTime = (SeekBarPreferenceGlow) getPreferenceScreen().findPreference(NAVIGATION_BUTTON_GLOW_TIME);
        mNavigationButtonGlowTime.setDefault(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.NAVIGATION_BUTTON_GLOW_TIME, 500));
        mNavigationButtonGlowTime.setOnPreferenceChangeListener(this);

        mNavigationBarLeftPref = (CheckBoxPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);

        if (!Utils.isPhone(getActivity())) {
            if(mNavigationBarLeftPref != null)
                getPreferenceScreen().removePreference(mNavigationBarLeftPref);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mNavigationButtonGlowTime) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BUTTON_GLOW_TIME, (Integer)objValue);
            return true;
        }
        return false;
    }
}

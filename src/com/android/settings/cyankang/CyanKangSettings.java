/*
 * Copyright (C) 2013 CyanKang
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

package com.android.settings.cyankang;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
<<<<<<< HEAD
import android.preference.Preference.OnPreferenceChangeListener;
=======
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;
>>>>>>> 0f15834... Advanced low battery indicator options [2/2]

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class CyanKangSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CyanKangSettings";
<<<<<<< HEAD
    
=======

    private static final String STATUS_BAR_TRAFFIC_ENABLE = "status_bar_traffic_enable";
    private static final String STATUS_BAR_TRAFFIC_HIDE = "status_bar_traffic_hide";
    private static final String STATUS_BAR_TRAFFIC_SUMMARY = "status_bar_traffic_summary";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";
    private static final String KEY_LOW_BATTERY_WARNING_POLICY = "pref_low_battery_warning_policy";

    private CheckBoxPreference mStatusBarTraffic_enable;
    private CheckBoxPreference mStatusBarTraffic_hide;
    private ListPreference mStatusBarTraffic_summary;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetworkStats;
    private ListPreference mLowBatteryWarning;

>>>>>>> 0f15834... Advanced low battery indicator options [2/2]
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.beerbar_settings);

<<<<<<< HEAD
=======
        mStatusBarNetworkStats = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetStatsUpdate = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        mStatusBarNetworkStats.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        long statsUpdate = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

        mStatusBarTraffic_summary.setEnabled(!mStatusBarNetworkStats.isChecked());

        mLowBatteryWarning = (ListPreference) findPreference(KEY_LOW_BATTERY_WARNING_POLICY);
        int lowBatteryWarning = Settings.System.getInt(resolver,
                Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY, 0);
        mLowBatteryWarning.setValue(String.valueOf(lowBatteryWarning));
        mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntry());
        mLowBatteryWarning.setOnPreferenceChangeListener(this);
>>>>>>> 0f15834... Advanced low battery indicator options [2/2]
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
<<<<<<< HEAD
=======
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mStatusBarTraffic_summary) {
            int val = Integer.valueOf((String) newValue);
            int index = mStatusBarTraffic_summary.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, val);
            mStatusBarTraffic_summary.setSummary(mStatusBarTraffic_summary.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(resolver,
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mLowBatteryWarning) {
            int lowBatteryWarning = Integer.valueOf((String) objValue);
            int index = mLowBatteryWarning.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver, Settings.System.POWER_UI_LOW_BATTERY_WARNING_POLICY,
                    lowBatteryWarning);
            mLowBatteryWarning.setSummary(mLowBatteryWarning.getEntries()[index]);
            return true;
        }
>>>>>>> 0f15834... Advanced low battery indicator options [2/2]
        return false;
    }
}

/*
 * Copyright (C) 2013 The Dirty Unicorns project
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

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Traffic extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "Traffic";

    private static final String STATUS_BAR_TRAFFIC_ENABLE = "status_bar_traffic_enable";
    private static final String STATUS_BAR_TRAFFIC_HIDE = "status_bar_traffic_hide";
    private static final String STATUS_BAR_TRAFFIC_SUMMARY = "status_bar_traffic_summary";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";

    private CheckBoxPreference mStatusBarTraffic_enable;
    private CheckBoxPreference mStatusBarTraffic_hide;
    private ListPreference mStatusBarTraffic_summary;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetworkStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.traffic_indicators);
        PreferenceScreen prefSet = getPreferenceScreen();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_dirt);

        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarTraffic_enable = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_ENABLE);
        mStatusBarTraffic_enable.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TRAFFIC_ENABLE, 0) == 1));

        mStatusBarTraffic_hide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_HIDE);
        mStatusBarTraffic_hide.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TRAFFIC_HIDE, 1) == 1));

        mStatusBarTraffic_summary = (ListPreference) findPreference(STATUS_BAR_TRAFFIC_SUMMARY);
        mStatusBarTraffic_summary.setOnPreferenceChangeListener(this);
        mStatusBarTraffic_summary.setValue((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TRAFFIC_SUMMARY, 3000)) + "");

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
        }
        return false;
    }


    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;

        if (preference == mStatusBarTraffic_enable) {
            value = mStatusBarTraffic_enable.isChecked();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_TRAFFIC_ENABLE, value ? 1 : 0);
        } else if (preference == mStatusBarTraffic_hide) {
            value = mStatusBarTraffic_hide.isChecked();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_TRAFFIC_HIDE, value ? 1 : 0);
        } else if (preference == mStatusBarNetworkStats) {
            value = mStatusBarNetworkStats.isChecked();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
            mStatusBarTraffic_summary.setEnabled(!value);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }
}

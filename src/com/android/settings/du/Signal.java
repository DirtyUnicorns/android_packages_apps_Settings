/*
 *  Copyright (C) 2014 The Dirty Unicorns Project
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

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.internal.util.slim.DeviceUtils;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;
import com.android.settings.util.CMDProcessor;

import java.io.File;

public class Signal extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "Signal";

    private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
    private static final String KEY_SHOW_4G = "show_4g_for_lte";
    private static final String CATEGORY_STATUSBAR_SIGNAL = "statusbar_signal_title";
    private static final String STATUSBAR_6BAR_SIGNAL = "statusbar_6bar_signal";

    private CheckBoxPreference mStatusBarNotifCount;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private CheckBoxPreference mShow4G;
    private PreferenceGroup mStatusbarSignalCategory;
    private CheckBoxPreference mStatusBarSixBarSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.signal);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_dirt);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1);
        mStatusBarNotifCount.setOnPreferenceChangeListener(this);

        mStatusBarNetworkActivity = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
        mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
            Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
        mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);
        mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);

        mStatusbarSignalCategory = (PreferenceGroup) findPreference(CATEGORY_STATUSBAR_SIGNAL);

        mShow4G = (CheckBoxPreference) findPreference(KEY_SHOW_4G);
        if (mShow4G != null) {
            mShow4G.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_4G_FOR_LTE, 0) == 1);
                mShow4G.setOnPreferenceChangeListener(this);
            if (!DeviceUtils.deviceSupportsMobileData(getActivity())) {
                mStatusbarSignalCategory.removePreference(findPreference(KEY_SHOW_4G));
            }
        }

        mStatusBarSixBarSignal = (CheckBoxPreference) findPreference(STATUSBAR_6BAR_SIGNAL);
        mStatusBarSixBarSignal.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_6BAR_SIGNAL, 0) == 1));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if  (preference == mStatusBarSixBarSignal) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_6BAR_SIGNAL, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarNotifCount) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);

        } else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
        } else if (preference == mShow4G) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.SHOW_4G_FOR_LTE, value ? 1 : 0);
        } else {
            return false;
        }

        return true;
    }
}

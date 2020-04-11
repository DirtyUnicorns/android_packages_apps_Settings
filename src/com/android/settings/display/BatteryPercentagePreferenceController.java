/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.display;

import static android.provider.Settings.System.SHOW_BATTERY_PERCENT;
import static android.provider.Settings.System.QS_SHOW_BATTERY_PERCENT;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.internal.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

/**
 * A controller to manage the switch for showing battery percentage in the status bar.
 */

public class BatteryPercentagePreferenceController extends BasePreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final int ON = 1;
    private static final int OFF = 0;

    private static final String KEY_SB_BATTERY_PERCENTAGE = "battery_percentage";
    private static final String KEY_QS_BATTERY_PERCENTAGE = "qs_battery_percentage";

    public BatteryPercentagePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(
                R.bool.config_battery_percentage_setting_available) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null && preference instanceof SwitchPreference) {
            SwitchPreference switchPref = (SwitchPreference) preference;
            if (TextUtils.equals(switchPref.getKey(), KEY_SB_BATTERY_PERCENTAGE)) {
                boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                        SHOW_BATTERY_PERCENT, OFF) == ON;
                switchPref.setChecked(enabled);
            } else if (TextUtils.equals(switchPref.getKey(), KEY_QS_BATTERY_PERCENTAGE)) {
                boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                        QS_SHOW_BATTERY_PERCENT, OFF) == ON;
                switchPref.setChecked(enabled);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SwitchPreference switchPref = (SwitchPreference) preference;
        if (TextUtils.equals(switchPref.getKey(), KEY_SB_BATTERY_PERCENTAGE)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    SHOW_BATTERY_PERCENT, enabled ? ON : OFF);
            switchPref.setChecked(enabled);
            return true;
        } else if (TextUtils.equals(switchPref.getKey(), KEY_QS_BATTERY_PERCENTAGE)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    QS_SHOW_BATTERY_PERCENT, enabled ? ON : OFF);
            switchPref.setChecked(enabled);
            return true;
        }
        return false;
    }
}

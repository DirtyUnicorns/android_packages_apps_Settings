/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.internal.util.slim.DeviceUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenMiscTweaks extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_ALLOW_ROTATION = "allow_rotation";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_BLUR_BEHIND = "blur_behind";
    private static final String KEY_BLUR_RADIUS = "blur_radius";
    private static final String KEY_DISABLE_CAMERA_WIDGET = "disable_camera_widget";
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";
    private static final String LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";
    private static final String PREF_LOCKSCREEN_TORCH = "lockscreen_torch";

    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mAllowRotation;
    private CheckBoxPreference mBlurBehind;
    private SeekBarPreference mBlurRadius;
    private CheckBoxPreference mCameraWidget;
    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mMaximizeKeyguardWidgets;
    private CheckBoxPreference mGlowpadTorch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_misc_tweaks);

        mSeeThrough = (CheckBoxPreference) findPreference(KEY_SEE_TRHOUGH);

        mCameraWidget = (CheckBoxPreference) findPreference(KEY_DISABLE_CAMERA_WIDGET);
        mCameraWidget.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DISABLE_CAMERA_WIDGET, 0) == 1);

        mLockRingBattery = (CheckBoxPreference) findPreference(BATTERY_AROUND_LOCKSCREEN_RING);
        mLockRingBattery.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);

        mMaximizeKeyguardWidgets = (CheckBoxPreference) findPreference(LOCKSCREEN_MAXIMIZE_WIDGETS);
        mMaximizeKeyguardWidgets.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);

        mGlowpadTorch = (CheckBoxPreference) findPreference(
                PREF_LOCKSCREEN_TORCH);
        mGlowpadTorch.setChecked(Settings.System.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_GLOWPAD_TORCH, 0) == 1);
        mGlowpadTorch.setOnPreferenceChangeListener(this);

        mAllowRotation = (CheckBoxPreference) findPreference(KEY_ALLOW_ROTATION);
        mAllowRotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ROTATION, 0) == 1);

        mBlurBehind = (CheckBoxPreference) findPreference(KEY_BLUR_BEHIND);
        mBlurBehind.setChecked(Settings.System.getInt(getContentResolver(),
            Settings.System.LOCKSCREEN_BLUR_BEHIND, 0) == 1);
        mBlurRadius = (SeekBarPreference) findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(getContentResolver(),
            Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);

        updateBlurPrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSeeThrough) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, mSeeThrough.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mAllowRotation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTATION, mAllowRotation.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mCameraWidget) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DISABLE_CAMERA_WIDGET, mCameraWidget.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, mLockRingBattery.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mGlowpadTorch) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_GLOWPAD_TORCH, mGlowpadTorch.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mMaximizeKeyguardWidgets) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, mMaximizeKeyguardWidgets.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mBlurBehind) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BLUR_BEHIND,
                    mBlurBehind.isChecked() ? 1 : 0);
            updateBlurPrefs();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == mBlurRadius) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, (Integer)value);
        }

         return true;
    }

    public void updateBlurPrefs() {
        // until i get around to digging through the frameworks to find where transparent lockscreen
        // is breaking the animation for blur lets just be a little dirty dirty dirty...
        if (mBlurBehind.isChecked()) {
            mSeeThrough.setEnabled(false);
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 1);
        } else {
            mSeeThrough.setEnabled(true);
            if (mSeeThrough.isChecked()) {
                Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 1);
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SEE_THROUGH, 0);
            }
        }
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}

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


import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;
import com.android.settings.util.CMDProcessor;

import java.io.File;

public class MiscTweaks extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "MiscTweaks";

    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String EMULATE_MENU_KEY = "emulate_menu_key";
    private static final String DISABLE_FC_NOTIFICATIONS = "disable_fc_notifications";
    private static final String DISABLE_IMMERSIVE_MESSAGE = "disable_immersive_message";
    private static final String SREC_ENABLE_TOUCHES = "srec_enable_touches";
    private static final String SREC_ENABLE_MIC = "srec_enable_mic";
    private static final String DISABLE_BOOTAUDIO = "disable_bootaudio";
    private static final String PREF_MENU_ARROWS = "navigation_bar_menu_arrow_keys";
    private static final String STATUS_BAR_CUSTOM_HEADER = "custom_status_bar_header";

    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mSMSBreath;
    private CheckBoxPreference mMissedCallBreath;
    private CheckBoxPreference mVoicemailBreath;
    private CheckBoxPreference mEmulateMenuKey;
    private CheckBoxPreference mDisableFC;
    private CheckBoxPreference mDisableIM;
    private CheckBoxPreference mSrecEnableTouches;
    private CheckBoxPreference mSrecEnableMic;
    private CheckBoxPreference mDisableBootAudio;
    private CheckBoxPreference mMenuArrowKeysCheckBox;
    private CheckBoxPreference mStatusBarCustomHeader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.misc_tweaks);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_dirt);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(resolver,Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        try {
            if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        mSMSBreath = (CheckBoxPreference) findPreference(SMS_BREATH);
        mSMSBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_SMS_BREATH, 0) == 1);
        mSMSBreath.setOnPreferenceChangeListener(this);

        mMissedCallBreath = (CheckBoxPreference) findPreference(MISSED_CALL_BREATH);
        mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
        mMissedCallBreath.setOnPreferenceChangeListener(this);

        mVoicemailBreath = (CheckBoxPreference) findPreference(VOICEMAIL_BREATH);
        mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
        mVoicemailBreath.setOnPreferenceChangeListener(this);

        mEmulateMenuKey = (CheckBoxPreference) prefSet.findPreference(EMULATE_MENU_KEY);
        mEmulateMenuKey.setChecked(Settings.System.getInt(resolver,
                Settings.System.EMULATE_HW_MENU_KEY, 0) == 1);
        mEmulateMenuKey.setOnPreferenceChangeListener(this);

        mMenuArrowKeysCheckBox = (CheckBoxPreference) findPreference(PREF_MENU_ARROWS);
        mMenuArrowKeysCheckBox.setChecked((Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS, 1) == 1));

        mDisableFC = (CheckBoxPreference) findPreference(DISABLE_FC_NOTIFICATIONS);
        mDisableFC.setChecked((Settings.System.getInt(resolver,
                Settings.System.DISABLE_FC_NOTIFICATIONS, 0) == 1));

        mDisableIM = (CheckBoxPreference) findPreference(DISABLE_IMMERSIVE_MESSAGE);
        mDisableIM.setChecked((Settings.System.getInt(resolver,
                Settings.System.DISABLE_IMMERSIVE_MESSAGE, 0) == 1));

        mSrecEnableTouches = (CheckBoxPreference) findPreference(SREC_ENABLE_TOUCHES);
        mSrecEnableTouches.setChecked((Settings.System.getInt(resolver,
                Settings.System.SREC_ENABLE_TOUCHES, 0) == 1));

        mSrecEnableMic = (CheckBoxPreference) findPreference(SREC_ENABLE_MIC);
        mSrecEnableMic.setChecked((Settings.System.getInt(resolver,
                Settings.System.SREC_ENABLE_MIC, 0) == 1));

        mStatusBarCustomHeader = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CUSTOM_HEADER);
        mStatusBarCustomHeader.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1);

        mDisableBootAudio = (CheckBoxPreference) findPreference("disable_bootaudio");

        if(!new File("/system/media/audio.mp3").exists() &&
                !new File("/system/media/boot_audio").exists() ) {
            mDisableBootAudio.setEnabled(false);
            mDisableBootAudio.setSummary(R.string.disable_bootaudio_summary_disabled);
        } else {
            mDisableBootAudio.setChecked(!new File("/system/media/audio.mp3").exists());
            if (mDisableBootAudio.isChecked())
                mDisableBootAudio.setSummary(R.string.disable_bootaudio_summary);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if  (preference == mDisableFC) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DISABLE_FC_NOTIFICATIONS, checked ? 1:0);
            return true;
        } else if  (preference == mStatusBarCustomHeader) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER, checked ? 1:0);
            return true;
        } else if  (preference == mSrecEnableTouches) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SREC_ENABLE_TOUCHES, checked ? 1:0);
            return true;
        } else if  (preference == mSrecEnableMic) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SREC_ENABLE_MIC, checked ? 1:0);
            return true;
        } else if  (preference == mDisableIM) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DISABLE_IMMERSIVE_MESSAGE, checked ? 1:0);
            return true;
        } else if (preference == mDisableBootAudio) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/audio.mp3 /system/media/boot_audio");
                Helpers.getMount("ro");
                preference.setSummary(R.string.disable_bootaudio_summary);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/boot_audio /system/media/audio.mp3");
                Helpers.getMount("ro");
            }
            return true;
        } else if (preference == mMenuArrowKeysCheckBox) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBrightnessControl) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
        } else if (preference == mSMSBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_SMS_BREATH, value ? 1 : 0);
        } else if (preference == mMissedCallBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_MISSED_CALL_BREATH, value ? 1 : 0);
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_VOICEMAIL_BREATH, value ? 1 : 0);
        } else if (preference == mEmulateMenuKey) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.EMULATE_HW_MENU_KEY, value ? 1 : 0);
        } else {
            return false;
        }

        return true;
    }
}

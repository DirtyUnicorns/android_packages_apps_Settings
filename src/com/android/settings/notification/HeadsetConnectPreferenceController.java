/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.notification;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.TextUtils;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

import libcore.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class HeadsetConnectPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String HEADSET_CONNECT_PLAYER = "headset_connect_player";

    private ListPreference mLaunchPlayerHeadsetConnection;

    public HeadsetConnectPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return HEADSET_CONNECT_PLAYER;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mLaunchPlayerHeadsetConnection = (ListPreference) screen.findPreference(HEADSET_CONNECT_PLAYER);
        int mLaunchPlayerHeadsetConnectionValue = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.HEADSET_CONNECT_PLAYER, 0, UserHandle.USER_CURRENT);
        mLaunchPlayerHeadsetConnection.setValue(Integer.toString(mLaunchPlayerHeadsetConnectionValue));
        mLaunchPlayerHeadsetConnection.setSummary(mLaunchPlayerHeadsetConnection.getEntry());
        mLaunchPlayerHeadsetConnection.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLaunchPlayerHeadsetConnection) {
            int mLaunchPlayerHeadsetConnectionValue = Integer.valueOf((String) newValue);
            int index = mLaunchPlayerHeadsetConnection.findIndexOfValue((String) newValue);
            mLaunchPlayerHeadsetConnection.setSummary(
                    mLaunchPlayerHeadsetConnection.getEntries()[index]);
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.HEADSET_CONNECT_PLAYER,
                    mLaunchPlayerHeadsetConnectionValue, UserHandle.USER_CURRENT);
        }
        return true;
    }
}

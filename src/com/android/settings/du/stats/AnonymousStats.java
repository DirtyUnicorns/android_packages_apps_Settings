/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.du.stats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.settings.R;

public class AnonymousStats extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {
	private long lastPressedTime;

	private static final int PERIOD = 2000;

	private static final String VIEW_STATS = "pref_view_stats";

	protected static final String ANONYMOUS_OPT_IN = "pref_anonymous_opt_in";
	protected static final String ANONYMOUS_FIRST_BOOT = "pref_anonymous_first_boot";
	protected static final String ANONYMOUS_LAST_CHECKED = "pref_anonymous_checked_in";
	protected static final String ANONYMOUS_ALARM_SET = "pref_anonymous_alarm_set";

	private CheckBoxPreference mEnableReporting;
	private Preference mViewStats;
	private SharedPreferences mPrefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getPreferenceManager() != null) {
			addPreferencesFromResource(R.xml.anonymous_stats);
			PreferenceScreen prefSet = getPreferenceScreen();

                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setIcon(R.drawable.ic_dt_stats);
                        alertDialog.setMessage(this.getResources().getString(R.string.anonymous_statistics_warning));
                        alertDialog.setTitle(R.string.anonymous_statistics_warning_title);
                        alertDialog.show();

			mPrefs = this.getSharedPreferences(Utilities.SETTINGS_PREF_NAME, 0);
			mEnableReporting = (CheckBoxPreference) prefSet
					.findPreference(ANONYMOUS_OPT_IN);
			mViewStats = prefSet.findPreference(VIEW_STATS);

			boolean firstBoot = mPrefs.getBoolean(ANONYMOUS_FIRST_BOOT, true);

			if (mEnableReporting.isChecked() && firstBoot) {
				mPrefs.edit().putBoolean(ANONYMOUS_FIRST_BOOT, false).apply();
				ReportingServiceManager.launchService(this);
			}

			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(1);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mViewStats) {
			// Display the stats page
			Intent intent = new Intent (this, ViewStats.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			// What this does is finish our top application, so when and if the user
			// exits via the ViewStats.java class, it will exit the application, instead
			// of navigate back to this class. However, if the user selects to go back
			// via the ActionBar with the Up navigation I implemented, it will bring the user
			// back to this (Preference)Activity.
			finish();
		} else {
			// If we didn't handle it, let preferences handle it.
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}
}

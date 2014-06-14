/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim.quicksettings;

import static com.android.internal.util.slim.QSConstants.TILES_DEFAULT;
import static com.android.internal.util.slim.QSConstants.DYNAMIC_TILES_DEFAULT;
import static com.android.internal.util.slim.QSConstants.TILE_AIRPLANE;
import static com.android.internal.util.slim.QSConstants.TILE_ALARM;
import static com.android.internal.util.slim.QSConstants.TILE_AUTOROTATE;
import static com.android.internal.util.slim.QSConstants.TILE_BATTERY;
import static com.android.internal.util.slim.QSConstants.TILE_BLUETOOTH;
import static com.android.internal.util.slim.QSConstants.TILE_BRIGHTNESS;
import static com.android.internal.util.slim.QSConstants.TILE_BUGREPORT;
import static com.android.internal.util.slim.QSConstants.TILE_DELIMITER;
import static com.android.internal.util.slim.QSConstants.TILE_EXPANDEDDESKTOP;
import static com.android.internal.util.slim.QSConstants.TILE_IMESWITCHER;
import static com.android.internal.util.slim.QSConstants.TILE_LOCATION;
import static com.android.internal.util.slim.QSConstants.TILE_LOCKSCREEN;
import static com.android.internal.util.slim.QSConstants.TILE_MOBILEDATA;
import static com.android.internal.util.slim.QSConstants.TILE_MUSIC;
import static com.android.internal.util.slim.QSConstants.TILE_NFC;
import static com.android.internal.util.slim.QSConstants.TILE_QUICKRECORD;
import static com.android.internal.util.slim.QSConstants.TILE_QUIETHOURS;
import static com.android.internal.util.slim.QSConstants.TILE_RINGER;
import static com.android.internal.util.slim.QSConstants.TILE_SCREENTIMEOUT;
import static com.android.internal.util.slim.QSConstants.TILE_SETTINGS;
import static com.android.internal.util.slim.QSConstants.TILE_SLEEP;
import static com.android.internal.util.slim.QSConstants.TILE_SYNC;
import static com.android.internal.util.slim.QSConstants.TILE_TORCH;
import static com.android.internal.util.slim.QSConstants.TILE_USBTETHER;
import static com.android.internal.util.slim.QSConstants.TILE_USER;
import static com.android.internal.util.slim.QSConstants.TILE_VOLUME;
import static com.android.internal.util.slim.QSConstants.TILE_WIFI;
import static com.android.internal.util.slim.QSConstants.TILE_WIFIAP;
import static com.android.internal.util.slim.QSConstants.TILE_REBOOT;
import static com.android.internal.util.slim.QSConstants.TILE_NETWORKADB;
import static com.android.internal.util.slim.QSConstants.TILE_GPS;
import static com.android.internal.util.slim.QSConstants.TILE_FCHARGE;
import static com.android.internal.util.slim.QSConstants.TILE_THEME;
import static com.android.internal.util.slim.QSConstants.TILE_SCREENSHOT;
import static com.android.internal.util.slim.QSConstants.TILE_HALO;
import static com.android.internal.util.slim.QSConstants.TILE_ONTHEGO;
import static com.android.internal.util.slim.QSConstants.TILE_PROFILE;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.util.slim.DeviceUtils;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuickSettingsUtil {
    private static final String TAG = "QuickSettingsUtil";

    public static final Map<String, TileInfo> TILES;

    private static final Map<String, TileInfo> ENABLED_TILES = new HashMap<String, TileInfo>();
    private static final Map<String, TileInfo> DISABLED_TILES = new HashMap<String, TileInfo>();

    static {
        TILES = Collections.unmodifiableMap(ENABLED_TILES);
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_AIRPLANE, R.string.title_tile_airplane,
                "com.android.systemui:drawable/ic_qs_airplane_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_BATTERY, R.string.title_tile_battery,
                "com.android.systemui:drawable/ic_qs_battery_neutral"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_BLUETOOTH, R.string.title_tile_bluetooth,
                "com.android.systemui:drawable/ic_qs_bluetooth_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_BRIGHTNESS, R.string.title_tile_brightness,
                "com.android.systemui:drawable/ic_qs_brightness_auto_off"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_EXPANDEDDESKTOP, R.string.title_tile_expanded_desktop,
                "com.android.systemui:drawable/ic_qs_expanded_desktop_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_SLEEP, R.string.title_tile_sleep,
                "com.android.systemui:drawable/ic_qs_sleep"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_LOCATION, R.string.title_tile_location,
                "com.android.systemui:drawable/ic_qs_location_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_LOCKSCREEN, R.string.title_tile_lockscreen,
                "com.android.systemui:drawable/ic_qs_lock_screen_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_MOBILEDATA, R.string.title_tile_mobiledata,
                "com.android.systemui:drawable/ic_qs_signal_full_4"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_NFC, R.string.title_tile_nfc,
                "com.android.systemui:drawable/ic_qs_nfc_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_AUTOROTATE, R.string.title_tile_autorotate,
                "com.android.systemui:drawable/ic_qs_auto_rotate"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_QUIETHOURS, R.string.title_tile_quiet_hours,
                "com.android.systemui:drawable/ic_qs_quiet_hours_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_SCREENTIMEOUT, R.string.title_tile_screen_timeout,
                "com.android.systemui:drawable/ic_qs_screen_timeout"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_SETTINGS, R.string.title_tile_settings,
                "com.android.systemui:drawable/ic_qs_settings"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_RINGER, R.string.title_tile_sound,
                "com.android.systemui:drawable/ic_qs_ring_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_SYNC, R.string.title_tile_sync,
                "com.android.systemui:drawable/ic_qs_sync_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_TORCH, R.string.title_tile_torch,
                "com.android.systemui:drawable/ic_qs_torch_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_USER, R.string.title_tile_user,
                "com.android.systemui:drawable/ic_qs_default_user"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_VOLUME, R.string.title_tile_volume,
                "com.android.systemui:drawable/ic_qs_volume"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_WIFI, R.string.title_tile_wifi,
                "com.android.systemui:drawable/ic_qs_wifi_full_4"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_WIFIAP, R.string.title_tile_wifiap,
                "com.android.systemui:drawable/ic_qs_wifi_ap_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_NETWORKADB, R.string.title_tile_network_adb,
                "com.android.systemui:drawable/ic_qs_network_adb_off"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_MUSIC, R.string.title_tile_music,
                "com.android.systemui:drawable/ic_qs_media_play"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_REBOOT, R.string.title_tile_reboot,
                "com.android.systemui:drawable/ic_qs_reboot"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_QUICKRECORD, R.string.title_tile_quick_record,
                "com.android.systemui:drawable/ic_qs_quickrecord"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_GPS, R.string.title_tile_gps,
                "com.android.systemui:drawable/ic_qs_gps_off"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_FCHARGE, R.string.title_tile_fcharge,
                "com.android.systemui:drawable/ic_qs_fcharge_off"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_THEME, R.string.title_tile_theme,
                "com.android.systemui:drawable/ic_qs_theme_manual"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_SCREENSHOT, R.string.title_tile_screenshot,
                "com.android.systemui:drawable/ic_qs_screenshot"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_ONTHEGO, R.string.title_tile_onthego,
                "com.android.systemui:drawable/ic_qs_onthego"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_PROFILE, R.string.title_tile_profile,
                "com.android.systemui:drawable/ic_qs_profiles"));
        registerTile(new QuickSettingsUtil.TileInfo(
                TILE_HALO, R.string.title_tile_halo,
                "com.android.systemui:drawable/ic_qs_halo_on"));
    }

    private static void registerTile(QuickSettingsUtil.TileInfo info) {
        ENABLED_TILES.put(info.getId(), info);
    }

    private static void removeTile(String id) {
        ENABLED_TILES.remove(id);
        DISABLED_TILES.remove(id);
        TILES_DEFAULT.remove(id);
    }

    private static void disableTile(String id) {
        if (ENABLED_TILES.containsKey(id)) {
            DISABLED_TILES.put(id, ENABLED_TILES.remove(id));
        }
    }

    private static void enableTile(String id) {
        if (DISABLED_TILES.containsKey(id)) {
            ENABLED_TILES.put(id, DISABLED_TILES.remove(id));
        }
    }

    private static synchronized void removeUnsupportedTiles(Context context) {
        // Don't show mobile data options if not supported
        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            removeTile(TILE_MOBILEDATA);
            removeTile(TILE_WIFIAP);
        }

        // Don't show the bluetooth options if not supported
        if (!DeviceUtils.deviceSupportsBluetooth()) {
            removeTile(TILE_BLUETOOTH);
        }

        // Don't show the NFC tile if not supported
        if (!DeviceUtils.deviceSupportsNfc(context)) {
            removeTile(TILE_NFC);
        }

        // Don't show the Torch tile if not supported
        if (!DeviceUtils.deviceSupportsTorch(context)) {
            removeTile(TILE_TORCH);
        }

        // Don't show the Network ADB tile if adb debugging is disabled
        if (!DeviceUtils.adbEnabled(context)) {
            removeTile(TILE_NETWORKADB);
        }

        // Don't show the Fast charge tile if not supported by kernel
        if (!DeviceUtils.fchargeEnabled(context)) {
            removeTile(TILE_FCHARGE);
        }

    }

    public static ArrayList<String> getAllDynamicTiles(Context context) {
         // Don't show the ime switcher if not supported
        if (!DeviceUtils.deviceSupportsImeSwitcher(context)) {
            DYNAMIC_TILES_DEFAULT.remove(TILE_IMESWITCHER);
        }
        // Don't show the usb tethering tile if not supported
        if (!DeviceUtils.deviceSupportsUsbTether(context)) {
            DYNAMIC_TILES_DEFAULT.remove(TILE_USBTETHER);
        }
        return DYNAMIC_TILES_DEFAULT;
    }

    public static String getDynamicTileDescription(Context context, String tile) {
        if (tile.equals(TILE_IMESWITCHER)) {
            return context.getResources().getString(R.string.dynamic_tile_ime_switcher);
        } else if (tile.equals(TILE_USBTETHER)) {
            return context.getResources().getString(R.string.dynamic_tile_usb_tether);
        } else if (tile.equals(TILE_ALARM)) {
            return context.getResources().getString(R.string.dynamic_tile_alarm);
        } else if (tile.equals(TILE_BUGREPORT)) {
            return context.getResources().getString(R.string.dynamic_tile_bugreport);
        }
        return null;
    }

    public static boolean[] toPrimitiveArray(ArrayList<Boolean> booleanList) {
        boolean[] primitives = new boolean[booleanList.size()];
        int index = 0;
        for (Boolean value : booleanList) {
            primitives[index++] = value;
        }
        return primitives;
    }

    public static synchronized void updateAvailableTiles(Context context) {
        removeUnsupportedTiles(context);
    }

    public static boolean isTileAvailable(String id) {
        return ENABLED_TILES.containsKey(id);
    }

    public static String getCurrentTiles(Context context) {
        String tiles = Settings.System.getString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES);
        if (tiles == null) {
            tiles = getDefaultTiles(context);
        }
        return tiles;
    }

    public static void saveCurrentTiles(Context context, String tiles) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES, tiles);
    }

    public static void resetTiles(Context context) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES, null);
    }

    public static String mergeInNewTileString(String oldString, String newString) {
        ArrayList<String> oldList = getTileListFromString(oldString);
        ArrayList<String> newList = getTileListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for (String tile : oldList) {
            if (newList.contains(tile)) {
                mergedList.add(tile);
            }
        }

        // append anything in newlist that isn't already in the merged list to
        // the end of the list
        for (String tile : newList) {
            if (!mergedList.contains(tile)) {
                mergedList.add(tile);
            }
        }

        // return merged list
        return getTileStringFromList(mergedList);
    }

    public static ArrayList<String> getTileListFromString(String tiles) {
        return new ArrayList<String>(Arrays.asList(tiles.split("\\" + TILE_DELIMITER)));
    }

    public static String getTileStringFromList(ArrayList<String> tiles) {
        if (tiles == null || tiles.size() <= 0) {
            return "";
        } else {
            String s = tiles.get(0);
            for (int i = 1; i < tiles.size(); i++) {
                s += TILE_DELIMITER + tiles.get(i);
            }
            return s;
        }
    }

    public static String getDefaultTiles(Context context) {
        removeUnsupportedTiles(context);
        return TextUtils.join(TILE_DELIMITER, TILES_DEFAULT);
    }

    public static class TileInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public TileInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() {
            return mId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public String getIcon() {
            return mIcon;
        }
    }
}

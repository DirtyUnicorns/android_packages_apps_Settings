/*
 * Copyright (C) 2013 SlimRoms Project
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Display;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import com.android.internal.widget.LockPatternUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LockscreenStyle extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "LockscreenStyle";

    private static final String WALLPAPER_NAME = "lockscreen_wallpaper";
    private static final String KEY_LOCKSCREEN_COLORIZE_ICON = "lockscreen_colorize_icon";
    private static final String KEY_LOCKSCREEN_LOCK_ICON = "lockscreen_lock_icon";
    private static final String KEY_LOCKSCREEN_FRAME_COLOR = "lockscreen_frame_color";
    private static final String KEY_LOCKSCREEN_LOCK_COLOR = "lockscreen_lock_color";
    private static final String KEY_LOCKSCREEN_DOTS_COLOR = "lockscreen_dots_color";
    private static final String LOCKSCREEN_BACKGROUND = "lockscreen_background";
    private static final String LOCKSCREEN_BACKGROUND_STYLE = "lockscreen_background_style";
    private static final String LOCKSCREEN_BACKGROUND_COLOR_FILL = "lockscreen_background_color_fill";

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int COLOR_FILL = 0;
    private static final int CUSTOM_IMAGE = 1;
    private static final int DEFAULT = 2;

    private String mDefault;

    private CheckBoxPreference mColorizeCustom;

    private ColorPickerPreference mFrameColor;
    private ColorPickerPreference mLockColor;
    private ColorPickerPreference mDotsColor;

    private ColorPickerPreference mLockColorFill;
    private ListPreference mLockBackground;

    private PreferenceCategory mLockscreenBackground;
    private File wallpaperImage;
    private File wallpaperTemporary;

    private ListPreference mLockIcon;

    private boolean mCheckPreferences;

    private File mLockImage;

    private static final int MENU_RESET = Menu.FIRST;

    private static final int DLG_RESET = 0;
    private static final int REQUEST_PICK_LOCK_ICON = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCustomView();
    }

    private PreferenceScreen createCustomView() {
        mCheckPreferences = false;
        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.lockscreen_style);
        prefSet = getPreferenceScreen();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_dirt);

        // Set to string so we don't have to create multiple objects of it
        mDefault = getResources().getString(R.string.default_string);

        mLockImage = new File(getActivity().getFilesDir() + "/lock_icon.tmp");

        mLockIcon = (ListPreference)
                findPreference(KEY_LOCKSCREEN_LOCK_ICON);
        mLockIcon.setOnPreferenceChangeListener(this);

        mColorizeCustom = (CheckBoxPreference)
                findPreference(KEY_LOCKSCREEN_COLORIZE_ICON);
        mColorizeCustom.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_COLORIZE_LOCK, 0) == 1);
        mColorizeCustom.setOnPreferenceChangeListener(this);

        mFrameColor = (ColorPickerPreference)
                findPreference(KEY_LOCKSCREEN_FRAME_COLOR);
        mFrameColor.setOnPreferenceChangeListener(this);
        int frameColor = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_FRAME_COLOR, -2);
        setPreferenceSummary(mFrameColor,
                getResources().getString(
                R.string.lockscreen_frame_color_summary), frameColor);
        mFrameColor.setNewPreviewColor(frameColor);

        mLockColor = (ColorPickerPreference)
                findPreference(KEY_LOCKSCREEN_LOCK_COLOR);
        mLockColor.setOnPreferenceChangeListener(this);
        int lockColor = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_COLOR, -2);
        setPreferenceSummary(mLockColor,
                getResources().getString(
                R.string.lockscreen_lock_color_summary), lockColor);
        mLockColor.setNewPreviewColor(lockColor);

        mDotsColor = (ColorPickerPreference)
                findPreference(KEY_LOCKSCREEN_DOTS_COLOR);
        mDotsColor.setOnPreferenceChangeListener(this);
        int dotsColor = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_DOTS_COLOR, -2);
        setPreferenceSummary(mDotsColor,
                getResources().getString(
                R.string.lockscreen_dots_color_summary), dotsColor);
        mDotsColor.setNewPreviewColor(dotsColor);

        boolean dotsDisabled = new LockPatternUtils(getActivity()).isSecure()
            && Settings.Secure.getInt(getContentResolver(),
            Settings.Secure.LOCK_BEFORE_UNLOCK, 0) == 0;
        boolean imageExists = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCKSCREEN_LOCK_ICON) != null;
        mDotsColor.setEnabled(!dotsDisabled);
        mLockIcon.setEnabled(!dotsDisabled);
        mColorizeCustom.setEnabled(!dotsDisabled && imageExists);

        mLockscreenBackground = (PreferenceCategory) findPreference(LOCKSCREEN_BACKGROUND);

        mLockBackground = (ListPreference) findPreference(LOCKSCREEN_BACKGROUND_STYLE);
        mLockBackground.setOnPreferenceChangeListener(this);
        mLockBackground.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2)));
        mLockBackground.setSummary(mLockBackground.getEntry());

        mLockColorFill = (ColorPickerPreference) findPreference(LOCKSCREEN_BACKGROUND_COLOR_FILL);
        mLockColorFill.setOnPreferenceChangeListener(this);
        mLockColorFill.setSummary(ColorPickerPreference.convertToARGB(
                Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_COLOR, 0x00000000)));

        updateVisiblePreferences();

        updateLockSummary();

        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefSet;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private Uri getLockscreenExternalUri() {
        File dir = getActivity().getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_LOCK_ICON) {

                if (mLockImage.length() == 0 || !mLockImage.exists()) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.shortcut_image_not_valid),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                File image = new File(getActivity().getFilesDir() + File.separator
                        + "lock_icon" + System.currentTimeMillis() + ".png");
                String path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                image.setReadable(true, false);

                deleteLockIcon();  // Delete current icon if it exists before saving new.
                Settings.Secure.putString(getContentResolver(),
                        Settings.Secure.LOCKSCREEN_LOCK_ICON, path);

                mColorizeCustom.setEnabled(path != null);
            } else if (requestCode == REQUEST_PICK_WALLPAPER) {
                FileOutputStream wallpaperStream = null;
                try {
                    wallpaperStream = getActivity().openFileOutput(WALLPAPER_NAME,
                            Context.MODE_WORLD_READABLE);

                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }
                Uri selectedImageUri = getLockscreenExternalUri();
                Bitmap bitmap;
                if (data != null) {
                    Uri mUri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                mUri);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);

                        Toast.makeText(getActivity(), getResources().getString(R.string.
                                background_result_successful), Toast.LENGTH_LONG).show();
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 1);
                        updateVisiblePreferences();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                    } catch (NullPointerException npe) {
                        Log.e(TAG, "SeletedImageUri was null.");
                        Toast.makeText(getActivity(), getResources().getString(R.string.
                                background_result_not_successful), Toast.LENGTH_LONG).show();
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                }

            }
        } else {
            if (mLockImage.exists()) {
                mLockImage.delete();
            }
        }
        updateLockSummary();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockIcon) {
            int indexOf = mLockIcon.findIndexOfValue(newValue.toString());
            if (indexOf == 0) {
                requestLockImage();
            } else if (indexOf == 2) {
                deleteLockIcon();
                resizeDuLock();
                updateLockSummary();
            } else if (indexOf == 3) {
                deleteLockIcon();
                resizeDuLockone();
                updateLockSummary();
            } else if (indexOf == 4) {
                deleteLockIcon();
                resizeDuLocktwo();
                updateLockSummary();
            } else if (indexOf == 5) {
                deleteLockIcon();
                resizeDuLockthree();
                updateLockSummary();
            } else if (indexOf == 6) {
                deleteLockIcon();
                resizeDuLockfour();
                updateLockSummary();
            } else if (indexOf == 7) {
                deleteLockIcon();
                resizeDuLockfive();
                updateLockSummary();
            } else if (indexOf == 8) {
                deleteLockIcon();
                resizeDuLocksix();
                updateLockSummary();
            } else if (indexOf == 9) {
                deleteLockIcon();
                resizeDuLockseven();
                updateLockSummary();
            } else if (indexOf == 10) {
                deleteLockIcon();
                resizeDuLockeight();
                updateLockSummary();
            } else if (indexOf == 11) {
                deleteLockIcon();
                resizeDuLocknine();
                updateLockSummary();
            } else if (indexOf == 12) {
                deleteLockIcon();
                resizeDuLockten();
                updateLockSummary();
            } else if (indexOf == 13) {
                deleteLockIcon();
                resizeDuLockeleven();
                updateLockSummary();
            } else if (indexOf == 14) {
                deleteLockIcon();
                resizeDuLocktwelve();
                updateLockSummary();
            } else if (indexOf == 15) {
                deleteLockIcon();
                resizeDuLockthirteen();
                updateLockSummary();
            } else if (indexOf == 1) {
                deleteLockIcon();
                updateLockSummary();
            }
            return true;
        } else if (preference == mColorizeCustom) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_COLORIZE_LOCK,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mFrameColor) {
            int val = Integer.valueOf(String.valueOf(newValue));
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_FRAME_COLOR, val);
            setPreferenceSummary(preference,
                    getResources().getString(R.string.lockscreen_frame_color_summary), val);
            return true;
        } else if (preference == mLockColor) {
            int val = Integer.valueOf(String.valueOf(newValue));
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_COLOR, val);
            setPreferenceSummary(preference,
                    getResources().getString(R.string.lockscreen_lock_color_summary), val);
            return true;
        } else if (preference == mDotsColor) {
            int val = Integer.valueOf(String.valueOf(newValue));
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_DOTS_COLOR, val);
            setPreferenceSummary(preference,
                    getResources().getString(R.string.lockscreen_dots_color_summary), val);
            return true;
        } else if (preference == mLockBackground) {
            int index = mLockBackground.findIndexOfValue(String.valueOf(newValue));
            preference.setSummary(mLockBackground.getEntries()[index]);
            return handleBackgroundSelection(index);
        } else if (preference == mLockColorFill) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int value = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_COLOR, value);
            return true;
        }
        return false;
    }

    private void updateVisiblePreferences() {
        int visible = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
        if (visible == 0) {
            mLockscreenBackground.addPreference(mLockColorFill);
        } else {
            mLockscreenBackground.removePreference(mLockColorFill);
        }
    }

    private void setPreferenceSummary(
            Preference preference, String defaultSummary, int value) {
        if (value == -2) {
            preference.setSummary(defaultSummary + " (" + mDefault + ")");
        } else {
            String hexColor = String.format("#%08x", (0xffffffff & value));
            preference.setSummary(defaultSummary + " (" + hexColor + ")");
        }
    }

    private void updateLockSummary() {
        int resId;
        String value = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCKSCREEN_LOCK_ICON);
        if (value == null) {
            resId = R.string.lockscreen_lock_icon_default;
            mLockIcon.setValueIndex(1);
        } else if (value.contains("du_lock")) {
            resId = R.string.lockscreen_lock_icon_du;
            mLockIcon.setValueIndex(2);
        } else if (value.contains("one")) {
            resId = R.string.lockscreen_lock_icon_du_one;
            mLockIcon.setValueIndex(3);
        } else if (value.contains("two")) {
            resId = R.string.lockscreen_lock_icon_du_two;
            mLockIcon.setValueIndex(4);
        } else if (value.contains("three")) {
            resId = R.string.lockscreen_lock_icon_du_three;
            mLockIcon.setValueIndex(5);
        } else if (value.contains("four")) {
            resId = R.string.lockscreen_lock_icon_du_four;
            mLockIcon.setValueIndex(6);
        } else if (value.contains("five")) {
            resId = R.string.lockscreen_lock_icon_du_five;
            mLockIcon.setValueIndex(7);
        } else if (value.contains("six")) {
            resId = R.string.lockscreen_lock_icon_du_six;
            mLockIcon.setValueIndex(8);
        } else if (value.contains("seven")) {
            resId = R.string.lockscreen_lock_icon_du_seven;
            mLockIcon.setValueIndex(9);
        } else if (value.contains("eight")) {
            resId = R.string.lockscreen_lock_icon_du_eight;
            mLockIcon.setValueIndex(10);
        } else if (value.contains("nine")) {
            resId = R.string.lockscreen_lock_icon_du_nine;
            mLockIcon.setValueIndex(11);
        } else if (value.contains("ten")) {
            resId = R.string.lockscreen_lock_icon_du_ten;
            mLockIcon.setValueIndex(12);
        } else if (value.contains("eleven")) {
            resId = R.string.lockscreen_lock_icon_du_eleven;
            mLockIcon.setValueIndex(13);
        } else if (value.contains("twelve")) {
            resId = R.string.lockscreen_lock_icon_du_twelve;
            mLockIcon.setValueIndex(14);
        } else if (value.contains("thirteen")) {
            resId = R.string.lockscreen_lock_icon_du_thirteen;
            mLockIcon.setValueIndex(15);
        } else {
            resId = R.string.lockscreen_lock_icon_custom;
            mLockIcon.setValueIndex(0);
        }
        mLockIcon.setSummary(getResources().getString(resId));
    }

    private void requestLockImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 144, getResources().getDisplayMetrics());

        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", px);
        intent.putExtra("aspectY", px);
        intent.putExtra("outputX", px);
        intent.putExtra("outputY", px);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        try {
            mLockImage.createNewFile();
            mLockImage.setWritable(true, false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mLockImage));
            startActivityForResult(intent, REQUEST_PICK_LOCK_ICON);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void deleteLockIcon() {
        String path = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCKSCREEN_LOCK_ICON);

        if (path != null) {
            File f = new File(path);
            if (f != null && f.exists()) {
                f.delete();
            }
        }

        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.LOCKSCREEN_LOCK_ICON, null);

        mColorizeCustom.setEnabled(false);
        updateLockSummary();
    }

    private void resizeDuLockthirteen() {
        Bitmap duLockthirteen = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_thirteen);
        if (duLockthirteen != null) {
            String path = null;
            int px = requestImageSize();
            duLockthirteen = Bitmap.createScaledBitmap(duLockthirteen, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "thirteen" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockthirteen.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLocktwelve() {
        Bitmap duLocktwelve = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_twelve);
        if (duLocktwelve != null) {
            String path = null;
            int px = requestImageSize();
            duLocktwelve = Bitmap.createScaledBitmap(duLocktwelve, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "twelve" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLocktwelve.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockeleven() {
        Bitmap duLockeleven = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_eleven);
        if (duLockeleven != null) {
            String path = null;
            int px = requestImageSize();
            duLockeleven = Bitmap.createScaledBitmap(duLockeleven, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "eleven" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockeleven.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockten() {
        Bitmap duLockten = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_ten);
        if (duLockten != null) {
            String path = null;
            int px = requestImageSize();
            duLockten = Bitmap.createScaledBitmap(duLockten, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "ten" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockten.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLocknine() {
        Bitmap duLocknine = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_nine);
        if (duLocknine != null) {
            String path = null;
            int px = requestImageSize();
            duLocknine = Bitmap.createScaledBitmap(duLocknine, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "nine" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLocknine.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockeight() {
        Bitmap duLockeight = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_eight);
        if (duLockeight != null) {
            String path = null;
            int px = requestImageSize();
            duLockeight = Bitmap.createScaledBitmap(duLockeight, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "eight" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockeight.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockseven() {
        Bitmap duLockseven = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_seven);
        if (duLockseven != null) {
            String path = null;
            int px = requestImageSize();
            duLockseven = Bitmap.createScaledBitmap(duLockseven, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "seven" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockseven.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLocksix() {
        Bitmap duLocksix = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_six);
        if (duLocksix != null) {
            String path = null;
            int px = requestImageSize();
            duLocksix = Bitmap.createScaledBitmap(duLocksix, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "six" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLocksix.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockfive() {
        Bitmap duLockfive = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_five);
        if (duLockfive != null) {
            String path = null;
            int px = requestImageSize();
            duLockfive = Bitmap.createScaledBitmap(duLockfive, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "five" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockfive.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockfour() {
        Bitmap duLockfour = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_four);
        if (duLockfour != null) {
            String path = null;
            int px = requestImageSize();
            duLockfour = Bitmap.createScaledBitmap(duLockfour, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "four" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockfour.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockthree() {
        Bitmap duLockthree = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_three);
        if (duLockthree != null) {
            String path = null;
            int px = requestImageSize();
            duLockthree = Bitmap.createScaledBitmap(duLockthree, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "three" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockthree.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLocktwo() {
        Bitmap duLocktwo = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_two);
        if (duLocktwo != null) {
            String path = null;
            int px = requestImageSize();
            duLocktwo = Bitmap.createScaledBitmap(duLocktwo, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "two" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLocktwo.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLockone() {
        Bitmap duLockone = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock_one);
        if (duLockone != null) {
            String path = null;
            int px = requestImageSize();
            duLockone = Bitmap.createScaledBitmap(duLockone, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "one" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLockone.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private void resizeDuLock() {
        Bitmap duLock = BitmapFactory.decodeResource(getResources(), R.drawable.du_lock);
        if (duLock != null) {
            String path = null;
            int px = requestImageSize();
            duLock = Bitmap.createScaledBitmap(duLock, px, px, true);
            try {
                mLockImage.createNewFile();
                mLockImage.setWritable(true, false);
                File image = new File(getActivity().getFilesDir() + File.separator
                            + "du_lock" + System.currentTimeMillis() + ".png");
                path = image.getAbsolutePath();
                mLockImage.renameTo(image);
                FileOutputStream outPut = new FileOutputStream(image);
                duLock.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                image.setReadable(true, false);
                outPut.flush();
                outPut.close();
            } catch (Exception e) {
                // Unicorns are better when they're dirty.
                Log.e(TAG, e.getMessage(), e);
                return;
            }

            deleteLockIcon();  // Delete current icon if it exists before saving new.
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LOCK_ICON, path);
            mColorizeCustom.setEnabled(path != null);
            updateLockSummary();
        }
    }

    private int requestImageSize() {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        LockscreenStyle getOwner() {
            return (LockscreenStyle) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.lockscreen_style_reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.Secure.putInt(getActivity().getContentResolver(),
                                    Settings.Secure.LOCKSCREEN_FRAME_COLOR, -2);
                            Settings.Secure.putInt(getActivity().getContentResolver(),
                                    Settings.Secure.LOCKSCREEN_LOCK_COLOR, -2);
                            Settings.Secure.putInt(getActivity().getContentResolver(),
                                    Settings.Secure.LOCKSCREEN_DOTS_COLOR, -2);
                            getOwner().createCustomView();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    private boolean handleBackgroundSelection(int index) {
        if (index == COLOR_FILL) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 0);
            updateVisiblePreferences();
            return true;
        } else if (index == CUSTOM_IMAGE) {
            // Used to reset the image when already set
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
            // Launches intent for user to select an image/crop it to set as background
            Display display = getActivity().getWindowManager().getDefaultDisplay();

            int width = getActivity().getWallpaperDesiredMinimumWidth();
            int height = getActivity().getWallpaperDesiredMinimumHeight();
            float spotlightX = (float)display.getWidth() / width;
            float spotlightY = (float)display.getHeight() / height;

            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra("aspectX", width);
            intent.putExtra("aspectY", height);
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
            intent.putExtra("spotlightX", spotlightX);
            intent.putExtra("spotlightY", spotlightY);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
        } else if (index == DEFAULT) {
            // Sets background to default
            Settings.System.putInt(getContentResolver(),
                            Settings.System.LOCKSCREEN_BACKGROUND_STYLE, 2);
            updateVisiblePreferences();
            return true;
        }
        return false;
    }
}

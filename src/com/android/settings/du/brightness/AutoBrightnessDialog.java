/*
 *  Copyright (C) 2013 The CyanogenMod Project
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
package com.android.settings.du.brightness;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Spline;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import com.android.settings.R;

public class AutoBrightnessDialog extends AlertDialog
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private static final String TAG = "ScreenAutoBrightness";

    private TextView mSensorLevel;
    private ListView mConfigList;

    private SensorManager mSensorManager;
    private Sensor mLightSensor;

    private static class SettingRow {
        int lux;
        int backlight;
        public SettingRow(int lux, int backlight) {
            this.lux = lux;
            this.backlight = backlight;
        }
    };

    private SettingRowAdapter mAdapter;
    private int mMinLevel;
    private boolean mIsDefault;

    private AlertDialog mSetupDialog;
    private AlertDialog mSplitDialog;

    private boolean mAllowLuxEdit;
    private String mSettingsValues;
    private boolean mScreenMode;
    private IPowerManager mPowerService;
    private int mCurrentButtonBrightness;
    private boolean mAllowLevelChange = false;

    private SensorEventListener mLightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final int lux = Math.round(event.values[0]);
            mSensorLevel.setText(getContext().getString(R.string.light_sensor_current_value, lux));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public AutoBrightnessDialog(Context context, boolean screenMode) {
        super(context);

        mScreenMode = screenMode;
        if (mScreenMode){
            mAllowLuxEdit = true;
            mSettingsValues = Settings.System.AUTO_BRIGHTNESS_SCREEN_BACKLIGHT;
        } else {
            mAllowLuxEdit = false;
            mSettingsValues = Settings.System.AUTO_BRIGHTNESS_BUTTON_BACKLIGHT;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = getContext();
        View view = getLayoutInflater().inflate(R.layout.dialog_auto_brightness_levels, null);
        setView(view);
        setTitle(R.string.auto_brightness_dialog_title);
        setCancelable(true);

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        setButton(DialogInterface.BUTTON_NEUTRAL,
                context.getString(R.string.auto_brightness_reset_button), this);
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (mScreenMode){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mMinLevel = pm.getMinimumAbsoluteScreenBrightness();
        } else {
            mMinLevel = 0;
            mPowerService = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            // get the current level to reset to
            try {
                mCurrentButtonBrightness = mPowerService.getCurrentButtonBrightnessValue();
            } catch(Exception e){
            }
        }

        mSensorLevel = (TextView) view.findViewById(R.id.light_sensor_value);

        mConfigList = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new SettingRowAdapter(context, new ArrayList<SettingRow>());
        mConfigList.setAdapter(mAdapter);

        if (mAllowLevelChange){
            registerForContextMenu(mConfigList);
        } else {
            mConfigList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
                    showSetup(position, null);
                    return true;
                }
            });
        }
    }

    @Override
    protected void onStart() {
        updateSettings(false);

        super.onStart();

        mSensorManager.registerListener(mLightSensorListener,
                mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Button neutralButton = getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmation();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mLightSensorListener, mLightSensor);
        if (mSetupDialog != null) {
            mSetupDialog.dismiss();
        }
        if (mSplitDialog != null) {
            mSplitDialog.dismiss();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        menu.setHeaderTitle(R.string.auto_brightness_level_options);

        menu.add(Menu.NONE, Menu.FIRST, 0, R.string.auto_brightness_menu_edit)
                .setEnabled(!mAdapter.isLastItem(info.position));

        if (mAllowLuxEdit && mAllowLevelChange){
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, R.string.auto_brightness_menu_split)
                .setEnabled(mAdapter.canSplitRow(info.position));
            menu.add(Menu.NONE, Menu.FIRST + 2, 2, R.string.auto_brightness_menu_remove)
                .setEnabled(mAdapter.getCount() > 1);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId() - Menu.FIRST) {
            case 0:
                showSetup(position, null);
                return true;
            case 1:
                showSplitDialog(position, null);
                break;
            case 2:
                mAdapter.removeRow(position);
                return true;
        }

        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            putSettings();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            cancel();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dialog == mSetupDialog) {
            mSetupDialog = null;
        } else if (dialog == mSplitDialog) {
            mSplitDialog = null;
        }
    }

    private void updateSettings(boolean forceDefault) {
        int[] lux = null, values = null;

        if (!forceDefault) {
            lux = fetchItems(Settings.System.AUTO_BRIGHTNESS_LUX);
            values = fetchItems(mSettingsValues);
        }

        if (lux != null && values != null && lux.length != values.length - 1) {
            Log.e(TAG, "Found invalid backlight settings, ignoring");
            values = null;
        }

        if (lux == null || values == null) {
            final Resources res = getContext().getResources();
            lux = res.getIntArray(com.android.internal.R.array.config_autoBrightnessLevels);
            if (mScreenMode){
                values = res.getIntArray(com.android.internal.R.array.config_autoBrightnessLcdBacklightValues);
            } else {
                values = res.getIntArray(com.android.internal.R.array.config_autoBrightnessButtonBacklightValues);
            }
            mIsDefault = true;
        } else {
            mIsDefault = false;
        }

        mAdapter.initFromSettings(lux, values);
    }

    private void showSetup(int position, Bundle state) {
        if (mSetupDialog != null && mSetupDialog.isShowing()) {
            return;
        }

        mSetupDialog = new RowSetupDialog(getContext(), position);
        mSetupDialog.setOnDismissListener(this);
        mSetupDialog.show();
    }

    private void showSplitDialog(final int position, Bundle state) {
        if (mSplitDialog != null && mSplitDialog.isShowing()) {
            return;
        }

        mSplitDialog = new RowSplitDialog(getContext(), position);
        mSplitDialog.setOnDismissListener(this);
        mSplitDialog.show();
    }

    private void showResetConfirmation() {
        final AlertDialog d = new AlertDialog.Builder(getContext())
            .setTitle(R.string.auto_brightness_reset_dialog_title)
            .setCancelable(true)
            .setMessage(R.string.auto_brightness_reset_confirmation)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    updateSettings(true);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .create();

        d.show();
    }

    private void putSettings() {
        int[] lux = null, values = null;

        if (!mIsDefault) {
            lux = mAdapter.getLuxValues();
            values = mAdapter.getBacklightValues();
        }

        if (mAllowLuxEdit){
            putItems(Settings.System.AUTO_BRIGHTNESS_LUX, lux);
        }
        putItems(mSettingsValues, values);
    }

    private int[] fetchItems(String setting) {
        String value = Settings.System.getString(getContext().getContentResolver(), setting);
        if (value != null) {
            String[] values = value.split(",");
            if (values != null && values.length != 0) {
                int[] result = new int[values.length];
                int i;

                for (i = 0; i < values.length; i++) {
                    try {
                        result[i] = Integer.valueOf(values[i]);
                    } catch (NumberFormatException e) {
                        break;
                    }
                }
                if (i == values.length) {
                    return result;
                }
            }
        }

        return null;
    }

    private void putItems(String setting, int[] values) {
        String value = null;
        if (values != null) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(values[i]);
            }
            value = builder.toString();
        }
        Settings.System.putString(getContext().getContentResolver(), setting, value);
    }

    private class RowSetupDialog extends AlertDialog implements DialogInterface.OnClickListener {

        private EditText mLuxInput;
        private SeekBar mBacklightBar;
        private EditText mBacklightInput;
        private TextView mLuxText;

        private int mPosition;

        public RowSetupDialog(Context context, int position) {
            super(context);
            mPosition = position;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            final View v = getLayoutInflater().inflate(R.layout.auto_brightness_level_setup, null);
            final Context context = getContext();

            mLuxInput = (EditText) v.findViewById(R.id.lux);
            mLuxText = (TextView) v.findViewById(R.id.lux_text);

            if (!mAllowLuxEdit){
                mLuxInput.setVisibility(View.GONE);
                mLuxText.setVisibility(View.GONE);
            }
            mBacklightBar = (SeekBar) v.findViewById(R.id.backlight);
            mBacklightInput = (EditText) v.findViewById(R.id.backlight_input);

            setTitle(R.string.auto_brightness_level_dialog_title);
            setCancelable(true);
            setView(v);

            mBacklightBar.setMax(brightnessToProgress(PowerManager.BRIGHTNESS_ON));
            initListeners();
            initFromPosition();

            setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), this);
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), this);

            super.onCreate(savedInstanceState);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                try {
                    int newLux = Integer.valueOf(mLuxInput.getText().toString());
                    int newBacklight = Integer.valueOf(mBacklightInput.getText().toString());
                    mAdapter.setValuesForRow(mPosition, newLux, newBacklight);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "NumberFormatException " + e);
                }
            }
        }

        private void initFromPosition() {
            final SettingRow row = mAdapter.getItem(mPosition);

            mLuxInput.setText(String.valueOf(row.lux));
            mBacklightBar.setProgress(brightnessToProgress(row.backlight));
            mBacklightInput.setText(String.valueOf(row.backlight));
        }

        private void initListeners() {
            mBacklightBar.setOnSeekBarChangeListener(new BrightnessSeekBarChangeListener() {
                @Override
                protected int getPosition(SeekBar seekBar) {
                    return mPosition;
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    super.onProgressChanged(seekBar, progress, fromUser);

                    int brightness = progressToBrightness(seekBar.getProgress());
                    mBacklightInput.setText(String.valueOf(brightness));
                }
            });

            mBacklightInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    boolean ok = false;
                    try {
                        int minValue = mPosition == 0
                                ? mMinLevel
                                : mAdapter.getItem(mPosition - 1).backlight;
                        int maxValue = mAdapter.isLastItem(mPosition)
                                ? PowerManager.BRIGHTNESS_ON
                                : mAdapter.getItem(mPosition + 1).backlight;
                        int newBrightness = Integer.valueOf(s.toString());

                        if (newBrightness >= minValue && newBrightness <= maxValue) {
                            ok = true;
                            mBacklightBar.setProgress(brightnessToProgress(newBrightness));
                        }
                    } catch (NumberFormatException e) {
                        //ignored, ok is false anyway
                    }

                    Button okButton = mSetupDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (okButton != null) {
                        okButton.setEnabled(ok);
                    }
                }
            });
        }
    }

    private class RowSplitDialog extends AlertDialog implements DialogInterface.OnClickListener {

        private TextView mLabel;
        private EditText mValue;

        private int mPosition;
        private int mMinLux, mMaxLux;

        public RowSplitDialog(Context context, int position) {
            super(context);
            mPosition = position;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            final View v = getLayoutInflater().inflate(R.layout.auto_brightness_split_dialog, null);
            final Context context = getContext();

            mLabel = (TextView) v.findViewById(R.id.split_label);
            mValue = (EditText) v.findViewById(R.id.split_position);

            setTitle(R.string.auto_brightness_split_dialog_title);
            setCancelable(true);
            setView(v);

            initValues();
            initListener();

            setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), this);
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), this);

            super.onCreate(savedInstanceState);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                int splitLux = Integer.valueOf(mValue.getText().toString());
                mAdapter.splitRow(mPosition, splitLux);
            }
        }

        private void initValues() {
            final SettingRow row = mAdapter.getItem(mPosition);

            mMinLux = row.lux + 1;
            mMaxLux = mAdapter.isLastItem(mPosition) ? 0 : mAdapter.getItem(mPosition + 1).lux - 1;

            mLabel.setText(getContext().getString(R.string.auto_brightness_split_lux_format,
                    mMinLux, mMaxLux));
            mValue.setText(String.valueOf(mMinLux));
        }

        private void initListener() {
            mValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    boolean ok = false;
                    try {
                        int newLux = Integer.valueOf(s.toString());
                        ok = newLux >= mMinLux && newLux <= mMaxLux;
                    } catch (NumberFormatException e) {
                        //ignored, ok is false anyway
                    }
                    Button okButton = getButton(DialogInterface.BUTTON_POSITIVE);
                    if (okButton != null) {
                        okButton.setEnabled(ok);
                    }
                }
            });
        }
    }

    private class SettingRowAdapter extends ArrayAdapter<SettingRow> {
        public SettingRowAdapter(Context context, ArrayList<SettingRow> rows) {
            super(context, 0, rows);
            setNotifyOnChange(false);
        }

        private boolean isLastItem(int position) {
            return position == getCount() - 1;
        }

        public boolean canSplitRow(int position) {
            if (isLastItem(position)) {
                return false;
            }

            SettingRow row = getItem(position);
            SettingRow next = getItem(position + 1);
            return next.lux > (row.lux + 1);
        }

        public void initFromSettings(int[] lux, int[] values) {
            ArrayList<SettingRow> settings = new ArrayList<SettingRow>(values.length);
            for (int i = 0; i < lux.length; i++) {
                settings.add(new SettingRow(i == 0 ? 0 : lux[i - 1], values[i]));
            }
            settings.add(new SettingRow(lux[lux.length - 1], values[values.length - 1]));

            clear();
            addAll(settings);
            notifyDataSetChanged();
        }

        public int[] getLuxValues() {
            int count = getCount();
            int[] lux = new int[count - 1];

            for (int i = 1; i < count; i++) {
                lux[i - 1] = getItem(i).lux;
            }

            return lux;
        }

        public int[] getBacklightValues() {
            int count = getCount();
            int[] values = new int[count];

            for (int i = 0; i < count; i++) {
                values[i] = getItem(i).backlight;
            }
            return values;
        }

        public void splitRow(int position, int splitLux) {
            if (!canSplitRow(position)) {
                return;
            }

            ArrayList<SettingRow> rows = new ArrayList<SettingRow>();
            for (int i = 0; i <= position; i++) {
                rows.add(getItem(i));
            }

            SettingRow lastRow = getItem(position);
            rows.add(new SettingRow(splitLux, lastRow.backlight));

            for (int i = position + 1; i < getCount(); i++) {
                rows.add(getItem(i));
            }

            clear();
            addAll(rows);
            sanitizeValuesAndNotify();
        }

        public void removeRow(int position) {
            if (getCount() <= 1) {
                return;
            }

            remove(getItem(position));
            sanitizeValuesAndNotify();
        }

        public void setValuesForRow(final int position, int newLux, int newBacklight) {
            final SettingRow row = getItem(position);
            boolean changed = false;

            if (isLastItem(position)) {
                return;
            }

            if (row.lux != newLux){
                row.lux = newLux;
                changed = true;
            }
            if (row.backlight != newBacklight){
                row.backlight = newBacklight;
                changed = true;
            }

            if (changed){
                sanitizeValuesAndNotify();
            }
        }

        public void sanitizeValuesAndNotify() {
            final int count = getCount();

            getItem(0).lux = 0;
            for (int i = 1; i < count; i++) {
                SettingRow lastRow = getItem(i - 1);
                SettingRow thisRow = getItem(i);

                thisRow.lux = Math.max(lastRow.lux + 1, thisRow.lux);
                thisRow.backlight = Math.max(lastRow.backlight, thisRow.backlight);
            }

            mIsDefault = false;
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.auto_brightness_list_item, parent, false);
                holder = new Holder();
                holder.lux = (TextView) convertView.findViewById(R.id.lux);
                holder.backlight = (SeekBar) convertView.findViewById(R.id.backlight);
                holder.backlightValue = (TextView) convertView.findViewById(R.id.backlight_value);
                convertView.setTag(holder);

                holder.backlight.setMax(brightnessToProgress(PowerManager.BRIGHTNESS_ON));
                holder.backlight.setOnSeekBarChangeListener(new BrightnessSeekBarChangeListener() {
                    @Override
                    protected int getPosition(SeekBar seekBar) {
                        return (Integer) seekBar.getTag();
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        super.onProgressChanged(seekBar, progress, fromUser);

                        if (fromUser) {
                            int pos = getPosition(seekBar);
                            progress = seekBar.getProgress();
                            getItem(pos).backlight = progressToBrightness(progress);
                            mIsDefault = false;
                        }

                        holder.updateBacklight();
                    }
                });
            } else {
                holder = (Holder) convertView.getTag();
            }

            SettingRow row = (SettingRow) getItem(position);

            final int resId = isLastItem(position)
                    ? R.string.auto_brightness_last_level_format
                    : R.string.auto_brightness_level_format;

            holder.lux.setText(getContext().getString(resId, row.lux));

            holder.backlight.setTag(position);
            holder.backlight.setProgress(brightnessToProgress(row.backlight));
            holder.updateBacklight();

            return convertView;
        }

        private class Holder {
            TextView lux;
            SeekBar backlight;
            TextView backlightValue;

            public void updateBacklight() {
                backlightValue.setText(String.valueOf(progressToBrightness(backlight.getProgress())));
            }
        };
    };

    private int brightnessToProgress(int brightness) {
        brightness -= mMinLevel;
        return brightness * 100;
    }

    private int progressToBrightness(int progress) {
        int brightness = progress / 100;
        return brightness + mMinLevel;
    }

    private abstract class BrightnessSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean mIsDragging = false;

        private void updateBrightness(float brightness) {
            if (mScreenMode){
                final Window window = getWindow();
                final WindowManager.LayoutParams params = window.getAttributes();
                params.screenBrightness = brightness;
                window.setAttributes(params);
            } else {
                try {
                    mPowerService.setButtonBrightness((int)brightness);
                } catch(Exception e){
                }
            }
        }

        protected abstract int getPosition(SeekBar seekBar);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int pos = getPosition(seekBar);
            if (fromUser) {
                int minValue = pos == 0
                        ? 0
                        : brightnessToProgress(mAdapter.getItem(pos - 1).backlight);
                int maxValue = mAdapter.isLastItem(pos)
                        ? seekBar.getMax()
                        : brightnessToProgress(mAdapter.getItem(pos + 1).backlight);

                if (progress < minValue) {
                    seekBar.setProgress(minValue);
                    progress = minValue;
                } else if (progress > maxValue) {
                    seekBar.setProgress(maxValue);
                    progress = maxValue;
                }
            }

            if (mIsDragging) {
                float brightness = progressToBrightness(progress);
                if (mScreenMode){
                    updateBrightness(brightness / PowerManager.BRIGHTNESS_ON);
                } else {
                    updateBrightness(brightness);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            float brightness = progressToBrightness(seekBar.getProgress());
            if (mScreenMode){
                updateBrightness(brightness / PowerManager.BRIGHTNESS_ON);
            } else {
                updateBrightness(brightness);
            }

            mIsDragging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mScreenMode){
                updateBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
            } else {
                updateBrightness(mCurrentButtonBrightness);
            }

            mIsDragging = false;
        }
    };
}

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

package com.android.settings.du.batterylight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;

public class BatteryLightPreference extends DialogPreference {

    private static String TAG = "BatteryLightPreference";
    public static final int DEFAULT_COLOR = 0xFFFFFF; //White

    private ImageView mLightColorView;
    private Resources mResources;
    private int mColorValue;

    /**
     * @param context
     * @param attrs
     */
    public BatteryLightPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColorValue = DEFAULT_COLOR;
        init();
    }

    public BatteryLightPreference(Context context, int color) {
        super(context, null);
        mColorValue = color;
        init();
    }

    private void init() {
        setLayoutResource(R.layout.preference_battery_light);
        mResources = getContext().getResources();
    }

    public void setColor(int color) {
        mColorValue = color;
        updatePreferenceViews();
    }

    public int getColor() {
        return mColorValue;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mLightColorView = (ImageView) view.findViewById(R.id.light_color);

        updatePreferenceViews();
    }

    private void updatePreferenceViews() {
        final int width = (int) mResources.getDimension(R.dimen.device_memory_usage_button_width);
        final int height = (int) mResources.getDimension(R.dimen.device_memory_usage_button_height);

        if (mLightColorView != null) {
            mLightColorView.setEnabled(true);
            mLightColorView.setImageDrawable(createRectShape(width, height, 0xFF000000 + mColorValue));
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
    }

    @Override
    protected Dialog createDialog() {
        final BatteryLightDialog d = new BatteryLightDialog(getContext(),
                0xFF000000 + mColorValue);
        d.setAlphaSliderVisible(false);

        d.setButton(AlertDialog.BUTTON_POSITIVE, mResources.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mColorValue =  d.getColor() - 0xFF000000; // strip alpha, led does not support it
                updatePreferenceViews();
                callChangeListener(this);
            }
        });
        d.setButton(AlertDialog.BUTTON_NEGATIVE, mResources.getString(R.string.cancel),
                (DialogInterface.OnClickListener) null);

        return d;
    }

    private static ShapeDrawable createRectShape(int width, int height, int color) {
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.setIntrinsicHeight(height);
        shape.setIntrinsicWidth(width);
        shape.getPaint().setColor(color);
        return shape;
    }
}

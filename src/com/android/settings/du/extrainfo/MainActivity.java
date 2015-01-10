/*
 * Copyright (C) 2014 The Dirty Unicorns project
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

package com.android.settings.du.extrainfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;

public class MainActivity extends Activity {
    private LinearLayout mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extrainfo_layout);
        mLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
        loadInfo();
    }

    private void addItem(String name, String value) { addItem(name, value, false); }
    private void addItem(String name, String value, boolean ucWords) {
        if (value != null && value.length() > 0) {
            mLayout.addView(new KeyTextView(this, name));
            if (ucWords) value = ExtraInfoLib.ucWords(value);
            mLayout.addView(new ValueTextView(this, value));
        }
    }

    private static class SeparatorTextView extends TextView {
        public SeparatorTextView(Context context) {
            super(context);
            setBackgroundColor(0xFFFFFFFF);
        }
    }

   private static class CategoryTextView extends TextView {
        public CategoryTextView(Context context, String text) {
            super(context);
            int indent = ExtraInfoLib.dpToPx(context, 5);
            int pad = ExtraInfoLib.dpToPx(context, 2);
            setTextAppearance(context, android.R.style.TextAppearance_Medium);
            setBackgroundColor(0xff263238);
            setTextColor(0xFFFFFFFF);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(indent, pad, pad, pad);
            setText(text);
        }
    }

    private static class KeyTextView extends TextView {
        public KeyTextView(Context context, String text) {
            super(context);
            int indent = ExtraInfoLib.dpToPx(context, 5);
            int pad = ExtraInfoLib.dpToPx(context, 2);
            setTextAppearance(context, android.R.style.TextAppearance_Small);
            setPadding(indent, pad, pad, pad);
            setText(text);
        }
    }

    private static class ValueTextView extends TextView {
        public ValueTextView(Context context, String text) {
            super(context);
            int indent = ExtraInfoLib.dpToPx(context, 20);
            int pad = ExtraInfoLib.dpToPx(context, 2);
            setTextColor(0xff263238);
            setPadding(indent, pad, pad, pad);
            setText(text);
        }
    }

    private void loadInfo() {
        ExtraInfo di = new ExtraInfo(this);

        addCategory(getString(R.string.basicinfo));

        addItem(getString(R.string.manufacturer), di.getBuildManufacturer(), true);
        addItem(getString(R.string.hardwaretype), di.getPropHardware());
        addItem(getString(R.string.systempartition), di.getSystemSize() + " " + getString(R.string.bytes));
        addItem(getString(R.string.datapartition), di.getDataSize() + " " +getString(R.string.bytes));
        addItem(getString(R.string.ram), di.getProcMemTotal());
        addItem(getString(R.string.bootloader), di.getPropBootloader());

        addCategory(getString(R.string.cpuinfo));

        addItem(getString(R.string.chipset_processor), di.getProcCpuDescription());
        addItem(getString(R.string.features), di.getProcCpuFeatures());
        addItem(getString(R.string.bogomips), di.getProcCpuBogoMips());
        addItem(getString(R.string.abi), di.getBuildCpuAbi());
        addItem(getString(R.string.abitwo), di.getPropCpuAbi2());
        addItem(getString(R.string.architecture), di.getProcCpuArchitecture());
        addItem(getString(R.string.implementer), di.getProcCpuImplementer());
        addItem(getString(R.string.variant), di.getProcCpuVariant());
        addItem(getString(R.string.part), di.getProcCpuPart());
        addItem(getString(R.string.revision), di.getProcCpuRevision());

        addCategory(getString(R.string.displayinfo));

        addItem(getString(R.string.diagonal), di.getDisplayDiagonalInches() + " " +getString(R.string.inches));
        addItem(getString(R.string.width), di.getDisplayWidthInches() + " " +getString(R.string.inches));
        addItem(getString(R.string.height), di.getDisplayHeightInches() + " " +getString(R.string.inches));
        addItem(getString(R.string.width), di.getDisplayWidth() + " " +getString(R.string.pixels));
        addItem(getString(R.string.height), di.getDisplayHeight() + " " +getString(R.string.pixels));
        addItem(getString(R.string.density), di.getDisplayDensity());
        addItem(getString(R.string.dots_per_inch), di.getDisplayDpi());
        addItem(getString(R.string.actualdpi_x), di.getDisplayDpiX());
        addItem(getString(R.string.actualdpi_y), di.getDisplayDpiY());
        addItem(getString(R.string.refresh_rate), di.getDisplayRefreshRate());

        }

    @SuppressWarnings("deprecation")
        private void addCategory(String name) {
        mLayout.addView(new CategoryTextView(this, name),
                        new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        mLayout.addView(new SeparatorTextView(this),
                        new LayoutParams(LayoutParams.FILL_PARENT, ExtraInfoLib.dpToPx(this, 1)));
    }
}

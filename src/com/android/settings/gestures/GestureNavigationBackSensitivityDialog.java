/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.gestures;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

/**
 * Dialog to set the back gesture's sensitivity in Gesture navigation mode.
 */
public class GestureNavigationBackSensitivityDialog extends InstrumentedDialogFragment {

    private boolean mArrowSwitchChecked;

    private static final String TAG = "GestureNavigationBackSensitivityDialog";
    private static final String KEY_BACK_SENSITIVITY = "back_sensitivity";
    private static final String KEY_BACK_DEAD_Y_ZONE = "back_dead_y_zone";

    public static void show(SystemNavigationGestureSettings parent, int sensitivity, int backDeadYZoneMode) {
        if (!parent.isAdded()) {
            return;
        }

        final GestureNavigationBackSensitivityDialog dialog =
                new GestureNavigationBackSensitivityDialog();
        final Bundle bundle = new Bundle();
        bundle.putInt(KEY_BACK_SENSITIVITY, sensitivity);
        bundle.putInt(KEY_BACK_DEAD_Y_ZONE, backDeadYZoneMode);
        dialog.setArguments(bundle);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURE_NAV_BACK_SENSITIVITY_DLG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_back_gesture_sensitivity, null);
        final SeekBar sensitivitySeekBar = view.findViewById(R.id.back_sensitivity_seekbar);
        sensitivitySeekBar.setProgress(getArguments().getInt(KEY_BACK_SENSITIVITY));
        final Switch arrowSwitch = view.findViewById(R.id.back_arrow_gesture_switch);
        mArrowSwitchChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.HIDE_BACK_ARROW_GESTURE, 0) == 1;
        arrowSwitch.setChecked(mArrowSwitchChecked);
        arrowSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrowSwitchChecked = arrowSwitch.isChecked() ? true : false;
            }
        });
        final SeekBar backDeadzoneSeekbar = view.findViewById(R.id.back_deadzone_seekbar);
        backDeadzoneSeekbar.setProgress(getArguments().getInt(KEY_BACK_DEAD_Y_ZONE));
        return new AlertDialog.Builder(getContext(), R.style.GestureDialogTheme)
                .setTitle(R.string.back_gesture_settings_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    int sensitivity = sensitivitySeekBar.getProgress();
                    getArguments().putInt(KEY_BACK_SENSITIVITY, sensitivity);
                    SystemNavigationGestureSettings.setBackSensitivity(getActivity(),
                            getOverlayManager(), sensitivity);
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                            Settings.Secure.HIDE_BACK_ARROW_GESTURE, mArrowSwitchChecked ? 1 : 0);
                    int backDeadYZoneMode = backDeadzoneSeekbar.getProgress();
                    getArguments().putInt(KEY_BACK_DEAD_Y_ZONE, backDeadYZoneMode);
                    SystemNavigationGestureSettings.setBackDeadYZone(getActivity(),
                            backDeadYZoneMode);
                })
                .create();
    }

    private IOverlayManager getOverlayManager() {
        return IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
    }
}

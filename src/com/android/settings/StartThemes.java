package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StartThemes extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startThemes();
        finish();
    }

    private void startThemes(){
        Intent themesExtrasIntent = new Intent();
        themesExtrasIntent.setClassName(
                "com.dirtyunicorns.themes", "com.dirtyunicorns.themes.MainActivity");
        startActivity(themesExtrasIntent);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DIRTYTWEAKS;
    }
}

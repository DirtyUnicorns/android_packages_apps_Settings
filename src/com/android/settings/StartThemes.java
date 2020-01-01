package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;

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

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
}

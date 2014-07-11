package com.android.settings.du.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.settings.R;

public class BootReceiver extends BroadcastReceiver {
	static final String TAG = "WeatherReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i(TAG, "CFX Weather service starting");
			context.startService(new Intent(context, WeatherService.class));
		}
	}
}

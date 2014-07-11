package com.android.settings.du.weather;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Set;

import com.android.settings.du.weather.ResourceMaps.ResInfo;
import org.codefirex.utils.WeatherAdapter;
import org.codefirex.utils.WeatherInfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;

import com.android.settings.R;

public class WeatherService extends Service {
	private static final String TAG = "WeatherService";

	static final String LOCATION_ACQUIRED_ACTION = "location_acquired";
	static final String LOCATION_UNAVAILABLE_ACTION = "location_unavailable";
	static final String LOCATION_REFRESHING = "location_refreshing";
	// coordinates sent to HttpService
	static final String LOCATION_EXTRA = "location_extra";

	// handler codes
	static final int INTERVAL_CHANGED = 1001;
	static final int LOCATION_MODE_CHANGED = 1002;
	static final int SCALE_CHANGED = 1003;
	static final int REFRESH_NOW = 1004;
	static final int PAUSE_SERVICE = 1005;
	static final int RESUME_SERVICE = 1006;

	static final String ALARM_TICKED = "cfx_weather_alarm_ticked";

	private LocationManager mLocationManager;
	private IntentFilter mFilter;
	private PendingIntent mAlarmPending;
	private WeatherNotification mNotification;
	private boolean mEnabled = false;
	private int mFailCount = 0;

	private Binder mBinder = new WeatherBinder();

	private static final long MINUTE_MULTIPLE = (1000 * 60);

	private LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			sendPosition(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}
	};

	class CacheIcon extends AsyncTask<Void, Void, Void> {

		private Context mContext;

		public CacheIcon(Context ctx) {
			mContext = ctx;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Set<Integer> set = ResourceMaps.weather_map.keySet();
			for (Integer i : set) {
				ResInfo info = ResourceMaps.weather_map.get(i);
				String filename = info.iconName + ".png";
				File f = new File(mContext.getCacheDir() + "/" + filename);
				InputStream is;
				try {
					if (!f.exists()) {
						is = mContext.getAssets().open(filename);
						int size = is.available();
						byte[] buffer = new byte[size];
						is.read(buffer);
						is.close();
						FileOutputStream fos = new FileOutputStream(f);
						fos.write(buffer);
						fos.close();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		}

	    @Override
	    protected void onPostExecute(Void voidz) {
			sendAdapterBroadcast(mEnabled ? WeatherAdapter.STATE_ON : WeatherAdapter.STATE_OFF);	    	
	    }
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ALARM_TICKED)) {
				resetLocationListener();
			}
		}
	};

	public class WeatherBinder extends Binder {
		WeatherService getService() {
			return WeatherService.this;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INTERVAL_CHANGED:
				resetAlarm();
				resetLocationListener();
				break;
			case LOCATION_MODE_CHANGED:
				resetLocationListener();
				break;
			case SCALE_CHANGED:
				sendAdapterBroadcast(WeatherAdapter.STATE_SCALE);
				break;
			case REFRESH_NOW:
				resetLocationListener();
				break;
			case PAUSE_SERVICE:
				mEnabled = false;
				sendAdapterBroadcast(WeatherAdapter.STATE_OFF);
				removeLocationListener();
				unregisterReceiver(mReceiver);
				cancelAlarm();
				break;
			case RESUME_SERVICE:
				mEnabled = true;
				sendAdapterBroadcast(WeatherAdapter.STATE_ON);
				registerReceiver(mReceiver, mFilter);
				resetAlarm();
				resetLocationListener();
				break;
			}
		}
	};

	private ResultReceiver mHttpReciever = new ResultReceiver(new Handler()) {
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			int code = resultData.getInt(HttpService.RESULT_CODE_TAG);
			if (HttpService.RESULT_SUCCEED == code) {
				sendAdapterBroadcast(WeatherAdapter.STATE_UPDATED);
			} else if (HttpService.RESULT_FAIL == code) {
				// we we're safe starting http service but something happened 
				// during the http request. initiate fail alarm
				if (mFailCount == 0) {
					startFailAlarm();
					mFailCount++;
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mEnabled = WeatherPrefs.getEnabled(this);
		mNotification = new WeatherNotification(this);

		new CacheIcon(this).execute();

		// create pending intent to fire when alarm is triggered
		mAlarmPending = PendingIntent.getBroadcast(this, 0, new Intent(
				ALARM_TICKED), PendingIntent.FLAG_CANCEL_CURRENT);

		mFilter = new IntentFilter();
		mFilter.addAction(ALARM_TICKED);

		if (mEnabled) {
			registerReceiver(mReceiver, mFilter);
			resetAlarm();
			resetLocationListener();
		}

	}

	private void resetLocationListener() {
		removeLocationListener();
		mLocationManager.requestLocationUpdates(getBestLocationMode(), 0, 0,
				mLocationListener);
	}

	private String getBestLocationMode() {
		String pref = WeatherPrefs.getLocationMode(this);
		if (isPreferedLocationAvailable(pref)) {
			return pref;
		} else {
			return LocationManager.PASSIVE_PROVIDER;
		}
	}

	private boolean isPreferedLocationAvailable(String pref) {
		boolean preferedAvailable = false;
		try {
			preferedAvailable = mLocationManager.isProviderEnabled(pref);
		} catch (Exception e) {
		}
		return preferedAvailable;
	}

	private boolean isSafeToUpdate() {
		return hasNetwork() && !isLocDisabled();
	}

	private boolean hasNetwork() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private void removeLocationListener() {
		sendAdapterBroadcast(WeatherAdapter.STATE_REFRESHING);
		mLocationManager.removeUpdates(mLocationListener);
	}

	private void resetAlarm() {
		long interval = Long.valueOf(WeatherPrefs.getInterval(this))
				* MINUTE_MULTIPLE;
		long START_TIME = Calendar.getInstance().getTimeInMillis() + interval;
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(mAlarmPending);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, START_TIME, interval,
				mAlarmPending);
	}

	private void startFailAlarm() {
		// we'll do 3 attempts at 30 second intervals if we are failing
		// after 3, try again at next primary interval
		long interval = MINUTE_MULTIPLE / 2;
		long START_TIME = Calendar.getInstance().getTimeInMillis() + interval;
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(mAlarmPending);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, START_TIME, interval,
				mAlarmPending);		
	}

	private void cancelAlarm() {
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(mAlarmPending);
	}

	private boolean isGpsLocationAvailable() {
		boolean hasGps = false;
		try {
			hasGps = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception e) {
		}
		return hasGps;
	}	

	private boolean isNetworkLocationAvailable() {
		boolean hasNetwork = false;
		try {
			hasNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception e) {
		}
		return hasNetwork;
	}

	private boolean isLocDisabled() {
		return !isGpsLocationAvailable()
				&& !isNetworkLocationAvailable();
	}

	// send coordinates to HttpService
	private void sendPosition(Location location) {
		if (!isSafeToUpdate()) {
			if (mFailCount == 0) {
				startFailAlarm();
				mFailCount++;
				return;
			} else if (mFailCount > 3) {
				// just try again at normal intervals
				mFailCount = 0;
				resetAlarm();
				return;
			}
			mFailCount++;
			return;
		}
		if (mFailCount > 0) {
			mFailCount = 0;
			resetAlarm();
		}
		Intent bestPosition = new Intent(this, HttpService.class);
		bestPosition.setAction(LOCATION_ACQUIRED_ACTION);
		bestPosition.putExtra(LOCATION_EXTRA, location);
		bestPosition.putExtra(HttpService.RESULT_TAG, mHttpReciever);
		startService(bestPosition);
		removeLocationListener();
	}

	private void sendAdapterBroadcast(int state) {
		Intent intent = new Intent();
		intent.setAction(WeatherAdapter.WEATHER_ACTION);
		intent.putExtra(WeatherAdapter.WEATHER_SERVICE_STATE, state);
		if (state == WeatherAdapter.STATE_SCALE) {
			intent.putExtra(WeatherAdapter.SCALE_TYPE, WeatherPrefs.getDegreeType(this));
		}
		sendStickyBroadcast(intent);
	}

	void sendMessage(Message m) {
		mHandler.sendMessage(m);
	}

	Message getMessage() {
		return mHandler.obtainMessage();
	}

	@Override
	public void onDestroy() {
		removeLocationListener();
		unregisterReceiver(mReceiver);
		cancelAlarm();
		mNotification.unregister();
	}
}

package com.android.settings.du.weather;

import org.codefirex.utils.WeatherAdapter;
import org.codefirex.utils.WeatherAdapter.WeatherListener;
import org.codefirex.utils.WeatherInfo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.android.settings.R;

public class WeatherNotification implements WeatherListener {
	Context mContext;
	WeatherAdapter mAdapter;
	WeatherInfo mInfo;
	RemoteViews mForecastView;
	SettingsObserver mObserver;
	Handler mHandler;
	boolean mViewEnabled = false;
	boolean mServiceEnabled = false;

	private static final boolean ADD_SYMBOL = true;

	public WeatherNotification(Context context) {
		mContext = context;
		mHandler = new Handler();
		mObserver = new SettingsObserver(mHandler);
		mObserver.observe();
		mAdapter = new WeatherAdapter(mContext, this);
		mAdapter.startUpdates();
	}

	void unregister() {
		mAdapter.stopUpdates();
	}

	private class SettingsObserver extends ContentObserver {
		public SettingsObserver(Handler handler) {
			super(handler);
		}

		void observe() {
			ContentResolver resolver = mContext.getContentResolver();
			resolver.registerContentObserver(
					Settings.System.getUriFor("cfx_weather_notification"),
					false, this);
			onChange(true);
		}

		@Override
		public void onChange(boolean selfChange) {
			ContentResolver resolver = mContext.getContentResolver();
			mViewEnabled = Settings.System.getBoolean(resolver,
					"cfx_weather_notification", false);
			updateNotification();
		}
	}

	@Override
	public void onServiceStateChanged(int state) {
		switch (state) {
		case WeatherAdapter.STATE_ON:
			mServiceEnabled = true;
			mObserver.onChange(true);
			break;
		case WeatherAdapter.STATE_OFF:
			mServiceEnabled = false;
			mObserver.onChange(true);
			break;
		case WeatherAdapter.STATE_SCALE:
			mInfo = mAdapter.getLastKnownWeather();
			updateNotification();
			break;
		case WeatherAdapter.STATE_UPDATED:
			mInfo = mAdapter.getLatestWeather();
			updateNotification();
			break;
		case WeatherAdapter.STATE_REFRESHING:
			break;
		}
	}

	void updateNotification() {
		if (mServiceEnabled && mViewEnabled) {
			buildNotification();
		} else {
			cancelNotification();
		}
	}

	void cancelNotification() {
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);
	}

	void buildNotification() {
		int icon = R.drawable.app_icon;
		long when = System.currentTimeMillis();
		String title = "CFXWeather";

		if (mInfo == null) {
			mInfo = mAdapter.getLatestWeather();
		}

		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(mContext);

		RemoteViews contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification_view);

		contentView
				.setImageViewBitmap(R.id.n_current_weather, WeatherInfo
						.getBitmapFromProvider(mContext, mInfo.mCurrentCode));
		contentView.setTextViewText(R.id.n_current_temp, addSymbol(mInfo.getCurrentTemp()));

		contentView.setImageViewBitmap(R.id.n_day_one_weather, WeatherInfo
				.getBitmapFromProvider(mContext, mInfo.getForecastInfo1()
						.getForecastCode()));
		contentView.setTextViewText(R.id.n_day_one_day, mInfo
				.getForecastInfo1().getForecastDay());
		contentView.setTextViewText(R.id.n_day_one_high_text, addSymbol(mInfo
				.getForecastInfo1().getForecastHighTemp()));
		contentView.setTextViewText(R.id.n_day_one_low_text, addSymbol(mInfo
				.getForecastInfo1().getForecastLowTemp()));

		contentView.setImageViewBitmap(R.id.n_day_two_weather, WeatherInfo
				.getBitmapFromProvider(mContext, mInfo.getForecastInfo2()
						.getForecastCode()));
		contentView.setTextViewText(R.id.n_day_two_day, mInfo
				.getForecastInfo2().getForecastDay());
		contentView.setTextViewText(R.id.n_day_two_high_text, addSymbol(mInfo
				.getForecastInfo2().getForecastHighTemp()));
		contentView.setTextViewText(R.id.n_day_two_low_text, addSymbol(mInfo
				.getForecastInfo2().getForecastLowTemp()));

		contentView.setImageViewBitmap(R.id.n_day_three_weather, WeatherInfo
				.getBitmapFromProvider(mContext, mInfo.getForecastInfo3()
						.getForecastCode()));
		contentView.setTextViewText(R.id.n_day_three_day, mInfo
				.getForecastInfo3().getForecastDay());
		contentView.setTextViewText(R.id.n_day_three_high_text, addSymbol(mInfo
				.getForecastInfo3().getForecastHighTemp()));
		contentView.setTextViewText(R.id.n_day_three_low_text, addSymbol(mInfo
				.getForecastInfo3().getForecastLowTemp()));

		contentView.setImageViewBitmap(R.id.n_day_four_weather, WeatherInfo
				.getBitmapFromProvider(mContext, mInfo.getForecastInfo4()
						.getForecastCode()));
		contentView.setTextViewText(R.id.n_day_four_day, mInfo
				.getForecastInfo4().getForecastDay());
		contentView.setTextViewText(R.id.n_day_four_high_text, addSymbol(mInfo
				.getForecastInfo4().getForecastHighTemp()));
		contentView.setTextViewText(R.id.n_day_four_low_text, addSymbol(mInfo
				.getForecastInfo4().getForecastLowTemp()));

		mForecastView = contentView;

		Intent notificationIntent = new Intent(mContext, WeatherActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);

		builder.setSmallIcon(icon);
		builder.setOngoing(true);
		builder.setContentIntent(contentIntent);
		builder.setContent(mForecastView);

		mNotificationManager.notify(1, builder.build());
	}

	String addSymbol(String s) {
		if (ADD_SYMBOL)
			return WeatherInfo.addSymbol(s);
		return s;
	}
}

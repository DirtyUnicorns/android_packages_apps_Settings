package com.android.settings.du.weather;

import org.codefirex.utils.WeatherInfo;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.android.settings.R;

public class HttpService extends IntentService {
	private static final String TAG = "WeatherService";
	static final String RESULT_TAG = "weather_service_result";
	static final String RESULT_CODE_TAG = "result_code_tag";
	static final int RESULT_CODE = 2001;
	static final int RESULT_SUCCEED = 2002;
	static final int RESULT_FAIL = 2003;

	private YahooWeatherUtils yahooWeatherUtils = YahooWeatherUtils
			.getInstance();

	public HttpService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (WeatherService.LOCATION_ACQUIRED_ACTION.equals(action)) {
			Location location = (Location) intent
					.getParcelableExtra(WeatherService.LOCATION_EXTRA);
			if (location != null) {
				int result = RESULT_FAIL;
				ResultReceiver rec = intent.getParcelableExtra(RESULT_TAG);
				String lat = String.valueOf(location.getLatitude());
				String lon = String.valueOf(location.getLongitude());
				try {
					WeatherInfo weatherInfo = yahooWeatherUtils
							.queryYahooWeather(this, lat, lon);
					weatherInfo.setCurrentScale(WeatherPrefs
							.getDegreeType(this));
					WeatherPrefs.setPrefsFromInfo(this, weatherInfo);
					result = RESULT_SUCCEED;
				} catch (Exception e) {
					result = RESULT_FAIL;
				} finally {
					if (rec != null) {
						Bundle b = new Bundle();
						b.putInt(RESULT_CODE_TAG, result);
						rec.send(RESULT_CODE, b);
					}
				}
			}
		}
	}
}

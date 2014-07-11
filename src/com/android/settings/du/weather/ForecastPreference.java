package com.android.settings.du.weather;

import com.android.settings.R;
import org.codefirex.utils.WeatherInfo;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastPreference extends Preference {
	public static final boolean DEBUG = false;

	TextView mCurrentCity;
	ImageView mCurrentWeatherIcon;
	TextView mCurrentWeatherText;
	TextView mCurrentTemp;
	TextView mCurrentTempHigh;
	TextView mCurrentTempLow;

	TextView mWindChill;
	TextView mWindSpeed;
	TextView mWindDirection;

	TextView mHumidity;
	TextView mPressure;
	TextView mVisibility;

	TextView mDayOne;
	ImageView mDayOneWeather;
	TextView mDayOneHigh;
	TextView mDayOneLow;
	TextView mDayTwo;
	ImageView mDayTwoWeather;
	TextView mDayTwoHigh;
	TextView mDayTwoLow;
	TextView mDayThree;
	ImageView mDayThreeWeather;
	TextView mDayThreeHigh;
	TextView mDayThreeLow;
	TextView mDayFour;
	ImageView mDayFourWeather;
	TextView mDayFourHigh;
	TextView mDayFourLow;

	Context mContext;
	WeatherInfo mWeatherInfo;

	public ForecastPreference(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ForecastPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ForecastPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setLayoutResource(R.layout.forecast_view);
	}

	public void setWeatherInfo(WeatherInfo weatherInfo) {
		mWeatherInfo = weatherInfo;
	}

	public void invalidate() {
		setSummary(getSummary() + " ");
	}

	@Override
	public void onBindView(View v) {
		super.onBindView(v);
		mCurrentWeatherIcon = (ImageView) v
				.findViewById(R.id.current_weather_icon);

		mCurrentWeatherText = (TextView) v
				.findViewById(R.id.current_weather_text);

		mCurrentCity = (TextView) v.findViewById(R.id.current_city);
		mCurrentTemp = (TextView) v.findViewById(R.id.current_temperature);
		mCurrentTempHigh = (TextView) v
				.findViewById(R.id.current_temperature_high);
		mCurrentTempLow = (TextView) v
				.findViewById(R.id.current_temperature_low);

		mDayOne = (TextView) v.findViewById(R.id.day_one_day);
		mDayOneWeather = (ImageView) v.findViewById(R.id.day_one_weather);
		mDayOneHigh = (TextView) v.findViewById(R.id.day_one_high_text);
		mDayOneLow = (TextView) v.findViewById(R.id.day_one_low_text);

		mDayTwo = (TextView) v.findViewById(R.id.day_two_day);
		mDayTwoWeather = (ImageView) v.findViewById(R.id.day_two_weather);
		mDayTwoHigh = (TextView) v.findViewById(R.id.day_two_high_text);
		mDayTwoLow = (TextView) v.findViewById(R.id.day_two_low_text);

		mDayThree = (TextView) v.findViewById(R.id.day_three_day);
		mDayThreeWeather = (ImageView) v.findViewById(R.id.day_three_weather);
		mDayThreeHigh = (TextView) v.findViewById(R.id.day_three_high_text);
		mDayThreeLow = (TextView) v.findViewById(R.id.day_three_low_text);

		mDayFour = (TextView) v.findViewById(R.id.day_four_day);
		mDayFourWeather = (ImageView) v.findViewById(R.id.day_four_weather);
		mDayFourHigh = (TextView) v.findViewById(R.id.day_four_high_text);
		mDayFourLow = (TextView) v.findViewById(R.id.day_four_low_text);

		if (mWeatherInfo != null)
			updateResources();
	}

	void updateResources() {
		mCurrentWeatherIcon.setImageDrawable(WeatherInfo.getIconFromProvider(
				mContext, mWeatherInfo.getCurrentCode()));
		mCurrentWeatherText.setText(mWeatherInfo.getCurrentText());
		mCurrentCity.setText(mWeatherInfo.getLocationCity());
		mCurrentTemp.setText(String.valueOf(WeatherInfo.addSymbol(mWeatherInfo.getCurrentTemp())));
		mCurrentTempHigh.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo1()
				.getForecastHighTemp())));
		mCurrentTempLow.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo1()
				.getForecastLowTemp())));

		mDayOne.setText(mWeatherInfo.getForecastInfo1().getForecastDay());
		mDayOneWeather.setImageDrawable(WeatherInfo.getIconFromProvider(
				mContext, mWeatherInfo.getForecastInfo1().getForecastCode()));
		mDayOneHigh.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo1()
				.getForecastHighTemp())));
		mDayOneLow.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo1()
				.getForecastLowTemp())));

		mDayTwo.setText(mWeatherInfo.getForecastInfo2().getForecastDay());
		mDayTwoWeather.setImageDrawable(WeatherInfo.getIconFromProvider(
				mContext, mWeatherInfo.getForecastInfo2().getForecastCode()));
		mDayTwoHigh.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo2()
				.getForecastHighTemp())));
		mDayTwoLow.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo2()
				.getForecastLowTemp())));

		mDayThree.setText(mWeatherInfo.getForecastInfo3().getForecastDay());
		mDayThreeWeather.setImageDrawable(WeatherInfo.getIconFromProvider(
				mContext, mWeatherInfo.getForecastInfo3().getForecastCode()));
		mDayThreeHigh.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo3()
				.getForecastHighTemp())));
		mDayThreeLow.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo3()
				.getForecastLowTemp())));

		mDayFour.setText(mWeatherInfo.getForecastInfo4().getForecastDay());
		mDayFourWeather.setImageDrawable(WeatherInfo.getIconFromProvider(
				mContext, mWeatherInfo.getForecastInfo4().getForecastCode()));
		mDayFourHigh.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo4()
				.getForecastHighTemp())));
		mDayFourLow.setText(WeatherInfo.addSymbol(String.valueOf(mWeatherInfo.getForecastInfo4()
				.getForecastLowTemp())));
	}
}

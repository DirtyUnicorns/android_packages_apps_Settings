package com.android.settings.du.weather;

import java.util.HashMap;
import java.util.Map;

import com.android.settings.R;

import android.content.Context;

public class ResourceMaps {
    static final Map<Integer, ResInfo> weather_map = new HashMap<Integer, ResInfo>();
    static final Map<Integer, DayInfo> day_map = new HashMap<Integer, DayInfo>();

	static {
		weather_map.put(0, new ResInfo(R.string.tornado, "tornado"));
		weather_map.put(1, new ResInfo(R.string.tropical_storm, "heavy_rain"));
		weather_map.put(2, new ResInfo(R.string.hurricane, "rain_tornado"));
		weather_map.put(3, new ResInfo(R.string.severe_thunderstorms, "rain_thunder"));
		weather_map.put(4, new ResInfo(R.string.thunderstorms, "rain_thunder"));
		weather_map.put(5, new ResInfo(R.string.mixed_rain_snow, "rain_snow"));
		weather_map.put(6, new ResInfo(R.string.mixed_rain_sleet, "ice"));
		weather_map.put(7, new ResInfo(R.string.mixed_snow_sleet, "ice_snow"));
		weather_map.put(8, new ResInfo(R.string.freezing_drizzle, "ice"));		
		weather_map.put(9, new ResInfo(R.string.drizzle, "rain"));
		weather_map.put(10, new ResInfo(R.string.freezing_rain, "ice"));
		weather_map.put(11, new ResInfo(R.string.showers, "heavy_rain"));
		weather_map.put(12, new ResInfo(R.string.showers, "heavy_rain"));
		weather_map.put(13, new ResInfo(R.string.snow_flurries, "snow"));
		weather_map.put(14, new ResInfo(R.string.light_snow_showers, "rain_snow"));
		weather_map.put(15, new ResInfo(R.string.blowing_snow, "heavysnow"));
		weather_map.put(16, new ResInfo(R.string.snow, "snow"));
		weather_map.put(17, new ResInfo(R.string.hail, "ice"));
		weather_map.put(18, new ResInfo(R.string.sleet, "ice_snow"));
		weather_map.put(19, new ResInfo(R.string.dust, "sunny"));
		weather_map.put(20, new ResInfo(R.string.foggy, "foggy"));
		weather_map.put(21, new ResInfo(R.string.haze, "heat"));
		weather_map.put(22, new ResInfo(R.string.smoky, "heat"));
		weather_map.put(23, new ResInfo(R.string.blustery, "sunny"));		
		weather_map.put(24, new ResInfo(R.string.windy, "partly_cloudy"));
		weather_map.put(25, new ResInfo(R.string.cold, "cold"));
		weather_map.put(26, new ResInfo(R.string.cloudy, "cloudy"));
		weather_map.put(27, new ResInfo(R.string.mostly_cloudy, "cloudy_night"));
		weather_map.put(28, new ResInfo(R.string.mostly_cloudy, "cloudy"));		
		weather_map.put(29, new ResInfo(R.string.partly_cloudy, "cloudy_night"));
		weather_map.put(30, new ResInfo(R.string.partly_cloudy, "partly_cloudy"));
		weather_map.put(31, new ResInfo(R.string.clear, "clear_night"));
		weather_map.put(32, new ResInfo(R.string.sunny, "sunny"));		
		weather_map.put(33, new ResInfo(R.string.fair, "clear_night"));
		weather_map.put(34, new ResInfo(R.string.fair, "sunny"));		
		weather_map.put(35, new ResInfo(R.string.mixed_rain_hail, "ice"));
		weather_map.put(36, new ResInfo(R.string.hot, "heat"));
		weather_map.put(37, new ResInfo(R.string.isolated_thunderstorms, "rain_thunder"));
		weather_map.put(38, new ResInfo(R.string.scattered_thunderstorms, "rain_thunder"));		
		weather_map.put(39, new ResInfo(R.string.scattered_thunderstorms, "rain_thunder"));
		weather_map.put(40, new ResInfo(R.string.scattered_showers, "rain_thunder"));
		weather_map.put(41, new ResInfo(R.string.heavy_snow, "night_rain_thunder"));
		weather_map.put(42, new ResInfo(R.string.scattered_snow_showers, "rain_snow"));		
		weather_map.put(43, new ResInfo(R.string.heavy_snow, "rain_snow"));
		weather_map.put(44, new ResInfo(R.string.partly_cloudy, "partly_cloudy"));
		weather_map.put(45, new ResInfo(R.string.thundershowers, "rain_thunder"));
		weather_map.put(46, new ResInfo(R.string.snow_showers, "rain_snow"));
		weather_map.put(47, new ResInfo(R.string.isolated_thundershowers, "rain_thunder"));
		weather_map.put(48, new ResInfo(R.string.not_available, "sunny"));

		day_map.put(0, new DayInfo(R.string.mon_short, R.string.mon_long));
		day_map.put(1, new DayInfo(R.string.tue_short, R.string.tue_long));
		day_map.put(2, new DayInfo(R.string.wed_short, R.string.wed_long));
		day_map.put(3, new DayInfo(R.string.thur_short, R.string.thur_long));
		day_map.put(4, new DayInfo(R.string.fri_short, R.string.fri_long));
		day_map.put(5, new DayInfo(R.string.sat_short, R.string.sat_long));
		day_map.put(6, new DayInfo(R.string.sun_short, R.string.sun_long));
	}

	static class ResInfo {
		int textRes;
		String iconName;

		public ResInfo(int textRes, String iconName) {
			this.textRes = textRes;
			this.iconName = iconName;
		}
	}

	static class DayInfo {
		int short_day;
		int long_day;

		public DayInfo(int short_day, int long_day) {
			this.short_day = short_day;
			this.long_day = long_day;
		}

		static int getKeyForDay(String day) {
			if (day.startsWith("mon") || day.startsWith("Mon")) {
				return 0;
			} else if (day.startsWith("tue") || day.startsWith("Tue")) {
			    return 1;
			} else if (day.startsWith("wed") || day.startsWith("Wed")) {
			    return 2;
			} else if (day.startsWith("thu") || day.startsWith("Thu")) {
			    return 3;
			} else if (day.startsWith("fri") || day.startsWith("Fri")) {
			    return 4;
			} else if (day.startsWith("sat") || day.startsWith("Sat")) {
			    return 5;
			} else if (day.startsWith("sun") || day.startsWith("Sun")) {
			    return 6;
			} else {
				// what a strange day from the Yahoo! server lolwut
				// we'll call it monday
				return 0;
			}
		}
	}

	static String getLongDay(Context ctx, String day) {
		int key = DayInfo.getKeyForDay(day);
		DayInfo dayInfo = day_map.get(key);
		return ctx.getString(dayInfo.long_day);
	}

	static String getShortDay(Context ctx, String day) {
		int key = DayInfo.getKeyForDay(day);
		DayInfo dayInfo = day_map.get(key);
		return ctx.getString(dayInfo.short_day);
	}
}

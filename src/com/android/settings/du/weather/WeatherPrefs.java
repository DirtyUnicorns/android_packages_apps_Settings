package com.android.settings.du.weather;

import org.codefirex.utils.WeatherInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.settings.R;

public class WeatherPrefs {
    private static final String WEATHER_PREFS = "weather_prefs";
    private static final String SETTINGS_PREFS = "settings_prefs";
    private static final String INTERVAL = "interval";
    private static final String ENABLED = "enabled";
    private static final String LOCATION_MODE = "location_mode";

    private static final String DEF_INTERVAL = "15";
    private static final String DEGREE_TYPE = "degree_type";

    static final int DEGREE_F = 1;
    static final int DEGREE_C = 2;

    private static SharedPreferences getWeatherPrefs(Context ctx) {
        return ctx.getSharedPreferences(WEATHER_PREFS, Context.MODE_PRIVATE);
    }
    
    private static SharedPreferences getSettingsPrefs(Context ctx) {
        return ctx.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getWeatherEdit(Context ctx) {
        return getWeatherPrefs(ctx).edit();
    }
    
    private static SharedPreferences.Editor getSettingsEdit(Context ctx) {
        return getSettingsPrefs(ctx).edit();
    }

    static boolean getEnabled(Context ctx) {
        return getSettingsPrefs(ctx).getBoolean(ENABLED, false);
    }

    static void setEnabled(Context ctx, boolean enabled) {
        getSettingsEdit(ctx).putBoolean(ENABLED, enabled).commit();
    }

    static String getInterval(Context ctx) {
        return getSettingsPrefs(ctx).getString(INTERVAL, DEF_INTERVAL);
    }

    static void setInterval(Context ctx, String interval) {
        getSettingsEdit(ctx).putString(INTERVAL, interval).commit();
    }

    static String getLocationMode(Context ctx) {
        return getSettingsPrefs(ctx).getString(LOCATION_MODE, LocationManager.FUSED_PROVIDER);
    }

    static void setLocationMode(Context ctx, String mode) {
        getSettingsEdit(ctx).putString(LOCATION_MODE, mode).commit();
    }

    static int getDegreeType(Context ctx) {
        return getSettingsPrefs(ctx).getInt(DEGREE_TYPE, DEGREE_F);
    }

    static void setDegreeType(Context ctx, int type) {
        getSettingsEdit(ctx).putInt(DEGREE_TYPE, type).commit();
        getWeatherEdit(ctx).putInt("temp_scale", type).commit();
    }

    static WeatherInfo getInfoFromPrefs(Context ctx) {
        WeatherInfo info = new WeatherInfo();
        info.mTitle = getWeatherPrefs(ctx).getString("title", "unknown");
        info.mLocationCity = getWeatherPrefs(ctx).getString("city", "unknown");
        info.mLocationCountry = getWeatherPrefs(ctx).getString("country", "unknown");
        info.mLastBuildDate = getWeatherPrefs(ctx).getString("date", "unknown");
        info.mCurrentText = getWeatherPrefs(ctx).getString("weather", "unknown");
        info.setCurrentScale(getWeatherPrefs(ctx).getInt("temp_scale", DEGREE_F));
        info.mCurrentTempF = getWeatherPrefs(ctx).getString("tempF", "0");
        info.mCurrentTempC = getWeatherPrefs(ctx).getString("tempC", "0");
        info.mWindChillF = getWeatherPrefs(ctx).getString("chillF",  "0");
        info.mWindChillC = getWeatherPrefs(ctx).getString("chillC",  "0");
        info.mWindDirection = getWeatherPrefs(ctx).getString("direction",  "unknown");
        info.mWindSpeed = getWeatherPrefs(ctx).getString("speed", "unknown");
        info.mAtmosphereHumidity = getWeatherPrefs(ctx).getString("humidity", "unknown");
        info.mAtmospherePressure = getWeatherPrefs(ctx).getString("pressure", "unknown");
        info.mAtmosphereVisibility = getWeatherPrefs(ctx).getString("visibility", "unknown");
        info.mCurrentCode = getWeatherPrefs(ctx).getInt("current_code", -1);
        info.mCurrentConditionIconURL = getWeatherPrefs(ctx).getString("current_url", "unknown");

        info.getForecastInfo1().setForecastDay(getWeatherPrefs(ctx).getString("f1_day", "unknown"));
        info.getForecastInfo1().setForecastDate(getWeatherPrefs(ctx).getString("f1_date", "unknown"));
        info.getForecastInfo1().setForecastText(getWeatherPrefs(ctx).getString("f1_weather","unknown"));
        info.getForecastInfo1().setForecastTempLowF(getWeatherPrefs(ctx).getString("f1_temp_lowF", "0"));
        info.getForecastInfo1().setForecastTempHighF(getWeatherPrefs(ctx).getString("f1_temp_highF", "0"));
        info.getForecastInfo1().setForecastTempLowC(getWeatherPrefs(ctx).getString("f1_temp_lowC", "0"));
        info.getForecastInfo1().setForecastTempHighC(getWeatherPrefs(ctx).getString("f1_temp_highC", "0"));
        info.getForecastInfo1().setForecastCode(getWeatherPrefs(ctx).getInt("f1_current_code", -1));

        info.getForecastInfo2().setForecastDay(getWeatherPrefs(ctx).getString("f2_day", "unknown"));
        info.getForecastInfo2().setForecastDate(getWeatherPrefs(ctx).getString("f2_date", "unknown"));
        info.getForecastInfo2().setForecastText(getWeatherPrefs(ctx).getString("f2_weather","unknown"));
        info.getForecastInfo2().setForecastTempLowF(getWeatherPrefs(ctx).getString("f2_temp_lowF", "0"));
        info.getForecastInfo2().setForecastTempHighF(getWeatherPrefs(ctx).getString("f2_temp_highF", "0"));
        info.getForecastInfo2().setForecastTempLowC(getWeatherPrefs(ctx).getString("f2_temp_lowC", "0"));
        info.getForecastInfo2().setForecastTempHighC(getWeatherPrefs(ctx).getString("f2_temp_highC", "0"));
        info.getForecastInfo2().setForecastCode(getWeatherPrefs(ctx).getInt("f2_current_code", -1));

        info.getForecastInfo3().setForecastDay(getWeatherPrefs(ctx).getString("f3_day", "unknown"));
        info.getForecastInfo3().setForecastDate(getWeatherPrefs(ctx).getString("f3_date", "unknown"));
        info.getForecastInfo3().setForecastText(getWeatherPrefs(ctx).getString("f3_weather","unknown"));
        info.getForecastInfo3().setForecastTempLowF(getWeatherPrefs(ctx).getString("f3_temp_lowF", "0"));
        info.getForecastInfo3().setForecastTempHighF(getWeatherPrefs(ctx).getString("f3_temp_highF", "0"));
        info.getForecastInfo3().setForecastTempLowC(getWeatherPrefs(ctx).getString("f3_temp_lowC", "0"));
        info.getForecastInfo3().setForecastTempHighC(getWeatherPrefs(ctx).getString("f3_temp_highC", "0"));
        info.getForecastInfo3().setForecastCode(getWeatherPrefs(ctx).getInt("f3_current_code", -1));

        info.getForecastInfo4().setForecastDay(getWeatherPrefs(ctx).getString("f4_day", "unknown"));
        info.getForecastInfo4().setForecastDate(getWeatherPrefs(ctx).getString("f4_date", "unknown"));
        info.getForecastInfo4().setForecastText(getWeatherPrefs(ctx).getString("f4_weather","unknown"));
        info.getForecastInfo4().setForecastTempLowF(getWeatherPrefs(ctx).getString("f4_temp_lowF", "0"));
        info.getForecastInfo4().setForecastTempHighF(getWeatherPrefs(ctx).getString("f4_temp_highF", "0"));
        info.getForecastInfo4().setForecastTempLowC(getWeatherPrefs(ctx).getString("f4_temp_lowC", "0"));
        info.getForecastInfo4().setForecastTempHighC(getWeatherPrefs(ctx).getString("f4_temp_highC", "0"));
        info.getForecastInfo4().setForecastCode(getWeatherPrefs(ctx).getInt("f4_current_code", -1));
        return info;
    }
    
    static void setPrefsFromInfo(Context ctx, WeatherInfo info) {
        getWeatherEdit(ctx).putString("title", info.mTitle)
        .putString("city", info.mLocationCity)
        .putString("country", info.mLocationCountry)
        .putString("date", info.mLastBuildDate)
        .putString("weather", info.mCurrentText)
        .putInt("temp_scale", info.mDegreeScale)
        .putString("tempF", String.valueOf(info.mCurrentTempF))
        .putString("tempC", String.valueOf(info.mCurrentTempC))
        .putString("chillF", info.mWindChillF)
        .putString("chillC", info.mWindChillC)
        .putString("direction", info.mWindDirection)
        .putString("speed", info.mWindSpeed)
        .putString("humidity", info.mAtmosphereHumidity)
        .putString("pressure", info.mAtmospherePressure)
        .putString("visibility", info.mAtmosphereVisibility)
        .putInt("current_code", info.mCurrentCode)
        .putString("current_url", info.mCurrentConditionIconURL)

        .putString("f1_day", info.getForecastInfo1().getForecastDay())
        .putString("f1_date", info.getForecastInfo1().getForecastDate())
        .putString("f1_weather", info.getForecastInfo1().getForecastText())
        .putString("f1_temp_lowF", String.valueOf(info.getForecastInfo1().getForecastTempLowF()))
        .putString("f1_temp_highF", String.valueOf(info.getForecastInfo1().getForecastTempHighF()))
        .putString("f1_temp_lowC", String.valueOf(info.getForecastInfo1().getForecastTempLowC()))
        .putString("f1_temp_highC", String.valueOf(info.getForecastInfo1().getForecastTempHighC()))
        .putInt("f1_current_code", info.getForecastInfo1().getForecastCode())
        .putString("f1_url", String.valueOf(info.getForecastInfo1().getForecastConditionIconURL()))

        .putString("f2_day", info.getForecastInfo2().getForecastDay())
        .putString("f2_date", info.getForecastInfo2().getForecastDate())
        .putString("f2_weather", info.getForecastInfo2().getForecastText())
        .putString("f2_temp_lowF", String.valueOf(info.getForecastInfo2().getForecastTempLowF()))
        .putString("f2_temp_highF", String.valueOf(info.getForecastInfo2().getForecastTempHighF()))
        .putString("f2_temp_lowC", String.valueOf(info.getForecastInfo2().getForecastTempLowC()))
        .putString("f2_temp_highC", String.valueOf(info.getForecastInfo2().getForecastTempHighC()))
        .putInt("f2_current_code", info.getForecastInfo2().getForecastCode())
        .putString("f2_url", String.valueOf(info.getForecastInfo2().getForecastConditionIconURL()))

        .putString("f3_day", info.getForecastInfo3().getForecastDay())
        .putString("f3_date", info.getForecastInfo3().getForecastDate())
        .putString("f3_weather", info.getForecastInfo3().getForecastText())
        .putString("f3_temp_lowF", String.valueOf(info.getForecastInfo3().getForecastTempLowF()))
        .putString("f3_temp_highF", String.valueOf(info.getForecastInfo3().getForecastTempHighF()))
        .putString("f3_temp_lowC", String.valueOf(info.getForecastInfo3().getForecastTempLowC()))
        .putString("f3_temp_highC", String.valueOf(info.getForecastInfo3().getForecastTempHighC()))
        .putInt("f3_current_code", info.getForecastInfo3().getForecastCode())
        .putString("f3_url", String.valueOf(info.getForecastInfo3().getForecastConditionIconURL()))

        .putString("f4_day", info.getForecastInfo4().getForecastDay())
        .putString("f4_date", info.getForecastInfo4().getForecastDate())
        .putString("f4_weather", info.getForecastInfo4().getForecastText())
        .putString("f4_temp_lowF", String.valueOf(info.getForecastInfo4().getForecastTempLowF()))
        .putString("f4_temp_highF", String.valueOf(info.getForecastInfo4().getForecastTempHighF()))
        .putString("f4_temp_lowC", String.valueOf(info.getForecastInfo4().getForecastTempLowC()))
        .putString("f4_temp_highC", String.valueOf(info.getForecastInfo4().getForecastTempHighC()))
        .putInt("f4_current_code", info.getForecastInfo4().getForecastCode())
        .putString("f4_url", String.valueOf(info.getForecastInfo4().getForecastConditionIconURL()))

        .commit();        
    }
}

/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Zhenghong Wang
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
 * limitations under the License.
 */

package com.android.settings.du.weather;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codefirex.utils.WeatherInfo;
import org.codefirex.utils.WeatherInfo.ForecastInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.android.settings.R;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class YahooWeatherUtils {
	private static final String TAG = "YahooWeatherUtils";
	public static final String YAHOO_WEATHER_ERROR = "Yahoo! Weather - Error";
	
	private String woeidNumber;
    private Context mContext;
//	private YahooWeatherInfoListener mWeatherInfoResult;

	public static YahooWeatherUtils getInstance() {
		return new YahooWeatherUtils();
	}
	
	public WeatherInfo queryYahooWeather(Context context, String cityName) {
        this.mContext = context;

        return handleWeatherQuery(context, cityName);
	}

    public WeatherInfo queryYahooWeather(Context context, String lat, String lon) {
        this.mContext = context;

        return handleWeatherQuery(context, lat, lon);
    }

    // alternative more robust query, json format!
    // query.yahooapis.com/v1/public/yql?q=select item from weather.forecast where location%3D"48907"&format=json

    private String getWeatherString(Context context, String woeidNumber) {
		String qResult = "";
		String queryString = "http://weather.yahooapis.com/forecastrss?w=" + woeidNumber;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
			
			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.i(TAG, e.toString());
		} catch (IOException e) {
			e.printStackTrace();
	        Log.i(TAG, e.toString());
		}

		return qResult;
	}

	private Document convertStringToDocument(Context context, String src) {
		Document dest = null;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dest;
	}

	private WeatherInfo parseWeatherInfo(Context context, Document doc) {
		WeatherInfo weatherInfo = new WeatherInfo();
		try {
			
			Node titleNode = doc.getElementsByTagName("title").item(0);
			
			if(titleNode.getTextContent().equals(YAHOO_WEATHER_ERROR)) {
				return null;
			}
			
			weatherInfo.setTitle(titleNode.getTextContent());
			weatherInfo.setDescription(doc.getElementsByTagName("description").item(0).getTextContent());
			weatherInfo.setLanguage(doc.getElementsByTagName("language").item(0).getTextContent());
			weatherInfo.setLastBuildDate(doc.getElementsByTagName("lastBuildDate").item(0).getTextContent());
			
			Node locationNode = doc.getElementsByTagName("yweather:location").item(0);
			weatherInfo.setLocationCity(locationNode.getAttributes().getNamedItem("city").getNodeValue());
			weatherInfo.setLocationRegion(locationNode.getAttributes().getNamedItem("region").getNodeValue());
			weatherInfo.setLocationCountry(locationNode.getAttributes().getNamedItem("country").getNodeValue());
			
			Node windNode = doc.getElementsByTagName("yweather:wind").item(0);
			weatherInfo.setWindChill(windNode.getAttributes().getNamedItem("chill").getNodeValue());
			weatherInfo.setWindDirection(windNode.getAttributes().getNamedItem("direction").getNodeValue());
			weatherInfo.setWindSpeed(windNode.getAttributes().getNamedItem("speed").getNodeValue());
			
			Node atmosphereNode = doc.getElementsByTagName("yweather:atmosphere").item(0);
			weatherInfo.setAtmosphereHumidity(atmosphereNode.getAttributes().getNamedItem("humidity").getNodeValue());
			weatherInfo.setAtmosphereVisibility(atmosphereNode.getAttributes().getNamedItem("visibility").getNodeValue());
			weatherInfo.setAtmospherePressure(atmosphereNode.getAttributes().getNamedItem("pressure").getNodeValue());
			weatherInfo.setAtmosphereRising(atmosphereNode.getAttributes().getNamedItem("rising").getNodeValue());
			
			Node astronomyNode = doc.getElementsByTagName("yweather:astronomy").item(0);
			weatherInfo.setAstronomySunrise(astronomyNode.getAttributes().getNamedItem("sunrise").getNodeValue());
			weatherInfo.setAstronomySunset(astronomyNode.getAttributes().getNamedItem("sunset").getNodeValue());
			
			weatherInfo.setConditionTitle(doc.getElementsByTagName("title").item(2).getTextContent());
			weatherInfo.setConditionLat(doc.getElementsByTagName("geo:lat").item(0).getTextContent());
			weatherInfo.setConditionLon(doc.getElementsByTagName("geo:long").item(0).getTextContent());
			
			Node currentConditionNode = doc.getElementsByTagName("yweather:condition").item(0);
			int currentCode = Integer.parseInt(currentConditionNode.getAttributes().getNamedItem("code").getNodeValue());
			weatherInfo.setCurrentCode(currentCode);
			weatherInfo.setCurrentText(WeatherProvider.getConditionText(mContext, currentCode));
//			weatherInfo.setCurrentText(
//					currentConditionNode.getAttributes().getNamedItem("text").getNodeValue());
			weatherInfo.setCurrentTempF(currentConditionNode.getAttributes().getNamedItem("temp").getNodeValue());
			weatherInfo.setCurrentConditionDate(
					currentConditionNode.getAttributes().getNamedItem("date").getNodeValue());
			
			this.parseForecastInfo(weatherInfo.getForecastInfo1(), doc, 0);
			this.parseForecastInfo(weatherInfo.getForecastInfo2(), doc, 1);
	        this.parseForecastInfo(weatherInfo.getForecastInfo3(), doc, 2);
	        this.parseForecastInfo(weatherInfo.getForecastInfo4(), doc, 3);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			weatherInfo = null;
		}
		
		return weatherInfo;
	}

	private void parseForecastInfo(final ForecastInfo forecastInfo, final Document doc, final int pIndex) {
		Node forecast1ConditionNode = doc.getElementsByTagName("yweather:forecast").item(pIndex);
		forecastInfo.setForecastCode(Integer.parseInt(
				forecast1ConditionNode.getAttributes().getNamedItem("code").getNodeValue()
				));
		forecastInfo.setForecastText(
				forecast1ConditionNode.getAttributes().getNamedItem("text").getNodeValue());
		forecastInfo.setForecastDate(
				forecast1ConditionNode.getAttributes().getNamedItem("date").getNodeValue());
		String day = forecast1ConditionNode.getAttributes().getNamedItem("day").getNodeValue();
		forecastInfo.setForecastDay(ResourceMaps.getShortDay(mContext, day));
		forecastInfo.setForecastTempHighF(forecast1ConditionNode.getAttributes().getNamedItem("high").getNodeValue());
		forecastInfo.setForecastTempLowF(forecast1ConditionNode.getAttributes().getNamedItem("low").getNodeValue());
	}

    private WeatherInfo handleWeatherQuery(Context context, String... cityName){

        WeatherInfo weatherInfo = null;

        WOEIDUtils woeidUtils = WOEIDUtils.getInstance();
        if (cityName.length > 1) {
            woeidNumber = woeidUtils.getWOEIDFromCoordinates(mContext, cityName[0], cityName[1]);
        } else {
            woeidNumber = woeidUtils.getWOEIDidFromCity(mContext, cityName[0]);
        }
        if(!woeidNumber.equals(WOEIDUtils.WOEID_NOT_FOUND)) {
            String weatherString = getWeatherString(mContext, woeidNumber);
            Document weatherDoc = convertStringToDocument(mContext, weatherString);
            weatherInfo = parseWeatherInfo(mContext, weatherDoc);
        }

        return weatherInfo;
    }

}

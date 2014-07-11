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
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.android.settings.R;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class WOEIDUtils {
	private static final String TAG = "WOEIDUtils";
	public static final String WOEID_NOT_FOUND = "WOEID_NOT_FOUND"; 
	private final String yahooapisBase = "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=";
	private final String yahooapisFormat = "&format=xml";
	private String yahooAPIsQuery;
	
	public static WOEIDUtils getInstance() {
		return new WOEIDUtils();
	}
	
	public String getWOEIDidFromCity(Context context, String cityName) {
		return queryWOEIDfromYahooAPIs(context, cityName);
	}

	public String getWOEIDFromCoordinates(Context context, String lat, String lon) {
	       StringBuilder b = new StringBuilder()
	        .append("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D%22")
	        .append(lat)
	        .append("%2C")
	        .append(lon)
	        .append("%22%20and%20gflags%3D%22R%22");
	        yahooAPIsQuery = b.toString();

	        String woeidString = queryYahooWeather(context, yahooAPIsQuery);
	        Document woeidDoc = convertStringToDocument(context, woeidString);
	        return getFirstMatchingWOEID(woeidDoc);
	}

	private String queryWOEIDfromYahooAPIs(Context context, String uriPlace) {
		Log.d("tag", "QueryYahooApis");

		yahooAPIsQuery = yahooapisBase + "%22" + uriPlace + "%22"
				+ yahooapisFormat;
		
		yahooAPIsQuery = yahooAPIsQuery.replace(" ", "%20");
		
		Log.d("tag", "yahooAPIsQuery: " + yahooAPIsQuery);

		String woeidString = queryYahooWeather(context, yahooAPIsQuery);
		Document woeidDoc = convertStringToDocument(context, woeidString);
		return getFirstMatchingWOEID(woeidDoc);

	}
	
	private String queryYahooWeather(Context context, String queryString) {
		Log.d("tag", "QueryYahooWeather = " + queryString);
		String qResult = "";

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
		Log.d("tag", "convertStringToDocument");
		Document dest = null;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (Exception e){
			e.printStackTrace();
		}

		return dest;

	}
	
	private String getFirstMatchingWOEID(Document srcDoc) {
		Log.d("tag", "parserWOEID");

		try {
			NodeList nodeListDescription = srcDoc.getElementsByTagName("woeid");
			if (nodeListDescription.getLength() > 0) {
				Node node = nodeListDescription.item(0);
				return node.getTextContent();
			} else {
				return WOEID_NOT_FOUND;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return WOEID_NOT_FOUND;
		}
		
	}
}

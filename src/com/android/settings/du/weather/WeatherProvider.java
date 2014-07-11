package com.android.settings.du.weather;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.android.settings.du.weather.ResourceMaps.ResInfo;
import org.codefirex.utils.WeatherInfo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.android.settings.R;

public class WeatherProvider extends ContentProvider {
	public static final String PACKAGE_NAME = "org.codefirex.cfxweather";
	public static final String WEATHER_AUTH = PACKAGE_NAME + ".icons";
	public static final String DATA_AUTH = PACKAGE_NAME + ".data";
	public static final Uri ICON_URI = Uri.parse("content://" + WEATHER_AUTH
			+ "/icons/#");
	public static final Uri DATA_URI = Uri.parse("content://" + DATA_AUTH);

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new RuntimeException("WeatherIconProvider.delete not supported");
	}

	@Override
	public String getType(Uri uri) {
		return WEATHER_AUTH;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new RuntimeException("WeatherIconProvider.insert not supported");
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Object[] row;
		if (uri.equals(DATA_URI)) {
			MatrixCursor result1 = new MatrixCursor(projection);
			WeatherInfo info = WeatherPrefs.getInfoFromPrefs(getContext());
			Bundle b = new Bundle();
			b.putParcelable(WeatherInfo.WEATHER_INFO_KEY, info);
			b.putBoolean(WeatherInfo.WEATHER_STATE_KEY, WeatherPrefs.getEnabled(getContext()));
			row = new Object[projection.length];
			for (int i = 0; i < projection.length; i++) {
				if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME) == 0) {
					row[i] = "WeatherInfo";
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.SIZE) == 0) {
					row[i] = 0;
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.DATA) == 0) {
					row[i] = b;
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.MIME_TYPE) == 0) {
					row[i] = DATA_AUTH;
				}
			}
			result1.addRow(row);
			result1.setExtras(b);
			return result1;
		} else {
			MatrixCursor result = new MatrixCursor(projection);
			long fileSize = 0;
			int index = Integer.parseInt((uri.getFragment()));
			File tempFile = getIconFromAssets(index);
			fileSize = tempFile.length();
			String iconName = getIconName(index);
			row = new Object[projection.length];
			for (int i = 0; i < projection.length; i++) {

				if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME) == 0) {
					row[i] = iconName;
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.SIZE) == 0) {
					row[i] = fileSize;
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.DATA) == 0) {
					row[i] = tempFile;
				} else if (projection[i]
						.compareToIgnoreCase(MediaStore.MediaColumns.MIME_TYPE) == 0) {
					row[i] = WEATHER_AUTH;
				}
			}
			result.addRow(row);
			return result;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new RuntimeException("WeatherIconProvider.update not supported");
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		int index = Integer.parseInt((uri.getFragment()));
		File tempFile = getIconFromAssets(index);
		return ParcelFileDescriptor.open(tempFile,
				ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private String getIconName(int conditionCode) {
		if (conditionCode == -1 || conditionCode == 3200) {
			conditionCode = 48;
		}
		ResInfo res = (ResInfo) ResourceMaps.weather_map.get(conditionCode);
		return res.iconName + ".png";
	}

	static String getConditionText(Context context, int conditionCode) {
		if (conditionCode == -1 || conditionCode == 3200) {
			conditionCode = 48;
		}
		ResInfo res = (ResInfo) ResourceMaps.weather_map.get(conditionCode);
		return context.getString(res.textRes);
	}

	private File getIconFromAssets(int index) {
		if (index == -1 || index == 3200) {
			index = 48;
		}
		String filename = getIconName(index);
		File f = new File(getContext().getCacheDir() + "/" + filename);
		InputStream is;
		try {
			if (!f.exists()) {
				is = getContext().getAssets().open(filename);
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
		return f;
	}
}

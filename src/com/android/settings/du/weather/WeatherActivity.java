package com.android.settings.du.weather;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.du.weather.WeatherService.WeatherBinder;
import org.codefirex.utils.WeatherAdapter;
import org.codefirex.utils.WeatherAdapter.WeatherListener;

public class WeatherActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	static final String TAG = "CFXWeatherActivity";
	static final String TITLE = "Weather";
	static final String SETTINGS_CATEGORY = "settings_category";
	static final String CALLING_PACKAGE = "calling_package";
	static final String NEEDS_BACK_ON_HOME = "needs_back_on_home";

	MenuItem mRefreshItem;

	PreferenceCategory mSettings;
	ListPreference mLocation;
	ListPreference mInterval;
	ListPreference mTempScale;
	ForecastPreference mForecast;
	Preference mCredits;

	WeatherBinder mBinder;
	WeatherAdapter mWeatherAdapter;
	WeatherListener mWeatherListener = new WeatherListener() {
		@Override
		public void onServiceStateChanged(int state) {
			switch (state) {
			case WeatherAdapter.STATE_ON:
				toggleForecast(true);
				enablePrefs(true);
				invalidateOptionsMenu();
				break;
			case WeatherAdapter.STATE_OFF:
				toggleForecast(false);
				enablePrefs(false);
				invalidateOptionsMenu();
				break;
			case WeatherAdapter.STATE_REFRESHING:
				if (mRefreshItem != null) {
					mRefreshItem.setActionView(R.layout.progress_spinner);
					mRefreshItem.expandActionView();
					enablePrefs(false);
					new Handler().postDelayed(mCheckTimeout, TIMEOUT);
				}
				break;
			case WeatherAdapter.STATE_SCALE:
				mForecast.setWeatherInfo(mWeatherAdapter.getLastKnownWeather());
				mForecast.invalidate();
				break;
			case WeatherAdapter.STATE_UPDATED:
				if (mRefreshItem != null) {
					new Handler().removeCallbacks(mCheckTimeout);
					mRefreshItem.collapseActionView();
					mRefreshItem.setActionView(null);
				}
				mForecast.setWeatherInfo(mWeatherAdapter.getLatestWeather());
				mForecast.invalidate();
				enablePrefs(WeatherPrefs.getEnabled(WeatherActivity.this));
				break;
			}
		}
	};	

	ServiceConnection mWeatherConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (WeatherBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (mBinder != null) mBinder = null;			
		}		
	};

	static final long TIMEOUT = 1000 * 10;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.weather_prefs);

		Intent intent = getIntent();
		boolean setBackOnHome = false;

		if (null != intent.getStringExtra(NEEDS_BACK_ON_HOME)
				&& "true".equals(intent.getStringExtra(NEEDS_BACK_ON_HOME))) {
			setBackOnHome = true;
		}

		ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_settings_dirt);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setTitle(TITLE);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mSettings = (PreferenceCategory) findPreference(SETTINGS_CATEGORY);

		Boolean isEnabled = WeatherPrefs.getEnabled(this);

		mLocation = (ListPreference) findPreference("weather_location_mode");
		mInterval = (ListPreference) findPreference("weather_interval");
		mTempScale = (ListPreference) findPreference("weather_temp_scale");

		mCredits = findPreference("credits_pref");

			mCredits.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					showCreditDialog();
					return true;
				}
			});

		mForecast = (ForecastPreference) findPreference("weather_forecast");
		mForecast.setSelectable(false);

		mLocation.setValue(String.valueOf(WeatherPrefs.getLocationMode(this)));
		mInterval.setValue(String.valueOf(WeatherPrefs.getInterval(this)));
		mTempScale.setValue(String.valueOf(WeatherPrefs.getDegreeType(this)));

		updateSummary(
				mLocation,
				getResources().getStringArray(R.array.weather_location_entries),
				getResources().getStringArray(R.array.weather_location_values));
		updateSummary(
				mInterval,
				getResources().getStringArray(R.array.weather_interval_entries),
				getResources().getStringArray(R.array.weather_interval_values));
		updateSummary(
				mTempScale,
				getResources().getStringArray(
						R.array.weather_temp_scale_entries), getResources()
						.getStringArray(R.array.weather_temp_scale_values));

		mLocation.setOnPreferenceChangeListener(this);
		mInterval.setOnPreferenceChangeListener(this);
		mTempScale.setOnPreferenceChangeListener(this);

		mWeatherAdapter = new WeatherAdapter(this, mWeatherListener);
		mForecast.setWeatherInfo(mWeatherAdapter.getLatestWeather());

		// initial state, we don't want a bunch of "unknown" showing
		toggleForecast(isEnabled && !mWeatherAdapter.getLastKnownWeather().getCurrentText().equals("unknown"));

		enablePrefs(isEnabled);
	}

	private void sendMessageToService(int msg) {
		if (mBinder != null) {
			Message m = mBinder.getService().getMessage();
			m.what = msg;
			mBinder.getService().sendMessage(m);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Boolean isEnabled = WeatherPrefs.getEnabled(this);
		getMenuInflater().inflate(R.menu.weather_menu, menu);
		mRefreshItem = menu.findItem(R.id.action_refresh);
		mRefreshItem.setEnabled(isEnabled);
		mRefreshItem.setVisible(isEnabled);
		MenuItem mSwitchItem = menu.findItem(R.id.action_switch);
		Switch s = (Switch) mSwitchItem.getActionView().findViewById(
				R.id.switchForActionBar);
		s.setChecked(isEnabled);
		s.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (mRefreshItem != null) {
					mRefreshItem.setEnabled(isChecked);
					mRefreshItem.setVisible(isChecked);
				}
				WeatherPrefs.setEnabled(WeatherActivity.this, isChecked);
				sendMessageToService(isChecked ? WeatherService.RESUME_SERVICE : WeatherService.PAUSE_SERVICE);
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			mRefreshItem = item;
			sendMessageToService(WeatherService.REFRESH_NOW);
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		mForecast.setWeatherInfo(mWeatherAdapter.getLastKnownWeather());
		mForecast.invalidate();
	}

	@Override
	public void onStart() {
		super.onStart();
		bindService(new Intent(this, WeatherService.class), mWeatherConn, Context.BIND_AUTO_CREATE);
		mWeatherAdapter.startUpdates();
	}

	@Override
	public void onStop() {
		super.onStop();
		unbindService(mWeatherConn);
		mWeatherAdapter.stopUpdates();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mLocation)) {
			String val = ((String) newValue).toString();
			WeatherPrefs.setLocationMode(this, val);
			sendMessageToService(WeatherService.LOCATION_MODE_CHANGED);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSummary(
							mLocation,
							getResources().getStringArray(
									R.array.weather_location_entries),
							getResources().getStringArray(
									R.array.weather_location_values));
				}
			}, 100);
			return true;
		} else if (preference.equals(mInterval)) {
			String val = ((String) newValue).toString();
			WeatherPrefs.setInterval(this, val);
			sendMessageToService(WeatherService.INTERVAL_CHANGED);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSummary(
							mInterval,
							getResources().getStringArray(
									R.array.weather_interval_entries),
							getResources().getStringArray(
									R.array.weather_interval_values));
				}
			}, 100);
			return true;
		} else if (preference.equals(mTempScale)) {
			int val = Integer.parseInt(((String) newValue).toString());
			WeatherPrefs.setDegreeType(this, val);
			sendMessageToService(WeatherService.SCALE_CHANGED);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSummary(
							mTempScale,
							getResources().getStringArray(
									R.array.weather_temp_scale_entries),
							getResources().getStringArray(
									R.array.weather_temp_scale_values));
				}
			}, 100);
			return true;
		}
		return false;
	}

	private void updateSummary(ListPreference pref, String[] entries,
			String[] values) {
		String currentVal = pref.getValue();
		String newEntry = "";
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(currentVal)) {
				newEntry = entries[i];
				break;
			}
		}
		pref.setSummary(newEntry);
	}

	private void toggleForecast(boolean enabled) {
		PreferenceScreen screen = getPreferenceScreen();
		if (enabled) {
			screen.addPreference(mForecast);
		} else {
			screen.removePreference(mForecast);
		}
	}

	private void enablePrefs(boolean enabled) {
		mLocation.setEnabled(enabled);
		mInterval.setEnabled(enabled);
		mTempScale.setEnabled(enabled);
	}

	private Runnable mCheckTimeout = new Runnable() {
		@Override
		public void run() {
			mRefreshItem.collapseActionView();
			mRefreshItem.setActionView(null);
			mForecast.invalidate();
			enablePrefs(WeatherPrefs.getEnabled(WeatherActivity.this));
		}
	};

	private void showCreditDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(R.string.credits);
		final AlertDialog dialog = b.create();

		View v = getLayoutInflater().inflate(R.layout.credits_view, null);

		// this could all be xml but got wonk in compiling the string
		TextView linkView = (TextView) v.findViewById(R.id.weather_icon_url);
		String str_links = "<a href='http://d3stroy.deviantart.com/art/SILq-Weather-Icons-356609017'>Icon set on DeviantART</a>";
		linkView.setLinksClickable(true);
		linkView.setMovementMethod(LinkMovementMethod.getInstance());
		linkView.setText(Html.fromHtml(str_links));

		TextView xmlCredit = (TextView) v.findViewById(R.id.credit_xml_github);
		String github_link = "<a href='https://github.com/zh-wang/YWeatherGetter4a'>YWeatherGetter4a on Github</a>";
		xmlCredit.setLinksClickable(true);
		xmlCredit.setMovementMethod(LinkMovementMethod.getInstance());
		xmlCredit.setText(Html.fromHtml(github_link));

		// have to maunally create buttons when AlertDialog has a custom view
		Button ok = (Button) v.findViewById(R.id.button_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.setView(v);
		dialog.show();
	}
}

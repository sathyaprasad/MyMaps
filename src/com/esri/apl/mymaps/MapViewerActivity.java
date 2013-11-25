package com.esri.apl.mymaps;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.apl.mymaps.BluetoothFragment.OnBluetoothChangeListener;
import com.esri.apl.mymaps.Enums.SensorBehavior;
import com.esri.apl.mymaps.NetworkMonitorFragment.OnNetworkChangeListener;
import com.esri.apl.mymaps.OptionDialogFragment.OnOptionDialogActionListener;
import com.esri.apl.mymaps.SensorProcessingFragment.OnSensorChangeListener;

public class MapViewerActivity extends Activity implements
		OnOptionDialogActionListener, OnBluetoothChangeListener,
		OnSensorChangeListener, OnNetworkChangeListener, OnInitListener {

	private FrameLayout drawerLayout = null;
	private DrawerLayout drawerContainer = null;
	private Integer currentMenu = R.string.menu_empty;

	private ScrollView infoScroll = null;
	private ScrollView layerScroll = null;
	private ScrollView aboutScroll = null;
	private ScrollView locationScroll = null;
	private ScrollView bookmarkScroll = null;
	private ScrollView settingsScroll = null;
	private ScrollView connectScroll = null;

	protected Menu menu = null;
	protected MapView mapView = null;
	protected ProgressBar progressBar = null;
	protected GraphicsLayer graphicsLayer = null;
	protected LocationService locationService = null;
	protected FrameLayout mapContainer = null;
	protected TextToSpeech textToSpeech = null;
	protected String webMapID = null;

	protected BluetoothFragment bluetoothFragment = null;
	protected NFCControlFragment nfcControlFragment = null;
	protected LoadWebMapFragment loadWebMapFragment = null;
	protected NetworkMonitorFragment networkMonitorFragment = null;
	protected SensorProcessingFragment sensorProcessingFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapviewer);
		mapContainer = (FrameLayout) findViewById(R.id.map);
		drawerContainer = (DrawerLayout) findViewById(R.id.map_container);
		drawerLayout = (FrameLayout) findViewById(R.id.drawer_container);

		infoScroll = (ScrollView) findViewById(R.id.description_control_scroll);
		layerScroll = (ScrollView) findViewById(R.id.layer_control_scroll);
		locationScroll = (ScrollView) findViewById(R.id.location_control_scroll);
		bookmarkScroll = (ScrollView) findViewById(R.id.bookmark_control_scroll);
		aboutScroll = (ScrollView) findViewById(R.id.about_control_scroll);
		settingsScroll = (ScrollView) findViewById(R.id.settings_control_scroll);
		connectScroll = (ScrollView) findViewById(R.id.connect_control_scroll);

		progressBar = new ProgressBar(getApplicationContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		progressBar.setLayoutParams(lp);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map_viewer_menu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mapView != null && mapView.isLoaded()) {
			mapView.pause();
		}
	}

	/* start loading webmap and fragments when the activity resumes */
	@Override
	protected void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.unpause();
		}
		if (loadWebMapFragment == null) {
			// setup text to speech
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, 0);

			initMapView(null, null);
		}
		if (bluetoothFragment == null) {
			bluetoothFragment = new BluetoothFragment();
			addFragment(bluetoothFragment, "bluetooth");
		}
		if (nfcControlFragment == null) {
			nfcControlFragment = NFCControlFragment.newInstance(webMapID);
			addFragment(nfcControlFragment, "nfc");
		}
		if (sensorProcessingFragment == null) {
			sensorProcessingFragment = new SensorProcessingFragment();
			addFragment(sensorProcessingFragment, "sensor");
		}
		if (networkMonitorFragment == null) {
			networkMonitorFragment = new NetworkMonitorFragment();
			addFragment(networkMonitorFragment, "network");
		}
	}

	/* setup the menu control for the drawers */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!mapView.isLoaded()) {
			return false;
		}
//		if (!menuReady) {
			loadWebMapFragment.loadMapControl();
//			menuReady = true;
//		}

		int itemID = item.getItemId();
		if (currentMenu == itemID && drawerContainer.isDrawerOpen(drawerLayout)) {
			drawerContainer.closeDrawers();
		} else {
			hideMenu();
			currentMenu = itemID;
			drawerContainer.openDrawer(drawerLayout);
			switch (itemID) {
			case R.id.action_description:
				infoScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_layers:
				layerScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_location:
				locationScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_about:
				aboutScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_bookmarks:
				bookmarkScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_connect:
				connectScroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_settings:
				settingsScroll.setVisibility(View.VISIBLE);
				break;
			case android.R.id.home:
				onBackPressed();
			default:
				break;
			}
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				textToSpeech = new TextToSpeech(this, this);
			}
		}
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (textToSpeech.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
				textToSpeech.setLanguage(Locale.US);
		} else if (status == TextToSpeech.ERROR) {
			Utility.toast(getApplicationContext(),
					"Sorry! Text To Speech failed...");
		}
	}

	private void hideMenu() {
		infoScroll.setVisibility(View.GONE);
		layerScroll.setVisibility(View.GONE);
		locationScroll.setVisibility(View.GONE);
		aboutScroll.setVisibility(View.GONE);
		bookmarkScroll.setVisibility(View.GONE);
		connectScroll.setVisibility(View.GONE);
		settingsScroll.setVisibility(View.GONE);
	}

	private void addFragment(Fragment fragment, String tag) {
		getFragmentManager().beginTransaction().add(fragment, tag).commit();
	}

	/* load the webmap */
	private void initMapView(String username, String password) {
		getActionBar().setTitle(null);
		mapContainer.addView(progressBar);
		if (loadWebMapFragment == null) {
			webMapID = getIntent().getStringExtra(DashboardActivity.Extra);
			loadWebMapFragment = LoadWebMapFragment.newInstance(webMapID);
			addFragment(loadWebMapFragment, "load webmap");
		}
		try {
			loadWebMapFragment.startLoadWebMap(username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * callback for login dialog, load the webmap again use the username and
	 * password from login dialog
	 */
	public void onOptionDialogAction(int action, String[] data) {
		if (action == OptionDialogFragment.LOGIN) {
			initMapView(data[0], data[1]);
		}
	}

	public void onBluetoothMessage(String message) {
		bluetoothFragment.processBluetoothMessage(message);
	}

	public void onSensorChange(SensorBehavior sensorBehavior, Bundle values) {
		sensorProcessingFragment.processSensorEvents(sensorBehavior, values);
	}

	public void onNetworkChange(Boolean isConnected) {
		networkMonitorFragment.processNetworkChange();
	}
}

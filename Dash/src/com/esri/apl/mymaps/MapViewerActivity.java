package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.esri.android.map.Callout;
import com.esri.android.map.DynamicLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.TiledServiceLayer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.MapLoadAction;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnWebMapLoadListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.apl.mymaps.BluetoothFragment.BluetoothConnectConfirm;
import com.esri.apl.mymaps.BluetoothFragment.ConnectThread;
import com.esri.apl.mymaps.BluetoothFragment.OnBluetoothChangeListener;
import com.esri.apl.mymaps.BluetoothFragment.ServerThread;
import com.esri.apl.mymaps.LoginFragment.OnLoginStatusChangeListener;
import com.esri.apl.mymaps.NetworkMonitorFragment.OnNetworkChangeListener;
import com.esri.apl.mymaps.SensorEventProcessingFragment.OnSensorChangeListener;
import com.esri.apl.mymaps.Status.BluetoothBehavior;
import com.esri.apl.mymaps.Status.SensorBehavior;
import com.esri.apl.mymaps.Status.SensorType;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.EsriServiceException;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Bookmark;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.WebMap;
import com.esri.core.portal.WebMapLayer;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.geocode.Locator;
import com.esri.core.tasks.ags.geocode.LocatorFindParameters;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

public class MapViewerActivity extends Activity implements OnLoginStatusChangeListener, OnNetworkChangeListener, OnSensorChangeListener, OnInitListener,
		OnBluetoothChangeListener {

	private static final String PORTAL_URL = "http://www.arcgis.com";
	private DBhelper dbhelper;
	private MapControlHelper mapControlHelper;
	public UIHelper uihelper;
	private MapView mv = null;
	private WebMap wm = null;
	private ActionBar ab = null;
	private Integer currentShowing = R.string.menu_empty;
	// private PopupContainer popupContainer = null;

	private Button myLoc = null;
	private Button defaultView = null;
	private TextView tv_info = null;
	private ScrollView info_control_scroll = null;
	private LinearLayout layer_control = null;
	private ScrollView layer_control_scroll = null;
	private LinearLayout location_control = null;
	private ScrollView about_control_scroll = null;
	private LinearLayout about_control = null;
	private ScrollView location_control_scroll = null;
	private ScrollView bookmark_control_scroll = null;
	private LinearLayout bookmark_control = null;
	private ScrollView settings_control_scroll = null;
	private LinearLayout settings_control = null;
	private ScrollView connect_control_scroll = null;
	private LinearLayout connect_control = null;
	private DrawerLayout drawer_container_layout = null;
	private FrameLayout drawer_layout = null;
	private FrameLayout mapContainer = null;
	private boolean menuReady;
	private boolean graphicLayerAlreadySetup;

	// private Map<Integer, ArcGISPopupInfo> pops;
	private List<View> geocodingResultViews;
	protected List<Bookmark> bookmarks;
	private Layer[] layers;
	private GraphicsLayer locationLayer;
	private boolean layerControlAlreadySetup;
	private LocationService locationService = null;;
	private Locator locator;
	protected Handler handler;
	protected Menu menu;

	protected String title, ID, owner, description, access, parent;
	private String type = Status.WEBMAP;
	private Float rating;
	private Long date_modified;
	private byte[] image;

	private FragmentManager fragmentManager;
	private LoginFragment loginFragment;
	private NFCControlFragment nfcControlFragment;
	private NetworkMonitorFragment networkMonitor;
	protected BluetoothFragment bluetoothFragment;

	private ArrayList<Popup> popupList;
	private PopupContainer popupContainer;
	private Callout callout;
	private Point pointTouched;
	private ProgressDialog progressDialog;
	private AtomicInteger count;
	private ProgressBar progressBar;
	protected MapViewerActivity mva;

	private static ProgressDialog dialog;
	private static int task = 0;
	private static boolean inWeakLight = false;
	private static boolean isPositioning = false;
	private long lastUpdate = System.currentTimeMillis();

	final float[] valuesAccelerometer = new float[3];
	final float[] valuesMagneticField = new float[3];

	final float[] matrixR = new float[9];
	final float[] matrixI = new float[9];
	final float[] matrixValues = new float[3];

	private SensorEventProcessingFragment sensorEventProcessFragment = null;
	protected Switch compassControl, lightSensorControl, gyroControl, gpsControl, proximitySensorControl, voiceControl, nfcControl;
	protected ToggleButton compassIndicator, lightSensorIndicator, gpsIndicator, gyroIndicator, proximitySensorIndicator, voiceSensorIndicator,
			nfcSensorIndicator;

	private SensorManager sensorManager;
	private Sensor lightSensor, proximitySensor, accelerometerSensor, magneticfieldSensor, gyroSensor;

	private SpeechRecognizer speechRecognizer;
	private TextToSpeech textToSpeech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_viewer);

		UUID uuid = UUID.randomUUID();
		Log.i("bluetooth", "uuid " + uuid.toString());

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticfieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		// TODO the dialog is leaking
		// dialog = ProgressDialog.show(this, getString(R.string.mapviewer),
		// getString(R.string.loading_webmap));
		dbhelper = new DBhelper(this);
		uihelper = new UIHelper(this);
		fragmentManager = getFragmentManager();
		mapContainer = (FrameLayout) findViewById(R.id.map);
		tv_info = (TextView) findViewById(R.id.map_info);
		info_control_scroll = (ScrollView) findViewById(R.id.description_control_scroll);
		layer_control = (LinearLayout) findViewById(R.id.layer_control);
		layer_control_scroll = (ScrollView) findViewById(R.id.layer_control_scroll);
		location_control = (LinearLayout) findViewById(R.id.location_control);
		location_control_scroll = (ScrollView) findViewById(R.id.location_control_scroll);
		bookmark_control = (LinearLayout) findViewById(R.id.bookmark_control);
		bookmark_control_scroll = (ScrollView) findViewById(R.id.bookmark_control_scroll);
		about_control = (LinearLayout) findViewById(R.id.about_control);
		about_control_scroll = (ScrollView) findViewById(R.id.about_control_scroll);
		settings_control = (LinearLayout) findViewById(R.id.settings_control);
		settings_control_scroll = (ScrollView) findViewById(R.id.settings_control_scroll);
		connect_control = (LinearLayout) findViewById(R.id.connect_control);
		connect_control_scroll = (ScrollView) findViewById(R.id.connect_control_scroll);
		drawer_container_layout = (DrawerLayout) findViewById(R.id.map_container);
		drawer_layout = (FrameLayout) findViewById(R.id.drawer_container);

		handler = new Handler();
		menuReady = false;
		graphicLayerAlreadySetup = false;
		layerControlAlreadySetup = false;
		locationLayer = new GraphicsLayer();
		locationLayer.setName(getString(R.string.graphic_layer));

		progressBar = new ProgressBar(this);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		progressBar.setLayoutParams(lp);
		geocodingResultViews = new ArrayList<View>();

		ab = getActionBar();
		ab.setTitle(null);
		mapContainer.addView(progressBar);
		Intent i = getIntent();
		ID = i.getStringExtra(DashActivity.Extra);
		parent = Status.CurrentParent;

		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, 0);

		nfcControlFragment = NFCControlFragment.newInstance(ID);
		fragmentManager.beginTransaction().add(nfcControlFragment, "nfc").commit();
		networkMonitor = new NetworkMonitorFragment();
		fragmentManager.beginTransaction().add(networkMonitor, "network").commit();
		sensorEventProcessFragment = new SensorEventProcessingFragment();
		fragmentManager.beginTransaction().add(sensorEventProcessFragment, "sensor").commit();
		try {
			bluetoothFragment = new BluetoothFragment();
			fragmentManager.beginTransaction().add(bluetoothFragment, "bluetooth").commit();
		} catch (NullPointerException e) {

		}

		final AsyncTask<String, Void, WebMap> maploading = new GetMap(this, null, null);
		maploading.execute(ID);
	}

	protected void onPause() {
		super.onPause();
		if (mv != null) {
			Log.i("appdash", "Mapview On Pause");
			mv.pause();
			if (locationService != null && !locationService.isStarted()) {
				locationService.stop();
			}
		}
	}

	protected void onResume() {
		super.onResume();
		if (mv != null) {
			Log.i("appdash", "Mapview On Resume");
			mv.unpause();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_viewer, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mv.isLoaded()) {
			setupLayerControl();
			setupGraphicLayer();
			menuReady = true;
		}

		if (!menuReady) {
			return false;
		}

		int itemID = item.getItemId();

		if (currentShowing == itemID && drawer_container_layout.isDrawerOpen(drawer_layout)) {
			// currentShowing = R.string.menu_empty;
			// return false;
			drawer_container_layout.closeDrawers();
		} else {
			drawer_container_layout.openDrawer(drawer_layout);
			// drawer_container_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
			switch (itemID) {
			case R.id.action_description:
				hideMenu();
				currentShowing = R.id.action_description;
				info_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_layers:
				hideMenu();
				currentShowing = R.id.action_layers;
				layer_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_location:
				hideMenu();
				currentShowing = R.id.action_location;
				location_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_about:
				hideMenu();
				currentShowing = R.id.action_about;
				about_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_bookmarks:
				hideMenu();
				currentShowing = R.id.action_bookmarks;
				bookmark_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_connect:
				hideMenu();
				currentShowing = R.id.action_connect;
				connect_control_scroll.setVisibility(View.VISIBLE);
				break;
			case R.id.action_settings:
				hideMenu();
				currentShowing = R.id.action_settings;
				settings_control_scroll.setVisibility(View.VISIBLE);
				break;
			case android.R.id.home:
				onBackPressed();
			default:
				break;
			}
		}
		return true;
	}

	private void setupGraphicLayer() {
		if (graphicLayerAlreadySetup) {
			return;
		}
		graphicLayerAlreadySetup = true;
		mv.addLayer(locationLayer);
	}

	private void setupLayerControl() {
		if (!mv.isLoaded() || layerControlAlreadySetup) {
			return;
		}
		layers = mv.getLayers();
		int length = layers.length;
		layerControlAlreadySetup = true;

		LinearLayout layerControlTitle = uihelper.titleWithImage(getString(R.string.menu_layers), R.drawable.layers, false);
		layer_control.addView(layerControlTitle);

		if (length == 0) {
			Toast.makeText(this, getString(R.string.no_layer), Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}
		if (length == 1) {
			float scale = getResources().getDisplayMetrics().density;
			TextView noLayer = new TextView(this);
			noLayer.setText(getString(R.string.no_operational_layer));
			noLayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, (int) (scale * 10.0f + 0.5f), 0, (int) (scale * 15.0f + 0.5f));
			noLayer.setLayoutParams(lp);
			noLayer.setGravity(Gravity.CENTER);
			layer_control.addView(noLayer);
		}

		for (int i = 0; i < length - 1; i++) {
			final int index = length - 1 - i;
			LinearLayout layerLayout = mapControlHelper.buildSingleLayerControlContainer(layers[index], index, this);
			layer_control.addView(layerLayout);
		}

		LinearLayout basemapControlTitle = uihelper.titleWithImage(getString(R.string.menu_basemap), R.drawable.basemap, false);
		layer_control.addView(basemapControlTitle);

		LinearLayout basemapLayout = mapControlHelper.setupBasemapSwitch(layers[0], this);
		layer_control.addView(basemapLayout);
	}

	private void startAndStopGPS() {
		if (mv == null || !mv.isLoaded()) {
			return;
		}
		if (locationService == null || !locationService.isStarted()) {
			locationLayer.removeAll();
			if (!Status.isLocated) {
				locationService = mv.getLocationService();
				locationService.setAccuracyCircleOn(true);
				locationService.setAllowNetworkLocation(true);
				locationService.setAutoPan(false);
				locationService.start();
				double scale = mv.getScale();
				mv.centerAt(locationService.getPoint(), true);
				mv.setScale(scale);
				if ("sdk".equals(Build.MODEL)) {
					Toast.makeText(this, getString(R.string.gps_emulator), Toast.LENGTH_SHORT).show();
				}
				Geometry resultLocGeom = locationService.getPoint();
				SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
				Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
				locationLayer.addGraphic(resultLocation);
				locationService.stop();
				handler.postDelayed(new Runnable() {
					public void run() {
						messageBuilder(BluetoothFragment.EXTENT_CHANGE,
								new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(), mv.getRotationAngle() });
					}
				}, 1000);
			}
		}
		/*
		 * else { locationService.stop(); }
		 */
	}

	static public class DismissDialog implements Runnable {
		public void run() {
			dialog.dismiss();
		}
	}

	class Geocoder extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

		@Override
		protected List<LocatorGeocodeResult> doInBackground(LocatorFindParameters... params) {
			// SpatialReference sr = mv.getSpatialReference();
			List<LocatorGeocodeResult> results = null;
			locator = new Locator(getResources().getString(R.string.geocoding_url));
			try {
				results = locator.find(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return results;
		}

		@Override
		protected void onPostExecute(final List<LocatorGeocodeResult> result) {
			task--;
			if (result == null || result.size() == 0) {
				// update UI with notice that no results were found
				Toast.makeText(MapViewerActivity.this, "No result found.", Toast.LENGTH_SHORT).show();
			} else {
				int result_count = result.size();
				Log.i("mymaps", Integer.toString(result_count));
				if (result_count > 5) {
					result_count = 5;
				}
				for (int i = 0; i < result_count; i++) {
					final int index = i;
					// TextView t = mapControlHelper.buildGeocodingResult(i,
					// result.get(i).getAddress());
					geocodingResultViews.add(mapControlHelper.buildGeocodingResult(i, result.get(i).getAddress(), result.get(i).getAttributes().get("Type")));
					geocodingResultViews.get(index).setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							locationLayer.removeAll();
							Geometry resultLocGeom = result.get(index).getLocation();
							SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
							Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
							locationLayer.addGraphic(resultLocation);
							TextSymbol resultAddress = new TextSymbol(12, result.get(index).getAddress(), Color.BLACK);
							resultAddress.setOffsetX(10);
							resultAddress.setOffsetY(10);
							Graphic resultText = new Graphic(resultLocGeom, resultAddress);
							locationLayer.addGraphic(resultText);
							mv.centerAt(result.get(index).getLocation(), true);
							handler.postDelayed(new Runnable() {
								public void run() {
									messageBuilder(BluetoothFragment.EXTENT_CHANGE, new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(),
											mv.getRotationAngle() });
								}
							}, 1000);
						}
					});
					location_control.addView(geocodingResultViews.get(index));
				}
			}
			handler.post(new DismissDialog());
		}
	}

	private class GetMap extends AsyncTask<String, Void, WebMap> {

		private Context mContext;
		private String username;
		private String password;
		private String errorMessage = null;
		private Boolean wait;

		public GetMap(Context mContext, String username, String password) {
			this.mContext = mContext;
			this.username = username;
			this.password = password;
			wait = false;
		}

		@Override
		protected WebMap doInBackground(String... params) {
			// publishProgress();
			ID = params[0];
			wm = null;
			Portal p = null;
			if (username != null && password != null) {
				UserCredentials uc = new UserCredentials();
				uc.setUserAccount(username, password);
				p = new Portal(PORTAL_URL, uc);
			} else {
				p = new Portal(PORTAL_URL, null);
			}
			final Portal portal = p;
			try {
				wm = WebMap.newInstance(ID, portal);
				PortalItem pi = wm.getInfo();
				title = pi.getTitle();
				ID = pi.getItemId();
				owner = pi.getOwner();
				date_modified = pi.getModified();
				image = pi.fetchThumbnail();
				description = pi.getDescription();
				rating = pi.getAvgRating();
				access = pi.getAccess().toString();
			} catch (EsriSecurityException e) {
				wait = true;
				e.printStackTrace();
				return null;
			} catch (EsriServiceException e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				// errorMessage = getString(R.string.invalid_webmap);
				e.printStackTrace();
				return null;
			}
			return wm;
		}

		@SuppressLint("NewApi")
		protected void onPostExecute(final WebMap wm) {
			Log.i("appdash", "onPostExecute");
			if (wm == null) {
				if (errorMessage != null) {
					Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
				}
				if (!wait) {
					onBackPressed();
				} else {
					loginFragment = LoginFragment.newInstance(PORTAL_URL);
					FragmentTransaction ft = fragmentManager.beginTransaction();
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					loginFragment.show(ft, "dialog");
					mapContainer.removeView(progressBar);
				}
				return;
			}

			OnWebMapLoadListener webmapListener = new OnWebMapLoadListener() {

				public void onWebMapLayerAdd(MapView tmpMapView, WebMap arg1, WebMapLayer arg2, Layer layer, UserCredentials arg4) {
					if (layer != null) {

						if (layer instanceof GraphicsLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is GraphicsLayer.");
						}
						if (layer instanceof GroupLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is GroupLayer.");
						}
						if (layer instanceof DynamicLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is DynamicLayer.");
						}
						if (layer instanceof TiledLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is TiledLayer.");
						}
						if (layer instanceof TiledServiceLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is TiledServiceLayer.");
						}
						if (layer instanceof ArcGISImageServiceLayer) {
							Log.i("appdash", "layer: " + layer.getTitle() + " is ArcGISImageServiceLayer.");
						}
						if (layer instanceof ArcGISFeatureLayer) {
							Log.i("appdash",
									"layer: " + layer.getTitle() + " is ArcGISFeatureLayer. Has attachment? " + ((ArcGISFeatureLayer) layer).hasAttachments());
						}

						if (layer instanceof ArcGISDynamicMapServiceLayer) {
							((ArcGISDynamicMapServiceLayer) layer).retrieveLegendInfo();
							// pops.putAll(((ArcGISDynamicMapServiceLayer)
							// layer).getPopupInfos());
							Log.i("appdash", "layer id: " + layer.getID());
							Log.i("appdash", "layer: " + layer.getTitle() + " is ArcGISDynamicMapServiceLayer");
						}
						if (layer instanceof ArcGISTiledMapServiceLayer) {
							((ArcGISTiledMapServiceLayer) layer).retrieveLegendInfo();
							// pops.putAll(((ArcGISTiledMapServiceLayer)
							// layer).getPopupInfos());
							Log.i("appdash", "layer id: " + layer.getID());
							Log.i("appdash", "layer: " + layer.getTitle() + " is ArcGISTiledMapServiceLayer");
						}
					}
				}

				public MapLoadAction<UserCredentials> onWebMapLoadError(MapView arg0, WebMap arg1, WebMapLayer arg2, Layer arg3, Throwable arg4,
						UserCredentials arg5) {
					Log.i("appdash", "onWebMapLoadError: problem loading layer");
					if (arg4 != null) {
						arg4.printStackTrace();
					}
					return new MapLoadAction<UserCredentials>(MapLoadAction.Action.CONTINUE_OPEN_AND_SKIP_CURRENT_LAYER, null);
				}
			};

			mv = new MapView(mContext, wm, ID, webmapListener);
			mv.setEsriLogoVisible(true);
			mv.setOnStatusChangedListener(new OnStatusChangedListener() {

				private static final long serialVersionUID = 1L;

				public void onStatusChanged(Object source, STATUS status) {
					if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == mv) {
						if (mv.isLoaded()) {
							ab.setHomeButtonEnabled(true);
							menu.findItem(R.id.action_about).setVisible(true);
							menu.findItem(R.id.action_description).setVisible(true);
							menu.findItem(R.id.action_location).setVisible(true);
							menu.findItem(R.id.action_layers).setVisible(true);
							menu.findItem(R.id.action_settings).setVisible(true);
							menu.findItem(R.id.action_connect).setVisible(true);
							mapControlHelper = new MapControlHelper(getApplicationContext(), mv);
							bookmarks = wm.getBookmarks();
							setupPopup();
							setupZoomAndPanListener();
							setupBookmarks();
							setupSettings();
							setupSensor();
							mv.setAllowRotationByPinch(true);
							handler.postDelayed(new Runnable() {
								public void run() {
									try {
										bluetoothFragment.startListen();
									} catch (NullPointerException e) {

									}
								}
							}, 1000);
						} else {
							Log.e("appdash", "mapview still loading");
						}
					}
				}
			});

			// mapContainer.removeAllViews();
			mapContainer.removeView(progressBar);
			mapContainer.addView(mv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			ab.setTitle("  " + title);

			Utility.insertToDatabase(dbhelper, ID, title, owner, image, date_modified, rating, description, access, parent, type);
			setupMenu();
			Log.i("appdash", "everything done");
			// handler.post(new DismissDialog());
			return;
		}
	}

	private void setupZoomAndPanListener() {
		// TODO continue
		final TextView scaleBar = (TextView) findViewById(R.id.scale_bar);
		int padding = Utility.pixelScaler(getApplicationContext(), 10);
		scaleBar.setPadding(padding, 0, padding, 0);
		scaleBar.setText(Utility.scaleFormatter(mv.getScale()));
		scaleBar.getParent().bringChildToFront(scaleBar);
		mv.setOnZoomListener(new OnZoomListener() {

			private static final long serialVersionUID = 2L;

			public void preAction(float arg0, float arg1, double arg2) {

			}

			public void postAction(float arg0, float arg1, double arg2) {
				scaleBar.setText(Utility.scaleFormatter(mv.getScale()));
				// sendBluetoothMessage(BluetoothFragment.EXTENT_CHANGE, null);
				messageBuilder(BluetoothFragment.EXTENT_CHANGE,
						new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(), mv.getRotationAngle() });
			}
		});
		mv.setOnPanListener(new OnPanListener() {

			public void prePointerUp(float arg0, float arg1, float arg2, float arg3) {

			}

			public void prePointerMove(float arg0, float arg1, float arg2, float arg3) {

			}

			public void postPointerUp(float arg0, float arg1, float arg2, float arg3) {

			}

			public void postPointerMove(float arg0, float arg1, float arg2, float arg3) {
				messageBuilder(BluetoothFragment.EXTENT_CHANGE,
						new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(), mv.getRotationAngle() });
			}
		});
		mv.setOnPinchListener(new OnPinchListener() {
			private static final long serialVersionUID = 3L;

			public void prePointersUp(float arg0, float arg1, float arg2, float arg3, double arg4) {
				// TODO Auto-generated method stub

			}

			public void prePointersMove(float arg0, float arg1, float arg2, float arg3, double arg4) {
				// TODO Auto-generated method stub

			}

			public void prePointersDown(float arg0, float arg1, float arg2, float arg3, double arg4) {
				// TODO Auto-generated method stub

			}

			public void postPointersUp(float arg0, float arg1, float arg2, float arg3, double arg4) {
				// TODO Auto-generated method stub

			}

			public void postPointersMove(float arg0, float arg1, float arg2, float arg3, double arg4) {
				messageBuilder(BluetoothFragment.EXTENT_CHANGE,
						new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(), mv.getRotationAngle() });
			}

			public void postPointersDown(float arg0, float arg1, float arg2, float arg3, double arg4) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void setupPopup() {
		mv.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				if (!mv.isLoaded() || (callout != null && callout.isShowing()) || isPositioning) {
					return;
				}
				Point p = mv.toMapPoint(x, y);
				messageBuilder(BluetoothFragment.SINGLE_TAP, new double[] { p.getX(), p.getY() });
				startQueryPopup(x, y);
			}

		});
	}

	private void startQueryPopup(float x, float y) {
		pointTouched = mv.toMapPoint(x, y);
		popupContainer = new PopupContainer(mv);
		popupList = new ArrayList<Popup>();
		// Display spinner.
		// if (progressDialog == null || !progressDialog.isShowing())
		// progressDialog = ProgressDialog.show(mv.getContext(), "",
		// "Querying...");
		Layer[] layers = mv.getLayers();
		count = new AtomicInteger();
		for (Layer layer : layers) {
			Log.i("appdash", "querying layer " + layer.getTitle());
			if (!layer.isInitialized() || !layer.isVisible()) {
				continue;
			}

			if (layer instanceof GroupLayer) {
				Layer[] ls = ((GroupLayer) layer).getLayers();
				for (Layer l : ls) {
					queryLayers(l, x, y);
				}
			} else {
				queryLayers(layer, x, y);
			}
		}
		if (count.intValue() == 0) {
			Log.i("appdash", "nothing to query");
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}
	}

	public void queryLayers(Layer layer, float x, float y) {
		int tolerance = 20;
		int id = popupContainer.hashCode();
		Envelope env = new Envelope(mv.toMapPoint(x, y), 20 * mv.getResolution(), 20 * mv.getResolution());
		if (layer instanceof ArcGISFeatureLayer) {
			Log.i("appdash", "layer " + layer.getTitle() + " is ArcGISFeatureLayer");
			ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;
			if (featureLayer.getPopupInfo() != null) {
				count.incrementAndGet();
				new RunQueryFeatureLayerTask(x, y, tolerance, id).execute(featureLayer);
			}
		} else if (layer instanceof ArcGISDynamicMapServiceLayer) {
			Log.i("appdash", "layer " + layer.getTitle() + " is ArcGISDynamicMapServiceLayer");
			ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
			ArcGISLayerInfo[] layerinfos = dynamicLayer.getAllLayers();
			if (layerinfos == null) {
				return;
			}

			for (ArcGISLayerInfo layerInfo : layerinfos) {
				// Obtain PopupInfo for sub-layer.
				PopupInfo popupInfo = dynamicLayer.getPopupInfo(layerInfo.getId());
				// Skip sub-layer which is without a popup
				// definition.
				if (popupInfo == null) {
					continue;
				}
				// Check if a sub-layer is visible.
				ArcGISLayerInfo info = layerInfo;
				while (info != null && info.isVisible()) {
					info = info.getParentLayer();
				}
				// Skip invisible sub-layer
				if (info != null && !info.isVisible()) {
					continue;
				}

				// Check if the sub-layer is within the scale range
				double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo.getMaxScale() : popupInfo.getMaxScale();
				double minScale = (layerInfo.getMinScale() != 0) ? layerInfo.getMinScale() : popupInfo.getMinScale();

				if ((maxScale == 0 || mv.getScale() > maxScale) && (minScale == 0 || mv.getScale() < minScale)) {
					// Query sub-layer which is associated with a
					// popup definition and is visible and in scale
					// range.
					count.incrementAndGet();
					new RunQueryDynamicLayerTask(env, layer, layerInfo.getId(), dynamicLayer.getSpatialReference(), id).execute(dynamicLayer.getUrl() + "/"
							+ layerInfo.getId());
				}
			}
		}
	}

	// Query feature layer by hit test
	private class RunQueryFeatureLayerTask extends AsyncTask<ArcGISFeatureLayer, Void, Graphic[]> {

		private int tolerance;
		private float x;
		private float y;
		private ArcGISFeatureLayer featureLayer;
		private int id;

		public RunQueryFeatureLayerTask(float x, float y, int tolerance, int id) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
			this.id = id;
		}

		@Override
		protected Graphic[] doInBackground(ArcGISFeatureLayer... params) {
			Log.i("appdash", "RunQueryFeatureLayerTask");
			for (ArcGISFeatureLayer featureLayer : params) {
				this.featureLayer = featureLayer;
				// Retrieve graphic ids near the point.
				int[] ids = featureLayer.getGraphicIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Graphic> graphics = new ArrayList<Graphic>();
					for (int id : ids) {
						// Obtain graphic based on the id.
						Graphic g = featureLayer.getGraphic(id);
						if (g == null)
							continue;
						graphics.add(g);
					}
					// Return an array of graphics near the point.
					return graphics.toArray(new Graphic[0]);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Graphic[] graphics) {
			Log.i("appdash", "RunQueryFeatureLayerTask done");
			count.decrementAndGet();
			if (graphics == null || graphics.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the
			// current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();
				return;
			}

			for (Graphic gr : graphics) {
				Popup popup = featureLayer.createPopup(mv, 0, gr);
				popupContainer.addPopup(popup);
				popupList.add(popup);
			}
			createPopupViews(graphics, id);
		}
	}

	// Query dynamic map service layer by QueryTask
	private class RunQueryDynamicLayerTask extends AsyncTask<String, Void, FeatureSet> {
		private Envelope env;
		private SpatialReference sr;
		private int id;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer, int subLayerId, SpatialReference sr, int id) {
			super();
			this.env = env;
			this.sr = sr;
			this.id = id;
			this.layer = layer;
			this.subLayerId = subLayerId;
		}

		@Override
		protected FeatureSet doInBackground(String... urls) {
			Log.i("appdash", "RunQueryDynamicLayerTask");
			for (String url : urls) {
				// Retrieve graphics within the envelope.
				Query query = new Query();
				query.setInSpatialReference(sr);
				query.setOutSpatialReference(sr);
				query.setGeometry(env);
				query.setMaxFeatures(10);
				query.setOutFields(new String[] { "*" });

				QueryTask queryTask = new QueryTask(url);
				try {
					FeatureSet results = queryTask.execute(query);
					return results;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final FeatureSet result) {
			Log.i("appdash", "RunQueryDynamicLayerTask done");
			count.decrementAndGet();
			if (result == null) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			Graphic[] graphics = result.getGraphics();
			if (graphics == null || graphics.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the
			// current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}
			PopupInfo popupInfo = layer.getPopupInfo(subLayerId);
			if (popupInfo == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
					progressDialog.dismiss();

				return;
			}

			for (Graphic gr : graphics) {
				Popup popup = layer.createPopup(mv, subLayerId, gr);
				popupContainer.addPopup(popup);
				popupList.add(popup);
			}
			createPopupViews(graphics, id);
		}
	}

	private void createPopupViews(Graphic[] graphics, final int id) {
		if (id != popupContainer.hashCode()) {
			if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)
				progressDialog.dismiss();
			return;
		}

		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
		makeCallout(0);
	}

	private void makeCallout(final int currentIndex) {
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());
		int totalPopupCount = popupContainer.getPopupCount();
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.map_popup_layout, null);
		TextView tv = (TextView) view.findViewById(R.id.map_popup_title);
		final ImageButton back = (ImageButton) view.findViewById(R.id.map_popup_back);
		final ImageButton next = (ImageButton) view.findViewById(R.id.map_popup_next);
		ImageView close = (ImageView) view.findViewById(R.id.map_popup_close);
		close.setVisibility(View.VISIBLE);
		tv.append(" " + Integer.toString(currentIndex + 1) + "/" + Integer.toString(totalPopupCount));
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.map_popup_container);
		layout.addView(Utility.PopupToView(popupList.get(currentIndex), mv), width, height);

		if (currentIndex != totalPopupCount - 1 && totalPopupCount != 1) {
			next.setVisibility(View.VISIBLE);
		} else {
			next.setVisibility(View.INVISIBLE);
		}
		if (currentIndex != 0) {
			back.setVisibility(View.VISIBLE);
		} else {
			back.setVisibility(View.INVISIBLE);
		}
		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == back.getId()) {
					makeCallout(currentIndex - 1);
					messageBuilder(BluetoothFragment.CALLOUT_CHANGE, new String[] { Integer.toString(currentIndex - 1) });
				} else if (v.getId() == next.getId()) {
					makeCallout(currentIndex + 1);
					messageBuilder(BluetoothFragment.CALLOUT_CHANGE, new String[] { Integer.toString(currentIndex + 1) });
				} else {
					if (callout != null) {
						callout.hide();
						messageBuilder(BluetoothFragment.CALLOUT_CHANGE, new String[] { Integer.toString(-1) });
					}
					return;
				}
			}
		};
		back.setOnClickListener(ocl);
		next.setOnClickListener(ocl);
		close.setOnClickListener(ocl);

		if (callout != null) {
			callout.hide();
		} else {
			callout = mv.getCallout();
			// TODO set callout style depending on the screen size
			callout.setStyle(R.xml.calloutstyle);
			callout.setOffset(0, 0);
		}
		callout.setContent(view);
		callout.setMaxHeight(height);
		callout.setMaxWidth(width);
		if (popupList.get(currentIndex).getGraphic().getGeometry().getType() == Type.POINT) {
			callout.show((Point) popupList.get(currentIndex).getGraphic().getGeometry());
		} else {
			callout.show(pointTouched);
		}
	}

	private void setupMenu() {
		// setup the map description
		tv_info.setText(Html.fromHtml(description));
		tv_info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		// tv_info.setBackgroundColor(Color.DKGRAY);
		tv_info.setPadding(10, 10, 10, 10);
		tv_info.setMovementMethod(LinkMovementMethod.getInstance());

		LinearLayout locationTitle = uihelper.titleWithImage(getString(R.string.location), R.drawable.extent, false);
		location_control.addView(locationTitle);

		LinearLayout myLocAndDefault = uihelper.twoButtons(getString(R.string.my_loc), R.id.my_loc, getString(R.string.default_loc), R.id.default_view);
		location_control.addView(myLocAndDefault);

		LinearLayout geocodingTitle = uihelper.titleWithImage(getString(R.string.menu_geocoding), R.drawable.geocoding, true);
		location_control.addView(geocodingTitle);

		EditText geocodingAddr = new EditText(this);
		geocodingAddr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		geocodingAddr.setHint(R.string.geocoding_tip);
		geocodingAddr.setId(R.id.geocoding_address);
		location_control.addView(geocodingAddr);

		LinearLayout searchAndClear = uihelper.twoButtons(getString(R.string.search), R.id.geocoding_confirm, getString(R.string.clear), R.id.geocoding_clear);
		location_control.addView(searchAndClear);

		myLoc = (Button) findViewById(R.id.my_loc);
		defaultView = (Button) findViewById(R.id.default_view);

		// setup my location
		myLoc.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startAndStopGPS();
				Status.isLocated = !Status.isLocated;
				if (Status.isLocated) {
					myLoc.setText("Clear");
				} else {
					myLoc.setText(getString(R.string.my_loc));
				}
			}
		});

		// setup default view
		defaultView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				SpatialReference input = wm.getInfo().getSpatialReference();
				SpatialReference output = mv.getSpatialReference();

				if (input == null) {
					input = SpatialReference.create(SpatialReference.WKID_WGS84);
				}

				Geometry extent = GeometryEngine.project(wm.getInitExtent(), input, output);
				if (extent != null) {
					Log.i("appdash", "get extent ");
					mv.setExtent(extent);
				}
				MapViewControlWidget.restoreRotatedMap(mv, mva);
				handler.postDelayed(new Runnable() {
					public void run() {
						messageBuilder(BluetoothFragment.EXTENT_CHANGE,
								new double[] { mv.getCenter().getX(), mv.getCenter().getY(), mv.getScale(), mv.getRotationAngle() });
					}
				}, 1000);
			}
		});

		// setup geocoding control
		final Button geocodingConfirmButton = (Button) findViewById(R.id.geocoding_confirm);
		final Button geocodingClearButton = (Button) findViewById(R.id.geocoding_clear);
		final EditText geocodingAddress = (EditText) findViewById(R.id.geocoding_address);

		geocodingAddr.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().contains("\n")) {
					geocodingConfirmButton.performClick();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});

		geocodingClearButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				for (View view : geocodingResultViews) {
					location_control.removeView(view);
				}
				geocodingResultViews = new ArrayList<View>();
				geocodingAddress.setText(getString(R.string.empty_text));
				locationLayer.removeAll();
			}
		});

		geocodingConfirmButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				dialog = ProgressDialog.show(mv.getContext(), getString(R.string.geocoding_title), getString(R.string.searching_address));
				if (task > 0) {
					return;
				}
				task++;
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.geocoding_address)).getWindowToken(), 0);
				String address = ((EditText) findViewById(R.id.geocoding_address)).getText().toString();
				geocodingClearButton.performClick();
				if (address == null) {
					return;
				}
				try {
					LocatorFindParameters findParams = new LocatorFindParameters(address);
					findParams.setMaxLocations(5);
					findParams.setOutSR(mv.getSpatialReference());
					List<String> outFilds = new ArrayList<String>();
					outFilds.add("*");
					findParams.setOutFields(outFilds);
					new Geocoder().execute(findParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		LinearLayout about = uihelper.buildAbout(wm.getInfo());
		about_control.addView(about);

		// bluetooth
		LinearLayout bluetoothSyncTitle = uihelper.titleWithImage(getString(R.string.bluetooth_sync_title), R.drawable.bluetooth_icon_small, false);
		connect_control.addView(bluetoothSyncTitle, 0);
	}

	private void setupBookmarks() {
		// bookmarks
		if (bookmarks != null && bookmarks.size() > 0) {
			menu.findItem(R.id.action_bookmarks).setVisible(true);
			LinearLayout bookmarkTitle = uihelper.titleWithImage(getString(R.string.bookmarks), R.drawable.bookmark, false);
			bookmark_control.addView(bookmarkTitle);
			mapControlHelper.buildBookmarkControl(bookmark_control, bookmarks);
		}
	}

	private void setupSettings() {
		settings_control = (LinearLayout) findViewById(R.id.settings_control);
		settings_control.addView(uihelper.titleWithImage(getString(R.string.menu_settings), R.drawable.i_options, false), 0);
	}

	private void hideMenu() {
		info_control_scroll.setVisibility(View.GONE);
		layer_control_scroll.setVisibility(View.GONE);
		location_control_scroll.setVisibility(View.GONE);
		about_control_scroll.setVisibility(View.GONE);
		bookmark_control_scroll.setVisibility(View.GONE);
		connect_control_scroll.setVisibility(View.GONE);
		settings_control_scroll.setVisibility(View.GONE);
	}

	public void onLoginStatusChanged(Boolean isLogedIn, String username, String password) {
		if (isLogedIn) {
			Toast.makeText(getApplicationContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();
			Intent i = getIntent();
			ID = i.getStringExtra(DashActivity.Extra);
			mapContainer.addView(progressBar);
			new GetMap(this, username, password).execute(ID);
		} else {
			onBackPressed();
		}
	}

	public void onNetworkChange(Boolean isConnected) {
		if (isConnected) {
			Toast.makeText(getApplicationContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
		}
	}

	private void setupSensor() {
		LinearLayout sensorIndicators = (LinearLayout) findViewById(R.id.sensor_indicators);

		LinearLayout compassControlContainer = (LinearLayout) findViewById(R.id.compass_control_container);
		LinearLayout lightSensorControlContainer = (LinearLayout) findViewById(R.id.light_sensor_control_container);
		LinearLayout GyroControlContainer = (LinearLayout) findViewById(R.id.gyroscope_control_container);
		LinearLayout GPSControlContainer = (LinearLayout) findViewById(R.id.gps_control_container);
		LinearLayout proximitySensorControlContainer = (LinearLayout) findViewById(R.id.proximity_sensor_control_container);
		LinearLayout voiceControlContainer = (LinearLayout) findViewById(R.id.voice_control_container);
		LinearLayout nfcControlContainer = (LinearLayout) findViewById(R.id.nfc_control_container);

		Utility.changeImageViewBackgroundToWhite(this, R.id.compass_icon, R.drawable.icon_sensor_compass);
		Utility.changeImageViewBackgroundToWhite(this, R.id.light_sensor_icon, R.drawable.icon_sensor_light);
		Utility.changeImageViewBackgroundToWhite(this, R.id.gps_icon, R.drawable.icon_sensor_gps);
		Utility.changeImageViewBackgroundToWhite(this, R.id.gyro_icon, R.drawable.icon_sensor_gyro);
		Utility.changeImageViewBackgroundToWhite(this, R.id.proximity_icon, R.drawable.icon_sensor_proximity);
		Utility.changeImageViewBackgroundToWhite(this, R.id.voice_icon, R.drawable.icon_sensor_voice);
		Utility.changeImageViewBackgroundToWhite(this, R.id.nfc_icon, R.drawable.icon_sensor_voice);

		sensorIndicators.getParent().bringChildToFront(sensorIndicators);
		compassIndicator = (ToggleButton) findViewById(R.id.compass_indicator);
		lightSensorIndicator = (ToggleButton) findViewById(R.id.light_sensor_indicator);
		gpsIndicator = (ToggleButton) findViewById(R.id.gps_indicator);
		gyroIndicator = (ToggleButton) findViewById(R.id.gyro_indicator);
		proximitySensorIndicator = (ToggleButton) findViewById(R.id.proximity_sensor_indicator);
		voiceSensorIndicator = (ToggleButton) findViewById(R.id.voice_indicator);
		nfcSensorIndicator = (ToggleButton) findViewById(R.id.nfc_indicator);

		compassControl = (Switch) findViewById(R.id.compass_control);
		lightSensorControl = (Switch) findViewById(R.id.light_sensor_control);
		gyroControl = (Switch) findViewById(R.id.gyroscope_control);
		gpsControl = (Switch) findViewById(R.id.gps_control);
		proximitySensorControl = (Switch) findViewById(R.id.proximity_sensor_control);
		voiceControl = (Switch) findViewById(R.id.voice_control);
		nfcControl = (Switch) findViewById(R.id.nfc_control);

		if (lightSensor != null) {
			lightSensorControlContainer.setVisibility(View.VISIBLE);
			lightSensorControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						lightSensorIndicator.setVisibility(View.VISIBLE);
						lightSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_light, R.drawable.white_circle));
					} else {
						lightSensorIndicator.setVisibility(View.GONE);
					}
				}
			});
			setupSensorToggle(lightSensorIndicator);
		}

		if (proximitySensor != null) {
			proximitySensorControlContainer.setVisibility(View.VISIBLE);
			proximitySensorControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						proximitySensorIndicator.setVisibility(View.VISIBLE);
						proximitySensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_proximity, R.drawable.white_circle));
					} else {
						proximitySensorIndicator.setVisibility(View.GONE);
					}
				}
			});
			setupSensorToggle(proximitySensorIndicator);
		}

		if (accelerometerSensor != null && magneticfieldSensor != null && gyroSensor != null) {
			compassControlContainer.setVisibility(View.VISIBLE);
			compassControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						compassIndicator.setVisibility(View.VISIBLE);
						compassIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_compass,
								R.drawable.white_circle));
					} else {
						// if (compassIndicator.isChecked()) {
						// compassIndicator.performClick();
						// }
						compassControl.setChecked(false);
						compassIndicator.setVisibility(View.GONE);
					}
				}
			});
			setupSensorToggle(compassIndicator);
		}

		if (accelerometerSensor != null && magneticfieldSensor != null && gyroSensor != null) {
			GyroControlContainer.setVisibility(View.VISIBLE);
			gyroControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						gyroIndicator.setVisibility(View.VISIBLE);
						gyroIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_gyro,
								R.drawable.white_circle));
					} else {
						gyroIndicator.setVisibility(View.GONE);
					}
				}
			});
			setupSensorToggle(gyroIndicator);
		}

		if (Status.isNFCAvailable) {
			nfcControlContainer.setVisibility(View.VISIBLE);
			nfcControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						nfcControlFragment.enable();
						nfcSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_voice,
								R.drawable.white_circle));
						// nfcSensorIndicator.setVisibility(View.VISIBLE);
					} else {
						nfcControlFragment.disable();
						// nfcSensorIndicator.setVisibility(View.GONE);
					}
				}
			});
			// setupSensorToggle(nfcSensorIndicator);
		}

		/*
		 * GPSControlContainer.setVisibility(View.VISIBLE);
		 * gpsControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 * public void onCheckedChanged(CompoundButton buttonView, boolean
		 * isChecked) { if (isChecked) {
		 * gpsIndicator.setVisibility(View.VISIBLE);
		 * gpsIndicator.setBackgroundDrawable
		 * (Utility.changeSensorIndicatorBackground(getApplicationContext(),
		 * R.drawable.icon_sensor_gps, R.drawable.white_circle)); } else {
		 * gpsIndicator.setVisibility(View.GONE); } } });
		 * gpsIndicator.setOnCheckedChangeListener(new OnCheckedChangeListener()
		 * { public void onCheckedChanged(CompoundButton buttonView, boolean
		 * isChecked) { startAndStopGPS(); } });
		 */

		// TODO add voice check
		voiceControlContainer.setVisibility(View.VISIBLE);
		voiceControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					voiceSensorIndicator.setVisibility(View.VISIBLE);
					voiceSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_voice,
							R.drawable.white_circle));
				} else {
					voiceSensorIndicator.setVisibility(View.GONE);
				}
			}
		});
		setupSensorToggle(voiceSensorIndicator);
	}

	private void setupSensorToggle(final ToggleButton toggler) {

		toggler.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (toggler.isChecked()) {
					if (compassIndicator != null && toggler.getId() == compassIndicator.getId()) {
						compassIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_compass_rotate,
								R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.compass, true);
						MapViewControlWidget.restoreRotatedMap(mv, mva);
						Toast.makeText(getApplicationContext(), getString(R.string.compass_tip), Toast.LENGTH_SHORT).show();
						// TODO when toggle sensor failed
					}
					if (lightSensorIndicator != null && toggler.getId() == lightSensorIndicator.getId()) {
						lightSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_light, R.drawable.red_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.light, true);
						Toast.makeText(getApplicationContext(), getString(R.string.light_sensor_tip), Toast.LENGTH_SHORT).show();
					}
					if (gyroIndicator != null && toggler.getId() == gyroIndicator.getId()) {
						sensorEventProcessFragment.toggleSensors(SensorType.gyro, true);
						gyroIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_gyro,
								R.drawable.red_circle));
						Toast.makeText(getApplicationContext(), getString(R.string.gyroscope_tip), Toast.LENGTH_SHORT).show();
					}
					if (gpsIndicator != null && toggler.getId() == gpsIndicator.getId()) {
						sensorEventProcessFragment.toggleSensors(SensorType.gps, true);
						gpsIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_gps,
								R.drawable.red_circle));
						Toast.makeText(getApplicationContext(), getString(R.string.gps_tip), Toast.LENGTH_SHORT).show();
					}
					if (proximitySensorIndicator != null && toggler.getId() == proximitySensorIndicator.getId()) {
						sensorEventProcessFragment.toggleSensors(SensorType.proximity, true);
						proximitySensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_proximity, R.drawable.red_circle));
						Toast.makeText(getApplicationContext(), getString(R.string.proximity_sensor_tip), Toast.LENGTH_SHORT).show();
					}
					if (voiceSensorIndicator != null && toggler.getId() == voiceSensorIndicator.getId()) {
						sensorEventProcessFragment.toggleSensors(SensorType.voice, true);
						Toast.makeText(getApplicationContext(), getString(R.string.voice_toast), Toast.LENGTH_SHORT).show();
						if (gyroIndicator.isChecked()) {
							gyroIndicator.performClick();
						}
						// voiceSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
						// R.drawable.icon_sensor_voice,
						// R.drawable.red_circle));
					}
					// if (nfcSensorIndicator != null && toggler.getId() ==
					// nfcSensorIndicator.getId()) {
					// nfcControlFragment.enable();
					// nfcSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
					// R.drawable.icon_sensor_voice,
					// R.drawable.red_circle));
					// }
				} else {
					if (compassIndicator != null && toggler.getId() == compassIndicator.getId()) {
						compassIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_compass,
								R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.compass, false);
						MapViewControlWidget.restoreRotatedMap(mv, mva);
						compassIndicator.setRotation(0);
					}
					if (lightSensorIndicator != null && toggler.getId() == lightSensorIndicator.getId()) {
						lightSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_light, R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.light, false);
					}
					if (gyroIndicator != null && toggler.getId() == gyroIndicator.getId()) {
						gyroIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_gyro,
								R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.gyro, false);
					}
					if (gpsIndicator != null && toggler.getId() == gpsIndicator.getId()) {
						gpsIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(), R.drawable.icon_sensor_gps,
								R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.gps, false);
					}
					if (proximitySensorIndicator != null && toggler.getId() == proximitySensorIndicator.getId()) {
						proximitySensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_proximity, R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.proximity, false);
					}
					if (voiceSensorIndicator != null && toggler.getId() == voiceSensorIndicator.getId()) {
						voiceSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
								R.drawable.icon_sensor_voice, R.drawable.white_circle));
						sensorEventProcessFragment.toggleSensors(SensorType.voice, false);
					}
					// if (nfcSensorIndicator != null && toggler.getId() ==
					// nfcSensorIndicator.getId()) {
					// nfcSensorIndicator.setBackgroundDrawable(Utility.changeSensorIndicatorBackground(getApplicationContext(),
					// R.drawable.icon_sensor_voice,
					// R.drawable.white_circle));
					// nfcControlFragment.disable();
					// }
				}
			}
		});
	}

	public void onSensorChange(SensorBehavior sensorBehavior, final Bundle args) {
		switch (sensorBehavior) {
		case PositionSensorsOn:
			isPositioning = true;
			MapViewControlWidget.lockOrientation(this);
			break;
		case PositionSensorsOff:
			isPositioning = false;
			MapViewControlWidget.unlockOrientation(this);
			break;
		case Shake:
			MapViewControlWidget.changeBasemap(this, null);
			Log.i("mymaps", "shake");
			break;
		case Lean:
			handler.post(new Runnable() {
				public void run() {
					MapViewControlWidget.panMap(mv, args.getDoubleArray(Status.DRIFT), mva);
				}
			});
			break;
		case Rotate:
			handler.post(new Runnable() {
				public void run() {
					rotateCompassIndicator(MapViewControlWidget.rotateMap(mv, args.getDouble(Status.ROTATION), mva));
				}
			});
			break;
		case DayLight:
			Log.i("mymaps", "light change to daylight");
			break;
		case WeakLight:
			Log.i("mymaps", "light change to weaklight");
			break;
		case LightSensorOn:
			MapViewControlWidget.setBrightness(this, args.getFloat(Status.LIGHT), false);
			Log.i("mymaps", "LightSensor on");
			break;
		case LightSensorOff:
			MapViewControlWidget.setBrightness(this, 0, true);
			Log.i("mymaps", "LightSensor off");
			break;
		case Near:
			MapViewControlWidget.changeBasemap(this, null);
			Log.i("mymaps", "near");
			break;
		case SpeechResult:
			final MapViewerActivity mva = this;
			final String command = args.getString(Status.VOICE_COMMAND);
			handler.post(new Runnable() {
				public void run() {
					try {
						MapViewControlWidget.executeVoiceCommands(mva, command, textToSpeech);
					} catch (Exception e) {

					}
				}
			});
			break;
		default:
			break;
		}
	}

	private void rotateCompassIndicator(double toDegree) {
		Log.i("compass", Double.toString(toDegree));
		Animation a = new RotateAnimation((float) -mv.getRotationAngle(), (float) toDegree, compassIndicator.getWidth() / 2, compassIndicator.getHeight() / 2);
		a.setDuration(300);
		a.setFillAfter(true);
		compassIndicator.startAnimation(a);
		// mv.setRotationAngle(toDegree, mv.getCenter(), true);
	}

	private void panMap(double[] values) {
		// MapViewControlHelper.panMap(mv, values);
		Point currentCenter = mv.getCenter();
		Point newCenter = new Point(currentCenter.getX() + values[0], currentCenter.getY() + values[1]);
		mv.centerAt(newCenter, true);
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
			Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
		}
	}

	public void onBluetoothChange(String msg, int type) {
		try {
			mva = this;
			Log.i("bl", "get a msg from bl");
			final String[] data = msg.split(BluetoothFragment.SPLITER);
			if (data[0] != null && data[0].equals(BluetoothFragment.CONNECT_REQUEST)) {
				Log.i("bl", "get connector msg");
				if (data[3] != null && !data[3].equals(ID)) {
					Log.i("bl", "data " + data[3] + " ID " + ID);
					handler.post(new Runnable() {
						public void run() {
							bluetoothFragment.disconnect();
							Toast.makeText(getApplicationContext(), getString(R.string.sync_hint), Toast.LENGTH_LONG).show();
						}
					});
				} else {
					bluetoothFragment.getConnected(data[1], data[2]);
				}
			} else if (data[0] != null && data[0].equals(BluetoothFragment.EXTENT_CHANGE)) {
				if (bluetoothFragment.bluetoothReceivingEnabled && bluetoothFragment.isSyncExtent) {
					handler.post(new Runnable() {
						public void run() {
							try {
								Point pt = new Point(Double.parseDouble(data[1]), Double.parseDouble(data[2]));
								mv.centerAt(pt, true);
								mv.setScale(Double.parseDouble(data[3]));
								mv.setRotationAngle(Double.parseDouble(data[4]));
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					});
				}
			} else if (data[0] != null && data[0].equals(BluetoothFragment.SINGLE_TAP)) {
				Log.i("bl", "tap received");
				Point p = mv.toScreenPoint(new Point(Float.parseFloat(data[1]), Float.parseFloat(data[2])));
				startQueryPopup(Float.parseFloat(Double.toString(p.getX())), Float.parseFloat(Double.toString(p.getY())));
			} else if (data[0] != null && data[0].equals(BluetoothFragment.LAYER_CHANGE)) {
				if (bluetoothFragment.bluetoothReceivingEnabled && bluetoothFragment.isSyncLayers) {
					handler.post(new Runnable() {
						public void run() {
							Log.i("bl", "layer change received");
							mv.getLayer(Integer.parseInt(data[1])).setVisible(Boolean.parseBoolean(data[2]));
						}
					});
				}
			} else if (data[0] != null && data[0].equals(BluetoothFragment.BASEMAP_CHANGE)) {
				if (bluetoothFragment.bluetoothReceivingEnabled && bluetoothFragment.isSyncLayers) {
					handler.post(new Runnable() {
						public void run() {
							MapViewControlWidget.changeBasemap(mva, data[1]);
						}
					});
				}
			} else if (data[0] != null && data[0].equals(BluetoothFragment.CALLOUT_CHANGE)) {
				Log.i("bl", "popup change received");
				if (bluetoothFragment.bluetoothReceivingEnabled && bluetoothFragment.isSyncPopup) {
					handler.post(new Runnable() {
						public void run() {
							int i = Integer.parseInt(data[1]);
							if (i != -1) {
								makeCallout(i);
							} else {
								callout.hide();
							}
						}
					});
				}
			}
		} catch (NullPointerException e) {

		}
	}

	public void messageBuilder(String type, String[] msg) {
		try {
			if (!bluetoothFragment.bluetoothSendingEnabled) {
				return;
			}
			if ((type.equals(BluetoothFragment.LAYER_CHANGE) || (type.equals(BluetoothFragment.BASEMAP_CHANGE))) && !bluetoothFragment.isSyncLayers) {
				return;
			}
			if ((type.equals(BluetoothFragment.CALLOUT_CHANGE)) && !bluetoothFragment.isSyncPopup) {
				return;
			}
			String result = "";
			result += type + BluetoothFragment.SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += msg[i] + BluetoothFragment.SPLITER;
			}
			result += type;
			bluetoothFragment.write(result.getBytes());
		} catch (NullPointerException e) {

		}
	}

	public void messageBuilder(String type, double[] msg) {
		try {
			if (!bluetoothFragment.bluetoothSendingEnabled) {
				return;
			}
			if (type.equals(BluetoothFragment.EXTENT_CHANGE) && !bluetoothFragment.isSyncExtent) {
				return;
			}
			if (type.equals(BluetoothFragment.SINGLE_TAP) && !bluetoothFragment.isSyncPopup) {
				return;
			}
			String result = "";
			result += type + BluetoothFragment.SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += Double.toString(msg[i]) + BluetoothFragment.SPLITER;
			}
			result += type;
			bluetoothFragment.write(result.getBytes());
		} catch (NullPointerException e) {

		}
	}

	public void messageBuilder(String type) {
		String result = type;
		try {
			bluetoothFragment.write(result.getBytes());
		} catch (NullPointerException e) {

		}
	}

	protected void toast(final String message) {
		mva = this;
		try {
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(mva, message, Toast.LENGTH_SHORT).show();
				}
			});
		} catch (Exception e) {

		}
	}
}

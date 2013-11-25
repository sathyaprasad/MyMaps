package com.esri.apl.mymaps;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.MapLoadAction;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnWebMapLoadListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.apl.mymaps.Enums.SensorType;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.EsriServiceException;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Bookmark;
import com.esri.core.map.Graphic;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.WebMap;
import com.esri.core.portal.WebMapLayer;
import com.esri.core.symbol.SimpleMarkerSymbol;

public class LoadWebMapFragment extends Fragment {

	private boolean menuReady;
	private WebMap webMap = null;
	private LoadWebMap loadWebMap = null;

	protected Switch compassControl = null, lightSensorControl = null,
			gyroControl = null, gpsControl = null,
			proximitySensorControl = null, voiceControl = null,
			nfcControl = null;
	protected ToggleButton compassIndicator = null,
			lightSensorIndicator = null, gpsIndicator = null,
			gyroIndicator = null, proximitySensorIndicator = null,
			voiceSensorIndicator = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuReady = false;
	}

	public void startLoadWebMap(String username, String password) {
		loadWebMap = new LoadWebMap();
		if (username == null || username == null) {
			loadWebMap.execute();
		} else {
			loadWebMap.execute(username, password);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (loadWebMap != null) {
			loadWebMap.cancel(true);
		}
	}

	public static LoadWebMapFragment newInstance(String webMapID) {
		LoadWebMapFragment loadWebMapFragment = new LoadWebMapFragment();
		Bundle args = new Bundle();
		args.putString("WebMapID", webMapID);
		loadWebMapFragment.setArguments(args);
		return loadWebMapFragment;
	}

	public String getWebMapID() {
		return getArguments().getString("WebMapID", null);
	}

	private MapViewerActivity getMapActivity() {
		return (MapViewerActivity) getActivity();
	}

	private class LoadWebMap extends AsyncTask<String, Void, WebMap> {

		private DashboardItem webMapItem = null;
		private Boolean waitingForLogin;
		private String webMapID = null, errorMessage = null;
		private static final String PORTAL_URL = "http://www.arcgis.com";

		public LoadWebMap() {

			waitingForLogin = false;
		}

		@Override
		protected WebMap doInBackground(String... params) {
			webMapID = getWebMapID();
			String username = null, password = null;
			if (params.length == 2) {
				username = params[0];
				password = params[1];
			}
			Portal portal = null;
			// if username and password are not null, try use them in the portal
			if (username != null && password != null && !username.isEmpty()
					&& !password.isEmpty()) {
				UserCredentials uc = new UserCredentials();
				uc.setUserAccount(username, password);
				portal = new Portal(PORTAL_URL, uc);
			} else {
				portal = new Portal(PORTAL_URL, null);
			}
			try {
				webMap = WebMap.newInstance(webMapID, portal);
				PortalItem pi = webMap.getInfo();
				webMapItem = new DashboardItem(pi.fetchThumbnail(),
						pi.getTitle(), webMapID, pi.getOwner(),
						pi.getDescription(), pi.getAccess().toString(),
						com.esri.apl.mymaps.Status.CurrentParent,
						DashboardItem.WEBMAP, pi.getModified(),
						pi.getAvgRating());
			} catch (EsriSecurityException e) {
				/*
				 * when EsriSecurityException is caught it means the username
				 * and password is wrong, start the login dialog
				 */
				if (username != null && password != null) {
					errorMessage = e.getMessage();
				}
				waitingForLogin = true;
				return null;
			} catch (EsriServiceException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (Exception e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
				return null;
			}
			return webMap;
		}

		protected void onPostExecute(final WebMap webMap) {
			if (webMap == null) {
				getMapActivity().mapContainer
						.removeView(getMapActivity().progressBar);
				if (errorMessage != null) {
					Utility.toast(getMapActivity(), errorMessage);
				}
				if (waitingForLogin) {
					OptionDialogFragment.newInstance(
							OptionDialogFragment.LOGIN, null).show(
							getMapActivity().getFragmentManager(), "login");
					return;
				} else {
					getMapActivity().onBackPressed();
				}
			} else {
				getMapActivity().mapView = new MapView(getMapActivity(),
						webMap, webMapID, new OnWebMapLoadListener() {
							public MapLoadAction<UserCredentials> onWebMapLoadError(
									MapView arg0, WebMap arg1,
									WebMapLayer arg2, Layer arg3,
									Throwable arg4, UserCredentials arg5) {
								return new MapLoadAction<UserCredentials>(
										MapLoadAction.Action.CONTINUE_OPEN_AND_SKIP_CURRENT_LAYER,
										null);
							}

							public void onWebMapLayerAdd(MapView arg0,
									WebMap arg1, WebMapLayer arg2, Layer layer,
									UserCredentials arg4) {
								if (layer != null) {
									if (layer instanceof ArcGISDynamicMapServiceLayer) {
										((ArcGISDynamicMapServiceLayer) layer)
												.retrieveLegendInfo();
									}
									if (layer instanceof ArcGISTiledMapServiceLayer) {
										((ArcGISTiledMapServiceLayer) layer)
												.retrieveLegendInfo();
									}
								}
							}
						});

				getMapActivity().mapView.setEsriLogoVisible(true);
				getMapActivity().mapView
						.setOnStatusChangedListener(new OnStatusChangedListener() {

							private static final long serialVersionUID = 54L;

							public void onStatusChanged(Object source,
									STATUS status) {
								// when the mapview is ready, update the record
								// in database and start doing setup for the map
								// control UI
								if (OnStatusChangedListener.STATUS.INITIALIZED == status
										&& source == getMapActivity().mapView) {
									try {
										getMapActivity().mapContainer
												.removeView(getMapActivity().progressBar);
										new Handler().postDelayed(
												new Runnable() {
													public void run() {
														updateWebMapRecord();
														setupMapControl();
													}
												}, 100);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						});
				getMapActivity().mapContainer.addView(getMapActivity().mapView,
						new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT));
			}
		}

		private void updateWebMapRecord() {
			new Thread(new Runnable() {
				public void run() {
					String parent = DashboardItem
							.getParentFolder(webMapItem.ID);
					if (parent != null) {
						webMapItem.parent = parent;
					}
					DashboardItem.insertToDatabase(new DatabaseHelper(
							getActivity()), webMapItem);
				}
			}).start();
		}
	}

	private void showMenuItems() {
		getMapActivity().menu.findItem(R.id.action_about).setVisible(true);
		getMapActivity().menu.findItem(R.id.action_description)
				.setVisible(true);
		getMapActivity().menu.findItem(R.id.action_location).setVisible(true);
		getMapActivity().menu.findItem(R.id.action_layers).setVisible(true);
		getMapActivity().menu.findItem(R.id.action_settings).setVisible(true);
		getMapActivity().menu.findItem(R.id.action_connect).setVisible(true);
	}

	private void showActionBarTitle() {
		getMapActivity().getActionBar().setHomeButtonEnabled(true);
		getMapActivity().getActionBar().setTitle(
				"  " + webMap.getInfo().getTitle());
	}

	private void setupMapControl() {
		setupPopupListener();
		setupExtentChangeListener();
		showActionBarTitle();
		showMenuItems();
		setupBookmarks();
		setupSettings();
		setupMapDescription();
		setupAbout();
	}

	/* setup the query for popup when there is a single tap */
	private void setupPopupListener() {
		getMapActivity().mapView
				.setOnSingleTapListener(new OnSingleTapListener() {

					private static final long serialVersionUID = -1076362381335410679L;

					public void onSingleTap(float x, float y) {
						if (!getMapActivity().mapView.isLoaded()
								|| Status.isPositionSensorWorking) {
							return;
						}
						Point p = getMapActivity().mapView.toMapPoint(x, y);
						MapControlWidget.queryMapView(getMapActivity(),
								p.getX(), p.getY(), true);
					}
				});
	}

	/* setup the listener for map extent change */
	private void setupExtentChangeListener() {
		final TextView scaleBar = (TextView) getMapActivity().findViewById(
				R.id.scale_bar);
		int padding = Utility.pixelScaler(getActivity(), 10);
		scaleBar.setPadding(padding, 0, padding, 0);
		scaleBar.setText(Utility.scaleFormatter(getMapActivity().mapView
				.getScale()));
		scaleBar.getParent().bringChildToFront(scaleBar);
		getMapActivity().mapView.setOnPanListener(new OnPanListener() {

			private static final long serialVersionUID = 1764889711361749247L;

			public void prePointerUp(float arg0, float arg1, float arg2,
					float arg3) {

			}

			public void prePointerMove(float arg0, float arg1, float arg2,
					float arg3) {

			}

			public void postPointerUp(float arg0, float arg1, float arg2,
					float arg3) {

			}

			public void postPointerMove(float arg0, float arg1, float arg2,
					float arg3) {

				// sent a bluetooth message when the extent changes
				getMapActivity().bluetoothFragment.messageBuilder(
						BluetoothFragment.EXTENT_CHANGE,
						Utility.extractMapViewExtent(getMapActivity().mapView));
			}
		});
		getMapActivity().mapView.setOnZoomListener(new OnZoomListener() {

			private static final long serialVersionUID = 1069587363052056045L;

			public void preAction(float arg0, float arg1, double arg2) {

			}

			public void postAction(float arg0, float arg1, double arg2) {
				double scale = getMapActivity().mapView.getScale();
				scaleBar.setText(Utility.scaleFormatter(scale));
				// sent a bluetooth when the scale changes
				getMapActivity().bluetoothFragment.messageBuilder(
						BluetoothFragment.SCALE, new double[] { scale });
			}
		});

		getMapActivity().mapView.setOnPinchListener(new OnPinchListener() {

			private static final long serialVersionUID = -2498156318146575240L;

			public void prePointersUp(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
			}

			public void prePointersMove(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
			}

			public void prePointersDown(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
			}

			public void postPointersUp(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
			}

			public void postPointersMove(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
				// sent a bluetooth message when the extent changes
				getMapActivity().bluetoothFragment.messageBuilder(
						BluetoothFragment.EXTENT_CHANGE,
						Utility.extractMapViewExtent(getMapActivity().mapView));
			}

			public void postPointersDown(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
			}
		});
	}

	/* setup the bookmark tab */
	private void setupBookmarks() {
		LinearLayout bookmark_control = (LinearLayout) getMapActivity()
				.findViewById(R.id.bookmark_control);
		List<Bookmark> bookmarks = webMap.getBookmarks();
		if (bookmarks != null && bookmarks.size() > 0) {
			getMapActivity().menu.findItem(R.id.action_bookmarks).setVisible(
					true);
			LinearLayout bookmarkTitle = MapControlUIBuilder.titleWithImage(
					getActivity(), getString(R.string.menu_bookmarks),
					R.drawable.bookmark, false);
			bookmark_control.addView(bookmarkTitle);
			MapControlUIBuilder.buildBookmarkControl(getMapActivity(),
					bookmark_control, bookmarks);
		}
	}

	private void setupSettings() {
		LinearLayout settings_control = (LinearLayout) getMapActivity()
				.findViewById(R.id.settings_control);
		settings_control.addView(MapControlUIBuilder.titleWithImage(
				getActivity(), getString(R.string.menu_settings),
				R.drawable.icon_setting, false), 0);
	}

	/* setup the map description */
	private void setupMapDescription() {
		TextView description = (TextView) getActivity().findViewById(
				R.id.map_info);
		description.setText(Html.fromHtml(webMap.getInfo().getDescription()));
		description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		description.setPadding(10, 10, 10, 10);
		description.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/* setup the map info */
	private void setupAbout() {
		LinearLayout about_control = (LinearLayout) getActivity().findViewById(
				R.id.about_control);
		LinearLayout about = MapControlUIBuilder.buildAbout(getActivity(),
				webMap.getInfo());
		about_control.addView(about);
	}

	/*
	 * load layer control, geocoding control, graphic layer and sensor control
	 * when the menu in map viewer activity is pressed
	 */
	public void loadMapControl() {
		if (!menuReady) {
			menuReady = true;
			setupLayerControl();
			setupGeocoding();
			setupGraphicLayer();
			setupSensors();
		}
	}

	private void setupGraphicLayer() {
		getMapActivity().graphicsLayer = new GraphicsLayer();
		getMapActivity().mapView.addLayer(getMapActivity().graphicsLayer);
	}

	private void setupLayerControl() {
		if (getMapActivity().mapView == null
				|| !getMapActivity().mapView.isLoaded()) {
			return;
		}
		Layer[] layers = getMapActivity().mapView.getLayers();
		int length = layers.length;
		LinearLayout layerControl = (LinearLayout) getActivity().findViewById(
				R.id.layer_control);
		LinearLayout layerControlTitle = MapControlUIBuilder.titleWithImage(
				getActivity(), getString(R.string.menu_layers),
				R.drawable.layers, false);
		layerControl.addView(layerControlTitle);

		if (length == 0) {
			Utility.toast(getActivity(), getString(R.string.layers_no_layer));
			getActivity().onBackPressed();
			return;
		}
		if (length == 1) {
			layerControl.addView(MapControlUIBuilder
					.buildNoLayerText(getActivity()));
		}

		// Operational layers control
		for (int i = 0; i < length - 1; i++) {
			final int index = length - 1 - i;
			LinearLayout layerLayout = MapControlUIBuilder
					.buildSingleLayerControlContainer(getMapActivity(),
							layers[index], index);
			layerControl.addView(layerLayout);
		}

		// Basemap control
		LinearLayout basemapControlTitle = MapControlUIBuilder.titleWithImage(
				getActivity(), getString(R.string.menu_basemap),
				R.drawable.basemap, false);
		layerControl.addView(basemapControlTitle);
		LinearLayout basemapLayout = MapControlUIBuilder.setupBasemapSwitch(
				getMapActivity(), layers[0]);
		MapControlWidget.defaultBasemapLayer = layers[0];
		layerControl.addView(basemapLayout);
	}

	private void setupGeocoding() {
		LinearLayout locationControl = (LinearLayout) getActivity()
				.findViewById(R.id.location_control);
		LinearLayout locationTitle = MapControlUIBuilder.titleWithImage(
				getActivity(), getString(R.string.geocoding_title),
				R.drawable.extent, false);
		locationControl.addView(locationTitle);
		MapControlUIBuilder.buildGeocodingControl(getMapActivity(), webMap,
				locationControl);
	}

	protected void startGPS() {
		if (getMapActivity().locationService == null
				|| !getMapActivity().locationService.isStarted()) {
			getMapActivity().graphicsLayer.removeAll();
			getMapActivity().locationService = getMapActivity().mapView
					.getLocationService();
			getMapActivity().locationService.setAccuracyCircleOn(true);
			getMapActivity().locationService.setAllowNetworkLocation(true);
			getMapActivity().locationService.setAutoPan(false);
			getMapActivity().locationService.start();
			MapControlWidget.centerMapAt(getMapActivity(),
					getMapActivity().locationService.getPoint(), true, true);
			if ("sdk".equals(Build.MODEL)) {
				Utility.toast(getActivity(), getString(R.string.gps_emulator));
				return;
			}
			Geometry resultLocGeom = getMapActivity().locationService
					.getPoint();
			SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
					Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
			Graphic resultLocation = new Graphic(resultLocGeom, resultSymbol);
			getMapActivity().graphicsLayer.addGraphic(resultLocation);
			getMapActivity().locationService.stop();
		}
	}

	/*
	 * setup all the sensors and sensor control depending what sensor is
	 * available on the device
	 */
	private void setupSensors() {
		final MapViewerActivity activity = getMapActivity();

		SensorManager sensorManager = (SensorManager) activity
				.getSystemService(Context.SENSOR_SERVICE);
		Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		Sensor proximitySensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		Sensor accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magneticfieldSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor gyroSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		LinearLayout sensorIndicators = (LinearLayout) activity
				.findViewById(R.id.sensor_indicators);
		LinearLayout compassControlContainer = (LinearLayout) activity
				.findViewById(R.id.compass_control_container);
		LinearLayout lightSensorControlContainer = (LinearLayout) activity
				.findViewById(R.id.light_sensor_control_container);
		LinearLayout GyroControlContainer = (LinearLayout) activity
				.findViewById(R.id.gyroscope_control_container);
		LinearLayout GPSControlContainer = (LinearLayout) activity
				.findViewById(R.id.gps_control_container);
		LinearLayout proximitySensorControlContainer = (LinearLayout) activity
				.findViewById(R.id.proximity_sensor_control_container);
		LinearLayout voiceControlContainer = (LinearLayout) activity
				.findViewById(R.id.voice_control_container);
		LinearLayout nfcControlContainer = (LinearLayout) activity
				.findViewById(R.id.nfc_control_container);

		Utility.changeImageViewBackgroundToWhite(activity, R.id.compass_icon,
				R.drawable.icon_sensor_compass);
		Utility.changeImageViewBackgroundToWhite(activity,
				R.id.light_sensor_icon, R.drawable.icon_sensor_light);
		Utility.changeImageViewBackgroundToWhite(activity, R.id.gps_icon,
				R.drawable.icon_sensor_gps);
		Utility.changeImageViewBackgroundToWhite(activity, R.id.gyro_icon,
				R.drawable.icon_sensor_gyro);
		Utility.changeImageViewBackgroundToWhite(activity, R.id.proximity_icon,
				R.drawable.icon_sensor_proximity);
		Utility.changeImageViewBackgroundToWhite(activity, R.id.voice_icon,
				R.drawable.icon_sensor_voice);
		Utility.changeImageViewBackgroundToWhite(activity, R.id.nfc_icon,
				R.drawable.icon_sensor_voice);

		sensorIndicators.getParent().bringChildToFront(sensorIndicators);
		compassIndicator = (ToggleButton) activity
				.findViewById(R.id.compass_indicator);
		lightSensorIndicator = (ToggleButton) activity
				.findViewById(R.id.light_sensor_indicator);
		gpsIndicator = (ToggleButton) activity.findViewById(R.id.gps_indicator);
		gyroIndicator = (ToggleButton) activity
				.findViewById(R.id.gyro_indicator);
		proximitySensorIndicator = (ToggleButton) activity
				.findViewById(R.id.proximity_sensor_indicator);
		voiceSensorIndicator = (ToggleButton) activity
				.findViewById(R.id.voice_indicator);

		compassControl = (Switch) activity.findViewById(R.id.compass_control);
		lightSensorControl = (Switch) activity
				.findViewById(R.id.light_sensor_control);
		gyroControl = (Switch) activity.findViewById(R.id.gyroscope_control);
		gpsControl = (Switch) activity.findViewById(R.id.gps_control);
		proximitySensorControl = (Switch) activity
				.findViewById(R.id.proximity_sensor_control);
		voiceControl = (Switch) activity.findViewById(R.id.voice_control);
		nfcControl = (Switch) activity.findViewById(R.id.nfc_control);

		if (lightSensor != null) {
			lightSensorControlContainer.setVisibility(View.VISIBLE);
			lightSensorControl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								lightSensorIndicator
										.setVisibility(View.VISIBLE);
								lightSensorIndicator.setBackgroundDrawable(Utility
										.changeSensorIndicatorBackground(
												activity,
												R.drawable.icon_sensor_light,
												R.drawable.white_circle));
							} else {
								lightSensorIndicator.setVisibility(View.GONE);
							}
						}
					});
			setupSensorIndicator(lightSensorIndicator);
		}

		if (proximitySensor != null) {
			proximitySensorControlContainer.setVisibility(View.VISIBLE);
			proximitySensorControl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								proximitySensorIndicator
										.setVisibility(View.VISIBLE);
								proximitySensorIndicator.setBackgroundDrawable(Utility
										.changeSensorIndicatorBackground(
												activity,
												R.drawable.icon_sensor_proximity,
												R.drawable.white_circle));
							} else {
								proximitySensorIndicator
										.setVisibility(View.GONE);
							}
						}
					});
			setupSensorIndicator(proximitySensorIndicator);
		}

		if (accelerometerSensor != null && magneticfieldSensor != null
				&& gyroSensor != null) {
			compassControlContainer.setVisibility(View.VISIBLE);
			compassControl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								compassIndicator.setVisibility(View.VISIBLE);
								compassIndicator.setBackgroundDrawable(Utility
										.changeSensorIndicatorBackground(
												activity,
												R.drawable.icon_sensor_compass,
												R.drawable.white_circle));
							} else {
								compassIndicator.setVisibility(View.GONE);
							}
						}
					});
			setupSensorIndicator(compassIndicator);
		}

		if (accelerometerSensor != null && magneticfieldSensor != null
				&& gyroSensor != null) {
			GyroControlContainer.setVisibility(View.VISIBLE);
			gyroControl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								gyroIndicator.setVisibility(View.VISIBLE);
								gyroIndicator.setBackgroundDrawable(Utility
										.changeSensorIndicatorBackground(
												activity,
												R.drawable.icon_sensor_gyro,
												R.drawable.white_circle));
							} else {
								gyroIndicator.setVisibility(View.GONE);
							}
						}
					});
			setupSensorIndicator(gyroIndicator);
		}

		if (Status.isNFCAvailable) {
			nfcControlContainer.setVisibility(View.VISIBLE);
			nfcControl
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								activity.nfcControlFragment.enable();
							} else {
								activity.nfcControlFragment.disable();
							}
						}
					});
		}

		GPSControlContainer.setVisibility(View.VISIBLE);
		gpsControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					gpsIndicator.setVisibility(View.VISIBLE);
					gpsIndicator.setBackgroundDrawable(Utility
							.changeSensorIndicatorBackground(activity,
									R.drawable.icon_sensor_gps,
									R.drawable.white_circle));
				} else {
					gpsIndicator.setVisibility(View.GONE);
				}
			}
		});
		setupSensorIndicator(gpsIndicator);

		voiceControlContainer.setVisibility(View.VISIBLE);
		voiceControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					voiceSensorIndicator.setVisibility(View.VISIBLE);
					voiceSensorIndicator.setBackgroundDrawable(Utility
							.changeSensorIndicatorBackground(activity,
									R.drawable.icon_sensor_voice,
									R.drawable.white_circle));
				} else {
					voiceSensorIndicator.setVisibility(View.GONE);
				}
			}
		});
		setupSensorIndicator(voiceSensorIndicator);
	}

	private void setupSensorIndicator(final ToggleButton toggler) {

		toggler.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				MapViewerActivity activity = getMapActivity();

				if (toggler.isChecked()) {
					if (compassIndicator != null
							&& toggler.getId() == compassIndicator.getId()) {
						compassIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_compass_rotate,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.compass, true);
						Utility.toast(activity, getString(R.string.compass_tip));
					}
					if (lightSensorIndicator != null
							&& toggler.getId() == lightSensorIndicator.getId()) {
						lightSensorIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_light,
										R.drawable.red_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.light, true);
						Utility.toast(activity,
								getString(R.string.light_sensor_tip));
					}
					if (gyroIndicator != null
							&& toggler.getId() == gyroIndicator.getId()) {
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.gyro, true);
						gyroIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_gyro,
										R.drawable.red_circle));
						Utility.toast(activity,
								getString(R.string.gyroscope_tip));
					}
					if (gpsIndicator != null
							&& toggler.getId() == gpsIndicator.getId()) {
						if (!Status.isMyLocationShowing) {
							((Button) activity
									.findViewById(R.id.geocoding_my_loc_id))
									.performClick();
						}
						gpsIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_gps,
										R.drawable.red_circle));
					}
					if (proximitySensorIndicator != null
							&& toggler.getId() == proximitySensorIndicator
									.getId()) {
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.proximity, true);
						proximitySensorIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_proximity,
										R.drawable.red_circle));
						Utility.toast(activity,
								getString(R.string.proximity_sensor_tip));
					}
					if (voiceSensorIndicator != null
							&& toggler.getId() == voiceSensorIndicator.getId()) {
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.voice, true);
						Utility.toast(activity, getString(R.string.voice_toast));
						if (gyroIndicator.isChecked()) {
							gyroIndicator.performClick();
						}
					}
				} else {
					if (compassIndicator != null
							&& toggler.getId() == compassIndicator.getId()) {
						compassIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_compass,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.compass, false);
						MapControlWidget.restoreRotatedMap(activity, true);
						compassIndicator.setRotation(0);
					}
					if (lightSensorIndicator != null
							&& toggler.getId() == lightSensorIndicator.getId()) {
						lightSensorIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_light,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.light, false);
					}
					if (gyroIndicator != null
							&& toggler.getId() == gyroIndicator.getId()) {
						gyroIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_gyro,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.gyro, false);
					}
					if (gpsIndicator != null
							&& toggler.getId() == gpsIndicator.getId()) {
						if (Status.isMyLocationShowing) {
							((Button) activity
									.findViewById(R.id.geocoding_my_loc_id))
									.performClick();
						}
						gpsIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_gps,
										R.drawable.white_circle));
					}
					if (proximitySensorIndicator != null
							&& toggler.getId() == proximitySensorIndicator
									.getId()) {
						proximitySensorIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_proximity,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.proximity, false);
					}
					if (voiceSensorIndicator != null
							&& toggler.getId() == voiceSensorIndicator.getId()) {
						voiceSensorIndicator.setBackgroundDrawable(Utility
								.changeSensorIndicatorBackground(activity,
										R.drawable.icon_sensor_voice,
										R.drawable.white_circle));
						activity.sensorProcessingFragment.toggleSensors(
								SensorType.voice, false);
					}
				}
			}
		});
	}
}

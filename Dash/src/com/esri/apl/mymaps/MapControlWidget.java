package com.esri.apl.mymaps;

import java.util.Locale;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.esri.android.map.Layer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.osm.OpenStreetMapLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;

public class MapControlWidget {

	public static Layer defaultBasemapLayer = null;
	public static final String DEFAULT_BASEMAP_LAYER = "default basemap layer";
	public static final double NO_CALIBERATE = 1000;

	private static boolean isAutoRotateOn = false;
	private static int currentOrientation = -10;
	private static float currentDpiFactor = 0.0f;
	private static boolean isDefaultPortrait = false;

	/* center the map at a certain point and send the bluetooth message */
	public static void centerMapAt(final MapViewerActivity activity,
			final Point center, final boolean animated,
			final boolean sendMessage) {
		new Handler().post(new Runnable() {
			public void run() {
				activity.mapView.centerAt(center, animated);
				if (sendMessage) {
					activity.bluetoothFragment.messageBuilder(
							BluetoothFragment.CENTER_AT,
							Utility.extractPoint(center, animated));
				}
			}
		});
	}

	/* pan the map and send the bluetooth message */
	public static void panMap(final MapViewerActivity activity,
			double[] values, final boolean sendMessage) {
		double[] calibratedXY = calibrateXY(values);
		Point currentScreenCenter = activity.mapView
				.toScreenPoint(activity.mapView.getCenter());
		final Point newScreenCenter = new Point(currentScreenCenter.getX()
				+ calibratedXY[1], currentScreenCenter.getY() + calibratedXY[0]);
		new Handler().post(new Runnable() {
			public void run() {
				centerMapAt(activity,
						activity.mapView.toMapPoint(newScreenCenter), false,
						sendMessage);
			}
		});
	}

	/* rotate the compass indicator depend on the angle of the map */
	public static void rotateCompassIndicator(final MapViewerActivity activity,
			double toDegree) {
		Animation a = new RotateAnimation(
				(float) -activity.mapView.getRotationAngle(), (float) toDegree,
				activity.loadWebMapFragment.compassIndicator.getWidth() / 2,
				activity.loadWebMapFragment.compassIndicator.getHeight() / 2);
		a.setDuration(300);
		a.setFillAfter(true);
		activity.loadWebMapFragment.compassIndicator.startAnimation(a);
	}

	/* rotate the map and send the bluetooth message */
	public static double rotateMap(final MapViewerActivity activity,
			double currentAzumith, final boolean sendMessage) {
		double tmp = 0;
		if (currentAzumith == NO_CALIBERATE) {
			tmp = 0;
		} else {
			tmp = calibrateRotation(currentAzumith);
		}
		final double degree = tmp;
		new Handler().post(new Runnable() {
			public void run() {
				activity.mapView.setRotationAngle(degree,
						activity.mapView.getCenter(), true);
				if (sendMessage) {
					activity.bluetoothFragment.messageBuilder(
							BluetoothFragment.ROTATE,
							Utility.extractRotation(degree,
									activity.mapView.getCenter(), true));
				}
			}
		});
		return -degree;
	}

	/* set the rotation of the map to zero */
	public static void restoreRotatedMap(final MapViewerActivity activity,
			final boolean sendMessage) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				rotateMap(activity, 0, sendMessage);
			}
		}, 1500);
	}

	/* query the map for popup and send the bluetooth message */
	public static void queryMapView(MapViewerActivity activity, float x,
			float y, boolean sendMessage) {
		MapControlUIBuilder.pointQueried = activity.mapView.toMapPoint(x, y);
		new CalloutQuery(activity).queryPopup(x, y);
		if (sendMessage) {
			activity.bluetoothFragment.messageBuilder(
					BluetoothFragment.SINGLE_TAP, new double[] {
							MapControlUIBuilder.pointQueried.getX(),
							MapControlUIBuilder.pointQueried.getY() });
		}
	}

	/* change the callout and send the bluetooth message */
	public static void switchCallout(MapViewerActivity activity, int index,
			boolean sendMessage) {
		MapControlUIBuilder.makeCallout(activity, index, null);
		if (sendMessage) {
			activity.bluetoothFragment.messageBuilder(
					BluetoothFragment.CALLOUT_CHANGE,
					new double[] { (double) index });
		}
	}

	/* change the layer visibility and send the bluetooth message */
	public static void switchLayer(MapViewerActivity activity, int index,
			CheckBox layerSwitch, Boolean isVisible, boolean sendMessage) {
		if (layerSwitch == null) {
			LinearLayout layerControl = (LinearLayout) activity
					.findViewById(R.id.layer_control);
			for (int i = 0; i < layerControl.getChildCount(); i++) {
				View v = layerControl.getChildAt(i);
				if (v.getTag() != null
						&& Integer.parseInt(v.getTag().toString()) == index) {
					layerSwitch = (CheckBox) v;
					break;
				}
			}
			if (layerSwitch == null) {
				return;
			}
		}
		layerSwitch.setChecked(isVisible);
		activity.mapView.getLayer(index).setVisible(isVisible);
		if (sendMessage) {
			activity.bluetoothFragment.messageBuilder(
					BluetoothFragment.LAYER_CHANGE,
					new String[] { Integer.toString(index),
							Boolean.toString(isVisible) });
		}
	}

	/* switch to the next basemap */
	public static void switchToNextBasemap(MapViewerActivity activity) {
		if (activity == null) {
			return;
		}
		RadioGroup rg = (RadioGroup) activity.findViewById(R.id.basemap_switch);
		if (rg == null) {
			return;
		}
		int childCount = rg.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (((RadioButton) rg.getChildAt(i)).isChecked()) {
				if (i == childCount - 1) {
					((RadioButton) rg.getChildAt(0)).performClick();
					break;
				} else {
					((RadioButton) rg.getChildAt(i + 1)).performClick();
					break;
				}
			}
		}
	}

	/* change the basemap and send the bluetooth message */
	public static void switchBasemap(MapViewerActivity activity, String name,
			RadioGroup rg, RadioButton rb, boolean sendMessage) {
		if (activity == null) {
			return;
		}
		if (rg == null) {
			rg = (RadioGroup) activity.findViewById(R.id.basemap_switch);
			if (rg == null) {
				return;
			}
		}
		if (rb == null) {
			for (int i = 0; i < rg.getChildCount(); i++) {
				if (((RadioButton) rg.getChildAt(i)).getText().toString()
						.equals(name)) {
					rb = (RadioButton) rg.getChildAt(i);
					break;
				}
			}
			if (rb == null) {
				return;
			}
		}
		if (name == null) {
			name = rb.getText().toString();
		}
		MapControlUIBuilder.switchRadioButton(rb, rg);
		activity.mapView.removeLayer(0);
		String key = rb.getTag().toString();
		if (key.equals(DEFAULT_BASEMAP_LAYER)) {
			activity.mapView.addLayer(defaultBasemapLayer, 0);
		} else if (key.equals(activity.getString(R.string.OSMMenu))) {
			activity.mapView.addLayer(new OpenStreetMapLayer(), 0);
		} else {
			activity.mapView.addLayer(new ArcGISTiledMapServiceLayer(key), 0);
		}
		if (sendMessage) {
			activity.bluetoothFragment.messageBuilder(
					BluetoothFragment.BASEMAP_CHANGE, new String[] { name });
		}
	}

	/* change the map extent and send the bluetooth message */
	public static void setMapExtent(MapViewerActivity activity,
			Geometry extent, boolean sendMessage) {
		activity.mapView.setExtent(extent);
		if (sendMessage) {
			activity.bluetoothFragment.messageBuilder(
					BluetoothFragment.EXTENT_CHANGE,
					Utility.extractMapViewExtent(activity.mapView));
		}
	}

	/* change the brightness between maximum brightness and auto brightness */
	public static void setBrightness(Activity activity, boolean isAuto) {
		try {
			if (!isAuto) {
				int brightnessMode = android.provider.Settings.System
						.getInt(activity.getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);

				if (brightnessMode == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
					android.provider.Settings.System
							.putInt(activity.getContentResolver(),
									android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
									android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
				}
				WindowManager.LayoutParams layoutParams = activity.getWindow()
						.getAttributes();
				layoutParams.screenBrightness = 255f;
				activity.getWindow().setAttributes(layoutParams);
			} else {
				android.provider.Settings.System
						.putInt(activity.getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int lockOrientation(Activity activity) {
		isAutoRotateOn = (android.provider.Settings.System.getInt(
				activity.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
		currentOrientation = getCurrentScreenOrientation(activity);
		activity.setRequestedOrientation(currentOrientation);
		return currentOrientation;
	}

	public static void unlockOrientation(Activity activity) {
		if (!isAutoRotateOn) {
			activity.setRequestedOrientation(currentOrientation);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		currentOrientation = -10;
	}

	/* decide the current orientation */
	public static int getCurrentScreenOrientation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		currentDpiFactor = 320.0f / dm.densityDpi;
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int orientation;
		// if the device's natural orientation is portrait:
		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				&& height > width
				|| (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
				&& width > height) {
			isDefaultPortrait = true;
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			default:
				Log.e("mymaps",
						"Unknown screen orientation. Defaulting to portrait.");
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			}
		}
		// if the device's natural orientation is landscape or if the device
		// is square:
		else {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			default:
				Log.e("mymaps",
						"Unknown screen orientation. Defaulting to landscape.");
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			}
		}
		return orientation;
	}

	/*
	 * change the azumith depending the orientation of the device
	 */
	public static double calibrateRotation(double azumith) {
		switch (currentOrientation) {
		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
			Log.e("mymaps", "SCREEN_ORIENTATION_PORTRAIT");
			if (isDefaultPortrait) {
				return azumith;
			} else {
				return azumith;
			}
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
			Log.e("mymaps", "SCREEN_ORIENTATION_REVERSE_PORTRAIT");
			if (isDefaultPortrait) {
				return azumith + 180.0f;
			} else {
				return azumith;
			}
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
			if (isDefaultPortrait) {
				return azumith + 90.0f;
			} else {
				return azumith - 90.0f;
			}
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
			if (isDefaultPortrait) {
				return azumith - 90.0f;
			} else {
				return azumith + 90.0f;
			}
		default:
			return 0;
		}
	}

	/*
	 * change the x, y coordinates depending the orientation of the device
	 */
	public static double[] calibrateXY(double[] values) {
		double x = values[0] * currentDpiFactor;
		double y = values[1] * currentDpiFactor;
		switch (currentOrientation) {
		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
			if (isDefaultPortrait) {
				return new double[] { x, -y };
			} else {
				return new double[] { -y, -x };
			}
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
			if (isDefaultPortrait) {
				return new double[] { -x, y };
			} else {
				return new double[] { y, x };
			}
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
			if (isDefaultPortrait) {
				return new double[] { y, x };
			} else {
				return new double[] { x, -y };
			}
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
			if (isDefaultPortrait) {
				return new double[] { -y, -x };
			} else {
				return new double[] { -x, y };
			}
		default:
			return new double[] { 0, 0 };
		}
	}

	/*
	 * match the voice command with a list of existing voice command and execute
	 * the code depending on the voice command
	 */
	public static void executeVoiceCommands(MapViewerActivity activity,
			String command) throws Exception {
		TextToSpeech textToSpeech = activity.textToSpeech;
		String cmd = command.toLowerCase(Locale.ENGLISH);
		if (cmd.contains("find")) {
			String[] target = cmd.split("find");
			if (target == null || target.length < 1 || target[1] == null) {
				return;
			}
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_location));
			((EditText) activity.findViewById(R.id.geocoding_address_id))
					.setText(target[1]);
			((Button) activity.findViewById(R.id.geocoding_search_id))
					.performClick();
			textToSpeech.speak(activity.getString(R.string.voice_command_find)
					+ target[1], TextToSpeech.QUEUE_FLUSH, null);
		} else if (cmd.equals("compass")) {
			if (!activity.loadWebMapFragment.compassControl.isChecked()) {
				activity.loadWebMapFragment.compassControl.performClick();
			}
			activity.loadWebMapFragment.compassIndicator.performClick();
			if (activity.loadWebMapFragment.compassIndicator.isChecked()) {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_compass_on),
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_compass_off),
						TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (cmd.equals("light")) {
			if (!activity.loadWebMapFragment.lightSensorControl.isChecked()) {
				activity.loadWebMapFragment.lightSensorControl.performClick();
			}
			activity.loadWebMapFragment.lightSensorIndicator.performClick();
			if (activity.loadWebMapFragment.lightSensorIndicator.isChecked()) {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_light_on),
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_light_off),
						TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (cmd.equals("gyro")) {
			if (!activity.loadWebMapFragment.gyroControl.isChecked()) {
				activity.loadWebMapFragment.gyroControl.performClick();
			}
			activity.loadWebMapFragment.gyroIndicator.performClick();
			if (activity.loadWebMapFragment.lightSensorIndicator.isChecked()) {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_gyro_off),
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_gyro_on),
						TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (cmd.equals("gps")) {
			if (!activity.loadWebMapFragment.gpsControl.isChecked()) {
				activity.loadWebMapFragment.gpsControl.performClick();
			}
			activity.loadWebMapFragment.gpsIndicator.performClick();
		} else if (cmd.equals("my location") || cmd.equals("where am i")) {
			activity.findViewById(R.id.geocoding_my_loc_id).performClick();
			textToSpeech.speak(
					activity.getString(R.string.voice_command_my_location),
					TextToSpeech.QUEUE_FLUSH, null);
		} else if (cmd.equals("extent")) {
			activity.findViewById(R.id.geocoding_default_extent_id)
					.performClick();
			textToSpeech.speak(
					activity.getString(R.string.voice_command_extent),
					TextToSpeech.QUEUE_FLUSH, null);
		} else if (cmd.equals("nfc")) {
			if (!activity.loadWebMapFragment.nfcControl.isChecked()) {
				activity.loadWebMapFragment.nfcControl.performClick();
			}
			activity.loadWebMapFragment.nfcControl.performClick();
			if (activity.loadWebMapFragment.nfcControl.isChecked()) {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_nfc_on),
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(
						activity.getString(R.string.voice_command_nfc_off),
						TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (cmd.equals("base map")) {
			MapControlWidget.switchToNextBasemap(activity);
		} else if (cmd.equals("layers")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_layers));
		} else if (cmd.equals("location")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_location));
		} else if (cmd.equals("bookmark")) {
			if (!Status.isBookmarkAvailable) {
				activity.onOptionsItemSelected((MenuItem) activity.menu
						.findItem(R.id.action_bookmarks));
			} else {
				textToSpeech.speak("sorry, there is no bookmark available",
						TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (cmd.equals("about")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_about));
		} else if (cmd.equals("description")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_description));
		} else if (cmd.equals("sensors")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu
					.findItem(R.id.action_settings));
		} else {
			Utility.toast(activity, "Can't understand " + cmd);
			textToSpeech.speak("sorry, I can't understand " + cmd,
					TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}

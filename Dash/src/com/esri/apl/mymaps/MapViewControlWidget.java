package com.esri.apl.mymaps;

import java.util.Locale;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Point;

public class MapViewControlWidget {

	private static boolean isAutoRotateOn = false;
	private static int currentOrientation = -10;
	private static float currentDpiFactor = 0.0f;
	private static boolean isDefaultPortrait = false;

	public static boolean panMap(MapView mapView, double[] values, MapViewerActivity activity) {
		double[] calibratedXY = calibrateXY(values);
		Point currentScreenCenter = mapView.toScreenPoint(mapView.getCenter());
		Point newScreenCenter = new Point(currentScreenCenter.getX() + calibratedXY[1], currentScreenCenter.getY() + calibratedXY[0]);
		mapView.centerAt(mapView.toMapPoint(newScreenCenter), false);
		try {
			activity.messageBuilder(BluetoothFragment.EXTENT_CHANGE, new double[] { mapView.getCenter().getX(), mapView.getCenter().getY(), mapView.getScale(),
					mapView.getRotationAngle() });
		} catch (Exception e) {

		}
		return true;
	}

	public static double rotateMap(final MapView mapView, double currentAzumith, final MapViewerActivity activity) {
		// mapView.setRotationAngle(-currentAzumith, mapView.getCenter(), true);
		double degree = calibrateRotation(currentAzumith);
		mapView.setRotationAngle(degree, mapView.getCenter(), true);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				try {
					activity.messageBuilder(BluetoothFragment.EXTENT_CHANGE,
							new double[] { mapView.getCenter().getX(), mapView.getCenter().getY(), mapView.getScale(), mapView.getRotationAngle() });
				} catch (Exception e) {

				}
			}
		}, 1000);
		return -degree;
	}

	public static boolean restoreRotatedMap(final MapView mapView, final MapViewerActivity activity) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				try {
					mapView.setRotationAngle(0, mapView.getCenter(), true);
					activity.messageBuilder(BluetoothFragment.EXTENT_CHANGE,
							new double[] { mapView.getCenter().getX(), mapView.getCenter().getY(), mapView.getScale(), mapView.getRotationAngle() });
				} catch (Exception e) {

				}
			}
		}, 1000);
		return true;
	}

	public static boolean setBrightness(Activity activity, float brightness, boolean isAuto) {
		try {
			if (!isAuto) {
				int brightnessMode = android.provider.Settings.System.getInt(activity.getContentResolver(),
						android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);

				if (brightnessMode == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
					android.provider.Settings.System.putInt(activity.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
							android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
				}
				WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
				layoutParams.screenBrightness = (int) (brightness * 255);
				activity.getWindow().setAttributes(layoutParams);
			} else {
				android.provider.Settings.System.putInt(activity.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
						android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static int lockOrientation(Activity activity) {
		isAutoRotateOn = (android.provider.Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
		if (isAutoRotateOn) {
			// Toast.makeText(activity, "Auto Rotate Disabled.",
			// Toast.LENGTH_SHORT).show();
		}
		currentOrientation = getCurrentScreenOrientation(activity);
		activity.setRequestedOrientation(currentOrientation);
		return currentOrientation;
	}

	public static void unlockOrientation(Activity activity) {
		if (!isAutoRotateOn) {
			activity.setRequestedOrientation(currentOrientation);
		} else {
			// Toast.makeText(activity, "Auto Rotate Enabled.",
			// Toast.LENGTH_SHORT).show();
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		currentOrientation = -10;
	}

	public static int getCurrentScreenOrientation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		currentDpiFactor = 320.0f / dm.densityDpi;
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int orientation;
		// if the device's natural orientation is portrait:
		if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width
				|| (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
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
				Log.e("mymaps", "Unknown screen orientation. Defaulting to portrait.");
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
				Log.e("mymaps", "Unknown screen orientation. Defaulting to landscape.");
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			}
		}
		return orientation;
	}

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
			Log.e("mymaps", "Screen Orientation not found");
			return new double[] { 0, 0 };
		}
	}

	public static void changeBasemap(Activity activity, String name) {
		if (activity == null) {
			return;
		}
		RadioGroup rg = (RadioGroup) activity.findViewById(R.id.basemap_switch);
		if (rg == null) {
			return;
		}
		int childCount = rg.getChildCount();
		Log.i("mymaps", "changeBasemap childCount: " + Integer.toString(childCount));
		for (int i = 0; i < childCount; i++) {
			if (name == null) {
				if (((RadioButton) rg.getChildAt(i)).isChecked()) {
					if (i == childCount - 1) {
						((RadioButton) rg.getChildAt(0)).performClick();
						Log.i("mymaps", "rg.getChildAt(0)");
						break;
					} else {
						((RadioButton) rg.getChildAt(i + 1)).performClick();
						Log.i("mymaps", "rg.getChildAt(" + Integer.toString(i + 1) + ")");
						break;
					}
				}
			} else {
				if (((RadioButton) rg.getChildAt(i)).getText().equals(name)) {
					((RadioButton) rg.getChildAt(i)).performClick();
				}
			}
		}
	}

	public static void executeVoiceCommands(MapViewerActivity activity, String cmd, TextToSpeech textToSpeech) {
		String command = cmd.toLowerCase(Locale.ENGLISH);
		if (command.contains("find")) {
			String[] target = command.split("find");
			if (target == null || target.length < 1 || target[1] == null) {
				return;
			}
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_location));
			((EditText) activity.findViewById(R.id.geocoding_address)).setText(target[1]);
			((Button) activity.findViewById(R.id.geocoding_confirm)).performClick();
			textToSpeech.speak(activity.getString(R.string.voice_command_find) + target[1], TextToSpeech.QUEUE_FLUSH, null);
		} else if (command.equals("compass")) {
			if (!activity.compassControl.isChecked()) {
				activity.compassControl.performClick();
			}
			activity.compassIndicator.performClick();
			if (activity.compassIndicator.isChecked()) {
				textToSpeech.speak(activity.getString(R.string.voice_command_compass_on), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(activity.getString(R.string.voice_command_compass_off), TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (command.equals("light")) {
			if (!activity.lightSensorControl.isChecked()) {
				activity.lightSensorControl.performClick();
			}
			activity.lightSensorIndicator.performClick();
			if (activity.lightSensorIndicator.isChecked()) {
				textToSpeech.speak(activity.getString(R.string.voice_command_light_on), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(activity.getString(R.string.voice_command_light_off), TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (command.equals("gyro")) {
			if (!activity.gyroControl.isChecked()) {
				activity.gyroControl.performClick();
			}
			activity.gyroIndicator.performClick();
			if (activity.lightSensorIndicator.isChecked()) {
				textToSpeech.speak(activity.getString(R.string.voice_command_gyro_off), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(activity.getString(R.string.voice_command_gyro_on), TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (command.equals("gps")) {
			if (!activity.gpsControl.isChecked()) {
				activity.gpsControl.performClick();
			}
			activity.gpsIndicator.performClick();
		} else if (command.equals("my location") || command.equals("where am i")) {
			activity.findViewById(R.id.my_loc).performClick();
			textToSpeech.speak(activity.getString(R.string.voice_command_my_location), TextToSpeech.QUEUE_FLUSH, null);
		} else if (command.equals("extent")) {
			activity.findViewById(R.id.default_view).performClick();
			textToSpeech.speak(activity.getString(R.string.voice_command_extent), TextToSpeech.QUEUE_FLUSH, null);
		} else if (command.equals("nfc")) {
			if (!activity.nfcControl.isChecked()) {
				activity.nfcControl.performClick();
			}
			activity.nfcSensorIndicator.performClick();
			if (activity.nfcSensorIndicator.isChecked()) {
				textToSpeech.speak(activity.getString(R.string.voice_command_nfc_on), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				textToSpeech.speak(activity.getString(R.string.voice_command_nfc_off), TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (command.equals("voice")) {
			if (!activity.voiceControl.isChecked()) {
				activity.voiceControl.performClick();
			}
			activity.voiceSensorIndicator.performClick();
			textToSpeech.speak(activity.getString(R.string.voice_command_voice_off), TextToSpeech.QUEUE_FLUSH, null);
		} else if (command.equals("base map")) {
			changeBasemap(activity, null);
		} else if (command.equals("layers")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_layers));
		} else if (command.equals("location")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_location));
		} else if (command.equals("bookmark")) {
			if (activity.bookmarks != null && activity.bookmarks.size() > 0) {
				activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_bookmarks));
			} else {
				textToSpeech.speak("sorry, there is no bookmark available", TextToSpeech.QUEUE_FLUSH, null);
			}
		} else if (command.equals("about")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_about));
		} else if (command.equals("description")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_description));
		} else if (command.equals("sensors")) {
			activity.onOptionsItemSelected((MenuItem) activity.menu.findItem(R.id.action_settings));
		} else {
			activity.toast("Can't understand " + command);
			textToSpeech.speak("sorry, I can't understand " + command, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}

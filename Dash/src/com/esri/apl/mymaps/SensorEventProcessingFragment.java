package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.esri.apl.mymaps.Status.SensorBehavior;
import com.esri.apl.mymaps.Status.SensorType;

public class SensorEventProcessingFragment extends Fragment implements SensorEventListener, RecognitionListener {
	private OnSensorChangeListener listener = null;
	private SensorManager sensorManager = null;
	private Sensor lightSensor = null, proximitySensor = null, accelerometerSensor = null, magneticfieldSensor = null, gyroSensor = null;
	public static Boolean isCompassEnabled = false, isLightSensorEnabled = false, isProximitySensorEnabled = false, isGyroscopeEnabled = false,
			isGPSEnabled = false, isVoiceEnabled = false;

	private SpeechRecognizer speechRecognizer;

	private Timer positionTimer = null;
	private Timer lightTimer = null;
	private Handler handler = new Handler();
	private static long lastUpdated = System.currentTimeMillis();

	private static int lightSensorTimeInterval = 1000;
	private static float weakLightThreshHold = 10;
	private static float currentLuminance = 0;
	private static boolean inWeakLight = false;
	private static float defaultLux = 1;

	private int gyroSensitivity = 5;
	private static double gyroThreshold = 0.1;
	private static float shakeThreshHold = 8;
	private static float acceleration = 0; // acceleration apart from gravity
	private static float currentAcceleration = SensorManager.GRAVITY_EARTH;
	private static float lastAcceleration = SensorManager.GRAVITY_EARTH;

	private static int positioningSensorTimeInterval = 10;
	private static double currentAzumith = 0, currentPitch = 0, currentRoll = 0;
	private static double azumithThreshold = 1;

	// angular speeds from gyro
	private float[] gyro = new float[3];

	// rotation matrix from gyro data
	private float[] gyroMatrix = new float[9];

	// orientation angles from gyro matrix
	private float[] gyroOrientation = new float[3];

	// magnetic field vector
	private float[] magnet = new float[3];

	// accelerometer vector
	private float[] accel = new float[3];

	// orientation angles from accel and magnet
	private float[] accMagOrientation = new float[3];

	// final orientation angles from sensor fusion
	private float[] fusedOrientation = new float[3];

	// accelerometer and magnetometer based rotation matrix
	private float[] rotationMatrix = new float[9];

	public static final float EPSILON = 0.000000001f;
	// private static final float NS2S = 1.0f / 1000000000.0f;
	private static final float NS2S = 1.0f / 1000000000000.0f;
	private float timestamp;
	private boolean initState = true;

	public static final int TIME_CONSTANT = 30;
	public static final float FILTER_COEFFICIENT = 0.98f;

	public interface OnSensorChangeListener {
		public void onSensorChange(SensorBehavior sensorBehavior, Bundle values);
	}

	public void changeGyroSensivity(int sensitivity) {
		this.gyroSensitivity = sensitivity;
	}

	public boolean toggleSensors(SensorType sensorType, boolean isEnabling) {
		switch (sensorType) {
		case compass:
			if (!isEnabling && isGyroscopeEnabled) {
				isCompassEnabled = false;
				currentAzumith = 0;
				return true;
			}
			if (toggleSensor(new Sensor[] { accelerometerSensor, magneticfieldSensor, gyroSensor }, isEnabling)) {
				isCompassEnabled = isEnabling;
				if (!isGyroscopeEnabled && isCompassEnabled) {
					positionTimer = new Timer();
					positionTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 500, positioningSensorTimeInterval);
					listener.onSensorChange(SensorBehavior.PositionSensorsOn, new Bundle());
				} else if (!isGyroscopeEnabled && !isCompassEnabled) {
					currentAzumith = 0;
					pausePositionSensorUpdate();
					listener.onSensorChange(SensorBehavior.PositionSensorsOff, new Bundle());
				}
				return true;
			} else {
				return false;
			}
		case gyro:
			if (!isEnabling && isCompassEnabled) {
				isGyroscopeEnabled = false;
				return true;
			}
			if (toggleSensor(new Sensor[] { accelerometerSensor, magneticfieldSensor, gyroSensor }, isEnabling)) {
				isGyroscopeEnabled = isEnabling;
				if (!isCompassEnabled && isGyroscopeEnabled) {
					positionTimer = new Timer();
					positionTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 500, positioningSensorTimeInterval);
					listener.onSensorChange(SensorBehavior.PositionSensorsOn, new Bundle());
				} else if (!isGyroscopeEnabled && !isCompassEnabled) {
					pausePositionSensorUpdate();
					listener.onSensorChange(SensorBehavior.PositionSensorsOff, new Bundle());
				}
				return true;
			} else {
				return false;
			}
		case light:
			if (toggleSensor(new Sensor[] { lightSensor }, isEnabling)) {
				isLightSensorEnabled = isEnabling;
				if (isLightSensorEnabled) {
					Bundle args = new Bundle();
					args.putFloat(Status.LIGHT, defaultLux);
					listener.onSensorChange(SensorBehavior.LightSensorOn, args);
					// lightTimer = new Timer();
					// lightTimer.scheduleAtFixedRate(new
					// calculateLightChangeTask(), 500,
					// lightSensorTimeInterval);
				} else {
					listener.onSensorChange(SensorBehavior.LightSensorOff, new Bundle());
					pauseLightSensorUpdate();
				}
				return true;
			} else {
				return false;
			}
		case proximity:
			if (toggleSensor(new Sensor[] { proximitySensor }, isEnabling)) {
				isProximitySensorEnabled = isEnabling;
				return true;
			} else {
				return false;
			}
		case voice:
			if (setupSpeechInput(isEnabling)) {
				isVoiceEnabled = isEnabling;
				return true;
			} else {
				return false;
			}
		case gps:

			break;
		default:
			break;
		}
		return true;
	}

	private boolean toggleSensor(Sensor[] sensors, boolean isEnabling) {
		for (Sensor sensor : sensors) {
			if (sensor == null) {
				return false;
			}
		}
		if (isEnabling) {
			for (Sensor sensor : sensors) {
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
			}
		} else {
			for (Sensor sensor : sensors) {
				sensorManager.unregisterListener(this, sensor);
			}
		}
		return true;
	}

	private boolean setupSpeechInput(boolean isEnabling) {
		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getActivity().getString(R.string.voice_command_tip));
		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
		startActivityForResult(listenIntent, 999);

		// if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
		// return false;
		// }
		// if (isEnabling) {
		// try {
		// textToSpeech = new TextToSpeech(getActivity(), new OnInitListener() {
		// public void onInit(int status) {
		// textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
		// }
		// });
		// speechRecognizer =
		// SpeechRecognizer.createSpeechRecognizer(getActivity());
		// speechRecognizer.setRecognitionListener(this);
		// startActivityForResult(intent, requestCode)
		// //
		// speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getActivity()));
		// } catch (Exception e) {
		// return false;
		// }
		// } else {
		// speechRecognizer.cancel();
		// textToSpeech.stop();
		// textToSpeech.shutdown();
		// }
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init sensors
		sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticfieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}

	// calculates orientation angles from accelerometer and magnetometer output
	private void calculateAccMagOrientation() {
		if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}

	// This function is borrowed from the Android reference
	// at
	// http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
	private void getRotationVectorFromGyro(float[] gyroValues, float[] deltaRotationVector, float timeFactor) {
		float[] normValues = new float[3];

		// Calculate the angular speed of the sample
		float omegaMagnitude = (float) Math.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2]);

		// Normalize the rotation vector if it's big enough to get the axis
		if (omegaMagnitude > EPSILON) {
			normValues[0] = gyroValues[0] / omegaMagnitude;
			normValues[1] = gyroValues[1] / omegaMagnitude;
			normValues[2] = gyroValues[2] / omegaMagnitude;
		}

		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
		float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

	// This function performs the integration of the gyroscope data.
	// It writes the gyroscope based orientation into gyroOrientation.
	private void gyroFunction(SensorEvent event) {
		// don't start until first accelerometer/magnetometer orientation has
		// been acquired
		if (accMagOrientation == null)
			return;

		// initialisation of the gyroscope based rotation matrix
		if (initState) {
			float[] initMatrix = new float[9];
			initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
			float[] test = new float[3];
			SensorManager.getOrientation(initMatrix, test);
			gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
			initState = false;
		}

		// copy the new gyro values into the gyro array
		// convert the raw gyro data into a rotation vector
		float[] deltaVector = new float[4];
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			System.arraycopy(event.values, 0, gyro, 0, 3);
			getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
		}

		// measurement done, save current time for next interval
		timestamp = event.timestamp;

		// convert rotation vector into rotation matrix
		float[] deltaMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

		// apply the new rotation interval on the gyroscope based rotation
		// matrix
		gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

		// get the gyroscope based orientation from the rotation matrix
		SensorManager.getOrientation(gyroMatrix, gyroOrientation);
	}

	private float[] getRotationMatrixFromOrientation(float[] o) {
		float[] xM = new float[9];
		float[] yM = new float[9];
		float[] zM = new float[9];

		float sinX = (float) Math.sin(o[1]);
		float cosX = (float) Math.cos(o[1]);
		float sinY = (float) Math.sin(o[2]);
		float cosY = (float) Math.cos(o[2]);
		float sinZ = (float) Math.sin(o[0]);
		float cosZ = (float) Math.cos(o[0]);

		// rotation about x-axis (pitch)
		xM[0] = 1.0f;
		xM[1] = 0.0f;
		xM[2] = 0.0f;
		xM[3] = 0.0f;
		xM[4] = cosX;
		xM[5] = sinX;
		xM[6] = 0.0f;
		xM[7] = -sinX;
		xM[8] = cosX;

		// rotation about y-axis (roll)
		yM[0] = cosY;
		yM[1] = 0.0f;
		yM[2] = sinY;
		yM[3] = 0.0f;
		yM[4] = 1.0f;
		yM[5] = 0.0f;
		yM[6] = -sinY;
		yM[7] = 0.0f;
		yM[8] = cosY;

		// rotation about z-axis (azimuth)
		zM[0] = cosZ;
		zM[1] = sinZ;
		zM[2] = 0.0f;
		zM[3] = -sinZ;
		zM[4] = cosZ;
		zM[5] = 0.0f;
		zM[6] = 0.0f;
		zM[7] = 0.0f;
		zM[8] = 1.0f;

		// rotation order is y, x, z (roll, pitch, azimuth)
		float[] resultMatrix = matrixMultiplication(xM, yM);
		resultMatrix = matrixMultiplication(zM, resultMatrix);
		return resultMatrix;
	}

	private float[] matrixMultiplication(float[] A, float[] B) {
		float[] result = new float[9];

		result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
		result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
		result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

		result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
		result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
		result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

		result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
		result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
		result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

		return result;
	}

	class calculateFusedOrientationTask extends TimerTask {
		public void run() {
			float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

			// azimuth
			if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
			} else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
			} else {
				fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
			}

			// pitch
			if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
			} else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
			} else {
				fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
			}

			// roll
			if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
			} else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
			} else {
				fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
			}

			// overwrite gyro matrix and orientation with fused orientation
			// to comensate gyro drift
			gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
			System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

			if (isCompassEnabled) {
				if (System.currentTimeMillis() - lastUpdated > 500 && Math.abs(currentAzumith - Math.toDegrees(fusedOrientation[0])) > azumithThreshold) {
					Bundle args = new Bundle();
					args.putDouble(Status.ROTATION, Math.toDegrees(fusedOrientation[0]));
					listener.onSensorChange(SensorBehavior.Rotate, args);
					lastUpdated = System.currentTimeMillis();
					currentAzumith = Math.toDegrees(fusedOrientation[0]);
				}
			}
			if (isGyroscopeEnabled) {
				if (Math.sqrt(fusedOrientation[1] * fusedOrientation[1] + fusedOrientation[2] * fusedOrientation[2]) > gyroThreshold) {
					Bundle args = new Bundle();
					args.putDoubleArray(Status.DRIFT, new double[] { fusedOrientation[1] * gyroSensitivity, fusedOrientation[2] * gyroSensitivity });
					listener.onSensorChange(SensorBehavior.Lean, args);
				}
			}
		}
	}

	private boolean isShaking(float[] values) {
		float x = values[0];
		float y = values[1];
		float z = values[2];
		lastAcceleration = currentAcceleration;
		currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
		float delta = currentAcceleration - lastAcceleration;
		acceleration = acceleration * 0.9f + delta; // perform low-cut filter
		if (acceleration > shakeThreshHold) {
			return true;
		}
		return false;
	}

	class calculateLightChangeTask extends TimerTask {
		public void run() {
			if (isLightSensorEnabled) {
				if (currentLuminance > weakLightThreshHold && inWeakLight) {
					inWeakLight = false;
					listener.onSensorChange(SensorBehavior.DayLight, new Bundle());
				} else if (currentLuminance < weakLightThreshHold && !inWeakLight) {
					inWeakLight = true;
					listener.onSensorChange(SensorBehavior.WeakLight, new Bundle());
				}
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			if (isGyroscopeEnabled) {
				if (isShaking(event.values)) {
					listener.onSensorChange(SensorBehavior.Shake, new Bundle());
					isGyroscopeEnabled = false;
					handler.postDelayed(new Runnable() {
						public void run() {
							isGyroscopeEnabled = true;
						}
					}, 1000);
				}
			}
			if (isCompassEnabled || isGyroscopeEnabled) {
				System.arraycopy(event.values, 0, accel, 0, 3);
				calculateAccMagOrientation();
			}
			break;

		case Sensor.TYPE_GYROSCOPE:
			if (isCompassEnabled || isGyroscopeEnabled) {
				gyroFunction(event);
			}
			break;

		case Sensor.TYPE_MAGNETIC_FIELD:
			if (isCompassEnabled || isGyroscopeEnabled) {
				System.arraycopy(event.values, 0, magnet, 0, 3);
			}
			break;

		case Sensor.TYPE_LIGHT:
			if (isLightSensorEnabled) {
				currentLuminance = event.values[0];
			}
			break;

		case Sensor.TYPE_PROXIMITY:
			if (isProximitySensorEnabled) {
				Log.i("mymaps", Float.toString(event.values[0]));
				if (event.values[0] == 0) {
					listener.onSensorChange(SensorBehavior.Near, new Bundle());
				}
			}
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnSensorChangeListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	public void pausePositionSensorUpdate() {
		if (positionTimer != null) {
			positionTimer.cancel();
		}
	}

	public void pauseLightSensorUpdate() {
		if (lightTimer != null) {
			lightTimer.cancel();
		}
	}

	public void pauseVoiceSensorUpdate() {
		// TODO stop voice input
	}

	public void pauseGPSUpdate() {
		// TODO stop gps
	}

	@Override
	public void onDetach() {
		super.onDetach();
		sensorManager.unregisterListener(this);
		listener = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		pausePositionSensorUpdate();
		pauseLightSensorUpdate();
	}

	// Speech Recognition Listener
	public void onBeginningOfSpeech() {
		Log.d("Speech", "onBeginningOfSpeech");
		listener.onSensorChange(SensorBehavior.SpeechBegin, new Bundle());
	}

	public void onBufferReceived(byte[] buffer) {
		Log.d("Speech", "onBufferReceived");
	}

	public void onEndOfSpeech() {
		Log.d("Speech", "onEndOfSpeech");
		listener.onSensorChange(SensorBehavior.SpeechEnd, new Bundle());
	}

	public void onError(int error) {
		Log.d("Speech", "onError");
		listener.onSensorChange(SensorBehavior.SpeechError, new Bundle());
	}

	public void onEvent(int eventType, Bundle params) {
		Log.d("Speech", "onEvent");
	}

	public void onPartialResults(Bundle partialResults) {
		Log.d("Speech", "onPartialResults");
	}

	public void onReadyForSpeech(Bundle params) {
		Log.d("Speech", "onReadyForSpeech");
	}

	public void onResults(Bundle results) {
		// Log.d("Speech", "onResults");
		// StringBuilder sb = new StringBuilder();
		// ArrayList<String> strlist =
		// results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		// for (int i = 0; i < strlist.size(); i++) {
		// Log.d("Speech", "result=" + strlist.get(i));
		// sb.append(strlist.get(i) + ", ");
		// }
		// textToSpeech.speak("I got: " + sb.toString(),
		// TextToSpeech.QUEUE_FLUSH, null);
		// listener.onSensorChange(SensorBehavior.SpeechResult, new Bundle());
	}

	public void onRmsChanged(float rmsdB) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
			StringBuilder sb = new StringBuilder();
			ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Log.i("Speech", "result size =" + suggestedWords.size());
			for (int i = 0; i < suggestedWords.size(); i++) {
				Log.i("Speech", "result=" + suggestedWords.get(i));
				sb.append(suggestedWords.get(i) + ", ");
			}
			// textToSpeech = new TextToSpeech(getActivity(), null);
			// textToSpeech.speak("I got: " + sb.toString(),
			// TextToSpeech.QUEUE_FLUSH, null);
			voiceCommandProcessing(suggestedWords);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void voiceCommandProcessing(ArrayList<String> words) {
		// String topMatchCommand = null;
		// double topMatchScore = 0;
		// SimilarityTool st = new SimilarityTool();
		String[] commands = getActivity().getResources().getStringArray(R.array.voice_commands);
		Bundle args = new Bundle();
		for (String word : words) {
			for (String command : commands) {
				if (word.contains("find")) {
					args.putString(Status.VOICE_COMMAND, word);
					listener.onSensorChange(SensorBehavior.SpeechResult, args);
					return;
				} else if (word.equals(command)) {
					args.putString(Status.VOICE_COMMAND, command);
					listener.onSensorChange(SensorBehavior.SpeechResult, args);
					return;
				}
			}
		}
		args.putString(Status.VOICE_COMMAND, words.get(0));
		listener.onSensorChange(SensorBehavior.SpeechResult, args);
		// for (String word : words) {
		// for (String command : commands) {
		// Log.i("mymaps", "score: " + Double.toString(st.CompareStrings(word,
		// command)));
		// if (topMatchScore < st.CompareStrings(word, command)) {
		// topMatchCommand = command;
		// topMatchScore = st.CompareStrings(word, command);
		// }
		// }
		// }
		// Log.i("mymaps", "top command: " + topMatchCommand + " top score: " +
		// Double.toString(topMatchScore));
		// Bundle args = new Bundle();
		// if (topMatchScore > 0.7) {
		// args.putString(Status.VOICE_COMMAND, topMatchCommand);
		// listener.onSensorChange(SensorBehavior.SpeechResult, args);
		// } else {
		// if (words.size() >= 1) {
		// args.putString(Status.VOICE_COMMAND, words.get(0));
		// listener.onSensorChange(SensorBehavior.SpeechNoResult, args);
		// }
		// }
	}
}

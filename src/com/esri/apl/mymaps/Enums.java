package com.esri.apl.mymaps;

public class Enums {

	public static enum LongClickOption {
		Delete, DeleteAll, DeleteAndMoveToParent, DeleteAndMoveToRoot, Move
	}

	public enum SensorType {
		compass, gyro, light, proximity, voice, gps
	}

	public static enum SensorBehavior {
		// position sensors set
		PositionSensorsOn, PositionSensorsOff,
		// proximity
		Near,
		// gyro
		Lean, Shake,
		// compass
		Rotate,
		// light
		LightSensorOn, LightSensorOff,
		// voice
		SpeechBegin, SpeechEnd, SpeechError, SpeechResult, SpeechNoResult,
	}
}

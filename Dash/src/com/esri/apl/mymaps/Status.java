package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.UUID;

public class Status {

	public static boolean isLocated = false;
	public static boolean isWifiP2pEnabled = false;
	public static boolean isGroupOwner = false;
	public static boolean isGroupFormed = false;
	public static boolean isNFCAvailable = false;
	public static boolean MultipleLevelFoldersAllowed = false;
	public static String CurrentParent = null;
	public static int adjust = 0;
	public static int paddingEnd = 0;
	public static int gridPadding = 0;
	public static int itemsPerColumn = 0;
	public static ArrayList<Integer> PaddingItem = new ArrayList<Integer>();

	public static final String AppIdentifier = "MyMaps";

	public static final String VOICE_COMMAND = "voice_command";
	public static final String WEBMAP = "webmap";
	public static final String FOLDER = "folder";

	public static final String LIGHT = "light";
	public static final String ROTATION = "rotation";
	public static final String DRIFT = "drift";

	public enum ClickType {
		LongClick, Click
	}

	public enum LongClickOption {
		Delete, DeleteAll, DeleteAndMoveToParent, DeleteAndMoveToRoot, Move
	}

	public enum GridCellType {
		WebMap, Folder
	}

	public enum SensorType {
		compass, gyro, light, proximity, voice, gps
	}

	public enum SensorBehavior {
		// position sensors set
		PositionSensorsOn, PositionSensorsOff,
		// proximity
		Near,
		// gyro
		Lean, Shake,
		// compass
		Rotate,
		// light
		DayLight, WeakLight, LightSensorOn, LightSensorOff,
		// gps
		LocationChange,
		// voice
		SpeechBegin, SpeechEnd, SpeechError, SpeechResult, SpeechNoResult,
		// Voice
		Voice_Compass, Voice_Light, Voice_Gyro, Voice_GPS, Voice_My_Location, Voice_Default_Extent, Voice_NFC, Voice_Voice, Voice_Basemap, Voice_Layers, Voice_Location, Voice_Bookmark, Voice_About, Voice_Description, Voice_Sensors
	}

	public enum WiFiDirectBehavior {
		ShowDeviceDetails, ConnectRequest, Connected, Disconnect
	}

	public enum BluetoothBehavior {
		StarListen, StopListen, SendMessage
	}

	public enum BluetoothMessageType {
		RequestConnection
	}
}

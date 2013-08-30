package com.esri.apl.mymaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DBhelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "webmap_database";
	private static final String TABLE_NAME = "webmaps";
	private static final String COL_ID = "ID";
	private static final String COL_TITLE = "title";
	private static final String COL_OWNER = "owner";
	private static final String COL_DESCRIPTION = "description";
	private static final String COL_IMAGE = "image";
	private static final String COL_RATING = "rating";
	private static final String COL_DATE = "date";
	private static final String COL_ACCESS = "access";
	private static final String COL_PARENT = "parent";
	private static final String COL_TYPE = "type";
	// private static final String COL_COMPASS = "Compass";
	// private static final String COL_GYROSCOPE = "Gyroscope";
	// private static final String COL_GPS = "GPS";
	// private static final String COL_LIGHTSENSOR = "LightSensor";
	// private static final String COL_VOICE = "Voice";
	// private static final String[] FIELDS = { COL_ID, COL_TITLE, COL_OWNER,
	// COL_IMAGE, COL_DATE, COL_RATING, COL_DESCRIPTION, COL_NUM_VIEWED };

	private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	// private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
	// " (" + COL_ID + " text not null PRIMARY KEY," + COL_TITLE + " text," +
	// COL_OWNER
	// + " text," + COL_IMAGE + " blob," + COL_DATE + " integer," + COL_RATING +
	// " float," + COL_DESCRIPTION + " text," + COL_ACCESS + " text,"
	// + COL_FOLDER + " text," + COL_COMPASS + " numeric," + COL_GYROSCOPE +
	// " numeric," + COL_GPS + " numeric," + COL_LIGHTSENSOR + " numeric,"
	// + COL_VOICE + " numeric" + ");";
	private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " text not null PRIMARY KEY," + COL_TITLE + " text," + COL_OWNER
			+ " text," + COL_IMAGE + " blob," + COL_DATE + " integer," + COL_RATING + " float," + COL_DESCRIPTION + " text," + COL_ACCESS + " text,"
			+ COL_PARENT + " text," + COL_TYPE + " text" + ");";

	private DatabaseHelper webmapDbHelper;
	private SQLiteDatabase webmapDb;
	@SuppressWarnings("unused")
	private final Context context;

	public DBhelper(Context context) {
		this.context = context;
		webmapDbHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(DROP_TABLE);
			onCreate(db);
		}
	}

	public void reset() {
		try {
			webmapDb = webmapDbHelper.getWritableDatabase();
			synchronized (webmapDb) {
				webmapDbHelper.onUpgrade(this.webmapDb, 1, 1);
			}
		} finally {
			webmapDbHelper.close();
		}
	}

	// public boolean insert(String ID, String title, String owner, byte[]
	// image, Long date_modified, Float rating, String description, String
	// access,
	// String folder, Boolean isCompassEnabled, Boolean isLightSensorEnabled,
	// Boolean isGyroscopeEnabled, Boolean isGPSEnabled, Boolean isVoiceEnabled)
	// {
	public boolean insert(String ID, String title, String owner, byte[] image, Long date_modified, Float rating, String description, String access,
			String parent, String type) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, ID);
		values.put(COL_TITLE, title);
		values.put(COL_OWNER, owner);
		values.put(COL_IMAGE, image);
		values.put(COL_DATE, date_modified);
		values.put(COL_RATING, rating);
		values.put(COL_DESCRIPTION, description);
		values.put(COL_ACCESS, access);
		values.put(COL_PARENT, parent);
		values.put(COL_TYPE, type);
		// values.put(COL_COMPASS, isCompassEnabled);
		// values.put(COL_LIGHTSENSOR, isLightSensorEnabled);
		// values.put(COL_GYROSCOPE, isGyroscopeEnabled);
		// values.put(COL_VOICE, isVoiceEnabled);
		// values.put(COL_GPS, isGPSEnabled);
		try {
			webmapDb = webmapDbHelper.getWritableDatabase();
			synchronized (webmapDb) {
				webmapDb.replace(TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			webmapDbHelper.close();
		}
		return true;
	}

	public Cursor query(String ID) {
		Cursor cr = null;
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		sqb.setTables(TABLE_NAME);
		if (ID != null) {
			sqb.appendWhere(COL_ID + "='" + ID + "'");
		}
		try {
			webmapDb = webmapDbHelper.getReadableDatabase();
			cr = sqb.query(webmapDb, null, null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			cr = null;
		}
		return cr;
	}

	public boolean delete(String ID) {
		if (ID == null) {
			return false;
		}
		try {
			webmapDb = webmapDbHelper.getWritableDatabase();
			synchronized (webmapDb) {
				webmapDb.delete(TABLE_NAME, COL_ID + "='" + ID + "'", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			webmapDb.close();
		}
		return true;
	}
}

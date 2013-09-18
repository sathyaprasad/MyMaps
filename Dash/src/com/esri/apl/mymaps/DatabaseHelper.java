package com.esri.apl.mymaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DatabaseHelper {

	private static int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "mymaps_database";
	private static final String TABLE_NAME = "mymaps_table";
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

	private static final String DROP_TABLE = "DROP TABLE IF EXISTS "
			+ TABLE_NAME;
	private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + COL_ID + " text not null PRIMARY KEY," + COL_TITLE
			+ " text," + COL_OWNER + " text," + COL_IMAGE + " blob," + COL_DATE
			+ " integer," + COL_RATING + " float," + COL_DESCRIPTION + " text,"
			+ COL_ACCESS + " text," + COL_PARENT + " text," + COL_TYPE
			+ " text" + ");";

	private mymapsDatabaseHelper mymapsDatabaseHelper;
	private SQLiteDatabase mymapsDatabase;

	private final Context context;

	public DatabaseHelper(Context context) {
		this.context = context;
		mymapsDatabaseHelper = new mymapsDatabaseHelper(this.context);
	}

	private static class mymapsDatabaseHelper extends SQLiteOpenHelper {
		mymapsDatabaseHelper(Context context) {
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

	/* reset the database */
	public void reset() {
		try {
			mymapsDatabase = mymapsDatabaseHelper.getWritableDatabase();
			synchronized (mymapsDatabase) {
				mymapsDatabaseHelper.onUpgrade(mymapsDatabase,
						DATABASE_VERSION, DATABASE_VERSION++);
			}
		} finally {
			mymapsDatabaseHelper.close();
		}
	}

	/* insert a record into database */
	public boolean insert(String ID, String title, String owner, byte[] image,
			Long date_modified, Float rating, String description,
			String access, String parent, String type) {
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
		try {
			mymapsDatabase = mymapsDatabaseHelper.getWritableDatabase();
			synchronized (mymapsDatabase) {
				mymapsDatabase.replace(TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			mymapsDatabaseHelper.close();
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
			mymapsDatabase = mymapsDatabaseHelper.getReadableDatabase();
			cr = sqb.query(mymapsDatabase, null, null, null, null, null, null);
		} catch (Exception e) {
			cr = null;
		}
		return cr;
	}

	/* delete a record from database */
	public boolean delete(String ID) {
		if (ID == null) {
			return false;
		}
		try {
			mymapsDatabase = mymapsDatabaseHelper.getWritableDatabase();
			synchronized (mymapsDatabase) {
				mymapsDatabase.delete(TABLE_NAME, COL_ID + "='" + ID + "'",
						null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			mymapsDatabase.close();
		}
		return true;
	}
}

package com.esri.apl.mymaps;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WebMapProvider extends ContentProvider {

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

//	private Uri providerURI;
//	private Uri.Builder uriBuilder;
	private static final String AUTHORITY = "com.esri.apl.appdash.provider";
	private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DATABASE_NAME);

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID
					+ " text not null PRIMARY KEY," + COL_TITLE + " text,"
					+ COL_OWNER + " text," + COL_IMAGE + " blob," + COL_DATE
					+ " integer," + COL_RATING + " float," + COL_DESCRIPTION
					+ " text" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

	private static DatabaseHelper webmapDbHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// webmapDbHelper = new DatabaseHelper(getContext());
		SQLiteDatabase db = webmapDbHelper.getWritableDatabase();
		if (selection != null) {
			db.delete(TABLE_NAME, COL_ID + "='" + selection + "'", null);
		} else {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID
					+ " text not null PRIMARY KEY," + COL_TITLE + " text,"
					+ COL_OWNER + " text," + COL_IMAGE + " blob," + COL_DATE
					+ " integer," + COL_RATING + " float," + COL_DESCRIPTION
					+ " text" + ");");
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = webmapDbHelper.getWritableDatabase();
		synchronized (db) {
			long rowID = db.replace(TABLE_NAME, null, values);
			if (rowID > 0) {
				Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			} else {
				throw new SQLException("Fail to insert row into " + uri);
			}
		}
	}

	@Override
	public boolean onCreate() {

		webmapDbHelper = new DatabaseHelper(getContext());

		// SQLiteDatabase db = webmapDbHelper.getWritableDatabase();
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID
		// + " text not null PRIMARY KEY," + COL_TITLE + " text,"
		// + COL_OWNER + " text," + COL_IMAGE + " text" + ");");

//		uriBuilder = new Uri.Builder();
//		uriBuilder.authority(AUTHORITY);
//		uriBuilder.scheme("content");
//		providerURI = uriBuilder.build();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		if (selection != null) {
			qb.appendWhere(COL_ID + "='" + selection + "'");
		}
		SQLiteDatabase db = webmapDbHelper.getReadableDatabase();
		Cursor cr = qb.query(db, null, null, null, null, null, null);
		return cr;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}

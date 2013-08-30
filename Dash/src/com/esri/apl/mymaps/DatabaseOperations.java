package com.esri.apl.mymaps;

import java.io.ByteArrayOutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

public class DatabaseOperations {
	private ContentResolver mContentResolver;
	private Uri providerURI;
	private Uri.Builder uriBuilder;
	private static final String COL_ID = "ID";
	private static final String COL_TITLE = "title";
	private static final String COL_OWNER = "owner";
	private static final String COL_DESCRIPTION = "description";
	private static final String COL_IMAGE = "image";
	private static final String COL_RATING = "rating";
	private static final String COL_DATE = "date";
	private static final String AUTHORITY = "com.esri.apl.appdash.provider";

	public DatabaseOperations(ContentResolver mContentResolver) {
		this.mContentResolver = mContentResolver;
		uriBuilder = new Uri.Builder();
		uriBuilder.authority(AUTHORITY);
		uriBuilder.scheme("content");
		providerURI = uriBuilder.build();
	}

	public void Insert(String ID, String title, String owner, Bitmap image, Long date, Float rating, String description) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, out);
		
		ContentValues keyValueToInsert = new ContentValues();
		keyValueToInsert.put(COL_ID, ID);
		keyValueToInsert.put(COL_TITLE, title);
		keyValueToInsert.put(COL_OWNER, owner);
		keyValueToInsert.put(COL_IMAGE, out.toByteArray());
		keyValueToInsert.put(COL_DATE, date);
		keyValueToInsert.put(COL_RATING, rating);
		keyValueToInsert.put(COL_DESCRIPTION, description);

		mContentResolver.insert(providerURI, keyValueToInsert);
	}

	public Cursor Query(String ID) {
		return mContentResolver.query(providerURI, null, ID, null, null);
	}

	public void Delete(String ID) {
		mContentResolver.delete(providerURI, ID, null);
	}
}

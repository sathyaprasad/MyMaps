package com.esri.apl.mymaps;

import java.util.ArrayList;

public class MapRecord {

	public static ArrayList<byte[]> images;
	public static ArrayList<String> titles;
	public static ArrayList<String> IDs;
	public static ArrayList<String> owners;
	public static ArrayList<Long> dates;
	public static ArrayList<Float> ratings;
	public static ArrayList<String> descriptions;
	public static ArrayList<String> accesses;
	public static ArrayList<String> parents;
	public static ArrayList<String> types;
	// public static ArrayList<Boolean> compass;
	// public static ArrayList<Boolean> light_sensor;
	// public static ArrayList<Boolean> gyroscope;
	// public static ArrayList<Boolean> gps;
	// public static ArrayList<Boolean> voice;

	public static ArrayList<byte[]> imagesInCurrentFolder;
	public static ArrayList<String> titlesInCurrentFolder;
	public static ArrayList<String> IDsInCurrentFolder;
	public static ArrayList<String> ownersInCurrentFolder;
	public static ArrayList<Long> datesInCurrentFolder;
	public static ArrayList<Float> ratingsInCurrentFolder;
	public static ArrayList<String> descriptionsInCurrentFolder;
	public static ArrayList<String> accessesInCurrentFolder;
	public static ArrayList<String> parentsInCurrentFolder;
	public static ArrayList<String> typesInCurrentFolder;

	public static void initData() {
		images = new ArrayList<byte[]>();
		titles = new ArrayList<String>();
		IDs = new ArrayList<String>();
		owners = new ArrayList<String>();
		dates = new ArrayList<Long>();
		ratings = new ArrayList<Float>();
		descriptions = new ArrayList<String>();
		accesses = new ArrayList<String>();
		parents = new ArrayList<String>();
		types = new ArrayList<String>();
		// compass = new ArrayList<Boolean>();
		// light_sensor = new ArrayList<Boolean>();
		// gyroscope = new ArrayList<Boolean>();
		// gps = new ArrayList<Boolean>();
		// voice = new ArrayList<Boolean>();
	}

	public static void initDataInCurrentFolder() {
		imagesInCurrentFolder = new ArrayList<byte[]>();
		titlesInCurrentFolder = new ArrayList<String>();
		IDsInCurrentFolder = new ArrayList<String>();
		ownersInCurrentFolder = new ArrayList<String>();
		datesInCurrentFolder = new ArrayList<Long>();
		ratingsInCurrentFolder = new ArrayList<Float>();
		descriptionsInCurrentFolder = new ArrayList<String>();
		accessesInCurrentFolder = new ArrayList<String>();
		parentsInCurrentFolder = new ArrayList<String>();
		typesInCurrentFolder = new ArrayList<String>();
	}

	public static void insertSingleRecord(String ID, String title, String owner, byte[] image,
			Long date_modified, Float rating, String description, String access, String parent,
			String type) {
		IDs.add(ID);
		titles.add(title);
		owners.add(owner);
		images.add(image);
		dates.add(date_modified);
		ratings.add(rating);
		descriptions.add(description);
		accesses.add(access);
		parents.add(parent);
		types.add(type);
	}

	public static void removeSingleRecord(int index) {
		IDs.remove(index);
		titles.remove(index);
		owners.remove(index);
		images.remove(index);
		dates.remove(index);
		ratings.remove(index);
		descriptions.remove(index);
		accesses.remove(index);
		parents.remove(index);
		types.remove(index);
	}

	public static void removeSingleRecord(String ID) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.IDs.get(i).equals(ID)) {
				removeSingleRecord(i);
			}
		}
	}
}

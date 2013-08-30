package com.esri.apl.mymaps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.esri.android.map.MapView;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;

public class Utility {

	public static String getWebMapID(String customURL) {
		Log.i("nfc test", "nfc test " + customURL);
		String[] id = customURL.trim().split("mymaps://");
		String[] id2 = customURL.trim().split("webmap=");
		Pattern p = Pattern.compile("\\W");
		if (id.length == 2) {
			Matcher makeMatch = p.matcher(id[1]);
			if (makeMatch.find() == false) {
				return id[1];
			}
		}
		if (id2.length == 2) {
			Matcher makeMatch = p.matcher(id2[1]);
			if (makeMatch.find() == false) {
				return id2[1];
			}
		}
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	public static String timeFormatter(String format, Long time) {
		return new SimpleDateFormat(format).format(new Date(time));
	}

	public static View PopupToView(Popup popup, MapView mapView) {
		PopupContainer pc = new PopupContainer(mapView);
		pc.addPopup(popup);
		return pc.getPopupContainerView();
	}

	public static int pixelScaler(Context context, int size) {
		return (int) (context.getResources().getDisplayMetrics().density * size + 0.5f);
	}

	public static String scaleFormatter(double rawScale) {
		String result = null;
		if (rawScale < 1000000) {
			result = "Scale: 1:" + Integer.toString((int) rawScale / 1000) + "K";
		} else {
			result = "Scale: 1:" + Integer.toString((int) rawScale / 1000000) + "M";
		}
		return result;
	}

	public static Rect locateView(View v) {
		int[] loc_int = new int[2];
		if (v == null)
			return null;
		try {
			v.getLocationOnScreen(loc_int);
		} catch (NullPointerException npe) {
			return null;
		}
		Rect location = new Rect();
		location.left = loc_int[0];
		location.top = loc_int[1];
		location.right = location.left + v.getWidth();
		location.bottom = location.top + v.getHeight();
		return location;
	}

	public static String getParentFolder(String ID) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (ID.equals(MapRecord.IDs.get(i))) {
				return MapRecord.parents.get(i);
			}
		}
		return null;
	}

	public static String getDefaultFolderName(Context context) {
		// int i = 2;
		String folderName = context.getString(R.string.new_folder_default_name);
		// while (isFolderNameDuplicated(folderName)) {
		// folderName = folderName + " " + Integer.toString(i);
		// i++;
		// }
		return folderName;
	}

	public static boolean isFolderNameDuplicated(String folderName) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.types.get(i).equals(Status.FOLDER)) {
				if (MapRecord.IDs.get(i).equals(folderName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void getMapRecordInCurrentFolder() {
		MapRecord.initDataInCurrentFolder();
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if ((Status.CurrentParent == null && MapRecord.parents.get(i) == null)
					|| (Status.CurrentParent != null && MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(Status.CurrentParent))) {
				if (MapRecord.types.get(i).equals(Status.WEBMAP)) {
					MapRecord.IDsInCurrentFolder.add(MapRecord.IDs.get(i));
					MapRecord.titlesInCurrentFolder.add(MapRecord.titles.get(i));
					MapRecord.ownersInCurrentFolder.add(MapRecord.owners.get(i));
					MapRecord.imagesInCurrentFolder.add(MapRecord.images.get(i));
					MapRecord.datesInCurrentFolder.add(MapRecord.dates.get(i));
					MapRecord.ratingsInCurrentFolder.add(MapRecord.ratings.get(i));
					MapRecord.descriptionsInCurrentFolder.add(MapRecord.descriptions.get(i));
					MapRecord.accessesInCurrentFolder.add(MapRecord.accesses.get(i));
					MapRecord.parentsInCurrentFolder.add(MapRecord.parents.get(i));
					MapRecord.typesInCurrentFolder.add(MapRecord.types.get(i));
				}
			}
		}
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if ((Status.CurrentParent == null && MapRecord.parents.get(i) == null)
					|| (Status.CurrentParent != null && MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(Status.CurrentParent))) {
				if (MapRecord.types.get(i).equals(Status.FOLDER)) {
					MapRecord.IDsInCurrentFolder.add(MapRecord.IDs.get(i));
					MapRecord.titlesInCurrentFolder.add(MapRecord.titles.get(i));
					MapRecord.ownersInCurrentFolder.add(MapRecord.owners.get(i));
					MapRecord.imagesInCurrentFolder.add(MapRecord.images.get(i));
					MapRecord.datesInCurrentFolder.add(MapRecord.dates.get(i));
					MapRecord.ratingsInCurrentFolder.add(MapRecord.ratings.get(i));
					MapRecord.descriptionsInCurrentFolder.add(MapRecord.descriptions.get(i));
					MapRecord.accessesInCurrentFolder.add(MapRecord.accesses.get(i));
					MapRecord.parentsInCurrentFolder.add(MapRecord.parents.get(i));
					MapRecord.typesInCurrentFolder.add(MapRecord.types.get(i));
				}
			}
		}
	}

	public static boolean insertToDatabase(DBhelper dbhelper, String ID, String title, String owner, byte[] image, Long date_modified, Float rating,
			String description, String access, String parent, String type) {
		try {
			return dbhelper.insert(ID, title, owner, image, date_modified, rating, description, access, parent, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String findParentOfParent(String parent) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.types.get(i).equals(Status.FOLDER) && (MapRecord.IDs.get(i).equals(parent))) {
				return MapRecord.parents.get(i);
			}
		}
		return null;
	}

	public static String getFolderPath() {
		String path = "";
		String parent = Status.CurrentParent;
		while (parent != null) {
			if (path.isEmpty()) {
				path += parent;
			} else {
				path = parent + " / " + path;
			}
			parent = findParentOfParent(parent);
		}
		if (!path.isEmpty()) {
			path = " / " + path;
		}
		return path;
	}

	public static String buildActionBarTitle(Context context) {
		String title = "  " + context.getString(R.string.app_name);
		return title + getFolderPath();
	}

	public static ArrayList<String> getFolderList(String currentFolder, boolean isWebMap) {
		ArrayList<String> childList = Utility.getChildList(currentFolder);
		ArrayList<String> folderList = new ArrayList<String>();
		if (Status.CurrentParent != null) {
			folderList.add("Home");
		}
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.types.get(i).equals(Status.FOLDER) && !MapRecord.IDs.get(i).equals(currentFolder)) {
				if (isWebMap) {
					folderList.add(MapRecord.IDs.get(i));
				} else if (Status.MultipleLevelFoldersAllowed) {
					boolean isChild = false;
					for (String child : childList) {
						if (MapRecord.IDs.get(i).equals(child)) {
							childList.remove(child);
							isChild = true;
							break;
						}
					}
					if (!isChild) {
						folderList.add(MapRecord.IDs.get(i));
					}
				}
			}
		}
		return folderList;
	}

	public static ArrayList<String> getChildList(String ID) {
		ArrayList<String> childList = new ArrayList<String>();
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(ID)) {
				childList.add(MapRecord.IDs.get(i));
				if (containChild(MapRecord.IDs.get(i))) {
					ArrayList<String> tmp = getChildList(MapRecord.IDs.get(i));
					for (String child : tmp) {
						childList.add(child);
					}
				}
			}
		}
		Log.i("mymaps", "child list: " + childList.toString());
		return childList;
	}

	public static boolean containChild(String ID) {
		for (String parent : MapRecord.parents) {
			if (parent != null && parent.equals(ID)) {
				return true;
			}
		}
		return false;
	}

	public static int getChildFolderCount(String ID) {
		int childTypeCount = 0;
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(ID) && MapRecord.types.get(i).equals(Status.FOLDER)) {
				childTypeCount++;
				if (containChildFolder(MapRecord.IDs.get(i))) {
					int tmp = getChildFolderCount(MapRecord.IDs.get(i));
					childTypeCount += tmp;
				}
			}
		}
		return childTypeCount;
	}

	public static int getChildWebMapCount(String ID) {
		int childTypeCount = 0;
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(ID)) {
				if (MapRecord.types.get(i).equals(Status.WEBMAP)) {
					childTypeCount++;
				} else {
					if (containChild(MapRecord.IDs.get(i))) {
						int tmp = getChildWebMapCount(MapRecord.IDs.get(i));
						childTypeCount += tmp;
					}
				}
			}
		}
		return childTypeCount;
	}

	public static boolean containChildFolder(String ID) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(ID) && MapRecord.types.get(i).equals(Status.FOLDER)) {
				return true;
			}
		}
		return false;
	}

	public static boolean deleteAllItemsInsideFolder(DBhelper dbhelper, String ID) {
		ArrayList<String> childList = Utility.getChildList(ID);
		for (String childID : childList) {
			dbhelper.delete(childID);
			MapRecord.removeSingleRecord(childID);
		}
		return true;
	}

	public static boolean moveItemsToNewFolder(DBhelper dbhelper, String ID, String newFolder, boolean isSelfMoving) {
		for (int i = 0; i < MapRecord.IDs.size(); i++) {
			if (!isSelfMoving && (MapRecord.parents.get(i) != null && MapRecord.parents.get(i).equals(ID))) {
				updateMapRecordParent(dbhelper, i, newFolder);
			}
			if (isSelfMoving && MapRecord.IDs.get(i).equals(ID)) {
				updateMapRecordParent(dbhelper, i, newFolder);
				return true;
			}
		}
		return true;
	}

	public static boolean updateMapRecordParent(DBhelper dbhelper, int index, String newFolder) {
		MapRecord.parents.set(index, newFolder);
		insertToDatabase(dbhelper, MapRecord.IDs.get(index), MapRecord.titles.get(index), MapRecord.owners.get(index), MapRecord.images.get(index),
				MapRecord.dates.get(index), MapRecord.ratings.get(index), MapRecord.descriptions.get(index), MapRecord.accesses.get(index),
				MapRecord.parents.get(index), MapRecord.types.get(index));
		return true;
	}

	public static boolean populateMapRecords(DBhelper dbhelper) {
		MapRecord.initData();
		Cursor cr = dbhelper.query(null);
		if (cr == null) {
			Log.e("Database items", "Cursor is null");
		}
		if (cr != null && cr.moveToFirst()) {
			while (!cr.isAfterLast()) {
				MapRecord.insertSingleRecord(cr.getString(0), cr.getString(1), cr.getString(2), cr.getBlob(3), cr.getLong(4), cr.getFloat(5), cr.getString(6),
						cr.getString(7), cr.getString(8), cr.getString(9));
				cr.moveToNext();
			}
		}
		cr.close();
		return true;
	}

	public static Drawable changeSensorIndicatorBackground(Context context, int origin, int target) {
		Resources r = context.getResources();
		int width = pixelScaler(context, 48);
		int height = pixelScaler(context, 48);

		Drawable targetDrawable = r.getDrawable(target);
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(targetBitmap);
		targetDrawable.setBounds(0, 0, width, height);
		targetDrawable.draw(c);

		Bitmap originBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(r, origin), width, height, false);
		Bitmap o = Bitmap.createScaledBitmap(originBitmap, width, height, false);
		Bitmap combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(combined);
		Bitmap background = Bitmap.createScaledBitmap(targetBitmap, width, height, false);
		canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(o, 0, 0, null);
		Drawable d = new BitmapDrawable(r, combined);
		return d;
	}

	public static Bitmap drawableToBitmap(Drawable drawable, Context context) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static void changeToggleButtonBackground(Context context, ToggleButton tb, int origin, int target) {
		tb.setBackgroundDrawable(changeSensorIndicatorBackground(context, R.drawable.icon_sensor_light, R.drawable.white_circle));
	}

	public static int getNumPerColumnInGridView(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		return (int) (((float) width) / pixelScaler(activity, 380));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void changeImageViewBackgroundToWhite(Activity activity, int id, int drawableID) {
		ImageView icon = (ImageView) activity.findViewById(id);
		icon.setBackground(changeSensorIndicatorBackground(activity, drawableID, R.drawable.settings_background_circle));
	}
}

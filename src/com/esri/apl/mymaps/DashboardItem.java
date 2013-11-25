package com.esri.apl.mymaps;

import java.util.ArrayList;

import android.database.Cursor;

public class DashboardItem {

	public static final String WEBMAP = "webmap";
	public static final String FOLDER = "folder";

	public static ArrayList<DashboardItem> allItems;
	public static ArrayList<DashboardItem> currentItems;

	public byte[] image;
	public String title, ID, owner, description, access, parent, type;
	public Long date;
	public Float rating;

	public DashboardItem(byte[] images, String titles, String ID,
			String owners, String descriptions, String accesses,
			String parents, String types, Long dates, Float ratings) {
		this.image = images;
		this.title = titles;
		this.ID = ID;
		this.owner = owners;
		this.description = descriptions;
		this.access = accesses;
		this.parent = parents;
		this.type = types;
		this.date = dates;
		this.rating = ratings;
	}

	public static void initData() {
		allItems = new ArrayList<DashboardItem>();
	}

	public static void initDataInCurrentFolder() {
		currentItems = new ArrayList<DashboardItem>();
	}

	public static void insertRecord(DashboardItem dashboardItem) {
		allItems.add(dashboardItem);
	}

	public static void removeRecord(DashboardItem dashboardItem) {
		allItems.remove(dashboardItem);
	}

	public static void removeRecord(String ID) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.ID.equals(ID)) {
				removeRecord(dashboardItem);
				break;
			}
		}
	}

	public static String getWebMapName(String ID) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.ID.equals(ID)) {
				return dashboardItem.title;
			}
		}
		return null;
	}

	/* read the records from database and store them in memeory as DashboardItem */
	public static void populateDashboardItems(DatabaseHelper dbhelper) {
		Cursor cr = dbhelper.query(null);
		if (cr != null && cr.moveToFirst()) {
			while (!cr.isAfterLast()) {
				insertRecord(new DashboardItem(cr.getBlob(3), cr.getString(1),
						cr.getString(0), cr.getString(2), cr.getString(6),
						cr.getString(7), cr.getString(8), cr.getString(9),
						cr.getLong(4), cr.getFloat(5)));
				cr.moveToNext();
			}
		}
		if (cr != null) {
			cr.close();
		}
	}

	/* insert a DashboardItem into database */
	public static void insertToDatabase(DatabaseHelper dbhelper,
			DashboardItem dashboardItem) {
		dbhelper.insert(dashboardItem.ID, dashboardItem.title,
				dashboardItem.owner, dashboardItem.image, dashboardItem.date,
				dashboardItem.rating, dashboardItem.description,
				dashboardItem.access, dashboardItem.parent, dashboardItem.type);
	}

	/* decide if the item contain any child */
	public static boolean containChild(String ID) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.parent != null && dashboardItem.parent.equals(ID)) {
				return true;
			}
		}
		return false;
	}

	public static String getParentFolder(String ID) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.ID.equals(ID)) {
				return dashboardItem.parent;
			}
		}
		return null;
	}

	public static boolean isFolderNameDuplicated(String folderName) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.type.equals(FOLDER)) {
				if (dashboardItem.ID.equals(folderName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void getItemsInCurrentFolder() {
		initDataInCurrentFolder();
		for (DashboardItem dashboardItem : allItems) {
			if ((Status.CurrentParent == null && dashboardItem.parent == null)
					|| (Status.CurrentParent != null
							&& dashboardItem.parent != null && Status.CurrentParent
								.equals(dashboardItem.parent))) {
				if (dashboardItem.type.equals(WEBMAP)) {
					currentItems.add(0, dashboardItem);
				} else if (dashboardItem.type.equals(FOLDER)) {
					currentItems.add(currentItems.size(), dashboardItem);
				}
			}
		}
	}

	public static int getChildFolderCount(String ID) {
		int childTypeCount = 0;
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.parent != null && dashboardItem.parent.equals(ID)
					&& dashboardItem.type.equals(FOLDER)) {
				childTypeCount++;
				if (containChildFolder(dashboardItem.ID)) {
					int tmp = getChildFolderCount(dashboardItem.ID);
					childTypeCount += tmp;
				}
			}
		}
		return childTypeCount;
	}

	public static boolean containChildFolder(String ID) {
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.parent != null && dashboardItem.parent.equals(ID)
					&& dashboardItem.type.equals(FOLDER)) {
				return true;
			}
		}
		return false;
	}

	public static int getChildWebMapCount(String ID) {
		int childTypeCount = 0;
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.parent != null && dashboardItem.parent.equals(ID)) {
				if (dashboardItem.type.equals(WEBMAP)) {
					childTypeCount++;
				} else if (containChild(dashboardItem.ID)) {
					int tmp = getChildWebMapCount(dashboardItem.ID);
					childTypeCount += tmp;
				}
			}
		}
		return childTypeCount;
	}

	/*
	 * generate a list of folder which the items in current folder can be moved
	 * to
	 */
	public static ArrayList<String> getFolderList(String currentFolder,
			boolean isWebMap) {
		ArrayList<String> childList = getChildList(currentFolder);
		ArrayList<String> folderList = new ArrayList<String>();
		if (Status.CurrentParent != null) {
			folderList.add("Home");
		}
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.type.equals(DashboardItem.FOLDER)
					&& !dashboardItem.ID.equals(currentFolder)) {
				if (isWebMap) {
					folderList.add(dashboardItem.ID);
				} else if (Status.MultipleLevelFoldersAllowed) {
					boolean isChild = false;
					for (String child : childList) {
						if (dashboardItem.ID.equals(child)) {
							childList.remove(child);
							isChild = true;
							break;
						}
					}
					if (!isChild) {
						folderList.add(dashboardItem.ID);
					}
				}
			}
		}
		return folderList;
	}

	/* get all the child of the given item */
	public static ArrayList<String> getChildList(String ID) {
		ArrayList<String> childList = new ArrayList<String>();
		for (DashboardItem dashboardItem : allItems) {
			if (dashboardItem.parent != null && dashboardItem.parent.equals(ID)
					&& containChild(dashboardItem.ID)) {
				ArrayList<String> tmp = getChildList(dashboardItem.ID);
				for (String child : tmp) {
					childList.add(child);
				}
			}
		}
		return childList;
	}

	public static boolean deleteAllItemsInsideFolder(DatabaseHelper dbhelper,
			String ID) {
		ArrayList<String> childList = getChildList(ID);
		for (String childID : childList) {
			dbhelper.delete(childID);
			removeRecord(childID);
		}
		return true;
	}

	public static boolean moveItemsToNewFolder(DatabaseHelper dbhelper,
			String ID, String newFolder, boolean isSelfMoving) {
		for (DashboardItem dashboardItem : allItems) {
			if ((!isSelfMoving && (dashboardItem.parent != null && dashboardItem.parent
					.equals(ID)))
					|| (isSelfMoving && dashboardItem.ID.equals(ID))) {
				dashboardItem.parent = newFolder;
				insertToDatabase(dbhelper, dashboardItem);
			}
		}
		return true;
	}
}

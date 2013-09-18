package com.esri.apl.mymaps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.esri.apl.mymaps.Enums.LongClickOption;
import com.esri.apl.mymaps.GridviewFragment.OnWebMapItemClickListener;
import com.esri.apl.mymaps.OptionDialogFragment.OnOptionDialogActionListener;
import com.esri.apl.mymaps.WebviewFragment.OnWebviewClickListener;
import com.esri.apl.mymaps.WelcomeFragment.OnShowcaseClickedListener;

public class DashboardActivity extends Activity implements
		OnWebviewClickListener, OnWebMapItemClickListener,
		OnShowcaseClickedListener, OnOptionDialogActionListener {

	private Menu menu = null;
	private DatabaseHelper dbHelper = null;
	private FragmentManager fragmentManager = null;
	private GridviewFragment gridviewFragment = null;
	private WebviewFragment webviewFragment = null;
	private WelcomeFragment welcomeFragment = null;

	protected static final String Extra = "extra";
	private static final String SHOWCASE_URL = "http://maps.esri.com/SP_DEMOS/mymaps/index.html";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		// TODO new relic

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("  " + getString(R.string.app_name));

		dbHelper = new DatabaseHelper(getApplicationContext());
		fragmentManager = getFragmentManager();
		Status.CurrentParent = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dashboard_menu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_reset:
			OptionDialogFragment.newInstance(OptionDialogFragment.RESET, null)
					.show(fragmentManager, "delete");
			break;
		case R.id.menu_showcase:
			startWebviewFragment();
			break;
		case R.id.menu_new_folder:
			OptionDialogFragment.newInstance(
					OptionDialogFragment.CREATE_FOLDER, null).show(
					fragmentManager, "create folder");
			break;
		case android.R.id.home:
			startGridviewFragment(null);
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (Status.isNFCAvailable = (nfcAdapter != null && nfcAdapter
				.isEnabled())) {
			fragmentManager.beginTransaction()
					.add(NFCControlFragment.newInstance(null), "nfc").commit();
		}
		loadContent();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/* navigate through different folders using the back button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (gridviewFragment != null && gridviewFragment.isVisible()
					&& Status.CurrentParent != null) {
				startGridviewFragment(DashboardItem
						.getParentFolder(Status.CurrentParent));
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/* load the webmap or dashboard depending on the intent */
	private void loadContent() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		if (intent.getDataString() != null) {
			String ID = Utility.extractWebMapID(intent.getDataString());
			intent.setData(null);
			if (ID != null) {
				startNewActivity(ID);
			} else {
				Utility.toast(getApplicationContext(), "Invalid WebMap");
				return;
			}
		}
		startGridviewFragment(Status.CurrentParent);
	}

	public void onShowcaseClicked() {
		startWebviewFragment();
	}

	/* called when items in gridview are clicked */
	public void onGridItemClicked(String ID, boolean isWebMap,
			boolean isLongClick) {
		if (isLongClick) {
			OptionDialogFragment.newInstance(OptionDialogFragment.LONG_CLICK,
					new String[] { ID, Boolean.toString(isWebMap) }).show(
					fragmentManager, "long click");
		} else {
			if (isWebMap) {
				if (!Utility.isInternetConnected(getApplicationContext())) {
					return;
				}
				startNewActivity(ID);
			} else {
				startGridviewFragment(ID);
			}
		}
	}

	public void onWebviewClicked(String webmapID) {
		startNewActivity(webmapID);
	}

	/* start the MapViewerActivity */
	protected void startNewActivity(String extra) {
		Intent it = new Intent(DashboardActivity.this, MapViewerActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		it.putExtra(Extra, extra);
		startActivity(it);
	}

	/* start the welcome screen */
	public void startWelcomeFragment(String message) {
		if (message == null) {
			message = getString(R.string.welcome_no_map);
		}
		welcomeFragment = WelcomeFragment.newInstance(message);
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_container, welcomeFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commit();
	}

	/* start the webview */
	public void startWebviewFragment() {
		if (!Utility.isInternetConnected(getApplicationContext())) {
			return;
		}
		if (webviewFragment == null) {
			webviewFragment = WebviewFragment.newInstance(SHOWCASE_URL);
		}
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_container, webviewFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null).commit();
	}

	/*
	 * start the gridview depending on the items in database, if there is no
	 * item in the database, start the welcome screen
	 */
	public void startGridviewFragment(String currentParent) {
		DashboardItem.initData();
		DashboardItem.populateDashboardItems(dbHelper);
		if (DashboardItem.allItems.size() == 0) {
			startWelcomeFragment(null);
			return;
		}
		Status.CurrentParent = currentParent;
		if (welcomeFragment != null) {
			welcomeFragment = null;
			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
				fragmentManager.popBackStackImmediate();
			}
		}
		if (menu != null) {
			if (Status.CurrentParent != null
					&& !Status.MultipleLevelFoldersAllowed) {
				menu.findItem(R.id.menu_new_folder).setVisible(false);
			} else {
				menu.findItem(R.id.menu_new_folder).setVisible(true);
			}
		}
		FrameLayout container = (FrameLayout) findViewById(R.id.fragment_container);
		container.removeAllViews();
		getActionBar().setTitle(
				Utility.getActionBarTitle(getApplicationContext()));
		DashboardItem.getItemsInCurrentFolder();
		gridviewFragment = GridviewFragment.newInstance(Status.CurrentParent);
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_container, gridviewFragment).commit();
	}

	/* callback for the options dialog */
	public void onOptionDialogAction(int action, String[] data) {
		if (action == OptionDialogFragment.RESET) {
			resetApp();
		} else if (action == OptionDialogFragment.CREATE_FOLDER) {
			insertNewFolder(data[0]);
		} else if (action == OptionDialogFragment.LONG_CLICK) {
			changeItemInDashboard(LongClickOption.valueOf(data[0]), data[1],
					data[2]);
		}
	}

	/*
	 * callback for the long click dialog. move or delete the webmap or folder
	 */
	private void changeItemInDashboard(LongClickOption longClickOption,
			String ID, String targetFolder) {
		switch (longClickOption) {
		case Delete:
			dbHelper.delete(ID);
			break;

		case DeleteAll:
			for (String childID : DashboardItem.getChildList(ID)) {
				dbHelper.delete(childID);
			}
			dbHelper.delete(ID);
			break;

		case DeleteAndMoveToParent:
			DashboardItem.moveItemsToNewFolder(dbHelper, ID,
					DashboardItem.getParentFolder(ID), false);
			dbHelper.delete(ID);
			break;

		case DeleteAndMoveToRoot:
			DashboardItem.moveItemsToNewFolder(dbHelper, ID, null, false);
			dbHelper.delete(ID);
			break;

		case Move:
			if (targetFolder.equals("Home")) {
				targetFolder = null;
			}
			DashboardItem
					.moveItemsToNewFolder(dbHelper, ID, targetFolder, true);
			break;
		}

		startGridviewFragment(Status.CurrentParent);

	}

	/* create a new folder and update the gridview */
	private void insertNewFolder(String name) {
		DashboardItem.insertRecord(new DashboardItem(null, null, name, null,
				null, null, Status.CurrentParent, DashboardItem.FOLDER, System
						.currentTimeMillis(), null));
		dbHelper.insert(name, null, null, null, System.currentTimeMillis(),
				null, null, null, Status.CurrentParent, DashboardItem.FOLDER);
		startGridviewFragment(Status.CurrentParent);
	}

	/* delete all items in database and restart the gridview */
	private void resetApp() {
		dbHelper.reset();
		DashboardItem.initData();
		loadContent();
	}
}
package com.esri.apl.mymaps;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.esri.apl.mymaps.CreateFolderFragment.OnNewFolderCreatedListener;
import com.esri.apl.mymaps.GridviewFragment.OnWebMapItemClickListener;
import com.esri.apl.mymaps.LongClickDialogFragment.OnLongClickOptionListener;
import com.esri.apl.mymaps.Status.ClickType;
import com.esri.apl.mymaps.Status.LongClickOption;
import com.esri.apl.mymaps.WebViewFragment.OnWebviewClickListener;
import com.esri.apl.mymaps.WelcomeFragment.OnShowcaseClickedListener;
import com.newrelic.agent.android.NewRelic;

public class DashActivity extends Activity implements OnWebviewClickListener, OnWebMapItemClickListener, OnShowcaseClickedListener, OnNewFolderCreatedListener,
		OnLongClickOptionListener {

	private DBhelper dbhelper;
	private static final String SHOWCASE_URL = "http://maps.esri.com/SP_DEMOS/mymaps/gallery.html";
	public static final String Extra = "extra";
	public static Boolean isCompassEnabled = false;
	public static Boolean isLightSensorEnabled = false;
	public static Boolean isGyroscopeEnabled = false;
	public static Boolean isGPSEnabled = false;
	public static Boolean isVoiceEnabled = false;

	private Menu menu;
	private ActionBar actionBar;
	private FragmentManager fragmentManager;
	private GridviewFragment gridviewFragment;
	private WebViewFragment webviewFragment;
	private WelcomeFragment welcomeFragment;

	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;
	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private WifiP2pManager wifiP2Pmanager;
	private BroadcastReceiver broadcastReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("appdash", "Dashactivity Oncreate");
		super.onCreate(savedInstanceState);
		NewRelic.withApplicationToken("AA92a786d8a2f1c2bb3a1c58207ac8b57f502d27c8 ").start(this.getApplication());

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			Log.i("appdash", "small screen");
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			Log.i("appdash", "normal screen");
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}

		setContentView(R.layout.main);
		dbhelper = new DBhelper(this);
		actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("  " + getString(R.string.app_name));
		webviewFragment = WebViewFragment.newInstance(SHOWCASE_URL);
		fragmentManager = getFragmentManager();
		// deviceListFragment = new DeviceListFragment();
		// fragmentManager.beginTransaction().add(deviceListFragment,
		// "wifi").commit();
		// deviceListFragment.onInitiateDiscovery();
		Status.CurrentParent = null;

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		wifiP2Pmanager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = wifiP2Pmanager.initialize(this, getMainLooper(), null);

		load(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clear_all:
			DeleteWebMapDialogFragment delete = new DeleteWebMapDialogFragment(null, getString(R.string.delete_confirm));
			delete.show(getFragmentManager(), "MyMaps");
			break;
		case R.id.menu_showcase:
			startWebviewFragment();
			break;
		case R.id.menu_new_folder:
			startCreateNewFolderFragment();
			break;
		case android.R.id.home:
			startGridviewFragment(null);
		default:
			break;
		}
		return true;
	}

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	protected void showAlert() {
		Toast.makeText(getApplicationContext(), "WebMap ID is invalid.", Toast.LENGTH_SHORT).show();
	}

	protected void load(Intent i) {
		if (i == null) {
			return;
		}

		if (i.getDataString() != null) {
			Log.i("mymaps", "get data string " + i.getDataString());
			String id = Utility.getWebMapID(i.getDataString());
			i.setData(null);
			if (id != null) {
				startNewActivity(id);
			} else {
				showAlert();
				return;
			}
		}

		MapRecord.initData();
		Utility.populateMapRecords(dbhelper);

		if (MapRecord.IDs.size() != 0) {
			startGridviewFragment(Status.CurrentParent);
		} else {
			startWelcomeFragment(null);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (Status.isNFCAvailable = (nfcAdapter != null && nfcAdapter.isEnabled())) {
			fragmentManager.beginTransaction().add(NFCControlFragment.newInstance(null), "nfc").commit();
		}

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			Log.i("nfc_test", "get nfc intent from ndef");
		}
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
			Log.i("nfc_test", "get nfc intent from tag");
		}
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
			Log.i("nfc_test", "get nfc intent from tech");
		}
		Log.i("mymaps", "on resume get intent: " + getIntent().getDataString());
		load(getIntent());
	}

	protected void startNewActivity(String extra) {
		Intent it = new Intent(DashActivity.this, MapViewerActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		it.putExtra(Extra, extra);
		startActivity(it);
	}

	@SuppressLint("ValidFragment")
	private class DeleteWebMapDialogFragment extends DialogFragment {

		String ID = null;
		String tip;

		public DeleteWebMapDialogFragment(String ID, String tip) {
			this.ID = ID;
			this.tip = tip;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(tip).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (ID == null) {
						dbhelper.reset();
						MapRecord.initData();
					} else {
						dbhelper.delete(ID);
					}
					if (dbhelper.query(null).getCount() == 0) {
						startWelcomeFragment(null);
					} else {
						load(getIntent());
					}
				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

				}
			});
			return builder.create();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (gridviewFragment != null && fragmentManager.findFragmentById(R.id.fragment_container).hashCode() == gridviewFragment.hashCode()
					&& Status.CurrentParent != null) {
				startGridviewFragment(Utility.findParentOfParent(Status.CurrentParent));
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onWebviewClicked(String data) {
		startNewActivity(data);
	}

	public void onGridItemClicked(String ID, String itemType, ClickType clickType) {
		if (clickType == ClickType.LongClick) {
			startLongClickDialogFragment(ID, itemType);
		} else {
			if (itemType.equals(Status.WEBMAP)) {
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo mNet = connManager.getActiveNetworkInfo();
				if (mNet == null) {
					Toast.makeText(getApplicationContext(), getString(R.string.home_msg_no_wifi), Toast.LENGTH_SHORT).show();
					return;
				}
				startNewActivity(ID);
			} else {
				startGridviewFragment(ID);
			}
		}
	}

	public void onShowcaseClicked() {
		startWebviewFragment();
	}

	public void OnNewFolderCreated(String newFolderName) {
		MapRecord.insertSingleRecord(newFolderName, null, null, null, System.currentTimeMillis(), null, null, null, Status.CurrentParent, Status.FOLDER);
		Utility.insertToDatabase(dbhelper, newFolderName, null, null, null, System.currentTimeMillis(), null, null, null, Status.CurrentParent, Status.FOLDER);
		startGridviewFragment(Status.CurrentParent);
	}

	public void startWebviewFragment() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mNet = connManager.getActiveNetworkInfo();
		if (mNet == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.home_msg_no_wifi), Toast.LENGTH_SHORT).show();
			return;
		}
		fragmentManager.beginTransaction().replace(R.id.fragment_container, webviewFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null).commit();
	}

	private void startLongClickDialogFragment(String ID, String type) {
		LongClickDialogFragment longClickDialogFragment = LongClickDialogFragment.newInstance(ID, type);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		longClickDialogFragment.show(ft, "dialog");
	}

	public void startCreateNewFolderFragment() {
		CreateFolderFragment createFolderFragment = CreateFolderFragment.newInstance();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		createFolderFragment.show(ft, "dialog");
	}

	public void startWelcomeFragment(String message) {
		if (message == null) {
			message = getString(R.string.home_msg_no_map);
		}
		welcomeFragment = WelcomeFragment.newInstance(message);
		fragmentManager.beginTransaction().replace(R.id.fragment_container, welcomeFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
	}

	public void startGridviewFragment(String currentParent) {
		Status.CurrentParent = currentParent;
		if (welcomeFragment != null) {
			// fragmentManager.beginTransaction().remove(welcomeFragment).commit();
			welcomeFragment = null;
			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
				fragmentManager.popBackStackImmediate();
			}
		}
		if (menu != null) {
			if (Status.CurrentParent != null && !Status.MultipleLevelFoldersAllowed) {
				menu.findItem(R.id.menu_new_folder).setVisible(false);
			} else {
				menu.findItem(R.id.menu_new_folder).setVisible(true);
			}
		}
		FrameLayout container = (FrameLayout) findViewById(R.id.fragment_container);
		container.removeAllViews();
		actionBar.setTitle(Utility.buildActionBarTitle(getApplicationContext()));
		Utility.getMapRecordInCurrentFolder();
		Status.adjust = 0;
		Status.gridPadding = 0;
		Status.itemsPerColumn = Utility.getNumPerColumnInGridView(this);
		gridviewFragment = GridviewFragment.newInstance(Status.CurrentParent);
		fragmentManager.beginTransaction().replace(R.id.fragment_container, gridviewFragment).commit();
	}

	public void onLongclickOptionPicked(LongClickOption longClickOption, String ID, String targetID) {
		switch (longClickOption) {
		case Delete:
			dbhelper.delete(ID);
			MapRecord.removeSingleRecord(ID);
			break;

		case DeleteAll:
			Status.CurrentParent = Utility.findParentOfParent(ID);
			Utility.deleteAllItemsInsideFolder(dbhelper, ID);
			dbhelper.delete(ID);
			MapRecord.removeSingleRecord(ID);
			break;

		case DeleteAndMoveToParent:
			Status.CurrentParent = Utility.findParentOfParent(ID);
			Utility.moveItemsToNewFolder(dbhelper, ID, Utility.findParentOfParent(ID), false);
			dbhelper.delete(ID);
			MapRecord.removeSingleRecord(ID);
			break;

		case DeleteAndMoveToRoot:
			Status.CurrentParent = Utility.findParentOfParent(null);
			Utility.moveItemsToNewFolder(dbhelper, ID, null, false);
			dbhelper.delete(ID);
			MapRecord.removeSingleRecord(ID);
			break;

		case Move:
			if (targetID.equals("Home")) {
				targetID = null;
			}
			Utility.moveItemsToNewFolder(dbhelper, ID, targetID, true);
			break;

		default:
			break;
		}
		if (MapRecord.IDs.size() > 0) {
			startGridviewFragment(Status.CurrentParent);
		} else {
			startWelcomeFragment(null);
		}
	}
}
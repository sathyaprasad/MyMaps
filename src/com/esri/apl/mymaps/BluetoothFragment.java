package com.esri.apl.mymaps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.core.geometry.Point;

public class BluetoothFragment extends Fragment {

	private UUID uuid = null;
	private Handler handler = null;
	private Lock lock = null;
	protected TextView selfStatus = null;
	private LinearLayout peerList = null;
	private ArrayList<BluetoothDevice> peers = null;
	private BluetoothAdapter bluetoothAdapter = null;
	private OnBluetoothChangeListener listener = null;

	protected ServerThread serverThread = null;
	protected ConnectThread connectThread = null;
	protected ConnectedThread connectedThread = null;

	protected boolean isReady = false;
	protected boolean isConnector = false;
	protected boolean isSyncExtent = true;
	protected boolean isSyncPopup = true;
	protected boolean isSyncLayers = true;
	protected boolean bluetoothSendingEnabled = true;
	protected boolean bluetoothReceivingEnabled = true;

	// connection
	public static final String CONNECT_REQUEST = "connect request";

	// message
	public static final String SPLITER = "#####";

	// map extent
	public static final String CENTER_AT = "center at";
	public static final String ROTATE = "rotate";
	public static final String SCALE = "scale";
	public static final String EXTENT_CHANGE = "extent change";

	// layers
	public static final String LAYER_CHANGE = "layer change";
	public static final String BASEMAP_CHANGE = "basemap change";

	// callout
	public static final String SINGLE_TAP = "single tap";
	public static final String CALLOUT_CLOSE = "callout close";
	public static final String CALLOUT_NEXT = "callout next";
	public static final String CALLOUT_PREVIOUS = "callout previous";
	public static final String CALLOUT_CHANGE = "callout change";

	private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null) {
					peers.add(device);
					updatePeers(device);
				}
			}
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					toast(getActivity(), "Bluetooth off");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					toast(getActivity(), "Turning Bluetooth off...");
					break;
				case BluetoothAdapter.STATE_ON:
					toast(getActivity(), "Bluetooth on");
					RadioButton rb = (RadioButton) getActivity().findViewById(
							R.id.mode_listen);
					if (rb.isChecked()) {
						startListen();
						toast(getActivity(), "Start Listening...");
					} else {
						rb.performClick();
					}
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					toast(getActivity(), "Turning Bluetooth on...");
					break;
				}
			}
		}
	};

	public interface OnBluetoothChangeListener {
		public void onBluetoothMessage(String message);
	}

	private MapViewerActivity getMapActivity() {
		return (MapViewerActivity) getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		lock = new ReentrantLock();
		handler = new Handler();
		uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			toast(getActivity(), getString(R.string.bluetooth_not_supported));
			return;
		}
		if (!bluetoothAdapter.isEnabled()) {
			try {
				bluetoothAdapter.enable();
			} catch (Exception e) {
				e.printStackTrace();
				toast(getActivity(),
						getString(R.string.bluetooth_enabled_problem));
				return;
			}
			toast(getActivity(), getString(R.string.bluetooth_enabled));
		}
		setupBluetoothControlUI();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnBluetoothChangeListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		try {
			super.onDetach();
			disconnect();
			listener = null;
			getActivity().unregisterReceiver(bluetoothBroadcastReceiver);
			if (connectedThread != null) {
				connectedThread.cancel();
			}
			bluetoothAdapter.cancelDiscovery();
			clearThreads();
		} catch (Exception e) {

		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter iFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		iFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		getActivity().registerReceiver(bluetoothBroadcastReceiver, iFilter);
		startListen();
	}

	public void write(byte[] out) {
		if (connectedThread != null) {
			ConnectedThread r;
			synchronized (this) {
				r = connectedThread;
			}
			r.write(out);
		}
	}

	/* setup the bluetooth control UI in the sidebar */
	private void setupBluetoothControlUI() {
		peerList = (LinearLayout) getActivity().findViewById(R.id.peer_list);
		TextView selfDeviceName = (TextView) getActivity().findViewById(
				R.id.device_name);
		TextView selfDetail = (TextView) getActivity().findViewById(
				R.id.device_details);
		selfStatus = (TextView) getActivity().findViewById(R.id.device_status);

		selfDeviceName.setText(bluetoothAdapter.getName());
		selfDetail.setText(bluetoothAdapter.getAddress());
		selfStatus.setText(R.string.bluetooth_not_connected);
		final RadioButton sendRB = (RadioButton) getActivity().findViewById(
				R.id.mode_send);
		final RadioButton receiveRB = (RadioButton) getActivity().findViewById(
				R.id.mode_receive);
		final RadioButton biRB = (RadioButton) getActivity().findViewById(
				R.id.mode_bi);
		OnCheckedChangeListener bluetoothOptionListener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					if (buttonView.hashCode() == sendRB.hashCode()) {
						bluetoothSendingEnabled = true;
						bluetoothReceivingEnabled = false;
					} else if (buttonView.hashCode() == receiveRB.hashCode()) {
						bluetoothSendingEnabled = false;
						bluetoothReceivingEnabled = true;
					} else if (buttonView.hashCode() == biRB.hashCode()) {
						bluetoothSendingEnabled = true;
						bluetoothReceivingEnabled = true;
					}
				}
			}
		};
		sendRB.setOnCheckedChangeListener(bluetoothOptionListener);
		receiveRB.setOnCheckedChangeListener(bluetoothOptionListener);
		biRB.setOnCheckedChangeListener(bluetoothOptionListener);

		final RadioButton listenRB = (RadioButton) getActivity().findViewById(
				R.id.mode_listen);
		final RadioButton discoveryRB = (RadioButton) getActivity()
				.findViewById(R.id.mode_discovery);
		OnCheckedChangeListener bluetoothModeOptionListener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					if (bluetoothAdapter == null
							|| !bluetoothAdapter.isEnabled()) {
						toast(getActivity(), "Bluetooth off");
						return;
					}
					if (buttonView.hashCode() == listenRB.hashCode()) {
						startListen();
						toast(getActivity(), "Start Listening...");
					} else if (buttonView.hashCode() == discoveryRB.hashCode()) {
						startDiscovery();
					}
				}
			}
		};
		listenRB.setOnCheckedChangeListener(bluetoothModeOptionListener);
		discoveryRB.setOnCheckedChangeListener(bluetoothModeOptionListener);

		((Button) getActivity().findViewById(R.id.disconnect))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						disconnect();
					}
				});

		/*
		 * change what to sync (extent, layers, popups) of bluetooth using these
		 * CheckBoxs
		 */
		final CheckBox syncExtent = (CheckBox) getActivity().findViewById(
				R.id.option_extent);
		final CheckBox syncLayers = (CheckBox) getActivity().findViewById(
				R.id.option_layers);
		final CheckBox syncPopups = (CheckBox) getActivity().findViewById(
				R.id.option_popups);
		OnCheckedChangeListener bluetoothSyncOptionListener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView.hashCode() == syncExtent.hashCode()) {
					isSyncExtent = isChecked;
				} else if (buttonView.hashCode() == syncLayers.hashCode()) {
					isSyncLayers = isChecked;
				} else if (buttonView.hashCode() == syncPopups.hashCode()) {
					isSyncPopup = isChecked;
				}
			}
		};
		syncExtent.setOnCheckedChangeListener(bluetoothSyncOptionListener);
		syncLayers.setOnCheckedChangeListener(bluetoothSyncOptionListener);
		syncPopups.setOnCheckedChangeListener(bluetoothSyncOptionListener);
	}

	/* update the UI when a new device is found */
	private void updatePeers(final BluetoothDevice device) {
		View view = ((LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.device_cell,
				null);
		view.setClickable(true);
		TextView deviceName = (TextView) view.findViewById(R.id.device_name);
		TextView deviceDetail = (TextView) view
				.findViewById(R.id.device_details);
		final TextView deviceStatus = (TextView) view
				.findViewById(R.id.device_status);
		deviceName.setText(device.getName());
		deviceDetail.setText(device.getAddress());
		deviceStatus.setText(getBondedStatus(device.getBondState()));
		view.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setBackgroundColor(Color.CYAN);
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				connectToDevice(device);
				deviceStatus.setText(R.string.bluetooth_connecting);
				selfStatus.setText(R.string.bluetooth_connecting);
			}
		});
		view.setTag(device.getAddress());
		if (findPeerInList(device.getAddress()) == null) {
			peerList.addView(view);
		}
	}

	protected void startListen() {
		try {
			bluetoothAdapter.cancelDiscovery();
			if (serverThread != null) {
				serverThread.cancel();
				serverThread = null;
			}
			serverThread = new ServerThread();
			serverThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startDiscovery() {
		clearThreads();
		searchPeers();
		toast(getActivity(), "Searching Devices...");
	}

	private synchronized void connectToDevice(BluetoothDevice device) {
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		connectThread = new ConnectThread(device);
		connectThread.start();
	}

	private synchronized void clearThreads() {
		if (serverThread != null) {
			serverThread.cancel();
			serverThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
	}

	protected synchronized void connectFail(final BluetoothDevice device) {
		handler.post(new Runnable() {
			public void run() {
				View v = findPeerInList(device.getAddress());
				TextView tv = (TextView) v.findViewById(R.id.device_status);
				tv.setText(getBondedStatus(device.getBondState()));
				selfStatus.setText(R.string.bluetooth_not_connected);
				toast(getActivity(), getString(R.string.bluetooth_connect_fail));
			}
		});
	}

	protected synchronized void disconnect() {
		handler.post(new Runnable() {
			public void run() {
				try {
					clearThreads();
					if (connectedThread != null) {
						connectedThread.cancel();
						connectedThread = null;
					}
					peerList.removeAllViews();
					selfStatus.setText(getActivity().getString(
							R.string.bluetooth_not_connected));
					toast(getActivity(), "Disconnecting...");
					getActivity().findViewById(R.id.mode_listen).performClick();
					startListen();
					getActivity().findViewById(R.id.bluetooth_mode_container)
							.setVisibility(View.VISIBLE);
					getActivity().findViewById(R.id.disconnect).setVisibility(
							View.GONE);
					getActivity().findViewById(R.id.bluetooth_option_container)
							.setVisibility(View.GONE);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		clearThreads();
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		handler.postDelayed(new Runnable() {
			public void run() {
				try {
					Log.i("bluetooth", bluetoothAdapter.getAddress() + " "
							+ bluetoothAdapter.getName());
					messageBuilder(
							CONNECT_REQUEST,
							new String[] {
									bluetoothAdapter.getAddress(),
									bluetoothAdapter.getName(),
									((MapViewerActivity) getActivity()).webMapID });
				} catch (NullPointerException e) {
					e.printStackTrace();
					disconnect();
				}
			}
		}, 1500);
	}

	/* update the UI when the device is connected */
	protected void getConnected(final String addr, final String name) {
		handler.post(new Runnable() {
			public void run() {
				getActivity().findViewById(R.id.bluetooth_mode_container)
						.setVisibility(View.GONE);
				getActivity().findViewById(R.id.bluetooth_option_container)
						.setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.disconnect).setVisibility(
						View.VISIBLE);
				selfStatus.setText(R.string.bluetooth_connected);
				if (peerList == null) {
					peerList = (LinearLayout) getActivity().findViewById(
							R.id.peer_list);
				}
				peerList.removeAllViews();
				View view = ((LayoutInflater) getActivity().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE)).inflate(
						R.layout.device_cell, null);
				view.setClickable(true);
				TextView deviceName = (TextView) view
						.findViewById(R.id.device_name);
				TextView deviceDetail = (TextView) view
						.findViewById(R.id.device_details);
				TextView deviceStatus = (TextView) view
						.findViewById(R.id.device_status);
				deviceName.setText(name);
				deviceDetail.setText(addr);
				deviceStatus.setText(R.string.bluetooth_connected);
				peerList.addView(view);
			}
		});
	}

	private String getBondedStatus(int status) {
		String str = "";
		switch (status) {
		case BluetoothDevice.BOND_BONDED:
			str = "Paired (Click to connect)";
			break;
		case BluetoothDevice.BOND_BONDING:
			str = "Pairing";
			break;
		case BluetoothDevice.BOND_NONE:
			str = "Not Paired (Click to connect)";
			break;

		default:
			break;
		}
		return str;
	}

	private void searchPeers() {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivity(discoverableIntent);
		peerList.removeAllViews();
		peers = new ArrayList<BluetoothDevice>();
		searchPairedDevices();
		searchNewDevices();
	}

	private void searchPairedDevices() {
		try {
			Set<BluetoothDevice> pairedDevices = bluetoothAdapter
					.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					peers.add(device);
					updatePeers(device);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean searchNewDevices() {
		try {
			bluetoothAdapter.startDiscovery();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	protected View findPeerInList(String address) {
		for (int i = 0; i < peerList.getChildCount(); i++) {
			if (peerList.getChildAt(i) != null
					&& peerList.getChildAt(i).getTag() != null
					&& ((String) peerList.getChildAt(i).getTag())
							.equals(address)) {
				return peerList.getChildAt(i);
			}
		}
		return null;
	}

	public class ServerThread extends Thread {
		private final BluetoothServerSocket serverSocket;

		public ServerThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
						getString(R.string.app_name), uuid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			serverSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			while (true) {
				try {
					socket = serverSocket.accept();
				} catch (Exception e) {
					break;
				}
				if (socket != null) {
					try {
						if (connectedThread == null) {
							Log.i("bluetooth", "connect request received");
							connected(socket, socket.getRemoteDevice());
						}
						serverSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		public void cancel() {
			try {
				serverSocket.close();
			} catch (Exception e) {
			}
		}
	}

	public class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			this.device = device;
			try {
				tmp = device.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
			}
			socket = tmp;
		}

		public void run() {
			bluetoothAdapter.cancelDiscovery();
			Log.i("bluetooth", "start client");

			try {
				socket.connect();
			} catch (Exception connectException) {
				Log.e("bluetooth", "connect fail");
				connectException.printStackTrace();
				try {
					socket.close();
					connectFail(device);
				} catch (IOException closeException) {
				}
				return;
			}

			synchronized (this) {
				connectThread = null;
			}
			isConnector = true;
			connected(socket, device);
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream is;
		private final OutputStream os;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream isTmp = null;
			OutputStream osTmp = null;

			try {
				isTmp = socket.getInputStream();
				osTmp = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("bluetooth", "temp sockets not created", e);
			}

			is = isTmp;
			os = osTmp;
		}

		public void run() {
			@SuppressWarnings("unused")
			int bytes;
			byte[] buffer;
			while (true) {
				try {
					// lock.lock();
					buffer = new byte[1024];
					bytes = is.read(buffer);
					listener.onBluetoothMessage(new String(buffer));
				} catch (Exception e) {
					e.printStackTrace();
					disconnect();
					break;
				} finally {
					// lock.unlock();
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				Log.i("bluetooth", "write: " + new String(buffer));
				os.write(buffer);
			} catch (IOException e) {
				Log.e("bluetooth", "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e("bluetooth", "close() of connect socket failed", e);
			}
		}
	}

	/* build a bluetooth message */
	/* extend change, single tap, center at, callout change */
	public void messageBuilder(String type, String[] msg) {
		if (!bluetoothSendingEnabled || !isSyncTypeEnabled(type)) {
			return;
		}
		lock.lock();
		try {
			String result = "";
			result += type + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += msg[i] + SPLITER;
			}
			// result += type;
			write(result.getBytes());
		} finally {
			lock.unlock();
		}
	}

	/* build a bluetooth message */
	/* layer change, basemap change */
	public void messageBuilder(String type, double[] msg) {
		if (!bluetoothSendingEnabled || !isSyncTypeEnabled(type)) {
			return;
		}
		lock.lock();
		try {
			String result = "";
			result += type + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += Double.toString(msg[i]) + SPLITER;
			}
			// result += type;
			write(result.getBytes());
		} finally {
			lock.unlock();
		}
	}

	/* process bluetooth message received from other devices */
	public void processBluetoothMessage(String message) {
		final MapViewerActivity activity = getMapActivity();
		final String[] data = message.split(SPLITER);
		if (data[0] == null) {
			return;
		}
		Log.i("bluetooth", "recevice bluetooth message: " + message);
		if (data[0].equals(CONNECT_REQUEST)) {
			if (data[3] != null && !data[3].equals(getMapActivity().webMapID)) {
				disconnect();
				toast(activity,
						activity.getString(R.string.bluetooth_sync_hint));
			} else {
				getConnected(data[1], data[2]);
				toast(activity,
						activity.getString(R.string.bluetooth_connected));
			}
		}
		if (!bluetoothReceivingEnabled || !isSyncTypeEnabled(data[0])) {
			return;
		}

		handler.post(new Runnable() {

			public void run() {

//				activity.loadWebMapFragment.loadMapControl();

				if (data[0].equals(EXTENT_CHANGE)) {
					MapControlWidget.centerMapAt(
							activity,
							new Point(Double.parseDouble(data[1]), Double
									.parseDouble(data[2])), false, false);
					activity.mapView.setScale(Double.parseDouble(data[3]));
					MapControlWidget.rotateMap(activity,
							Double.parseDouble(data[4]), false);
				} else if (data[0].equals(CENTER_AT)) {
					MapControlWidget.centerMapAt(
							activity,
							new Point(Double.parseDouble(data[1]), Double
									.parseDouble(data[2])), Utility
									.doubleToBoolean(Double
											.parseDouble(data[3])), false);
				} else if (data[0].equals(ROTATE)) {
					MapControlWidget.rotateMap(activity, Double
							.parseDouble(data[1]), false);
				} else if (data[0].equals(SCALE)) {
					activity.mapView.setScale(Double.parseDouble(data[1]));
				} else if (data[0].equals(LAYER_CHANGE)) {
					MapControlWidget.switchLayer(activity,
							Integer.parseInt(data[1]), null,
							Boolean.parseBoolean(data[2]), false);
				} else if (data[0].equals(BASEMAP_CHANGE)) {
					MapControlWidget.switchBasemap(activity, data[1], null,
							null, false);
				} else if (data[0].equals(SINGLE_TAP)) {
					MapControlWidget.queryMapView(activity,
							Float.parseFloat(data[1]),
							Float.parseFloat(data[2]), false);
				} else if (data[0].equals(CALLOUT_CHANGE)) {
					MapControlWidget.switchCallout(activity,
							(int) Double.parseDouble(data[1]), false);
				}
			}
		});
	}

	private void toast(final Context context, final String msg) {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean isSyncTypeEnabled(String type) {
		if ((type.equals(LAYER_CHANGE) || (type.equals(BASEMAP_CHANGE)))
				&& !isSyncLayers) {
			return false;
		}
		if ((type.equals(EXTENT_CHANGE) || type.equals(CENTER_AT)
				|| type.equals(SCALE) || type.equals(ROTATE))
				&& !isSyncExtent) {
			return false;
		}
		if ((type.equals(SINGLE_TAP) || type.equals(CALLOUT_CHANGE))
				&& !isSyncPopup) {
			return false;
		}
		return true;
	}
}

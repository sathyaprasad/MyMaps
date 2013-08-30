package com.esri.apl.mymaps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.apl.mymaps.Status.BluetoothMessageType;

public class BluetoothFragment extends Fragment {
	private static final int REQUEST_ENABLE_BT = 500;

	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private Handler handler = new Handler();
	private LinearLayout peerList;
	private LayoutInflater layoutInflater;
	private BluetoothAdapter bluetoothAdapter;
	private ArrayList<BluetoothDevice> peers;
	private Set<BluetoothDevice> pairedDevices;
	private IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	private OnBluetoothChangeListener listener;
	protected ServerThread serverThread;
	protected ConnectThread connectThread;
	protected ConnectedThread connectedThread;
	protected TextView selfDeviceStatus;
	private int state;
	protected boolean isReady = false;
	protected boolean isConnector = false;
	protected boolean isSyncExtent = true;
	protected boolean isSyncPopup = true;
	protected boolean isSyncLayers = true;
	protected boolean bluetoothSendingEnabled = true;
	protected boolean bluetoothReceivingEnabled = true;
	// private final Handler handler = new Handler();

	public static final String CONNECT_REQUEST = "connect request";
	public static final String EXTENT_CHANGE = "extent change";
	public static final String SINGLE_TAP = "single tap";
	public static final String LAYER_CHANGE = "layer change";
	public static final String BASEMAP_CHANGE = "basemap change";
	public static final String CALLOUT_CLOSE = "callout close";
	public static final String CALLOUT_NEXT = "callout next";
	public static final String CALLOUT_PREVIOUS = "callout previous";
	public static final String CALLOUT_CHANGE = "callout change";
	public static final String SPLITER = "#####";

	public interface OnBluetoothChangeListener {
		public void onBluetoothChange(String message, int type);
	}

	private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				peers.add(device);
				updatePeers(device);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.i("bluetooth", "ACTION_DISCOVERY_FINISHED");
			} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
				Log.i("bluetooth", "ACTION_CONNECTION_STATE_CHANGED");
			}
			if (BluetoothAdapter.STATE_CONNECTED == bluetoothAdapter.getState()) {
				Log.i("bluetooth", "STATE_CONNECTED  111 ");
			} else if (BluetoothAdapter.STATE_DISCONNECTED == bluetoothAdapter.getState()) {
				Log.i("bluetooth", "STATE_CONNECTED  111 ");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setRetainInstance(true);
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (bluetoothAdapter == null) {
				toast(getString(R.string.bt_not_supported));
				return;
			}

			if (!bluetoothAdapter.isEnabled()) {
				toast(getString(R.string.bluetooth_enabled));
				// return;
				bluetoothAdapter.enable();
				// Intent enableBtIntent = new
				// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				// enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
				// 1000);
				// startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			peerList = (LinearLayout) getActivity().findViewById(R.id.peer_list);
			layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			TextView selfDeviceName = (TextView) getActivity().findViewById(R.id.device_name);
			TextView selfDetail = (TextView) getActivity().findViewById(R.id.device_details);
			selfDeviceStatus = (TextView) getActivity().findViewById(R.id.device_status);

			selfDeviceName.setText(bluetoothAdapter.getName());
			selfDetail.setText(bluetoothAdapter.getAddress());
			selfDeviceStatus.setText(R.string.bluetooth_not_connected);

			final RadioButton sendRB = (RadioButton) getActivity().findViewById(R.id.mode_send);
			final RadioButton receiveRB = (RadioButton) getActivity().findViewById(R.id.mode_receive);
			final RadioButton biRB = (RadioButton) getActivity().findViewById(R.id.mode_bi);
			OnCheckedChangeListener occl = new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
			sendRB.setOnCheckedChangeListener(occl);
			receiveRB.setOnCheckedChangeListener(occl);
			biRB.setOnCheckedChangeListener(occl);

			final RadioButton listenRB = (RadioButton) getActivity().findViewById(R.id.mode_listen);
			final RadioButton discoveryRB = (RadioButton) getActivity().findViewById(R.id.mode_discovery);
			OnCheckedChangeListener occl2 = new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						if (buttonView.hashCode() == listenRB.hashCode()) {
							startListen();
							toast("Start Listening...");
						} else if (buttonView.hashCode() == discoveryRB.hashCode()) {
							startDiscovery();
						}
					}
				}
			};
			listenRB.setOnCheckedChangeListener(occl2);
			discoveryRB.setOnCheckedChangeListener(occl2);

			Button disconnectButton = (Button) getActivity().findViewById(R.id.disconnect);
			disconnectButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					disconnect();
				}
			});

			final CheckBox syncExtent = (CheckBox) getActivity().findViewById(R.id.option_extent);
			final CheckBox syncLayers = (CheckBox) getActivity().findViewById(R.id.option_layers);
			final CheckBox syncPopups = (CheckBox) getActivity().findViewById(R.id.option_popups);
			OnCheckedChangeListener occl3 = new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (buttonView.hashCode() == syncExtent.hashCode()) {
						isSyncExtent = isChecked;
					} else if (buttonView.hashCode() == syncLayers.hashCode()) {
						isSyncLayers = isChecked;
					} else if (buttonView.hashCode() == syncPopups.hashCode()) {
						isSyncPopup = isChecked;
					}
				}
			};
			syncExtent.setOnCheckedChangeListener(occl3);
			syncLayers.setOnCheckedChangeListener(occl3);
			syncPopups.setOnCheckedChangeListener(occl3);
		} catch (Exception e) {

		}
	}

	public synchronized int getState() {
		return state;
	}

	protected void getConnected(final String addr, final String name) {
		handler.post(new Runnable() {
			public void run() {
				getActivity().findViewById(R.id.bluetooth_mode_container).setVisibility(View.GONE);
				getActivity().findViewById(R.id.bluetooth_option_container).setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.disconnect).setVisibility(View.VISIBLE);
				selfDeviceStatus.setText(R.string.bluetooth_connected);
				if (peerList == null) {
					peerList = (LinearLayout) getActivity().findViewById(R.id.peer_list);
				}
				peerList.removeAllViews();
				View view = layoutInflater.inflate(R.layout.device_cell, null);
				view.setClickable(true);
				TextView deviceName = (TextView) view.findViewById(R.id.device_name);
				TextView deviceDetail = (TextView) view.findViewById(R.id.device_details);
				TextView deviceStatus = (TextView) view.findViewById(R.id.device_status);
				deviceName.setText(name);
				deviceDetail.setText(addr);
				deviceStatus.setText(R.string.bluetooth_connected);
				peerList.addView(view);
				toast(getString(R.string.bluetooth_connected));
			}
		});
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

		}
	}

	private void startDiscovery() {
		clearThreads();
		searchPeers();
		toast("Searching Devices...");
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
				selfDeviceStatus.setText(R.string.bluetooth_not_connected);
				toast(getString(R.string.bluetooth_connect_fail));
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
					selfDeviceStatus.setText(getActivity().getString(R.string.bluetooth_not_connected));
					toast("Disconnecting...");
					getActivity().findViewById(R.id.mode_listen).performClick();
					startListen();

					getActivity().findViewById(R.id.bluetooth_mode_container).setVisibility(View.VISIBLE);
					getActivity().findViewById(R.id.disconnect).setVisibility(View.GONE);
					getActivity().findViewById(R.id.bluetooth_option_container).setVisibility(View.GONE);
				} catch (NullPointerException e) {

				}
			}
		});
	}

	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		clearThreads();
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		handler.postDelayed(new Runnable() {
			public void run() {
				try {
					write((messageBuilder(CONNECT_REQUEST, new String[] { bluetoothAdapter.getAddress(), bluetoothAdapter.getName(),
							((MapViewerActivity) getActivity()).ID })).getBytes());
				} catch (NullPointerException e) {
					disconnect();
				}
			}
		}, 1500);
	}

	private void searchPeers() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivity(discoverableIntent);
		peerList.removeAllViews();
		peers = new ArrayList<BluetoothDevice>();
		searchPairedDevices();
		searchNewDevices();
	}

	private void searchPairedDevices() {
		try {
			pairedDevices = bluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					peers.add(device);
					updatePeers(device);
				}
			}
		} catch (Exception e) {

		}
	}

	private boolean searchNewDevices() {
		try {
			bluetoothAdapter.startDiscovery();
		} catch (Exception e) {

		}
		return true;
	}

	protected void toast(final String message) {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void updatePeers(final BluetoothDevice device) {
		View view = layoutInflater.inflate(R.layout.device_cell, null);
		view.setClickable(true);
		TextView deviceName = (TextView) view.findViewById(R.id.device_name);
		TextView deviceDetail = (TextView) view.findViewById(R.id.device_details);
		final TextView deviceStatus = (TextView) view.findViewById(R.id.device_status);
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
				selfDeviceStatus.setText(R.string.bluetooth_connecting);
			}
		});
		view.setTag(device.getAddress());
		if (findPeerInList(device.getAddress()) == null) {
			peerList.addView(view);
		}
	}

	protected View findPeerInList(String address) {
		for (int i = 0; i < peerList.getChildCount(); i++) {
			if (peerList.getChildAt(i) != null && peerList.getChildAt(i).getTag() != null && ((String) peerList.getChildAt(i).getTag()).equals(address)) {
				return peerList.getChildAt(i);
			}
		}
		return null;
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

	private class PeerListAdapter extends ArrayAdapter<BluetoothDevice> {
		public PeerListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = layoutInflater.inflate(R.layout.device_cell, null);
			}
			BluetoothDevice device = peers.get(position);
			TextView deviceName = (TextView) view.findViewById(R.id.device_name);
			TextView deviceDetail = (TextView) view.findViewById(R.id.device_details);
			TextView deviceStatus = (TextView) view.findViewById(R.id.device_status);
			deviceName.setText(device.getName());
			deviceDetail.setText(device.getAddress());
			deviceStatus.setText(getBondedStatus(device.getBondState()));
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

				}
			});
			return view;
		}
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
		getActivity().registerReceiver(bluetoothBroadcastReceiver, filter);
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

	public String messageBuilder(String type, String[] msg) {
		String result = "";
		if (type.equals(CONNECT_REQUEST)) {
			result += CONNECT_REQUEST + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += msg[i] + SPLITER;
			}
			result += CONNECT_REQUEST;
		} else if (type.equals(LAYER_CHANGE)) {
			result += LAYER_CHANGE + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += msg[i] + SPLITER;
			}
			result += LAYER_CHANGE;
		}
		return result;
	}

	public String messageBuilder(String type, double[] msg) {
		String result = "";
		if (type.equals(EXTENT_CHANGE)) {
			result += EXTENT_CHANGE + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += Double.toString(msg[i]) + SPLITER;
			}
			result += EXTENT_CHANGE;
		} else if (type.equals(SINGLE_TAP)) {
			result += SINGLE_TAP + SPLITER;
			for (int i = 0; i < msg.length; i++) {
				result += Double.toString(msg[i]) + SPLITER;
			}
			result += SINGLE_TAP;
		}
		return result;
	}

	// public void write(BluetoothMessage sa) {
	// if (connectedThread != null) {
	// ConnectedThread r;
	// synchronized (this) {
	// r = connectedThread;
	// }
	// r.write(sa);
	// }
	// }

	public class ServerThread extends Thread {
		private final BluetoothServerSocket serverSocket;

		public ServerThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid);
			} catch (Exception e) {
			}
			serverSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			Log.i("bluetooth", "listener running");
			while (true) {
				try {
					socket = serverSocket.accept();
					// InputStream is = socket.getInputStream();
					// ObjectInputStream ois = new ObjectInputStream(is);
					// try {
					// BluetoothMessage bm = (BluetoothMessage)
					// ois.readObject();
					// Log.i("bluetooth", "get message " + bm.type.toString() +
					// " addr " + bm.address + " name "
					// + bm.name + " id " + bm.WebMapID);
					// } catch (ClassNotFoundException e) {
					// e.printStackTrace();
					// }
				} catch (IOException e) {
					break;
				}
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					// manageConnectedSocket(socket);
					try {
						// TODO connect
						if (connectedThread == null) {
							connected(socket, socket.getRemoteDevice());
						}
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		public void cancel() {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice device;

		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
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
				// OutputStream os = socket.getOutputStream();
				// ObjectOutputStream oos = new ObjectOutputStream(os);
				// oos.writeObject(new
				// BluetoothMessage(BluetoothMessageType.RequestConnection,
				// bluetoothAdapter
				// .getAddress(), bluetoothAdapter.getName(),
				// ((MapViewerActivity) getActivity()).ID));
				// Log.i("bluetooth", "socket connected");
			} catch (Exception connectException) {
				Log.e("bluetooth", "connect fail");
				try {
					socket.close();
					connectFail(device);
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			// manageConnectedSocket(mmSocket);
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
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream is;
		// private final ObjectInputStream ois;
		private final OutputStream os;

		// private final ObjectOutputStream oos;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream isTmp = null;
			OutputStream osTmp = null;
			// ObjectOutputStream oosTmp = null;
			// ObjectInputStream oisTmp = null;

			try {
				isTmp = socket.getInputStream();
				// oisTmp = new ObjectInputStream(isTmp);
				osTmp = socket.getOutputStream();
				// oosTmp = new ObjectOutputStream(osTmp);
			} catch (IOException e) {
				Log.e("bluetooth", "temp sockets not created", e);
			}

			is = isTmp;
			os = osTmp;
			// ois = oisTmp;
			// oos = oosTmp;
			isReady = true;
		}

		public void run() {
			Log.i("bluetooth", "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = is.read(buffer);
					Log.i("bl", "new bl msg received");
					listener.onBluetoothChange(new String(buffer), 0);

					// BluetoothMessage bm = (BluetoothMessage)
					// ois.readObject();
					// Log.i("bl", "addr " + bm.address);
				} catch (Exception e) {
					Log.e("bluetooth", "disconnected", e);
					disconnect();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				os.write(buffer);
			} catch (IOException e) {
				Log.e("bluetooth", "Exception during write", e);
			}
		}

		// public void write(BluetoothMessage bm) {
		// try {
		// oos.writeObject(bm);
		// } catch (IOException e) {
		// Log.e("bluetooth", "Exception during write", e);
		// }
		// }

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e("bluetooth", "close() of connect socket failed", e);
			}
		}
	}

	@SuppressWarnings("unused")
	private class BluetoothMessage implements Serializable {

		private static final long serialVersionUID = 4L;

		private BluetoothMessageType type;
		private String address, name, WebMapID;
		private double x, y, scale, rotationAngle;

		public BluetoothMessage(BluetoothMessageType bmt, String address, String name, String WebMapID) {
			this.type = bmt;
			this.address = address;
			this.name = name;
			this.WebMapID = WebMapID;
		}

		public BluetoothMessage(BluetoothMessageType bmt, double x, double y, double scale, double rotationAngle) {
			this.type = bmt;
			this.x = x;
			this.y = y;
			this.rotationAngle = rotationAngle;
		}

		public BluetoothMessage() {
			this.address = "ads";
		}
	}

	@SuppressLint("ValidFragment")
	protected class BluetoothConnectConfirm extends DialogFragment {
		protected int type;
		protected String message;
		protected String ID;

		public BluetoothConnectConfirm(String msg, int type, String ID) {
			this.type = type;
			this.message = msg;
			this.ID = ID;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					listener.onBluetoothChange(ID, 1);
				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					switch (type) {
					case 0:
						// TODO disconnect
						break;

					default:
						break;
					}
				}
			});
			return builder.create();
		}
	}
}

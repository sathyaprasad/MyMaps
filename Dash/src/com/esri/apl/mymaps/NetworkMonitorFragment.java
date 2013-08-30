package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class NetworkMonitorFragment extends Fragment {
	private OnNetworkChangeListener listener;
	private IntentFilter newworkStatusFilter;
	private NetworkMonitor networkMonitor;
	public boolean isConnected;

	public NetworkMonitorFragment() {
		isConnected = true;
	}

	public interface OnNetworkChangeListener {
		public void onNetworkChange(Boolean isConnected);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		newworkStatusFilter = new IntentFilter();
		newworkStatusFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		networkMonitor = new NetworkMonitor();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnNetworkChangeListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(networkMonitor);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(networkMonitor, newworkStatusFilter);
	}

	public class NetworkMonitor extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo == null) {
				if (isConnected) {
					isConnected = false;
					listener.onNetworkChange(false);
				}
			} else {
				if (!isConnected) {
					isConnected = true;
					listener.onNetworkChange(true);
				}
			}
		}
	}
}

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
	private boolean isConnected;

	public interface OnNetworkChangeListener {
		public void onNetworkChange(Boolean isConnected);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		isConnected = true;
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

	/* notify the activity when the network connectivity changes */
	public class NetworkMonitor extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if ((netInfo == null && isConnected)
					|| (netInfo != null && !isConnected)) {
				isConnected = !isConnected;
				listener.onNetworkChange(isConnected);
			}
		}
	}

	/* make a toast when the network connectivity changes */
	public void processNetworkChange() {
		String toast = null;
		if (isConnected) {
			toast = getString(R.string.internet_connected);
		} else {
			toast = getString(R.string.internet_disconnected);
		}
		Utility.toast(getActivity(), toast);
	}
}

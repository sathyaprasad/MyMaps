package com.esri.apl.mymaps;

import java.nio.charset.Charset;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class NFCControlFragment extends Fragment implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private OnNFCDisabledListener listener = null;
	private NfcAdapter nfcAdapter;
	private static boolean isDisabled;

	public interface OnNFCDisabledListener {
		public void onNFCDisabled(boolean isAvailable);
	}

	public static NFCControlFragment newInstance(String webmapID) {
		NFCControlFragment nfcControlFragment = new NFCControlFragment();
		Bundle args = new Bundle();
		args.putString("webmapID", webmapID);
		nfcControlFragment.setArguments(args);
		return nfcControlFragment;
	}

	public void enable() {
		isDisabled = false;
	}

	public void disable() {
		isDisabled = true;
	}

	public String getWebmapID() {
		return "mymaps://" + getArguments().getString("webmapID", "");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Activity activity = getActivity();
		super.onCreate(savedInstanceState);
		isDisabled = true;
		nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(this, activity);
			nfcAdapter.setOnNdefPushCompleteCallback(this, activity);
		}
		Log.i("nfc", "nfc message " + getWebmapID());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnNFCDisabledListener) activity;
		} catch (ClassCastException e) {
//			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (nfcAdapter == null && !nfcAdapter.isEnabled()) {
			Log.e("NFC", "NFC is not available");
			return;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onNdefPushComplete(NfcEvent event) {
		Log.i("NFC", "onNdefPushComplete");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public NdefMessage createNdefMessage(NfcEvent event) {
		if (!isDisabled) {
			return new NdefMessage(new NdefRecord[] { new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, getWebmapID().getBytes(Charset.forName("US-ASCII")),
					new byte[0], new byte[0]) });
		} else {
			return null;
		}
	}

	// public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
	// byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
	// NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	// mimeBytes, new byte[0], payload);
	// return mimeRecord;
	// }

}

package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.Fragment;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;

import java.nio.charset.Charset;

public class NFCControlFragment extends Fragment implements
		CreateNdefMessageCallback {
	private NfcAdapter nfcAdapter;
	private static boolean isDisabled;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Activity activity = getActivity();
		super.onCreate(savedInstanceState);
		isDisabled = true;
		nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(this, activity);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (nfcAdapter == null && !nfcAdapter.isEnabled()) {
			Utility.toast(getActivity(), "NFC is not available or not enabled");
			return;
		}
	}

	public NdefMessage createNdefMessage(NfcEvent event) {
		if (!isDisabled) {
			return new NdefMessage(new NdefRecord[] { new NdefRecord(
					NdefRecord.TNF_ABSOLUTE_URI, getWebmapID().getBytes(
							Charset.forName("US-ASCII")), new byte[0],
					new byte[0]) });
		} else {
			return null;
		}
	}

}

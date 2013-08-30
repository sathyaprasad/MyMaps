package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FolderFragment extends Fragment {
	private OnFolderItemClickListener listener;

	public interface OnFolderItemClickListener {
		public void onFolderItemClicked();
	}

	public static FolderFragment newInstance() {
		FolderFragment folderFragment = new FolderFragment();
		Bundle args = new Bundle();
		folderFragment.setArguments(args);
		return folderFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

		return null;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnFolderItemClickListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
}

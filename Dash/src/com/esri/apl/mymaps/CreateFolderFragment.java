package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CreateFolderFragment extends DialogFragment {
	private OnNewFolderCreatedListener listener;

	public interface OnNewFolderCreatedListener {
		public void OnNewFolderCreated(String newFolderName);
	}

	public static CreateFolderFragment newInstance() {
		CreateFolderFragment fragment = new CreateFolderFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		final View view = inflater.inflate(R.layout.add_folder_dialog_layout, container, false);
		ImageView closeButton = (ImageView) view.findViewById(R.id.new_folder_close_icon);
		Button cancelButton = (Button) view.findViewById(R.id.new_folder_cancel);
		OnClickListener closeListener = new OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		};
		closeButton.setOnClickListener(closeListener);
		cancelButton.setOnClickListener(closeListener);

		final EditText newFolderName = (EditText) view.findViewById(R.id.new_folder_name);
		newFolderName.setSelection(newFolderName.getText().length());
		Button createButton = (Button) view.findViewById(R.id.new_folder_create);
		createButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String name = newFolderName.getText().toString();
				if (name == null || name.isEmpty()) {
					Toast.makeText(getActivity(), "Folder name can't be empty.", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (name.equals("Home")) {
					Toast.makeText(getActivity(), "Child folder can't be named 'Home'.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (Utility.isFolderNameDuplicated(name)) {
					Toast.makeText(getActivity(), "Folder name already exists.", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				listener.OnNewFolderCreated(name);
				dismiss();
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnNewFolderCreatedListener) activity;
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

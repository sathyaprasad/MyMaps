package com.esri.apl.mymaps;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.apl.mymaps.Status.LongClickOption;

public class LongClickDialogFragment extends DialogFragment {
	private OnLongClickOptionListener listener;

	public interface OnLongClickOptionListener {
		public void onLongclickOptionPicked(LongClickOption longClickOption, String ID, String targetID);
	}

	public static LongClickDialogFragment newInstance(String ID, String type) {
		LongClickDialogFragment fragment = new LongClickDialogFragment();
		Bundle args = new Bundle();
		args.putString("ID", ID);
		args.putString("type", type);
		fragment.setArguments(args);
		return fragment;
	}

	public String getID() {
		return getArguments().getString("ID", null);
	}

	public String getType() {
		return getArguments().getString("type", Status.WEBMAP);
	}

	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		getDialog().setCanceledOnTouchOutside(false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		final String ID = getID();
		final boolean isWebMap = getType().equals(Status.WEBMAP);
		final String[] deleteOptions = getActivity().getResources().getStringArray(R.array.delete_options);
		final View view = inflater.inflate(R.layout.long_click_dialog_layout, container, false);
		final RadioButton deleteRB = (RadioButton) view.findViewById(R.id.long_click_delete_rb);
		final RadioButton moveRB = (RadioButton) view.findViewById(R.id.long_click_move_rb);
		final Button cancelBTN = (Button) view.findViewById(R.id.long_click_cancel);
		final Button enterBTN = (Button) view.findViewById(R.id.long_click_enter);
		final Spinner deleteSP = (Spinner) view.findViewById(R.id.long_click_delete_spinner);
		final Spinner moveSP = (Spinner) view.findViewById(R.id.long_click_move_spinner);
		if (isWebMap) {
			deleteSP.setVisibility(View.GONE);
		} else {
			deleteSP.setVisibility(View.VISIBLE);
			ArrayList<String> deleteOptionsList = new ArrayList<String>();
			for (int i = 0; i < deleteOptions.length; i++) {
				if (!deleteOptions[i].isEmpty()) {
					deleteOptionsList.add(deleteOptions[i]);
				}
			}
			deleteSP.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
					deleteOptionsList));
		}

		OnCheckedChangeListener occl = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (buttonView.getId() == deleteRB.getId()) {
					if (isChecked) {
						moveRB.setChecked(false);
					}
				} else {
					if (isChecked) {
						deleteRB.setChecked(false);
					}
				}
			}
		};
		deleteRB.setOnCheckedChangeListener(occl);

		ArrayList<String> targetFolderList = Utility.getFolderList(ID, isWebMap);
		if (targetFolderList.size() > 0) {
			moveSP.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
					targetFolderList));
			moveRB.setOnCheckedChangeListener(occl);
		} else {
			moveSP.setVisibility(View.GONE);
			moveRB.setVisibility(View.GONE);
		}
		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == enterBTN.getId()) {
					if (deleteRB.isChecked()) {
						if (isWebMap) {
							listener.onLongclickOptionPicked(LongClickOption.Delete, ID, null);
						} else {
							if (deleteSP.getSelectedItem().toString().equals(deleteOptions[0])) {
								listener.onLongclickOptionPicked(LongClickOption.DeleteAll, ID, null);
							} else if (deleteSP.getSelectedItem().toString().equals(deleteOptions[1])) {
								listener.onLongclickOptionPicked(LongClickOption.DeleteAndMoveToParent, ID, null);
							}
						}
					} else if (moveRB.isChecked()) {
						if (moveSP.getSelectedItem() != null) {
							listener.onLongclickOptionPicked(LongClickOption.Move, ID, moveSP.getSelectedItem()
									.toString());
						} else {
							Toast.makeText(getActivity(), "Please choose the folder.", Toast.LENGTH_SHORT).show();
						}

					} else {
						Toast.makeText(getActivity(), "Please choose an option.", Toast.LENGTH_SHORT).show();
					}
				}
				dismiss();
			}
		};
		enterBTN.setOnClickListener(ocl);
		cancelBTN.setOnClickListener(ocl);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnLongClickOptionListener) activity;
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

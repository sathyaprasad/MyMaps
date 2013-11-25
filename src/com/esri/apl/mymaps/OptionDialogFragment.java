package com.esri.apl.mymaps;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.esri.apl.mymaps.Enums.LongClickOption;

public class OptionDialogFragment extends DialogFragment {

	private OnOptionDialogActionListener listener;

	public static final int RESET = 0;
	public static final int LOGIN = 1;
	public static final int LONG_CLICK = 2;
	public static final int CREATE_FOLDER = 3;

	private static final String TYPE = "type";
	private static final String DATA = "data";

	public interface OnOptionDialogActionListener {
		public void onOptionDialogAction(int action, String[] data);
	}

	public static OptionDialogFragment newInstance(int type, String[] data) {
		OptionDialogFragment dialog = new OptionDialogFragment();
		Bundle args = new Bundle();
		args.putInt(TYPE, type);
		args.putStringArray(DATA, data);
		dialog.setArguments(args);
		return dialog;
	}

	public int getType() {
		return getArguments().getInt(TYPE, -1);
	}

	public String[] getData() {
		return getArguments().getStringArray(DATA);
	}

	/* build differnet dialogs depending on the input type parameter */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int type = getType();
		if (type == RESET) {
			return buildResetDialog();
		} else if (type == LOGIN) {
			// build login dialog
			final AlertDialog loginDialog = buildLoginDialog();
			loginDialog.setCancelable(false);
			loginDialog.setCanceledOnTouchOutside(false);
			// disable the back button when login dialog is showing
			loginDialog.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						return true;
					}
					return false;
				}
			});
			loginDialog.setOnShowListener(new OnShowListener() {
				public void onShow(DialogInterface dialog) {
					// override the positive button so the dialog will not be
					// dismissed when the button the pressed
					Button b = loginDialog
							.getButton(AlertDialog.BUTTON_POSITIVE);
					b.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							String username = ((EditText) loginDialog
									.findViewById(R.id.login_username))
									.getText().toString();
							String password = ((EditText) loginDialog
									.findViewById(R.id.login_password))
									.getText().toString();
							if (username == null || password == null
									|| username.isEmpty() || password.isEmpty()) {
								Utility.toast(getActivity(),
										"username and password can't be empty");
							} else {
								listener.onOptionDialogAction(LOGIN,
										new String[] { username, password });
								dismiss();
							}
						}
					});
				}
			});
			return loginDialog;
		} else if (type == LONG_CLICK) {
			// build long click dialog
			return buildLongClickDialog();
		} else if (type == CREATE_FOLDER) {
			// build create folder dialog
			return buildCreateFolderDialog();
		} else {
			return null;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnOptionDialogActionListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	private AlertDialog buildResetDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.reset_title)
				.setMessage(R.string.reset_message)
				.setPositiveButton(R.string.reset_positive,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// tell the activity to reset the app
								listener.onOptionDialogAction(RESET, null);
							}
						}).setNegativeButton(R.string.reset_negative, null);
		return builder.create();
	}

	private AlertDialog buildLongClickDialog() {
		final String ID = getData()[0];
		String title = ID;
		final boolean isWebMap = Boolean.parseBoolean(getData()[1]);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.longclick_dialog_layout, null);

		final RadioButton deleteRadioButton = (RadioButton) view
				.findViewById(R.id.longclick_delete_rb);
		final RadioButton moveRadioButton = (RadioButton) view
				.findViewById(R.id.longclick_move_rb);
		final Spinner deleteSpinner = (Spinner) view
				.findViewById(R.id.longclick_delete_spinner);
		final Spinner moveSpinner = (Spinner) view
				.findViewById(R.id.longclick_move_spinner);

		deleteRadioButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (buttonView.getId() == deleteRadioButton.getId()) {
							if (isChecked) {
								deleteRadioButton.setChecked(false);
							}
						} else {
							if (isChecked) {
								deleteRadioButton.setChecked(false);
							}
						}
					}
				});

		OnCheckedChangeListener changeRadioButtonCheckedStatus = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView.getId() == deleteRadioButton.getId()) {
					if (isChecked) {
						moveRadioButton.setChecked(false);
					}
				} else {
					if (isChecked) {
						deleteRadioButton.setChecked(false);
					}
				}
			}
		};
		deleteRadioButton
				.setOnCheckedChangeListener(changeRadioButtonCheckedStatus);
		moveRadioButton
				.setOnCheckedChangeListener(changeRadioButtonCheckedStatus);

		// generate the UI of the dialog based on the item clicked
		ArrayList<String> targetFolderList = DashboardItem.getFolderList(ID,
				isWebMap);
		if (targetFolderList.size() > 0) {
			moveSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_dropdown_item,
					targetFolderList));
		} else {
			moveSpinner.setVisibility(View.GONE);
			moveRadioButton.setVisibility(View.GONE);
		}
		if (isWebMap) {
			deleteSpinner.setVisibility(View.GONE);
			title = DashboardItem.getWebMapName(ID);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title)
				.setView(view)
				.setPositiveButton(R.string.longclick_positive,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								LongClickOption option = null;
								String targetFolder = null;
								if (deleteRadioButton.isChecked()) {
									if (isWebMap) {
										option = LongClickOption.Delete;
									} else {
										String[] deleteOptions = getActivity()
												.getResources()
												.getStringArray(
														R.array.longclick_delete_options);
										if (deleteSpinner.getSelectedItem()
												.toString()
												.equals(deleteOptions[0])) {
											option = LongClickOption.DeleteAll;
										} else if (deleteSpinner
												.getSelectedItem().toString()
												.equals(deleteOptions[1])) {
											option = LongClickOption.DeleteAndMoveToParent;
										}
									}
								} else if (moveRadioButton.isChecked()) {
									if (moveSpinner.getSelectedItem() != null) {
										option = LongClickOption.Move;
										targetFolder = moveSpinner
												.getSelectedItem().toString();
									} else {
										Utility.toast(
												getActivity(),
												getString(R.string.longclick_no_folder_selected));
										return;
									}
								} else {
									Utility.toast(
											getActivity(),
											getString(R.string.longclick_no_option_selected));
									return;
								}
								listener.onOptionDialogAction(LONG_CLICK,
										new String[] { option.toString(), ID,
												targetFolder });
							}
						}).setNegativeButton(R.string.longclick_negative, null);
		return builder.create();
	}

	private AlertDialog buildLoginDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.login_dialog_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.login_title)
				.setView(view)
				.setCancelable(false)
				.setPositiveButton(R.string.login_positive, null)
				.setNegativeButton(R.string.login_negative,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								getActivity().onBackPressed();
							}
						});
		return builder.create();
	}

	private AlertDialog buildCreateFolderDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(
				R.layout.create_folder_dialog_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.create_folder_title)
				.setView(view)
				.setPositiveButton(R.string.create_folder_positive,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String name = ((EditText) view
										.findViewById(R.id.new_folder_name))
										.getText().toString();
								// check if the folder name already exists
								if (!checkFolderName(name)) {
									return;
								}
								listener.onOptionDialogAction(CREATE_FOLDER,
										new String[] { name });
							}
						})
				.setNegativeButton(R.string.create_folder_negative, null);
		return builder.create();
	}

	private boolean checkFolderName(String name) {
		if (name == null || name.isEmpty()) {
			Utility.toast(getActivity(),
					getString(R.string.create_folder_empty_name));
			return false;
		}
		if (name.equals(getString(R.string.create_folder_home))) {
			Utility.toast(getActivity(),
					getString(R.string.create_folder_home_name));
			return false;
		}
		if (DashboardItem.isFolderNameDuplicated(name)) {
			Utility.toast(getActivity(),
					getString(R.string.create_folder_duplicate_name));
			return false;
		}
		return true;
	}
}

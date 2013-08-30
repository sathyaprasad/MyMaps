package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.SearchManager.OnDismissListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;

public class LoginFragment extends DialogFragment {
	private OnLoginStatusChangeListener listener;
	private String errorMessage;
	private static Boolean isLogedIn;
	private String usernameString;
	private String passwordString;

	// private FrameLayout progress;
	// private RelativeLayout loginControl;

	public interface OnLoginStatusChangeListener {
		public void onLoginStatusChanged(Boolean isLogedIn, String username, String password);
	}

	public static LoginFragment newInstance(String loginUrl) {
		LoginFragment fragment = new LoginFragment();
		Bundle args = new Bundle();
		args.putString("loginUrl", loginUrl);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnLoginStatusChangeListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public String getLoginUrl() {
		return getArguments().getString("loginUrl", "");
	}

	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		isLogedIn = false;
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);
		final View view = inflater.inflate(R.layout.login_layout, container, false);
		final EditText username = (EditText) view.findViewById(R.id.login_username);
		final EditText password = (EditText) view.findViewById(R.id.login_password);
		final FrameLayout titleBar = (FrameLayout) view.findViewById(R.id.login_title_bar);
		// progress = (FrameLayout) view.findViewById(R.id.login_progress);
		// loginControl = (RelativeLayout)
		// view.findViewById(R.id.login_container);
		Button login = (Button) view.findViewById(R.id.login_login);
		ImageView exit = (ImageView) view.findViewById(R.id.login_close_icon);

		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
				listener.onLoginStatusChanged(false, null, null);
			}
		});

		login.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// progress.setVisibility(View.VISIBLE);
				// loginControl.setVisibility(View.GONE);
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
				LinearLayout progress = (LinearLayout) view.findViewById(R.id.login_progress_bar);
				LinearLayout control = (LinearLayout) view.findViewById(R.id.login_control);

				UserCredentials uc = new UserCredentials();
				usernameString = username.getText().toString();
				passwordString = password.getText().toString();
				if (usernameString.isEmpty() || passwordString.isEmpty()) {
					Toast.makeText(getActivity(), getString(R.string.login_empty), Toast.LENGTH_SHORT).show();
					return;
				}
				progress.setVisibility(View.VISIBLE);
				control.setVisibility(View.GONE);
				titleBar.setVisibility(View.GONE);
				uc.setUserAccount(usernameString, passwordString);
				new LoginTask(progress, control, titleBar).execute(uc);
			}
		});
		return view;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("mymaps", "key back");
			dismiss();
			listener.onLoginStatusChanged(false, null, null);
		}
		return true;
	}

	protected class LoginTask extends AsyncTask<UserCredentials, Void, Void> {

		private LinearLayout progress;
		private LinearLayout control;
		private FrameLayout title;

		public LoginTask(LinearLayout progress, LinearLayout control, FrameLayout title) {
			this.progress = progress;
			this.control = control;
			this.title = title;
		}

		@Override
		protected Void doInBackground(UserCredentials... params) {
			// v.setVisibility(View.VISIBLE);
			UserCredentials uc = params[0];
			Portal portal = new Portal(getLoginUrl(), uc);
			try {
				portal.fetchUser();
				isLogedIn = true;
			} catch (EsriSecurityException e) {
				errorMessage = ((EsriSecurityException) e).getMessage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			if (isLogedIn) {
				dismiss();
				listener.onLoginStatusChanged(true, usernameString, passwordString);
			} else {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
				control.setVisibility(View.VISIBLE);
				title.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			}
		}
	}
}

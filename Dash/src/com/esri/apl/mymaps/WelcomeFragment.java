package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class WelcomeFragment extends Fragment {

	private OnShowcaseClickedListener listener;

	public interface OnShowcaseClickedListener {
		public void onShowcaseClicked();
	}

	public static WelcomeFragment newInstance(String message) {
		WelcomeFragment fg = new WelcomeFragment();
		Bundle args = new Bundle();
		args.putString("message", message);
		fg.setArguments(args);
		return fg;
	}

	public String getMessage() {
		return getArguments().getString("message", "");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.welcome_message_layout, container, false);
		TextView messageText = (TextView) view.findViewById(R.id.home_msg);
		messageText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				listener.onShowcaseClicked();
			}
		});
		messageText.setText(getMessage());
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnShowcaseClickedListener) activity;
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

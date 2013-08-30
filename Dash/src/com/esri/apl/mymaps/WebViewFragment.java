package com.esri.apl.mymaps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class WebViewFragment extends Fragment {

	private static final String SCHEMA = "mymaps://";
	private OnWebviewClickListener listener;

	public interface OnWebviewClickListener {
		public void onWebviewClicked(String data);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnWebviewClickListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public static WebViewFragment newInstance(String url) {
		WebViewFragment fragment = new WebViewFragment();
		Bundle args = new Bundle();
		args.putString("URL", url);
		fragment.setArguments(args);
		return fragment;
	}

	public String getUrl() {
		return getArguments().getString("URL", "");
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.web_view_layout, container, false);
		final ProgressBar progressBar = new ProgressBar(getActivity());
		final FrameLayout webviewContainer = (FrameLayout) view.findViewById(R.id.web_view_container);
		final WebView webView = (WebView) view.findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.clearCache(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(SCHEMA)) {
					listener.onWebviewClicked(Utility.getWebMapID(url));
				} else {
					view.loadUrl(url);
				}
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
						Gravity.CENTER);
				webviewContainer.addView(progressBar, lp);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				webviewContainer.removeView(progressBar);
				webView.setVisibility(View.VISIBLE);
			}
		});
		webView.loadUrl(getUrl());
		return view;
	}
}

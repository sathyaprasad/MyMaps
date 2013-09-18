package com.esri.apl.mymaps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.MapView;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Point;

public class Utility {

	/* extract the webmap ID from custom URL, if there is no match, return null */
	public static String extractWebMapID(String customURL) {
		String[] IDFromCustomUrl = customURL.trim().split("mymaps://");
		String[] IDFromUrl = customURL.trim().split("webmap=");
		Pattern p = Pattern.compile("\\W");
		if (IDFromCustomUrl.length == 2) {
			Matcher makeMatch = p.matcher(IDFromCustomUrl[1]);
			if (makeMatch.find() == false) {
				return IDFromCustomUrl[1];
			}
		}
		if (IDFromUrl.length == 2) {
			Matcher makeMatch = p.matcher(IDFromUrl[1]);
			if (makeMatch.find() == false) {
				return IDFromUrl[1];
			}
		}
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	public static String timeFormatter(String format, Long time) {
		return new SimpleDateFormat(format).format(new Date(time));
	}

	public static int pixelScaler(Context context, int size) {
		return (int) (context.getResources().getDisplayMetrics().density * size + 0.5f);
	}

	/* create view from popup */
	public static View PopupToView(Popup popup, MapView mapView) {
		PopupContainer pc = new PopupContainer(mapView);
		pc.addPopup(popup);
		return pc.getPopupContainerView();
	}

	/* setup the text for scale bar */
	public static String scaleFormatter(double rawScale) {
		String result = null;
		if (rawScale < 1000000) {
			result = "Scale: 1:" + Integer.toString((int) rawScale / 1000)
					+ "K";
		} else {
			result = "Scale: 1:" + Integer.toString((int) rawScale / 1000000)
					+ "M";
		}
		return result;
	}

	/* make a toast */
	public static void toast(final Context context, final String message) {
		new Handler().post(new Runnable() {
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/* construct a string to show the current location like home/maps/ */
	public static String getFolderPath() {
		String path = "";
		String parent = Status.CurrentParent;
		while (parent != null) {
			if (path.isEmpty()) {
				path += parent;
			} else {
				path = parent + " / " + path;
			}
			parent = DashboardItem.getParentFolder(parent);
		}
		if (!path.isEmpty()) {
			path = " / " + path;
		}
		return path;
	}

	public static String getActionBarTitle(Context context) {
		String title = "  " + context.getString(R.string.app_name);
		return title + getFolderPath();
	}

	public static boolean isInternetConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNet = connManager.getActiveNetworkInfo();
		if (mNet == null) {
			toast(context, context.getString(R.string.welcome_no_wifi));
			return false;
		}
		return true;
	}

	public static void hideCallout(MapViewerActivity activity) {
		Callout callout = activity.mapView.getCallout();
		if (callout != null && callout.isShowing()) {
			callout.hide();
		}
	}

	public static double booleanToDouble(boolean b) {
		if (b) {
			return 1;
		} else {
			return 0;
		}
	}

	public static boolean doubleToBoolean(double d) {
		if ((int) d == 1) {
			return true;
		}
		return false;
	}

	/* get the center point, scale and rotation of the mapview */
	public static double[] extractMapViewExtent(MapView mapView) {
		return new double[] { mapView.getCenter().getX(),
				mapView.getCenter().getY(), mapView.getScale(),
				mapView.getRotationAngle() };
	}

	/* get x, y coordinates of a point */
	public static double[] extractPoint(Point point, Boolean animated) {
		return new double[] { point.getX(), point.getY(),
				booleanToDouble(animated) };
	}

	/* get the rotation of the mapview */
	public static double[] extractRotation(double degree, Point point,
			Boolean animated) {
		return new double[] { degree, point.getX(), point.getY(),
				booleanToDouble(animated) };
	}

	/* change the background of the icon */
	public static void changeImageViewBackgroundToWhite(Activity activity,
			int id, int drawableID) {
		ImageView icon = (ImageView) activity.findViewById(id);
		icon.setImageDrawable(changeSensorIndicatorBackground(activity,
				drawableID, R.drawable.settings_background_circle));
	}

	/* redraw icon */
	public static Drawable changeSensorIndicatorBackground(Context context,
			int origin, int target) {
		Resources r = context.getResources();
		int width = pixelScaler(context, 48);
		int height = pixelScaler(context, 48);

		Drawable targetDrawable = r.getDrawable(target);
		Bitmap targetBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(targetBitmap);
		targetDrawable.setBounds(0, 0, width, height);
		targetDrawable.draw(c);

		Bitmap originBitmap = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(r, origin), width, height, false);
		Bitmap o = Bitmap
				.createScaledBitmap(originBitmap, width, height, false);
		Bitmap combined = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(combined);
		Bitmap background = Bitmap.createScaledBitmap(targetBitmap, width,
				height, false);
		canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(o, 0, 0, null);
		Drawable d = new BitmapDrawable(r, combined);
		return d;
	}
}

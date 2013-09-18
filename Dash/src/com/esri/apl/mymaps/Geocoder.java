package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.geocode.Locator;
import com.esri.core.tasks.ags.geocode.LocatorFindParameters;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;

public class Geocoder extends
		AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {
	private ProgressDialog progressDialog = null;
	private LinearLayout locationControl = null;
	private MapViewerActivity activity = null;
	private ArrayList<View> geocodingResultViews = null;

	public Geocoder(MapViewerActivity activity, LinearLayout locationControl,
			ProgressDialog progressDialog) {
		this.activity = activity;
		this.locationControl = locationControl;
		this.progressDialog = progressDialog;
	}

	@Override
	protected List<LocatorGeocodeResult> doInBackground(
			LocatorFindParameters... params) {
		geocodingResultViews = new ArrayList<View>();
		List<LocatorGeocodeResult> results = null;
		Locator locator = new Locator(activity.getResources().getString(
				R.string.geocoding_url));
		try {
			results = locator.find(params[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	@Override
	protected void onPostExecute(final List<LocatorGeocodeResult> result) {
		if (result == null || result.size() == 0) {
			Utility.toast(activity,
					activity.getString(R.string.geocoding_no_result));
		} else {
			int result_count = result.size();
			if (result_count > 5) {
				result_count = 5;
			}
			for (int i = 0; i < result_count; i++) {
				final int index = i;
				TextView resultTextView = MapControlUIBuilder
						.buildGeocodingResult(activity, i, result.get(i)
								.getAddress(), result.get(i).getAttributes()
								.get("Type"));
				resultTextView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						activity.graphicsLayer.removeAll();
						// draw the symbol for geocoding result on the map
						Geometry resultLocGeom = result.get(index)
								.getLocation();
						SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
								Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
						Graphic resultLocation = new Graphic(resultLocGeom,
								resultSymbol);
						activity.graphicsLayer.addGraphic(resultLocation);
						// add text for geocoding result on the map
						TextSymbol resultAddress = new TextSymbol(12, result
								.get(index).getAddress(), Color.BLACK);
						resultAddress.setOffsetX(10);
						resultAddress.setOffsetY(10);
						Graphic resultText = new Graphic(resultLocGeom,
								resultAddress);
						activity.graphicsLayer.addGraphic(resultText);
						// center the map to geocoding result
						MapControlWidget.centerMapAt(activity, result
								.get(index).getLocation(), true, true);
					}
				});
				// add the geocoding results into the sidebar
				locationControl.addView(resultTextView);
				geocodingResultViews.add(resultTextView);
			}
			locationControl.setTag(geocodingResultViews);
		}
		progressDialog.dismiss();
	}
}

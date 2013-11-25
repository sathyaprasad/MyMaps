package com.esri.apl.mymaps;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

public class CalloutQuery {
	private MapViewerActivity activity = null;
	private ArrayList<Popup> popupList = null;
	private PopupContainer popupContainer = null;

	public CalloutQuery(MapViewerActivity activity) {
		this.activity = activity;
	}

	/* query all the layers in the mapview for popups */
	public void queryPopup(float x, float y) {
		popupContainer = new PopupContainer(activity.mapView);
		popupList = new ArrayList<Popup>();
		Layer[] layers = activity.mapView.getLayers();
		for (Layer layer : layers) {
			if (!layer.isInitialized() || !layer.isVisible()) {
				continue;
			}
			if (layer instanceof GroupLayer) {
				Layer[] ls = ((GroupLayer) layer).getLayers();
				for (Layer l : ls) {
					queryLayers(l, x, y);
				}
			} else {
				queryLayers(layer, x, y);
			}
		}
	}

	private void queryLayers(Layer layer, float x, float y) {
		int tolerance = 20;
		Point p = activity.mapView.toScreenPoint(new Point(x, y));
		x = (float) p.getX();
		y = (float) p.getY();
		Envelope env = new Envelope(activity.mapView.toMapPoint(x, y),
				20 * activity.mapView.getResolution(),
				20 * activity.mapView.getResolution());
		if (layer instanceof ArcGISFeatureLayer) {
			ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;
			if (featureLayer.getPopupInfo() != null) {
				new RunQueryFeatureLayerTask(x, y, tolerance)
						.execute(featureLayer);
			}
		} else if (layer instanceof ArcGISDynamicMapServiceLayer) {
			ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
			ArcGISLayerInfo[] layerinfos = dynamicLayer.getAllLayers();
			if (layerinfos == null) {
				return;
			}

			for (ArcGISLayerInfo layerInfo : layerinfos) {
				// Obtain PopupInfo for sub-layer.
				PopupInfo popupInfo = dynamicLayer.getPopupInfo(layerInfo
						.getId());
				// Skip sub-layer which is without a popup
				// definition.
				if (popupInfo == null) {
					continue;
				}
				// Check if a sub-layer is visible.
				ArcGISLayerInfo info = layerInfo;
				while (info != null && info.isVisible()) {
					info = info.getParentLayer();
				}
				// Skip invisible sub-layer
				if (info != null && !info.isVisible()) {
					continue;
				}

				// Check if the sub-layer is within the scale range
				double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo
						.getMaxScale() : popupInfo.getMaxScale();
				double minScale = (layerInfo.getMinScale() != 0) ? layerInfo
						.getMinScale() : popupInfo.getMinScale();

				if ((maxScale == 0 || activity.mapView.getScale() > maxScale)
						&& (minScale == 0 || activity.mapView.getScale() < minScale)) {
					// Query sub-layer which is associated with a
					// popup definition and is visible and in scale
					// range.
					new RunQueryDynamicLayerTask(env, layer, layerInfo.getId(),
							dynamicLayer.getSpatialReference())
							.execute(dynamicLayer.getUrl() + "/"
									+ layerInfo.getId());
				}
			}
		}
	}

	// Query feature layer by hit test
	private class RunQueryFeatureLayerTask extends
			AsyncTask<ArcGISFeatureLayer, Void, Graphic[]> {

		private int tolerance;
		private float x;
		private float y;
		private ArcGISFeatureLayer featureLayer;

		public RunQueryFeatureLayerTask(float x, float y, int tolerance) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
		}

		@Override
		protected Graphic[] doInBackground(ArcGISFeatureLayer... params) {
			for (ArcGISFeatureLayer featureLayer : params) {
				this.featureLayer = featureLayer;
				// Retrieve graphic ids near the point.
				int[] ids = featureLayer.getGraphicIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Graphic> graphics = new ArrayList<Graphic>();
					for (int id : ids) {
						// Obtain graphic based on the id.
						Graphic g = featureLayer.getGraphic(id);
						if (g == null)
							continue;
						graphics.add(g);
					}
					// Return an array of graphics near the point.
					return graphics.toArray(new Graphic[0]);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Graphic[] graphics) {
			if (graphics == null || graphics.length == 0) {
				return;
			}
			for (Graphic gr : graphics) {
				Popup popup = featureLayer.createPopup(activity.mapView, 0, gr);
				popupContainer.addPopup(popup);
				popupList.add(popup);
			}
			MapControlUIBuilder.makeCallout(activity, 0, popupList);
		}
	}

	// Query dynamic map service layer by QueryTask
	private class RunQueryDynamicLayerTask extends
			AsyncTask<String, Void, FeatureSet> {
		private Envelope env;
		private SpatialReference sr;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer,
				int subLayerId, SpatialReference sr) {
			super();
			this.env = env;
			this.sr = sr;
			this.layer = layer;
			this.subLayerId = subLayerId;
		}

		@Override
		protected FeatureSet doInBackground(String... urls) {
			for (String url : urls) {
				// Retrieve graphics within the envelope.
				Query query = new Query();
				query.setInSpatialReference(sr);
				query.setOutSpatialReference(sr);
				query.setGeometry(env);
				query.setMaxFeatures(10);
				query.setOutFields(new String[] { "*" });

				QueryTask queryTask = new QueryTask(url);
				try {
					FeatureSet results = queryTask.execute(query);
					return results;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final FeatureSet result) {
			if (result == null) {
				return;
			}
			Graphic[] graphics = result.getGraphics();
			if (graphics == null || graphics.length == 0) {
				return;
			}
			PopupInfo popupInfo = layer.getPopupInfo(subLayerId);
			if (popupInfo == null) {
				return;
			}

			for (Graphic gr : graphics) {
				Popup popup = layer.createPopup(activity.mapView, subLayerId,
						gr);
				popupContainer.addPopup(popup);
				popupList.add(popup);
			}
			MapControlUIBuilder.makeCallout(activity, 0, popupList);
		}
	}

}

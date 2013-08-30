package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.osm.OpenStreetMapLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Bookmark;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureTemplate.DRAWING_TOOL;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;
import com.esri.core.map.Legend;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;

public class MapControlHelper {

	final private MapView mv;
	private float scale;
	private Context context;
	private LinkedList<String> recordList;
	private ArrayList<Legend> legendList;
	private ArrayList<FeatureTemplate> templateList;
	private ArrayList<ArcGISFeatureLayer> featurelayerList;

	public MapControlHelper(final Context context, MapView mv) {
		recordList = new LinkedList<String>();
		this.context = context;
		this.mv = mv;
		scale = context.getResources().getDisplayMetrics().density;
	}

	public boolean foundDuplicate(String str) {
		if (recordList.indexOf(str) == -1) {
			recordList.addFirst(str);
			return false;
		}
		return true;
	}

	public void emptyRecord() {
		recordList = new LinkedList<String>();
	}

	public LinearLayout buildSingleLayerControlContainer(final Layer layer, final int index, final MapViewerActivity mva) {

		boolean containLegend = false;
		boolean visible = false;
		legendList = new ArrayList<Legend>();
		templateList = new ArrayList<FeatureTemplate>();
		featurelayerList = new ArrayList<ArcGISFeatureLayer>();

		// container for the one layer's layer control
		LinearLayout singleLayerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.setMargins(0, 20, 10, 20);
		singleLayerLayout.setLayoutParams(ll);
		singleLayerLayout.setGravity(Gravity.CENTER);
		singleLayerLayout.setOrientation(LinearLayout.VERTICAL);

		// container for the legends
		final LinearLayout legendsControl = new LinearLayout(context);
		legendsControl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		legendsControl.setOrientation(LinearLayout.VERTICAL);

		// add legends here
		ArcGISLayerInfo[] info = null;

		if (layer instanceof GroupLayer) {
			Log.i("appdash", "layer: " + layer.getTitle() + " is GroupLayer.");
			Layer[] ls = ((GroupLayer) layer).getLayers();
			for (Layer l : ls) {
				visible = l.isVisible();
				Log.i("appdash", "sublayer of group layer visible? " + l.isVisible());
				if (l instanceof ArcGISFeatureLayer) {
					Log.i("appdash", "layer: " + l.getTitle() + " is ArcGISFeatureLayer.");
					Log.i("appdash", "layer: " + l.getTitle() + " isShowLegend? " + l.isShowLegend());
					Log.i("appdash", "layer: " + l.getTitle() + " isVisible? " + l.isVisible());
					if (l.isShowLegend()) {
						makeFeatureLayerLegend(l);
					}
				}
				if (l instanceof GroupLayer) {
					for (Layer ll2 : ls) {
						if (ll2.isShowLegend()) {
							makeFeatureLayerLegend(ll2);
						}
					}
				}
			}
		}

		if (layer instanceof ArcGISFeatureLayer) {
			visible = layer.isVisible();
			Log.i("appdash", "layer: " + layer.getTitle() + " is ArcGISFeatureLayer. Has attachment? " + ((ArcGISFeatureLayer) layer).hasAttachments());
			Log.i("appdash", "layer: " + layer.getTitle() + " isShowLegend? " + layer.isShowLegend());
			Log.i("appdash", "layer: " + layer.getTitle() + " isVisible? " + layer.isVisible());
			if (layer.isShowLegend()) {
				makeFeatureLayerLegend(layer);
			}
		}

		if (layer instanceof ArcGISDynamicMapServiceLayer) {
			ArcGISDynamicMapServiceLayer aLayer = (ArcGISDynamicMapServiceLayer) layer;
			info = aLayer.getAllLayers();
			visible = layer.isVisible();
		}

		if (layer instanceof ArcGISTiledMapServiceLayer) {
			ArcGISTiledMapServiceLayer aLayer = (ArcGISTiledMapServiceLayer) layer;
			info = aLayer.getAllLayers();
			visible = layer.isVisible();
		}

		if ((info != null && info.length != 0)) {
			emptyRecord();
			for (int j = 0; j < info.length; j++) {
				List<Legend> legends = info[j].getLegend();
				Log.i("appdash", "legends");
				if (legends != null && legends.size() != 0 && legends.get(0).getLabel() != "") {
					containLegend = true;
					Log.i("appdash", "legends not null or size = 0");
					for (int k = 0; k < legends.size(); k++) {
						String labelText = legends.get(k).getLabel();
						if (foundDuplicate(labelText)) {
							Log.i("appdash", "found duplicate label");
							continue;
						}
						LinearLayout legendLayout = legendLayoutMaker(legends.get(k));
						legendsControl.addView(legendLayout);
					}
				}
			}
		}

		Log.i("appdash", "legendList.size(): " + legendList.size());
		if (legendList.size() != 0) {
			containLegend = true;
			for (int k = 0; k < legendList.size(); k++) {
				String labelText = legendList.get(k).getLabel();
				Log.i("appdash", "label: " + labelText);
				LinearLayout legendLayout = null;
				if (labelText == null || labelText.length() == 0) {
					legendLayout = legendLayoutMaker(new Legend(legendList.get(k).getImage(), layer.getTitle()));
				} else {
					legendLayout = legendLayoutMaker(legendList.get(k));
				}
				legendsControl.addView(legendLayout);
			}
		}

		final LinearLayout switchAndTitle = new LinearLayout(context);
		final ImageView expand = new ImageView(context);
		final TextView layerName = new TextView(context);
		final CheckBox layerSwitch = new CheckBox(context);

		OnClickListener ocl = new OnClickListener() {

			public void onClick(View v) {
				if (legendsControl == null || legendsControl.getChildCount() == 0) {
					Log.i("appdash", "legend control child count: 0");
					return;
				} else {
					Log.i("appdash", "legend control child count: " + Integer.toString(legendsControl.getChildCount()));
					if (legendsControl.getVisibility() == View.GONE) {
						legendsControl.setVisibility(View.VISIBLE);
						expand.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.expanded32d));
					} else {
						legendsControl.setVisibility(View.GONE);
						expand.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.collapsed32d));
					}
				}
			}
		};

		// title and switch
		switchAndTitle.setOrientation(LinearLayout.HORIZONTAL);
		switchAndTitle.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// setup the checkbox
		layerSwitch.setChecked(visible);
		layerSwitch.setGravity(Gravity.LEFT);
		layerSwitch.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 4));
		layerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mv.getLayer(index) instanceof GroupLayer) {
					Layer[] ls = ((GroupLayer) mv.getLayer(index)).getLayers();
					for (Layer l : ls) {
						l.setVisible(isChecked);
					}
				} else {
					mv.getLayer(index).setVisible(!mv.getLayer(index).isVisible());
				}
				try {
					Log.i("bl", "layer change send");
					mva.messageBuilder(BluetoothFragment.LAYER_CHANGE, new String[] { Integer.toString(index), Boolean.toString(isChecked) });
				} catch (Exception e) {
				}
			}
		});
		switchAndTitle.addView(layerSwitch);

		// add title
		layerName.setText(layer.getTitle());
		layerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		layerName.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		layerName.setOnClickListener(ocl);
		switchAndTitle.addView(layerName);

		// add expand button
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 4);
		lp.setMargins(0, 8, 0, 0);
		expand.setLayoutParams(lp);
		if (containLegend) {
			expand.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.expanded32d));
			expand.setOnClickListener(ocl);
			expand.setTag("expand");
		} else {
			// expand.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
			// R.drawable.layers36d));
		}
		switchAndTitle.addView(expand);

		singleLayerLayout.addView(switchAndTitle);

		if (containLegend) {
			singleLayerLayout.addView(legendsControl);
		}

		return singleLayerLayout;
	}

	private LinearLayout legendLayoutMaker(Legend legend) {
		LinearLayout legendLayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(20, 0, 0, 0);
		legendLayout.setLayoutParams(lp);
		legendLayout.setOrientation(LinearLayout.HORIZONTAL);
		legendLayout.setGravity(Gravity.CENTER_VERTICAL);

		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(legend.getImage());
		imageView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 7));
		legendLayout.addView(imageView);

		TextView legendLabel = new TextView(context);
		legendLabel.setText(legend.getLabel());
		legendLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		legendLabel.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		legendLayout.addView(legendLabel);
		return legendLayout;
	}

	public void makeFeatureLayerLegend(Layer l) {
		if (l instanceof ArcGISFeatureLayer) {
			ArcGISFeatureLayer featurelayer1 = (ArcGISFeatureLayer) l;

			FeatureType[] types = featurelayer1.getTypes();
			for (FeatureType featureType : types) {
				FeatureTemplate[] templates = featureType.getTemplates();
				for (FeatureTemplate featureTemplate : templates) {
					// FeatureTemplate.DRAWING_TOOL dt =
					// featureTemplate.getDrawingTool();
					String name = featureTemplate.getName();
					// Graphic g =
					// featurelayer1.createFeatureWithTemplate(featureTemplate,
					// null);
					// Renderer<Graphic> renderer = featurelayer1.getRenderer();
					// Symbol symbol = renderer.getSymbol(g);
					Bitmap bitmap = createSymbolBitmap(featurelayer1, featureTemplate);

					legendList.add(new Legend(bitmap, name));
					// legendList.add(new Legend(bitmap, name, symbol));
					templateList.add(featureTemplate);
					featurelayerList.add((ArcGISFeatureLayer) l);
				}
			}
			if (legendList.size() == 0) { // no types
				FeatureTemplate[] templates = featurelayer1.getTemplates();
				for (FeatureTemplate featureTemplate : templates) {
					String name = featureTemplate.getName();
					// Graphic g =
					// featurelayer1.createFeatureWithTemplate(featureTemplate,
					// null);
					// Renderer<Graphic> renderer = featurelayer1.getRenderer();
					// Symbol symbol = renderer.getSymbol(g);
					Bitmap bitmap = createSymbolBitmap(featurelayer1, featureTemplate);
					legendList.add(new Legend(bitmap, name));
					// legendList.add(new Legend(bitmap, name, symbol));
					templateList.add(featureTemplate);
					featurelayerList.add((ArcGISFeatureLayer) l);
				}
			}
		}
	}

	private Bitmap createSymbolBitmap(ArcGISFeatureLayer featurelayer, FeatureTemplate featureTemplate) {
		// determine feature type
		FeatureTemplate.DRAWING_TOOL drawing_tool = featureTemplate.getDrawingTool();
		Geometry geometry = null;
		if (drawing_tool == DRAWING_TOOL.POLYGON) {
			Polygon polygon = new Polygon();
			polygon.startPath(0, 0);
			polygon.lineTo(0, 40);
			polygon.lineTo(40, 40);
			polygon.lineTo(40, 0);
			polygon.lineTo(0, 0);
			geometry = polygon;
		} else if (drawing_tool == DRAWING_TOOL.LINE) {
			Polyline polyline = new Polyline();
			polyline.startPath(1, 1);
			polyline.lineTo(39, 39);
			geometry = polyline;
		} else if (drawing_tool == DRAWING_TOOL.POINT)
			geometry = new Point(20, 20);

		Graphic g = featurelayer.createFeatureWithTemplate(featureTemplate, null);
		Renderer<Graphic> renderer = featurelayer.getRenderer();
		Symbol symbol = renderer.getSymbol(g); // g.getSymbol();

		Bitmap bitmap = featurelayer.createSymbolImage(symbol, geometry, 40, 40, Color.TRANSPARENT);

		return bitmap;
	}

	public LinearLayout setupBasemapSwitch(final Layer layer, final MapViewerActivity mva) {

		// boolean defaultBaseMap = false;
		final String currentBasemapUrl = layer.getUrl();
		String currentBasemapTitle = layer.getTitle() + " (Default)";
		Log.i("appdash", "Title: " + currentBasemapTitle);
		Log.i("appdash", "Url: " + currentBasemapUrl);
		Log.i("appdash", "id: " + layer.getID());

		HashMap<String, String> baseMap = new HashMap<String, String>();
		baseMap.put(context.getString(R.string.WORLD_STREET_MAP), context.getString(R.string.StreetMapMenu));
		baseMap.put(context.getString(R.string.WORLD_TOPO_MAP), context.getString(R.string.TopoMenu));
		baseMap.put(context.getString(R.string.WORLD_NATGEO_MAP), context.getString(R.string.NatGeoMenu));
		baseMap.put(context.getString(R.string.OCEAN_BASEMAP), context.getString(R.string.OceanMenu));
		baseMap.put(context.getString(R.string.OSMMenu), context.getString(R.string.OSMMenu));

		if (layer instanceof OpenStreetMapLayer) {
			baseMap.remove(context.getString(R.string.OSMMenu));
		} else if (baseMap.keySet().contains(layer.getUrl())) {
			// defaultBaseMap = true;
			currentBasemapTitle = baseMap.get(currentBasemapUrl) + " (Default)";
			baseMap.remove(currentBasemapUrl);
		} else {

		}

		// container for basemap layer control
		LinearLayout basemapLayerLayout = new LinearLayout(context);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.setMargins(0, 20, 10, 20);
		basemapLayerLayout.setLayoutParams(ll);
		basemapLayerLayout.setGravity(Gravity.CENTER);
		basemapLayerLayout.setOrientation(LinearLayout.VERTICAL);

		final RadioGroup rg = new RadioGroup(context);
		rg.setId(R.id.basemap_switch);
		rg.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		final RadioButton defaultbasemapButton = new RadioButton(context);
		defaultbasemapButton.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		defaultbasemapButton.setTextColor(Color.WHITE);
		defaultbasemapButton.setText(currentBasemapTitle);
		defaultbasemapButton.setChecked(true);
		defaultbasemapButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					return;
				}
				mva.messageBuilder(BluetoothFragment.BASEMAP_CHANGE, new String[] { (String) buttonView.getText() });
				switchRadioButton(defaultbasemapButton, rg);
				mv.removeLayer(0);
				mv.addLayer(layer, 0);
			}
		});
		rg.addView(defaultbasemapButton);

		Set<String> keys = baseMap.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			final String key = iterator.next();
			final RadioButton rb = new RadioButton(context);
			rb.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rb.setTextColor(Color.WHITE);
			if (key == null) {
				rb.setText(currentBasemapTitle);
				rb.setChecked(true);
			} else if (currentBasemapUrl != null && currentBasemapUrl.equals(key)) {
				rb.setText(baseMap.get(key));
				rb.setChecked(true);
			} else {
				rb.setText(baseMap.get(key));
				rb.setChecked(false);
			}

			rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (!isChecked) {
						return;
					}
					mva.messageBuilder(BluetoothFragment.BASEMAP_CHANGE, new String[] { (String) buttonView.getText() });
					switchRadioButton(rb, rg);
					mv.removeLayer(0);
					if (key.equals(context.getString(R.string.OSMMenu))) {
						OpenStreetMapLayer osmBasemap = new OpenStreetMapLayer();
						mv.addLayer(osmBasemap, 0);
					} else {
						ArcGISTiledMapServiceLayer basemap = new ArcGISTiledMapServiceLayer(key);
						mv.addLayer(basemap, 0);
					}
				}
			});

			rg.addView(rb);
		}

		basemapLayerLayout.addView(rg);

		return basemapLayerLayout;
	}

	private void switchRadioButton(final RadioButton rb, final RadioGroup rg) {
		for (int i = 0; i < rg.getChildCount(); i++) {
			RadioButton rbtmp = (RadioButton) rg.getChildAt(i);
			rbtmp.setChecked(false);
		}
		((RadioButton) rb).setChecked(true);
	}

	public LinearLayout buildBookmarkControl(LinearLayout bookmarksControl, final List<Bookmark> bookmarks) {
		if (bookmarks == null || bookmarks.size() == 0) {
			TextView noBookmark = new TextView(context);
			noBookmark.setText(context.getString(R.string.no_bookmark));
			noBookmark.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			noBookmark.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			noBookmark.setGravity(Gravity.CENTER);
			bookmarksControl.addView(noBookmark);
			return null;
		}
		for (int i = 0; i < bookmarks.size(); i++) {
			final int index = i;
			LinearLayout singleBookmark = new LinearLayout(context);
			singleBookmark.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			singleBookmark.setOrientation(LinearLayout.HORIZONTAL);
			singleBookmark.setGravity(Gravity.CENTER_VERTICAL);

			final ImageView image = new ImageView(context);
			image.setLayoutParams(new TableLayout.LayoutParams((int) (scale * 18.0f + 0.5f), (int) (scale * 18.0f + 0.5f), 5));
			image.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bookmark));
			singleBookmark.addView(image);

			final TextView bookmarkName = new TextView(context);
			bookmarkName.setText(bookmarks.get(i).getName());
			bookmarkName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			bookmarkName.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
			bookmarkName.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					mv.setExtent(bookmarks.get(index).getExtent());
					Log.i("appdash", "bookmark clicked");
				}
			});
			singleBookmark.addView(bookmarkName);

			bookmarksControl.addView(singleBookmark);
		}
		return null;
	}

	public TextView buildGeocodingResult(int rank, String address, String type) {
		final TextView singleResult = new TextView(context);
		int padding = Utility.pixelScaler(context, 10);
		singleResult.setPadding(padding, padding, padding, padding);
		if (!type.isEmpty()) {
			singleResult.setText(Integer.toString(rank + 1) + ". Address: " + address + "\nType: " + type);
		} else {
			singleResult.setText(Integer.toString(rank + 1) + ". Address: " + address);
		}
		singleResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		singleResult.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		return singleResult;
	}
}

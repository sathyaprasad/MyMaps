package com.esri.apl.mymaps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.osm.OpenStreetMapLayer;
import com.esri.android.map.popup.Popup;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Bookmark;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureTemplate.DRAWING_TOOL;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;
import com.esri.core.map.Legend;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.WebMap;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.ags.geocode.LocatorFindParameters;

public class MapControlUIBuilder {
	private static ArrayList<FeatureTemplate> templateList = null;
	private static ArrayList<ArcGISFeatureLayer> featurelayerList = null;

	// callout
	public static Callout callout = null;
	public static Point pointQueried = null;
	public static ArrayList<Popup> popupList = null;

	private static int getDimension(Context context, int resID) {
		return (int) context.getResources().getDimension(resID);
	}

	/* build the title bar with icon and text in map viewer activity side bar */
	public static LinearLayout titleWithImage(Context context, String text,
			int draw, boolean addTopMargin) {
		LinearLayout titleWithImagelayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int topMargin = getSize(context, 10);
		int bottomMargin = getSize(context, 5);
		int iconSize = getSize(context, 36);
		if (addTopMargin) {
			lp.setMargins(0, topMargin, 0, bottomMargin);
		} else {
			lp.setMargins(0, 0, 0, bottomMargin);
		}
		titleWithImagelayout.setLayoutParams(lp);
		titleWithImagelayout.setOrientation(LinearLayout.HORIZONTAL);
		titleWithImagelayout.setGravity(Gravity.CENTER);
		titleWithImagelayout.setBackgroundColor(Color.TRANSPARENT);
		titleWithImagelayout.setPadding(0, topMargin, 0, bottomMargin);

		ImageView imageView = new ImageView(context);
		imageView.setImageDrawable(context.getResources().getDrawable(draw));
		imageView.setLayoutParams(new LinearLayout.LayoutParams(iconSize,
				iconSize));
		imageView.setPadding(0, 0, topMargin, 0);

		titleWithImagelayout.addView(imageView);

		TextView legendLabel = new TextView(context);
		legendLabel.setText(text);
		legendLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		legendLabel.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		titleWithImagelayout.addView(legendLabel);

		return titleWithImagelayout;
	}

	private static int getSize(Context context, float size) {
		return (int) (context.getResources().getDisplayMetrics().density * size + 0.5);
	}

	/* build two buttons in map viewer activity side bar */
	public static LinearLayout twoButtons(Context context, String text1,
			int ID1, String text2, int ID2) {
		LinearLayout container = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		container.setLayoutParams(lp);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.CENTER);

		GradientDrawable drawable = new GradientDrawable();
		drawable.setShape(GradientDrawable.RECTANGLE);
		drawable.setStroke(1, Color.WHITE);
		drawable.setColor(Color.BLACK);

		Button button1 = new Button(context);
		button1.setText(text1);
		button1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		button1.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		button1.setId(ID1);
		container.addView(button1);

		Button button2 = new Button(context);
		button2.setText(text2);
		button2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		button2.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		button2.setId(ID2);
		container.addView(button2);

		return container;
	}

	public static View makeLine(Context context) {
		View line = new View(context);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		line.setLayoutParams(lp);
		line.setBackgroundColor(Color.LTGRAY);
		return line;
	}

	public static View makeEmptyVerticalView(Context context, int height) {
		View space = new View(context);
		space.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				height));
		space.setBackgroundColor(Color.BLACK);
		space.setAlpha(0.5f);
		return space;
	}

	public static LinearLayout makeBlocks(Context context, String title,
			int titleSize, String text, int textSize) {
		LinearLayout container = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, 0, getSize(context, 15));
		container.setLayoutParams(lp);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

		TextView textBlock = new TextView(context);
		textBlock.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

		Spannable spannable = new SpannableString(title + "\n" + text);
		spannable.setSpan(new RelativeSizeSpan(1.0f), 0, title.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		textBlock.setText(spannable);
		textBlock.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		container.addView(textBlock);

		return container;
	}

	/* build the textview to show there is no operational layer available */
	public static TextView buildNoLayerText(Context context) {
		float scale = context.getResources().getDisplayMetrics().density;
		TextView noLayerTextView = new TextView(context);
		noLayerTextView.setText(context
				.getString(R.string.layers_no_operational_layer));
		noLayerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, (int) (scale * 10.0f + 0.5f), 0,
				(int) (scale * 15.0f + 0.5f));
		noLayerTextView.setLayoutParams(lp);
		noLayerTextView.setGravity(Gravity.CENTER);
		return noLayerTextView;
	}

	/*
	 * build the layer control for one layer, it includes building the icon,
	 * check box and title for the layer
	 */
	public static LinearLayout buildSingleLayerControlContainer(
			final MapViewerActivity activity, final Layer layer, final int index) {

		boolean containLegend = false;
		boolean visible = false;
		ArrayList<Legend> legendList = new ArrayList<Legend>();
		templateList = new ArrayList<FeatureTemplate>();
		featurelayerList = new ArrayList<ArcGISFeatureLayer>();

		// container for the one layer's layer control
		LinearLayout singleLayerLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.setMargins(0, 20, 10, 20);
		singleLayerLayout.setLayoutParams(ll);
		singleLayerLayout.setGravity(Gravity.CENTER);
		singleLayerLayout.setOrientation(LinearLayout.VERTICAL);

		// container for the legends
		final LinearLayout legendsControl = new LinearLayout(activity);
		legendsControl.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		legendsControl.setOrientation(LinearLayout.VERTICAL);

		// add legends here
		ArcGISLayerInfo[] info = null;

		if (layer instanceof GroupLayer) {
			Layer[] ls = ((GroupLayer) layer).getLayers();
			for (Layer l : ls) {
				visible = l.isVisible();
				if (l instanceof ArcGISFeatureLayer) {
					if (l.isShowLegend()) {
						makeFeatureLayerLegend(legendList, l);
					}
				}
				if (l instanceof GroupLayer) {
					for (Layer ll2 : ls) {
						if (ll2.isShowLegend()) {
							makeFeatureLayerLegend(legendList, ll2);
						}
					}
				}
			}
		}

		if (layer instanceof ArcGISFeatureLayer) {
			visible = layer.isVisible();
			if (layer.isShowLegend()) {
				makeFeatureLayerLegend(legendList, layer);
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
			LinkedList<String> recordList = new LinkedList<String>();
			for (int j = 0; j < info.length; j++) {
				List<Legend> legends = info[j].getLegend();
				if (legends != null && legends.size() != 0
						&& legends.get(0).getLabel() != "") {
					containLegend = true;
					for (int k = 0; k < legends.size(); k++) {
						String labelText = legends.get(k).getLabel();
						if (isDuplicated(recordList, labelText)) {
							continue;
						}
						LinearLayout legendLayout = legendLayoutMaker(activity,
								legends.get(k));
						legendsControl.addView(legendLayout);
					}
				}
			}
		}

		if (legendList.size() != 0) {
			containLegend = true;
			for (int k = 0; k < legendList.size(); k++) {
				String labelText = legendList.get(k).getLabel();
				LinearLayout legendLayout = null;
				if (labelText == null || labelText.length() == 0) {
					legendLayout = legendLayoutMaker(activity, new Legend(
							legendList.get(k).getImage(), layer.getTitle()));
				} else {
					legendLayout = legendLayoutMaker(activity,
							legendList.get(k));
				}
				legendsControl.addView(legendLayout);
			}
		}

		final LinearLayout switchAndTitle = new LinearLayout(activity);
		final ImageView expand = new ImageView(activity);
		final TextView layerName = new TextView(activity);
		final CheckBox layerSwitch = new CheckBox(activity);

		OnClickListener ocl = new OnClickListener() {

			public void onClick(View v) {
				if (legendsControl == null
						|| legendsControl.getChildCount() == 0) {
					return;
				} else {
					if (legendsControl.getVisibility() == View.GONE) {
						legendsControl.setVisibility(View.VISIBLE);
						expand.setImageBitmap(BitmapFactory.decodeResource(
								activity.getResources(), R.drawable.expanded));
					} else {
						legendsControl.setVisibility(View.GONE);
						expand.setImageBitmap(BitmapFactory.decodeResource(
								activity.getResources(), R.drawable.collapsed));
					}
				}
			}
		};

		// title and switch
		switchAndTitle.setOrientation(LinearLayout.HORIZONTAL);
		switchAndTitle.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// setup the checkbox
		layerSwitch.setChecked(visible);
		layerSwitch.setTag(index);
		layerSwitch.setGravity(Gravity.LEFT);
		layerSwitch.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 4));

		layerSwitch.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				layerSwitch.setChecked(layerSwitch.isChecked());
				activity.bluetoothFragment.messageBuilder(
						BluetoothFragment.LAYER_CHANGE,
						new String[] { Integer.toString(index),
								Boolean.toString(layerSwitch.isChecked()) });
			}
		});

		layerSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setLayerVisible(activity.mapView.getLayer(index),
								isChecked);
					}

					private void setLayerVisible(Layer layer, boolean isVisible) {
						layer.setVisible(isVisible);
						if (layer instanceof GroupLayer) {
							Layer[] ls = ((GroupLayer) layer).getLayers();
							for (Layer l : ls) {
								if (l instanceof GroupLayer) {
									setLayerVisible(l, isVisible);
								} else {
									layer.setVisible(isVisible);
								}
							}
						}
					}
				});
		switchAndTitle.addView(layerSwitch);

		// add title
		layerName.setText(layer.getTitle());
		layerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		layerName.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		layerName.setOnClickListener(ocl);
		switchAndTitle.addView(layerName);

		// add expand button
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 4);
		lp.setMargins(0, 8, 0, 0);
		expand.setLayoutParams(lp);
		if (containLegend) {
			expand.setImageBitmap(BitmapFactory.decodeResource(
					activity.getResources(), R.drawable.expanded));
			expand.setOnClickListener(ocl);
			expand.setTag("expand");
		}
		switchAndTitle.addView(expand);

		singleLayerLayout.addView(switchAndTitle);

		if (containLegend) {
			singleLayerLayout.addView(legendsControl);
		}

		singleLayerLayout.setTag(index);

		return singleLayerLayout;
	}

	public static boolean isDuplicated(LinkedList<String> recordList, String str) {
		if (recordList.indexOf(str) == -1) {
			recordList.addFirst(str);
			return false;
		}
		return true;
	}

	private static LinearLayout legendLayoutMaker(Context context, Legend legend) {
		LinearLayout legendLayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(20, 0, 0, 0);
		legendLayout.setLayoutParams(lp);
		legendLayout.setOrientation(LinearLayout.HORIZONTAL);
		legendLayout.setGravity(Gravity.CENTER_VERTICAL);

		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(legend.getImage());
		imageView.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 7));
		legendLayout.addView(imageView);

		TextView legendLabel = new TextView(context);
		legendLabel.setText(legend.getLabel());
		legendLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		legendLabel.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		legendLayout.addView(legendLabel);
		return legendLayout;
	}

	public static void makeFeatureLayerLegend(ArrayList<Legend> legendList,
			Layer l) {
		if (l instanceof ArcGISFeatureLayer) {
			ArcGISFeatureLayer featurelayer1 = (ArcGISFeatureLayer) l;

			FeatureType[] types = featurelayer1.getTypes();
			for (FeatureType featureType : types) {
				FeatureTemplate[] templates = featureType.getTemplates();
				for (FeatureTemplate featureTemplate : templates) {
					String name = featureTemplate.getName();
					Bitmap bitmap = createSymbolBitmap(featurelayer1,
							featureTemplate);

					legendList.add(new Legend(bitmap, name));
					templateList.add(featureTemplate);
					featurelayerList.add((ArcGISFeatureLayer) l);
				}
			}
			if (legendList.size() == 0) { // no types
				FeatureTemplate[] templates = featurelayer1.getTemplates();
				for (FeatureTemplate featureTemplate : templates) {
					String name = featureTemplate.getName();
					Bitmap bitmap = createSymbolBitmap(featurelayer1,
							featureTemplate);
					legendList.add(new Legend(bitmap, name));
					templateList.add(featureTemplate);
					featurelayerList.add((ArcGISFeatureLayer) l);
				}
			}
		}
	}

	private static Bitmap createSymbolBitmap(ArcGISFeatureLayer featurelayer,
			FeatureTemplate featureTemplate) {

		FeatureTemplate.DRAWING_TOOL drawing_tool = featureTemplate
				.getDrawingTool();
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
		} else if (drawing_tool == DRAWING_TOOL.POINT) {
			geometry = new Point(20, 20);
		}

		Graphic g = featurelayer.createFeatureWithTemplate(featureTemplate,
				null);
		Renderer renderer = featurelayer.getRenderer();
		Symbol symbol = renderer.getSymbol(g);

		Bitmap bitmap = featurelayer.createSymbolImage(symbol, geometry, 40,
				40, Color.TRANSPARENT);

		return bitmap;
	}

	public static LinearLayout setupBasemapSwitch(
			final MapViewerActivity activity, final Layer layer) {

		final String currentBasemapUrl = layer.getUrl();
		String defaultBasemapTitle = layer.getTitle() + " (Default)";

		HashMap<String, String> baseMap = new HashMap<String, String>();
		baseMap.put(activity.getString(R.string.WORLD_STREET_MAP),
				activity.getString(R.string.StreetMapMenu));
		baseMap.put(activity.getString(R.string.WORLD_TOPO_MAP),
				activity.getString(R.string.TopoMenu));
		baseMap.put(activity.getString(R.string.WORLD_NATGEO_MAP),
				activity.getString(R.string.NatGeoMenu));
		baseMap.put(activity.getString(R.string.OCEAN_BASEMAP),
				activity.getString(R.string.OceanMenu));
		baseMap.put(activity.getString(R.string.OSMMenu),
				activity.getString(R.string.OSMMenu));

		if (layer instanceof OpenStreetMapLayer) {
			baseMap.remove(activity.getString(R.string.OSMMenu));
		} else if (baseMap.keySet().contains(layer.getUrl())) {
			defaultBasemapTitle = baseMap.get(currentBasemapUrl) + " (Default)";
			baseMap.remove(currentBasemapUrl);
		}

		// container for basemap layer control
		LinearLayout basemapLayerLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.setMargins(0, 20, 10, 20);
		basemapLayerLayout.setLayoutParams(ll);
		basemapLayerLayout.setGravity(Gravity.CENTER);
		basemapLayerLayout.setOrientation(LinearLayout.VERTICAL);

		final RadioGroup rg = new RadioGroup(activity);
		rg.setId(R.id.basemap_switch);
		rg.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		final RadioButton defaultbasemapButton = new RadioButton(activity);
		defaultbasemapButton.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		defaultbasemapButton.setTextColor(Color.WHITE);
		defaultbasemapButton.setTag(MapControlWidget.DEFAULT_BASEMAP_LAYER);
		defaultbasemapButton.setText(defaultBasemapTitle);
		defaultbasemapButton.setChecked(true);
		defaultbasemapButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (!isChecked) {
							return;
						}
						MapControlWidget.switchBasemap(activity, null, rg,
								defaultbasemapButton, true);
					}
				});
		rg.addView(defaultbasemapButton);

		Set<String> keys = baseMap.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			final String key = iterator.next();
			final RadioButton rb = new RadioButton(activity);
			rb.setLayoutParams(new TableLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rb.setTextColor(Color.WHITE);
			rb.setText(baseMap.get(key));
			rb.setChecked(false);
			rb.setTag(key);

			rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (!isChecked) {
						return;
					}
					MapControlWidget
							.switchBasemap(activity, null, rg, rb, true);
				}
			});

			rg.addView(rb);
		}

		basemapLayerLayout.addView(rg);

		return basemapLayerLayout;
	}

	public static void switchRadioButton(final RadioButton rb,
			final RadioGroup rg) {
		for (int i = 0; i < rg.getChildCount(); i++) {
			RadioButton rbtmp = (RadioButton) rg.getChildAt(i);
			if (rbtmp.hashCode() != rb.hashCode()) {
				rbtmp.setChecked(false);
			}
		}
	}

	public static void buildBookmarkControl(final MapViewerActivity activity,
			LinearLayout bookmarksControl, final List<Bookmark> bookmarks) {
		float scale = activity.getResources().getDisplayMetrics().density;
		if (bookmarks == null || bookmarks.size() == 0) {
			Status.isBookmarkAvailable = false;
			TextView noBookmark = new TextView(activity);
			noBookmark.setText(activity.getString(R.string.no_bookmark));
			noBookmark.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			noBookmark.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			noBookmark.setGravity(Gravity.CENTER);
			bookmarksControl.addView(noBookmark);
			return;
		}
		for (int i = 0; i < bookmarks.size(); i++) {
			Status.isBookmarkAvailable = true;
			final int index = i;
			LinearLayout singleBookmark = new LinearLayout(activity);
			singleBookmark.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			singleBookmark.setOrientation(LinearLayout.HORIZONTAL);
			singleBookmark.setGravity(Gravity.CENTER_VERTICAL);

			final ImageView image = new ImageView(activity);
			image.setLayoutParams(new TableLayout.LayoutParams(
					(int) (scale * 18.0f + 0.5f), (int) (scale * 18.0f + 0.5f),
					5));
			image.setImageBitmap(BitmapFactory.decodeResource(
					activity.getResources(), R.drawable.bookmark));
			singleBookmark.addView(image);

			final TextView bookmarkName = new TextView(activity);
			bookmarkName.setText(bookmarks.get(i).getName());
			bookmarkName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			bookmarkName.setLayoutParams(new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
			bookmarkName.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					activity.mapView
							.setExtent(bookmarks.get(index).getExtent());
					// sent a bluetooth message when the extent changes
					activity.bluetoothFragment.messageBuilder(
							BluetoothFragment.EXTENT_CHANGE,
							Utility.extractMapViewExtent(activity.mapView));
				}
			});
			singleBookmark.addView(bookmarkName);

			bookmarksControl.addView(singleBookmark);
		}
	}

	public static TextView buildGeocodingResult(Context context, int rank,
			String address, String type) {
		final TextView singleResult = new TextView(context);
		int padding = Utility.pixelScaler(context, 10);
		singleResult.setPadding(padding, padding, padding, padding);
		if (!type.isEmpty()) {
			singleResult.setText(Integer.toString(rank + 1) + ". Address: "
					+ address + "\nType: " + type);
		} else {
			singleResult.setText(Integer.toString(rank + 1) + ". Address: "
					+ address);
		}
		singleResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		singleResult.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		return singleResult;
	}

	@SuppressLint("SimpleDateFormat")
	public static LinearLayout buildAbout(Context context, PortalItem pi) {
		int textSize = 18;
		LinearLayout about_control = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		about_control.setLayoutParams(lp);
		about_control.setOrientation(LinearLayout.VERTICAL);
		about_control.setGravity(Gravity.LEFT);

		LinearLayout titleLayout = makeBlocks(context,
				context.getString(R.string.about_title), 0, pi.getTitle(),
				textSize);
		about_control.addView(titleLayout);

		LinearLayout ownerLayout = makeBlocks(context,
				context.getString(R.string.about_owner), 0, pi.getOwner(),
				textSize);
		about_control.addView(ownerLayout);

		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(pi
				.getCreated()));
		LinearLayout createdLayout = makeBlocks(context,
				context.getString(R.string.about_created), 0, date, textSize);
		about_control.addView(createdLayout);

		String date2 = new SimpleDateFormat("yyyy-MM-dd").format(new Date(pi
				.getModified()));
		LinearLayout modifiedLayout = makeBlocks(context,
				context.getString(R.string.about_modified), 0, date2, textSize);
		about_control.addView(modifiedLayout);

		LinearLayout snippetLayout = makeBlocks(context,
				context.getString(R.string.about_snippet), 0, pi.getSnippet(),
				textSize);
		about_control.addView(snippetLayout);

		LinearLayout accesslevelLayout = makeBlocks(context,
				context.getString(R.string.about_access_level), 0, pi
						.getAccess().toString(), textSize);
		about_control.addView(accesslevelLayout);

		LinearLayout numOfViewLayout = makeBlocks(context,
				context.getString(R.string.about_num_view), 0,
				Integer.toString(pi.getNumViews()), textSize);
		about_control.addView(numOfViewLayout);

		LinearLayout numOfRatingLayout = makeBlocks(context,
				context.getString(R.string.about_num_rating), 0,
				Integer.toString(pi.getNumRatings()), textSize);
		about_control.addView(numOfRatingLayout);

		LinearLayout avgRatingLayout = makeBlocks(context,
				context.getString(R.string.about_avg_rating), 0,
				Double.toString(Math.round(pi.getAvgRating() * 100.0) / 100.0)
						+ "/5", textSize);
		about_control.addView(avgRatingLayout);
		LinearLayout creditsLayout = makeBlocks(context,
				context.getString(R.string.about_credits), textSize,
				context.getString(R.string.about_esri_credits), textSize);
		about_control.addView(creditsLayout);

		return about_control;
	}

	public static void buildGeocodingControl(final MapViewerActivity activity,
			final WebMap webMap, final LinearLayout locationControl) {
		locationControl.addView(twoButtons(activity,
				activity.getString(R.string.geocoding_my_loc_string),
				R.id.geocoding_my_loc_id,
				activity.getString(R.string.geocoding_default_extent_string),
				R.id.geocoding_default_extent_id));

		final Button myLocBtn = (Button) activity
				.findViewById(R.id.geocoding_my_loc_id);
		myLocBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Status.isMyLocationShowing) {
					activity.graphicsLayer.removeAll();
					myLocBtn.setText(activity
							.getString(R.string.geocoding_my_loc_string));
				} else {
					activity.loadWebMapFragment.startGPS();
					myLocBtn.setText(activity
							.getString(R.string.geocoding_clear));
				}
				Status.isMyLocationShowing = !Status.isMyLocationShowing;
			}
		});
		((Button) activity.findViewById(R.id.geocoding_default_extent_id))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						SpatialReference input = webMap.getInfo()
								.getSpatialReference();
						SpatialReference output = activity.mapView
								.getSpatialReference();
						if (input == null) {
							input = SpatialReference
									.create(SpatialReference.WKID_WGS84);
						}
						Geometry extent = GeometryEngine.project(
								webMap.getInitExtent(), input, output);
						if (extent != null) {
							MapControlWidget.setMapExtent(activity, extent,
									true);
						}
					}
				});

		LinearLayout geocodingTitle = titleWithImage(activity,
				activity.getString(R.string.geocoding_geocoder),
				R.drawable.geocoding, true);
		locationControl.addView(geocodingTitle);

		final EditText geocodingAddress = new EditText(activity);
		geocodingAddress.setId(R.id.geocoding_address_id);
		geocodingAddress.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		geocodingAddress.setHint(R.string.geocoding_tip);
		locationControl.addView(geocodingAddress);
		locationControl.addView(twoButtons(activity,
				activity.getString(R.string.geocoding_search),
				R.id.geocoding_search_id,
				activity.getString(R.string.geocoding_clear),
				R.id.geocoding_clear_id));
		final Button geocodingSearchButton = (Button) activity
				.findViewById(R.id.geocoding_search_id);
		Button geocodingClearButton = (Button) activity
				.findViewById(R.id.geocoding_clear_id);

		geocodingClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				@SuppressWarnings("unchecked")
				ArrayList<View> tag = (ArrayList<View>) locationControl
						.getTag();
				if (tag == null) {
					return;
				}
				for (View view : tag) {
					locationControl.removeView(view);
				}
				geocodingAddress.setText("");
				activity.graphicsLayer.removeAll();
			}
		});

		geocodingSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(geocodingAddress.getWindowToken(),
						0);
				String address = geocodingAddress.getText().toString();
				if (address == null) {
					return;
				}
				try {
					LocatorFindParameters findParams = new LocatorFindParameters(
							address);
					findParams.setMaxLocations(5);
					findParams.setOutSR(activity.mapView.getSpatialReference());
					List<String> outFilds = new ArrayList<String>();
					outFilds.add("*");
					findParams.setOutFields(outFilds);
					new Geocoder(
							activity,
							locationControl,
							ProgressDialog.show(
									activity.mapView.getContext(),
									activity.getString(R.string.geocoding_geocoder),
									activity.getString(R.string.geocoding_searching)))
							.execute(findParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		geocodingAddress.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().contains("\n")) {
					geocodingSearchButton.performClick();
					return;
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
				if (s.toString().contains("\n")) {
					s.delete(s.length() - 1, s.length());
				}
			}
		});
	}

	/* build the UI and control for callout */
	protected static void makeCallout(final MapViewerActivity activity,
			final int index, ArrayList<Popup> results) {

		if (index == -1) {
			try {
				callout.hide();
				popupList = null;
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		if (results != null) {
			popupList = results;
		}

		if (index < 0 || index > popupList.size() - 1) {
			return;
		}

		int height = getDimension(activity, R.dimen.callout_height);
		int width = getDimension(activity, R.dimen.callout_width);
		int totalPopupCount = popupList.size();
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.callout_layout, null);
		final ImageButton back = (ImageButton) view
				.findViewById(R.id.callout_back);
		final ImageButton next = (ImageButton) view
				.findViewById(R.id.callout_next);
		ImageView close = (ImageView) view.findViewById(R.id.callout_close);
		LinearLayout layout = (LinearLayout) view
				.findViewById(R.id.callout_container);
		((TextView) view.findViewById(R.id.callout_title)).setText("POPUP "
				+ Integer.toString(index + 1) + "/"
				+ Integer.toString(totalPopupCount));
		layout.addView(
				Utility.PopupToView(popupList.get(index), activity.mapView),
				width, height);
		if (callout != null) {
			callout.hide();
		} else {
			callout = activity.mapView.getCallout();
			callout.setOffset(0, 0);
			callout.setMaxHeight(height);
			callout.setMaxWidth(width);
		}
		callout.setContent(view);

		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				callout.hide();
				popupList = null;
				activity.bluetoothFragment.messageBuilder(
						BluetoothFragment.CALLOUT_CHANGE,
						new String[] { Integer.toString(-1) });
			}
		});

		if (index != totalPopupCount - 1 && totalPopupCount != 1) {
			next.setVisibility(View.VISIBLE);
		} else {
			next.setVisibility(View.INVISIBLE);
		}
		if (index != 0) {
			back.setVisibility(View.VISIBLE);
		} else {
			back.setVisibility(View.INVISIBLE);
		}
		OnClickListener calloutControlButton = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == back.getId()) {
					MapControlWidget.switchCallout(activity, index - 1, true);
				} else if (v.getId() == next.getId()) {
					MapControlWidget.switchCallout(activity, index + 1, true);
				}
			}
		};
		back.setOnClickListener(calloutControlButton);
		next.setOnClickListener(calloutControlButton);
		if (popupList.get(index).getGraphic().getGeometry().getType() == Type.POINT) {
			callout.show((Point) popupList.get(index).getGraphic()
					.getGeometry());
		} else {
			callout.show(pointQueried);
		}
	}
}

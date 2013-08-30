package com.esri.apl.mymaps;

import com.esri.apl.mymaps.Status.ClickType;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

public class GridviewFragment extends Fragment {
	private OnWebMapItemClickListener listener;

	public interface OnWebMapItemClickListener {
		public void onGridItemClicked(String ID, String itemType, ClickType clickType);
	}

	public static GridviewFragment newInstance(String parent) {
		GridviewFragment gridviewFragment = new GridviewFragment();
		Bundle args = new Bundle();
		args.putString("parent", parent);
		gridviewFragment.setArguments(args);
		Utility.getMapRecordInCurrentFolder();
		return gridviewFragment;
	}

	public String getParent() {
		return getArguments().getString("parent", null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gridview_layout, container, false);
		GridView gridview = (GridView) view.findViewById(R.id.mapgridview);
		gridview.setAdapter(new ImageAdapter(view.getContext()));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
				boolean isWebMap = MapRecord.typesInCurrentFolder.get(position).equals(Status.WEBMAP);
				if (isWebMap) {
					listener.onGridItemClicked(MapRecord.IDsInCurrentFolder.get(position), Status.WEBMAP, ClickType.Click);
				} else {
					listener.onGridItemClicked(MapRecord.IDsInCurrentFolder.get(position), Status.FOLDER, ClickType.Click);
				}
			}
		});
		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
				Log.i("mymaps", "a long click happened");
				// ClipData.Item item = new
				// ClipData.Item(Integer.toString((Integer) view.getTag()));
				// ClipData dragData = ClipData.newPlainText("label", "text");
				// DragShadowBuilder shadowBuilder = new
				// View.DragShadowBuilder(view);
				// view.startDrag(dragData, shadowBuilder, view, 0);

				boolean isWebMap = MapRecord.typesInCurrentFolder.get(position).equals(Status.WEBMAP);
				if (isWebMap) {
					listener.onGridItemClicked(MapRecord.IDsInCurrentFolder.get(position), Status.WEBMAP, ClickType.LongClick);
				} else {
					listener.onGridItemClicked(MapRecord.IDsInCurrentFolder.get(position), Status.FOLDER, ClickType.LongClick);
				}
				return false;
			}
		});
		gridview.setOnDragListener(new OnDragListener() {
			public boolean onDrag(View v, DragEvent event) {
				final int action = event.getAction();
				switch (action) {
				case DragEvent.ACTION_DRAG_STARTED:
					Log.i("mymaps", "a drag started");
					if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						return true;
					} else {
						return false;
					}
				case DragEvent.ACTION_DRAG_ENTERED:
					Log.i("mymaps", "a drag entered");
					break;
				case DragEvent.ACTION_DROP:
					Log.i("mymaps", "a drag droped");
					View view = (View) event.getLocalState();
					break;
				default:
					break;
				}

				return false;
			}
		});
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnWebMapItemClickListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}

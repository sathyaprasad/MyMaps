package com.esri.apl.mymaps;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

public class GridviewFragment extends Fragment {
	private OnWebMapItemClickListener listener;

	public interface OnWebMapItemClickListener {
		public void onGridItemClicked(String ID, boolean isWebMap,
				boolean isLongClick);
	}

	public static GridviewFragment newInstance(String parent) {
		GridviewFragment gridviewFragment = new GridviewFragment();
		Bundle args = new Bundle();
		args.putString("parent", parent);
		gridviewFragment.setArguments(args);
		DashboardItem.getItemsInCurrentFolder();
		return gridviewFragment;
	}

	public String getParent() {
		return getArguments().getString("parent", null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.gridview_layout, container, false);
		GridView gridview = (GridView) view.findViewById(R.id.gridview);
		gridview.setAdapter(new GridCellAdapter(view.getContext()));
		// setup the on click listener for the items in gridview
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view,
					final int position, long id) {
				DashboardItem currentItem = DashboardItem.currentItems
						.get(position);
				listener.onGridItemClicked(currentItem.ID,
						currentItem.type.equals(DashboardItem.WEBMAP), false);
			}
		});
		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView,
					View view, int position, long id) {
				DashboardItem currentItem = DashboardItem.currentItems
						.get(position);
				listener.onGridItemClicked(currentItem.ID,
						currentItem.type.equals(DashboardItem.WEBMAP), true);
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
}

package com.esri.apl.mymaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridCellAdapter extends BaseAdapter {
	private Context context;

	public GridCellAdapter(Context context) {
		this.context = context;
	}

	public int getCount() {
		if (DashboardItem.currentItems != null) {
			return DashboardItem.currentItems.size();
		} else {
			return 0;
		}
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView = convertView;
		if (gridView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.gridview_dashboard_item, null);
		}
		DashboardItem currentItem = DashboardItem.currentItems.get(position);
		boolean isWebMap = currentItem.type.equals(DashboardItem.WEBMAP);

		TextView rating = (TextView) gridView.findViewById(R.id.gridview_rate);
		TextView owner = (TextView) gridView.findViewById(R.id.gridview_owner);
		TextView views = (TextView) gridView.findViewById(R.id.gridview_views);
		TextView date = (TextView) gridView.findViewById(R.id.gridview_date);
		TextView title = (TextView) gridView.findViewById(R.id.gridview_title);
		ImageView thumbnail = (ImageView) gridView
				.findViewById(R.id.gridview_thumbnail);
		title.setMaxLines(2);
		// setup the UI for the item depending on whether it is a webmap or
		// folder
		if (isWebMap) {
			title.setText(currentItem.title);
			owner.setText("By " + currentItem.owner);
			owner.setVisibility(View.VISIBLE);
			date.setText("Modified "
					+ Utility.timeFormatter("MMM dd, yyyy", currentItem.date));
			rating.setVisibility(View.VISIBLE);
			rating.setText("Rating "
					+ Double.toString(Math.round(currentItem.rating * 100.0) / 100.0)
					+ "/5");
			views.setText("Access " + currentItem.access);
			views.setVisibility(View.VISIBLE);
			Bitmap bmp = BitmapFactory.decodeByteArray(currentItem.image, 0,
					currentItem.image.length);
			thumbnail.setImageBitmap(bmp);
		} else {
			title.setText(currentItem.ID);
			owner.setVisibility(View.INVISIBLE);
			date.setText("WebMap "
					+ DashboardItem.getChildWebMapCount(currentItem.ID));
			if (Status.MultipleLevelFoldersAllowed) {
				rating.setVisibility(View.VISIBLE);
				rating.setText("Sub folder "
						+ DashboardItem.getChildFolderCount(currentItem.ID));
			} else {
				rating.setVisibility(View.GONE);
			}
			views.setVisibility(View.INVISIBLE);
			thumbnail.setImageResource(R.drawable.folder);
			thumbnail.setPadding(0, 0, 0, 0);
		}
		return gridView;
	}

}

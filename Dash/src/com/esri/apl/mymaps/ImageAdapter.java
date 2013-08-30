package com.esri.apl.mymaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private ImageView imageView;

	// private int adjust = 0;
	// private int padding = 0;
	// private int paddingEnd = 0;
	// private int itemsPerColumn = 0;

	public ImageAdapter(Context mContext) {
		this.mContext = mContext;
	}

	public int getCount() {
		return MapRecord.IDsInCurrentFolder.size();
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
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.cell, null);
		}
		// if ((position - 1 >= 0) && position <
		// MapRecord.IDsInCurrentFolder.size() &&
		// MapRecord.typesInCurrentFolder.get(position).equals(Status.FOLDER)
		// && MapRecord.typesInCurrentFolder.get(position - 1) != null &&
		// MapRecord.typesInCurrentFolder.get(position -
		// 1).equals(Status.WEBMAP)) {
		// if ((position) % itemsPerColumn != 0) {
		// adjust = itemsPerColumn - (position) % itemsPerColumn;
		// paddingEnd = position + adjust;
		// padding = adjust;
		// }
		// Log.i("mymaps", "itemsPerColumn: " +
		// Integer.toString(itemsPerColumn));
		// Log.i("mymaps", "position + 1: " + Integer.toString(position + 1));
		// Log.i("mymaps", "Status.adjust: " + Integer.toString(itemsPerColumn -
		// (position) % itemsPerColumn));
		// Log.i("mymaps", "paddingItem: " + Integer.toString(padding));
		// }
		// Log.i("mymaps", "position + 1 (real): " + Integer.toString(position +
		// 1));
		// if (padding > 0) {
		// padding--;
		// Log.i("mymaps", "padding: " + Integer.toString(padding));
		// gridView.setClickable(false);
		// return gridView;
		// } else if ((position <= paddingEnd) && (position > paddingEnd -
		// adjust)) {
		// Log.i("mymaps", "position between padding: " +
		// Integer.toString(position));
		// gridView.setClickable(false);
		// return gridView;
		// } else {
		// gridView.setClickable(true);
		// int index = 0;
		// if (position < paddingEnd) {
		// index = position;
		// } else {
		// index = position - adjust;
		// }
		boolean isWebMap = MapRecord.typesInCurrentFolder.get(position).equals(Status.WEBMAP);
		String date_modified = Utility.timeFormatter("MMM dd, yyyy", MapRecord.datesInCurrentFolder.get(position));
		TextView tv_rating = (TextView) gridView.findViewById(R.id.mapgrid_rate);
		TextView tv_owner = (TextView) gridView.findViewById(R.id.mapgrid_owner);
		TextView tv_views = (TextView) gridView.findViewById(R.id.mapgrid_views);
		TextView tv_date = (TextView) gridView.findViewById(R.id.mapgrid_date);
		TextView tv_title = (TextView) gridView.findViewById(R.id.mapgrid_title);
		imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
		if (isWebMap) {
			tv_title.setText(MapRecord.titlesInCurrentFolder.get(position));
			tv_title.setMaxLines(2);
			tv_owner.setText("By " + MapRecord.ownersInCurrentFolder.get(position));
			tv_date.setText("Modified " + date_modified);
			tv_rating.setText("Rating " + Double.toString(Math.round(MapRecord.ratingsInCurrentFolder.get(position) * 100.0) / 100.0) + "/5");
			tv_views.setText("Access " + MapRecord.accessesInCurrentFolder.get(position));
			Bitmap bmp = BitmapFactory.decodeByteArray(MapRecord.imagesInCurrentFolder.get(position), 0, MapRecord.imagesInCurrentFolder.get(position).length);
			imageView.setImageBitmap(bmp);
		} else {
			tv_title.setText(MapRecord.IDsInCurrentFolder.get(position));
			tv_title.setMaxLines(2);
			tv_owner.setVisibility(View.INVISIBLE);
			tv_date.setText("WebMap " + Utility.getChildWebMapCount(MapRecord.IDsInCurrentFolder.get(position)));
			if (Status.MultipleLevelFoldersAllowed) {
				tv_rating.setText("Sub folder " + Utility.getChildFolderCount(MapRecord.IDsInCurrentFolder.get(position)));
			} else {
				tv_rating.setVisibility(View.GONE);
			}
			tv_views.setText("Modified " + date_modified);
			imageView.setImageResource(R.drawable.folder2);
			LayoutParams params = (LayoutParams) imageView.getLayoutParams();
			// params.width = Utility.pixelScaler(mContext, 110);
			// imageView.setLayoutParams(params);
			imageView.setPadding(0, 0, 0, 0);
		}

		gridView.setTag(position);
		return gridView;
	}
}

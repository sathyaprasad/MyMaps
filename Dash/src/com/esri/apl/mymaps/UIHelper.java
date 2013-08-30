package com.esri.apl.mymaps;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.esri.core.portal.PortalItem;

public class UIHelper {

	private Context context;
	private float scale;

	public UIHelper(final Context context) {
		this.context = context;
		scale = context.getResources().getDisplayMetrics().density;
	}

	public LinearLayout titleWithImage(String text, int draw, boolean topMargin) {
		LinearLayout titleWithImagelayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (topMargin) {
			lp.setMargins(0, (int) (scale * 10.0f + 0.5f), 0, (int) (scale * 5.0f + 0.5f));
		} else {
			lp.setMargins(0, 0, 0, (int) (scale * 5.0f + 0.5f));
		}
		titleWithImagelayout.setLayoutParams(lp);
		titleWithImagelayout.setOrientation(LinearLayout.HORIZONTAL);
		titleWithImagelayout.setGravity(Gravity.CENTER);
		// titleWithImagelayout.setBackgroundColor(Color.rgb(43, 43, 43));
		titleWithImagelayout.setBackgroundColor(Color.TRANSPARENT);
		int paddingSize = (int) (scale * 5.0f + 0.5f);
		titleWithImagelayout.setPadding(0, paddingSize, 0, paddingSize);

		ImageView imageView = new ImageView(context);
		imageView.setImageDrawable(context.getResources().getDrawable(draw));
		imageView.setLayoutParams(new LinearLayout.LayoutParams((int) (scale * 36.0f + 0.5f), (int) (scale * 36.0f + 0.5f)));
		imageView.setPadding(0, 0, (int) (scale * 10.0f + 0.5f), 0);

		titleWithImagelayout.addView(imageView);

		TextView legendLabel = new TextView(context);
		legendLabel.setText(text);
		legendLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		legendLabel.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		titleWithImagelayout.addView(legendLabel);

		return titleWithImagelayout;
	}

	public LinearLayout twoButtons(String text1, int ID1, String text2, int ID2) {
		LinearLayout container = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		// lp.setMargins(80, 0, 80, 50);
		container.setLayoutParams(lp);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.CENTER);

		// View view1 = new View(context);
		// view1.setLayoutParams(new
		// TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT, 1));
		// container.addView(view1);
		GradientDrawable drawable = new GradientDrawable();
		drawable.setShape(GradientDrawable.RECTANGLE);
		drawable.setStroke(1, Color.WHITE);
		drawable.setColor(Color.BLACK);

		Button button1 = new Button(context);
		button1.setText(text1);
		button1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		button1.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		button1.setId(ID1);
		// button1.setBackgroundDrawable(drawable);
		container.addView(button1);

		// View view2 = new View(context);
		// view2.setLayoutParams(new
		// TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT, 1));
		// container.addView(view2);

		Button button2 = new Button(context);
		button2.setText(text2);
		button2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		button2.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		button2.setId(ID2);
		// button2.setBackgroundDrawable(drawable);
		container.addView(button2);

		// View view3 = new View(context);
		// view3.setLayoutParams(new
		// TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT, 1));
		// container.addView(view3);

		return container;
	}

	public View makeLine() {
		View line = new View(context);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		line.setLayoutParams(lp);
		line.setBackgroundColor(Color.LTGRAY);
		return line;
	}

	public View makeEmptyVerticalView(int height) {
		View space = new View(context);
		space.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));
		space.setBackgroundColor(Color.BLACK);
		space.setAlpha(0.5f);
		return space;
	}

	public LinearLayout makeBlocks(String title, int titleSize, String text, int textSize) {
		LinearLayout container = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, 0, 30);
		container.setLayoutParams(lp);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

		TextView textBlock = new TextView(context);
		textBlock.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

		Spannable spannable = new SpannableString(title + "\n" + text);
		spannable.setSpan(new RelativeSizeSpan(1.0f), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		textBlock.setText(spannable);
		textBlock.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		container.addView(textBlock);

		return container;
	}

	@SuppressLint("SimpleDateFormat")
	public LinearLayout buildAbout(PortalItem pi) {
		int textSize = 18;
		LinearLayout about_control = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		about_control.setLayoutParams(lp);
		about_control.setOrientation(LinearLayout.VERTICAL);
		about_control.setGravity(Gravity.LEFT);

		LinearLayout titleLayout = makeBlocks(context.getString(R.string.title), 0, pi.getTitle(), textSize);
		about_control.addView(titleLayout);

		LinearLayout ownerLayout = makeBlocks(context.getString(R.string.owner), 0, pi.getOwner(), textSize);
		about_control.addView(ownerLayout);

		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(pi.getCreated()));
		LinearLayout createdLayout = makeBlocks(context.getString(R.string.created), 0, date, textSize);
		about_control.addView(createdLayout);

		String date2 = new SimpleDateFormat("yyyy-MM-dd").format(new Date(pi.getModified()));
		LinearLayout modifiedLayout = makeBlocks(context.getString(R.string.modified), 0, date2, textSize);
		about_control.addView(modifiedLayout);

		LinearLayout snippetLayout = makeBlocks(context.getString(R.string.snippet), 0, pi.getSnippet(), textSize);
		about_control.addView(snippetLayout);

		LinearLayout accesslevelLayout = makeBlocks(context.getString(R.string.access_level), 0, pi.getAccess().toString(), textSize);
		about_control.addView(accesslevelLayout);

		LinearLayout numOfViewLayout = makeBlocks(context.getString(R.string.num_view), 0, Integer.toString(pi.getNumViews()), textSize);
		about_control.addView(numOfViewLayout);

		LinearLayout numOfRatingLayout = makeBlocks(context.getString(R.string.num_rating), 0, Integer.toString(pi.getNumRatings()), textSize);
		about_control.addView(numOfRatingLayout);

		LinearLayout avgRatingLayout = makeBlocks(context.getString(R.string.avg_rating), 0, Double.toString(Math.round(pi.getAvgRating() * 100.0) / 100.0)
				+ "/5", textSize);
		about_control.addView(avgRatingLayout);

		// RatingBar ratingBar = new RatingBar(context);
		// ratingBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		// ratingBar.setMax(5);
		// ratingBar.setNumStars(5);
		// ratingBar.setIsIndicator(true);
		// ratingBar.setStepSize(0.1f);
		// ratingBar.setRating((float) (Math.round(pi.getAvgRating() * 100.0) /
		// 100.0));
		// about_control.addView(ratingBar);

		LinearLayout creditsLayout = makeBlocks(context.getString(R.string.credits), textSize, context.getString(R.string.esri_credits), textSize);
		about_control.addView(creditsLayout);

		return about_control;
	}
	
	
}

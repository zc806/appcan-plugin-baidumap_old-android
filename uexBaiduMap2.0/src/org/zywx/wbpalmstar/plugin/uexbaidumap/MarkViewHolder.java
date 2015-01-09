package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

public class MarkViewHolder {

	private String id;

	public MarkViewHolder(TextView tvTitle, RatingBar ratingBar, ViewGroup group) {
		this.tvTitle = tvTitle;
		this.ratingBar = ratingBar;
		this.group = group;
	}

	public TextView tvTitle;
	public RatingBar ratingBar;
	public ViewGroup group;

	public String getId() {
		return id;
	};

	public void updateData(String markId, String title, String rate,
			Drawable backgroundNor) {
		int rateint = 0;
		try {
			rateint = Integer.valueOf(rate);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		if (backgroundNor != null && group != null) {
			group.setBackgroundDrawable(backgroundNor);
		}

		this.id = markId;
		if (this.tvTitle != null) {
			
			if (title == null || title.length() == 0) {
				this.tvTitle.setVisibility(View.GONE);
			} else {
				this.tvTitle.setVisibility(View.VISIBLE);
				this.tvTitle.setText(title);
			}
		}
		if (this.ratingBar != null) {
			this.ratingBar.setVisibility(View.VISIBLE);
			this.ratingBar.setRating((float) rateint); 
		}

	}

}

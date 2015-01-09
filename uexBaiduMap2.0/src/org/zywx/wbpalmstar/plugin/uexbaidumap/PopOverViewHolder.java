package org.zywx.wbpalmstar.plugin.uexbaidumap;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask$ImageLoadTaskCallback;
import org.zywx.wbpalmstar.base.cache.ImageLoaderManager;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PopOverViewHolder {

	private ImageLoaderManager loaderManager;
	private Context context;
	private String id;

	public PopOverViewHolder(Context ctx, TextView tvTitle, TextView tvContent,
			ImageView ivIcon) {
		this.context = ctx;
		this.tvTitle = tvTitle;
		this.tvContent = tvContent;
		this.ivIcon = ivIcon;
		loaderManager = ImageLoaderManager.initImageLoaderManager(context);
	}

	public PopOverViewHolder(Context ctx, TextView tvTitle, TextView tvContent,
			ImageView ivIcon, ViewGroup group) {
		this.context = ctx;
		this.group = group;
		this.tvTitle = tvTitle;
		this.tvContent = tvContent;
		this.ivIcon = ivIcon;
		loaderManager = ImageLoaderManager.initImageLoaderManager(context);
	}

	public TextView tvTitle;
	public TextView tvContent;
	public ImageView ivIcon;
	public ViewGroup group;

	public String getId() {
		return id;
	};

	public void updateData(String markId, String title, String content,
			String imgUrl, Drawable backgroundNor, Drawable backgroundClicked,
			String titleColor, String contentColor) {
		if (backgroundNor != null && group != null) {
			StateListDrawable drawable = new StateListDrawable();
			try {

				drawable.addState(new int[] { android.R.attr.state_focused,
						android.R.attr.state_enabled }, backgroundClicked);
				drawable.addState(new int[] { android.R.attr.state_pressed,
						android.R.attr.state_enabled }, backgroundClicked);
				drawable.addState(new int[] { android.R.attr.state_checked,
						android.R.attr.state_enabled }, backgroundClicked);
				drawable.addState(new int[] { android.R.attr.state_selected,
						android.R.attr.state_enabled }, backgroundClicked);
				drawable.addState(new int[] {}, backgroundNor);

			} catch (Exception e) {
				e.printStackTrace();
			}
			group.setBackgroundDrawable(drawable);
		}

		this.id = markId;
		if (this.tvTitle != null) {
			if (title == null || title.length() == 0) {
				this.tvTitle.setVisibility(View.GONE);
			} else {
				if (titleColor != null) {
					this.tvTitle.setTextColor(BUtility.parseColor(titleColor));
				}
				this.tvTitle.setVisibility(View.VISIBLE);
				this.tvTitle.setText(title);
			}
		}
		if (this.tvContent != null) {
			if (content == null || content.length() == 0) {
				this.tvContent.setVisibility(View.GONE);
			} else {
				if (contentColor != null) {
					this.tvContent.setTextColor(BUtility
							.parseColor(contentColor));
				}
				this.tvContent.setVisibility(View.VISIBLE);
				this.tvContent.setText(content);
			}
		}
		if (this.ivIcon != null) {
			if (imgUrl == null) {
				ivIcon.setVisibility(View.GONE);
				return;
			} else {
				ivIcon.setVisibility(View.VISIBLE);
			}
			ivIcon.setTag(imgUrl);
			Bitmap cachedBitmap = loaderManager.getCacheBitmap(imgUrl);
			ivIcon.setImageBitmap(cachedBitmap);
			if (cachedBitmap == null) {
				loaderManager.asyncLoad(new ImageLoadTask(imgUrl) {

					@Override
					protected Bitmap doInBackground() {
						return MapUtillity.getImage(context, filePath);
					}
				}.addCallback(new ImageLoadTask$ImageLoadTaskCallback() {

					@Override
					public void onImageLoaded(ImageLoadTask task, Bitmap bitmap) {
						Object tagedObj = ivIcon.getTag();
						if (tagedObj == null) {
							return;
						}
						String tagedUrl = (String) tagedObj;
						if (task.filePath.equals(tagedUrl)) {
							ivIcon.setImageBitmap(bitmap);
						}
					}

				}));
			}
		}
	}

	public void updateData(String markId, String title, String content,
			String imgUrl) {
		updateData(markId, title, content, imgUrl, null, null, null, null);
	}
}

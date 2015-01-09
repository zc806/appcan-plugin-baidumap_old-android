package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.graphics.drawable.Drawable;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.OverlayItem;

public class MarkItem extends OverlayItem {

	public String mId;
	public String mDrawableUrl;
	public int mImgWidth;
	public int mImgHeight;
	public GeoPoint gp;

	public String title;
	public String star;

	public int fontColor;

	public MarkItem(GeoPoint gPoint, String title, String description) {
		super(gPoint, title, description);
	}

	@Override
	public void setMarker(Drawable mark) {
		super.setMarker(mark);
	}

}

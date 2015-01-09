package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import org.zywx.wbpalmstar.base.BDebug;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;

public class MarkOverlays extends ItemizedOverlay<OverlayItem> {

	public static final String TAG = "MarkOverlays";

	private ArrayList<OverlayItem> mOverlayItemList;
	public Context mContext;
	private Paint mPaintText;
	private OverlayTapCallback mOverlayTapCallback;
	private MarkOverlayTapCallback markOverlayTapCallback;

	public MarkOverlays(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		init();
	}

	public MarkOverlays(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		init();
	}

	private void init() {
		mOverlayItemList = new ArrayList<OverlayItem>();
		mPaintText = new Paint();
		mPaintText.setColor(Color.RED);
		mPaintText.setTextSize(13);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlayItemList.get(i);
	}

	@Override
	public int size() {
		return mOverlayItemList.size();
	}

	public void clear() {
		mOverlayItemList.clear();
		populate();
	}

	public void addOverlay(OverlayItem overlayItem) {
		mOverlayItemList.add(overlayItem);
		populate();
	}

	public void setMarkOverlayTapCallback(MarkOverlayTapCallback markOverlayTapCallback) {
		this.markOverlayTapCallback = markOverlayTapCallback;
	}

	public MarkItem getMarkItemById(String overlayId) {
		if (overlayId == null || overlayId.length() == 0) {
			return null;
		}
		MarkItem findedItem = null;
		for (OverlayItem item : mOverlayItemList) {
			if (item instanceof MarkItem) {
				MarkItem markItem = (MarkItem) item;
				if (overlayId.equals(String.valueOf(markItem.mId))) {
					findedItem = markItem;
					break;
				}
			}
		}
		return findedItem;
	}

	public void addOverlay(ArrayList<OverlayItem> overlayItems) {
		mOverlayItemList.addAll(overlayItems);
		populate();
	}

	public void myPopulate() {
		populate();
	}

	public boolean updateItem(MarkItem markItem) {
		String markId = markItem.mId;
		if (markId == null || markId.length() == 0) {
			return false;
		}
		boolean updated = false;
		ArrayList<OverlayItem> arrayList = mOverlayItemList;
		for (int i = 0, size = arrayList.size(); i < size; i++) {
			OverlayItem currentItem = arrayList.get(i);
			if (currentItem instanceof MarkItem) {
				MarkItem currentMark = (MarkItem) currentItem;
				if (currentMark.mId.equals(markId)) {
					arrayList.set(i, markItem);
					populate();
					updated = true;
				}
			}
		}
		return updated;
	}

	public void removeOverlay(String OverlayId) {
		int size = mOverlayItemList.size();
		for (int i = 0; i < size; ++i) {
			MarkItem mItem = (MarkItem) mOverlayItemList.get(i);
			if (mItem.mId.equals(OverlayId)) {
				mOverlayItemList.remove(i);
				populate();
				break;
			}
		}
	}

	public void removeAllOverlay() {
		mOverlayItemList.clear();
		populate();
	}

	public void setOverlayTapCallback(OverlayTapCallback callback) {
		mOverlayTapCallback = callback;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		int size = mOverlayItemList.size();
		for (int i = size - 1; i >= 0; i--) {
			OverlayItem overLayItem = getItem(i);
			Point point = projection.toPixels(overLayItem.getPoint(), null);
			String title = overLayItem.getTitle();
			if (null != title) {
				canvas.drawText(overLayItem.getTitle(), point.x + 10, point.y - 15, mPaintText);
			}
			boundCenterBottom(overLayItem.getMarker(android.R.attr.state_empty));
		}
		super.draw(canvas, mapView, shadow);
	}

	@Override
	protected boolean onTap(int i) {
		BDebug.i(TAG, "onTap() @" + i);
		OverlayItem item = mOverlayItemList.get(i);
		setFocus(item);
		if (null != mOverlayTapCallback) {
			mOverlayTapCallback.onOverlayTaped(((MarkItem) item).mId, item.getPoint());
		}
		return true;
	}

	@Override
	public boolean onTap(GeoPoint gp, MapView mapView) {
		BDebug.i(TAG, "onTap() @ longitue:" + gp.getLongitudeE6() + "  latitude:" + gp.getLatitudeE6());
		if (markOverlayTapCallback != null) {
			markOverlayTapCallback.onMarkOverlayTap(gp, mapView);
		}
		return super.onTap(gp, mapView);
	}

	@Override
	protected boolean hitTest(OverlayItem item, Drawable drawable, int hitX, int hitY) {
		if(drawable==null){
			return false;
		}else{
			return super.hitTest(item, drawable, hitX, hitY);
		}
	}

	public interface MarkOverlayTapCallback {
		void onMarkOverlayTap(GeoPoint gp, MapView mapView);
	}

}

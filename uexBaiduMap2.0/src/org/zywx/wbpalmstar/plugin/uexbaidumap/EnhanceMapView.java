package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.lang.reflect.Method;
import android.content.Context;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;

public class EnhanceMapView extends MapView {

	public static final String TAG = "EnhanceMapView";
	public static final int STATE_START = 0;
	public static final int STATE_MOVE = 1;
	public static final int STATE_STOP = 2;

	private OnMapDragListener mDragListener;
	private int startX;// 触摸初始位置X坐标
	private int startY;// 触摸初始位置Y坐标
	private Projection mProjection;
	public int currentMode = MODE_IDLE;
	public static final int MODE_IDLE = 0;
	public static final int MODE_DRAG = 1;
	public static final int MODE_ZOOM = 2;

	private int touchSlop = ViewConfiguration.getTouchSlop();

	public EnhanceMapView(Context context) {
		super(context);
		setLayerTypeForHeighVersion();
		mProjection = getProjection();
	}

	public void setOnMapDragListener(OnMapDragListener listener) {
		mDragListener = listener;
	}

	protected void setLayerTypeForHeighVersion() {
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[] { int.class, Paint.class };
			Method setOpaqueMode = View.class.getDeclaredMethod("setLayerType", paramTypes);
			setOpaqueMode.invoke(this, 1, null);
		} catch (Exception e) {
			;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int currentX = (int) event.getX();
		int currentY = (int) event.getY();
		GeoPoint geoPoint = mProjection.fromPixels(currentX, currentY);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startX = currentX;
			startY = currentY;
			if (mDragListener != null) {
				mDragListener.onMapDrag(this, STATE_START, geoPoint);
			}
			currentMode = MODE_IDLE;
			break;
		case MotionEvent.ACTION_MOVE:

			switch (currentMode) {
			case MODE_IDLE:
				if (Math.abs(currentX - startX) > touchSlop || Math.abs(currentY - startY) > touchSlop) {
					currentMode = MODE_DRAG;
				}
				break;
			case MODE_DRAG:
				if (mDragListener != null) {
					mDragListener.onMapDrag(this, STATE_MOVE, geoPoint);
				}
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mDragListener != null) {
				mDragListener.onMapDrag(this, STATE_STOP, geoPoint);
			}
			break;
		}
		return super.onTouchEvent(event);
	}

}

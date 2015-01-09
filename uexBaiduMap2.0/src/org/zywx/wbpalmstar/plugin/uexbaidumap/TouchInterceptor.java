package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class TouchInterceptor implements OnTouchListener {

	private int touchSlop;
	public int currentMode = MODE_IDLE;
	public static final int MODE_IDLE = 0;
	public static final int MODE_DRAG = 1;
	public static final int MODE_ZOOM = 2;

	public TouchInterceptor(boolean scrollEnable, boolean zoomEnable) {
		touchSlop = ViewConfiguration.getTouchSlop();
		this.zoomEnable = zoomEnable;
		this.scrollEnable = scrollEnable;
	}

	private boolean zoomEnable = true;
	private boolean scrollEnable = true;

	public void setZoomEnable(boolean zoomEnable) {
		this.zoomEnable = zoomEnable;
	}

	public void setScrollEnable(boolean scrollEnable) {
		this.scrollEnable = scrollEnable;
	}

	private float startX;
	private float startY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (scrollEnable && zoomEnable) {
			return false;
		}
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		boolean isHandle = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startX = event.getX();
			startY = event.getY();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			currentMode = MODE_ZOOM;
			break;
		case MotionEvent.ACTION_MOVE:
			switch (currentMode) {
			case MODE_IDLE:
				if (Math.abs(event.getX() - startX) > touchSlop || Math.abs(event.getY() - startY) > touchSlop) {
					currentMode = MODE_DRAG;
				}
				break;
			case MODE_DRAG:
				isHandle = !scrollEnable;
				break;
			case MODE_ZOOM:
				isHandle = !zoomEnable;
				break;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			currentMode = MODE_DRAG;
			isHandle = !zoomEnable;
			break;
		case MotionEvent.ACTION_UP:
			currentMode = MODE_IDLE;
			isHandle = !scrollEnable;
			break;
		}
		return isHandle;
	}

}

package org.zywx.wbpalmstar.plugin.uexbaidumap;

import com.baidu.mapapi.GeoPoint;

/**
 * 监听用户拖拽地图的事件
 */
public interface OnMapDragListener {
	void onMapDrag(EnhanceMapView mapView, int state, GeoPoint point);
}

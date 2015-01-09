package org.zywx.wbpalmstar.plugin.uexbaidumap;

import com.baidu.mapapi.GeoPoint;

public interface OverlayTapCallback {
	boolean onOverlayTaped(String overlayId, GeoPoint point);
}

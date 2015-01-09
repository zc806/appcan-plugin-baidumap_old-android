package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.app.Activity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.RouteOverlay;

public class IdentityRouteOverlay extends RouteOverlay {

	private String id;

	public IdentityRouteOverlay(Activity context, MapView mapView, String id) {
		super(context, mapView);
		this.id = id;
	}

	public String getId() {
		return id;
	}

}

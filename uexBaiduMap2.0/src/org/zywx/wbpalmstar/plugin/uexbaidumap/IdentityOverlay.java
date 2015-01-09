package org.zywx.wbpalmstar.plugin.uexbaidumap;

import com.baidu.mapapi.Overlay;

public class IdentityOverlay extends Overlay {

	private String id;

	public IdentityOverlay(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

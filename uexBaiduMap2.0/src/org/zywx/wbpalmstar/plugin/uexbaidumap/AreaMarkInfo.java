package org.zywx.wbpalmstar.plugin.uexbaidumap;

import com.baidu.mapapi.GeoPoint;

public class AreaMarkInfo {

	private GeoPoint ltPoint;
	private GeoPoint rbPoint;
	private String id;
	private String imgUrl;

	public GeoPoint getLtPoint() {
		return ltPoint;
	}

	public void setLtPoint(GeoPoint ltPoint) {
		this.ltPoint = ltPoint;
	}

	public GeoPoint getRbPoint() {
		return rbPoint;
	}

	public void setRbPoint(GeoPoint rbPoint) {
		this.rbPoint = rbPoint;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public boolean isBasicOk() {
		if (id != null && ltPoint != null && rbPoint != null && imgUrl != null) {
			return true;
		} else {
			return false;
		}
	}

}
package org.zywx.wbpalmstar.plugin.uexbaidumap;

import com.baidu.mapapi.GeoPoint;

public class CircleInfo {

	private String id;
	private int fillColor;
	private int strokeColor;
	private int lineWidth;
	private int radius;
	private GeoPoint centerPoint;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFillColor() {
		return fillColor;
	}

	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}

	public int getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(int strokeColor) {
		this.strokeColor = strokeColor;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public GeoPoint getCenterPoint() {
		return centerPoint;
	}

	public void setCenterPoint(GeoPoint centerPoint) {
		this.centerPoint = centerPoint;
	}

}

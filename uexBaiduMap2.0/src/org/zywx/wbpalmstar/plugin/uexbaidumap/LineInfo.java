package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import java.util.List;
import com.baidu.mapapi.GeoPoint;

public class LineInfo {

	
	
	private String id;
	private List<GeoPoint> list;
	private int fillColor;
	private int strokeColor;
	private int lineWidth;

	public LineInfo() {
		list = new ArrayList<GeoPoint>();
	}

	
	public void addGeoPoint(GeoPoint geoPoint) {
		list.add(geoPoint);
	}

	public List<GeoPoint> getGeoPointList() {
		return list;
	}

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

}

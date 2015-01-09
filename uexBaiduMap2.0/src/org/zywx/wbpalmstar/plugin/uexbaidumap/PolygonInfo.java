package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import java.util.List;
import com.baidu.mapapi.GeoPoint;

/**
 * 封装多边形overlay信息
 * 
 * @author zhenyu.fang
 */
public class PolygonInfo {

	private int fillColor;
	private int strokeColor;
	private int lineWidth;
	private String id;
	private List<GeoPoint> geoPoints;

	public PolygonInfo() {
		geoPoints = new ArrayList<GeoPoint>();
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}

	public void addGeoPoint(GeoPoint geoPoint) {
		geoPoints.add(geoPoint);
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

}

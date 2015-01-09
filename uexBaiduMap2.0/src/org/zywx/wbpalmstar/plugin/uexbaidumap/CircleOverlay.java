package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;

public class CircleOverlay extends IdentityOverlay {

	private GeoPoint geoPoint;
	private int fillColor;
	public int strokeColor;
	private int lineWidth;
	private int radius;
	private Paint paint;

	public CircleOverlay(CircleInfo circleInfo) {
		super(circleInfo.getId());
		this.geoPoint = circleInfo.getCenterPoint();
		this.fillColor = circleInfo.getFillColor();
		this.strokeColor = circleInfo.getStrokeColor();
		this.lineWidth = circleInfo.getLineWidth();
		this.radius = circleInfo.getRadius();
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(lineWidth);
		paint.setColor(fillColor);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		int radiusPx = (int) projection.metersToEquatorPixels(radius);
		Point point = projection.toPixels(geoPoint, null);
		if (point != null) {
			canvas.drawCircle(point.x, point.y, radiusPx, paint);
		}
	}

}
package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.List;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;

/**
 * 多边形Overlay
 * 
 * @author zhenyu.fang
 * 
 */
public class PolygonOverlay extends IdentityOverlay {

	private Paint paint;
	private List<GeoPoint> geoPoints;
	private Path path;

	public PolygonOverlay(PolygonInfo polygonInfo) {
		super(polygonInfo.getId());
		geoPoints = polygonInfo.getGeoPoints();
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(polygonInfo.getFillColor());
		paint.setStrokeWidth(polygonInfo.getLineWidth());
		paint.setStyle(Style.FILL_AND_STROKE);
		path = new Path();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		List<GeoPoint> localList = geoPoints;
		Path localPath = path;
		Paint localPaint = paint;
		localPath.reset();
		for (int i = 0, size = localList.size(); i < size; i++) {
			GeoPoint geoPoint = localList.get(i);
			Point point = projection.toPixels(geoPoint, null);
			if (point != null) {
				if (i == 0) {
					// 起始点
					localPath.moveTo(point.x, point.y);
				} else if (i == size - 1) {
					// 终点，封闭路线汇成多边形
					localPath.lineTo(point.x, point.y);
					localPath.close();
				} else {
					localPath.lineTo(point.x, point.y);
				}
			}
		}
		canvas.drawPath(localPath, localPaint);
	}

}

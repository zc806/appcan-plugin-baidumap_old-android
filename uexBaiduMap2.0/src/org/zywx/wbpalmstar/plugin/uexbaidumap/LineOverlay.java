package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;

public class LineOverlay extends IdentityOverlay {

	private List<GeoPoint> pointList;
	private Paint pathPaint = null;
	private Path path;
	private Paint dotPaint;

	public LineOverlay(LineInfo lineInfo) {
		super(lineInfo.getId());
		pointList = new ArrayList<GeoPoint>();
		List<GeoPoint> geoPoints = lineInfo.getGeoPointList();
		if (geoPoints != null) {
			pointList.addAll(geoPoints);
		}
		pathPaint = new Paint();
		pathPaint.setAntiAlias(true);
		pathPaint.setColor(lineInfo.getFillColor());
		pathPaint.setStrokeWidth(lineInfo.getLineWidth());
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);
		path = new Path();
		dotPaint = new Paint();
		dotPaint.setColor(Color.RED);
		dotPaint.setStrokeWidth(40);
		dotPaint.setAntiAlias(true);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		path.reset();
		List<GeoPoint> localList = pointList;
		Paint localPaint = pathPaint;
		int size = localList.size();
		for (int i = 0; i < size; i++) {
			GeoPoint gp = localList.get(i);
			Point point = projection.toPixels(gp, null);
			if (point != null) {
				if (i == 0) {
					path.moveTo(point.x, point.y);
				} else {
					path.lineTo(point.x, point.y);
				}
				canvas.drawPath(path, localPaint);
			}// end if null
		}// end for

	}
}

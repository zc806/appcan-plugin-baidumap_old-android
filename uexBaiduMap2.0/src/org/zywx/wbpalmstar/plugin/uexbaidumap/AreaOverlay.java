package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Projection;

public class AreaOverlay extends IdentityOverlay {

	public static final String TAG = "AreaOverlay";
	private GeoPoint ltGP;
	private GeoPoint rbGP;
	private Bitmap bitmap;
	private MapView mapView;
	private Matrix matrix = new Matrix();
	private float[] values = new float[9];
	private String imageUrl;

	public AreaOverlay(MapView mapView, AreaMarkInfo areaMarkInfo, Bitmap bitmap) {
		super(areaMarkInfo.getId());
		this.ltGP = areaMarkInfo.getLtPoint();
		this.rbGP = areaMarkInfo.getRbPoint();
		imageUrl = areaMarkInfo.getImgUrl();
		this.bitmap = bitmap;
		this.mapView = mapView;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Point ltPoint = projection.toPixels(ltGP, null);
		Point rbPoint = projection.toPixels(rbGP, null);
		if (ltPoint != null && rbPoint != null && bitmap != null) {
			setMatrixByPoint(matrix, ltPoint, rbPoint, bitmap);
			canvas.drawBitmap(bitmap, matrix, null);
		}
	}

	public void setMatrixByPoint(Matrix matrix, Point ltPoint, Point rbPoint, Bitmap bitmap) {
		float bitmapWidth = bitmap.getWidth();
		float bitmapHeight = bitmap.getHeight();
		float rangeX = Math.abs(rbPoint.x - ltPoint.x);
		float rangeY = Math.abs(rbPoint.y - ltPoint.y);
		float scaleX = rangeX / bitmapWidth;
		float scaleY = rangeY / bitmapHeight;
		matrix.reset();
		matrix.setScale(scaleX, scaleY, Math.abs(rbPoint.x - ltPoint.y), Math.abs(rbPoint.y - ltPoint.y));
		float transX = getValue(matrix, Matrix.MTRANS_X);
		float transY = getValue(matrix, Matrix.MTRANS_Y);
		matrix.postTranslate(ltPoint.x - transX, ltPoint.y - transY);
	}

	protected float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(values);
		return values[whichValue];
	}

	public void updatePosition(GeoPoint ltPoint, GeoPoint rbPoint) {
		this.ltGP = ltPoint;
		this.rbGP = rbPoint;
		this.mapView.postInvalidate();
	}

	public void updateImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void updateBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.mapView.postInvalidate();
	}

	public String getImageUrl() {
		return imageUrl;
	}

}

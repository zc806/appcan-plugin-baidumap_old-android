package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;

public class LocusOverlay extends Overlay {
	
	private ArrayList<GeoPoint> mPointList;
	private MapView mMapView;
	private Paint mPointPaint;
	private Paint mPathPaint;
	private Path mPath;	
	private Drawable mBeginDrawable;
	private Drawable mEndDrawable;
	private int mPathColor;
	
	public LocusOverlay(MapView view){
		mMapView = view;
		mPathColor = Color.BLUE;
		mPointList = new ArrayList<GeoPoint>();
		mPointPaint = new Paint();
		mPointPaint.setColor(mPathColor);
		mPointPaint.setAntiAlias(true);
		mPointPaint.setStyle(Style.FILL);
		
		mPathPaint = new Paint();
		mPathPaint.setColor(mPathColor);
		mPathPaint.setDither(true);
		mPathPaint.setStyle(Paint.Style.STROKE);
		mPathPaint.setStrokeJoin(Paint.Join.ROUND);
		mPathPaint.setStrokeCap(Paint.Cap.ROUND);
		mPathPaint.setStrokeWidth(4);
		mPath = new Path();
	}
	
	public void addPoint(GeoPoint p){
		mPointList.add(p);
		mMapView.postInvalidate();
	}
	
	public int getPointCount(){
		return mPointList.size();
	}
	
	public GeoPoint getLastPoint(){
		
		return mPointList.get(mPointList.size() - 1);
	}
	
	public void setBeginDrawable(Drawable d){
		mBeginDrawable = d;
		mMapView.postInvalidate();
	}
	
	public void setEndDrawable(Drawable d){
		mEndDrawable = d;
		mMapView.postInvalidate();
	}
	
	public void setPathColor(int color){
		mPathColor = color;
		mPointPaint.setColor(mPathColor);
		mPathPaint.setColor(mPathColor);
		mMapView.postInvalidate();
	}
	
	public void clear(){
		mPointList.clear();
		mMapView.postInvalidate();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		Projection projection = mapView.getProjection();
		mPath.reset();
		int size = mPointList.size();
		for(int i = 0; i < size; ++i){
			GeoPoint gp = mPointList.get(i);
			Point p = new Point();
			p = projection.toPixels(gp, p);
			//point
			if(0 == i && null != mBeginDrawable){
				int x = p.x - mBeginDrawable.getIntrinsicWidth() / 2;
				int y = p.y - mBeginDrawable.getIntrinsicHeight(); 
				Overlay.drawAt(canvas, mBeginDrawable, x, y, shadow);
			}else if((size - 1) == i && null != mEndDrawable){
				int x = p.x - mBeginDrawable.getIntrinsicWidth() / 2;
				int y = p.y - mBeginDrawable.getIntrinsicHeight(); 
				Overlay.drawAt(canvas, mEndDrawable, x, y, shadow);
			}
			canvas.drawCircle(p.x, p.y, 5.0f, mPointPaint);
			//path
			if(0 == i){
				mPath.moveTo(p.x, p.y);	
			}else{
				mPath.lineTo(p.x, p.y);
			}
			canvas.drawPath(mPath, mPathPaint);
		}
	}
}

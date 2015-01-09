package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask$ImageLoadTaskCallback;
import org.zywx.wbpalmstar.base.cache.ImageLoaderManager;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexbaidumap.MarkOverlays.MarkOverlayTapCallback;
import org.zywx.wbpalmstar.plugin.uexbaidumap.RoutePlanInfo.OnRoutePlanCallback;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;

public class BaiduMapLocationActivity extends MapActivity implements
		MKGeneralListener, LocationListener {

	public static final String TAG = "BaiduMapLocationActivity";
	// 地图模式：普通
	public static final int TYPE_NORMAL = 0;
	// 地图模式：卫星
	public static final int TYPE_SATELLITE = 1;
	// 开启实时交通
	public static final int TYPE_TRAFFIC_ON = 2;
	// 关闭实时交通
	public static final int TYPE_TRAFFIC_OFF = 3;

	public static final String INTENT_KEY_APIKEY = "apikey";
	private EnhanceMapView mMapView;
	private BMapManager mapManager;
	private LocationCallback mLocationCallback;
	public static final int DEFAULT_ZOOM_LEVEL = 15;
	public static final int LOCUS_ZOOM_LEVEL = 13;
	private LocusOverlay mLocusOverlay;
	private MarkOverlays mMarkOverlays;
	private BitmapCache mBitmapCache;
	private MyLocationOverlay myLocationOverlay;
	private OnMapOccurErrorCallback errorCallback;
	private MyLocationListener myLocationListener;
	private MKSearch mkSearch = null;
	private LayoutInflater inflater;
	private View popOverStyle1;
	private View popOverStyle2;
	private View popOverStyle3;
	private View popOverStyle4;
	private Animation popOverShowAnim;
	private Animation popOverHideAnim;
	private float density = DisplayMetrics.DENSITY_DEFAULT;
	private ImageLoaderManager imageLoaderManager = null;

	public void setErrorCallback(OnMapOccurErrorCallback errorCallback) {
		this.errorCallback = errorCallback;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i(TAG, "onCreate");
		String apiKey = getIntent().getStringExtra(INTENT_KEY_APIKEY);
		mMapView = new EnhanceMapView(this);
		mMapView.setClickable(true);
		RelativeLayout.LayoutParams mp = new RelativeLayout.LayoutParams(-1, -1);
		mMapView.setLayoutParams(mp);
		setContentView(mMapView);
		mapManager = new BMapManager(getApplication());
		mapManager.init(apiKey, this);
		mapManager.start();
		super.initMapActivity(mapManager);
		mMapView.getController().setZoom(DEFAULT_ZOOM_LEVEL);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setDrawOverlayWhenZooming(true);
		inflater = LayoutInflater.from(this);
		popOverShowAnim = AnimationUtils.loadAnimation(this,
				EUExUtil.getResAnimID("plugin_map_popover_show_anim"));
		popOverHideAnim = AnimationUtils.loadAnimation(this,
				EUExUtil.getResAnimID("plugin_map_popover_hide_anim"));
		density = getResources().getDisplayMetrics().density;
		imageLoaderManager = ImageLoaderManager.initImageLoaderManager(this);
		mBitmapCache = new BitmapCache();
		MyLog.i(TAG, "onCreateOver");
	}

	@Override
	protected void onResume() {
		if (myLocationOverlay != null) {
			if (myLocationListener != null) {
				mapManager.getLocationManager().requestLocationUpdates(
						myLocationListener);
			}
			// 开启指南针更新
			myLocationOverlay.enableCompass();
			// 开启位置更新
			myLocationOverlay.enableMyLocation();
			MyLog.i(TAG, "onResume() --> contain myOverlay:"
					+ mMapView.getOverlays().contains(myLocationOverlay));
		}
		mapManager.start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (myLocationOverlay != null) {
			if (myLocationListener != null) {
				mapManager.getLocationManager().removeUpdates(
						myLocationListener);
			}
			// 关闭指南针更新
			myLocationOverlay.disableCompass();
			// 关闭位置更新
			myLocationOverlay.disableMyLocation();
			MyLog.i(TAG,
					"onPause()  disableLocation-------> contain myOverlay:"
							+ mMapView.getOverlays()
									.contains(myLocationOverlay));
		}
		mapManager.stop();
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		MyLog.e(TAG, "BaiduMapLocationActivity: onLowMemory");
		super.onLowMemory();
	}

	@Override
	protected void onDestroy() {
		mapManager.destroy();
		mapManager = null;
		mBitmapCache.destroy();
		imageLoaderManager.clear();
		MyLog.i(TAG, "onDestroy");
		super.onDestroy();
	}

	public void clean() {
		mBitmapCache.clear();
		if (mMarkOverlays != null) {
			mMarkOverlays.clear();
		}
		if (mLocusOverlay != null) {
			mLocusOverlay.clear();
		}
		List<Overlay> overlays = mMapView.getOverlays();
		if (overlays != null) {
			overlays.clear();
		}
		if (popOverStyle1 != null && mMapView.indexOfChild(popOverStyle1) != -1) {
			mMapView.removeView(popOverStyle1);
		}
		if (popOverStyle2 != null && mMapView.indexOfChild(popOverStyle2) != -1) {
			mMapView.removeView(popOverStyle2);
		}
		if (popOverStyle3 != null && mMapView.indexOfChild(popOverStyle3) != -1) {
			mMapView.removeView(popOverStyle3);
		}
		if (popOverStyle4 != null && mMapView.indexOfChild(popOverStyle4) != -1) {
			mMapView.removeView(popOverStyle4);
		}
		MyLog.i(TAG, "clean");
	}

	public void setBeginDrawable(Drawable d) {
		if (null == mLocusOverlay) {
			mLocusOverlay = new LocusOverlay(mMapView);
			mMapView.getOverlays().add(mLocusOverlay);
		}
		mLocusOverlay.setBeginDrawable(d);
	}

	public void setEndDrawable(Drawable d) {
		if (null == mLocusOverlay) {
			mLocusOverlay = new LocusOverlay(mMapView);
			mMapView.getOverlays().add(mLocusOverlay);
		}
		mLocusOverlay.setEndDrawable(d);
	}

	public void setPathColor(int color) {
		if (null == mLocusOverlay) {
			mLocusOverlay = new LocusOverlay(mMapView);
			mMapView.getOverlays().add(mLocusOverlay);
		}
		mLocusOverlay.setPathColor(color);
	}

	public void setOnMapDragListener(OnMapDragListener lis) {
		mMapView.setOnMapDragListener(lis);
	}

	/**
	 * 地图显示位置监听
	 */
	private class MyLocationListener implements LocationListener {

		private LocationCallback callback;

		public MyLocationListener(LocationCallback callback) {
			this.callback = callback;
		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				callback.onLocationed(location.getLongitude(),
						location.getLatitude());
			}
		}

	}

	/**
	 * 显示用户位置标注
	 * 
	 * @param callback
	 */
	public void showUserLocation(LocationCallback callback) {
		if (myLocationOverlay == null) {
			myLocationOverlay = new MyLocationOverlay(this, mMapView);
			MKLocationManager mgr = mapManager.getLocationManager();
			if (myLocationListener != null) {
				mgr.removeUpdates(myLocationListener);
			}
			myLocationListener = new MyLocationListener(callback);
			mMapView.getOverlays().add(myLocationOverlay);
			mgr.requestLocationUpdates(myLocationListener);
			mgr.enableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
			mgr.enableProvider(MKLocationManager.MK_GPS_PROVIDER);
			myLocationOverlay.enableCompass();
			myLocationOverlay.enableMyLocation();
			MyLog.i(TAG, "showUserLocation");
		}
	}

	/**
	 * 隐藏用户位置标注
	 */
	public void hideUserLocation() {
		if (myLocationListener != null) {
			mapManager.getLocationManager().removeUpdates(myLocationListener);
			myLocationListener = null;
		}
		if (myLocationOverlay != null) {
			myLocationOverlay.disableCompass();
			myLocationOverlay.disableMyLocation();
			mMapView.getOverlays().remove(myLocationOverlay);
			mMapView.postInvalidate();
			myLocationOverlay = null;
		}
		MyLog.i(TAG, "hideUserLocation");
	}

	public void enableLocation(LocationCallback callback) {
		if (myLocationListener != null) {
			mapManager.getLocationManager().removeUpdates(myLocationListener);
		}
		mLocationCallback = callback;
		mapManager.stop();
		MKLocationManager mgr = mapManager.getLocationManager();
		mgr.requestLocationUpdates(this);
		mgr.enableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		mgr.enableProvider(MKLocationManager.MK_GPS_PROVIDER);
		mgr.setNotifyInternal(60, 5);
		mapManager.start();
		MyLog.i(TAG, "enableLocation");
	}

	@Override
	public void onLocationChanged(Location location) {
		MyLog.i(TAG, "this_onLocationChanged");
		if (location != null) {
			if (mLocationCallback != null) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				GeoPoint gpsPoint = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));
				mLocationCallback.onLocationed(
						((double) gpsPoint.getLongitudeE6()) / 1E6,
						((double) gpsPoint.getLatitudeE6()) / 1E6);
				disableLocation();
			} else {
				MyLog.w(TAG, "mLocationCallback is null");
			}
		} else {
			MyLog.e(TAG, "location is null");
		}
	}

	private void disableLocation() {
		MKLocationManager mgr = mapManager.getLocationManager();
		mgr.removeUpdates(this);
		mgr.disableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		mgr.disableProvider(MKLocationManager.MK_GPS_PROVIDER);
		mLocationCallback = null;
		MyLog.i(TAG, "disableLocation");
	}

	public void invlidate() {
		if (null != mMapView && !isFinishing()) {
			mMapView.postInvalidate();
		}
	}

	public void addAreaMark(List<AreaMarkInfo> list) {
		for (AreaMarkInfo areaMarkInfo : list) {
			String imageUrl = areaMarkInfo.getImgUrl();
			Bitmap cachedBitmap = imageLoaderManager.getCacheBitmap(imageUrl);
			final AreaOverlay areaOverlay = new AreaOverlay(mMapView,
					areaMarkInfo, cachedBitmap);
			if (cachedBitmap == null) {
				imageLoaderManager.asyncLoad(new ImageLoadTask(imageUrl) {

					@Override
					protected Bitmap doInBackground() {
						return MapUtillity.getImage(
								BaiduMapLocationActivity.this, filePath);
					}

				}.addCallback(new ImageLoadTask$ImageLoadTaskCallback() {

					@Override
					public void onImageLoaded(ImageLoadTask task, Bitmap bitmap) {
						if (areaOverlay.getImageUrl().equals(task.filePath)) {
							areaOverlay.updateBitmap(bitmap);
						}
					}
				}));
			}
			mMapView.getOverlays().add(areaOverlay);
		}
		mMapView.postInvalidate();
	}

	public void updateAreaMark(AreaMarkInfo areaMarkInfo) {
		if (areaMarkInfo == null) {
			return;
		}
		List<Overlay> overlays = mMapView.getOverlays();
		for (int i = 0, size = overlays.size(); i < size; i++) {
			Overlay overlay = overlays.get(i);
			if (overlay instanceof AreaOverlay) {
				final AreaOverlay areaOverlay = (AreaOverlay) overlay;
				if (areaOverlay.getId().equals(areaMarkInfo.getId())) {
					areaOverlay.updateImageUrl(areaMarkInfo.getImgUrl());
					areaOverlay.updatePosition(areaMarkInfo.getLtPoint(),
							areaMarkInfo.getRbPoint());
					String imageUrl = areaMarkInfo.getImgUrl();
					Bitmap cachedBitmap = imageLoaderManager
							.getCacheBitmap(imageUrl);
					if (cachedBitmap == null) {
						imageLoaderManager
								.asyncLoad(new ImageLoadTask(imageUrl) {

									@Override
									protected Bitmap doInBackground() {
										return MapUtillity.getImage(
												BaiduMapLocationActivity.this,
												filePath);
									}

								}.addCallback(new ImageLoadTask$ImageLoadTaskCallback() {

									@Override
									public void onImageLoaded(
											ImageLoadTask task, Bitmap bitmap) {
										if (areaOverlay.getImageUrl().equals(
												task.filePath)) {
											areaOverlay.updateBitmap(bitmap);
										}
									}
								}));
					} else {
						areaOverlay.updateBitmap(cachedBitmap);
					}
				}// end if id equals
			}// end if instanceof
		}// end for

	}

	public void addMark(ArrayList<MarkItem> markList,
			OverlayTapCallback callback) {
		Log.i("fzy", "ActivityAddMark()------>");
		if (null == mMarkOverlays) {
			int defaultDrawableId = EUExUtil
					.getResDrawableID("plugin_map_default_marker");
			Drawable dfd = null;
			if (0 != defaultDrawableId) {
				dfd = getResources().getDrawable(defaultDrawableId);
				if (null != dfd) {
					dfd.setBounds(0, 0, dfd.getIntrinsicWidth(),
							dfd.getIntrinsicHeight());
				}
			}
			mMarkOverlays = new MarkOverlays(null, this);
		}
		if (mMapView.getOverlays().contains(mMarkOverlays)) {
			// 已添加
			mMapView.getOverlays().remove(mMarkOverlays);
			mMapView.getOverlays().add(mMarkOverlays);
		} else {
			// 未添加
			mMapView.getOverlays().add(mMarkOverlays);
		}
		for (MarkItem item : markList) {
			final MarkItem finalMarkItem = item;
			Bitmap cachedBitmap = imageLoaderManager
					.getCacheBitmap(item.mDrawableUrl);
			Log.i("fzy", "cacheBitmap:" + cachedBitmap + " url:"
					+ item.mDrawableUrl);
			if (cachedBitmap == null) {
				if (item.mDrawableUrl.startsWith("http")) {
					imageLoaderManager.asyncLoad(new ImageLoadTask(
							item.mDrawableUrl) {

						@Override
						protected Bitmap doInBackground() {
							return MapUtillity.getImage(
									BaiduMapLocationActivity.this, filePath);
						}
					}.addCallback(new ImageLoadTask$ImageLoadTaskCallback() {

						@Override
						public void onImageLoaded(ImageLoadTask task,
								Bitmap bitmap) {
							Log.i("fzy", "onImageLoaded():" + bitmap);
							if (bitmap != null) {
								finalMarkItem.setMarker(new BitmapDrawable(
										bitmap));
								Log.i("fzy", "setMarker:" + bitmap);
							}
						}
					}));
				} else {
					Bitmap bitmap = MapUtillity.getImage(this,
							item.mDrawableUrl);
					if (bitmap != null) {
						item.setMarker(new BitmapDrawable(bitmap));
					}
				}
			} else {
				item.setMarker(new BitmapDrawable(cachedBitmap));
				Log.i("fzy", "dirertSetMarker:" + cachedBitmap);
			}
			mMarkOverlays.addOverlay(item);
		}
		mMarkOverlays.setOverlayTapCallback(callback);
		mMarkOverlays.setMarkOverlayTapCallback(new MarkOverlayTapCallback() {

			@Override
			public void onMarkOverlayTap(GeoPoint gp, MapView mapView) {
				hidePopOverView();
			}
		});
		mMapView.postInvalidate();
	}

	static Drawable norDrawable = null;

	// TODO
	public void addMarkNewStyle(ArrayList<MarkItem> markList,
			OverlayTapCallback callback) {
		if (null == mMarkOverlays) {
			int defaultDrawableId = EUExUtil
					.getResDrawableID("plugin_map_default_marker");
			Drawable dfd = null;
			if (0 != defaultDrawableId) {
				dfd = getResources().getDrawable(defaultDrawableId);
				if (null != dfd) {
					dfd.setBounds(0, 0, dfd.getIntrinsicWidth(),
							dfd.getIntrinsicHeight());
				}
			}
			mMarkOverlays = new MarkOverlays(null, this);
		}
		if (mMapView.getOverlays().contains(mMarkOverlays)) {
			// 已添加
			mMapView.getOverlays().remove(mMarkOverlays);
			mMapView.getOverlays().add(mMarkOverlays);
		} else {
			// 未添加
			mMapView.getOverlays().add(mMarkOverlays);
		}
		for (MarkItem item : markList) {

			View view = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_map_popover_mark"), null);
			TextView tvTitle = (TextView) view.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_mark_title"));
			RatingBar ratingBar = (RatingBar) view.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_mark_rate"));
			ViewGroup group = (ViewGroup) view.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_mark_root"));

			MarkViewHolder viewHolder = new MarkViewHolder(tvTitle, ratingBar,
					group);
			tvTitle.setTextColor(item.fontColor);
			if (item.mDrawableUrl != null && item.mDrawableUrl.length() > 0) {
				if (norDrawable == null) {
					try {
						String assetFileName = null;
						String path = item.mDrawableUrl;
						if (path.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
							assetFileName = BUtility.F_Widget_RES_path
									+ path.substring(BUtility.F_Widget_RES_SCHEMA
											.length());
						}
						norDrawable = NinePatchUtils.decodeDrawableFromAsset(
								BaiduMapLocationActivity.this, assetFileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			viewHolder.updateData(item.mId, item.title, item.star, norDrawable);

			view.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
			view.buildDrawingCache();
			view.setDrawingCacheEnabled(true);
			Bitmap bitmap = view.getDrawingCache();
			Bitmap bitmap2 = Bitmap.createBitmap(bitmap);
			view.setDrawingCacheEnabled(false);
			item.setMarker(new BitmapDrawable(bitmap2));
			mMarkOverlays.addOverlay(item);
		}
		mMarkOverlays.setOverlayTapCallback(callback);
		mMarkOverlays.setMarkOverlayTapCallback(new MarkOverlayTapCallback() {

			@Override
			public void onMarkOverlayTap(GeoPoint gp, MapView mapView) {
				hidePopOverView();
			}
		});
		mMapView.postInvalidate();
	}

	/**
	 * 隐藏弹出层
	 */
	private void hidePopOverView() {
		if (popOverStyle1 != null
				&& popOverStyle1.getVisibility() == View.VISIBLE) {
			popOverStyle1.startAnimation(popOverHideAnim);
			popOverStyle1.setVisibility(View.GONE);
		}

		if (popOverStyle2 != null
				&& popOverStyle2.getVisibility() == View.VISIBLE) {
			popOverStyle2.startAnimation(popOverHideAnim);
			popOverStyle2.setVisibility(View.GONE);
		}

		if (popOverStyle3 != null
				&& popOverStyle3.getVisibility() == View.VISIBLE) {
			popOverStyle3.startAnimation(popOverHideAnim);
			popOverStyle3.setVisibility(View.GONE);
			BDebug.i(TAG, "hidePopOverStyle3():");
		}
	}

	private void hidePopOverViewById(String markId) {
		if (markId == null || markId.length() == 0) {
			return;
		}
		if (popOverStyle1 != null
				&& popOverStyle1.getVisibility() == View.VISIBLE) {
			PopOverViewHolder viewHolder = (PopOverViewHolder) popOverStyle1
					.getTag();
			if (markId.equals(viewHolder.getId())) {
				popOverStyle1.startAnimation(popOverHideAnim);
				popOverStyle1.setVisibility(View.GONE);
			}
		}

		if (popOverStyle2 != null
				&& popOverStyle2.getVisibility() == View.VISIBLE) {
			PopOverViewHolder viewHolder = (PopOverViewHolder) popOverStyle2
					.getTag();
			if (markId.equals(viewHolder.getId())) {
				popOverStyle2.startAnimation(popOverHideAnim);
				popOverStyle2.setVisibility(View.GONE);
			}
		}

		if (popOverStyle3 != null
				&& popOverStyle3.getVisibility() == View.VISIBLE) {
			PopOverViewHolder viewHolder = (PopOverViewHolder) popOverStyle3
					.getTag();
			if (markId.equals(viewHolder.getId())) {
				popOverStyle3.startAnimation(popOverHideAnim);
				popOverStyle3.setVisibility(View.GONE);
				BDebug.i(TAG, "hidePopOverStyle3():" + markId);
			}
		}
	}

	public void updateMark(MarkItem markItem) {
		if (mMarkOverlays != null && markItem != null) {
			if (mMarkOverlays.updateItem(markItem)) {
				mBitmapCache.getImage(markItem, this);
				mMarkOverlays.myPopulate();
				mMapView.postInvalidate();
			}
		}
	}

	public void clearMarks(String[] ids) {
		if (mMarkOverlays != null) {
			if (ids == null) {
				mMarkOverlays.removeAllOverlay();
				// 清除所有Mark时关闭所有PopOverView
				hidePopOverView();
			} else {
				for (String id : ids) {
					mMarkOverlays.removeOverlay(id);
					// 清除指定markId弹出的PopOverView
					hidePopOverViewById(id);
				}
			}
			mMapView.postInvalidate();
		}
	}

	public void setCenter(double longitude, double latitude) {
		GeoPoint geoPoint = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));
		mMapView.getController().setCenter(geoPoint);
		MyLog.i(TAG, "setCenter");
	}

	/**
	 * 缩放到指定的经纬度跨度
	 * 
	 * @param longitudeSpan
	 *            精度跨度
	 * @param latitudeSpan
	 *            纬度跨度
	 */
	public void zoomToSpan(float longitudeSpan, float latitudeSpan) {
		mMapView.getController().zoomToSpan((int) (longitudeSpan * 1E6),
				(int) (latitudeSpan * 1E6));
	}

	/**
	 * 设置地图显示模式
	 * 
	 * @param type
	 */
	public void setType(int type) {
		switch (type) {
		case TYPE_NORMAL:
			// 普通模式
			mMapView.setSatellite(false);
			break;
		case TYPE_SATELLITE:
			// 卫星模式
			mMapView.setSatellite(true);
			break;
		case TYPE_TRAFFIC_ON:
			// 开启实时交通模式
			mMapView.setTraffic(true);
			break;
		case TYPE_TRAFFIC_OFF:
			// 关闭实时交通模式
			mMapView.setTraffic(false);
			break;
		}
	}

	/**
	 * 地图 放大一级
	 */
	public void zoomIn() {
		mMapView.getController().zoomIn();
	}

	/**
	 * 地图缩小一级
	 */
	public void zoomOut() {
		mMapView.getController().zoomOut();
	}

	public void setZoomLevel(int lev) {
		if (lev > 18) {
			lev = 18;
		}
		if (lev < 3) {
			lev = 3;
		}
		mMapView.getController().setZoom(lev);
	}

	/**
	 * 添加直线Overlay
	 * 
	 * @param lineInfo
	 */
	public void addLineOverlay(LineInfo lineInfo) {
		LineOverlay lineOverlay = new LineOverlay(lineInfo);
		mMapView.getOverlays().add(lineOverlay);
		mMapView.postInvalidate();
	}

	public void addCircleOverlay(CircleInfo circleInfo) {
		CircleOverlay circleOverlay = new CircleOverlay(circleInfo);
		mMapView.getOverlays().add(circleOverlay);
		mMapView.postInvalidate();
	}

	/**
	 * 添加多边形Overlay
	 * 
	 * @param polygonInfo
	 */
	public void addPolygonOverlay(PolygonInfo polygonInfo) {
		PolygonOverlay polygonOverlay = new PolygonOverlay(polygonInfo);
		mMapView.getOverlays().add(polygonOverlay);
		mMapView.postInvalidate();
	}

	public void clearAreaMark(String[] ids) {
		List<Overlay> overlays = mMapView.getOverlays();
		Iterator<Overlay> iterator = overlays.iterator();
		// 未指定ID，删除全部
		if (ids == null) {
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof AreaOverlay) {
					iterator.remove();
				}
			}
		} else {
			// 指定了ID数组，删除指定的Overlay
			int length = ids.length;
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof AreaOverlay) {
					AreaOverlay areaOverlay = (AreaOverlay) overlay;
					inner: for (int i = 0; i < length; i++) {
						String currentId = ids[i];
						if (areaOverlay.getId().equals(currentId)) {
							iterator.remove();
							break inner;
						}
					}
				}
			}
		}
		mMapView.postInvalidate();
	}

	public void clearOverLayers(String[] ids) {
		List<Overlay> overlays = mMapView.getOverlays();
		Iterator<Overlay> iterator = overlays.iterator();
		if (ids == null) {
			// 未指定ID，删除全部
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof IdentityOverlay) {
					iterator.remove();
				}
			}
		} else {
			// 指定了ID数组，删除指定的Overlay
			int length = ids.length;
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof IdentityOverlay) {
					IdentityOverlay identityOverlay = (IdentityOverlay) overlay;
					inner: for (int i = 0; i < length; i++) {
						String currentId = ids[i];
						if (identityOverlay.getId().equals(currentId)) {
							iterator.remove();
							break inner;
						}
					}
				}
			}
		}
		mMapView.postInvalidate();
	}

	public void hideMap(boolean needJudge) {
		View view = getWindow().getDecorView();
		if (needJudge) {
			if (view.isShown()) {
				view.setVisibility(View.GONE);
			}
		} else {
			view.setVisibility(View.GONE);
		}
	}

	public void showMap() {
		View decorView = getWindow().getDecorView();
		if (!decorView.isShown()) {
			decorView.setVisibility(View.VISIBLE);
		}
	}

	// 纬度,经度
	public void addTrack(double longitude, double latitude) {
		if (null == mLocusOverlay) {
			mLocusOverlay = new LocusOverlay(mMapView);
		}
		if (mMapView.getOverlays().contains(mLocusOverlay)) {
			mMapView.getOverlays().remove(mLocusOverlay);
			mMapView.getOverlays().add(mLocusOverlay);
		} else {
			mMapView.getOverlays().add(mLocusOverlay);
		}
		GeoPoint gPoint = new GeoPoint((int) (longitude * 1E6),
				(int) (latitude * 1E6));
		mLocusOverlay.addPoint(gPoint);
		int c = mLocusOverlay.getPointCount();
		if (c == 1) {
			mMapView.getController().animateTo(gPoint);
		} else {
			mMapView.getController().animateTo(mLocusOverlay.getLastPoint());
		}
	}

	public void clearTrack() {
		if (null != mLocusOverlay) {
			mLocusOverlay.clear();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		if (myLocationOverlay == null) {
			return false;
		}
		return myLocationOverlay.isMyLocationEnabled();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onGetNetworkState(int state) {
		Toast.makeText(this, "网络已断开!", Toast.LENGTH_LONG).show();
		if (errorCallback != null) {
			errorCallback.onNetworkError();
		}
	}

	@Override
	public void onGetPermissionState(int iError) {
		if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
			Toast.makeText(this, "appkey错误,无法打开百度地图!", Toast.LENGTH_LONG)
					.show();
			if (errorCallback != null) {
				errorCallback.onPermissionDenied();
			}
		}
	}

	/**
	 * 注册搜索回调函数
	 * 
	 * @param listener
	 */
	public void registerMKSearchCallback(MKSearchListener listener) {
		if (mkSearch == null) {
			mkSearch = new MKSearch();
		}
		mkSearch.init(mapManager, listener);
	}

	/**
	 * 设置搜索POI时每页的结果数
	 * 
	 * @param size
	 */
	public void setPoiPageCapacity(int size) {
		MKSearch.setPoiPageCapacity(size);
	}

	/**
	 * 设置是否允许缩放
	 * 
	 * @param enable
	 */
	public void setZoomEnable(boolean enable) {
		if (enable) {
			mMapView.setBuiltInZoomControls(true);
		} else {
			mMapView.setBuiltInZoomControls(false);
		}
	}

	public void setScrollEnable(boolean enable) {
		// mMapView.setScrollEnable(enable);
	}

	/**
	 * 关键字联想搜索
	 * 
	 * @param key
	 */
	public void suggestionSearch(String key, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.suggestionSearch(key);
	}

	/**
	 * 在指定城市内根据关键字搜索poi兴趣点
	 * 
	 * @param city
	 * @param key
	 * @param pageIndex
	 */
	public void poiSearchInCity(String city, String key, int pageIndex,
			MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.goToPoiPage(pageIndex);
		mkSearch.poiSearchInCity(city, key);
	}

	/**
	 * 在指定中心点和半径范围内搜索poi兴趣点
	 * 
	 * @param key
	 * @param gp
	 * @param radius
	 * @param pageIndex
	 */
	public void poiSearchNearBy(String key, GeoPoint gp, int radius,
			int pageIndex, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.goToPoiPage(pageIndex);
		mkSearch.poiSearchNearBy(key, gp, radius);
	}

	/**
	 * 在指定的精度为范围内搜索poi兴趣点
	 * 
	 * @param key
	 * @param ltGP
	 * @param rbGP
	 * @param pageIndex
	 */
	public void poiSearchInBounds(String key, GeoPoint ltGP, GeoPoint rbGP,
			int pageIndex, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.goToPoiPage(pageIndex);
		mkSearch.poiSearchInbounds(key, ltGP, rbGP);
	}

	/**
	 * 根据多个关键字在指定中心点和范围类搜索poi兴趣点
	 * 
	 * @param key
	 * @param gp
	 * @param raduis
	 * @param pageIndex
	 */
	public void poiMultiSearchNearBy(String[] key, GeoPoint gp, int raduis,
			int pageIndex, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.goToPoiPage(pageIndex);
		mkSearch.poiMultiSearchNearBy(key, gp, raduis);
	}

	/**
	 * 根据多个关键字在指定经纬度范围内搜索poi兴趣点
	 * 
	 * @param key
	 * @param ltGP
	 * @param rbGP
	 * @param pageIndex
	 */
	public void poiMultiSearchInBounds(String[] key, GeoPoint ltGP,
			GeoPoint rbGP, int pageIndex, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.goToPoiPage(pageIndex);
		mkSearch.poiMultiSearchInbounds(key, ltGP, rbGP);
	}

	/**
	 * 搜索公交线路
	 * 
	 * @param city
	 * @param lineUid
	 */
	public void busLineSearch(String city, String lineUid,
			MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.poiSearchInCity(city, lineUid);
	}

	public void busLineSearch(String city, String lineUid) {
		mkSearch.busLineSearch(city, lineUid);
	}

	public void busLineResultSearch(MKBusLineResult busLineResult) {
		IdentityRouteOverlay routeOverlay = new IdentityRouteOverlay(
				BaiduMapLocationActivity.this, mMapView, "");
		routeOverlay.setData(busLineResult.getBusRoute());
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(routeOverlay);
		mMapView.invalidate();
		mMapView.getController().animateTo(
				busLineResult.getBusRoute().getStart());
	}

	/**
	 * 根据位置名称获得地理位置
	 * 
	 * @param city
	 * @param address
	 */
	public void geocode(String city, String address, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.geocode(address, city);
	}

	/**
	 * 根据经纬度获取位置信息
	 * 
	 * @param gp
	 */
	public void reverseGeocode(GeoPoint gp, MKSearchListener listener) {
		registerMKSearchCallback(listener);
		mkSearch.reverseGeocode(gp);
	}

	/**
	 * 搜索驾车线路
	 * 
	 * @param routePlanInfo
	 * @param callback
	 */
	public void drivingSearch(RoutePlanInfo routePlanInfo,
			DefaultMKSearchCallback callback) {
		registerMKSearchCallback(callback);
		mkSearch.drivingSearch(routePlanInfo.getStartCity(),
				routePlanInfo.getStartNode(), routePlanInfo.getEndCity(),
				routePlanInfo.getEndNode());
	}

	/**
	 * 搜索公交线路
	 * 
	 * @param routePlanInfo
	 * @param callback
	 */
	public void transitSearch(RoutePlanInfo routePlanInfo,
			DefaultMKSearchCallback callback) {
		registerMKSearchCallback(callback);
		mkSearch.transitSearch(routePlanInfo.getStartCity(),
				routePlanInfo.getStartNode(), routePlanInfo.getEndNode());
	}

	/**
	 * 搜搜步行路线
	 * 
	 * @param routePlanInfo
	 * @param callback
	 */
	public void walkingSearch(RoutePlanInfo routePlanInfo,
			DefaultMKSearchCallback callback) {
		registerMKSearchCallback(callback);
		mkSearch.walkingSearch(routePlanInfo.getStartCity(),
				routePlanInfo.getStartNode(), routePlanInfo.getEndCity(),
				routePlanInfo.getEndNode());
	}

	public void showRoutePlan(final RoutePlanInfo routePlanInfo,
			final OnRoutePlanCallback callback) {
		registerMKSearchCallback(new DefaultMKSearchCallback() {

			@Override
			public void onGetDrivingRouteResult(
					MKDrivingRouteResult drivingRouteResult, int iError) {
				if (drivingRouteResult == null || iError != 0) {
					if (callback != null) {
						callback.onRoutePlanResultOk(false);
					}
				} else {
					IdentityRouteOverlay routeOverlay = new IdentityRouteOverlay(
							BaiduMapLocationActivity.this, mMapView,
							routePlanInfo.getId());
					routeOverlay.setData(drivingRouteResult.getPlan(0)
							.getRoute(0));
					mMapView.getOverlays().add(routeOverlay);
					mMapView.invalidate();
					if (callback != null) {
						callback.onRoutePlanResultOk(true);
					}
				}
			}

			@Override
			public void onGetTransitRouteResult(
					MKTransitRouteResult transitRouteResult, int iError) {
				if (transitRouteResult == null || iError != 0) {
					if (callback != null) {
						callback.onRoutePlanResultOk(false);
					}
				} else {
					IdentityRouteOverlay routeOverlay = new IdentityRouteOverlay(
							BaiduMapLocationActivity.this, mMapView,
							routePlanInfo.getId());
					routeOverlay.setData(transitRouteResult.getPlan(0)
							.getRoute(0));
					mMapView.getOverlays().add(routeOverlay);
					mMapView.invalidate();
					if (callback != null) {
						callback.onRoutePlanResultOk(true);
					}
				}
			}

			@Override
			public void onGetWalkingRouteResult(
					MKWalkingRouteResult walkingRouteResult, int iError) {
				if (walkingRouteResult == null || iError != 0) {
					if (callback != null) {
						callback.onRoutePlanResultOk(false);
					}
				} else {
					IdentityRouteOverlay routeOverlay = new IdentityRouteOverlay(
							BaiduMapLocationActivity.this, mMapView,
							routePlanInfo.getId());
					routeOverlay.setData(walkingRouteResult.getPlan(0)
							.getRoute(0));
					mMapView.getOverlays().add(routeOverlay);
					mMapView.invalidate();
					callback.onRoutePlanResultOk(true);
				}
			}
		});
		switch (routePlanInfo.getType()) {
		case RoutePlanInfo.PLAN_TYPE_DRIVE:
			mkSearch.drivingSearch(routePlanInfo.getStartCity(),
					routePlanInfo.getStartNode(), routePlanInfo.getEndCity(),
					routePlanInfo.getEndNode());
			break;
		case RoutePlanInfo.PLAN_TYPE_TRANS:
			mkSearch.transitSearch(routePlanInfo.getStartCity(),
					routePlanInfo.getStartNode(), routePlanInfo.getEndNode());
			break;
		case RoutePlanInfo.PLAN_TYPE_WALK:
			mkSearch.walkingSearch(routePlanInfo.getStartCity(),
					routePlanInfo.getStartNode(), routePlanInfo.getEndCity(),
					routePlanInfo.getEndNode());
			break;
		}

	}

	/**
	 * 根据ID数组删除指定ID的路线计划
	 * 
	 * @param ids
	 */
	public void clearRoutePlan(String[] ids) {
		List<Overlay> overlays = mMapView.getOverlays();
		Iterator<Overlay> iterator = overlays.iterator();
		if (ids == null) {
			// 未指定ID，删除全部
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof IdentityRouteOverlay) {
					iterator.remove();
				}
			}
		} else {
			// 指定了ID数组，删除指定的Overlay
			int length = ids.length;
			while (iterator.hasNext()) {
				Overlay overlay = iterator.next();
				if (overlay instanceof IdentityRouteOverlay) {
					IdentityRouteOverlay routeOverlay = (IdentityRouteOverlay) overlay;
					inner: for (int i = 0; i < length; i++) {
						String currentId = ids[i];
						if (routeOverlay.getId().equals(currentId)) {
							iterator.remove();
							break inner;
						}
					}
				}
			}
		}
		mMapView.postInvalidate();
	}

	public void showPopOverStyle1(final String markId, String title,
			String content, String imgUrl,
			final BubbleViewTouchCallback callback) {
		MarkItem markItem = mMarkOverlays.getMarkItemById(markId);
		if (markItem == null) {
			return;
		}
		PopOverViewHolder viewHolder = null;
		if (popOverStyle1 == null) {
			popOverStyle1 = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_map_popover_style1"), null);
			TextView tvTitle = (TextView) popOverStyle1.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_title"));
			TextView tvContent = (TextView) popOverStyle1.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_content"));
			ImageView ivIcon = (ImageView) popOverStyle1.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_styele1_image"));
			viewHolder = new PopOverViewHolder(this, tvTitle, tvContent, ivIcon);
			popOverStyle1.setTag(viewHolder);

		} else {
			viewHolder = (PopOverViewHolder) popOverStyle1.getTag();
		}
		if (mMapView.indexOfChild(popOverStyle1) == -1) {
			// 将popOverStyle1加入MapView但是隐藏显示
			mMapView.addView(popOverStyle1, new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
					MapView.LayoutParams.BOTTOM_CENTER));
			popOverStyle1.setVisibility(View.GONE);
		}
		viewHolder.updateData(markId, title, content, imgUrl);
		mMapView.updateViewLayout(popOverStyle1,
				new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, markItem.getPoint(), 0,
						-(int) (markItem.mImgHeight / density),
						MapView.LayoutParams.BOTTOM_CENTER));
		popOverStyle1.startAnimation(popOverShowAnim);
		popOverStyle1.setVisibility(View.VISIBLE);
		popOverStyle1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (callback != null) {
							callback.onBubbleTouch(markId);
						}
					}
				});

			}
		});
	}

	public void showPopOverStyle2(final String markId, String title,
			String content, final BubbleViewTouchCallback callback) {
		MarkItem markItem = mMarkOverlays.getMarkItemById(markId);
		if (markItem == null) {
			return;
		}
		PopOverViewHolder viewHolder = null;
		if (popOverStyle2 == null) {
			popOverStyle2 = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_map_popover_style2"), null);
			TextView tvTitle = (TextView) popOverStyle2.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_title"));
			TextView tvContent = (TextView) popOverStyle2.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_content"));
			viewHolder = new PopOverViewHolder(this, tvTitle, tvContent, null);
			popOverStyle2.setTag(viewHolder);
		} else {
			viewHolder = (PopOverViewHolder) popOverStyle2.getTag();
		}
		if (mMapView.indexOfChild(popOverStyle2) == -1) {
			// 将popOverStyle2加入MapView但是隐藏显示
			mMapView.addView(popOverStyle2, new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
					MapView.LayoutParams.BOTTOM_CENTER));
			popOverStyle2.setVisibility(View.GONE);
		}
		viewHolder.updateData(markId, title, content, null);
		mMapView.updateViewLayout(popOverStyle2,
				new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, markItem.getPoint(), 0,
						-(int) (markItem.mImgHeight / density),
						MapView.LayoutParams.BOTTOM_CENTER));
		popOverStyle2.startAnimation(popOverShowAnim);
		popOverStyle2.setVisibility(View.VISIBLE);
		popOverStyle2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (callback != null) {
							callback.onBubbleTouch(markId);
						}
					}
				});
			}
		});
	}

	public void showPopOverStyle3(final String markId, String title,
			String content, final BubbleViewTouchCallback callback) {
		MarkItem markItem = mMarkOverlays.getMarkItemById(markId);
		if (markItem == null) {
			return;
		}
		PopOverViewHolder viewHolder = null;
		if (popOverStyle3 == null) {
			popOverStyle3 = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_map_popover_style3"), null);
			TextView tvTitle = (TextView) popOverStyle3.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_title"));
			TextView tvContent = (TextView) popOverStyle3.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_content"));
			viewHolder = new PopOverViewHolder(this, tvTitle, tvContent, null);
			popOverStyle3.setTag(viewHolder);
		} else {
			viewHolder = (PopOverViewHolder) popOverStyle3.getTag();
		}
		if (mMapView.indexOfChild(popOverStyle3) == -1) {
			// 将popOverStyle3加入MapView但是隐藏显示
			mMapView.addView(popOverStyle3, new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
					MapView.LayoutParams.BOTTOM_CENTER));
			popOverStyle3.setVisibility(View.GONE);
		}
		viewHolder.updateData(markId, title, content, null);
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				markItem.getPoint(), 0, 0, MapView.LayoutParams.BOTTOM_CENTER);
		mMapView.updateViewLayout(popOverStyle3, params);
		popOverStyle3.startAnimation(popOverShowAnim);
		popOverStyle3.setVisibility(View.VISIBLE);
		popOverStyle3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (callback != null) {
							callback.onBubbleTouch(markId);
						}
					}
				});

			}
		});
	}

	public void showPopOverStyle4(final String markId, String title,
			String content, String imgUrl, String backgroundNor,
			String backgroundClicked, String titleFontColor,
			String contentFontColor, final BubbleViewTouchCallback callback) {
		// TODO
		MarkItem markItem = mMarkOverlays.getMarkItemById(markId);
		if (markItem == null) {
			return;
		}
		PopOverViewHolder viewHolder = null;
		if (popOverStyle4 == null) {
			popOverStyle4 = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_map_popover_style1"), null);
			ViewGroup group = (ViewGroup) popOverStyle4.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_root"));
			TextView tvTitle = (TextView) popOverStyle4.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_title"));
			TextView tvContent = (TextView) popOverStyle4.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_style1_content"));
			ImageView ivIcon = (ImageView) popOverStyle4.findViewById(EUExUtil
					.getResIdID("plugin_map_popover_styele1_image"));
			viewHolder = new PopOverViewHolder(this, tvTitle, tvContent,
					ivIcon, group);
			popOverStyle4.setTag(viewHolder);

		} else {
			viewHolder = (PopOverViewHolder) popOverStyle4.getTag();
		}

		if (mMapView.indexOfChild(popOverStyle4) == -1) {
			// 将popOverStyle1加入MapView但是隐藏显示
			mMapView.addView(popOverStyle4, new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
					MapView.LayoutParams.BOTTOM_CENTER));
			popOverStyle4.setVisibility(View.GONE);
		}

		Drawable norDrawable = null;
		try {
			String assetFileName = null;
			String path = backgroundNor;
			if (path.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
				assetFileName = BUtility.F_Widget_RES_path
						+ path.substring(BUtility.F_Widget_RES_SCHEMA.length());
			}
			norDrawable = NinePatchUtils.decodeDrawableFromAsset(
					BaiduMapLocationActivity.this, assetFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Drawable clickedDrawable = null;
		try {
			String assetFileName = null;
			String path = backgroundClicked;
			if (path.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
				assetFileName = BUtility.F_Widget_RES_path
						+ path.substring(BUtility.F_Widget_RES_SCHEMA.length());
			}
			clickedDrawable = NinePatchUtils.decodeDrawableFromAsset(
					BaiduMapLocationActivity.this, assetFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		viewHolder.updateData(markId, title, content, imgUrl, norDrawable,
				clickedDrawable, titleFontColor, contentFontColor);
		mMapView.updateViewLayout(popOverStyle4,
				new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, markItem.getPoint(), 0,
						-(int) (markItem.mImgHeight / density),
						MapView.LayoutParams.BOTTOM_CENTER));
		popOverStyle4.startAnimation(popOverShowAnim);
		popOverStyle4.setVisibility(View.VISIBLE);
		popOverStyle4.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (callback != null) {
							callback.onBubbleTouch(markId);
						}
					}
				});

			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}

}

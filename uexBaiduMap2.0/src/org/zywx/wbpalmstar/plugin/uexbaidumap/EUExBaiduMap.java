package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;

@SuppressWarnings("deprecation")
public class EUExBaiduMap extends EUExBase implements OnMapOccurErrorCallback {
	public static final String MAP_ACTIVITY_ID = "mapActivityID";
	public static final String TAG = "uexBaiduMap";
	public static final String ENABLE = "1";
	public static final String DISABLE = "0";
	public static final String CALLBACK_GET_CURRENT_LOCATION = "uexBaiduMap.cbGetCurrentLocation";
	public static final String CALLBACK_FAST_GET_LOCATION = "uexBaiduMap.cbFastGetLocation";
	public static final String CALLBACK_SHOW_USER_LOCATION = "uexBaiduMap.cbShowUserLocation";
	public static final String CALLBACK_SEARCH_SUGGESTION = "uexBaiduMap.cbSuggestionSearch";
	public static final String CALLBACK_POI_SEARCH_IN_CITY = "uexBaiduMap.cbPoiSearchInCity";
	public static final String CALLBACK_POI_SEARCH_AREA = "uexBaiduMap.cbPoiSearchArea";
	public static final String CALLBACK_POI_MULTI_SEARCH_AREA = "uexBaiduMap.cbPoiMultiSearchArea";
	public static final String CALLBACK_GEOCODE = "uexBaiduMap.cbGeocode";
	public static final String CALLBACK_REVERSE_GEOCODE = "uexBaiduMap.cbReverseGeocode";
	public static final String CALLBACK_SHOW_ROUTE_PLAN = "uexBaiduMap.cbShowRoutePlan";
	public static final String CALLBACK_BUS_LINE_SEARCH = "uexBaiduMap.cbBusLineSearch";
	public static final String ON_FUNCTION_TOUCH_MARK = "uexBaiduMap.onTouchMark";
	public static final String ON_FUNCTION_TOUCH_BUBBLE_VIEW = "uexBaiduMap.onTouchBubbleView";
	public static final String ON_FUNCTION_CLICK = "uexBaiduMap.onMapClick";
	public static final String ON_FUNCTION_MAP_MOVE = "uexBaiduMap.onMapMove";
	public static final String ON_FUNCTION_MAP_DRAG = "uexBaiduMap.onMapDrag";
	public static final String ON_FUNCTION_NETWORK_ERROR = "uexBaiduMap.onNetworkError";
	public static final String ON_FUNCTION_PERMISSION_DENIED = "uexBaiduMap.onPermissionDenied";
	public static final String CALLBACK_FROMGOOGLE = "uexBaiduMap.cbBaiduFromGoogle";
	public static final String JSON_KEY_LONGITUDE = "longitude";
	public static final String JSON_KEY_LATITUDE = "latitude";
	public static final String JSON_KEY_ID = "id";
	public static final String JSON_KEY_RESULT = "result";
	public static final String JS_HEADER_MAP_ON_DRAG = "javascript:if(uexBaiduMap.onMapDrag){uexBaiduMap.onMapDrag(";
	private BaiduMapLocationActivity mMapContext;
	private BaiduMapLocation mapLocation;
	public static int count = 0;

	public EUExBaiduMap(Context context, EBrowserView inParent) {
		super(context, inParent);
	}

	/**
	 * 打开百度地图<br>
	 * 实际形式open(String apiKey,int left,int top,int width,int height,double
	 * longitude,double latitude)
	 * 
	 * @param params
	 */
	public void open(String[] params) {
		if (params.length < 7 || null != mMapContext) {
			return;
		}
		String key = params[0];
		int left = 0;
		int top = 0;
		int width = 0;
		int height = 0;
		double latitude = 0;
		double longitude = 0;
		try {
			left = Integer.parseInt(params[1]);
			top = Integer.parseInt(params[2]);
			width = Integer.parseInt(params[3]);
			height = Integer.parseInt(params[4]);
			longitude = Double.parseDouble(params[5]);// 经度
			latitude = Double.parseDouble(params[6]);// 纬度
		} catch (Exception e) {
			errorCallback(0, 0, "传入参数错误");
			return;
		}

		openpBaiduMap(key, left, top, width, height, latitude, longitude);
	}

	private void openpBaiduMap(final String apiKey, final int x, final int y,
			final int width, final int height, final double latitude,
			final double longitude) {

		((ActivityGroup) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MyLog.i(TAG, "openBaiduMap" + apiKey + " " + x + " " + y + " "
						+ width + " " + height + " " + latitude + " "
						+ longitude);
				
				LocalActivityManager mgr = ((ActivityGroup) mContext)
						.getLocalActivityManager();
				
				Activity activity = mgr.getActivity(MAP_ACTIVITY_ID);

				if (activity != null) {
					
					return;
				}
				Intent intent = new Intent(mContext,
						BaiduMapLocationActivity.class);
				intent.putExtra(BaiduMapLocationActivity.INTENT_KEY_APIKEY,
						apiKey);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				count++;
				Window window = mgr.startActivity(
						MAP_ACTIVITY_ID, intent);
				mMapContext = (BaiduMapLocationActivity) window.getContext();
				View mMapDecorView = window.getDecorView();
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						width, height);
				lp.leftMargin = x;
				lp.topMargin = y;
				addViewToCurrentWindow(mMapDecorView, lp);
				mMapContext.setCenter(longitude, latitude);
				mMapContext.setErrorCallback(EUExBaiduMap.this);
				mMapContext.setOnMapDragListener(new OnMapDragListener() {

					@Override
					public void onMapDrag(EnhanceMapView mapView, int state,
							GeoPoint point) {
						double log = point.getLongitudeE6() / 1E6;
						double lat = point.getLatitudeE6() / 1E6;
						String js = SCRIPT_HEADER + "if("
								+ ON_FUNCTION_MAP_DRAG + "){"
								+ ON_FUNCTION_MAP_DRAG + "(" + state + ","
								+ lat + "," + log + ");}";
						onCallback(js);
					}
				});
			}

		});
	}

	/**
	 * 添加标注,实际形式<br>
	 * addMark(String jsonStr)
	 * 
	 * @param params
	 */
	public void addMark(String[] params) {
		Log.i("fzy", "uexAddMark()----->");
		final String inParams = params[0];
		if (params.length == 1) {
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						String jsonString = inParams;
						ArrayList<MarkItem> markList = MapUtillity
								.parseMarkJson(jsonString);
						if (markList == null) {
							errorCallback(0, 0, "Json数据错误");
							return;
						}
						mMapContext.addMark(markList, new OverlayTapCallback() {

							@Override
							public boolean onOverlayTaped(String overlayId,
									GeoPoint point) {
								double latitude = point.getLatitudeE6() / 1E6;// 纬度
								double longitude = point.getLongitudeE6() / 1E6;// 经度
								String js = SCRIPT_HEADER + "if("
										+ ON_FUNCTION_TOUCH_MARK + "){"
										+ ON_FUNCTION_TOUCH_MARK + "("
										+ overlayId + "," + latitude + ","
										+ longitude + ");}";
								onCallback(js);
								return true;
							}
						});
					}
				}
			});
		}
	}

	// TODO
	/**
	 * 添加标注新样式,实际形式<br>
	 * addMark(String jsonStr)
	 * 
	 * @param params
	 */
	public void addMarkNewStyle(String[] params) {
		Log.i("fzy", "addMarkNewStyle()----->");
		final String inParams = params[0];
		if (params.length == 1) {
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						String jsonString = inParams;
						ArrayList<MarkItem> markList = MapUtillity
								.parseMarkNewStyleJson(jsonString);
						if (markList == null) {
							errorCallback(0, 0, "Json数据错误");
							return;
						}
						mMapContext.addMarkNewStyle(markList,
								new OverlayTapCallback() {

									@Override
									public boolean onOverlayTaped(
											String overlayId, GeoPoint point) {
										double latitude = point.getLatitudeE6() / 1E6;// 纬度
										double longitude = point
												.getLongitudeE6() / 1E6;// 经度
										String js = SCRIPT_HEADER + "if("
												+ ON_FUNCTION_TOUCH_MARK + "){"
												+ ON_FUNCTION_TOUCH_MARK + "("
												+ overlayId + "," + latitude
												+ "," + longitude + ");}";
										onCallback(js);
										return true;
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 更新之前添加进来的mark<br>
	 * 实际形式updateMark(String json);
	 * 
	 * @param params
	 */
	public void updateMark(String[] params) {
		if (params.length == 1) {
			String jsonString = params[0];
			WWidgetData wd = mBrwView.getCurrentWidget();
			String baseUrl = mBrwView.getCurrentUrl();
			String wgtUrl = wd.getWidgetPath();
			final MarkItem markItem = MapUtillity.parseUpdateMark(jsonString,
					baseUrl, wgtUrl, wd.m_wgtType);
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.updateMark(markItem);
					}
				}
			});
		}
	}

	/**
	 * 添加LineOverlay<br>
	 * 实际形式addLineOverlay(String jsonString)
	 * 
	 * @param params
	 */
	public void addLineOverLayer(String[] params) {
		if (params.length == 1) {
			String jsonString = params[0];
			final LineInfo lineInfo = MapUtillity.parseLineInfoJson(jsonString);
			if (lineInfo == null) {
				errorCallback(0, 0, "JSON数据错误!");
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.addLineOverlay(lineInfo);
					}
				}

			});

		}
	}

	/**
	 * 添加CircleOverlay<br>
	 * 实际形式addCircleOverLayer(String json)
	 * 
	 * @param params
	 */
	public void addCircleOverLayer(String[] params) {
		if (params.length == 1) {
			String jsonString = params[0];
			final CircleInfo circleInfo = MapUtillity
					.parseCircleInfoJson(jsonString);
			if (circleInfo == null) {
				errorCallback(0, 0, "JSON数据错误!");
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.addCircleOverlay(circleInfo);
					}
				}
			});
		}
	}

	/**
	 * 添加多边形Overlay<br>
	 * 实际形式addPolygonOverLayer(String json);
	 * 
	 * @param params
	 */
	public void addPolygonOverLayer(String[] params) {
		if (params.length == 1) {
			String jsonString = params[0];
			final PolygonInfo polygonInfo = MapUtillity
					.parasePolygonInfo(jsonString);
			if (polygonInfo == null) {
				errorCallback(0, 0, "Json数据错误");
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.addPolygonOverlay(polygonInfo);
					}
				}
			});
		}
	}

	/**
	 * 清除Mark<br>
	 * 实际形式clearMarks(String[] ids)
	 * 
	 * @param params
	 */
	public void clearMarks(String[] params) {
		final String inParams = params.length == 0 ? null : params[0];
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					if (inParams == null) {
						mMapContext.clearMarks(null);
					} else {
						mMapContext.clearMarks(inParams.split(","));
					}
				}
			}
		});
	}

	/**
	 * 清除AreaMark<br>
	 * clearAreaMarks(String[] ids)
	 * 
	 * @param params
	 */
	public void clearAreaMarks(final String[] params) {
		final String inParams = params.length == 0 ? null : params[0];
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					if (inParams == null) {
						mMapContext.clearAreaMark(null);
					} else {
						mMapContext.clearAreaMark(inParams.split(","));
					}
				}
			}
		});
	}

	/**
	 * 清除指定ID的Overlay<br>
	 * 实际形式clearOverLayers(String[] ids) 如果参数为空的话，则清除所有的Overlay
	 * 
	 * @param params
	 */
	public void clearOverLayers(String[] params) {
		final String inParams = params.length == 0 ? null : params[0];
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					if (inParams == null) {
						mMapContext.clearOverLayers(null);
					} else {
						String[] overLayerIds = inParams.split(",");
						mMapContext.clearOverLayers(overLayerIds);
					}
				}
			}
		});
	}

	/**
	 * 设置地图中心点<br>
	 * 实际形式:setCenter();
	 * 
	 * @param params
	 */
	public void setCenter(String[] params) {
		MyLog.i(TAG, "setCenter");
		if (params.length == 2) {
			try {
				final double longitude = (float) Double.parseDouble(params[0]);
				final double latitude = Double.parseDouble(params[1]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.setCenter(longitude, latitude);
						}
					}
				});
			} catch (Exception e) {
				errorCallback(0, 0, "参数错误");
				e.printStackTrace();
			}
		}

	}

	/**
	 * 设置缩放级别<br>
	 * 实际形式:setZoomLevel(int level);
	 * 
	 * @param params
	 */
	public void setZoomLevel(String[] params) {
		if (params.length == 1) {
			try {
				final int zoomLevel = Integer.parseInt(params[0]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.setZoomLevel(zoomLevel);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 放大一个级别<br>
	 * 实际形式:zoomIn()
	 * 
	 * @param params
	 */
	public void zoomIn(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.zoomIn();
				}
			}
		});
	}

	/**
	 * 缩小一个级别<br>
	 * 实际形式:zoomOut()
	 * 
	 * @param params
	 */
	public void zoomOut(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.zoomOut();
				}
			}
		});
	}

	/**
	 * 缩放到指定经纬度跨度<br>
	 * 实际形式 zoomToSpan(String longitudeSpan,String latitudeSpan)
	 * 
	 * @param params
	 */
	public void zoomToSpan(String[] params) {
		if (params.length == 2) {
			try {
				final float longitudeSpan = Float.parseFloat(params[0]);
				final float latitudeSpan = Float.parseFloat(params[1]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.zoomToSpan(longitudeSpan, latitudeSpan);
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 设置地图显示类型
	 * 
	 * @param params
	 */
	public void setType(String[] params) {
		if (params.length == 1) {
			String typeString = params[0];
			try {
				final int type = Integer.parseInt(typeString);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.setType(type);
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 隐藏地图<br>
	 * hide()
	 * 
	 * @param params
	 */
	public void hide(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.hideMap(true);
				}
			}
		});
	}

	/**
	 * 显示地图<br>
	 * 实际形式show()
	 * 
	 * @param params
	 */
	public void show(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.showMap();
				}
			}
		});

	}

	/**
	 * 快速获取当前位置
	 * 
	 * @param params
	 */
	public void fastGetLocation(String[] params) {
		MyLog.i(TAG, "fastGetCurrentLocation");
		if (params.length != 1) {
			return;
		}
		final String apiKey = params[0];
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mapLocation == null) {
					mapLocation = new BaiduMapLocation(mContext, apiKey);
				}
				mapLocation.requestLocation(apiKey, new LocationCallback() {

					@Override
					public void onLocationed(double longitude, double latitude) {
						MyLog.i(TAG, "fastGetCurrentLocation--onLocationed");
						JSONObject json = new JSONObject();
						try {
							String longitudeStr = String.format("%.6f",
									longitude);
							String latitudeStr = String
									.format("%.6f", latitude);
							json.put(JSON_KEY_LONGITUDE, longitudeStr);
							json.put(JSON_KEY_LATITUDE, latitudeStr);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						jsCallback(CALLBACK_FAST_GET_LOCATION, 0,
								EUExCallback.F_C_JSON, json.toString());
					}
				});
			}
		});
	}

	public void getCurrentLocation(String[] params) {
		MyLog.i(TAG, "getCurrentLocation");
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.enableLocation(new LocationCallback() {

						@Override
						public void onLocationed(double longitude,
								double latitude) {
							MyLog.i(TAG, "getCurrentLocation---onLocationed");
							JSONObject json = new JSONObject();
							try {
								String longitudeStr = String.format("%.6f",
										longitude);
								String latitudeStr = String.format("%.6f",
										latitude);
								BDebug.i(TAG, "longitudeStr:" + longitudeStr
										+ "  latitudeStr:" + latitudeStr);
								json.put(JSON_KEY_LONGITUDE, longitudeStr);
								json.put(JSON_KEY_LATITUDE, latitudeStr);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							jsCallback(CALLBACK_GET_CURRENT_LOCATION, 0,
									EUExCallback.F_C_JSON, json.toString());
						}

					});
				}
			}
		});

	}

	/**
	 * 在地图上添加一个标注，显示用户位置，并且会不断定位回调用户最新位置<br>
	 * showUserLocation()
	 * 
	 * @param params
	 */
	public void showUserLocation(String[] params) {
		MyLog.i(TAG, "showUserLocation");
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.showUserLocation(new LocationCallback() {

						@Override
						public void onLocationed(double longitude,
								double latitude) {
							MyLog.i(TAG, "showUserLocation--onLocationed");
							JSONObject json = new JSONObject();
							try {
								json.put(
										JSON_KEY_LONGITUDE,
										new BigDecimal(longitude).setScale(6,
												BigDecimal.ROUND_HALF_UP)
												.toPlainString());
								json.put(
										JSON_KEY_LATITUDE,
										new BigDecimal(latitude).setScale(6,
												BigDecimal.ROUND_HALF_UP)
												.toPlainString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							jsCallback(CALLBACK_SHOW_USER_LOCATION, 0,
									EUExCallback.F_C_JSON, json.toString());
						}

					});
				}
			}
		});
	}

	/**
	 * 隐藏用户位置<br>
	 * hideUserLocation()
	 * 
	 * @param params
	 */
	public void hideUserLocation(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.hideUserLocation();
				}
			}
		});
	}

	public void setTrackBeginImage(final String[] params) {
		if (params.length == 1) {
			final String inParams = params[0];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						String bgPath = inParams;
						if (null != bgPath) {
							WWidgetData wd = mBrwView.getCurrentWidget();
							if (bgPath.contains("://")) {
								bgPath = BUtility.makeRealPath(bgPath,
										wd.m_widgetPath, wd.m_wgtType);
							} else {
								bgPath = BUtility.makeUrl(
										mBrwView.getCurrentUrl(), bgPath);
								bgPath = BUtility.makeRealPath(bgPath,
										wd.m_widgetPath, wd.m_wgtType);
							}
						}
						Bitmap map = getBitmap(bgPath);
						if (null != map) {
							mMapContext
									.setBeginDrawable(new BitmapDrawable(map));
						}
					}
				}
			});
		}
	}

	public void setTrackEndImage(String[] params) {
		if (params.length == 1) {
			final String inParams = params[0];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						String bgPath = inParams;
						if (null != bgPath) {
							WWidgetData wd = mBrwView.getCurrentWidget();
							if (bgPath.contains("://")) {
								bgPath = BUtility.makeRealPath(bgPath,
										wd.m_widgetPath, wd.m_wgtType);
							} else {
								bgPath = BUtility.makeUrl(
										mBrwView.getCurrentUrl(), bgPath);
								bgPath = BUtility.makeRealPath(bgPath,
										wd.m_widgetPath, wd.m_wgtType);
							}
						}
						Bitmap map = ImageColorUtils.getImage(mMapContext,
								bgPath);
						if (null != map) {
							mMapContext.setEndDrawable(new BitmapDrawable(map));
						}
					}
				}
			});
		}
	}

	public void setTrackColor(String[] params) {
		if (params.length == 1) {
			final int color = BUtility.parseColor(params[0]);
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.setPathColor(color);
					}
				}
			});
		}
	}

	/**
	 * 添加轨迹
	 * 
	 * @param params
	 */
	public void addOneTrack(String[] params) {
		if (params.length == 2) {
			try {
				final double longitude = Double.parseDouble(params[0]);
				final double latitude = Double.parseDouble(params[1]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.addTrack(longitude, latitude);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void clearAllTrack(String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mMapContext != null) {
					mMapContext.clearTrack();
				}
			}
		});

	}

	@Override
	protected boolean clean() {
		MyLog.i(TAG, "auto-clean");
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (null != mMapContext) {
					View mMapDecorView = mMapContext.getWindow().getDecorView();
					removeViewFromCurrentWindow(mMapDecorView);
					mMapContext.clean();
					mMapContext = null;
					destroyActivity();
//					LocalActivityManager mgr = ((ActivityGroup) mContext)
//							.getLocalActivityManager();
//					mgr.destroyActivity(TAG + EUExBaiduMap.this.hashCode(),
//							true);
				}
			}
		});

		return true;
	}
	
	public void destroyActivity() {
		sendMessageWithType(0, null);
	}
	
	private void sendMessageWithType(int msgType, String[] params) {
		Message msg = new Message();
		msg.what = msgType;
		msg.obj = this;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onHandleMessage(Message msg) {
		LocalActivityManager mgr = ((ActivityGroup) mContext)
				.getLocalActivityManager();
		Activity activity = mgr.getActivity(MAP_ACTIVITY_ID);

		if (activity != null) {
			
			if (0 == msg.what) {
				mgr.destroyActivity(MAP_ACTIVITY_ID,
						true);
			}
		}
		
	}

	public void clean(String[] params) {
		MyLog.i(TAG, "Handler clean");
		if (null != mMapContext) {
			View mMapDecorView = mMapContext.getWindow().getDecorView();
			removeViewFromCurrentWindow(mMapDecorView);
			mMapContext.clean();
			mMapContext = null;
			LocalActivityManager mgr = ((ActivityGroup) mContext)
					.getLocalActivityManager();
			mgr.destroyActivity(TAG + EUExBaiduMap.this.hashCode(), true);
		}

	}

	/**
	 * 联想检索 实际形式suggestionSearch(String key)
	 * 
	 * @param params
	 */
	public void suggestionSearch(String[] params) {
		if (params.length == 1) {
			final String key = params[0];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.suggestionSearch(key,
								new DefaultMKSearchCallback() {

									@Override
									public void onGetSuggestionResult(
											MKSuggestionResult suggestionResult,
											int iError) {
										if (iError != 0
												|| suggestionResult == null) {
											return;
										}
										String jsonString = MapUtillity
												.suggestionResult2JsonString(suggestionResult);
										jsCallback(CALLBACK_SEARCH_SUGGESTION,
												0, EUExCallback.F_C_JSON,
												jsonString);
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 根据关键字在指定的城市中搜索poi兴趣点 <br>
	 * 实际形式poiSearchInCity(String city,String key,int pageIndex)
	 * 
	 * @param params
	 */
	public void poiSearchInCity(String[] params) {
		if (params.length == 3) {
			final String city = params[0];
			final String key = params[1];
			try {
				final int pageIndex = Integer.parseInt(params[2]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.poiSearchInCity(city, key, pageIndex,
									new DefaultMKSearchCallback() {

										@Override
										public void onGetPoiResult(
												MKPoiResult mkPoiResult,
												int type, int iError) {
											if (iError != 0
													|| mkPoiResult == null
													|| type != MKSearch.TYPE_POI_LIST) {
												errorCallback(0, 0,
														"搜索城市poi结果失败!");
												return;
											}
											String searchInCityjsonString = MapUtillity
													.poiSearchResult2JsonString(mkPoiResult);
											jsCallback(
													CALLBACK_POI_SEARCH_IN_CITY,
													0, EUExCallback.F_C_JSON,
													searchInCityjsonString);
										}

									});
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据关键字在指定中心点和半径范围内搜索poi兴趣点<br>
	 * 实际形式:poiSearchNearBy(String key,double longitude,double latitude,int
	 * radius,int pageIndex)
	 * 
	 * @param params
	 */
	public void poiSearchNearBy(String[] params) {
		if (params.length == 5) {
			final String key = params[0];
			try {
				final double longitude = Double.parseDouble(params[1]);
				final double latitude = Double.parseDouble(params[2]);
				final int radius = Integer.parseInt(params[3]);
				final int pageIndex = Integer.parseInt(params[4]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.poiSearchNearBy(key, new GeoPoint(
									(int) (latitude * 1E6),
									(int) (longitude * 1E6)), radius,
									pageIndex, new DefaultMKSearchCallback() {

										@Override
										public void onGetPoiResult(
												MKPoiResult poiResult,
												int type, int iError) {
											if (iError != 0
													|| poiResult == null
													|| type != MKSearch.TYPE_AREA_POI_LIST) {
												errorCallback(0, 0, "搜索附近poi失败");
												return;
											}
											String searchAreaJsonString = MapUtillity
													.poiSearchResult2JsonString(poiResult);
											jsCallback(
													CALLBACK_POI_SEARCH_AREA,
													0, EUExCallback.F_C_JSON,
													searchAreaJsonString);
										}
									});
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据多个关键字在指定的中心点和半径范围内搜索poi兴趣点<br>
	 * 实际形式poiMultiSearchNearBy(String[] key,double longitude,double
	 * latitude,int radius,int pageIndex)
	 * 
	 * @param params
	 */
	public void poiMultiSearchNearBy(String[] params) {
		if (params.length == 5) {
			// String str = params[0].substring(1, params[0].length()-1);
			final String[] key = params[0].split(",");
			if (key == null || key.length < 1 || key.length > 10) {
				return;
			}
			try {
				final double longitude = Double.parseDouble(params[1]);
				final double latitude = Double.parseDouble(params[2]);
				final int radius = Integer.parseInt(params[3]);
				final int pageIndex = Integer.parseInt(params[4]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.poiMultiSearchNearBy(key, new GeoPoint(
									(int) (latitude * 1E6),
									(int) (longitude * 1E6)), radius,
									pageIndex, new DefaultMKSearchCallback() {

										@Override
										public void onGetPoiResult(
												MKPoiResult poiResult,
												int type, int iError) {
											if (poiResult == null
													|| iError != 0
													|| type != MKSearch.TYPE_AREA_MULTI_POI_LIST) {
												errorCallback(0, 0, "搜索附近poi失败");
												return;
											}
											String multiSearchAreaJsonString = MapUtillity
													.poiSearchResult2JsonString(poiResult);
											jsCallback(
													CALLBACK_POI_MULTI_SEARCH_AREA,
													0, EUExCallback.F_C_JSON,
													multiSearchAreaJsonString);
										}
									});
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据关键字在指定经纬度范围内搜索poi兴趣点<br>
	 * 实际形式：poiSearchInBounds(String key,double ltLongitude,double
	 * ltLatitude,rbLongitude,rbLatitude,int pageIndex)
	 * 
	 * @param params
	 */
	public void poiSearchInBounds(String[] params) {
		if (params.length == 6) {
			final String key = params[0];
			try {
				final double ltLongitude = Double.parseDouble(params[1]);
				final double ltLatitude = Double.parseDouble(params[2]);
				final double rbLongitude = Double.parseDouble(params[3]);
				final double rbLatitude = Double.parseDouble(params[4]);
				final int pageIndex = Integer.parseInt(params[5]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.poiSearchInBounds(key, new GeoPoint(
									(int) (ltLatitude * 1E6),
									(int) (ltLongitude * 1E6)), new GeoPoint(
									(int) (rbLatitude * 1E6),
									(int) (rbLongitude * 1E6)), pageIndex,
									new DefaultMKSearchCallback() {
										@Override
										public void onGetPoiResult(
												MKPoiResult poiResult,
												int type, int iError) {
											if (iError != 0
													|| poiResult == null
													|| type != MKSearch.TYPE_AREA_POI_LIST) {
												errorCallback(0, 0, "搜索poi失败");
												return;
											}
											String searchAreaJsonString = MapUtillity
													.poiSearchResult2JsonString(poiResult);
											jsCallback(
													CALLBACK_POI_SEARCH_AREA,
													0, EUExCallback.F_C_JSON,
													searchAreaJsonString);
										}
									});
						}
					}
				});

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据多个关键字在指定经纬度范围内搜索poi兴趣点<br>
	 * 实际形式：poiMultiSearchInBounds(String[] key,double ltLongitude,double
	 * ltLatitude,rbLongitude,rbLatitude,int pageIndex)
	 * 
	 * @param params
	 */
	public void poiMultiSearchInBounds(String[] params) {
		if (params.length == 6) {
			// String str = params[0].substring(1, params[0].length()-1);
			final String[] key = params[0].split(",");
			try {
				final double ltLongitude = Double.parseDouble(params[1]);
				final double ltLatitude = Double.parseDouble(params[2]);
				final double rbLongitude = Double.parseDouble(params[3]);
				final double rbLatitude = Double.parseDouble(params[4]);
				final int pageIndex = Integer.parseInt(params[5]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.poiMultiSearchInBounds(key,
									new GeoPoint((int) (ltLatitude * 1E6),
											(int) (ltLongitude * 1E6)),
									new GeoPoint((int) (rbLatitude * 1E6),
											(int) (rbLongitude * 1E6)),
									pageIndex, new DefaultMKSearchCallback() {

										@Override
										public void onGetPoiResult(
												MKPoiResult poiResult,
												int type, int iError) {
											if (poiResult == null
													|| iError != 0
													|| type != MKSearch.TYPE_AREA_MULTI_POI_LIST) {
												errorCallback(0, 0, "搜索poi失败");
												return;
											}
											String multiSearchAreaJsonString = MapUtillity
													.poiSearchResult2JsonString(poiResult);
											jsCallback(
													CALLBACK_POI_MULTI_SEARCH_AREA,
													0, EUExCallback.F_C_JSON,
													multiSearchAreaJsonString);
										}

									});
						}
					}
				});

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据地址信息获取编码信息<br>
	 * 实际形式:geocode(String city,String address)
	 * 
	 * @param params
	 */
	public void geocode(String[] params) {
		if (params.length == 2) {
			final String city = params[0];
			final String address = params[1];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.geocode(city, address,
								new DefaultMKSearchCallback() {
									@Override
									public void onGetAddrResult(
											MKAddrInfo addrInfo, int iError) {
										if (addrInfo == null
												|| iError != 0
												|| addrInfo.type != MKAddrInfo.MK_GEOCODE) {
											errorCallback(0, 0, "获取地理编码信息失败");
											return;
										}
										String geocodeJsonString = MapUtillity
												.geocodeMkAddrInfo2JsonString(addrInfo);
										jsCallback(CALLBACK_GEOCODE, 0,
												EUExCallback.F_C_JSON,
												geocodeJsonString);
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 根据经纬度获取地址信息<br>
	 * 实际形式:reverseGeocode(double longitude,double latitude)
	 * 
	 * @param params
	 */
	public void reverseGeocode(String[] params) {
		if (params.length == 2) {
			try {
				final double longitude = Double.parseDouble(params[0]);
				final double latitude = Double.parseDouble(params[1]);
				MyLog.i(TAG, "reverseGeocode " + longitude + " " + latitude);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.reverseGeocode(new GeoPoint(
									(int) (latitude * 1E6),
									(int) (longitude * 1E6)),
									new DefaultMKSearchCallback() {
										@Override
										public void onGetAddrResult(
												MKAddrInfo addrInfo, int iError) {
											if (addrInfo == null
													|| iError != 0
													|| addrInfo.type != MKAddrInfo.MK_REVERSEGEOCODE) {
												errorCallback(0, 0,
														"获取反地理编码信息失败");
												return;
											}
											String reverseGeocodeJsonString = MapUtillity
													.reverseGeocodeMkAddrInfo2JsonString(addrInfo);
											jsCallback(
													CALLBACK_REVERSE_GEOCODE,
													0, 0,
													reverseGeocodeJsonString);
										}
									});
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置poi搜索每页数量<br>
	 * 实际形式:setpoiPageCapacity(int pageCapacity)
	 * 
	 * @param params
	 */
	public void setPoiPageCapacity(String[] params) {
		if (params.length == 1) {
			try {
				final int pageCapacity = Integer.parseInt(params[0]);
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.setPoiPageCapacity(pageCapacity);
						}
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置是否允许地图手动缩放<br>
	 * 实际形式setZommEnable(String enable)
	 * 
	 * @param params
	 */
	public void setZoomEnable(String[] params) {
		if (params.length == 1) {
			final String enable = params[0];

			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						if (ENABLE.equals(enable)) {
							mMapContext.setZoomEnable(true);
						} else if (DISABLE.equals(enable)) {
							mMapContext.setZoomEnable(false);
						}
					}
				}
			});
		}
	}

	/**
	 * 设置是否允许地图手动滚动<br>
	 * 实际形式setScrollEnable(boolean enable);
	 * 
	 * @param params
	 */
	public void setScrollEnable(String[] params) {
		if (params.length == 1) {
			final String enable = params[0];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						if (ENABLE.equals(enable)) {
							mMapContext.setScrollEnable(true);
						} else if (DISABLE.equals(enable)) {
							mMapContext.setScrollEnable(false);
						}
					}
				}
			});
		}
	}

	/**
	 * 搜索公交<br>
	 * 实际形式busLineSearch(String city,String lineUid);
	 * 
	 * @param params
	 */
	public void busLineSearch(String[] params) {
		if (params.length == 2) {
			final String city = params[0];
			final String lineUid = params[1];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.busLineSearch(city, lineUid,
								new MKSearchListener() {
									@Override
									public void onGetBusDetailResult(
											MKBusLineResult busLineResult,
											int iError) {
										if (busLineResult == null
												|| iError != 0) {
											errorCallback(0, 0, "搜索公交线路失败");
											return;
										}
										mMapContext
												.busLineResultSearch(busLineResult);
										String mkBusLineResultJsonString = MapUtillity
												.mkBusLineResult2JsonString(busLineResult);
										jsCallback(CALLBACK_BUS_LINE_SEARCH, 0,
												EUExCallback.F_C_JSON,
												mkBusLineResultJsonString);
									}

									@Override
									public void onGetAddrResult(
											MKAddrInfo arg0, int arg1) {
									}

									@Override
									public void onGetDrivingRouteResult(
											MKDrivingRouteResult arg0, int arg1) {
									}

									@Override
									public void onGetPoiDetailSearchResult(
											int arg0, int arg1) {
									}

									@Override
									public void onGetPoiResult(MKPoiResult res,
											int type, int error) {
										if (error != 0 || res == null) {
											errorCallback(0, 0, "搜索公交线路失败");
											return;
										}
										MKPoiInfo curPoi = null;
										int totalPoiNum = res.getNumPois();
										for (int idx = 0; idx < totalPoiNum; idx++) {
											Log.d("busline", "the busline is "
													+ idx);
											curPoi = res.getPoi(idx);
											if (2 == curPoi.ePoiType) {
												break;
											}
										}
										mMapContext.busLineSearch(city,
												curPoi.uid);
									}

									@Override
									public void onGetRGCShareUrlResult(
											String arg0, int arg1) {
									}

									@Override
									public void onGetSuggestionResult(
											MKSuggestionResult arg0, int arg1) {
									}

									@Override
									public void onGetTransitRouteResult(
											MKTransitRouteResult arg0, int arg1) {
									}

									@Override
									public void onGetWalkingRouteResult(
											MKWalkingRouteResult arg0, int arg1) {
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 搜索公交线路<br>
	 * 
	 * @param params
	 */
	public void transitSearch(String[] params) {
		if (params.length == 1) {
			final RoutePlanInfo routePlanInfo = MapUtillity
					.paraseRoutePlanInfo(params[0]);
			if (routePlanInfo == null) {
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.transitSearch(routePlanInfo,
								new DefaultMKSearchCallback() {
									@Override
									public void onGetDrivingRouteResult(
											MKDrivingRouteResult drivingRouteResult,
											int iError) {

									}
								});
					}
				}
			});
		}
	}

	/**
	 * 搜索步行线路<br>
	 * 
	 * @param params
	 */
	public void walkingSearch(String[] params) {
		if (params.length == 1) {
			final RoutePlanInfo routePlanInfo = MapUtillity
					.paraseRoutePlanInfo(params[0]);
			if (routePlanInfo == null) {
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.walkingSearch(routePlanInfo,
								new DefaultMKSearchCallback() {
									@Override
									public void onGetWalkingRouteResult(
											MKWalkingRouteResult walkingRouteResult,
											int iError) {

									}
								});
					}
				}
			});
		}
	}

	public void drivingSearch(String[] params) {
		if (params.length == 1) {
			final RoutePlanInfo routePlanInfo = MapUtillity
					.paraseRoutePlanInfo(params[0]);
			if (routePlanInfo == null) {
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.drivingSearch(routePlanInfo,
								new DefaultMKSearchCallback() {
									@Override
									public void onGetDrivingRouteResult(
											MKDrivingRouteResult drivingRouteResult,
											int iError) {

									}
								});
					}
				}
			});
		}
	}

	public void showRoutePlan(String[] params) {
		if (params.length == 1) {
			final RoutePlanInfo routePlanInfo = MapUtillity
					.paraseRoutePlanInfo(params[0]);
			if (routePlanInfo == null) {
				return;
			}
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.showRoutePlan(routePlanInfo,
								new RoutePlanInfo.OnRoutePlanCallback() {

									@Override
									public void onRoutePlanResultOk(
											boolean success) {
										JSONObject json = new JSONObject();
										try {
											json.put(
													JSON_KEY_RESULT,
													success ? EUExCallback.F_C_SUCCESS
															: EUExCallback.F_C_FAILED);
											json.put(JSON_KEY_ID,
													routePlanInfo.getId());
											jsCallback(
													CALLBACK_SHOW_ROUTE_PLAN,
													0, EUExCallback.F_C_JSON,
													json.toString());
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 根据ID数组清除RoutePlan，不传则全部清除
	 * 
	 * @param params
	 */
	public void clearRoutePlan(String[] params) {
		final String inParams = params.length == 0 ? null : params[0];
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mMapContext != null) {
					if (inParams == null) {
						mMapContext.clearRoutePlan(null);
					} else {
						mMapContext.clearRoutePlan(inParams.split(","));
					}
				}
			}
		});
	}

	/**
	 * 显示左边有图片，右边2行文字的弹出窗口，背景浅灰色<br>
	 * 实际形式showBubbleView1(String markId,String imgUrl,String title,String
	 * content)
	 * 
	 * @param params
	 */
	public void showBubbleView1(String[] params) {
		if (params.length == 4) {
			final String markId = params[0];
			String imgUrl = params[1];
			final String title = params[2];
			final String content = params[3];
			String baseUrl = mBrwView.getCurrentUrl();
			String wgtUrl = mBrwView.getCurrentWidget().getWidgetPath();
			imgUrl = MapUtillity.makeFullPath(imgUrl, baseUrl, wgtUrl,
					mBrwView.getCurrentWidget().m_wgtType);
			final String finalPath = imgUrl;
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.showPopOverStyle1(markId, title, content,
								finalPath, new BubbleViewTouchCallback() {

									@Override
									public void onBubbleTouch(String id) {
										String js = SCRIPT_HEADER + "if("
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "){"
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "(" + id + ");}";
										onCallback(js);
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 显示2行文字，背景浅灰色<br>
	 * 实际形式showBubbleView1(String markId,String title,String content)
	 * 
	 * @param params
	 */
	public void showBubbleView2(String[] params) {
		if (params.length == 3) {
			final String markId = params[0];
			final String title = params[1];
			final String content = params[2];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.showPopOverStyle2(markId, title, content,
								new BubbleViewTouchCallback() {

									@Override
									public void onBubbleTouch(String id) {
										String js = SCRIPT_HEADER + "if("
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "){"
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "(" + id + ");}";
										onCallback(js);
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 显示2行文字，背景深黑色<br>
	 * 实际形式showBubbleView3(String markId,String title,String content)
	 * 
	 * @param params
	 */
	public void showBubbleView3(String[] params) {
		if (params.length == 3) {
			final String markId = params[0];
			final String title = params[1];
			final String content = params[2];
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mMapContext != null) {
						mMapContext.showPopOverStyle3(markId, title, content,
								new BubbleViewTouchCallback() {

									@Override
									public void onBubbleTouch(String id) {
										String js = SCRIPT_HEADER + "if("
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "){"
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "(" + id + ");}";
										onCallback(js);
									}
								});
					}
				}
			});
		}
	}

	/**
	 * 显示左边有图片，右边2行文字的弹出窗口，背景自定义<br>
	 * 实际形式showBubbleView4(String markId,String imgUrl,String title,String
	 * content)
	 * 
	 * @param params
	 */
	public void showBubbleView4(final String[] params) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (params.length >= 6) {
					final String markId = params[0];
					String imgUrl = params[1];
					final String title = params[2];
					final String content = params[3];
					final String backgroundNor = params[4];
					final String backgroundClicked = params[5];
					final String titleFontColor = params[6];
					final String contentFontColor = params[7];
					String baseUrl = mBrwView.getCurrentUrl();
					String wgtUrl = mBrwView.getCurrentWidget().getWidgetPath();
					imgUrl = MapUtillity.makeFullPath(imgUrl, baseUrl, wgtUrl,
							mBrwView.getCurrentWidget().m_wgtType);
					final String finalPath = imgUrl;

					if (mMapContext != null) {
						mMapContext.showPopOverStyle4(markId, title, content,
								finalPath, backgroundNor, backgroundClicked,
								titleFontColor, contentFontColor,
								new BubbleViewTouchCallback() {

									@Override
									public void onBubbleTouch(String id) {
										String js = SCRIPT_HEADER + "if("
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "){"
												+ ON_FUNCTION_TOUCH_BUBBLE_VIEW
												+ "(" + id + ");}";
										onCallback(js);
									}
								});
					}
				}

			}
		});
	}

	/**
	 * 添加覆盖一块区域的标注<br>
	 * addAreaMark(String json)
	 * 
	 * @param params
	 */
	public void addAreaMark(String[] params) {
		if (params.length == 1) {
			final List<AreaMarkInfo> list = MapUtillity
					.parseAreaMarkInfoList(params[0]);
			if (list != null && list.size() > 0) {
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.addAreaMark(list);
						}
					}
				});
			}
		}
	}

	/**
	 * 更新AreaMark的信息<br>
	 * updateAreaMark(String json)
	 * 
	 * @param params
	 */
	public void updateAreaMark(String[] params) {
		if (params.length == 1) {
			final AreaMarkInfo areaMarkInfo = MapUtillity
					.parseAreaMarkInfoJson(params[0]);
			if (areaMarkInfo != null) {
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mMapContext != null) {
							mMapContext.updateAreaMark(areaMarkInfo);
						}
					}
				});
			}
		}
	}

	@Override
	public void onNetworkError() {
		BDebug.e(TAG, "onNetworkError()----------->");
		String js = SCRIPT_HEADER + "if(" + ON_FUNCTION_NETWORK_ERROR + "){"
				+ ON_FUNCTION_NETWORK_ERROR + "();}";
		onCallback(js);
	}

	@Override
	public void onPermissionDenied() {
		BDebug.e(TAG, "onPermissionDenied()----------->");
		String js = SCRIPT_HEADER + "if(" + ON_FUNCTION_PERMISSION_DENIED
				+ "){" + ON_FUNCTION_PERMISSION_DENIED + "();}";
		onCallback(js);
	}

	/*** google坐标转换为百度 ***/
	public void getBaiduFromGoogle(final String[] params) {
		MyLog.i(TAG, "getBaiduFromGoogle");
		if (params.length == 2) {
			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Double x = 0.0;
					Double y = 0.0;
					try {
						x = Double.parseDouble(params[1]);
						y = Double.parseDouble(params[0]);
					} catch (Exception e) {
						// TODO: handle exception
					}
					Map<String, Object> map = BaiduMapUtils.googleToBaidu(x, y);
					if (map != null) {
						String longitude = (String) map.get("y");
						String latitude = (String) map.get("x");
						String js = SCRIPT_HEADER + "if(" + CALLBACK_FROMGOOGLE
								+ "){" + CALLBACK_FROMGOOGLE + "('" + longitude
								+ "','" + latitude + "');}";
						Log.e("===", js);
						onCallback(js);
					}
				}
			});
		}
	}
}

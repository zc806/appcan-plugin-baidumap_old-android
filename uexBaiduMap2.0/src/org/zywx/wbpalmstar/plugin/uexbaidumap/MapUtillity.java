package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MKSuggestionInfo;
import com.baidu.mapapi.MKSuggestionResult;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.URLUtil;

public class MapUtillity {

	public static final String TAG = "MapUtillity";

	public static final String KEY_MARK_ID = "id";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";

	public static final String KEY_STAR = "star";
	public static final String KEY_IMAGE_URL = "imageUrl";
	public static final String KEY_IMAGE_WIDTH = "imageWidth";
	public static final String KEY_IMAGE_HEIGHT = "imageHeight";
	public static final String KEY_MESSAGE = "message";
	public static final String MARK_LIST_TAG = "markInfo";
	public static final String MARK_BACKGROUND = "background";
	public static final String MARK_FONTCOLOR = "fontcolor";
	public static final String MARK_TITLEFONTCOLOR = "titlefontcolor";
	public static final String MARK_CONTENTFONTCOLOR = "contentfontcolor";

	public static final String KEY_FILL_COLOR = "fillColor";
	public static final String KEY_STROKE_COLOR = "strokeColor";
	public static final String KEY_LINE_WIDTH = "lineWidth";
	public static final String KEY_LIST = "list";
	public static final String KEY_RADIUS = "radius";
	public static final String KEY_PROPERTY = "property";
	public static final String KEY_CITY = "city";
	public static final String KEY_SUGGESTION = "suggestion";
	// poi搜索结果总页数
	public static final String KEY_PAGE_NUM = "pageNum";
	// poi搜索结果当前页的poi数量
	public static final String KEY_CURRENT_POIS_NUM = "currPoiNum";
	// poi搜索结果的总结果数
	public static final String KEY_TOTAL_POI_NUM = "totalPoiNum";
	// poi搜索结果的总结果数
	public static final String KEY_PAGE_INDEX = "pageIndex";

	public static final String KEY_ADDRESS = "address";
	public static final String KEY_EPOI_TYPE = "ePoiType";
	public static final String KEY_NAME = "name";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_POST_CODE = "postCode";
	public static final String KEY_UID = "uid";

	public static final String KEY_ADDR = "addr";
	public static final String KEY_BUSINESS = "business";

	public static final String KEY_START = "start";
	public static final String KEY_END = "end";
	public static final String KEY_TYPE = "type";

	public static final String KEY_DISTRICT = "district";
	public static final String KEY_PROVINCE = "province";
	public static final String KEY_STREET_NAME = "streetName";
	public static final String KEY_STREET_NUMBER = "streetNumber";

	public static final String KEY_BUS_NAME = "busName";
	public static final String KEY_COMPANY = "company";
	public static final String KEY_START_TIME = "startTime";
	public static final String KEY_END_TIME = "endTime";
	public static final String KEY_SETP_INFO = "stepInfo";
	public static final String KEY_TITLE = "title";

	public static final String KEY_LT_LONGITUDE = "ltLongitude";
	public static final String KEY_LT_LATITUDE = "ltLatitude";
	public static final String KEY_RB_LONGITUDE = "rbLongitude";
	public static final String KEY_RB_LATITUDE = "rbLatitude";

	public static byte[] loadDataFromNetwork(String path) {
		if (path == null) {
			throw new NullPointerException("NullPointer");
		}
		if (!URLUtil.isNetworkUrl(path)) {
			return null;
		}
		int resCode = -1;
		InputStream is = null;
		byte[] data = null;
		ByteArrayOutputStream baos = null;
		try {
			HttpGet httpGet = new HttpGet(path);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpGet);
			resCode = response.getStatusLine().getStatusCode();
			if (resCode == HttpURLConnection.HTTP_OK) {
				baos = new ByteArrayOutputStream(4096);
				is = response.getEntity().getContent();
				byte[] buffer = new byte[4096];
				int actulSize = 0;
				while ((actulSize = is.read(buffer)) != -1) {
					baos.write(buffer, 0, actulSize);
				}
				data = baos.toByteArray();
				baos.close();
			}
		} catch (IOException e) {
			BDebug.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			BDebug.e(TAG, "OutOfMemoryError:" + error.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static ArrayList<MarkItem> parseMarkJson(String json) {
		ArrayList<MarkItem> itemArray = null;
		try {
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray(MARK_LIST_TAG);
			int size = array.length();
			itemArray = new ArrayList<MarkItem>(size);
			for (int i = 0; i < size; i++) {
				JSONObject item = array.optJSONObject(i);
				MarkItem markItem = parseMarkItemJson(item);
				if (markItem != null) {
					itemArray.add(markItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemArray;
	}

	public static MarkItem parseMarkItemJson(JSONObject json) {
		MarkItem markItem = null;
		try {
			String msg = json.optString(KEY_MESSAGE);
			String markUrl = json.getString(KEY_IMAGE_URL);
			int w = json.getInt(KEY_IMAGE_WIDTH);
			int h = json.getInt(KEY_IMAGE_HEIGHT);
			String id = json.getString(KEY_MARK_ID);
			double latitude = json.getDouble(KEY_LATITUDE);
			double longitude = json.getDouble(KEY_LONGITUDE);
			GeoPoint gPoint = new GeoPoint((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			markItem = new MarkItem(gPoint, null, msg);
			markItem.mDrawableUrl = markUrl;
			markItem.mId = id;
			markItem.mImgWidth = w;
			markItem.mImgHeight = h;
		} catch (JSONException e) {
			e.printStackTrace();
			markItem = null;
		}
		return markItem;
	}

	public static ArrayList<MarkItem> parseMarkNewStyleJson(String json) {
		ArrayList<MarkItem> itemArray = null;
		try {
			JSONObject object = new JSONObject(json);
			String background = object.optString(MARK_BACKGROUND);
			String fontcolor = object.optString(MARK_FONTCOLOR);
			int fcolor = BUtility.parseColor(fontcolor);

			JSONArray array = object.getJSONArray(MARK_LIST_TAG);
			int size = array.length();
			itemArray = new ArrayList<MarkItem>(size);
			for (int i = 0; i < size; i++) {
				JSONObject item = array.optJSONObject(i);
				MarkItem markItem = parseMarkItemNewStyleJson(item);
				markItem.mDrawableUrl = background;
				markItem.fontColor = fcolor;
				if (markItem != null) {
					itemArray.add(markItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemArray;
	}

	public static MarkItem parseMarkItemNewStyleJson(JSONObject json) {
		MarkItem markItem = null;
		try {
			String msg = json.optString(KEY_MESSAGE);
			String title = json.getString(KEY_TITLE);
			String star = json.getString(KEY_STAR);
			int w = json.getInt(KEY_IMAGE_WIDTH);
			int h = json.getInt(KEY_IMAGE_HEIGHT);
			String id = json.getString(KEY_MARK_ID);
			double latitude = json.getDouble(KEY_LATITUDE);
			double longitude = json.getDouble(KEY_LONGITUDE);
			GeoPoint gPoint = new GeoPoint((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			markItem = new MarkItem(gPoint, null, msg);
			markItem.mId = id;
			markItem.mImgWidth = w;
			markItem.mImgHeight = h;
			markItem.title = title;
			markItem.star = star;
		} catch (JSONException e) {
			e.printStackTrace();
			markItem = null;
		}
		return markItem;
	}

	public static MarkItem parseUpdateMark(String msg, String baseUrl,
			String widgetPath, int widgetType) {
		MarkItem markItem = null;
		try {
			JSONObject jsonObject = new JSONObject(msg);
			markItem = parseMarkItemJson(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return markItem;
	}

	/**
	 * 解析LineOverlay信息
	 * 
	 * @param msg
	 * @return
	 */
	public static LineInfo parseLineInfoJson(String msg) {
		if (msg == null || msg.length() == 0) {
			return null;
		}
		LineInfo lineInfo = null;
		try {
			JSONObject json = new JSONObject(msg);
			lineInfo = new LineInfo();
			lineInfo.setId(json.getString(KEY_MARK_ID));
			String fillColorString = json.getString(KEY_FILL_COLOR);
			lineInfo.setFillColor(BUtility.parseColor(fillColorString));
			String strokeColorString = json.getString(KEY_STROKE_COLOR);
			lineInfo.setStrokeColor(BUtility.parseColor(strokeColorString));
			lineInfo.setLineWidth(json.getInt(KEY_LINE_WIDTH));
			JSONArray array = json.getJSONArray(KEY_PROPERTY);
			for (int i = 0, size = array.length(); i < size; i++) {
				JSONObject item = array.getJSONObject(i);
				double longitude = item.getDouble(KEY_LONGITUDE);
				double latitude = item.getDouble(KEY_LATITUDE);
				GeoPoint geoPoint = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));
				lineInfo.addGeoPoint(geoPoint);
			}
		} catch (JSONException e) {
			lineInfo = null;
			BDebug.e(TAG, "parseLineInfoJson() ERROR:" + e.getMessage());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			lineInfo = null;
			BDebug.e(TAG, "parseLineInfoJson() ERROR:" + e.getMessage());
			e.printStackTrace();
		}
		return lineInfo;
	}

	/**
	 * 解析CircleInfo信息
	 * 
	 * @param msg
	 * @return
	 */
	public static CircleInfo parseCircleInfoJson(String msg) {
		if (msg == null || msg.length() == 0) {
			return null;
		}
		CircleInfo circleInfo = null;
		try {
			JSONObject json = new JSONObject(msg);
			circleInfo = new CircleInfo();
			circleInfo.setId(json.getString(KEY_MARK_ID));
			String fillColorString = json.getString(KEY_FILL_COLOR);
			circleInfo.setFillColor(BUtility.parseColor(fillColorString));
			String strokeColorString = json.getString(KEY_STROKE_COLOR);
			circleInfo.setStrokeColor(BUtility.parseColor(strokeColorString));
			circleInfo.setLineWidth(json.getInt(KEY_LINE_WIDTH));
			circleInfo.setRadius(json.getInt(KEY_RADIUS));
			double longitude = json.getDouble(KEY_LONGITUDE);
			double latitude = json.getDouble(KEY_LATITUDE);
			GeoPoint geoPoint = new GeoPoint((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			circleInfo.setCenterPoint(geoPoint);
		} catch (JSONException e) {
			circleInfo = null;
			BDebug.e(TAG, "parseCircleInfoJson() ERROR:" + e.getMessage());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			BDebug.e(TAG, "parseCircleInfoJson() ERROR:" + e.getMessage());
			e.printStackTrace();
		}
		return circleInfo;
	}

	/**
	 * 解析多边形信息
	 * 
	 * @param msg
	 * @return
	 */
	public static PolygonInfo parasePolygonInfo(String msg) {
		if (msg == null || msg.length() == 0) {
			return null;
		}
		PolygonInfo polygonInfo = null;
		try {
			JSONObject json = new JSONObject(msg);
			polygonInfo = new PolygonInfo();
			polygonInfo.setId(json.getString(KEY_MARK_ID));
			String fillColorString = json.getString(KEY_FILL_COLOR);
			polygonInfo.setFillColor(BUtility.parseColor(fillColorString));
			String strokeColorString = json.getString(KEY_STROKE_COLOR);
			polygonInfo.setStrokeColor(BUtility.parseColor(strokeColorString));
			polygonInfo.setLineWidth(json.getInt(KEY_LINE_WIDTH));
			JSONArray array = json.getJSONArray(KEY_PROPERTY);
			for (int i = 0, size = array.length(); i < size; i++) {
				JSONObject item = array.getJSONObject(i);
				double longitude = item.getDouble(KEY_LONGITUDE);
				double latitude = item.getDouble(KEY_LATITUDE);
				GeoPoint geoPoint = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));
				polygonInfo.addGeoPoint(geoPoint);
			}
		} catch (JSONException e) {
			BDebug.e(TAG, "parasePolygonInfo() ERROR:" + e.getMessage());
			e.printStackTrace();
			polygonInfo = null;
		}
		return polygonInfo;
	}

	public static RoutePlanInfo paraseRoutePlanInfo(String msg) {
		RoutePlanInfo routePlanInfo = null;
		try {
			JSONObject json = new JSONObject(msg);
			routePlanInfo = new RoutePlanInfo();
			routePlanInfo.setId(json.getString(KEY_MARK_ID));
			routePlanInfo.setType(json.getInt(KEY_TYPE));
			JSONObject startJson = json.getJSONObject(KEY_START);
			routePlanInfo.setStartCity(startJson.optString(KEY_CITY));
			routePlanInfo.setStartNode(parseMKPlanNode(startJson));
			JSONObject endJson = json.getJSONObject(KEY_END);
			routePlanInfo.setEndCity(endJson.optString(KEY_CITY));
			routePlanInfo.setEndNode(parseMKPlanNode(endJson));
		} catch (JSONException e) {
			routePlanInfo = null;
			e.printStackTrace();
		}
		return routePlanInfo;
	}

	private static MKPlanNode parseMKPlanNode(JSONObject json) {
		MKPlanNode planNode = new MKPlanNode();
		planNode.name = json.optString(KEY_NAME);
		String longitudeStr = json.optString(KEY_LONGITUDE, null);
		String latitudeStr = json.optString(KEY_LATITUDE, null);
		if (longitudeStr != null && latitudeStr != null) {
			try {
				float longitude = Float.parseFloat(longitudeStr);
				float latitude = Float.parseFloat(latitudeStr);
				GeoPoint gp = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));
				planNode.pt = gp;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return planNode;
	}

	public static String suggestionResult2JsonString(MKSuggestionResult result) {
		if (result == null) {
			return "";
		}
		ArrayList<MKSuggestionInfo> list = result.getAllSuggestions();
		JSONArray jsonArray = new JSONArray();
		try {
			for (MKSuggestionInfo info : list) {
				JSONObject item = new JSONObject();
				item.put(KEY_CITY, info.city);
				item.put(KEY_SUGGESTION, info.key);
				jsonArray.put(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonArray.toString();
	}

	public static String poiSearchResult2JsonString(MKPoiResult result) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_PAGE_NUM, result.getNumPages());
			json.put(KEY_CURRENT_POIS_NUM, result.getCurrentNumPois());
			json.put(KEY_TOTAL_POI_NUM, result.getNumPois());
			json.put(KEY_PAGE_INDEX, result.getPageIndex());
			ArrayList<MKPoiInfo> arrayList = result.getAllPoi();
			JSONArray array = new JSONArray();
			if (arrayList != null) {
				for (MKPoiInfo mkPoiInfo : arrayList) {
					array.put(mkPoiInfo2Json(mkPoiInfo));
				}
			}
			json.put(KEY_LIST, array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 将MKPoiInfo转换为Json对象
	 * 
	 * @param mkPoiInfo
	 * @return
	 */
	public static JSONObject mkPoiInfo2Json(MKPoiInfo mkPoiInfo) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_ADDRESS, mkPoiInfo.address);
			json.put(KEY_CITY, mkPoiInfo.city);
			json.put(KEY_EPOI_TYPE, mkPoiInfo.ePoiType);
			json.put(KEY_NAME, mkPoiInfo.name);
			json.put(KEY_PHONE, mkPoiInfo.phoneNum);
			json.put(KEY_POST_CODE, mkPoiInfo.postCode);
			json.put(KEY_LONGITUDE,
					((float) mkPoiInfo.pt.getLongitudeE6()) / 1E6);
			json.put(KEY_LATITUDE, ((float) mkPoiInfo.pt.getLatitudeE6()) / 1E6);
			json.put(KEY_UID, mkPoiInfo.uid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 将MKAddrInfo(geocode时)转化为json对象
	 * 
	 * @param addrInfo
	 * @return
	 */
	public static String geocodeMkAddrInfo2JsonString(MKAddrInfo addrInfo) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_LONGITUDE,
					((float) addrInfo.geoPt.getLongitudeE6()) / 1E6);
			json.put(KEY_LATITUDE,
					((float) addrInfo.geoPt.getLatitudeE6()) / 1E6);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 将MKAddrInfo(reverseGeocode时)转化为json对象字符串
	 * 
	 * @param addrInfo
	 * @return
	 */
	public static String reverseGeocodeMkAddrInfo2JsonString(MKAddrInfo addrInfo) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_LONGITUDE,
					((float) addrInfo.geoPt.getLongitudeE6()) / 1E6);
			json.put(KEY_LATITUDE,
					((float) addrInfo.geoPt.getLatitudeE6()) / 1E6);
			json.put(KEY_ADDRESS, addrInfo.strAddr);
			if (addrInfo.type == MKAddrInfo.MK_REVERSEGEOCODE) {
				MKGeocoderAddressComponent addressComponent = addrInfo.addressComponents;
				if (addressComponent != null) {
					json.put(KEY_CITY, addressComponent.city);
					json.put(KEY_DISTRICT, addressComponent.district);
					json.put(KEY_PROVINCE, addressComponent.province);
					json.put(KEY_STREET_NAME, addressComponent.street);
					json.put(KEY_STREET_NUMBER, addressComponent.streetNumber);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 将MKGeocoderAddressComponent转化为Json对象
	 * 
	 * @param addressComponent
	 * @return
	 */
	public static JSONObject mkGeocoderAddressComponent2Json(
			MKGeocoderAddressComponent addressComponent) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_CITY, addressComponent.city);
			json.put(KEY_DISTRICT, addressComponent.district);
			json.put(KEY_PROVINCE, addressComponent.province);
			json.put(KEY_STREET_NAME, addressComponent.street);
			json.put(KEY_STREET_NUMBER, addressComponent.streetNumber);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static String mkBusLineResult2JsonString(
			MKBusLineResult busLineResult) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_BUS_NAME, busLineResult.getBusName());
			json.put(KEY_COMPANY, busLineResult.getBusCompany());
			json.put(KEY_START_TIME, busLineResult.getStartTime());
			json.put(KEY_END_TIME, busLineResult.getEndTime());
			MKRoute mkRoute = busLineResult.getBusRoute();
			JSONArray jsonArray = new JSONArray();
			for (int i = 0, size = mkRoute.getNumSteps(); i < size; i++) {
				MKStep mkStep = mkRoute.getStep(i);
				jsonArray.put(mkStepInfo2Json(mkStep));
			}
			json.put(KEY_SETP_INFO, jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 将MKStep对象转化为json对象
	 * 
	 * @param mkStep
	 * @return
	 */
	public static JSONObject mkStepInfo2Json(MKStep mkStep) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_LONGITUDE,
					((float) mkStep.getPoint().getLongitudeE6()) / 1E6);
			json.put(KEY_LATITUDE,
					((float) mkStep.getPoint().getLatitudeE6()) / 1E6);
			json.put(KEY_TITLE, mkStep.getContent());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static List<AreaMarkInfo> parseAreaMarkInfoList(String msg) {
		List<AreaMarkInfo> list = null;
		try {
			JSONObject json = new JSONObject(msg);
			list = new ArrayList<AreaMarkInfo>();
			JSONArray array = json.getJSONArray(MARK_LIST_TAG);
			for (int i = 0, size = array.length(); i < size; i++) {
				JSONObject item = array.getJSONObject(i);
				AreaMarkInfo areaMarkInfo = parseAreaMarkInfo(item);
				if (areaMarkInfo != null) {
					list.add(areaMarkInfo);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 解析AreaMarkInfo
	 * 
	 * @param json
	 * @return
	 */
	public static AreaMarkInfo parseAreaMarkInfo(JSONObject json) {
		AreaMarkInfo areaMarkInfo = null;
		try {
			areaMarkInfo = new AreaMarkInfo();
			areaMarkInfo.setId(json.getString(KEY_MARK_ID));
			String imageUrl = json.optString(KEY_IMAGE_URL);
			areaMarkInfo.setImgUrl(imageUrl);
			double ltLongitude = json.getDouble(KEY_LT_LONGITUDE);
			double ltLatitude = json.getDouble(KEY_LT_LATITUDE);
			double rbLongitude = json.getDouble(KEY_RB_LONGITUDE);
			double rbLatitude = json.getDouble(KEY_RB_LATITUDE);
			areaMarkInfo.setLtPoint(new GeoPoint((int) (ltLatitude * 1E6),
					(int) (ltLongitude * 1E6)));
			areaMarkInfo.setRbPoint(new GeoPoint((int) (rbLatitude * 1E6),
					(int) (rbLongitude * 1E6)));
		} catch (JSONException e) {
			e.printStackTrace();
			areaMarkInfo = null;
		}
		return areaMarkInfo;
	}

	public static AreaMarkInfo parseAreaMarkInfoJson(String msg) {
		AreaMarkInfo areaMarkInfo = null;
		try {
			JSONObject json = new JSONObject(msg);
			areaMarkInfo = parseAreaMarkInfo(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return areaMarkInfo;
	}

	public static Bitmap downloadImageFromNetwork(String url) {
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
			HttpConnectionParams.setSoTimeout(httpParams, 30000);
			HttpResponse httpResponse = new DefaultHttpClient(httpParams)
					.execute(httpGet);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				is = httpResponse.getEntity().getContent();
				byte[] data = transStreamToBytes(is, 4096);
				if (data != null) {
					bitmap = BitmapFactory
							.decodeByteArray(data, 0, data.length);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static byte[] transStreamToBytes(InputStream is, int buffSize) {
		if (is == null) {
			return null;
		}
		if (buffSize <= 0) {
			throw new IllegalArgumentException(
					"buffSize can not less than zero.....");
		}
		byte[] data = null;
		byte[] buffer = new byte[buffSize];
		int actualSize = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ((actualSize = is.read(buffer)) != -1) {
				baos.write(buffer, 0, actualSize);
			}
			data = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static String getActionString(MotionEvent event) {
		String action = "";
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			action = "ACTION_DOWN";
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			action = "ACTION_POINTER_DOWN";
			break;
		case MotionEvent.ACTION_MOVE:
			action = "ACTION_MOVE";
			break;
		case MotionEvent.ACTION_POINTER_UP:
			action = "ACTION_POINTER_UP";
			break;
		case MotionEvent.ACTION_UP:
			action = "ACTION_UP";
			break;
		}
		return action;
	}

	public static String makeFullPath(String markUrl, String baseUrl,
			String widgetPath, int widgetType) {
		String fullPath = markUrl;
		if (fullPath.contains("://")) {
			fullPath = BUtility.makeRealPath(markUrl, widgetPath, widgetType);
		} else {
			fullPath = BUtility.makeUrl(baseUrl, markUrl);
			fullPath = BUtility.makeRealPath(markUrl, widgetPath, widgetType);
		}
		return fullPath;
	}

	public static Bitmap getImage(Context ctx, String imgUrl) {
		Log.i("fzy", "getImage():" + imgUrl);
		if (imgUrl == null || imgUrl.length() == 0) {
			return null;
		}
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			if (URLUtil.isNetworkUrl(imgUrl)) {
				bitmap = MapUtillity.downloadImageFromNetwork(imgUrl);
			} else {
				if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
					is = BUtility.getInputStreamByResPath(ctx, imgUrl);
					bitmap = BitmapFactory.decodeStream(is);
				} else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
					imgUrl = imgUrl.replace(BUtility.F_FILE_SCHEMA, "");
					bitmap = BitmapFactory.decodeFile(imgUrl);
				} else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
					try {
						is = ctx.getAssets().open(imgUrl);
						if (is != null) {
							bitmap = BitmapFactory.decodeStream(is);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					bitmap = BitmapFactory.decodeFile(imgUrl);
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally {
			Log.i("fzy", "is:" + is);
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i("fzy", "bitmap:" + bitmap);
		return bitmap;
	}

	// /**
	// * 修正GPS定位
	// *
	// * @param gpsPoint
	// * @return
	// */
	//
	// public static GeoPoint fixedGps(GeoPoint gpsPoint) {
	// GeoPoint fixedPoint =
	// CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(gpsPoint));
	// BDebug.i(TAG, "gps:(" + gpsPoint.getLongitudeE6() + " : " +
	// gpsPoint.getLatitudeE6() + ")  " + "   fixed:("
	// + fixedPoint.getLongitudeE6() + " : " + fixedPoint.getLatitudeE6() +
	// ")");
	// return fixedPoint;
	// }

}

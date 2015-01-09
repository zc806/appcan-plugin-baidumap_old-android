package org.zywx.wbpalmstar.plugin.uexbaidumap;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import android.util.Log;

public class BaiduMapUtils {
	
	public static final String googleToBaiduUrl = "http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=#x#&y=#y#";
	
		public static String reverseGeocode(String url) {
			try {
				HttpGet httpGet = new HttpGet(url);
				BasicHttpParams httpParams = new BasicHttpParams();
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParams);
				HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
				int responseCode = httpResponse.getStatusLine().getStatusCode();
				if (responseCode == HttpStatus.SC_OK) {
					String charSet = EntityUtils.getContentCharSet(httpResponse.getEntity());
					if (null == charSet) {
						charSet = "UTF-8";
					}
					String str = new String(EntityUtils.toByteArray(httpResponse.getEntity()), charSet);
					defaultHttpClient.getConnectionManager().shutdown();
					Log.i("-----------","str = " + str);
					return str;
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	/**
	 * 转换谷歌为百度坐标
	 * @param x 经度
	 * @param y	纬度
	 * @return 获取转换过的百度坐标
	 */
	public static Map<String, Object> googleToBaidu(final Double x, final Double y){
		final Map<String, Object> map = new HashMap<String, Object>();
		String relt = reverseGeocode(getGoogleToBaiduUrl(String.valueOf(x), String.valueOf(y)));
		if(!TextUtils.isEmpty(relt)){
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(relt);
				String xjson = jsonObj.optString("x");
				String yjson = jsonObj.optString("y");
				map.put("x", new String(Base64.decode(xjson)));
				map.put("y", new String(Base64.decode(yjson)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}  
		return map;
	}
	
	/**
	 * 获取请求地址
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static String getGoogleToBaiduUrl(String x, String y){
		return googleToBaiduUrl.replace("#x#", x).replace("#y#", y);
	}
	/**
	 * URL中文字符编码
	 * @param url
	 * @return
	 */
	public static String encode(String s){
		return URLEncoder.encode(s);
	}
}


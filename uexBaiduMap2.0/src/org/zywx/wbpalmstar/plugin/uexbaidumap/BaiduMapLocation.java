package org.zywx.wbpalmstar.plugin.uexbaidumap;

import android.content.Context;
import android.location.Location;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKLocationManager;

public class BaiduMapLocation implements MKGeneralListener, LocationListener {

	private Context context;
	private String apiKey;
	private BMapManager mapManager = null;
	private LocationCallback callback;
	public static final String TAG = "BaiduMapLocation";

	public BaiduMapLocation(Context context, String apiKey) {
		this.context = context;
		this.apiKey = apiKey;
	}

	private void ensureInit() {
		if (mapManager == null) {
			mapManager = new BMapManager(context.getApplicationContext());
			mapManager.init(apiKey, this);
			mapManager.start();
		}
	}

	public void requestLocation(String apiKey, LocationCallback callback) {
		ensureInit();
		enableLocation(callback);
	}

	public void enableLocation(LocationCallback callback) {
		this.callback = callback;
		mapManager.stop();
		MKLocationManager mgr = mapManager.getLocationManager();
		mgr.requestLocationUpdates(this);
		mgr.enableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		mgr.enableProvider(MKLocationManager.MK_GPS_PROVIDER);
		mgr.setNotifyInternal(60, 5);
		mapManager.start();
	}

	private void disableLocation() {
		MKLocationManager mgr = mapManager.getLocationManager();
		mgr.removeUpdates(this);
		mgr.disableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		mgr.disableProvider(MKLocationManager.MK_GPS_PROVIDER);
		mapManager.stop();
		callback = null;
	}

	@Override
	public void onGetNetworkState(int arg0) {

	}

	@Override
	public void onGetPermissionState(int arg0) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			if (callback != null) {
				float accuracy = location.getAccuracy();
				if (accuracy <= 300) {
					double longitude = location.getLongitude();
					double latitude = location.getLatitude();
					GeoPoint gpsPoint = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
					callback.onLocationed(((double) gpsPoint.getLongitudeE6()) / 1E6,
							((double) gpsPoint.getLatitudeE6()) / 1E6);
					disableLocation();
				}
			}
		}
	}

}

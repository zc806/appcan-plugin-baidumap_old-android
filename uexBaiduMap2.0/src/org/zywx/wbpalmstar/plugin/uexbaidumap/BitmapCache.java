package org.zywx.wbpalmstar.plugin.uexbaidumap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class BitmapCache {

	public HashMap<String, Bitmap> mBitmapCache;
	public ArrayList<BitmapTask> mTaskList;

	public BitmapCache() {
		mBitmapCache = new HashMap<String, Bitmap>();
		mTaskList = new ArrayList<BitmapCache.BitmapTask>();
	}

	public void clear() {
		for (BitmapTask task : mTaskList) {
			task.stop();
		}
		mTaskList.clear();
	}

	public void destroy() {
		clear();
		for (Map.Entry<String, Bitmap> entry : mBitmapCache.entrySet()) {
			Bitmap temp = entry.getValue();
			if (null != temp) {
				temp.recycle();
			}
		}
		mBitmapCache.clear();
	}

	protected Bitmap getImage(String url) {

		Bitmap result = mBitmapCache.get(url);
		if (null != result) {
			return result;
		}
		return null;
	}

	protected Bitmap getImage(MarkItem item, Context ctx) {
		String mapPath = item.mDrawableUrl;
		Log.i(EUExBaiduMap.TAG, "getImage()  url: " + item.mDrawableUrl);
		Bitmap result = mBitmapCache.get(mapPath);
		if (null != result) {
			Drawable d = new BitmapDrawable(result);
			item.setMarker(d);
			Log.i(EUExBaiduMap.TAG, "getImage() haveCache: "+d);
			return result;
		}
		InputStream in = null;
		try {
			if (null != mapPath && 0 != mapPath.length()) {
				if (mapPath.startsWith("/sdcard")) {
					File file = new File(mapPath);
					in = new FileInputStream(file);
				} else if (mapPath.startsWith("widget/")) {
					AssetManager asm = ctx.getAssets();
					in = asm.open(mapPath);
					Log.i(EUExBaiduMap.TAG, "getImage()  openAsset: " + in);
				} else if (mapPath.startsWith("/data/data")) {
					;
				} else if (mapPath.startsWith("http")) {
					BitmapTask task = new BitmapTask(item, ctx);
					mTaskList.add(task);
					task.execute();
					return null;
				}
				if (null != in) {
					result = BitmapFactory.decodeStream(in);
					Log.i(EUExBaiduMap.TAG, "getImage()  decodeBitmap: " + result);
					in.close();
					result = Bitmap.createScaledBitmap(result, item.mImgWidth, item.mImgHeight, false);
					mBitmapCache.put(mapPath, result);
					Drawable d = new BitmapDrawable(result);
					item.setMarker(d);
					Log.i(EUExBaiduMap.TAG, "getImage()  setMark: " + d);
					return result;
				}
			}
		} catch (Exception e) {
			try {
				in.close();
			} catch (Exception e1) {
				;
			}
		}
		return null;
	}

	class BitmapTask extends AsyncTask<String, String, Bitmap> {

		public MarkItem mMarkItem;
		public HttpGet mHttpGet;
		public HttpClient mHttpClient;
		public Context mContext;

		public BitmapTask(MarkItem item, Context ctx) {
			mMarkItem = item;
			mContext = ctx;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap result = null;
			try {
				mHttpGet = new HttpGet(mMarkItem.mDrawableUrl);
				mHttpClient = new DefaultHttpClient();
				HttpResponse response = mHttpClient.execute(mHttpGet);
				int code = response.getStatusLine().getStatusCode();
				if (200 == code) {
					InputStream is = response.getEntity().getContent();
					result = BitmapFactory.decodeStream(is);
					result = Bitmap.createScaledBitmap(result, mMarkItem.mImgWidth, mMarkItem.mImgHeight, false);
					mBitmapCache.put(mMarkItem.mDrawableUrl, result);
					is.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != mHttpGet) {
					mHttpGet.abort();
				}
				if (null != mHttpClient) {
					mHttpClient.getConnectionManager().shutdown();
				}
			}
			return result;
		}

		public void stop() {
			if (null != mHttpGet) {
				mHttpGet.abort();
			}
			if (null != mHttpClient) {
				mHttpClient.getConnectionManager().shutdown();
			}
			super.cancel(true);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (null != result) {
				Drawable d = new BitmapDrawable(result);
				mMarkItem.setMarker(d);
				if (mContext != null) {
					((BaiduMapLocationActivity) mContext).invlidate();
				}
			}
		}
	}
}

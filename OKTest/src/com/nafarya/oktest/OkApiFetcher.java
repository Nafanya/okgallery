package com.nafarya.oktest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import android.util.Log;

public class OkApiFetcher {
	
	private static final String TAG = "OkApiFetcher";
	
	private static final String REQUEST_GET = "get"; 
	public static final String METHOD_GET_PHOTOS = "photos.getPhotos";
	public static final String METHOD_GET_FRIENDS = "friends.get";
	public static final String METHOD_GET_CURRENT_USER = "users.getCurrentUser";
	public static final String METHOD_GET_STREAM = "stream.get";
	
	private static final String PHOTO_SMALL = "pic50x50";
	private static final String PHOTO_MEDIUM = "pic128x128";
	private static final String PHOTO_LARGE = "pic640x480";
	
	private static Odnoklassniki mOdnoklassniki = null;
	
	public OkApiFetcher(Odnoklassniki ok) {
		mOdnoklassniki = ok;
	}
	
	public ArrayList<PhotoItem> fetchPhotos() {
		ArrayList<PhotoItem> items = new ArrayList<PhotoItem>();
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("count", "100");
			String response = mOdnoklassniki.request(METHOD_GET_PHOTOS, params, REQUEST_GET);
			JSONObject json = new JSONObject(response); 
			JSONArray photos = json.getJSONArray("photos");
			for (int i = 0; i < photos.length(); i++) {
				try {
					JSONObject photo = photos.getJSONObject(i);
					String urlSmall = photo.getString(PHOTO_SMALL);
					String urlMedium = photo.getString(PHOTO_MEDIUM);
					String urlLarge = photo.getString(PHOTO_LARGE);
					
					//Log.i(TAG, "Parsed: " + urlSmall + "\n" + urlMedium + "\n" + urlLarge);
					
					PhotoItem item = new PhotoItem();
					item.setUrlSmall(urlSmall);
					item.setUrlMedium(urlMedium);
					item.setUrlLarge(urlLarge);
					
					items.add(item);
					
				} catch (JSONException e) {
					Log.e(TAG, "JSON error: " + e.toString());
				}
			}
		} catch (IOException ioe) {
			Log.e(TAG, "Failed fetch photos");
			//ioe.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, "JSON parse error");
			//e.printStackTrace();
		}
		return items;
	}
	
	public String request(String method) {
		try {
			String result = mOdnoklassniki.request(method, null, REQUEST_GET);
			//Log.i(TAG, "Fetched response: " + result);
			return result;
		} catch (IOException e) {
			Log.e(TAG, "Request error");
			e.printStackTrace();
		}
		return null;
	}
	
	public String request(String method, Map<String, String> params) {
		try {
			String result = mOdnoklassniki.request(method, params, REQUEST_GET);
			//Log.i(TAG, "Fetched respnse: " + result);
			return result;
		} catch (IOException e) {
			Log.e(TAG, "Request error");
			e.printStackTrace();
		}
		return null;
	}

}

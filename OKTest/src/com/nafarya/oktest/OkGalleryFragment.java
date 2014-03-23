package com.nafarya.oktest;

import java.util.ArrayList;
import java.util.HashMap;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.util.OkScope;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class OkGalleryFragment extends Fragment {
	
	final static String TAG = "OkGalleryFragment";
	
	private final String PERMISSIONS = OkScope.VALUABLE_ACCESS.concat(";").concat(OkScope.PHOTO_CONTENT);
	
	private Context mContext;
	protected Odnoklassniki mOdnoklassniki;
	private OkApiFetcher okApiFetcher;
	
	//Button mButtonGetPhotos;
	GridView mGridView;
	ArrayList<PhotoItem> mPhotoItems;
	ThumbnailDownloader<ImageView> mThumbnailThread;
	
	private LruCache<String, Bitmap> mMemoryCache;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
        mOdnoklassniki = Odnoklassniki.getInstance(mContext);
        
        if (mOdnoklassniki.hasAccessToken()) {
        	Log.i(TAG, "Has access token from store" + mOdnoklassniki.getCurrentAccessToken());
        } else {
            mOdnoklassniki.requestAuthorization(getActivity(), false, PERMISSIONS);
        	Log.i(TAG, "Hasn't access token");
        }
        
		okApiFetcher = new OkApiFetcher(mOdnoklassniki);
		
		new FetchStreamTask().execute();
		new FetchPhotosTask().execute(OkApiFetcher.METHOD_GET_PHOTOS);
		
        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
        	public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail, String url) {
        		addBitmapToMemoryCache(url, thumbnail);
        		if (isVisible()) {
        			imageView.setImageBitmap(thumbnail);
        		}
        	}
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContext = container.getContext();
    	Log.i(TAG, "onCreateView fragment");
        View v = inflater.inflate(R.layout.fragment_ok_gallery, container, false);
        
        mGridView = (GridView)v.findViewById(R.id.gridView);
        /*
        mButtonGetPhotos = (Button)v.findViewById(R.id.buttonGetPhotos);
        mButtonGetPhotos.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "Fetching photos");
				new FetchPhotos().execute();
				Log.i(TAG, "Fetching current user");
				new FetchCur().execute();
			}
		});
		*/
        
        setupAdapter();
        setupCache();
        
        return v;
    }
    
    void setupAdapter() {
    	
    	if (getActivity() == null || mGridView == null) return;
    	
    	if (mPhotoItems != null) {
    		mGridView.setAdapter(new GalleryItemAdapter(mPhotoItems));
    	} else {
    		mGridView.setAdapter(null);
    	}
    	
    }
    
    void setupCache() {
    	final int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
    	final int cacheSize = maxMemory / 8;
    	Log.i(TAG, "Cache size is: " + cacheSize);
    	
    	mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
    		@Override
    		protected int sizeOf(String key, Bitmap bitmap) {
    			return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
    		}
    	};
    }
    
    private class GalleryItemAdapter extends ArrayAdapter<PhotoItem> {
    	
    	public GalleryItemAdapter(ArrayList<PhotoItem> items) {
    		super(getActivity(), 0, items);
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		if (convertView == null) {
    			convertView = getActivity().getLayoutInflater()
    					.inflate(R.layout.photo_item, parent, false);
    		}
    		
    		ImageView imageView = (ImageView)convertView
    				.findViewById(R.id.photo_item_imageView);
    		//imageView.setImageResource(R.drawable.cat);
    		
    		PhotoItem item = getItem(position);
    		
    		Bitmap bitmap = getBitmapFromMemCache(item.getUrl());
    		if (bitmap == null) {
    			Log.i(TAG, "Downloading image");
    			mThumbnailThread.queueThumbnail(imageView, item.getUrl());
    		} else {
    			Log.i(TAG, "Using cached image");
    			imageView.setImageBitmap(bitmap);
    		}
    		/*
    		// Pre cache
    		for (int i = Math.max(0, position - 5); i < Math.min(position + 5, mPhotoItems.size()); i++) {
    			if (i == position) continue;
    			item = getItem(i);
        		bitmap = getBitmapFromMemCache(item.getUrl());
        		if (bitmap == null) {
        			mThumbnailThread.queueThumbnail(imageView, item.getUrl());
        		}
    		}
    		*/
    		return convertView;
    	}
    }
    
    void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    	if (getBitmapFromMemCache(key) == null) {
    		Log.i(TAG, "Put bitmap in cache");
    		mMemoryCache.put(key, bitmap);
    		Log.i(TAG, "Bitmap size in kb: " + 
    					(bitmap.getRowBytes() * bitmap.getHeight()) / 1024);
    	}
    }
    
    Bitmap getBitmapFromMemCache(String key) {
    	if (key == null) {
    		return null;
    	}
		return mMemoryCache.get(key);
    }
    
    private class FetchPhotosTask extends AsyncTask<String, Void, ArrayList<PhotoItem>> {

		@Override
		protected ArrayList<PhotoItem> doInBackground(String... method) {
			return okApiFetcher.fetchPhotos();
		}
		
		protected void onPostExecute(ArrayList<PhotoItem> items) {
			mPhotoItems = items;
			setupAdapter();
		}
    	
    }
    
    private class FetchStreamTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... method) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("patterns", "JOIN");
			String result = okApiFetcher.request(OkApiFetcher.METHOD_GET_STREAM, params);
			Log.i(TAG, "Stream response: " + result);
			return null;
		}
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mOdnoklassniki.removeTokenRequestListener();
    }


}

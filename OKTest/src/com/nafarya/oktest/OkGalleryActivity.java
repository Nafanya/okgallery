package com.nafarya.oktest;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class OkGalleryActivity extends FragmentActivity {
	
	final static String TAG = "OkGalleryActivity";
	
	protected final Context mContext = this;
	
	protected static final String APP_ID = "572588032";
	protected static final String APP_SECRET = "31F3E52DE4ECD8D56AB6FE06";
	protected static final String APP_KEY = "CBABACCDCBABABABA";
	
	protected Odnoklassniki mOdnoklassniki;
	
	Button mButtonLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupOk();
		
		setContentView(R.layout.activity_fragment);
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = new OkGalleryFragment();
			manager.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
		
	}

	void setupOk() {
		mOdnoklassniki = Odnoklassniki.createInstance(this, APP_ID, APP_SECRET, APP_KEY);
		mOdnoklassniki.setTokenRequestListener(new OkTokenRequestListener() {
			@Override
			public void onSuccess(final String accessToken) {
				Toast.makeText(mContext, "Recieved new token : " + accessToken, Toast.LENGTH_LONG).show();
				Log.i(TAG, "Recieved token : " + accessToken);
			}

			@Override
			public void onCancel() {
				Toast.makeText(mContext, "Authorization was canceled", Toast.LENGTH_LONG).show();
				Log.i(TAG, "Authorization was canceled");
			}
			
			@Override
			public void onError() {
				Toast.makeText(mContext, "Error getting token", Toast.LENGTH_LONG).show();
				Log.i(TAG, "Authorization error");
			}
		});
		mOdnoklassniki.refreshToken(mContext);
	}
	
	@Override
	protected void onDestroy() {
		mOdnoklassniki.removeTokenRequestListener();
		super.onDestroy();
	}

}

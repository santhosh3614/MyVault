package com.justsmartapps.myvault.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;

import com.justsmartapps.myvault.utils.MyVaultUtils;

public class BaseActivity extends ActionBarActivity{
	private GalleryLockApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (GalleryLockApplication) getApplicationContext();
		application.setPushLogin(false);
	}

	@Override
	public void finish() {
		application.setBackPressed(true);
		super.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((GalleryLockApplication) getApplicationContext())
				.setCurrentActivity(this);
		if (application.isPushLogin() && !application.isBackPressed()) {
			Intent loginIntent = new Intent(this, LoginActivity.class);
			loginIntent.putExtra(LoginActivity.LOGIN_TYPE,
					LoginActivity.GUEST_LOGIN);
			startActivity(loginIntent);
			application.setPushLogin(false);
		}
		if (application.isBackPressed()) {
			application.setBackPressed(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyVaultUtils.clearCurrentActivity(this);
		application.setPushLogin(true);
		// if (GalleryLockUtils.isAppSentToBackGround(this)) {
		// Activity currentActivity = ((GalleryLockApplication)
		// getApplicationContext())
		// .getCurrentActivity();
		// if (!(currentActivity instanceof LoginActivity)) {
		// // Intent loginIntent = new Intent(this, LoginActivity.class);
		// // loginIntent.putExtra(LoginActivity.LOGIN_TYPE,
		// // LoginActivity.GUEST_LOGIN);
		// // startActivity(loginIntent);
		// }
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyVaultUtils.clearCurrentActivity(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		application.setBackPressed(true);
	}
}

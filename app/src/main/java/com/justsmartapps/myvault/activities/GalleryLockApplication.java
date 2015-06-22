package com.justsmartapps.myvault.activities;

import android.app.Activity;
import android.app.Application;

public class GalleryLockApplication extends Application {
	private Activity currentActivity;

	private boolean pushLogin;
	private boolean backPressed;

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public boolean isPushLogin() {
		return pushLogin;
	}

	public void setPushLogin(boolean pushLogin) {
		this.pushLogin = pushLogin;
	}

	public boolean isBackPressed() {
		return backPressed;
	}

	public void setBackPressed(boolean backPressed) {
		this.backPressed = backPressed;
	}
}

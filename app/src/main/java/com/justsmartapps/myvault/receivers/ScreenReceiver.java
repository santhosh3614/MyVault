package com.justsmartapps.myvault.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.justsmartapps.myvault.activities.GalleryLockApplication;
import com.justsmartapps.myvault.activities.LoginActivity;

public class ScreenReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			// MyVaultUtils.displayVisibleToast(context, "SCREENN_ON");
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// MyVaultUtils.displayVisibleToast(context, "SCREENN_OFF");
			GalleryLockApplication application = (GalleryLockApplication) context
					.getApplicationContext();
			Activity currentActivity = application.getCurrentActivity();
			if (!(currentActivity instanceof LoginActivity)) {
				Intent loginIntent = new Intent(context, LoginActivity.class);
				loginIntent.putExtra(LoginActivity.LOGIN_TYPE,
						LoginActivity.GUEST_LOGIN);
				context.startActivity(loginIntent);
			}
		}
	}
}

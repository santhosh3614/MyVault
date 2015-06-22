package com.justsmartapps.myvault.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.view.CustomAlertDialog;

public class AppRater {
	private final static String APP_TITLE = "MyVault";
//	private final static String APP_PNAME = "com.androiddev.myvault";
	private final static int DAYS_UNTIL_PROMPT = 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 7;

	public static void appLaunched(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(
				LocalDatabase.PASSWORD_PREFERENCES, 0);
		if (prefs.getBoolean("dontshowagain", false)) {
			return;
		}
		SharedPreferences.Editor editor = prefs.edit();
		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);
		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}
		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
			}
		}
		editor.commit();
	}

	public static void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		final CustomAlertDialog alertDialog = new CustomAlertDialog(mContext);
		alertDialog.setTitle("Rate " + APP_TITLE);
		alertDialog
				.setMessage("We're working hard to make MyVault helpful and we'd love to hear how we're doing.Would you mind taking a moment to share the ways MyVault has been helpful to you?");
		alertDialog.setPositiveButton("Rate it", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id="
								+ mContext.getPackageName())));
				dialog.dismiss();
			}
		});
		alertDialog.setNeutralButton("Remind me later", null);
		alertDialog.setNegativeButton("No Thanks!!", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}
}

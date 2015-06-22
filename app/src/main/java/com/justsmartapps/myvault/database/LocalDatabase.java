package com.justsmartapps.myvault.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.justsmartapps.myvault.core.Passcode;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class LocalDatabase {
	private static final String PASSWORD_KEY = "PASSWORD_KEY";
	private static final String SECURITY_QUESTION_KEY = "SECURITY_QUESTION_KEY";
	private static final String SECURITY_ANSWER_KEY = "SECURITY_ANSWER_KEY";
	private static final String LAST_BACKUP_TIME_KEY = "LAST_BACKUP_TIME_KEY";
	public static final String PASSWORD_PREFERENCES = "PASSWORD_PREFERENCES";
	public static final String FIRST_LAUNCH_TIME = "FIRST_LAUNCH_TIME";
	public static final String TO_SHOW_FAQ = "TO_SHOW_FAQ";
	public static final String LAUNCH_TIME_BACKUP = "LAUNCH_TIME_BACKUP";
	public static final String DISPLAY_NOTIFICATIONS = "DISPLAY_NOTIFICATIONS";
	
	public static final int SHOW_NOTIFICATIONS = 1;
	public static final int HIDE_NOTIFICATIONS = 2;
	private static LocalDatabase instance;
	private Typeface typeface;

	public static LocalDatabase getInstance() {
		if (instance == null) {
			instance = new LocalDatabase();
		}
		return instance;
	}

	public void savePasscode(Passcode passcode, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences
				.edit()
				.putString(PASSWORD_KEY, passcode.getPasscode())
				.putString(SECURITY_QUESTION_KEY,
						passcode.getSecurityQuestion())
				.putString(SECURITY_ANSWER_KEY, passcode.getSecurityAnswer())
				.commit();
	}

	public Passcode getPasscode(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		Passcode passcode = new Passcode();
		passcode.setPasscode(sharedPreferences.getString(PASSWORD_KEY, ""));
		passcode.setSecurityQuestion(sharedPreferences.getString(
				SECURITY_QUESTION_KEY, ""));
		passcode.setSecurityAnswer(sharedPreferences.getString(
				SECURITY_ANSWER_KEY, ""));
		return passcode;
	}

	public void setBackupTime(String lastBackUpTIme, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putString(LAST_BACKUP_TIME_KEY, lastBackUpTIme).commit();
	}

	public String getBackUpTime(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getString(LAST_BACKUP_TIME_KEY, "");
	}

	public void setFirstLaunchTime(long firstTime, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences.edit().putLong(FIRST_LAUNCH_TIME, firstTime).commit();
	}

	public long getFirstLaunchTime(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(FIRST_LAUNCH_TIME, 0);
	}

	public void setLaunchTimeBackUp(long firstTime, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences.edit().putLong(LAUNCH_TIME_BACKUP, firstTime)
				.commit();
	}

	public long getLaunchTimeBackUp(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(LAUNCH_TIME_BACKUP, 0);
	}

	public void setToShowFaq(boolean showFaq, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences.edit().putBoolean(TO_SHOW_FAQ, showFaq).commit();
	}

	public boolean getToShowFaq(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(TO_SHOW_FAQ, false);
	}

	public void setDisplayNotifications(int displayNotifications,
			Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putInt(DISPLAY_NOTIFICATIONS, displayNotifications).commit();
	}

	public int getDisplayNotifications(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PASSWORD_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(DISPLAY_NOTIFICATIONS, 0);
	}

	public Typeface getTypeface(Context context) {
		if (typeface != null) {
			return typeface;
		}
		typeface = Typeface.createFromAsset(context.getAssets(),
				MyVaultUtils.Bariol_Regular_Typeface_String);
		return typeface;
	}
}

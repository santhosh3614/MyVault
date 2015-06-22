package com.justsmartapps.myvault.activities;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.core.Passcode;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.keyboard.CustomKeyboardView;
import com.justsmartapps.myvault.listeners.IRadioKeyListener;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class LoginActivity extends Activity implements IRadioKeyListener {
	private TextView loginHelpText;
	public static final int CONFIRM_LOGIN = 2;
	public static final int NEW_LOGIN = 1;
	public static final int NORMAL_LOGIN = 3;
	public static final int GUEST_LOGIN = 4;
	public static final int CHANGE_LOGIN = 5;
	public static final int CHANGE_QUESTION = 6;
	public static final int FORGOT_PIN = 7;
	public static final String CHANGE_LOGIN_KEY = "CHANGE_LOGIN_KEY";
	public static final String LOGIN_TYPE = "LOGIN_TYPE";
	public static final String PASSCODE_KEY = "PASSCODE_KEY";
	public static final String RECEIVER_ACTION = "FINISH_LOGIN";
	private int loginType;
	private CustomKeyboardView keyboardView;
	private TextView loginErrorText;
	private String newPasscode;
	private int changeLoginType;
	private FinishReceiver finishReceiver;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity_layout);
//		RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
		// Adding the SnowFallView
		// SnowFallView snowFallView = new SnowFallView(this, 1);
		// LayoutParams params = new RelativeLayout.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// container.addView(snowFallView, params);
		loginErrorText = (TextView) findViewById(R.id.login_errorText);
		loginErrorText.setVisibility(View.INVISIBLE);
		keyboardView = new CustomKeyboardView(this, R.id.login_keyboard,
				R.id.login_images_linear, loginErrorText);
		loginHelpText = (TextView) findViewById(R.id.login_loginText);
		finishReceiver = new FinishReceiver();
		registerReceiver(finishReceiver, new IntentFilter(RECEIVER_ACTION));
		LocalDatabase instance = LocalDatabase.getInstance();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			changeLoginType = extras.getInt(CHANGE_LOGIN_KEY);
			if (changeLoginType == CHANGE_LOGIN) {
				loginHelpText.setText("Old Passcode Please");
			}
			if (changeLoginType == CHANGE_QUESTION) {
				loginHelpText.setText("Enter 4 digit Passcode");
			}
		}
		refreshLoginText();
		GalleryLockApplication application = (GalleryLockApplication) getApplicationContext();
		application.setPushLogin(false);
		if (extras != null) {
			int type = extras.getInt(LOGIN_TYPE);
			if (type != 0) {
				loginType = type;
				if (loginType == NEW_LOGIN) {
					keyboardView.revertKeyBoard();
					loginHelpText.setText("Create new Passcode");
				}
			}
		}
		long firstLaunchTime = instance.getFirstLaunchTime(this);
		if (firstLaunchTime == 0) {
			instance.setFirstLaunchTime(System.currentTimeMillis(), this);
		}
	}

	public void setupShortCut(boolean create) {
		Intent shortcut = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		Intent shortcutIntent = getPackageManager().getLaunchIntentForPackage(
				getPackageName());
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		shortcut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcut.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
				this, R.drawable.ic_launcher_christ);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		sendBroadcast(shortcut);
		// Intent shortcutIntent =
		// getPackageManager().getLaunchIntentForPackage(
		// getPackageName());
		// if (shortcutIntent != null) {
		// final Intent removeIntent = new Intent();
		// removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// removeIntent
		// .putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
		// removeIntent.putExtra("duplicate", false);
		// removeIntent
		// .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// sendBroadcast(removeIntent);
		// }
	}

	private void refreshLoginText() {
		if (MyVaultUtils.isNewLogin(this)) {
			loginType = NEW_LOGIN;
			loginHelpText.setText("Create new Passcode");
			// setupShortCut(true);
		} else {
			loginType = NORMAL_LOGIN;
			// deleleShortCut();
			keyboardView.changeKeyBoard(changeLoginType == CHANGE_QUESTION
					|| changeLoginType == CHANGE_LOGIN);
		}
	}

	class RestoreFilesLoader extends AsyncTask<Void, Void, Boolean> {
		private File backUpFile;
		private ProgressDialog progressDialog;

		public RestoreFilesLoader(File backUpFile) {
			this.backUpFile = backUpFile;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setMessage("Checking the Backup is avaliable!!");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			File imageDatabase = new File(backUpFile,
					ImageDatabase.DATABASE_NAME);
			// File folderDatabase = new File(backUpFile,
			// FolderDatabase.DATABASE_NAME);
			File sharedPreferences = new File(backUpFile,
					LocalDatabase.PASSWORD_PREFERENCES + ".xml");
			String parent = getFilesDir().getParent();
			File databaseFile = new File(parent + File.separator + "databases"
					+ File.separator);
			if (!databaseFile.exists()) {
				databaseFile.mkdir();
			}
			File sharedPrefFile = new File(parent + File.separator
					+ "shared_prefs" + File.separator);
			if (!sharedPrefFile.exists()) {
				sharedPrefFile.mkdir();
			}
			boolean sharedPref = false;
			// File appFolderDatabase = new File(databaseFile,
			// FolderDatabase.DATABASE_NAME);
			File appImageDatabase = new File(databaseFile,
					ImageDatabase.DATABASE_NAME);
			File appSharedPreferences = new File(parent + File.separator
					+ "shared_prefs" + File.separator
					+ LocalDatabase.PASSWORD_PREFERENCES + ".xml");
			if (imageDatabase.exists()) {
				MyVaultUtils.copyFileToSdcard(imageDatabase, appImageDatabase);
			}
			// if (folderDatabase.exists()) {
			// GalleryLockUtils.copyFileToSdcard(folderDatabase,
			// appFolderDatabase);
			// }
			if (sharedPreferences.exists()) {
				sharedPref = MyVaultUtils.copyFileToSdcard(sharedPreferences,
						appSharedPreferences);
			}
			return sharedPref;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.cancel();
			if (result) {
				loginType = NORMAL_LOGIN;
				loginHelpText.setText("Enter 4 digit PIN");
				keyboardView.changeKeyBoard(false);
				LocalDatabase.getInstance().setBackupTime(null,
						LoginActivity.this);
			} else {
				loginType = NEW_LOGIN;
				loginHelpText.setText("Create new Passcode");
			}
			super.onPostExecute(result);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		((GalleryLockApplication) getApplicationContext())
				.setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// MyVaultUtils.clearCurrentActivity(this);
	}

	@Override
	public void onEnterKeyPressed(String paramString) {
		loginErrorText.setVisibility(View.INVISIBLE);
		if (isValidPassword(paramString)) {
			if (loginType == NEW_LOGIN) {
				keyboardView.clearRadios(true, false);
				loginType = CONFIRM_LOGIN;
				loginHelpText.setText("Confirm Passcode");
				newPasscode = paramString;
			} else if (loginType == CONFIRM_LOGIN) {
				if (newPasscode != null && newPasscode.equals(paramString)) {
					Passcode passcode = new Passcode();
					passcode.setPasscode(newPasscode);
					finish();
					if (changeLoginType == CHANGE_LOGIN) {
						LocalDatabase localDatabase = LocalDatabase
								.getInstance();
						Passcode savedPassCode = localDatabase
								.getPasscode(this);
						passcode.setSecurityQuestion(savedPassCode
								.getSecurityQuestion());
						passcode.setSecurityAnswer(savedPassCode
								.getSecurityAnswer());
						MyVaultUtils.displayVisibleToast(this,
								"Changed Successfully");
						LocalDatabase.getInstance()
								.savePasscode(passcode, this);
					} else {
						Intent intent = new Intent(this,
								SecurityQuestionActivity.class);
						intent.putExtra(PASSCODE_KEY, passcode);
						startActivity(intent);
					}
				} else {
					keyboardView.clearRadios(true, true);
					loginErrorText.setVisibility(View.VISIBLE);
					loginErrorText.setText("Passcodes doesnt match!!");
				}
			} else if (loginType == NORMAL_LOGIN) {
				Passcode passcode = LocalDatabase.getInstance().getPasscode(
						this);
				if (passcode.getPasscode().length() > 0
						&& passcode.getPasscode().equals(paramString)) {
					if (changeLoginType == CHANGE_LOGIN) {
						loginHelpText.setText("Enter new Passcode");
						keyboardView.clearRadios(true, false);
						keyboardView.revertKeyBoard();
						loginType = NEW_LOGIN;
					} else if (changeLoginType == CHANGE_QUESTION) {
						finish();
						Intent securityIntent = new Intent(this,
								SecurityQuestionActivity.class);
						securityIntent.putExtra(LoginActivity.PASSCODE_KEY,
								passcode);
						securityIntent.putExtra(
								SecurityQuestionActivity.CHANGE_QUESTION_KEY,
								CHANGE_QUESTION);
						startActivity(securityIntent);
					} else {
						finish();
						Intent intent = new Intent(this, GalleryActivity.class);
						startActivity(intent);
					}
				} else {
					keyboardView.clearRadios(true, true);
					loginErrorText.setVisibility(View.VISIBLE);
					loginErrorText.setText("Passcode mismatch");
				}
			} else if (loginType == GUEST_LOGIN) {
				Passcode passcode = LocalDatabase.getInstance().getPasscode(
						this);
				if (passcode.getPasscode().length() > 0
						&& passcode.getPasscode().equals(paramString)) {
					finish();
				} else {
					keyboardView.clearRadios(true, true);
					loginErrorText.setVisibility(View.VISIBLE);
					loginErrorText.setText("Passcode mismatch");
				}
			}
		} else {
			loginErrorText.setVisibility(View.VISIBLE);
			loginErrorText.setText("Please enter 4 digit passcode");
		}
	}

	@Override
	public void onBackPressed() {
		if (loginType == GUEST_LOGIN) {
			moveTaskToBack(true);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onForgotPIN() {
		Intent securityIntent = new Intent(this, SecurityQuestionActivity.class);
		securityIntent.putExtra(SecurityQuestionActivity.CHANGE_QUESTION_KEY,
				FORGOT_PIN);
		securityIntent.putExtra(PASSCODE_KEY, LocalDatabase.getInstance()
				.getPasscode(this));
		startActivity(securityIntent);
	}

	@Override
	public void onPasswordFinished(String paramString) {
	}

	private boolean isValidPassword(String password) {
		return !TextUtils.isEmpty(password) && password.length() > 3;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// GalleryLockUtils.clearCurrentActivity(this);
		try {
			unregisterReceiver(finishReceiver);
		} catch (Exception e) {
		}
	}

	class FinishReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}
}

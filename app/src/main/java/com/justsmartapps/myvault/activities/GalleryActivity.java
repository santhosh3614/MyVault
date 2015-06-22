package com.justsmartapps.myvault.activities;

import imageloadig.FileCache;
import imageloadig.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.receivers.GalleryLockDeviceAdminReceiver;
import com.justsmartapps.myvault.receivers.ScreenReceiver;
import com.justsmartapps.myvault.utils.AppRater;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.view.CustomAlertDialog;
import com.justsmartapps.myvault.view.FlatTextView;
import com.justsmartapps.myvault.view.PagerSlidingTabStrip;

@SuppressLint("NewApi")
public class GalleryActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {
	private static final String STATE_ACTIVE_POSITION = "net.simonvt.menudrawer.samples.LeftDrawerSample.activePosition";
	private static final int ACTIVATION_REQUEST = 2112;
	private static final int CAMERA_REQUEST = 12;
	protected ListView mList;
	private int mActivePosition = 0;
	private ScreenReceiver screenReceiver;
	private PagerSlidingTabStrip tabs;
	private DevicePolicyManager devPolicyManager;
	private ToggleButton antiUninstallSwitch;
	private ComponentName deviceAdminComponentName;
	// private NormalGalleryFragment galleryFragment;
	// private HiddenGalleryFragment hiddenGalleryFragment;
	private TextView backupDate;
	private AdView mAdView;
	private TextView deleteChangePhone;
	private DrawerLayout mDrawerLayout;
	private FrameLayout mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	private ViewPager galleryPager;
	// private NormalFoldersLoader normalFoldersLoader;
	// private HiddenFoldersLoader hiddenFoldersLoader;
	private View dividerBMenuDeleteBackup;
	private TextView deleteSDCardBackup;
	private LocalDatabase localDatabase;
	private GalleryLockApplication application;
	private NotificationManager mNotificationManager;
	private final static int DAYS_UNTIL_PROMPT = 2;
	private ToggleButton notiToggle;
	private InterstitialAd mInterstitial;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		localDatabase = LocalDatabase.getInstance();
		boolean toShowFaq = localDatabase.getToShowFaq(this);
		if (!toShowFaq) {
			Intent intent = new Intent(this, FAQActivity.class);
			startActivity(intent);
			localDatabase.setToShowFaq(true, this);
		}
		File parent = MyVaultUtils.getAppPathParent(this);
		if (!parent.exists()) {
			parent.mkdir();
		}
		setContentView(R.layout.gallery_activity_layout);
		AppRater.appLaunched(this);
		deleleShortCut();
		mTitle = getTitle();
		application = (GalleryLockApplication) getApplication();
		mDrawerTitle = "Settings";
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (FrameLayout) findViewById(R.id.left_drawer);
		ScrollView galleryMenuLayout = (ScrollView) getLayoutInflater().inflate(R.layout.gallery_menu_layout, null);
		mDrawerList.addView(galleryMenuLayout);
		if (localDatabase.getDisplayNotifications(this) == 0) {
			localDatabase.setDisplayNotifications(LocalDatabase.SHOW_NOTIFICATIONS, this);
		}
		long launchTimeBackUp = localDatabase.getLaunchTimeBackUp(GalleryActivity.this);
		if (System.currentTimeMillis() >= launchTimeBackUp + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
			if (localDatabase.getDisplayNotifications(this) == LocalDatabase.SHOW_NOTIFICATIONS) {
				displayNotification();
			}
		}
		mInterstitial = new InterstitialAd(this);
		mInterstitial.setAdUnitId("ca-app-pub-9116505711607766/8551205932");
		mInterstitial.loadAd(new AdRequest.Builder().build());
		// Views of Menu
		galleryMenuLayout.findViewById(R.id.menu_faq_layout).setOnClickListener(this);
		galleryMenuLayout.findViewById(R.id.menu_share_layout).setOnClickListener(this);
		galleryMenuLayout.findViewById(R.id.menu_rate_layout).setOnClickListener(this);
		galleryMenuLayout.findViewById(R.id.menu_feedback_layout).setOnClickListener(this);
		FlatTextView generalTitle = (FlatTextView) galleryMenuLayout.findViewById(R.id.menu_title_general);
		generalTitle.setTypeface(generalTitle.getTypeface(), Typeface.BOLD_ITALIC);
		FlatTextView securityTitle = (FlatTextView) galleryMenuLayout.findViewById(R.id.menu_title_security);
		securityTitle.setTypeface(generalTitle.getTypeface(), Typeface.BOLD_ITALIC);
		FlatTextView lostFilesTitle = (FlatTextView) galleryMenuLayout
				.findViewById(R.id.menu_title_backup_change_phone);
		lostFilesTitle.setTypeface(generalTitle.getTypeface(), Typeface.BOLD_ITALIC);
		FlatTextView othersTitle = (FlatTextView) galleryMenuLayout.findViewById(R.id.menu_title_others);
		othersTitle.setTypeface(generalTitle.getTypeface(), Typeface.BOLD_ITALIC);
		FlatTextView changePassword = (FlatTextView) galleryMenuLayout.findViewById(R.id.menu_change_password);
		FlatTextView changeQuestion = (FlatTextView) galleryMenuLayout.findViewById(R.id.menu_change_question);
		deleteSDCardBackup = (TextView) galleryMenuLayout.findViewById(R.id.menu_delete_backup);
		deleteSDCardBackup.setOnClickListener(this);
		dividerBMenuDeleteBackup = galleryMenuLayout.findViewById(R.id.divider_below_menu_delete_backup);
		antiUninstallSwitch = (ToggleButton) galleryMenuLayout.findViewById(R.id.menu_anti_uninstall);
		notiToggle = (ToggleButton) galleryMenuLayout.findViewById(R.id.menu_toggle_notifications);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			antiUninstallSwitch.setButtonDrawable(R.drawable.selector_switch);
			antiUninstallSwitch.setTextOff("");
			antiUninstallSwitch.setTextOn("");
			antiUninstallSwitch.setBackgroundDrawable(null);
			notiToggle.setButtonDrawable(R.drawable.selector_switch);
			notiToggle.setTextOff("");
			notiToggle.setTextOn("");
			notiToggle.setBackgroundDrawable(null);
		}
		notiToggle.setChecked(localDatabase.getDisplayNotifications(this) == LocalDatabase.SHOW_NOTIFICATIONS);
		galleryMenuLayout.findViewById(R.id.menu_backup_sdcard_layout).setOnClickListener(this);
		backupDate = (TextView) galleryMenuLayout.findViewById(R.id.menu_backup_sdcard_text_date);
		TextView backUpChangePhone = (TextView) galleryMenuLayout.findViewById(R.id.menu_backup_change_phone);
		TextView lostFilesCheck = (TextView) galleryMenuLayout.findViewById(R.id.menu_lost_files_check);
		TextView restoreChangePhone = (TextView) galleryMenuLayout.findViewById(R.id.menu_restore_change_phone);
		deleteChangePhone = (TextView) galleryMenuLayout.findViewById(R.id.menu_backup_delete_change_phone);
		deleteChangePhone.setOnClickListener(this);
		backUpChangePhone.setOnClickListener(this);
		restoreChangePhone.setOnClickListener(this);
		lostFilesCheck.setOnClickListener(this);
		String backUpTime = localDatabase.getBackUpTime(this);
		if (backUpTime != null && !TextUtils.isEmpty(backUpTime) && backUpTime.trim().length() >= 0
				&& MyVaultUtils.getBackupFolder(this).exists()) {
			backupDate.setText(localDatabase.getBackUpTime(this));
			deleteSDCardBackup.setVisibility(View.VISIBLE);
		} else {
			deleteSDCardBackup.setVisibility(View.GONE);
			dividerBMenuDeleteBackup.setVisibility(View.GONE);
		}
		// sdCardBackup.setOnClickListener(this);
		antiUninstallSwitch.setOnCheckedChangeListener(this);
		notiToggle.setOnCheckedChangeListener(this);
		devPolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(this, GalleryLockDeviceAdminReceiver.class);
		boolean adminActive = devPolicyManager.isAdminActive(deviceAdminComponentName);
		antiUninstallSwitch.setChecked(adminActive);
		changePassword.setOnClickListener(this);
		changeQuestion.setOnClickListener(this);
		// set a custom shadow that overlays the main content when the
		// drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView yourTextView = (TextView) findViewById(titleId);
		if (yourTextView != null) {
			yourTextView.setTypeface(LocalDatabase.getInstance().getTypeface(this));
		}
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.settings_icon_no_space_new, /*
												 * nav drawer image to replace
												 * 'Up' caret
												 */
		R.string.drawer_open, /*
							 * "open drawer" description for accessibility
							 */
		R.string.drawer_close /*
							 * "close drawer" description for accessibility
							 */
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
				long launchTimeBackUp = localDatabase.getLaunchTimeBackUp(GalleryActivity.this);
				if (System.currentTimeMillis() >= launchTimeBackUp + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
					showLaunchBackUpDialog();
				}
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		screenReceiver = new ScreenReceiver();
		registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setDividerColorResource(R.color.grass_primary);
		tabs.setUnderlineColorResource(R.color.grass_primary);
		tabs.setIndicatorColorResource(R.color.grass_primary);
		galleryPager = (ViewPager) findViewById(R.id.gallery_pager);
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		NormalGalleryFragment normalFragment = new NormalGalleryFragment();
		HiddenGalleryFragment hiddenFragment = new HiddenGalleryFragment();
		fragments.add(normalFragment);
		fragments.add(hiddenFragment);
		FolderFragmentAdapter fragmentAdapter = new FolderFragmentAdapter(getSupportFragmentManager(), fragments);
		galleryPager.setAdapter(fragmentAdapter);
		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
				.getDisplayMetrics());
		galleryPager.setPageMargin(pageMargin);
		tabs.setViewPager(galleryPager);
		tabs.setShouldExpand(true);
		if (savedInstanceState != null) {
			mActivePosition = savedInstanceState.getInt(STATE_ACTIVE_POSITION);
		}
		mAdView = (AdView) findViewById(R.id.adView);
		new Thread(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAdView.setAdListener(new AdListener() {
							@Override
							public void onAdLoaded() {
								super.onAdLoaded();
								RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) galleryPager
										.getLayoutParams();
								mAdView.setVisibility(View.VISIBLE);
								layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
								layoutParams.addRule(RelativeLayout.ABOVE, R.id.adView);
							}

							@Override
							public void onAdFailedToLoad(int errorCode) {
								super.onAdFailedToLoad(errorCode);
								RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) galleryPager
										.getLayoutParams();
								mAdView.setVisibility(View.GONE);
								layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
								galleryPager.setLayoutParams(layoutParams);
							}
						});
						mAdView.loadAd(new AdRequest.Builder().build());
					}
				});
			}
		}).start();
	}

	private void deleleShortCut() {
		Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
		Intent shortcutIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		shortcut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcut.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_christ);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		sendBroadcast(shortcut);
	}

	private int notificationID = 100;
	private File cameraFile;

	protected void displayNotification() {
		Log.i("Start", "notification");
		/* Invoking the default notification service */
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("MyVault");
		mBuilder.setContentText("BackUp your data frequently!!");
		mBuilder.setTicker("MyVault - Backup your data");
		mBuilder.setSmallIcon(R.drawable.ic_launcher_christ);
		try {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			/* notificationID allows you to update the notification later on. */
			mNotificationManager.notify(notificationID, mBuilder.build());
		} catch (Exception e) {
		}
	}

	protected void cancelNotification() {
		mNotificationManager.cancel(notificationID);
	}

	private void showLaunchBackUpDialog() {
		final CustomAlertDialog alertDialog = new CustomAlertDialog(GalleryActivity.this);
		alertDialog.setTitle("BackUp your data frequently");
		alertDialog
				.setMessage("We strongly recommened you to take your vault backup frequently by clicking the BackUp option in settings.");
		alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				localDatabase.setLaunchTimeBackUp(System.currentTimeMillis(), GalleryActivity.this);
			}
		});
		alertDialog.setNegativeButton(null, null);
		AlertDialog dialog = alertDialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_folder_view, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.gallery_folder_view_refresh).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case android.R.id.home:
			return true;
		case R.id.gallery_folder_view_refresh:
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File filedir = new File(Environment.getExternalStorageDirectory() + "/HiddenCamera");
			filedir.mkdirs();
			cameraFile = new File(Environment.getExternalStorageDirectory() + "/HiddenCamera",
					System.currentTimeMillis() + ".jpg");
			Uri outputFileUri = Uri.fromFile(cameraFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(intent, CAMERA_REQUEST);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	protected void onPause() {
		if (mAdView != null) {
			mAdView.pause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mAdView != null) {
			mAdView.resume();
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		showExitDialog();
	}

	private void showExitDialog() {
		CustomAlertDialog alertDialog = new CustomAlertDialog(this);
		alertDialog.setTitle("MyVault");
		alertDialog.setMessage("Do you want to exit!!");
		alertDialog.setPositiveButton("exit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new ImageLoader(getApplicationContext(), 0, 0).clearCache();
				finish();
				if (mInterstitial.isLoaded()) {
					mInterstitial.show();
				}
			}
		});
		alertDialog.setNegativeButton("Cancel", null);
		alertDialog.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
	}

	class FolderFragmentAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> fragments;
		private final String[] TITLES = { "Normal Gallery", "Hidden Gallery" };

		public FolderFragmentAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdView != null) {
			mAdView.destroy();
		}
		unregisterReceiver(screenReceiver);
		new FileCache(this).clear();
	}

	private void decrypt(String sourcePath, String destination) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		FileInputStream fis = new FileInputStream(sourcePath);
		FileOutputStream fos = new FileOutputStream(destination);
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, sks);
		CipherInputStream cis = new CipherInputStream(fis, cipher);
		int b;
		byte[] d = new byte[1024];
		while ((b = cis.read(d)) != -1) {
			fos.write(d, 0, b);
		}
		fos.flush();
		fos.close();
		cis.close();
	}

	class DeleteSDBackUpLoader extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(GalleryActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(GalleryActivity.this);
			}
			progressDialog.setMessage("Deleting the backup from SDCard!!!!");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			File backUp = MyVaultUtils.getBackupFolder(GalleryActivity.this);
			if (backUp.exists()) {
				try {
					MyVaultUtils.delete(backUp);
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if (result) {
				deleteSDCardBackup.setVisibility(View.GONE);
				dividerBMenuDeleteBackup.setVisibility(View.GONE);
				backupDate.setText("click here to backup data");
				localDatabase.setBackupTime(null, GalleryActivity.this);
			}
		}
	}

	@Override
	public void onClick(View v) {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		switch (v.getId()) {
		case R.id.menu_share_layout:
			application.setBackPressed(true);
			MyVaultUtils.shareApp(GalleryActivity.this);
			break;
		case R.id.menu_faq_layout:
			Intent intent = new Intent(GalleryActivity.this, FAQActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_rate_layout:
			application.setBackPressed(true);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
			break;
		case R.id.menu_feedback_layout:
			application.setBackPressed(true);
			MyVaultUtils.sendFeedBack(GalleryActivity.this);
			break;
		case R.id.menu_change_password:
			loginIntent.putExtra(LoginActivity.CHANGE_LOGIN_KEY, LoginActivity.CHANGE_LOGIN);
			startActivity(loginIntent);
			break;
		case R.id.menu_change_question:
			loginIntent.putExtra(LoginActivity.CHANGE_LOGIN_KEY, LoginActivity.CHANGE_QUESTION);
			startActivity(loginIntent);
			break;
		case R.id.menu_delete_backup:
			CustomAlertDialog deleteSDBackDialog = new CustomAlertDialog(GalleryActivity.this);
			deleteSDBackDialog.setTitle("Delete Backup!!");
			deleteSDBackDialog
					.setMessage(Html
							.fromHtml("Are you sure Do you want to delete the backup?? If you want to backup your data again then click <i><b>'BackUp to SDCard'</b></i>"));
			deleteSDBackDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new DeleteSDBackUpLoader().execute();
				}
			});
			deleteSDBackDialog.setNegativeButton("Cancel", null);
			deleteSDBackDialog.show();
			break;
		case R.id.menu_backup_sdcard_layout:
			CustomAlertDialog backUpDialog = new CustomAlertDialog(GalleryActivity.this);
			backUpDialog.setTitle("BackUp Your data!!");
			backUpDialog
					.setMessage("BackUp your vault data to SDCard so that it will helpfull when you re-install your MyVault again to the device");
			backUpDialog.setPositiveButton("BackUp", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new BackUpFilesLoader().execute();
				}
			});
			backUpDialog.setNegativeButton("Cancel", null);
			backUpDialog.show();
			break;
		case R.id.menu_backup_change_phone:
			CustomAlertDialog unhideDialog = new CustomAlertDialog(GalleryActivity.this);
			unhideDialog.setTitle("Unhiding Photos");
			unhideDialog
					.setMessage(Html
							.fromHtml("Unhides all the hidden photos to the <i><b>MyVault_UnHide</b></i> folder. If you cannot find the photos in the Gallery we recommended you to restart the phone."));
			unhideDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					File appPath = MyVaultUtils.getAppPathInSdCard(GalleryActivity.this);
					File[] listFiles = appPath.listFiles();
					ArrayList<File> asList = new ArrayList<File>();
					for (int i = 0; i < listFiles.length; i++) {
						asList.add(listFiles[i]);
					}
					if (listFiles.length == 0) {
						Toast.makeText(GalleryActivity.this, "There are no photos in your Vault", Toast.LENGTH_LONG)
								.show();
					}
					new ImageUnhidingLoader(asList, true).execute();
				}
			});
			unhideDialog.setNegativeButton("Cancel", null);
			unhideDialog.show();
			break;
		case R.id.menu_restore_change_phone:
			break;
		case R.id.menu_lost_files_check:
			CustomAlertDialog lostFilesCheckDialog = new CustomAlertDialog(GalleryActivity.this);
			lostFilesCheckDialog.setTitle("Lost Photos");
			lostFilesCheckDialog.setMessage("Check for your Lost photos");
			lostFilesCheckDialog.setPositiveButton("Check", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new ImageLostFinderLoader().execute();
				}
			});
			lostFilesCheckDialog.setNegativeButton("Cancel", null);
			lostFilesCheckDialog.show();
			break;
		case R.id.menu_backup_delete_change_phone:
			break;
		}
	}

	class ImageLostFinderLoader extends AsyncTask<Void, Integer, ArrayList<File>> {
		private ProgressDialog progressDialog;
		private File[] listFiles;
		private File appPath;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(GalleryActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(GalleryActivity.this);
			}
			appPath = MyVaultUtils.getAppPathInSdCard(GalleryActivity.this);
			listFiles = appPath.listFiles();
			progressDialog.setCancelable(false);
			progressDialog.setTitle("Lost Photos");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage("Checking the Lost photos!!");
			progressDialog.show();
		}

		@Override
		protected ArrayList<File> doInBackground(Void... params) {
			if (appPath.isDirectory()) {
				ArrayList<File> absPaths = new ArrayList<File>();
				ContentResolver resolver = getContentResolver();
				for (int i = 0; i < listFiles.length; i++) {
					String absolutePath = listFiles[i].getAbsolutePath();
					if (absolutePath != null) {
						Cursor query = resolver.query(CustomContentProvider.CONTENT_URI_IMAGE, null,
								ImageDatabase.IMAGE_NEW_PATH + " = ?", new String[] { absolutePath }, null);
						if (query.getCount() <= 0) {
							absPaths.add(listFiles[i]);
						}
					}
				}
				return absPaths;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<File> result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if (result.size() == 0) {
				Toast.makeText(GalleryActivity.this, "No photos are lost", Toast.LENGTH_LONG).show();
				return;
			}
			CustomAlertDialog lostImagesDialog = new CustomAlertDialog(GalleryActivity.this);
			lostImagesDialog.setTitle("Lost Photos");
			lostImagesDialog
					.setMessage(Html.fromHtml(result.size()
							+ " photos are found and these photos will export to <i><b>MyVault_Lost_Photos</b></i> folder. If you cannot find the photos in the Gallery we recommended you to restart the phone."));
			lostImagesDialog.setNegativeButton("Cancel", null);
			lostImagesDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new ImageUnhidingLoader(result, false).execute();
				}
			});
			lostImagesDialog.show();
		}
	}

	class ImageUnhidingLoader extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog progressDialog;
		private ArrayList<File> imagePaths;
		private boolean deleteTable;

		public ImageUnhidingLoader(ArrayList<File> listFiles, boolean deleteTable) {
			this.imagePaths = listFiles;
			this.deleteTable = deleteTable;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(GalleryActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(GalleryActivity.this);
			}
			progressDialog.setCancelable(true);
			progressDialog.setTitle("Hiding");
			progressDialog.setMessage("UnHiding the Photos!!");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			progressDialog.setMax(imagePaths.size());
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File unHideFolder = MyVaultUtils.getUnHideFolder(GalleryActivity.this);
			if (unHideFolder.isDirectory()) {
				for (int i = 0; i < imagePaths.size(); i++) {
					try {
						String absolutePath = imagePaths.get(i).getAbsolutePath();
						File dataFile = new File(absolutePath);
						decrypt(absolutePath, unHideFolder.getAbsolutePath() + File.separator
								+ imagePaths.get(i).getName() + ".jpg");
						new MyVaultUtils.SingleMediaScanner(GalleryActivity.this, dataFile, false);
						dataFile.delete();
						publishProgress(i);
					} catch (InvalidKeyException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (deleteTable) {
					ImageDatabase imageDatabase = new ImageDatabase(GalleryActivity.this);
					imageDatabase.deleteTableData();
					imageDatabase.close();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			try {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file//"
						+ Environment.getExternalStorageDirectory())));
			} catch (Exception e) {
			}
			Intent intent = new Intent(HiddenGalleryFragment.HIDDEN_RECEIVER_ACTION);
			sendBroadcast(intent);
			Intent normalIntent = new Intent(NormalGalleryFragment.RECEIVER_ACTION);
			sendBroadcast(normalIntent);
		}
	}

	class HideImagesLoader extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog progressDialog;
		private File file;

		public HideImagesLoader(File file) {
			this.file = file;
		}

		@SuppressLint("InlinedApi")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(GalleryActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(GalleryActivity.this);
			}
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setTitle("Picture from camera");
			progressDialog.setMessage("Moving the picture to the Hidden Vault!!");
			if (!progressDialog.isShowing()) {
				progressDialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			File storagePath = MyVaultUtils.getAppPathInSdCard(GalleryActivity.this);
			if (file != null) {
				File destination = null;
				int end = file.getAbsolutePath().toString().lastIndexOf(File.separator);
				String str2 = file.getAbsolutePath().toString().substring(end + 1, file.getAbsolutePath().length());
				String[] split = str2.split("\\.");
				destination = new File(storagePath.getAbsolutePath(), split[0]);
				ContentValues contentValues = new ContentValues();
				contentValues.put(ImageDatabase.IMAGE_ID, file.getName());
				contentValues.put(ImageDatabase.IMAGE_NAME, file.getName());
				contentValues.put(ImageDatabase.IMAGE_PATH, file.getAbsolutePath());
				contentValues.put(ImageDatabase.IMAGE_FOLDER_ID, MyVaultUtils.FOLDER_ID);
				contentValues.put(ImageDatabase.IMAGE_FOLDER_NAME, MyVaultUtils.FOLDER_NAME);
				contentValues.put(ImageDatabase.IMAGE_NEW_PATH, destination.getAbsolutePath());
				contentValues.put(ImageDatabase.IMAGE_MIME_TYPE, "mime/jpeg");
				contentValues.put(ImageDatabase.IMAGE_SIZE, file.length());
				contentValues.put(ImageDatabase.IMAGE_DATE_TAKEN, file.lastModified());
				contentValues.put(ImageDatabase.IMAGE_DATE_ADDED, file.lastModified());
				contentValues.put(ImageDatabase.IMAGE_DATE_MODIFIED, file.lastModified());
				if (file.exists()) {
					try {
						MyVaultUtils.encrypt(file, destination);
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
						e.printStackTrace();
					}
					file.delete();
					new MyVaultUtils.SingleMediaScanner(getApplicationContext(), file, true);
				}
				Uri insert = getContentResolver().insert(CustomContentProvider.CONTENT_URI_IMAGE, contentValues);
				System.out.println("URI INSERT " + insert);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			cameraFile = null;
			MyVaultUtils.displayVisibleToast(GalleryActivity.this,
					"You are succesfully moved the photos to the MyVault!!");
		}
	}

	class BackUpChangePhone extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private File backupChangePhoneFolder;
		private File appBackFile;

		public BackUpChangePhone(File backupChangePhoneFolder, File appBackFile) {
			this.backupChangePhoneFolder = backupChangePhoneFolder;
			this.appBackFile = appBackFile;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(GalleryActivity.this);
			progressDialog.setMessage("BackUp in Progress!!!!");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				MyVaultUtils.copyDirectory(appBackFile, backupChangePhoneFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
		}
	}

	class BackUpFilesLoader extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(GalleryActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(GalleryActivity.this);
			}
			progressDialog.setMessage("BackUp in Progress!!!!");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File backUp = MyVaultUtils.getBackupFolder(GalleryActivity.this);
			if (!backUp.exists()) {
				backUp.mkdir();
			}
			String parent = getFilesDir().getParent();
			File imageDatabase = new File(parent + File.separator + "databases" + File.separator
					+ ImageDatabase.DATABASE_NAME);
			File sharedPreferences = new File(parent + File.separator + "shared_prefs" + File.separator
					+ LocalDatabase.PASSWORD_PREFERENCES + ".xml");
			File imageBackupFile = new File(backUp, ImageDatabase.DATABASE_NAME);
			File sharedBackPrefFile = new File(backUp, LocalDatabase.PASSWORD_PREFERENCES + ".xml");
			MyVaultUtils.copyFileToSdcard(imageDatabase, imageBackupFile);
			MyVaultUtils.copyFileToSdcard(sharedPreferences, sharedBackPrefFile);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			LocalDatabase.getInstance().setBackupTime(MyVaultUtils.convertSecondsToDate(System.currentTimeMillis()),
					GalleryActivity.this);
			backupDate.setText(MyVaultUtils.convertSecondsToDate(System.currentTimeMillis()));
			deleteSDCardBackup.setVisibility(View.VISIBLE);
			dividerBMenuDeleteBackup.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}

	private void startAntiUnistall() {
		// Push the anti un-install
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"Anti uninstall feature ensures that the application cannot be uninstalled without prior permission");
		startActivityForResult(intent, ACTIVATION_REQUEST);
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg1 == RESULT_OK) {
			if (arg0 == ACTIVATION_REQUEST) {
				antiUninstallSwitch.setChecked(true);
			} else if (arg0 == CAMERA_REQUEST) {
				new HideImagesLoader(cameraFile).execute();
			} else {
				antiUninstallSwitch.setChecked(false);
			}
		} else {
			antiUninstallSwitch.setChecked(false);
		}
	}

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}

	public String getRealPathFromURI(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.menu_anti_uninstall:
			if (isChecked) {
				application.setBackPressed(true);
				startAntiUnistall();
			} else {
				devPolicyManager.removeActiveAdmin(deviceAdminComponentName);
			}
			break;
		case R.id.menu_toggle_notifications:
			localDatabase.setDisplayNotifications(isChecked ? LocalDatabase.SHOW_NOTIFICATIONS
					: LocalDatabase.HIDE_NOTIFICATIONS, this);
			break;
		default:
			break;
		}
	}
}

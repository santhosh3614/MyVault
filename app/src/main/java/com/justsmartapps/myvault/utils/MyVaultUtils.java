package com.justsmartapps.myvault.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.justsmartapps.myvault.activities.GalleryLockApplication;
import com.justsmartapps.myvault.core.Passcode;
import com.justsmartapps.myvault.database.LocalDatabase;

public class MyVaultUtils {
	public static final String BACKUP_PHONE_FOLDER_NAME = "MyVault_backup";
	public static final String Bariol_Regular_Typeface_String = "Montserrat-Regular.otf";
	public static final String Qarmic_Sans_Abridged_String = "Qarmic_sans_Abridged.ttf";
	public static final String Junction_regular_String = "Junction-regular.otf";
	public static final String FOLDER_ID = "123456";
	public static final String FOLDER_NAME = "Hidden Camera";

	public static enum GalleryType {
		GALLERY_TYPE_NORMAL, GALLERY_TYPE_HIDDEN
	}

	public static String getMimeType(String mimeType) {
		return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
	}

	public static void startVibrate(Context context) {
		long pattern[] = { 0, 100, };
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, -1);
	}

	public static void sendFeedBack(Context context) {
		try {
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL,
					new String[] { "justsmartapps@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			context.startActivity(Intent.createChooser(Email, "Send Feedback:"));
		} catch (Exception e) {
		}
	}

	public static void shareApp(Context context) {
		try {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, "MyVault");
			String message = "\nHi, I'm using MyVault. It can hide your personal photos. It is so great and safe!!\n\n";
			message = message
					+ "https://play.google.com/store/apps/details?id="
					+ context.getPackageName() + "\n\n";
			i.putExtra(Intent.EXTRA_TEXT, message);
			context.startActivity(Intent.createChooser(i, "choose one"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean getTextAppreance(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		int density = metrics.densityDpi;
		if (density == DisplayMetrics.DENSITY_HIGH) {
			// Toast.makeText(context,
			// "DENSITY_HIGH... Density is " + String.valueOf(density),
			// Toast.LENGTH_LONG).show();
			return false;
		} else if (density == DisplayMetrics.DENSITY_MEDIUM) {
			// Toast.makeText(context,
			// "DENSITY_MEDIUM... Density is " + String.valueOf(density),
			// Toast.LENGTH_LONG).show();
			return false;
		} else if (density == DisplayMetrics.DENSITY_LOW) {
			// Toast.makeText(context,
			// "DENSITY_LOW... Density is " + String.valueOf(density),
			// Toast.LENGTH_LONG).show();
			return false;
		} else {
			return true;
			// Toast.makeText(
			// context,
			// "Density is neither HIGH, MEDIUM OR LOW.  Density is "
			// + String.valueOf(density), Toast.LENGTH_LONG)
			// .show();
		}
	}

	public static class SingleMediaScanner implements
			MediaScannerConnectionClient {
		private MediaScannerConnection mMs;
		private File mFile;
		private boolean delete;
		private Context context;

		public SingleMediaScanner(Context context, File f, boolean delete) {
			mFile = f;
			this.context = context;
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
			this.delete = delete;
		}

		@Override
		public void onMediaScannerConnected() {
			mMs.scanFile(mFile.getAbsolutePath(), null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			System.out.println("" + path);
			if (delete) {
				context.getContentResolver().delete(uri, null, null);
			}
			mMs.disconnect();
		}
	}

	public static void displayHiddenToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void displayVisibleToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static byte[] decryptToByteArray(String image) throws IOException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException {
		FileInputStream fis = new FileInputStream(image);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// FileOutputStream fos = new FileOutputStream(image.getDataPath());
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
				"AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, sks);
		CipherInputStream cis = new CipherInputStream(fis, cipher);
		int b;
		byte[] d = new byte[1024];
		while ((b = cis.read(d)) != -1) {
			outputStream.write(d, 0, b);
		}
		outputStream.flush();
		outputStream.close();
		cis.close();
		return outputStream.toByteArray();
	}

	public static File getAppPathInSdCard(Context context) {
		File sdCard = Environment.getExternalStorageDirectory();
		// /storage/sdcard0
		LocalDatabase instance = LocalDatabase.getInstance();
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ ".MyVault_Do_Delete" + File.separator + "MyVault_"
				+ instance.getFirstLaunchTime(context));
		if (!lockFile.exists()) {
			lockFile.mkdirs();
		}
		return lockFile;
	}

	public static File getAppPathParent(Context context) {
		File sdCard = Environment.getExternalStorageDirectory();
		// /storage/sdcard0
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ ".MyVault_Do_Delete");
		return lockFile;
	}

	public static void encrypt(File source, File destination)
			throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		// Here you read the cleartext.
		FileInputStream fis = new FileInputStream(source);
		// This stream write the encrypted text. This stream will be wrapped by
		// another stream.
		FileOutputStream fos = new FileOutputStream(destination);
		// Length is 16 byte
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
				"AES");
		// Create cipher
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, sks);
		// Wrap the output stream
		CipherOutputStream cos = new CipherOutputStream(fos, cipher);
		// Write bytes
		int b;
		byte[] d = new byte[2048];
		while ((b = fis.read(d)) != -1) {
			cos.write(d, 0, b);
		}
		// Flush and close streams.
		cos.flush();
		cos.close();
		fis.close();
	}

	public static void decrypt(String sourcePath, String destination)
			throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		FileInputStream fis = new FileInputStream(sourcePath);
		FileOutputStream fos = new FileOutputStream(destination);
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
				"AES");
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

	public static File getBackupFolder(Context context) {
		File sdCard = Environment.getExternalStorageDirectory();
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ ".MyVault_Do_Delete");
		return new File(lockFile, "backup");
	}

	public static File getUnHideFolder(Context context) {
		File sdCard = Environment.getExternalStorageDirectory();
		// /storage/sdcard0
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ "MyVault_UnHide" + File.separator);
		if (!lockFile.exists()) {
			lockFile.mkdirs();
		}
		return lockFile;
	}

	public static File getLostFolder(Context context) {
		File sdCard = Environment.getExternalStorageDirectory();
		// /storage/sdcard0
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ "MyVault_Lost_Photos" + File.separator);
		if (!lockFile.exists()) {
			lockFile.mkdirs();
		}
		return lockFile;
	}

	public static boolean isSharedExits(Context context) {
		String sharedPath = "/data/data/" + context.getPackageName()
				+ "/shared_prefs/" + LocalDatabase.PASSWORD_PREFERENCES
				+ ".xml";
		return new File(sharedPath).exists();
	}

	public static boolean isNewLogin(Context context) {
		String sharedPath = "/data/data/" + context.getPackageName()
				+ "/shared_prefs/" + LocalDatabase.PASSWORD_PREFERENCES
				+ ".xml";
		if (!new File(sharedPath).exists()) {
			return true;
		} else {
			Passcode passcode = LocalDatabase.getInstance()
					.getPasscode(context);
			return TextUtils.isEmpty(passcode.getPasscode());
		}
	}

	public static File getBackupChangePhoneFolder() {
		File sdCard = Environment.getExternalStorageDirectory();
		File lockFile = new File(sdCard.getAbsolutePath(), File.separator
				+ BACKUP_PHONE_FOLDER_NAME + File.separator);
		return lockFile;
	}

	public static boolean isDirectoryEmpty(File directory) {
		File[] contents = directory.listFiles();
		if (contents == null || contents.length == 0) {
			return true;
		}
		return false;
	}

	public static void delete(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				// directory is empty, then delete it
				if (file.list().length == 0) {
					file.delete();
				} else {
					// list all the directory contents
					String files[] = file.list();
					for (String temp : files) {
						// construct the file structure
						File fileDelete = new File(file, temp);
						// recursive delete
						delete(fileDelete);
					}
					// check the directory again, if empty then delete it
					if (file.list().length == 0) {
						file.delete();
					}
				}
			} else {
				// if file, then delete it
				file.delete();
			}
		}
	}

	public static boolean isAppSentToBackGround(Context context) {
		try {
			ActivityManager manager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			RunningTaskInfo runningTaskInfo = manager.getRunningTasks(1).get(0);
			return !runningTaskInfo.topActivity.getPackageName().equals(
					context.getPackageName());
		} catch (Exception e) {
		}
		return false;
	}

	public static void clearCurrentActivity(Activity activity) {
		GalleryLockApplication application = (GalleryLockApplication) activity
				.getApplicationContext();
		Activity currentActivity = application.getCurrentActivity();
		if (currentActivity != null && currentActivity.equals(activity))
			application.setCurrentActivity(null);
	}

	public static boolean isScreenHighOrLow(Context context) {
		int density = context.getResources().getDisplayMetrics().densityDpi;
		if ((density == DisplayMetrics.DENSITY_LOW)
				|| (density == DisplayMetrics.DENSITY_MEDIUM)) {
			return false;
		} else {
			return true;
		}
	}

	public static void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public static boolean copyFileToSdcard(File sourceFile, File destinationFile) {
		try {
			if (sourceFile.exists()) {
				if (destinationFile.exists()) {
					PrintWriter writer;
					writer = new PrintWriter(destinationFile);
					writer.print("");
					writer.close();
				} else {
					destinationFile.createNewFile();
				}
				FileInputStream input = new FileInputStream(sourceFile);
				FileOutputStream output = new FileOutputStream(destinationFile);
				byte[] buf = new byte[1024];
				int length;
				while ((length = input.read(buf)) > 0) {
					output.write(buf, 0, length);
				}
				input.close();
				output.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressLint("DefaultLocale")
	public static String convertByteSizeToKMG(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	@SuppressLint("SimpleDateFormat")
	public static String convertSecondsToDate(long milliSeconds) {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	public static boolean isNetworkAvaliable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}

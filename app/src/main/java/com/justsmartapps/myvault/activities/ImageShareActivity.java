package com.justsmartapps.myvault.activities;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v7.app.ActionBarActivity;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class ImageShareActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_share_layout);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean fromMyVault = extras.getBoolean("FROM_MY_VAULT");
			if (fromMyVault) {
				MyVaultUtils.displayVisibleToast(this,
						"No operation performed!!");
				finish();
				return;
			}
			ArrayList<Uri> listOfImageUris = getIntent()
					.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			if (listOfImageUris == null) {
				Uri parcelableExtra = getIntent().getParcelableExtra(
						Intent.EXTRA_STREAM);
				listOfImageUris = new ArrayList<Uri>();
				listOfImageUris.add(parcelableExtra);
			}
			MyVaultUtils.displayHiddenToast(this, listOfImageUris.size() + " ");
			System.out.println(listOfImageUris.size());
			new HideImagesLoader(listOfImageUris).execute();
		} else {
			finish();
		}
	}

	class HideImagesLoader extends AsyncTask<Void, Integer, Boolean> {
		private ProgressDialog progressDialog;
		private ArrayList<Uri> listOfImageUris;

		public HideImagesLoader(ArrayList<Uri> listOfImageUris) {
			this.listOfImageUris = listOfImageUris;
		}

		@SuppressLint("InlinedApi")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(ImageShareActivity.this,
						ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(ImageShareActivity.this);
			}
			progressDialog.setCancelable(true);
			progressDialog.setTitle("Hiding");
			progressDialog.setMessage("Hiding the Images!!");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			progressDialog.setMax(listOfImageUris.size());
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			File storagePath = MyVaultUtils
					.getAppPathInSdCard(ImageShareActivity.this);
			try {
				for (int i = 0; i < listOfImageUris.size(); i++) {
					Uri uri = listOfImageUris.get(i);
					String[] projection = { Media._ID, Media.DISPLAY_NAME,
							Media.DATA, Media.MIME_TYPE, Media.SIZE,
							Media.DATE_TAKEN, Media.DATE_MODIFIED,
							Media.BUCKET_DISPLAY_NAME, Media.BUCKET_ID };
					Cursor cursor = getContentResolver().query(uri, projection,
							null, null, null);
					cursor.moveToFirst();
					// Adding the image to the local database
					String imagePath = cursor.getString(cursor
							.getColumnIndex(Media.DATA));
					String dataPath = imagePath;
					File file = new File(dataPath);
					File destination = null;
					int end = file.getAbsolutePath().toString()
							.lastIndexOf(File.separator);
					String str2 = file
							.getAbsolutePath()
							.toString()
							.substring(end + 1, file.getAbsolutePath().length());
					String[] split = str2.split("\\.");
					destination = new File(storagePath.getAbsolutePath(),
							split[0]);
					ContentValues contentValues = new ContentValues();
					contentValues.put(ImageDatabase.IMAGE_ID,
							cursor.getString(cursor.getColumnIndex(Media._ID)));
					contentValues.put(ImageDatabase.IMAGE_NAME, cursor
							.getString(cursor
									.getColumnIndex(Media.DISPLAY_NAME)));
					contentValues
							.put(ImageDatabase.IMAGE_PATH, cursor
									.getString(cursor
											.getColumnIndex(Media.DATA)));
					contentValues.put(ImageDatabase.IMAGE_FOLDER_ID, cursor
							.getString(cursor.getColumnIndex(Media.BUCKET_ID)));
					contentValues
							.put(ImageDatabase.IMAGE_FOLDER_NAME,
									cursor.getString(cursor
											.getColumnIndex(Media.BUCKET_DISPLAY_NAME)));
					contentValues.put(ImageDatabase.IMAGE_NEW_PATH,
							destination.getAbsolutePath());
					contentValues.put(ImageDatabase.IMAGE_MIME_TYPE, cursor
							.getString(cursor.getColumnIndex(Media.MIME_TYPE)));
					contentValues.put(ImageDatabase.IMAGE_SIZE,
							cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
					contentValues.put(ImageDatabase.IMAGE_DATE_TAKEN, cursor
							.getLong(cursor.getColumnIndex(Media.DATE_TAKEN)));
					contentValues.put(ImageDatabase.IMAGE_DATE_ADDED,
							System.currentTimeMillis());
					contentValues.put(ImageDatabase.IMAGE_DATE_MODIFIED,
							cursor.getLong(cursor
									.getColumnIndex(Media.DATE_MODIFIED)));
					if (file.exists()) {
						try {
							MyVaultUtils.encrypt(file, destination);
						} catch (InvalidKeyException | NoSuchAlgorithmException
								| NoSuchPaddingException | IOException e) {
							e.printStackTrace();
						}
						// scanTheFile(file);
						file.delete();
						new MyVaultUtils.SingleMediaScanner(
								getApplicationContext(), file, true);
					}
					getContentResolver().insert(
							CustomContentProvider.CONTENT_URI_IMAGE,
							contentValues);
					publishProgress(i);
					cursor.close();
				}
				return true;
			} catch (Exception exception) {
				exception.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			if (result) {
				MyVaultUtils
						.displayVisibleToast(ImageShareActivity.this,
								"You are succesfully moved the images to the MyVault!!");
			} else {
				MyVaultUtils.displayVisibleToast(ImageShareActivity.this,
						"No operation performed!!");
			}
			finish();
		}
	}
	// private void insertImage(Cursor cursor, String newPath) {
	// ContentValues contentValues = new ContentValues();
	// contentValues.put(ImageDatabase.IMAGE_ID,
	// cursor.getString(cursor.getColumnIndex(Media._ID)));
	// contentValues.put(ImageDatabase.IMAGE_NAME,
	// cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME)));
	// contentValues.put(ImageDatabase.IMAGE_PATH,
	// cursor.getString(cursor.getColumnIndex(Media.DATA)));
	// contentValues.put(ImageDatabase.IMAGE_FOLDER_ID,
	// cursor.getString(cursor.getColumnIndex(Media.BUCKET_ID)));
	// contentValues.put(ImageDatabase.IMAGE_FOLDER_NAME, cursor
	// .getString(cursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME)));
	// contentValues.put(ImageDatabase.IMAGE_NEW_PATH, image.getNewPath());
	// contentValues.put(ImageDatabase.IMAGE_MIME_TYPE,
	// cursor.getString(cursor.getColumnIndex(Media.MIME_TYPE)));
	// contentValues.put(ImageDatabase.IMAGE_SIZE,
	// cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
	// contentValues.put(ImageDatabase.IMAGE_DATE_TAKEN,
	// cursor.getLong(cursor.getColumnIndex(Media.DATE_TAKEN)));
	// contentValues.put(ImageDatabase.IMAGE_DATE_ADDED, image.getDateAdded());
	// contentValues.put(ImageDatabase.IMAGE_DATE_MODIFIED,
	// cursor.getLong(cursor.getColumnIndex(Media.DATE_MODIFIED)));
	// getContentResolver().insert(CustomContentProvider.CONTENT_URI_IMAGE,
	// contentValues);
	// }
}

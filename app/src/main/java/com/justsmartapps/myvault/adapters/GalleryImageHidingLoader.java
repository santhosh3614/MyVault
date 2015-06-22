package com.justsmartapps.myvault.adapters;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.justsmartapps.myvault.activities.GalleryViewActivity;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class GalleryImageHidingLoader extends AsyncTask<Void, Integer, Integer> {
	private ArrayList<Image> images;
	private ProgressDialog progressDialog;
	private Context context;
	private int folderType;
	private AsyncInterface asyncInterface;

	public GalleryImageHidingLoader(ArrayList<Image> images, Context context,
			int folderType, AsyncInterface asyncInterface) {
		this.images = images;
		this.folderType = folderType;
		this.asyncInterface = asyncInterface;
		this.context = context;
	}

	@SuppressLint("InlinedApi")
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			progressDialog = new ProgressDialog(context,
					ProgressDialog.THEME_HOLO_LIGHT);
		} else {
			progressDialog = new ProgressDialog(context);
		}
		progressDialog.setCancelable(true);
		progressDialog.setTitle("Hiding");
		if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			progressDialog.setMessage("Hiding the Images!!");
		} else {
			progressDialog.setMessage("UnHiding the Images!!");
		}
		if (images.size() > 1) {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		progressDialog.setProgress(0);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMax(images.size());
		progressDialog.show();
	}

	@Override
	protected Integer doInBackground(Void... params) {
		File storagePath = MyVaultUtils.getAppPathInSdCard(context);
		for (int i = 0; i < images.size(); i++) {
			Image selectedImage = images.get(i);
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				if (isCancelled()) {
					break;
				}
				String dataPath = images.get(i).getDataPath();
				File file = new File(dataPath);
				File destination = null;
				int end = file.getAbsolutePath().toString()
						.lastIndexOf(File.separator);
				file.getAbsolutePath().toString().substring(0, end);
				String str2 = file.getAbsolutePath().toString()
						.substring(end + 1, file.getAbsolutePath().length());
				String[] split = str2.split("\\.");
				destination = new File(storagePath.getAbsolutePath(), split[0]
						+ System.currentTimeMillis());
				selectedImage.setNewPath(destination.getAbsolutePath());
				selectedImage.setDateAdded(System.currentTimeMillis());
				if (file.exists()) {
					try {
						MyVaultUtils.encrypt(file, destination);
					} catch (InvalidKeyException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException e) {
						e.printStackTrace();
					}
					file.delete();
					insertImage(selectedImage);
					new MyVaultUtils.SingleMediaScanner(context, file, true);
				}
			} else {
				File file = new File(selectedImage.getNewPath());
				File dataFile = new File(selectedImage.getDataPath());
				try {
					MyVaultUtils.decrypt(selectedImage.getNewPath(),
							selectedImage.getDataPath());
					file.delete();
					context.getContentResolver().delete(
							CustomContentProvider.CONTENT_URI_IMAGE,
							ImageDatabase.IMAGE_ID + " = ?",
							new String[] { selectedImage.getImageId() });
					new MyVaultUtils.SingleMediaScanner(context, dataFile,
							false);
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| NoSuchPaddingException | IOException e1) {
					e1.printStackTrace();
				}
			}
			publishProgress(i);
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Integer count) {
		super.onPostExecute(count);
		progressDialog.dismiss();
		asyncInterface.onPostExecuted();
	}

	private void insertImage(Image image) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(ImageDatabase.IMAGE_ID, image.getImageId());
		contentValues
				.put(ImageDatabase.IMAGE_NAME, image.getImageDisplayName());
		contentValues.put(ImageDatabase.IMAGE_PATH, image.getDataPath());
		contentValues.put(ImageDatabase.IMAGE_FOLDER_ID, image.getFolderId());
		contentValues.put(ImageDatabase.IMAGE_FOLDER_NAME,
				image.getFolderName());
		contentValues.put(ImageDatabase.IMAGE_NEW_PATH, image.getNewPath());
		contentValues.put(ImageDatabase.IMAGE_MIME_TYPE, image.getMimeType());
		contentValues.put(ImageDatabase.IMAGE_SIZE, image.getSize());
		contentValues.put(ImageDatabase.IMAGE_DATE_TAKEN, image.getDateTaken());
		contentValues.put(ImageDatabase.IMAGE_DATE_ADDED, image.getDateAdded());
		contentValues.put(ImageDatabase.IMAGE_DATE_MODIFIED,
				image.getDateModified());
		context.getContentResolver().insert(
				CustomContentProvider.CONTENT_URI_IMAGE, contentValues);
	}
}
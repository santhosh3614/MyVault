package com.justsmartapps.myvault.adapters;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.justsmartapps.myvault.activities.GalleryViewActivity;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class GalleryDeleteImageLoader extends AsyncTask<Void, Void, Void> {
	private ProgressDialog progressDialog;
	private ArrayList<Image> selectedImages;
	private Context context;
	private int folderType;
	private AsyncInterface asyncInterface;

	public GalleryDeleteImageLoader(ArrayList<Image> selectedImages,Context context,int folderType,AsyncInterface asyncInterface) {
		this.asyncInterface = asyncInterface;
		this.selectedImages = selectedImages;
		this.context = context;
		this.folderType = folderType;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = new ProgressDialog(context);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("Deleting!!");
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
		asyncInterface.onPreExecuted();
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			for (int i = 0; i < selectedImages.size(); i++) {
				File file = new File(selectedImages.get(i).getDataPath());
				file.delete();
				new MyVaultUtils.SingleMediaScanner(
						context, file, true);
			}
		} else {
			for (int i = 0; i < selectedImages.size(); i++) {
				File file = new File(selectedImages.get(i).getNewPath());
				boolean delete = file.delete();
				if (delete) {
					context.getContentResolver().delete(
							CustomContentProvider.CONTENT_URI_IMAGE,
							ImageDatabase.IMAGE_ID + " = ?",
							new String[] { selectedImages.get(i)
									.getImageId() });
				}
			}
		}
		try {
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file//"
							+ Environment.getExternalStorageDirectory())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		progressDialog.cancel();
		asyncInterface.onPostExecuted();
	
	}
}

package com.justsmartapps.myvault.activities;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class ManageSpaceActivity extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		finish();
		// setContentView(R.layout.activity_restore);
		// File backUpFile = GalleryLockUtils.getAppPathInSdCard(this);
		// findViewById(R.id.restore_ok).setOnClickListener(this);
		// findViewById(R.id.restore_cancel).setOnClickListener(this);
		// if (!backUpFile.exists()) {
		// finish();
		// startLoginActivity();
		// }
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	class RestoreFilesLoader extends AsyncTask<Void, Void, Void> {
		private File backUpFile;
		private ProgressDialog progressDialog;

		public RestoreFilesLoader(File backUpFile) {
			this.backUpFile = backUpFile;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ManageSpaceActivity.this);
			progressDialog.setMessage("Restoring the Files");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File imageDatabase = new File(backUpFile,
					ImageDatabase.DATABASE_NAME);
			// File folderDatabase = new File(backUpFile,
			// FolderDatabase.DATABASE_NAME);
			File sharedPreferences = new File(backUpFile,
					LocalDatabase.PASSWORD_PREFERENCES);
			String parent = getFilesDir().getParent();
			// File appFolderDatabase = new File(parent + File.separator
			// + "databases" + File.separator
			// + FolderDatabase.DATABASE_NAME);
			File appImageDatabase = new File(parent + File.separator
					+ "databases" + File.separator
					+ ImageDatabase.DATABASE_NAME);
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
				MyVaultUtils.copyFileToSdcard(sharedPreferences,
						appSharedPreferences);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.cancel();
			startLoginActivity();
			super.onPostExecute(result);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.restore_ok) {
			File backUpFile = MyVaultUtils.getAppPathInSdCard(this);
			new RestoreFilesLoader(backUpFile).execute();
		} else if (v.getId() == R.id.restore_cancel) {
			startLoginActivity();
		}
	}
}

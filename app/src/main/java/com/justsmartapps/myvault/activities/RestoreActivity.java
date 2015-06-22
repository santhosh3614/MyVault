package com.justsmartapps.myvault.activities;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.view.CustomAlertDialog;

public class RestoreActivity extends Activity {
    private File backUp;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backUp = MyVaultUtils.getBackupFolder(this);
        if (backUp.exists() && MyVaultUtils.isNewLogin(this)) {
            CustomAlertDialog alertDialog = new CustomAlertDialog(this);
            alertDialog.setTitle("Warning!!");
            alertDialog.setCancel();
            alertDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            });
            alertDialog
                    .setMessage(Html
                            .fromHtml("We found your backup in sdcard, Do you need to restore all the settings like Login credentials and your hidden vault!! \n <b>Press Cancel to start your MyVault cleanly.</b>\nPress Restore to get the Previous data."));
            alertDialog.setPositiveButton("Restore", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File imageDatabase = new File(backUp,
                            ImageDatabase.DATABASE_NAME);
                    // File folderDatabase = new File(backUp,
                    // FolderDatabase.DATABASE_NAME);
                    File sharedPreferences = new File(backUp,
                            LocalDatabase.PASSWORD_PREFERENCES + ".xml");
                    if (imageDatabase.exists() && sharedPreferences.exists()) {
                        RestoreFilesLoader filesLoader = new RestoreFilesLoader(
                                backUp);
                        filesLoader.execute();
                    }
                }
            });
            alertDialog.setNegativeButton("Cancel", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        MyVaultUtils.delete(backUp);
                        MyVaultUtils.delete(MyVaultUtils
                                .getAppPathParent(RestoreActivity.this));
                        // GalleryLockUtils.delete(GalleryLockUtils
                        // .getAppPathInSdCard(RestoreActivity.this));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                    startLoginActivity();
                }
            });
            alertDialog.show();
        } else {
            finish();
            startLoginActivity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("Bakc Pressed");
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    class RestoreFilesLoader extends AsyncTask<Void, Void, Boolean> {
        private File backUpFile;
        private ProgressDialog progressDialog;

        public RestoreFilesLoader(File backUpFile) {
            this.backUpFile = backUpFile;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(RestoreActivity.this);
            progressDialog.setMessage("Checking the Backup is avaliable!!");
            progressDialog.setCancelable(true);
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
            if (sharedPreferences.exists()) {
                sharedPref = MyVaultUtils.copyFileToSdcard(sharedPreferences,
                        appSharedPreferences);
            }
            if (imageDatabase.exists()) {
                MyVaultUtils.copyFileToSdcard(imageDatabase, appImageDatabase);
            }
            // if (folderDatabase.exists()) {
            // GalleryLockUtils.copyFileToSdcard(folderDatabase,
            // appFolderDatabase);
            // }
            return sharedPref;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            LocalDatabase.getInstance().setBackupTime(null,
                    RestoreActivity.this);
            // if (backUp.exists()) {
            // try {
            // MyVaultUtils.delete(backUp);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // }
            startLoginActivity();
            super.onPostExecute(result);
        }
    }
}

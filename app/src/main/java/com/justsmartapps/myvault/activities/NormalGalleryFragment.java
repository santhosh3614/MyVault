package com.justsmartapps.myvault.activities;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.adapters.FolderCursorAdapter;
import com.justsmartapps.myvault.core.Folder;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.utils.MyVaultUtils.GalleryType;
import com.justsmartapps.myvault.view.CustomAlertDialog;

public class NormalGalleryFragment extends Fragment implements
		LoaderCallbacks<Cursor> {
	private ListView folderList;
	private ProgressBar loadingBar;
	private UpdateReceiver updateReceiver;
	public static final String RECEIVER_ACTION = "RECEIVER_NORMAL_ACTION";
	private TextView noImagesText;
	private final int GALLERY_FOLDER_LOADER = 0;
	private String[] folderProjection;
	private String folderGroupBy;
	private String folderOrderBy;
	private FolderCursorAdapter folderCursorAdapter;
	private LoaderManager loaderManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View folderView = inflater.inflate(R.layout.folder_fragment, container,
				false);
		folderList = (ListView) folderView.findViewById(R.id.folder_list);
		loadingBar = (ProgressBar) folderView
				.findViewById(R.id.folder_loading_bar);
		loadingBar.setVisibility(View.VISIBLE);
		noImagesText = (TextView) folderView
				.findViewById(R.id.gallery_view_no_images_text);
		folderList.setVisibility(View.VISIBLE);
		updateReceiver = new UpdateReceiver();
		getActivity().registerReceiver(updateReceiver,
				new IntentFilter(RECEIVER_ACTION));
		folderProjection = new String[] { ImageColumns.BUCKET_ID + " AS _id",
				ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME,
				ImageColumns.DATE_TAKEN, ImageColumns.DATE_MODIFIED,
				ImageColumns.DATE_ADDED, ImageColumns.DATA };
		folderGroupBy = "1) GROUP BY 1,(2";
		folderOrderBy = "MAX(" + Media.DATE_ADDED + ") DESC";
		loaderManager = getLoaderManager();
		loaderManager.initLoader(GALLERY_FOLDER_LOADER, null, this);
		folderCursorAdapter = new FolderCursorAdapter(getActivity(), null,
				GalleryType.GALLERY_TYPE_NORMAL);
		folderList.setAdapter(folderCursorAdapter);
		folderList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Cursor cursor = folderCursorAdapter.getCursor();
				cursor.moveToPosition(arg2);
				Folder selectedFolder = new Folder();
				selectedFolder.setFolderId(cursor.getString(cursor
						.getColumnIndex(Media.BUCKET_ID)));
				selectedFolder.setFolderDisplayName(cursor.getString(cursor
						.getColumnIndex(Media.BUCKET_DISPLAY_NAME)));
				selectedFolder.setFirstImagePath(cursor.getString(cursor
						.getColumnIndex(Media.DATA)));
				Intent intent = new Intent(getActivity(),
						GalleryViewActivity.class);
				intent.putExtra(GalleryViewActivity.SELECTED_FOLDER,
						selectedFolder);
				intent.putExtra(GalleryViewActivity.FOLDER_TYPE,
						GalleryViewActivity.NORMAL_GALLERY);
				getActivity().startActivity(intent);
			}
		});
		folderList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				MyVaultUtils.startVibrate(getActivity());
				Cursor cursor = folderCursorAdapter.getCursor();
				cursor.moveToPosition(position);
				Folder selectedFolder = new Folder();
				final String folderId = cursor.getString(cursor
						.getColumnIndex(Media.BUCKET_ID));
				selectedFolder.setFolderId(folderId);
				final String folderName = cursor.getString(cursor
						.getColumnIndex(Media.BUCKET_DISPLAY_NAME));
				selectedFolder.setFolderDisplayName(folderName);
				selectedFolder.setFirstImagePath(cursor.getString(cursor
						.getColumnIndex(Media.DATA)));
				CustomAlertDialog alertDialog = new CustomAlertDialog(
						getActivity());
				alertDialog.setTitle(folderName);
				alertDialog.setPositiveButton(null, null);
				alertDialog.setNegativeButton("Cancel", null);
				final String[] moreImageOptions;
				moreImageOptions = new String[] { "Delete", "Hide" };
				final Typeface typeface = LocalDatabase.getInstance()
						.getTypeface(getActivity());
				ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(
						getActivity(), android.R.layout.simple_list_item_1,
						moreImageOptions) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						if (convertView == null) {
							convertView = getActivity()
									.getLayoutInflater()
									.inflate(
											android.R.layout.simple_list_item_1,
											parent, false);
						}
						TextView textView = (TextView) convertView
								.findViewById(android.R.id.text1);
						textView.setTypeface(typeface);
						textView.setBackgroundResource(R.drawable.selector_dialog_button);
						return super.getView(position, convertView, parent);
					}
				};
				alertDialog.setAdapter(optionsAdapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								String option = moreImageOptions[item];
								if (option.equals("Delete")) {
									CustomAlertDialog deleteDialog = new CustomAlertDialog(
											getActivity());
									deleteDialog.setTitle("Delete");
									deleteDialog
											.setMessage("Are you sure do you want to delete");
									deleteDialog
											.setPositiveButton(
													"Delete",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															new DeleteImagesLoader(
																	folderId)
																	.execute();;
														}
													});
									deleteDialog.setNegativeButton("Cancel",
											null);
									deleteDialog.show();
								} else if (option.equals("Hide")) {
									CustomAlertDialog foledrUnhideDialog = new CustomAlertDialog(
											getActivity());
									foledrUnhideDialog.setTitle(folderName
											+ " Hiding!!");
									foledrUnhideDialog
											.setMessage("Are you sure Do you want to hide the folder");
									foledrUnhideDialog
											.setPositiveButton(
													"Hide",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															new ImagesHidingLoader(
																	folderId)
																	.execute();
														}
													});
									foledrUnhideDialog.setNegativeButton(
											"Cancel", null);
									foledrUnhideDialog.show();
								}
							}
						});
				alertDialog.show();
				return true;
			}
		});
		return folderView;
	}

	class DeleteImagesLoader extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private String folderId;

		public DeleteImagesLoader(String folderId) {
			this.folderId = folderId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Deleting!!");
			if (!progressDialog.isShowing()) {
				progressDialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			String imageSelection = ImageColumns.BUCKET_ID + " = ?";
			String[] imageSelectionArgs = new String[] { folderId };
			getActivity().getContentResolver().delete(
					Media.EXTERNAL_CONTENT_URI, imageSelection,
					imageSelectionArgs);
			try {
				getActivity()
						.sendBroadcast(
								new Intent(
										Intent.ACTION_MEDIA_MOUNTED,
										Uri.parse("file//"
												+ Environment
														.getExternalStorageDirectory())));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.cancel();
			loaderManager.restartLoader(GALLERY_FOLDER_LOADER, null,
					NormalGalleryFragment.this);
		}
	}

	class ImagesHidingLoader extends AsyncTask<Void, Integer, Integer> {
		// private ArrayList<Image> images;
		private ProgressDialog progressDialog;
		private String folderID;
		private Cursor cursor;

		public ImagesHidingLoader(String folderID) {
			this.folderID = folderID;
		}

		@SuppressLint("InlinedApi")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(getActivity(),
						ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(getActivity());
			}
			progressDialog.setCancelable(true);
			progressDialog.setTitle("Hiding");
			progressDialog.setMessage("Hiding the Images!!");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			String[] projections = new String[] { Media._ID,
					Media.DISPLAY_NAME, Media.DATA, Media.MIME_TYPE,
					Media.SIZE, Media.DATE_TAKEN, Media.DATE_MODIFIED,
					Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME };
			String selection = Media.BUCKET_ID + " = ?";
			String[] selectionArgs = new String[] { folderID };
			String orderBy = Media.DATE_TAKEN + " DESC";
			cursor = getActivity().getContentResolver().query(
					Media.EXTERNAL_CONTENT_URI, projections, selection,
					selectionArgs, orderBy);
			progressDialog.setMax(cursor.getCount());
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// ArrayList<Image> selImages = galleryCursorAdapter.selectedImages;
			int imagesCount = cursor.getCount();
			File storagePath = MyVaultUtils.getAppPathInSdCard(getActivity());
			int i = 0;
			if (cursor.moveToFirst()) {
				do {
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
						file.delete();
						new SingleMediaScanner(getActivity(), file, true);
					}
					getActivity().getContentResolver().insert(
							CustomContentProvider.CONTENT_URI_IMAGE,
							contentValues);
					publishProgress(i);
				} while (cursor.moveToNext());
			}
			// for (int i = 0; i < selImages.size(); i++) {
			// if (folderType == NORMAL_GALLERY) {
			// if (isCancelled()) {
			// break;
			// }
			// String dataPath = selImages.get(i).getDataPath();
			// File file = new File(dataPath);
			// File destination = null;
			// int end = file.getAbsolutePath().toString()
			// .lastIndexOf(File.separator);
			// file.getAbsolutePath().toString().substring(0, end);
			// String str2 = file
			// .getAbsolutePath()
			// .toString()
			// .substring(end + 1, file.getAbsolutePath().length());
			// String[] split = str2.split("\\.");
			// destination = new File(storagePath.getAbsolutePath(),
			// split[0]);
			// selImages.get(i).setNewPath(destination.getAbsolutePath());
			// selImages.get(i).setDateAdded(System.currentTimeMillis());
			// if (file.exists()) {
			// try {
			// GalleryLockUtils.encrypt(file, destination);
			// } catch (InvalidKeyException | NoSuchAlgorithmException
			// | NoSuchPaddingException | IOException e) {
			// e.printStackTrace();
			// }
			// file.delete();
			// insertImage(selImages.get(i));
			// new SingleMediaScanner(getApplicationContext(), file,
			// true);
			// }
			// } else {
			// File file = new File(selImages.get(i).getNewPath());
			// File dataFile = new File(selImages.get(i).getDataPath());
			// try {
			// decrypt(selImages.get(i).getNewPath(), selImages.get(i)
			// .getDataPath());
			// file.delete();
			// getContentResolver().delete(
			// CustomContentProvider.CONTENT_URI_IMAGE,
			// ImageDatabase.IMAGE_ID + " = ?",
			// new String[] { selImages.get(i).getImageId() });
			// new SingleMediaScanner(getApplicationContext(),
			// dataFile, false);
			// } catch (InvalidKeyException | NoSuchAlgorithmException
			// | NoSuchPaddingException | IOException e1) {
			// e1.printStackTrace();
			// }
			// }
			// publishProgress(i);
			// }
			return imagesCount;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Integer count) {
			super.onPostExecute(count);
			cursor.close();
			progressDialog.dismiss();
			loaderManager.initLoader(GALLERY_FOLDER_LOADER, null,
					NormalGalleryFragment.this);
			cursor.close();
		}
	}

	public class SingleMediaScanner implements MediaScannerConnectionClient {
		private MediaScannerConnection mMs;
		private File mFile;
		private boolean delete;

		public SingleMediaScanner(Context context, File f, boolean delete) {
			mFile = f;
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
				getActivity().getContentResolver().delete(uri, null, null);
			}
			mMs.disconnect();
		}
	}

	private void checkTheImages() {
		if (folderCursorAdapter.getCount() > 0) {
			noImagesText.setVisibility(View.GONE);
		} else {
			noImagesText.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(updateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			loaderManager.initLoader(GALLERY_FOLDER_LOADER, null,
					NormalGalleryFragment.this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
		/*
		 * Takes action based on the ID of the Loader that's being created
		 */
		switch (loaderID) {
		case GALLERY_FOLDER_LOADER:
			return new CursorLoader(getActivity(), Media.EXTERNAL_CONTENT_URI,
					folderProjection, folderGroupBy, null, folderOrderBy);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		folderCursorAdapter.changeCursor(arg1);
		loadingBar.setVisibility(View.GONE);
		checkTheImages();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		folderCursorAdapter.changeCursor(null);
	}
}

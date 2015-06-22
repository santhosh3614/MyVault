package com.justsmartapps.myvault.activities;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import com.justsmartapps.myvault.listeners.IUpdateListener;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.utils.MyVaultUtils.GalleryType;
import com.justsmartapps.myvault.view.CustomAlertDialog;

public class HiddenGalleryFragment extends Fragment implements IUpdateListener,
		LoaderCallbacks<Cursor> {
	private ProgressBar loadingBar;
	private ListView folderList;
	public static final String HIDDEN_RECEIVER_ACTION = "HIDDEN_RECEIVER_ACTION";
	private UpdateReceiver updateReceiver;
	private TextView noImagesText;
	private final int HIDDEN_FOLDER_LOADER = 5;
	private FolderCursorAdapter hiddenFolderCursorAdapter;
	private LoaderManager manager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View folderView = inflater.inflate(R.layout.folder_fragment, container,
				false);
		folderList = (ListView) folderView.findViewById(R.id.folder_list);
		loadingBar = (ProgressBar) folderView
				.findViewById(R.id.folder_loading_bar);
		loadingBar.setVisibility(View.VISIBLE);
		folderList.setVisibility(View.VISIBLE);
		noImagesText = (TextView) folderView
				.findViewById(R.id.gallery_view_no_images_text);
		updateReceiver = new UpdateReceiver();
		getActivity().registerReceiver(updateReceiver,
				new IntentFilter(HIDDEN_RECEIVER_ACTION));
		manager = getActivity().getSupportLoaderManager();
		manager.initLoader(HIDDEN_FOLDER_LOADER, null, this);
		hiddenFolderCursorAdapter = new FolderCursorAdapter(getActivity(),
				null, GalleryType.GALLERY_TYPE_HIDDEN);
		folderList.setAdapter(hiddenFolderCursorAdapter);
		folderList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Cursor cursor = hiddenFolderCursorAdapter.getCursor();
				cursor.moveToPosition(arg2);
				Folder selectedFolder = new Folder();
				selectedFolder.setFolderId(cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_FOLDER_ID)));
				selectedFolder.setFolderDisplayName(cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_FOLDER_NAME)));
				selectedFolder.setFirstImagePath(cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_PATH)));
				Intent intent = new Intent(getActivity(),
						GalleryViewActivity.class);
				intent.putExtra(GalleryViewActivity.SELECTED_FOLDER,
						selectedFolder);
				intent.putExtra(GalleryViewActivity.FOLDER_TYPE,
						GalleryViewActivity.HIDDEN_GALLERY);
				getActivity().startActivity(intent);
			}
		});
		folderList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				MyVaultUtils.startVibrate(getActivity());
				Cursor cursor = hiddenFolderCursorAdapter.getCursor();
				cursor.moveToPosition(position);
				final String folderId = cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_FOLDER_ID));
				Folder selectedFolder = new Folder();
				selectedFolder.setFolderId(folderId);
				final String folderName = cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_FOLDER_NAME));
				selectedFolder.setFolderDisplayName(folderName);
				selectedFolder.setFirstImagePath(cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_PATH)));
				CustomAlertDialog alertDialog = new CustomAlertDialog(
						getActivity());
				alertDialog.setTitle(folderName);
				alertDialog.setPositiveButton(null, null);
				alertDialog.setNegativeButton("Cancel", null);
				final String[] moreImageOptions;
				moreImageOptions = new String[] { "Delete", "UnHide" };
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
								} else if (option.equals("UnHide")) {
									CustomAlertDialog foledrUnhideDialog = new CustomAlertDialog(
											getActivity());
									foledrUnhideDialog.setTitle(folderName
											+ " Un Hiding!!");
									foledrUnhideDialog
											.setMessage("Are you sure Do you want to Unhide the folder");
									foledrUnhideDialog
											.setPositiveButton(
													"UnHide",
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
			progressDialog.setTitle("UnHiding");
			progressDialog.setMessage("UnHiding the Images!!");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			String[] projections = new String[] { ImageDatabase.ID,
					ImageDatabase.ID + " AS _id", ImageDatabase.IMAGE_ID,
					ImageDatabase.IMAGE_FOLDER_ID,
					ImageDatabase.IMAGE_FOLDER_NAME, ImageDatabase.IMAGE_NAME,
					ImageDatabase.IMAGE_DATE_TAKEN,
					ImageDatabase.IMAGE_DATE_MODIFIED,
					ImageDatabase.IMAGE_MIME_TYPE, ImageDatabase.IMAGE_PATH,
					ImageDatabase.IMAGE_NEW_PATH, ImageDatabase.IMAGE_SIZE };
			String selection = ImageDatabase.IMAGE_FOLDER_ID + " = ?";
			String[] selectionArgs = new String[] { folderID };
			cursor = getActivity().getContentResolver().query(
					CustomContentProvider.CONTENT_URI_IMAGE, projections,
					selection, selectionArgs, null);
			progressDialog.setMax(cursor.getCount());
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// ArrayList<Image> selImages = galleryCursorAdapter.selectedImages;
			int imagesCount = cursor.getCount();
			int i = 0;
			if (cursor.moveToFirst()) {
				do {
					String newPath = cursor.getString(cursor
							.getColumnIndex(ImageDatabase.IMAGE_NEW_PATH));
					String dataPath = cursor.getString(cursor
							.getColumnIndex(ImageDatabase.IMAGE_PATH));
					String imageId = cursor.getString(cursor
							.getColumnIndex(ImageDatabase.IMAGE_ID));
					File file = new File(newPath);
					File dataFile = new File(dataPath);
					try {
						MyVaultUtils.decrypt(newPath, dataPath);
						file.delete();
						getActivity().getContentResolver().delete(
								CustomContentProvider.CONTENT_URI_IMAGE,
								ImageDatabase.IMAGE_ID + " = ?",
								new String[] { imageId });
						new SingleMediaScanner(getActivity(), dataFile, false);
					} catch (InvalidKeyException | NoSuchAlgorithmException
							| NoSuchPaddingException | IOException e1) {
						e1.printStackTrace();
					}
					publishProgress(i);
					i++;
				} while (cursor.moveToNext());
			}
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
			progressDialog.dismiss();
			// ArrayList<Image> selectedImages = cursor.selectedImages;
			// if (selectedImages.size() == 0) {
			// Toast.makeText(GalleryViewActivity.this,
			// "Please select atleast one", Toast.LENGTH_SHORT).show();
			// return;
			// } else {
			// // If the complete images are set to hide then delete the folder
			// // from the database
			// getActionBar().setTitle(selectedFolder.getFolderDisplayName());
			// }
			// images.removeAll(result);
			// makeOptionsBarVisbility(false);
			// galleryCursorAdapter.isSelectable = false;
			// galleryCursorAdapter.makeAllUnselected();
			manager.initLoader(HIDDEN_FOLDER_LOADER, null,
					HiddenGalleryFragment.this);
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
			String imageSelection = ImageDatabase.IMAGE_FOLDER_ID + " = ?";
			String[] imageSelectionArgs = new String[] { folderId };
			getActivity().getContentResolver().delete(
					CustomContentProvider.CONTENT_URI_IMAGE, imageSelection,
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
			manager.restartLoader(HIDDEN_FOLDER_LOADER, null,
					HiddenGalleryFragment.this);
		}
	}

	private void checkTheImages() {
		if (hiddenFolderCursorAdapter.getCount() > 0) {
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
			manager.restartLoader(HIDDEN_FOLDER_LOADER, null,
					HiddenGalleryFragment.this);
		}
	}

	@Override
	public void updateAdapter(int galleryType) {
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
		switch (loaderID) {
		case HIDDEN_FOLDER_LOADER:
			String[] folderProjection = new String[] {
					"DISTINCT " + ImageDatabase.IMAGE_FOLDER_ID + " as _id",
					ImageDatabase.IMAGE_FOLDER_ID, ImageDatabase.IMAGE_ID,
					ImageDatabase.IMAGE_FOLDER_NAME, ImageDatabase.IMAGE_NAME,
					ImageDatabase.IMAGE_PATH, ImageDatabase.IMAGE_NEW_PATH };
			return new CursorLoader(getActivity(),
					CustomContentProvider.CONTENT_URI_IMAGES_FOLDER,
					folderProjection, null, null, null);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		hiddenFolderCursorAdapter.changeCursor(arg1);
		loadingBar.setVisibility(View.GONE);
		checkTheImages();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		hiddenFolderCursorAdapter.changeCursor(null);
	}
}

package com.justsmartapps.myvault.activities;

import imageloadig.FileCache;

import java.io.File;
import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.adapters.AsyncInterface;
import com.justsmartapps.myvault.adapters.GalleryCursorAdapter;
import com.justsmartapps.myvault.adapters.GalleryDeleteImageLoader;
import com.justsmartapps.myvault.adapters.GalleryImageHidingLoader;
import com.justsmartapps.myvault.core.Folder;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.view.CustomAlertDialog;
import com.justsmartapps.myvault.view.FlatButton;

@SuppressLint("NewApi")
public class GalleryViewActivity extends BaseActivity implements
		OnClickListener, LoaderCallbacks<Cursor> {
	private ProgressBar loadingBar;
	private GridView imageGridView;
	public static final int NORMAL_GALLERY = 1;
	public static final int HIDDEN_GALLERY = 2;
	public static final int LOST_GALLERY = 3;
	public static final String GALLERY_TYPE_KEY = "GALLERY_TYPE_KEY";
	public static final String FOLDER_TYPE = "FOLDER_TYPE";
	private static final int NORMAL_IMAGES = 3;
	private static final int HIDDEN_IMAGES = 4;
	public static final String SELECTED_FOLDER = "SELECTED_FOLDER";
	public static final String LOST_IMAGES = "LOST_IMAGES";
	private FlatButton hideText;
	private FlatButton cancelText;
	private ImageDatabase imageDatabase;
	private int folderType = NORMAL_GALLERY;
	private LinearLayout optionsBar;
	private Animation inAnimation;
	private AdView mAdView;
	private MenuItem shareMenuItem;
	private Folder selectedFolder;
	private GalleryCursorAdapter galleryCursorAdapter;
	private Cursor cursor;
	private String[] projections;
	private String selection;
	private String[] selectionArgs;
	private String orderBy;
	private LoaderManager manager;
	private int currentloader;
	// private ArrayList<String> lostImages;
	// private Animation outAnimation;
	private ObjectAnimator objectAnimator;

	// private boolean selectAllEnabled;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_view_activity_layout);
//		RelativeLayout galleryViewConatiner = (RelativeLayout) findViewById(R.id.gallery_view_container);
		// Adding the SnowFallView
		// SnowFallView snowFallView = new SnowFallView(this, 2);
		// LayoutParams params = new RelativeLayout.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// galleryViewConatiner.addView(snowFallView, params);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			selectedFolder = extras.getParcelable(SELECTED_FOLDER);
			folderType = extras.getInt(FOLDER_TYPE);
			// lostImages = (ArrayList<String>) extras.get(LOST_IMAGES);
		}
		if (selectedFolder == null) {
			finish();
		}
		initViews();
		imageDatabase = new ImageDatabase(this);
		currentloader = 0;
		ArrayList<Boolean> selectedStates = new ArrayList<Boolean>();
		if (folderType == NORMAL_GALLERY) {
			currentloader = NORMAL_IMAGES;
			hideText.setText("Hide");
			projections = new String[] { Media._ID, Media.DISPLAY_NAME,
					Media.DATA, Media.MIME_TYPE, Media.SIZE, Media.DATE_TAKEN,
					Media.DATE_MODIFIED };
			selection = Media.BUCKET_ID + " = ?";
			selectionArgs = new String[] { selectedFolder.getFolderId() };
			orderBy = Media.DATE_TAKEN + " DESC";
			cursor = getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projections,
					selection, selectionArgs, orderBy);
		} else if (folderType == HIDDEN_GALLERY) {
			currentloader = HIDDEN_IMAGES;
			hideText.setText("UnHide");
			projections = new String[] { ImageDatabase.ID,
					ImageDatabase.ID + " AS _id", ImageDatabase.IMAGE_ID,
					ImageDatabase.IMAGE_FOLDER_ID,
					ImageDatabase.IMAGE_FOLDER_NAME, ImageDatabase.IMAGE_NAME,
					ImageDatabase.IMAGE_DATE_TAKEN,
					ImageDatabase.IMAGE_DATE_MODIFIED,
					ImageDatabase.IMAGE_MIME_TYPE, ImageDatabase.IMAGE_PATH,
					ImageDatabase.IMAGE_NEW_PATH, ImageDatabase.IMAGE_SIZE };
			selection = ImageDatabase.IMAGE_FOLDER_ID + " = ?";
			selectionArgs = new String[] { selectedFolder.getFolderId() };
			orderBy = ImageDatabase.IMAGE_DATE_ADDED + " DESC";
			cursor = imageDatabase.getWritableDatabase().query(
					ImageDatabase.TABLE_NAME, projections, selection,
					selectionArgs, null, null, orderBy);
		} else {
		}
		manager = getSupportLoaderManager();
		manager.initLoader(currentloader, null, this);
		for (int i = 0; i < cursor.getCount(); i++) {
			selectedStates.add(false);
		}
		galleryCursorAdapter = new GalleryCursorAdapter(this, null, folderType,
				selectedFolder, selectedStates, getSupportActionBar());
		imageGridView.setAdapter(galleryCursorAdapter);
		Animation animation = AnimationUtils.loadAnimation(
				GalleryViewActivity.this, android.R.anim.fade_in);
		GridLayoutAnimationController controller = new GridLayoutAnimationController(
				animation, .2f, .2f);
		imageGridView.setLayoutAnimation(controller);
		loadAdView();
		loadActionBar();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			objectAnimator = ObjectAnimator.ofFloat(optionsBar, "RotationX", 0,
					180f, 0);
			objectAnimator.setDuration(500);
		}
		// outAnimation = AnimationUtils.loadAnimation(GalleryViewActivity.this,
		// R.anim.rotate_anim);
		inAnimation = AnimationUtils.loadAnimation(GalleryViewActivity.this,
				R.anim.options_in_anim);
	}

	private void initViews() {
		imageGridView = (GridView) findViewById(R.id.gallery_view_grid);
		hideText = (FlatButton) findViewById(R.id.gallery_view_hide);
		optionsBar = (LinearLayout) findViewById(R.id.gallery_view_options);
		cancelText = (FlatButton) findViewById(R.id.gallery_view_cancel);
		makeOptionsBarVisbility(false);
		hideText.setOnClickListener(this);
		cancelText.setOnClickListener(this);
		imageGridView.setVisibility(View.VISIBLE);
		loadingBar = (ProgressBar) findViewById(R.id.gallery_view_loading);
		loadingBar.setVisibility(View.VISIBLE);
	}

	private void loadActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(selectedFolder.getFolderDisplayName());
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView yourTextView = (TextView) findViewById(titleId);
		if (yourTextView != null) {
			yourTextView.setTypeface(LocalDatabase.getInstance().getTypeface(
					this));
		}
	}

	private void loadAdView() {
		mAdView = (AdView) findViewById(R.id.gallery_view_top_layout_adview);
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
								mAdView.setVisibility(View.VISIBLE);
							}

							@Override
							public void onAdFailedToLoad(int errorCode) {
								super.onAdFailedToLoad(errorCode);
								mAdView.setVisibility(View.GONE);
							}
						});
						mAdView.loadAd(new AdRequest.Builder().build());
					}
				});
			}
		}).start();
	}

	@Override
	protected void onPause() {
		mAdView.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mAdView.resume();
		super.onResume();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_view_menu, menu);
		shareMenuItem = menu.findItem(R.id.gallery_view_menu_share);
		// TODO
		if (folderType == HIDDEN_GALLERY) {
			shareMenuItem.setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.gallery_view_menu_share:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			intent.putExtra("FROM_MY_VAULT", true);
			intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
			ArrayList<Uri> files = new ArrayList<Uri>();
			ArrayList<Image> imagesSelected = galleryCursorAdapter.selectedImages;
			if (imagesSelected.size() == 0) {
				MyVaultUtils.displayVisibleToast(GalleryViewActivity.this,
						"Please select atleast one image to share");
				break;
			}
			for (Image image : imagesSelected) {
				File file = new File(image.getDataPath());
				Uri uri = Uri.fromFile(file);
				files.add(uri);
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
			startActivity(Intent.createChooser(intent, "Share Image"));
			break;
		case R.id.gallery_view_menu_edit:
			if (galleryCursorAdapter.isSelectable) {
				galleryCursorAdapter.makeAllUnselected();
				galleryCursorAdapter.isSelectable = false;
				getSupportActionBar().setTitle(
						selectedFolder.getFolderDisplayName());
				makeOptionsBarVisbility(false);
			} else {
				galleryCursorAdapter.isSelectable = true;
				getSupportActionBar().setTitle("(0) selected");
				makeOptionsBarVisbility(true);
			}
			break;
		case R.id.gallery_view_menu_more_options:
			CustomAlertDialog alertDialog = new CustomAlertDialog(
					GalleryViewActivity.this);
			alertDialog.setTitle("Make your selection");
			alertDialog.setNegativeButton("Cancel", null);
			final ArrayList<Image> selectedImages = galleryCursorAdapter.selectedImages;
			String selectAll = null;
			final ArrayList<String> menuOptions = new ArrayList<String>();
			if (galleryCursorAdapter.selectedImages.size() == galleryCursorAdapter
					.getCursor().getCount()) {
				selectAll = "UnSelect All";
			} else {
				selectAll = "Select All";
			}
			if (folderType == NORMAL_GALLERY && selectedImages.size() == 1) {
				menuOptions.add("Open With");
			}
			if (selectedImages.size() == 1) {
				menuOptions.add("Details");
				// menuOptions.add("Rename");
				menuOptions.add("Delete");
				menuOptions.add(selectAll);
				// moreImageOptions = new String[] { "Open With", "Details",
				// "Rename", "Delete" };
			} else if (selectedImages.size() == 0) {
				menuOptions.add(selectAll);
				// moreImageOptions = new String[] { selectAll, };
			} else {
				menuOptions.add("Delete");
				menuOptions.add(selectAll);
				// moreImageOptions = new String[] { selectAll, "Delete" };
			}
			final Typeface typeface = Typeface.createFromAsset(getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(
					GalleryViewActivity.this,
					android.R.layout.simple_list_item_1, menuOptions) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					if (convertView == null) {
						convertView = getLayoutInflater().inflate(
								android.R.layout.simple_list_item_1, parent,
								false);
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
							String option = menuOptions.get(item);
							if (option.equals("Details")) {
								ImageDetailsDialog detailsDialog = new ImageDetailsDialog(
										GalleryViewActivity.this,
										selectedImages.get(0));
								detailsDialog.show();
							} else if (option.equals("Select All")) {
								galleryCursorAdapter.makeAllSelected();
								makeOptionsBarVisbility(true);
							} else if (option.equals("UnSelect All")) {
								galleryCursorAdapter.isSelectable = false;
								galleryCursorAdapter.makeAllUnselected();
								makeOptionsBarVisbility(false);
							} else if (option.equals("Rename")) {
								Builder edittextDialog = new AlertDialog.Builder(
										GalleryViewActivity.this);
								final EditText editText = new EditText(
										GalleryViewActivity.this);
								edittextDialog.setView(editText);
								edittextDialog.setTitle("Rename Image");
								edittextDialog
										.setMessage("Rename your image with the new Name");
								edittextDialog.setPositiveButton("Rename",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												String newName = editText
														.getText().toString();
												try {
													if (newName != null
															&& newName.trim()
																	.length() > 0) {
														Image image = selectedImages
																.get(0);
														File file = null;
														if (folderType == NORMAL_GALLERY) {
															// String dataPath =
															// image
															// .getDataPath();
															// String[] split =
															// dataPath
															// .split("\\"
															// +
															// File.separator);
															// image.setDataPath(newName
															// .trim()
															// + "."
															// + MyVaultUtils
															// .getMimeType(image
															// .getMimeType()));;
															file = new File(
																	image.getDataPath());
														} else {
															file = new File(
																	image.getNewPath());
														}
														File newFile = new File(
																file.getParentFile(),
																newName.trim()
																		+ ".jpg");
														boolean renameTo = file
																.renameTo(newFile);
														if (renameTo) {
															if (folderType == NORMAL_GALLERY) {
															} else {
																ContentValues values = new ContentValues();
																values.put(
																		ImageDatabase.IMAGE_PATH,
																		image.getDataPath());
																values.put(
																		ImageDatabase.IMAGE_NEW_PATH,
																		image.getNewPath());
																getContentResolver()
																		.update(CustomContentProvider.CONTENT_URI_IMAGE,
																				values,
																				ImageDatabase.IMAGE_ID
																						+ " = ?",
																				new String[] { image
																						.getImageId() });
															}
														}
													} else {
														MyVaultUtils
																.displayVisibleToast(
																		GalleryViewActivity.this,
																		"Image name must not be empty");
													}
												} catch (Exception e) {
													e.printStackTrace();
													MyVaultUtils
															.displayHiddenToast(
																	GalleryViewActivity.this,
																	"Error while renaming the Image");
												}
											}
										});
								edittextDialog
										.setNegativeButton("Cancel", null);
								edittextDialog.show();
							} else if (option.equals("Delete")) {
								CustomAlertDialog deleteDialog = new CustomAlertDialog(
										GalleryViewActivity.this);
								deleteDialog.setTitle("Delete");
								deleteDialog
										.setMessage("Are you sure do you want to delete");
								deleteDialog.setPositiveButton("Delete",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												GalleryDeleteImageLoader deleteImageLoader = new GalleryDeleteImageLoader(
														selectedImages,
														GalleryViewActivity.this,
														folderType,
														new AsyncInterface() {
															@Override
															public void onPreExecuted() {
															}

															@Override
															public void onPostExecuted() {
																makeOptionsBarVisbility(false);
																galleryCursorAdapter.isSelectable = false;
																galleryCursorAdapter
																		.makeAllUnselected();
																manager.initLoader(
																		currentloader,
																		null,
																		GalleryViewActivity.this);
															}
														});
												deleteImageLoader.execute();
											}
										});
								deleteDialog.setNegativeButton("Cancel", null);
								deleteDialog.show();
							} else if (option.equals("Open With")) {
								String parseString = null;
								if (folderType == NORMAL_GALLERY) {
									parseString = selectedImages.get(0)
											.getDataPath();
								} else {
									parseString = selectedImages.get(0)
											.getNewPath();
								}
								// Uri.from
								Intent intent = new Intent();
								intent.setAction(Intent.ACTION_VIEW);
								intent.setDataAndType(
										Uri.fromFile(new File(parseString)),
										"image/*");
								startActivity(intent);
							}
						}
					});
			alertDialog.show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// class ImagesHidingLoader extends AsyncTask<Void, Integer, Integer> {
	// private ArrayList<Image> images;
	// private ProgressDialog progressDialog;
	//
	// public ImagesHidingLoader(ArrayList<Image> images) {
	// this.images = images;
	// }
	//
	// @SuppressLint("InlinedApi")
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	// progressDialog = new ProgressDialog(GalleryViewActivity.this,
	// ProgressDialog.THEME_HOLO_LIGHT);
	// } else {
	// progressDialog = new ProgressDialog(GalleryViewActivity.this);
	// }
	// progressDialog.setCancelable(true);
	// progressDialog.setTitle("Hiding");
	// if (folderType == NORMAL_GALLERY) {
	// progressDialog.setMessage("Hiding the Images!!");
	// } else {
	// progressDialog.setMessage("UnHiding the Images!!");
	// }
	// progressDialog.setIcon(R.drawable.ic_launcher);
	// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	// progressDialog.setProgress(0);
	// progressDialog.setMax(galleryCursorAdapter.selectedImages.size());
	// progressDialog.show();
	// }
	//
	// @Override
	// protected Integer doInBackground(Void... params) {
	// File storagePath = MyVaultUtils
	// .getAppPathInSdCard(GalleryViewActivity.this);
	// for (int i = 0; i < images.size(); i++) {
	// if (folderType == NORMAL_GALLERY) {
	// if (isCancelled()) {
	// break;
	// }
	// String dataPath = images.get(i).getDataPath();
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
	// MyVaultUtils.encrypt(file, destination);
	// } catch (InvalidKeyException | NoSuchAlgorithmException
	// | NoSuchPaddingException | IOException e) {
	// e.printStackTrace();
	// }
	// file.delete();
	// insertImage(selImages.get(i));
	// new MyVaultUtils.SingleMediaScanner(
	// getApplicationContext(), file, true);
	// }
	// } else {
	// File file = new File(selImages.get(i).getNewPath());
	// File dataFile = new File(selImages.get(i).getDataPath());
	// try {
	// MyVaultUtils.decrypt(selImages.get(i).getNewPath(),
	// selImages.get(i).getDataPath());
	// file.delete();
	// getContentResolver().delete(
	// CustomContentProvider.CONTENT_URI_IMAGE,
	// ImageDatabase.IMAGE_ID + " = ?",
	// new String[] { selImages.get(i).getImageId() });
	// new MyVaultUtils.SingleMediaScanner(
	// getApplicationContext(), dataFile, false);
	// } catch (InvalidKeyException | NoSuchAlgorithmException
	// | NoSuchPaddingException | IOException e1) {
	// e1.printStackTrace();
	// }
	// }
	// publishProgress(i);
	// }
	// return 0;
	// }
	//
	// @Override
	// protected void onProgressUpdate(Integer... values) {
	// super.onProgressUpdate(values);
	// progressDialog.setProgress(values[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(Integer count) {
	// super.onPostExecute(count);
	// progressDialog.dismiss();
	// ArrayList<Image> selectedImages = galleryCursorAdapter.selectedImages;
	// if (selectedImages.size() == 0) {
	// Toast.makeText(GalleryViewActivity.this,
	// "Please select atleast one", Toast.LENGTH_SHORT).show();
	// return;
	// } else {
	// // If the complete images are set to hide then delete the folder
	// // from the database
	// getSupportActionBar().setTitle(
	// selectedFolder.getFolderDisplayName());
	// }
	// // images.removeAll(result);
	// makeOptionsBarVisbility(false);
	// galleryCursorAdapter.isSelectable = false;
	// galleryCursorAdapter.makeAllUnselected();
	// manager.initLoader(currentloader, null, GalleryViewActivity.this);
	// }
	// }
	//
	// private void insertImage(Image image) {
	// ContentValues contentValues = new ContentValues();
	// contentValues.put(ImageDatabase.IMAGE_ID, image.getImageId());
	// contentValues
	// .put(ImageDatabase.IMAGE_NAME, image.getImageDisplayName());
	// contentValues.put(ImageDatabase.IMAGE_PATH, image.getDataPath());
	// contentValues.put(ImageDatabase.IMAGE_FOLDER_ID, image.getFolderId());
	// contentValues.put(ImageDatabase.IMAGE_FOLDER_NAME,
	// image.getFolderName());
	// contentValues.put(ImageDatabase.IMAGE_NEW_PATH, image.getNewPath());
	// contentValues.put(ImageDatabase.IMAGE_MIME_TYPE, image.getMimeType());
	// contentValues.put(ImageDatabase.IMAGE_SIZE, image.getSize());
	// contentValues.put(ImageDatabase.IMAGE_DATE_TAKEN, image.getDateTaken());
	// contentValues.put(ImageDatabase.IMAGE_DATE_ADDED, image.getDateAdded());
	// contentValues.put(ImageDatabase.IMAGE_DATE_MODIFIED,
	// image.getDateModified());
	// getContentResolver().insert(CustomContentProvider.CONTENT_URI_IMAGE,
	// contentValues);
	// }
	// private void scanTheFile(File filePath) {
	// Intent mediaScanIntent = new Intent(
	// Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	// Uri contentUri = Uri.fromFile(filePath);
	// mediaScanIntent.setData(contentUri);
	// sendBroadcast(mediaScanIntent);
	// }
	public void makeOptionsBarVisbility(boolean makeVisible) {
		final RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) imageGridView
				.getLayoutParams();
		if (makeVisible) {
			// Visible the Options Bar
			// optionsBar.startAnimation(objectAnimator);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				objectAnimator.start();
			} else {
				optionsBar.startAnimation(inAnimation);
			}
			optionsBar.setVisibility(View.VISIBLE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			layoutParams.addRule(RelativeLayout.ABOVE,
					R.id.gallery_view_options);
			if (shareMenuItem != null && folderType == NORMAL_GALLERY) {
				shareMenuItem.setVisible(true);
			}
		} else {
			if (shareMenuItem != null) {
				shareMenuItem.setVisible(false);
			}
			// Hide the Options Bar
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			// optionsBar.startAnimation(outAnimation);
			optionsBar.setVisibility(View.GONE);
		}
		imageGridView.setLayoutParams(layoutParams);
	}

	// class DeleteImagesLoader extends AsyncTask<Void, Void, Void> {
	// private ProgressDialog progressDialog;
	// private ArrayList<Image> selectedImages;
	//
	// public DeleteImagesLoader(ArrayList<Image> selectedImages) {
	// this.selectedImages = selectedImages;
	// }
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// progressDialog = new ProgressDialog(GalleryViewActivity.this);
	// progressDialog.setCanceledOnTouchOutside(false);
	// progressDialog.setCancelable(false);
	// progressDialog.setMessage("Deleting!!");
	// if (!progressDialog.isShowing()) {
	// progressDialog.show();
	// }
	// }
	//
	// @Override
	// protected Void doInBackground(Void... params) {
	// if (folderType == NORMAL_GALLERY) {
	// for (int i = 0; i < selectedImages.size(); i++) {
	// File file = new File(selectedImages.get(i).getDataPath());
	// file.delete();
	// new MyVaultUtils.SingleMediaScanner(
	// getApplicationContext(), file, true);
	// }
	// } else {
	// for (int i = 0; i < selectedImages.size(); i++) {
	// File file = new File(selectedImages.get(i).getNewPath());
	// boolean delete = file.delete();
	// if (delete) {
	// getContentResolver().delete(
	// CustomContentProvider.CONTENT_URI_IMAGE,
	// ImageDatabase.IMAGE_ID + " = ?",
	// new String[] { selectedImages.get(i)
	// .getImageId() });
	// }
	// }
	// }
	// try {
	// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
	// Uri.parse("file//"
	// + Environment.getExternalStorageDirectory())));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Void result) {
	// super.onPostExecute(result);
	// progressDialog.cancel();
	// makeOptionsBarVisbility(false);
	// galleryCursorAdapter.isSelectable = false;
	// galleryCursorAdapter.makeAllUnselected();
	// manager.initLoader(currentloader, null, GalleryViewActivity.this);
	// }
	// }
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdView.destroy();
		imageDatabase.close();
		try {
			new FileCache(this).clear();
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		if (optionsBar.isShown()) {
			galleryCursorAdapter.isSelectable = false;
			galleryCursorAdapter.makeAllUnselected();
			makeOptionsBarVisbility(false);
			return;
		}
		super.onBackPressed();
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.gallery_view_hide:
			galleryCursorAdapter.isSelectable = false;
			ArrayList<Image> images = galleryCursorAdapter.selectedImages;
			if (images.size() == 0) {
				Toast.makeText(GalleryViewActivity.this,
						"Please select atleast one", Toast.LENGTH_SHORT).show();
				return;
			}
			GalleryImageHidingLoader loader = new GalleryImageHidingLoader(
					images, GalleryViewActivity.this, folderType,
					new AsyncInterface() {
						@Override
						public void onPreExecuted() {
						}

						@Override
						public void onPostExecuted() {
							// from the database
							getSupportActionBar().setTitle(
									selectedFolder.getFolderDisplayName());
							// images.removeAll(result);
							makeOptionsBarVisbility(false);
							galleryCursorAdapter.isSelectable = false;
							galleryCursorAdapter.makeAllUnselected();
							manager.initLoader(currentloader, null,
									GalleryViewActivity.this);
						}
					});
			loader.execute();
			// new ImagesHidingLoader(images).execute();
			break;
		case R.id.gallery_view_cancel:
			galleryCursorAdapter.isSelectable = false;
			galleryCursorAdapter.makeAllUnselected();
			makeOptionsBarVisbility(false);
			break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		switch (arg0) {
		case NORMAL_IMAGES:
			return new CursorLoader(GalleryViewActivity.this,
					Media.EXTERNAL_CONTENT_URI, projections, selection,
					selectionArgs, orderBy);
		case HIDDEN_IMAGES:
			return new CursorLoader(GalleryViewActivity.this,
					CustomContentProvider.CONTENT_URI_IMAGE, projections,
					selection, selectionArgs, orderBy);
		default:
			break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		loadingBar.setVisibility(View.GONE);
		galleryCursorAdapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		galleryCursorAdapter.changeCursor(null);
	}
}

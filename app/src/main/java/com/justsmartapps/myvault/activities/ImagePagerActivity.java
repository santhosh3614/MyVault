package com.justsmartapps.myvault.activities;

import imageloadig.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.adapters.AsyncInterface;
import com.justsmartapps.myvault.adapters.GalleryDeleteImageLoader;
import com.justsmartapps.myvault.adapters.GalleryImageHidingLoader;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.providers.CustomContentProvider;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.view.CustomAlertDialog;

public class ImagePagerActivity extends BaseActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String IMAGES_KEY = "IMAGES_KEY";
	public static final String SELECTED_IMAGE = "SELECTED_IMAGE";
	public static final String IMAGE_POSITION_KEY = "IMAGE_POSITION_KEY";
	public static final String IMAGE_FOLDER_TYPE = "IMAGE_FOLDER_TYPE";
	public static final String IMAGE_FOLDER_NAME = "IMAGE_FOLDER_NAME";
	private ViewPager pager;
	private String folderId;
	private int folderType;
	private ImageLoader imageLoader;
	private ImageDatabase imageDatabase;
	private Image selectedImage;
	private Cursor cursor;
	private ImagePagerAdapter imagePagerAdapter;
	private String folderName;
	protected Image currentImage;
	private AdView mAdView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_pager_layout);
		// RelativeLayout container = (RelativeLayout)
		// findViewById(R.id.image_pager_container);
		// SnowFallView snowFallView = new SnowFallView(this, 2);
		// LayoutParams params = new RelativeLayout.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// container.addView(snowFallView, params);
		Bundle bundle = getIntent().getExtras();
		int pagerPosition = 0;
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		int widthPixels = outMetrics.widthPixels;
		int heightPixels = outMetrics.heightPixels;
		if (bundle != null) {
			pagerPosition = bundle.getInt(IMAGE_POSITION_KEY, 0);
			folderId = bundle.getString(IMAGES_KEY);
			folderType = bundle.getInt(IMAGE_FOLDER_TYPE);
			selectedImage = bundle.getParcelable(SELECTED_IMAGE);
			folderName = bundle.getString(IMAGE_FOLDER_NAME);
		}
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}
		imageLoader = new ImageLoader(this, widthPixels, heightPixels);
		pager = (ViewPager) findViewById(R.id.pager);
		loadActionBar();
		cursor = null;
		imageDatabase = new ImageDatabase(this);
		if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			String[] projection = { Media._ID, Media.DISPLAY_NAME, Media.DATA,
					Media.MIME_TYPE, Media.SIZE, Media.DATE_TAKEN,
					Media.DATE_MODIFIED };
			cursor = getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
					Media.BUCKET_ID + " = ?", new String[] { folderId },
					Media.DATE_TAKEN + " DESC");
		} else {
			String[] projection = { ImageDatabase.ID,
					ImageDatabase.ID + " AS _id", ImageDatabase.IMAGE_ID,
					ImageDatabase.IMAGE_FOLDER_ID,
					ImageDatabase.IMAGE_FOLDER_NAME, ImageDatabase.IMAGE_NAME,
					ImageDatabase.IMAGE_DATE_TAKEN,
					ImageDatabase.IMAGE_DATE_MODIFIED,
					ImageDatabase.IMAGE_MIME_TYPE,
					ImageDatabase.IMAGE_NEW_PATH, ImageDatabase.IMAGE_PATH,
					ImageDatabase.IMAGE_SIZE };
			cursor = imageDatabase.getWritableDatabase().query(
					ImageDatabase.TABLE_NAME, projection,
					ImageDatabase.IMAGE_FOLDER_ID + " = ?",
					new String[] { folderId }, null, null,
					ImageDatabase.IMAGE_DATE_ADDED + " DESC");
		}
		cursor.moveToPosition(pagerPosition);
		imagePagerAdapter = new ImagePagerAdapter(cursor);
		pager.setAdapter(imagePagerAdapter);
		pager.setCurrentItem(pagerPosition);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (cursor.moveToPosition(arg0)) {
					String imageName = null;
					if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
						imageName = cursor.getString(cursor
								.getColumnIndex(Media.DISPLAY_NAME));
						currentImage = addNormalImage(cursor);
					} else {
						imageName = cursor.getString(cursor
								.getColumnIndex(ImageDatabase.IMAGE_NAME));
						currentImage = addHiddenImage(cursor);
					}
					getSupportActionBar().setTitle(imageName);
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
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

	private void loadActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(selectedImage.getImageDisplayName());
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView yourTextView = (TextView) findViewById(titleId);
		if (yourTextView != null) {
			yourTextView.setTypeface(LocalDatabase.getInstance().getTypeface(
					this));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (imageDatabase != null) {
			imageDatabase.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_single_image_view, menu);
		MenuItem hideMenuItem = menu.findItem(R.id.gallery_single_image_hide);
		MenuItem shareMenuItem = menu.findItem(R.id.gallery_single_image_share);
		// MenuItem openWithItem = menu.findItem(R.id.gallery_single_open_with);
		// MenuItem detailsItem = menu.findItem(R.id.gallery_single_details);
		// MenuItem deleteItem = menu.findItem(R.id.gallery_single_delete);
		if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			hideMenuItem.setIcon(R.drawable.hide_image);
			hideMenuItem.setTitle("Un Hide the Image");
		} else {
			// openWithItem.setVisible(false);
			shareMenuItem.setVisible(false);
			hideMenuItem.setIcon(R.drawable.unhide_image);
			hideMenuItem.setTitle("Hide the Image");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.gallery_single_image_hide:
			ArrayList<Image> singleImages = new ArrayList<Image>();
			// if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			// singleImages.add(addNormalImage(cursor));
			// } else {
			// singleImages.add(addHiddenImage(cursor));
			// }
			singleImages.add(currentImage);
			GalleryImageHidingLoader loader = new GalleryImageHidingLoader(
					singleImages, ImagePagerActivity.this, folderType,
					new AsyncInterface() {
						@Override
						public void onPreExecuted() {
						}

						@Override
						public void onPostExecuted() {
							finish();
						}
					});
			loader.execute();
			break;
		case R.id.gallery_single_image_more:
			CustomAlertDialog alertDialog = new CustomAlertDialog(
					ImagePagerActivity.this);
			alertDialog.setTitle("Make your selection");
			alertDialog.setNegativeButton("Cancel", null);
			final ArrayList<String> menuOptions = new ArrayList<String>();
			final ArrayList<Image> optionImages = new ArrayList<Image>();
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				menuOptions.add("Open With");
				// optionImages.add(addNormalImage(cursor));
			} else {
				// optionImages.add(addHiddenImage(cursor));
			}
			optionImages.add(currentImage);
			menuOptions.add("Details");
			menuOptions.add("Delete");
			final Typeface typeface = Typeface.createFromAsset(getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(
					ImagePagerActivity.this,
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
										ImagePagerActivity.this, optionImages
												.get(0));
								detailsDialog.show();
							} else if (option.equals("Rename")) {
								Builder edittextDialog = new AlertDialog.Builder(
										ImagePagerActivity.this);
								final EditText editText = new EditText(
										ImagePagerActivity.this);
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
														File file = null;
														if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
															// String dataPath =
															// optionImages
															// .get(0)
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
																	optionImages
																			.get(0)
																			.getDataPath());
														} else {
															file = new File(
																	optionImages
																			.get(0)
																			.getNewPath());
														}
														File newFile = new File(
																file.getParentFile(),
																newName.trim()
																		+ ".jpg");
														boolean renameTo = file
																.renameTo(newFile);
														if (renameTo) {
															if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
															} else {
																ContentValues values = new ContentValues();
																values.put(
																		ImageDatabase.IMAGE_PATH,
																		optionImages
																				.get(0)
																				.getDataPath());
																values.put(
																		ImageDatabase.IMAGE_NEW_PATH,
																		optionImages
																				.get(0)
																				.getNewPath());
																getContentResolver()
																		.update(CustomContentProvider.CONTENT_URI_IMAGE,
																				values,
																				ImageDatabase.IMAGE_ID
																						+ " = ?",
																				new String[] { optionImages
																						.get(0)
																						.getImageId() });
															}
														}
													} else {
														MyVaultUtils
																.displayVisibleToast(
																		ImagePagerActivity.this,
																		"Image name must not be empty");
													}
												} catch (Exception e) {
													e.printStackTrace();
													MyVaultUtils
															.displayHiddenToast(
																	ImagePagerActivity.this,
																	"Error while renaming the Image");
												}
											}
										});
								edittextDialog
										.setNegativeButton("Cancel", null);
								edittextDialog.show();
							} else if (option.equals("Delete")) {
								CustomAlertDialog deleteDialog = new CustomAlertDialog(
										ImagePagerActivity.this);
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
														optionImages,
														ImagePagerActivity.this,
														folderType,
														new AsyncInterface() {
															@Override
															public void onPreExecuted() {
															}

															@Override
															public void onPostExecuted() {
																finish();
															}
														});
												deleteImageLoader.execute();
											}
										});
								deleteDialog.setNegativeButton("Cancel", null);
								deleteDialog.show();
							} else if (option.equals("Open With")) {
								String parseString = null;
								if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
									parseString = optionImages.get(0)
											.getDataPath();
								} else {
									parseString = optionImages.get(0)
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
		case R.id.gallery_single_image_share:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			intent.putExtra("FROM_MY_VAULT", true);
			intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
			ArrayList<Uri> files = new ArrayList<Uri>();
			final ArrayList<Image> optionShareImages = new ArrayList<Image>();
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				// optionShareImages.add(addNormalImage(cursor));
			} else {
				// optionShareImages.add(addHiddenImage(cursor));
			}
			optionShareImages.add(currentImage);
			if (optionShareImages.size() == 0) {
				MyVaultUtils.displayVisibleToast(ImagePagerActivity.this,
						"Please select atleast one image to share");
				break;
			}
			File file = new File(optionShareImages.get(0).getDataPath());
			Uri uri = Uri.fromFile(file);
			files.add(uri);
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
			startActivity(Intent.createChooser(intent, "Share Image"));
			break;
		// case R.id.gallery_single_details:
		// break;
		// case R.id.gallery_single_delete:
		// break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private Image addNormalImage(Cursor cursor) {
		Image image = new Image();
		final String imageId = cursor.getString(cursor
				.getColumnIndex(Media._ID));
		image.setImageId(imageId);
		image.setFolderId(folderId);
		image.setFolderName(folderName);
		image.setImageDisplayName(cursor.getString(cursor
				.getColumnIndex(Media.DISPLAY_NAME)));
		image.setDataPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));
		image.setMimeType(cursor.getString(cursor
				.getColumnIndex(Media.MIME_TYPE)));
		long size = cursor.getLong(cursor.getColumnIndex(Media.SIZE));
		image.setSize(size);
		long dateModified = cursor.getLong(cursor
				.getColumnIndex(Media.DATE_MODIFIED));
		image.setDateModified(dateModified);
		long dateTaken = cursor
				.getLong(cursor.getColumnIndex(Media.DATE_TAKEN));
		image.setDateTaken(dateTaken);
		image.setDateAdded(System.currentTimeMillis());
		return image;
	}

	private Image addHiddenImage(Cursor cursor) {
		Image image = new Image();
		final String imageId = cursor.getString(cursor
				.getColumnIndex(ImageDatabase.IMAGE_ID));
		image.setImageId(imageId);
		image.setImageDisplayName(cursor.getString(cursor
				.getColumnIndex(ImageDatabase.IMAGE_NAME)));
		image.setDataPath(cursor.getString(cursor
				.getColumnIndex(ImageDatabase.IMAGE_PATH)));
		image.setNewPath(cursor.getString(cursor
				.getColumnIndex(ImageDatabase.IMAGE_NEW_PATH)));
		image.setMimeType(cursor.getString(cursor
				.getColumnIndex(ImageDatabase.IMAGE_MIME_TYPE)));
		long size = cursor.getLong(cursor
				.getColumnIndex(ImageDatabase.IMAGE_SIZE));
		image.setSize(size);
		long dateModified = cursor.getLong(cursor
				.getColumnIndex(ImageDatabase.IMAGE_DATE_MODIFIED));
		image.setDateModified(dateModified);
		long dateTaken = cursor.getLong(cursor
				.getColumnIndex(ImageDatabase.IMAGE_DATE_TAKEN));
		image.setDateTaken(dateTaken);
		image.setDateAdded(cursor.getLong(cursor
				.getColumnIndex(ImageDatabase.IMAGE_DATE_TAKEN)));
		image.setFolderName(folderName);
		image.setFolderId(folderId);
		return image;
	}

	class ImagesHidingLoader extends AsyncTask<Void, Void, Integer> {
		private ProgressDialog progressDialog;
		private Image selectedImage;

		public ImagesHidingLoader(Image image) {
			selectedImage = image;
		}

		@SuppressLint("InlinedApi")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				progressDialog = new ProgressDialog(ImagePagerActivity.this,
						ProgressDialog.THEME_HOLO_LIGHT);
			} else {
				progressDialog = new ProgressDialog(ImagePagerActivity.this);
			}
			progressDialog.setCancelable(true);
			progressDialog.setTitle("Hiding");
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				progressDialog.setMessage("Hiding the Images!!");
			} else {
				progressDialog.setMessage("UnHiding the Images!!");
			}
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			File storagePath = MyVaultUtils
					.getAppPathInSdCard(ImagePagerActivity.this);
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				String dataPath = selectedImage.getDataPath();
				File file = new File(dataPath);
				File destination = null;
				int end = file.getAbsolutePath().toString()
						.lastIndexOf(File.separator);
				file.getAbsolutePath().toString().substring(0, end);
				String str2 = file.getAbsolutePath().toString()
						.substring(end + 1, file.getAbsolutePath().length());
				String[] split = str2.split("\\.");
				destination = new File(storagePath.getAbsolutePath(), split[0]);
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
					new MyVaultUtils.SingleMediaScanner(
							getApplicationContext(), file, true);
				}
			} else {
				File file = new File(selectedImage.getNewPath());
				File dataFile = new File(selectedImage.getDataPath());
				try {
					MyVaultUtils.decrypt(selectedImage.getNewPath(),
							selectedImage.getDataPath());
					file.delete();
					getContentResolver().delete(
							CustomContentProvider.CONTENT_URI_IMAGE,
							ImageDatabase.IMAGE_ID + " = ?",
							new String[] { selectedImage.getImageId() });
					new MyVaultUtils.SingleMediaScanner(
							getApplicationContext(), dataFile, false);
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| NoSuchPaddingException | IOException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Integer count) {
			super.onPostExecute(count);
			progressDialog.dismiss();
		}
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
		getContentResolver().insert(CustomContentProvider.CONTENT_URI_IMAGE,
				contentValues);
	}

	private class ImagePagerAdapter extends PagerAdapter {
		private Cursor cursor;
		private LayoutInflater inflater;

		public ImagePagerAdapter(Cursor cursor) {
			this.cursor = cursor;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return cursor.getCount();
		}

		@SuppressLint("InflateParams")
		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			if (!cursor.moveToPosition(position)) {
				throw new IllegalStateException(
						"couldn't move cursor to position " + position);
			}
			View imageLayout = inflater.inflate(
					R.layout.image_pager_adapter_item_layout, view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image_full_view);
			String path = null;
			if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
				path = cursor.getString(cursor.getColumnIndex(Media.DATA));
				String string = cursor.getString(cursor
						.getColumnIndex(Media.SIZE));
				imageLoader.DisplayImage(path, null, imageView, false, false,
						false, string);
			} else {
				path = cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_NEW_PATH));
				String string = cursor.getString(cursor
						.getColumnIndex(ImageDatabase.IMAGE_SIZE));
				imageLoader.DisplayImage(path, null, imageView, true, false,
						false, string);
			}
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (getSupportActionBar().isShowing()) {
						getSupportActionBar().hide();
						mAdView.setVisibility(View.GONE);
					} else {
						getSupportActionBar().show();
						mAdView.setVisibility(View.VISIBLE);
					}
				}
			});
			view.addView(imageLayout);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
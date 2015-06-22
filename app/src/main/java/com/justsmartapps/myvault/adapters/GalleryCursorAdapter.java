package com.justsmartapps.myvault.adapters;

import imageloadig.ImageLoader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.activities.GalleryViewActivity;
import com.justsmartapps.myvault.activities.ImagePagerActivity;
import com.justsmartapps.myvault.core.Folder;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class GalleryCursorAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private int folderType;
	public boolean isSelectable;
	public ArrayList<Image> selectedImages;
	public ArrayList<Boolean> selectedStates;
	// private Cursor getCursor();
	private GalleryViewActivity activity;
	private Folder selectedFolder;
	private ActionBar actionBar;
	private Animation animation;

	public GalleryCursorAdapter(Activity activity, Cursor c, int folderType,
			Folder selectedFolder, ArrayList<Boolean> selectedStates,
			ActionBar actionBar) {
		super(activity, c, 0);
		inflater = LayoutInflater.from(activity);
		this.actionBar = actionBar;
		int width = (int) activity.getResources().getDimension(
				R.dimen.image_width);
		int height = (int) activity.getResources().getDimension(
				R.dimen.image_height);
		imageLoader = new ImageLoader(activity, width, height);
		this.selectedFolder = selectedFolder;
		selectedImages = new ArrayList<Image>();
		this.selectedStates = selectedStates;
		if (activity instanceof GalleryViewActivity) {
			this.activity = (GalleryViewActivity) activity;
		}
		// cursor = c;
		this.folderType = folderType;
		animation = AnimationUtils.loadAnimation(activity, R.anim.zoom_in);
	}

	class ViewHolder {
		public ImageView image;
		public View imageOverLay;
		public TextView imageName;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (getCursor().getCount() != selectedStates.size()) {
			int remainingCount = 0;
			if (getCursor().getCount() > selectedStates.size()) {
				remainingCount = getCursor().getCount() - selectedStates.size();
			} else if (getCursor().getCount() < selectedStates.size()) {
				remainingCount = selectedStates.size() - getCursor().getCount();
			}
			for (int i = 0; i < remainingCount; i++) {
				selectedStates.add(false);
			}
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.gallery_image_item_layout,
					parent, false);
			viewHolder.image = (ImageView) convertView
					.findViewById(R.id.gallery_item_image);
			viewHolder.imageName = (TextView) convertView
					.findViewById(R.id.gallery_item_image_name);
			viewHolder.imageOverLay = convertView
					.findViewById(R.id.gallery_item_image_overlay);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}
		String imageName = null;
		if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
			String imagePath = getCursor().getString(
					getCursor().getColumnIndex(Media.DATA));
			imageName = getCursor().getString(
					getCursor().getColumnIndex(Media.DISPLAY_NAME));;
			imageLoader.DisplayImage(imagePath, null, viewHolder.image, false,
					true, true);
		} else {
			String imagePath = getCursor().getString(
					getCursor().getColumnIndex(ImageDatabase.IMAGE_NEW_PATH));
			imageName = getCursor().getString(
					getCursor().getColumnIndex(ImageDatabase.IMAGE_NAME));;
			imageLoader.DisplayImage(imagePath, null, viewHolder.image, true,
					true, true);
		}
		viewHolder.imageName.setText(imageName);
		Boolean selectedState = selectedStates.get(position);
		if (selectedState) {
			viewHolder.imageOverLay.setVisibility(View.VISIBLE);
		} else {
			viewHolder.imageOverLay.setVisibility(View.GONE);
		}
		viewHolder.image.setTag(position);
		viewHolder.imageOverLay.setTag(position);
		viewHolder.image.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (!isSelectable) {
					MyVaultUtils.startVibrate(activity);
					Integer posi = (Integer) v.getTag();
					getCursor().moveToPosition(posi);
					if (viewHolder.imageOverLay.isShown()) {
						if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
							final String imageId = getCursor().getString(
									getCursor().getColumnIndex(Media._ID));
							deleteImage(imageId);
						} else {
							final String imageId = getCursor().getString(
									getCursor().getColumnIndex(Media._ID));
							deleteImage(imageId);
						}
					} else {
						isSelectable = true;
						Image image = new Image();
						if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
							addNormalImage(image);
						} else {
							addHiddenImage(image);
						}
						selectedImages.add(image);
						actionBar.setTitle("(" + selectedImages.size()
								+ ") selected");
						selectedStates.set(position, true);
						viewHolder.imageOverLay.setVisibility(View.VISIBLE);
						activity.makeOptionsBarVisbility(true);
					}
				}
				return false;
			}
		});
		viewHolder.image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSelectable) {
					Integer posi = (Integer) v.getTag();
					getCursor().moveToPosition(posi);
					if (viewHolder.imageOverLay.isShown()) {
					} else {
						Image image = new Image();
						if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
							addNormalImage(image);
						} else {
							addHiddenImage(image);
						}
						selectedImages.add(image);
						actionBar.setTitle("(" + selectedImages.size()
								+ ") selected");
						selectedStates.set(position, true);
						viewHolder.imageOverLay.setVisibility(View.VISIBLE);
					}
				} else {
					final Image image = new Image();
					if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
						addNormalImage(image);
					} else {
						addHiddenImage(image);
					}
					viewHolder.image.startAnimation(animation);
					animation.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							pushImage(image, position);
						}
					});
				}
			}
		});
		viewHolder.imageOverLay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer posi = (Integer) v.getTag();
				getCursor().moveToPosition(posi);
				if (viewHolder.imageOverLay.isShown()) {
					if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
						final String imageId = getCursor().getString(
								getCursor().getColumnIndex(Media._ID));
						deleteImage(imageId);
					} else {
						final String imageId = getCursor().getString(
								getCursor().getColumnIndex(
										ImageDatabase.IMAGE_ID));
						deleteImage(imageId);
					}
					selectedStates.set(position, false);
					viewHolder.imageOverLay.setVisibility(View.GONE);
				}
			}
		});
		return convertView;
	}

	private void pushImage(Image image, int position) {
		Intent intent = new Intent(activity, ImagePagerActivity.class);
		intent.putExtra(ImagePagerActivity.SELECTED_IMAGE, image);
		intent.putExtra(ImagePagerActivity.IMAGES_KEY,
				selectedFolder.getFolderId());
		intent.putExtra(ImagePagerActivity.IMAGE_FOLDER_NAME,
				selectedFolder.getFolderDisplayName());
		intent.putExtra(ImagePagerActivity.IMAGE_FOLDER_TYPE, folderType);
		intent.putExtra(ImagePagerActivity.IMAGE_POSITION_KEY, position);
		activity.startActivity(intent);
	}

	private void addNormalImage(Image image) {
		final String imageId = getCursor().getString(
				getCursor().getColumnIndex(Media._ID));
		image.setImageId(imageId);
		image.setFolderId(selectedFolder.getFolderId());
		image.setFolderName(selectedFolder.getFolderDisplayName());
		image.setImageDisplayName(getCursor().getString(
				getCursor().getColumnIndex(Media.DISPLAY_NAME)));
		image.setDataPath(getCursor().getString(
				getCursor().getColumnIndex(Media.DATA)));
		image.setMimeType(getCursor().getString(
				getCursor().getColumnIndex(Media.MIME_TYPE)));
		long size = getCursor().getLong(getCursor().getColumnIndex(Media.SIZE));
		image.setSize(size);
		long dateModified = getCursor().getLong(
				getCursor().getColumnIndex(Media.DATE_MODIFIED));
		image.setDateModified(dateModified);
		long dateTaken = getCursor().getLong(
				getCursor().getColumnIndex(Media.DATE_TAKEN));
		image.setDateTaken(dateTaken);
		image.setFolderName(selectedFolder.getFolderDisplayName());
		image.setDateAdded(System.currentTimeMillis());
	}

	private void addHiddenImage(Image image) {
		final String imageId = getCursor().getString(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_ID));
		image.setImageId(imageId);
		image.setImageDisplayName(getCursor().getString(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_NAME)));
		image.setDataPath(getCursor().getString(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_PATH)));
		image.setNewPath(getCursor().getString(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_NEW_PATH)));
		image.setMimeType(getCursor().getString(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_MIME_TYPE)));
		long size = getCursor().getLong(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_SIZE));
		image.setSize(size);
		long dateModified = getCursor().getLong(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_DATE_MODIFIED));
		image.setDateModified(dateModified);
		long dateTaken = getCursor().getLong(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_DATE_TAKEN));
		image.setDateTaken(dateTaken);
		image.setDateAdded(getCursor().getLong(
				getCursor().getColumnIndex(ImageDatabase.IMAGE_DATE_TAKEN)));
		image.setFolderName(selectedFolder.getFolderDisplayName());
	}

	public void makeAllUnselected() {
		// isSelectable = false;
		for (int i = 0; i < selectedStates.size(); i++) {
			selectedStates.set(i, false);
		}
		selectedImages.clear();
		actionBar.setTitle(selectedFolder.getFolderDisplayName());
		notifyDataSetChanged();
	}

	public void makeAllSelected() {
		isSelectable = true;
		for (int i = 0; i < selectedStates.size(); i++) {
			selectedStates.set(i, true);
		}
		selectedImages.clear();
		Cursor cursor = getCursor();
		if (cursor.moveToFirst()) {
			do {
				Image image = new Image();
				if (folderType == GalleryViewActivity.NORMAL_GALLERY) {
					final String imageId = cursor.getString(cursor
							.getColumnIndex(Media._ID));
					image.setImageId(imageId);
					image.setImageDisplayName(cursor.getString(cursor
							.getColumnIndex(Media.DISPLAY_NAME)));
					image.setDataPath(cursor.getString(cursor
							.getColumnIndex(Media.DATA)));
					image.setMimeType(cursor.getString(cursor
							.getColumnIndex(Media.MIME_TYPE)));
					long size = cursor.getLong(cursor
							.getColumnIndex(Media.SIZE));
					image.setSize(size);
					long dateModified = cursor.getLong(cursor
							.getColumnIndex(Media.DATE_MODIFIED));
					image.setDateModified(dateModified);
					long dateTaken = cursor.getLong(cursor
							.getColumnIndex(Media.DATE_TAKEN));
					image.setDateTaken(dateTaken);
					image.setFolderName(selectedFolder.getFolderDisplayName());
					image.setFolderId(selectedFolder.getFolderId());
				} else {
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
					image.setFolderName(selectedFolder.getFolderDisplayName());
					image.setFolderId(selectedFolder.getFolderId());
				}
				selectedImages.add(image);
				actionBar.setTitle("(" + selectedImages.size() + ") selected");
			} while (cursor.moveToNext());
		}
		notifyDataSetChanged();
	}

	public int getFirstUnSelectedPosition() {
		for (int i = 0; i < selectedStates.size(); i++) {
			if (!selectedStates.get(i)) {
				return i;
			}
		}
		return -1;
	}

	private void deleteImage(String imageId) {
		if (selectedImages.size() == 0) {
			return;
		}
		int index = -1;
		for (int i = 0; i < selectedImages.size(); i++) {
			if (selectedImages.get(i).getImageId().equals(imageId)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			selectedImages.remove(index);
		}
		actionBar.setTitle("(" + selectedImages.size() + ") selected");
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
	}
}

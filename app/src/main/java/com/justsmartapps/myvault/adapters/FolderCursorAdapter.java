package com.justsmartapps.myvault.adapters;

import imageloadig.ImageLoader;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.database.ImageDatabase;
import com.justsmartapps.myvault.utils.MyVaultUtils.GalleryType;
import com.justsmartapps.myvault.view.FlatTextView;

public class FolderCursorAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private GalleryType normalGallery;
	public static final int NORMAL_GALLERY = 1;
	public static final int HIDDEN_GALLERY = 2;

	public FolderCursorAdapter(Context context, Cursor c,
			GalleryType normalGallery) {
		super(context, c, 0);
		inflater = LayoutInflater.from(context);
		int width = (int) context.getResources().getDimension(
				R.dimen.folder_image_height);
		int height = (int) context.getResources().getDimension(
				R.dimen.folder_image_width);
		imageLoader = new ImageLoader(context, width, height);
		this.normalGallery = normalGallery;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor cursor) {
		FlatTextView folderNameTextView = (FlatTextView) arg0
				.findViewById(R.id.folder_list_name);
		ImageView firstImage = (ImageView) arg0
				.findViewById(R.id.folder_first_image);
		String folderFirstImage = null;
		String folderName = null;
		if (normalGallery == GalleryType.GALLERY_TYPE_HIDDEN) {
			folderName = getCursor()
					.getString(
							getCursor().getColumnIndex(
									ImageDatabase.IMAGE_FOLDER_NAME));
			folderFirstImage = getCursor().getString(
					getCursor().getColumnIndex(ImageDatabase.IMAGE_NEW_PATH));
			imageLoader.DisplayImage(folderFirstImage, null, firstImage, true,
					true, true);
		} else {
			folderName = getCursor().getString(
					getCursor().getColumnIndex(Media.BUCKET_DISPLAY_NAME));
			folderFirstImage = getCursor().getString(
					getCursor().getColumnIndex(Media.DATA));
			imageLoader.DisplayImage(folderFirstImage, null, firstImage, false,
					true, true);
		}
		folderNameTextView.setText(folderName);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return inflater.inflate(R.layout.folder_list_item_layout, arg2, false);
	}
}

package com.justsmartapps.myvault.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.justsmartapps.myvault.core.Image;

public class ImageDatabase extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "image_base.db";
	private static final int DATABASE_VERSION = 1;
	public static final String ID = "_ID";
	public static final String IMAGE_FOLDER_ID = "IMAGE_FOLDER_ID";
	public static final String IMAGE_FOLDER_NAME = "IMAGE_FOLDER_NAME";
	public static final String IMAGE_ID = "IMAGE_ID";
	public static final String IMAGE_NAME = "IMAGE_NAME";
	public static final String IMAGE_NEW_PATH = "IMAGE_NEW_PATH";
	public static final String IMAGE_PATH = "IMAGE_PATH";
	public static final String IMAGE_MIME_TYPE = "IMAGE_MIME_TYPE";
	public static final String IMAGE_SIZE = "IMAGE_SIZE";
	public static final String IMAGE_DATE_TAKEN = "IMAGE_DATE_TAKEN";
	public static final String IMAGE_DATE_MODIFIED = "IMAGE_DATE_MODIFIED";
	public static final String IMAGE_DATE_ADDED = "IMAGE_DATE_ADDED";
	public static final String TABLE_NAME = "HIDDEN_IMAGE_TABLE";
	private SQLiteDatabase database;

	public ImageDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + IMAGE_ID
				+ " LONG, " + IMAGE_FOLDER_ID + " LONG," + IMAGE_NAME
				+ " VARCHAR, " + IMAGE_PATH + " VARCHAR ," + IMAGE_FOLDER_NAME
				+ " VARCHAR," + IMAGE_MIME_TYPE + " VARCHAR," + IMAGE_SIZE
				+ " LONG," + IMAGE_DATE_TAKEN + " LONG," + IMAGE_DATE_MODIFIED
				+ " LONG," + IMAGE_DATE_ADDED + " LONG," + IMAGE_NEW_PATH
				+ " VARCHAR )");
		database = arg0;
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

	public boolean insertImage(Image image) {
		SQLiteDatabase sqliteDatabase = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(IMAGE_ID, image.getImageId());
		contentValues.put(IMAGE_NAME, image.getImageDisplayName());
		contentValues.put(IMAGE_PATH, image.getDataPath());
		contentValues.put(IMAGE_FOLDER_ID, image.getFolderId());
		contentValues.put(IMAGE_FOLDER_NAME, image.getFolderName());
		contentValues.put(IMAGE_NEW_PATH, image.getNewPath());
		contentValues.put(IMAGE_MIME_TYPE, image.getMimeType());
		contentValues.put(IMAGE_SIZE, image.getSize());
		contentValues.put(IMAGE_DATE_TAKEN, image.getDateTaken());
		contentValues.put(IMAGE_DATE_ADDED, image.getDateAdded());
		contentValues.put(IMAGE_DATE_MODIFIED, image.getDateModified());
		long l = sqliteDatabase.insert(TABLE_NAME, null, contentValues);
		sqliteDatabase.close();
		return l != -1L;
	}

	public ArrayList<Image> getAllImagesFromDataBase() {
		SQLiteDatabase database = getWritableDatabase();
		ArrayList<Image> images = new ArrayList<Image>();
		Cursor localCursor = database.query(TABLE_NAME, null, null, null, null,
				null, null);
		if (localCursor != null) {
			if (localCursor.moveToFirst()) {
				do {
					Image localImage = new Image();
					localImage.setImageId(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_ID)));
					localImage.setImageDisplayName(localCursor
							.getString(localCursor.getColumnIndex(IMAGE_NAME)));
					localImage.setDataPath(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_PATH)));
					localImage.setFolderId(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_FOLDER_ID)));
					localImage.setFolderName(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_FOLDER_NAME)));
					localImage.setNewPath(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_NEW_PATH)));
					localImage.setMimeType(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_MIME_TYPE)));
					localImage.setSize(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_SIZE)));
					localImage.setDateTaken(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_TAKEN)));
					localImage.setDateAdded(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_ADDED)));
					localImage.setDateModified(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_MODIFIED)));
					images.add(localImage);
				} while (localCursor.moveToNext());
			}
			localCursor.close();
		}
		database.close();
		return images;
	}

	public void deleteTableData() {
		SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
		localSQLiteDatabase.delete(TABLE_NAME, null, null);
		localSQLiteDatabase.close();
	}

	/**
	 * Get the Count of images from the folder
	 * 
	 * @param folderId
	 * @return
	 */
	public int getTotalCountOfImagesFromFolder(String folderId) {
		SQLiteDatabase database = getWritableDatabase();
		Cursor localCursor = database.query(TABLE_NAME, null, IMAGE_FOLDER_ID
				+ " = ?", new String[] { folderId }, null, null, null);
		int count = localCursor.getCount();
		localCursor.close();
		database.close();
		return count;
	}

	public ArrayList<Image> getAllImagesFromFolder(String folderId, int count) {
		SQLiteDatabase database = getWritableDatabase();
		ArrayList<Image> images = new ArrayList<Image>();
		int imageCount = 0;
		Cursor localCursor = database.query(TABLE_NAME, null, IMAGE_FOLDER_ID
				+ " = ?", new String[] { folderId }, null, null,
				IMAGE_DATE_ADDED + " DESC");
		if (localCursor != null) {
			if (localCursor.moveToFirst()) {
				do {
					if (count != 0 && imageCount >= count) {
						break;
					}
					Image localImage = new Image();
					localImage.setImageId(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_ID)));
					localImage.setImageDisplayName(localCursor
							.getString(localCursor.getColumnIndex(IMAGE_NAME)));
					localImage.setDataPath(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_PATH)));
					localImage.setFolderId(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_FOLDER_ID)));
					localImage.setFolderName(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_FOLDER_NAME)));
					localImage.setNewPath(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_NEW_PATH)));
					localImage.setMimeType(localCursor.getString(localCursor
							.getColumnIndex(IMAGE_MIME_TYPE)));
					localImage.setSize(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_SIZE)));
					localImage.setDateTaken(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_TAKEN)));
					localImage.setDateTaken(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_ADDED)));
					localImage.setDateModified(localCursor.getLong(localCursor
							.getColumnIndex(IMAGE_DATE_MODIFIED)));
					imageCount++;
					images.add(localImage);
				} while (localCursor.moveToNext());
			}
			localCursor.close();
		}
		database.close();
		return images;
	}

	public int deleteImage(String imageId) {
		SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
		int i = localSQLiteDatabase.delete(TABLE_NAME, "IMAGE_ID = ?",
				new String[] { imageId });
		localSQLiteDatabase.close();
		return i;
	}

	public boolean findImage(String absolutePath) {
		Cursor cursor = database.query(TABLE_NAME, null, IMAGE_NEW_PATH
				+ " = ?", new String[] { absolutePath }, null, null, null);
		return cursor.getCount() > 0;
	}
}

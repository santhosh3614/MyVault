package com.justsmartapps.myvault.providers;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.justsmartapps.myvault.database.ImageDatabase;

public class CustomContentProvider extends ContentProvider {
	// database
	private ImageDatabase imagedatabase;
	// private FolderDatabase folderDatabase;
	// used for the UriMacher
	// private static final int FOLDERS = 10;
	// private static final int FOLDER_ID = 20;
	private static final int IMAGES = 30;
	private static final int IMAGE_ID = 40;
	private static final int IMAGE_ID_FOLDER = 50;
	private static final String AUTHORITY = "com.androiddev.myvault.contentprovider";
	private static final String BASE_PATH_IMAGE = "images";
	// private static final String BASE_PATH_FOLDER = "folders";
	// public static final Uri CONTENT_URI_FOLDER = Uri.parse("content://"
	// + AUTHORITY + "/" + BASE_PATH_FOLDER);
	public static final Uri CONTENT_URI_IMAGE = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_IMAGE);
	public static final Uri CONTENT_URI_IMAGES_FOLDER = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_IMAGE + "/FOLDER");
	// public static final String CONTENT_TYPE_FOLDER =
	// ContentResolver.CURSOR_DIR_BASE_TYPE
	// + "/folders";
	// public static final String CONTENT_ITEM_TYPE_FOLDER =
	// ContentResolver.CURSOR_ITEM_BASE_TYPE
	// + "/folder";
	public static final String CONTENT_TYPE_IMAGE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/images";
	public static final String CONTENT_ITEM_TYPE_IMAGE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/image";
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		// sURIMatcher.addURI(AUTHORITY, BASE_PATH_FOLDER, FOLDERS);
		// sURIMatcher.addURI(AUTHORITY, BASE_PATH_FOLDER + "/#", FOLDER_ID);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_IMAGE, IMAGES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_IMAGE + "/#", IMAGE_ID);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_IMAGE + "/FOLDER",
				IMAGE_ID_FOLDER);
	}

	@Override
	public boolean onCreate() {
		imagedatabase = new ImageDatabase(getContext());
		// folderDatabase = new FolderDatabase(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// check if the caller has requested a column which does not exists
		// checkColumns(projection);
		// Set the table
		queryBuilder.setTables(ImageDatabase.TABLE_NAME);
		int uriType = sURIMatcher.match(uri);
		String groupBy = null;
		switch (uriType) {
		case IMAGES:
			break;
		case IMAGE_ID_FOLDER:
			groupBy = ImageDatabase.IMAGE_FOLDER_ID;
			break;
		case IMAGE_ID:
			// adding the ID to the original query
			queryBuilder.appendWhere(ImageDatabase.ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase db = imagedatabase.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, groupBy, null, sortOrder);
		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	public void checkColumns(String[] projection) {
		String[] available = { ImageDatabase.ID, ImageDatabase.IMAGE_FOLDER_ID,
				ImageDatabase.IMAGE_FOLDER_NAME, ImageDatabase.IMAGE_ID,
				ImageDatabase.IMAGE_NAME, ImageDatabase.IMAGE_NEW_PATH,
				ImageDatabase.IMAGE_PATH, ImageDatabase.IMAGE_MIME_TYPE,
				ImageDatabase.IMAGE_SIZE, ImageDatabase.IMAGE_DATE_TAKEN,
				ImageDatabase.IMAGE_DATE_MODIFIED,
				ImageDatabase.IMAGE_DATE_ADDED };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase database = imagedatabase.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case IMAGES:
			id = database.insert(ImageDatabase.TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH_IMAGE + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase database = imagedatabase.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case IMAGES:
			rowsDeleted = database.delete(ImageDatabase.TABLE_NAME, selection,
					selectionArgs);
			break;
		case IMAGE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = database.delete(ImageDatabase.TABLE_NAME,
						ImageDatabase.ID + " = ?", new String[] { id });
			} else {
				rowsDeleted = database.delete(ImageDatabase.TABLE_NAME,
						ImageDatabase.ID + " = " + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase database = imagedatabase.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case IMAGES:
			rowsDeleted = database.update(ImageDatabase.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case IMAGE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = database.update(ImageDatabase.TABLE_NAME, values,
						ImageDatabase.ID + " = ?", new String[] { id });
			} else {
				rowsDeleted = database.update(ImageDatabase.TABLE_NAME, values,
						ImageDatabase.ID + " = " + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
}

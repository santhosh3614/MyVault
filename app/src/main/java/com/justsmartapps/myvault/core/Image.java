package com.justsmartapps.myvault.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
	private String imageId;
	private String imageDisplayName;
	private String folderId;
	private String folderName;
	private String mimeType;
	private String dataPath;
	private String thumbPath;
	private long size;
	
	private long dateModified;
	private long dateTaken;
	private long dateAdded;
	private String newPath;
	private boolean isSelected;
	public static final Creator<Image> CREATOR = new Creator<Image>() {
		@Override
		public Image[] newArray(int size) {
			return new Image[size];
		}

		@Override
		public Image createFromParcel(Parcel source) {
			return new Image(source);
		}
	};

	public Image() {
	}

	public Image(Parcel source) {
		setImageId(source.readString());
		setImageDisplayName(source.readString());
		setFolderId(source.readString());
		setFolderName(source.readString());
		setMimeType(source.readString());
		setDataPath(source.readString());
		setNewPath(source.readString());
		setSelected(source.readByte() != 0);
		setDateAdded(source.readLong());
		setDateTaken(source.readLong());
		setDateModified(source.readLong());
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getImageDisplayName() {
		return imageDisplayName;
	}

	public void setImageDisplayName(String imageDisplayName) {
		this.imageDisplayName = imageDisplayName;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getImageId());
		dest.writeString(getImageDisplayName());
		dest.writeString(getFolderId());
		dest.writeString(getFolderName());
		dest.writeString(getMimeType());
		dest.writeString(getDataPath());
		dest.writeString(getNewPath());
		dest.writeByte((byte) (isSelected() ? 1 : 0));
		dest.writeLong(getDateAdded());
		dest.writeLong(getDateTaken());
		dest.writeLong(getDateModified());
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getDateModified() {
		return dateModified;
	}

	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	public long getDateTaken() {
		return dateTaken;
	}

	public void setDateTaken(long dateTaken) {
		this.dateTaken = dateTaken;
	}

	public long getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(long dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getThumbPath() {
		return thumbPath;
	}

	public void setThumbPath(String thumbPath) {
		this.thumbPath = thumbPath;
	}
}

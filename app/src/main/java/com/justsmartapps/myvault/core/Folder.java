package com.justsmartapps.myvault.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Folder implements Parcelable {
	private int _id;
	private String folderDisplayName;
	private String folderId;
	private String firstImagePath;
	private String secondImagePath;
	private String thirdImagePath;
	private int countOfImages;
	public static final Creator<Folder> CREATOR = new Creator<Folder>() {
		@Override
		public Folder[] newArray(int size) {
			return null;
		}

		@Override
		public Folder createFromParcel(Parcel source) {
			return new Folder(source);
		}
	};

	public Folder() {
	}

	public Folder(Folder copyFolder) {
		set_id(copyFolder.get_id());
		setCountOfImages(copyFolder.getCountOfImages());
		setFirstImagePath(copyFolder.getFirstImagePath());
		setFolderDisplayName(copyFolder.getFolderDisplayName());
		setFolderId(copyFolder.getFolderId());
		setSecondImagePath(copyFolder.getSecondImagePath());
		setThirdImagePath(copyFolder.getThirdImagePath());
	}

	public Folder(Parcel source) {
		set_id(source.readInt());
		setFolderDisplayName(source.readString());
		setFolderId(source.readString());
		setFirstImagePath(source.readString());
		setSecondImagePath(source.readString());
		setThirdImagePath(source.readString());
		setCountOfImages(source.readInt());
	}

	public int describeContents() {
		return 9;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(get_id());
		dest.writeString(getFolderDisplayName());
		dest.writeString(getFolderId());
		dest.writeString(getFirstImagePath());
		dest.writeString(getSecondImagePath());
		dest.writeString(getThirdImagePath());
		dest.writeInt(getCountOfImages());
	}

	public String getFolderDisplayName() {
		return folderDisplayName;
	}

	public void setFolderDisplayName(String folderDisplayName) {
		this.folderDisplayName = folderDisplayName;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getFirstImagePath() {
		return firstImagePath;
	}

	public void setFirstImagePath(String firstImagePath) {
		this.firstImagePath = firstImagePath;
	}

	public String getSecondImagePath() {
		return secondImagePath;
	}

	public void setSecondImagePath(String secondImagePath) {
		this.secondImagePath = secondImagePath;
	}

	public String getThirdImagePath() {
		return thirdImagePath;
	}

	public void setThirdImagePath(String thirdImagePath) {
		this.thirdImagePath = thirdImagePath;
	}

	public int getCountOfImages() {
		return countOfImages;
	}

	public void setCountOfImages(int countOfImages) {
		this.countOfImages = countOfImages;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}
}

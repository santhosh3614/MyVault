package com.justsmartapps.myvault.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Passcode implements Parcelable {
	private String passcode;
	private String securityQuestion;
	private String securityAnswer;

	public static final Creator<Passcode> CREATOR = new Creator<Passcode>() {

		@Override
		public Passcode[] newArray(int size) {
			return new Passcode[size];
		}

		@Override
		public Passcode createFromParcel(Parcel source) {
			return new Passcode(source);
		}
	};

	public Passcode(Parcel source) {
		setPasscode(source.readString());
		setSecurityQuestion(source.readString());
		setSecurityAnswer(source.readString());
	}

	public Passcode() {
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getPasscode());
		dest.writeString(getSecurityQuestion());
		dest.writeString(getSecurityAnswer());

	}

}

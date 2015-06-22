package com.justsmartapps.myvault.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class CustomAlertDialog {
	private Context context;
	private Builder builder;
	private Typeface typeface;

	@SuppressLint("NewApi")
	public CustomAlertDialog(Context context) {
		this.context = context;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			builder = new AlertDialog.Builder(context,
					AlertDialog.THEME_HOLO_LIGHT);
		} else {
			builder = new AlertDialog.Builder(context);
		}
		typeface = Typeface.createFromAsset(context.getAssets(),
				MyVaultUtils.Bariol_Regular_Typeface_String);
	}

	public void dismiss() {
	}

	public void setCancel() {
		builder.setCancelable(false);
	}

	public void setTitle(CharSequence title) {
		builder.setTitle(title);
	}

	public void setMessage(CharSequence message) {
		builder.setMessage(message);
	}

	public void setPositiveButton(CharSequence postiveText,
			OnClickListener clickListener) {
		builder.setPositiveButton(postiveText, clickListener);
	}

	public void setOnCancelListener(OnCancelListener onCancelListener) {
		builder.setOnCancelListener(onCancelListener);
	}

	public void setNegativeButton(CharSequence negativeText,
			OnClickListener clickListener) {
		builder.setNegativeButton(negativeText, clickListener);
	}

	public void setNeutralButton(CharSequence negativeText,
			OnClickListener clickListener) {
		builder.setNeutralButton(negativeText, clickListener);
	}

	public void setAdapter(ListAdapter listAdapter,
			OnClickListener clickListener) {
		builder.setAdapter(listAdapter, clickListener);
	}

	public AlertDialog show() {
		AlertDialog alertDialog = builder.show();
		int titleId = context.getResources().getIdentifier("alertTitle", "id",
				"android");
		int color = context.getResources().getColor(R.color.grass_darker);
		// float dimen = 20;
		TextView titleText = (TextView) alertDialog.findViewById(titleId);
		if (titleText != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				titleText.setTextColor(color);
			}
			// titleText.setTextSize(dimen);
			titleText.setTypeface(typeface);
		}
		int titleDividerId = context.getResources().getIdentifier(
				"titleDivider", "id", "android");
		View titleDividerLine = alertDialog.findViewById(titleDividerId);
		if (titleDividerLine != null) {
			titleDividerLine.setBackgroundColor(color);
		}
		int messageId = context.getResources().getIdentifier("message", "id",
				"android");
		TextView messageText = (TextView) alertDialog.findViewById(messageId);
		if (messageText != null) {
			messageText.setTypeface(typeface);
		}
		Button positive = alertDialog
				.getButton(DialogInterface.BUTTON_POSITIVE);
		if (positive != null) {
			positive.setTypeface(typeface);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				positive.setBackgroundResource(R.drawable.selector_dialog_button);
			}
		}
		Button negativeButton = alertDialog
				.getButton(DialogInterface.BUTTON_NEGATIVE);
		if (negativeButton != null) {
			negativeButton.setTypeface(typeface);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				negativeButton
						.setBackgroundResource(R.drawable.selector_dialog_button);
			}
		}
		Button neutralButton = alertDialog
				.getButton(DialogInterface.BUTTON_NEUTRAL);
		if (neutralButton != null) {
			neutralButton.setTypeface(typeface);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				neutralButton
						.setBackgroundResource(R.drawable.selector_dialog_button);
			}
		}
		return alertDialog;
	}
}

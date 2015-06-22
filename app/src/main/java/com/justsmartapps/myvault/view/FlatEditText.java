package com.justsmartapps.myvault.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

@SuppressLint("NewApi")
public class FlatEditText extends EditText {
	private int style = 2;
	public int cornerRadius = 4;
	public int borderRadius = 2;
	public int completePadding = 15;
	public int[] padding = new int[] { completePadding, completePadding,
			completePadding, completePadding };

	public FlatEditText(Context context) {
		super(context);
		init(context);
	}

	public FlatEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		int[] colorArray = context.getResources().getIntArray(R.array.grass);
		GradientDrawable backgroundDrawable = new GradientDrawable();
		backgroundDrawable.setCornerRadius(cornerRadius);
		if (style == 0) { // flat
			setTextColor(colorArray[3]);
			backgroundDrawable.setColor(colorArray[2]);
			backgroundDrawable.setStroke(0, colorArray[2]);
		} else if (style == 1) { // box
			setTextColor(colorArray[2]);
			backgroundDrawable.setColor(Color.WHITE);
			backgroundDrawable.setStroke(borderRadius, colorArray[2]);
		} else if (style == 2) { // transparent
			backgroundDrawable.setColor(Color.TRANSPARENT);
			backgroundDrawable.setStroke(borderRadius, colorArray[2]);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(backgroundDrawable);
		} else {
			setBackgroundDrawable(backgroundDrawable);
		}
		if (MyVaultUtils.getTextAppreance(context)) {
			setTextAppearance(context, android.R.attr.textAppearanceMedium);
		}
		setPadding(padding[0], padding[1], padding[2], padding[3]);
		setTextColor(colorArray[0]);
		setHintTextColor(colorArray[3]);
		setClickable(true);
		// check for IDE preview render
		if (!this.isInEditMode()) {
			Typeface typeface = Typeface.createFromAsset(context.getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			if (typeface != null)
				setTypeface(typeface);
		}
	}

	public FlatEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
}

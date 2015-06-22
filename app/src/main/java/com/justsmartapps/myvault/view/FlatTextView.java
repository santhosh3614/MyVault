package com.justsmartapps.myvault.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.justsmartapps.myvault.utils.MyVaultUtils;

public class FlatTextView extends TextView {
	public boolean boldText = false;

	public FlatTextView(Context context) {
		super(context);
		init(context);
	}

	public FlatTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FlatTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@Override
	public void setTextColor(int color) {
		super.setTextColor(color);
	}

	private void init(Context context) {
		// setting the text color only if there is no android:textColor
		// attribute used
		// if (textColor == 0) {
		// textColor = Color.BLACK;
		// }
		// setTextColor(textColor);
		// check for IDE preview render
		// setTextColor(textColor);
		if (MyVaultUtils.getTextAppreance(context)) {
			setTextAppearance(context, android.R.attr.textAppearanceMedium);
		}
		if (!this.isInEditMode()) {
			Typeface typeface = Typeface.createFromAsset(context.getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			if (typeface != null)
				if (boldText) {
					setTypeface(typeface, Typeface.BOLD);
				} else {
					setTypeface(typeface);
				}
		}
	}
}

package com.justsmartapps.myvault.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

@SuppressLint("NewApi")
public class FlatButton extends Button {
	private float radius = 4;
	private int bottom = 5;
	private int[] padding = new int[] { 10, 10, 10, 10 };
	public int textColor = Color.WHITE;
	public int textSize = 14;

	public FlatButton(Context context) {
		super(context);
		init(context);
	}

	public FlatButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FlatButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		float[] outerRadius = new float[] { radius, radius, radius, radius,
				radius, radius, radius, radius };
		ShapeDrawable normalFront = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		int[] colorArray = context.getResources().getIntArray(R.array.grass);
		normalFront.getPaint().setColor(colorArray[2]);
		normalFront.setPadding(padding[0], padding[1], padding[2], padding[3]);
		ShapeDrawable normalBack = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		normalBack.getPaint().setColor(colorArray[1]);
		Drawable[] d = { normalBack, normalFront };
		LayerDrawable normal = new LayerDrawable(d);
		normal.setLayerInset(0, 0, 0, 0, 0);
		normal.setLayerInset(1, 0, 0, 0, bottom);
		// creating pressed state drawable
		ShapeDrawable pressedFront = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		pressedFront.getPaint().setColor(colorArray[1]);
		pressedFront.setPadding(padding[0], padding[1], padding[2], padding[3]);
		ShapeDrawable pressedBack = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		pressedBack.getPaint().setColor(colorArray[0]);
		Drawable[] d2 = { pressedBack, pressedFront };
		LayerDrawable pressed = new LayerDrawable(d2);
		pressed.setLayerInset(0, 0, 0, 0, 0);
		pressed.setLayerInset(1, 0, 0, 0, bottom);
		// creating disabled state drawable
		ShapeDrawable disabledFront = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		disabledFront.getPaint().setColor(colorArray[3]);
		disabledFront
				.setPadding(padding[0], padding[1], padding[2], padding[3]);
		ShapeDrawable disabledBack = new ShapeDrawable(new RoundRectShape(
				outerRadius, null, null));
		disabledBack.getPaint().setColor(colorArray[2]);
		Drawable[] d3 = { disabledBack, disabledFront };
		LayerDrawable disabled = new LayerDrawable(d3);
		disabled.setLayerInset(0, 0, 0, 0, 0);
		disabled.setLayerInset(1, 0, 0, 0, bottom);
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_pressed }, pressed);
		states.addState(new int[] { android.R.attr.state_focused }, normal);
		states.addState(new int[] {}, normal);
		states.addState(new int[] { -android.R.attr.state_enabled }, disabled);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(states);
		} else {
			setBackgroundDrawable(states);
		}
		if (MyVaultUtils.getTextAppreance(context)) {
			setTextAppearance(context, android.R.attr.textAppearanceMedium);
		}
		setTextColor(textColor);
		setClickable(true);
		// check for IDE preview render
		if (!this.isInEditMode()) {
			Typeface typeface = Typeface.createFromAsset(context.getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			if (typeface != null)
				setTypeface(typeface);
		}
	}
}

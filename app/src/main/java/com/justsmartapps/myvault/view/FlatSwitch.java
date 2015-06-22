package com.justsmartapps.myvault.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class FlatSwitch extends ToggleButton {
	public float radius = 4;
	public int space = 14;
	public int padding;
	public String offText = "Off";
	public String onText = "On";
	public int textColor = Color.BLACK;

	public FlatSwitch(Context context) {
		super(context);
		init(context);
	}

	public FlatSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FlatSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// creating unchecked-enabled state drawable
		float[] outerRadius = new float[] { radius, radius, radius, radius,
				radius, radius, radius, radius };
		padding = space / 10;
		padding = 6;
		int[] colorArray = context.getResources().getIntArray(R.array.grass);
		ShapeDrawable uncheckedEnabledFrontCore = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		uncheckedEnabledFrontCore.getPaint().setColor(colorArray[3]);
		InsetDrawable uncheckedEnabledFront = new InsetDrawable(
				uncheckedEnabledFrontCore, padding);
		ShapeDrawable uncheckedEnabledBack = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		uncheckedEnabledBack.getPaint().setColor(colorArray[0]);
		uncheckedEnabledBack.setIntrinsicWidth(space / 2 * 5);
		uncheckedEnabledBack.setIntrinsicHeight(space);
		uncheckedEnabledBack.setPadding(0, 0, space / 2 * 5, 0);
		Drawable[] d1 = { uncheckedEnabledBack, uncheckedEnabledFront };
		LayerDrawable uncheckedEnabled = new LayerDrawable(d1);
		// creating checked-enabled state drawable
		ShapeDrawable checkedEnabledFrontCore = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		checkedEnabledFrontCore.getPaint().setColor(colorArray[2]);
		InsetDrawable checkedEnabledFront = new InsetDrawable(
				checkedEnabledFrontCore, padding);
		ShapeDrawable checkedEnabledBack = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		checkedEnabledBack.getPaint().setColor(colorArray[3]);
		checkedEnabledBack.setPadding(space / 2 * 5, 0, 0, 0);
		Drawable[] d2 = { checkedEnabledBack, checkedEnabledFront };
		LayerDrawable checkedEnabled = new LayerDrawable(d2);
		// creating unchecked-disabled state drawable
		ShapeDrawable uncheckedDisabledFrontCore = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		uncheckedDisabledFrontCore.getPaint().setColor(
				Color.parseColor("#d2d2d2"));
		InsetDrawable uncheckedDisabledFront = new InsetDrawable(
				uncheckedDisabledFrontCore, padding);
		ShapeDrawable uncheckedDisabledBack = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		uncheckedDisabledBack.getPaint().setColor(Color.parseColor("#f2f2f2"));
		uncheckedDisabledBack.setPadding(0, 0, space / 2 * 5, 0);
		Drawable[] d3 = { uncheckedDisabledBack, uncheckedDisabledFront };
		LayerDrawable uncheckedDisabled = new LayerDrawable(d3);
		// creating checked-disabled state drawable
		ShapeDrawable checkedDisabledFrontCore = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		checkedDisabledFrontCore.getPaint().setColor(colorArray[3]);
		InsetDrawable checkedDisabledFront = new InsetDrawable(
				checkedDisabledFrontCore, padding);
		ShapeDrawable checkedDisabledBack = new ShapeDrawable(
				new RoundRectShape(outerRadius, null, null));
		checkedDisabledBack.getPaint().setColor(Color.parseColor("#f2f2f2"));
		checkedDisabledBack.setPadding(space / 2 * 5, 0, 0, 0);
		Drawable[] d4 = { checkedDisabledBack, checkedDisabledFront };
		LayerDrawable checkedDisabled = new LayerDrawable(d4);
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { -android.R.attr.state_checked,
				android.R.attr.state_enabled }, new InsetDrawable(
				uncheckedEnabled, padding * 2));
		states.addState(new int[] { android.R.attr.state_checked,
				android.R.attr.state_enabled }, new InsetDrawable(
				checkedEnabled, padding * 2));
		states.addState(new int[] { -android.R.attr.state_checked,
				-android.R.attr.state_enabled }, new InsetDrawable(
				uncheckedDisabled, padding * 2));
		states.addState(new int[] { android.R.attr.state_checked,
				-android.R.attr.state_enabled }, new InsetDrawable(
				checkedDisabled, padding * 2));
		setBackgroundDrawable(states);
		setTextOff(offText);
		setTextOn(onText);
		setTextColor(textColor);
		if (!this.isInEditMode()) {
			Typeface typeface = Typeface.createFromAsset(context.getAssets(),
					MyVaultUtils.Bariol_Regular_Typeface_String);
			if (typeface != null)
				setTypeface(typeface, Typeface.BOLD);
		}
	}
}

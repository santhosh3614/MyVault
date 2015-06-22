package com.justsmartapps.myvault.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class SpinnerAdapter extends ArrayAdapter<String> {
	private Activity context;
	private LayoutInflater inflater;
	private String[] questions;
	private Typeface typeface;

	public SpinnerAdapter(Activity context, String[] questions) {
		super(context, R.layout.security_spinner_layout);
		this.context = context;
		inflater = context.getLayoutInflater();
		this.questions = questions;
		typeface = Typeface.createFromAsset(context.getAssets(),
				MyVaultUtils.Bariol_Regular_Typeface_String);
	}

	@Override
	public int getCount() {
		return questions.length;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.security_spinner_layout,
					parent, false);
		}
		TextView textView = (TextView) convertView
				.findViewById(R.id.security_spinner_text);
		if (typeface != null)
			textView.setTypeface(typeface);
		textView.setPadding(15, 15, 15, 15);
		textView.setTextColor(context.getResources().getColor(
				R.color.grass_darker));
		textView.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.selector_spinner_drop_down));
		textView.setText(questions[position]);
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.security_spinner_layout,
					parent, false);
		}
		TextView textView = (TextView) convertView
				.findViewById(R.id.security_spinner_text);
		if (typeface != null)
			textView.setTypeface(typeface);
		textView.setPadding(10, 10, 10, 10);
		textView.setTextColor(context.getResources().getColor(
				R.color.grass_darker));
		textView.setText(questions[position]);
		return convertView;
	}
}

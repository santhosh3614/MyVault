package com.justsmartapps.myvault.activities;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.core.Image;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class ImageDetailsDialog extends Dialog {
	public ImageDetailsDialog(Context context, Image image) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_details_layout);
		TextView nameValue = (TextView) findViewById(R.id.image_details_name_value);
		TextView pathValue = (TextView) findViewById(R.id.image_details_path_value);
		TextView dateTakenValue = (TextView) findViewById(R.id.image_details_date_taken_value);
		TextView dateModifiedValue = (TextView) findViewById(R.id.image_details_date_modified_value);
		TextView sizeValue = (TextView) findViewById(R.id.image_details_size_value);
		TextView cancel = (TextView) findViewById(R.id.image_details_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		nameValue.setText(image.getImageDisplayName() + "");
		pathValue.setText(image.getDataPath() + "");
		dateTakenValue.setText(MyVaultUtils.convertSecondsToDate(image
				.getDateTaken()));
		dateModifiedValue.setText(MyVaultUtils.convertSecondsToDate(image
				.getDateModified()));
		sizeValue.setText(MyVaultUtils.convertByteSizeToKMG(image.getSize(),
				true));
	}
}

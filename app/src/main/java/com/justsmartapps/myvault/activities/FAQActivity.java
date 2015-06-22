package com.justsmartapps.myvault.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.view.FlatTextView;

public class FAQActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faq_layout);
		TextView faqTitle = (TextView) findViewById(R.id.faq_message_title);
		faqTitle.setText(Html.fromHtml("<b>FAQ</b>"));
		FlatTextView faqMessage = (FlatTextView) findViewById(R.id.faq_message);
		// <b><u><font size='10'>Hidden Camera :</u></b></font><br/> You can
		// take the photos from the Hidden camera which directly moves the photo
		// to the Hidden Gallery.<br/><br/>
		faqMessage
				.setText(Html
						.fromHtml("<b><u><font size='10'>Normal Gallery :</font></u></b><br/>This is your normal gallery which is your replica of device gallery <br/><br/><b><u><font size='10'>Hidden Gallery :</font></u></b><br/>This is your private gallery,you can hide the personal photos in this gallery.<br/><br/><b><u><font size='10' color='red'>Caution:</font></u></b><br/>Your hidden photos are arranged with the existing folder name in the HiddenGallery<br/><br/><b><u><font size='10'>How to hide pictures:</font></u></b><br/>You can directly hide the photos from your device gallery<br/>  1.Open Gallery application<br/>  2.Please select one or more photos/folders<br/>  3.Press Share button<br/>  4.Select MyVault as a Target<br/>  5.Wait for MyVault to perform the operation<br/>  6.Now your photos are in the safe hands<br/><br/>You can hide the photos from the MyVault Application<br/>  1.Select one or more photos<br/>  2.Press Hide to move the photos to the HiddenGallery<br/>  3.Long Press the folder to get the hide option.<br/>  4.Press Hide to move the folder to the HiddenGallery<br/>  5.Now your photos are in the safe hands<br/><br/><b><u><font size='10'>Unhiding your photos:</font></u></b><br/>  1.Go to MyVault -> HiddenGallery<br/>  2.Select the desired photo/folder<br/>  3.Press Unhide<br/>  4.Wait for the MyVault to unhide the photos to original folder<br/><br/><b><u><font size='10'>Anti-Uninstall:</font></u></b><br/>Anti-Uninstall is used to prevent others to un-install the app until unless you un-check from the device administrator<br/><br/><b><u><font size='10'>Unhide all:</font></u></b><br/>You can unhide all the photos to the <b>MyVault_UnHide</b> folder<br/><br/><b><u><font size='10'>Lost Files:</font></u></b><br/>If you think some of your photos has been lost.<br/>1.Go to settings <br/>2.press Lost Files<br/>Now you can find your lost photos in <b>MyVault_Lost_Photos</b> folder<br/><br/><b><u><font size='10'>BackUp</font></u></b><br/>BackUp your vault data to SDCard so that it will be helpfull when you re-install your MyVault again to the device.<br/><br/>"));
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView yourTextView = (TextView) findViewById(titleId);
		if (yourTextView != null) {
			yourTextView.setTypeface(LocalDatabase.getInstance().getTypeface(
					this));
		}
	}

	public void finishScreen(View view) {
		finish();
	}
}

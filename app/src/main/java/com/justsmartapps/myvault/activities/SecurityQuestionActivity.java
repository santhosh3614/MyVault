package com.justsmartapps.myvault.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.adapters.SpinnerAdapter;
import com.justsmartapps.myvault.core.Passcode;
import com.justsmartapps.myvault.database.LocalDatabase;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class SecurityQuestionActivity extends Activity {
	public static final String CHANGE_QUESTION_KEY = "CHANGE_QUESTION_KEY";
	private int REQ_CODE = 2112;
	private EditText answer;
	private Passcode passCode;
	private Spinner questionSpinner;
	private int changeQuestion;
	private String[] questionsArray;
	private Animation animation;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_question_layout);
		questionSpinner = (Spinner) findViewById(R.id.security_questList);
		int[] colorArray = getResources().getIntArray(R.array.grass);
		GradientDrawable backgroundDrawable = new GradientDrawable();
		backgroundDrawable.setCornerRadius(4);
		backgroundDrawable.setColor(Color.TRANSPARENT);
		backgroundDrawable.setStroke(2, colorArray[2]);
		animation = AnimationUtils.loadAnimation(this, R.anim.shake);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			questionSpinner.setBackground(backgroundDrawable);
		} else {
			questionSpinner.setBackgroundDrawable(backgroundDrawable);
		}
		answer = (EditText) findViewById(R.id.security_answer);
		questionsArray = getResources().getStringArray(
				R.array.security_questions);
		SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, getResources()
				.getStringArray(R.array.security_questions));
		questionSpinner.setAdapter(spinnerAdapter);
		TextView forgotQuestion = (TextView) findViewById(R.id.security_forgotOther);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			passCode = extras.getParcelable(LoginActivity.PASSCODE_KEY);
			changeQuestion = extras.getInt(CHANGE_QUESTION_KEY);
		}
		if (changeQuestion == LoginActivity.FORGOT_PIN) {
			questionSpinner.setVisibility(View.GONE);
			forgotQuestion.setVisibility(View.VISIBLE);
			forgotQuestion.setText(passCode.getSecurityQuestion());
			RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) answer
					.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW,
					R.id.security_forgotOther);
			Button forgotAnswer = (Button) findViewById(R.id.security_forgotAnswer);
			forgotAnswer.setVisibility(View.VISIBLE);
		}
		questionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				questionSpinner.setTag(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public void forgotAnswer(View view) {
		Intent intent = new Intent(this, VerificationActivity.class);
		startActivityForResult(intent, REQ_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQ_CODE) {
				sendBroadcast(new Intent(LoginActivity.RECEIVER_ACTION));
				finish();
			}
		} else {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		((GalleryLockApplication) getApplicationContext())
				.setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyVaultUtils.clearCurrentActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyVaultUtils.clearCurrentActivity(this);
	}

	public void securityOk(View view) {
		if (answer.getText().toString().trim().length() > 0) {
			if (passCode != null) {
				if (changeQuestion == LoginActivity.FORGOT_PIN) {
					if (passCode.getSecurityAnswer().equals(
							answer.getText().toString())) {
						sendBroadcast(new Intent(LoginActivity.RECEIVER_ACTION));
						Intent loginIntent = new Intent(this,
								LoginActivity.class);
						loginIntent.putExtra(LoginActivity.LOGIN_TYPE,
								LoginActivity.NEW_LOGIN);
						startActivity(loginIntent);
						finish();
					} else {
						answer.startAnimation(animation);
						MyVaultUtils.displayVisibleToast(this,
								"Security Answer mismatch");
					}
				} else {
					Object tag = questionSpinner.getTag();
					int position = 0;
					if (tag != null) {
						position = (int) tag;
					}
					passCode.setSecurityQuestion(questionsArray[position]);
					passCode.setSecurityAnswer(answer.getText().toString());
					finish();
					LocalDatabase instance = LocalDatabase.getInstance();
					instance.savePasscode(passCode, this);
					if (changeQuestion != LoginActivity.CHANGE_QUESTION) {
						Intent intent = new Intent(this, GalleryActivity.class);
						startActivity(intent);
					} else if (changeQuestion == LoginActivity.CHANGE_QUESTION) {
						MyVaultUtils.displayVisibleToast(this,
								"Security Question Changed successfully");
					}
				}
			}
		} else {
			MyVaultUtils.displayVisibleToast(this, "Answer must not be empty");
		}
	}
}

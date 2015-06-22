package com.justsmartapps.myvault.activities;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;
import com.justsmartapps.myvault.view.FlatButton;

public class VerificationActivity extends Activity implements OnClickListener {
	private static final int REQ_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_account_layout);
		FlatButton continueButton = (FlatButton) findViewById(R.id.verify_layout_continue);
		FlatButton cancelButton = (FlatButton) findViewById(R.id.verify_layout_cancel);
		continueButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CODE) {
			if (resultCode == RESULT_OK && data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					boolean booleanResult = extras
							.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
					MyVaultUtils.displayVisibleToast(this, "" + booleanResult);
					if (booleanResult) {
						setResult(Activity.RESULT_OK);
						Intent loginIntent = new Intent(this,
								LoginActivity.class);
						loginIntent.putExtra(LoginActivity.LOGIN_TYPE,
								LoginActivity.NEW_LOGIN);
						startActivity(loginIntent);
					} else {
						MyVaultUtils.displayVisibleToast(this,
								"Problem while authentication!!");
						setResult(Activity.RESULT_CANCELED);
					}
					finish();
					return;
				}
			}
			setResult(RESULT_CANCELED);
			MyVaultUtils.displayVisibleToast(this,
					"Problem while authentication!!");
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class AMFTask extends AsyncTask<Void, Void, Intent> {
		private AccountManagerFuture<Bundle> future;

		public AMFTask(AccountManagerFuture<Bundle> future) {
			this.future = future;
		}

		@Override
		protected void onPostExecute(Intent result) {
			if (result != null) {
				startActivityForResult(result, REQ_CODE);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}

		@Override
		protected Intent doInBackground(Void... params) {
			try {
				Bundle result = future.getResult();
				Object object = result.get(AccountManager.KEY_INTENT);
				Intent intent = (Intent) object;
				return intent;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.verify_layout_continue) {
			if (!MyVaultUtils.isNetworkAvaliable(this)) {
				MyVaultUtils
						.displayVisibleToast(this,
								"Internet connection is disabled!! So we are not able to process further");
				return;
			}
			AccountManager accountManager = AccountManager.get(this);
			Account[] accounts = accountManager.getAccountsByType("com.google");
			if (accounts.length > 0) {
				AccountManagerCallback<Bundle> accountManagerCallback = new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						new AMFTask(future).execute();
					}
				};
				AccountManager.get(this).confirmCredentials(accounts[0], null,
						null, accountManagerCallback, null);
			} else {
				MyVaultUtils.displayVisibleToast(this,
						"You dont have any synchronized accounts..!!");
			}
		} else if (v.getId() == R.id.verify_layout_cancel) {
			finish();
		}
	}
}

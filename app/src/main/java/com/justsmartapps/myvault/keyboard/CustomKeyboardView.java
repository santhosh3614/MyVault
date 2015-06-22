package com.justsmartapps.myvault.keyboard;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.listeners.IRadioKeyListener;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class CustomKeyboardView implements OnKeyboardActionListener {
	private static final int EMPTY_CODE = 4545;
	private KeyboardView keyboardView;
	private Activity activity;
	private Keyboard keyboard;
	// private EditText editText;
	private StringBuffer password;
	private final int DELETE_CODE = -5;
	private final int DONE_CODE = 55006;
	private final int FORGOT_PIN_CODE = 2112;
	private LinearLayout imagesLayout;
	private int keyCount;
	private IRadioKeyListener iRadioKeyListener;
	private TextView loginErrorText;
	private boolean dontWaitUntilEnter;
	// private Handler mHandler;
	private Animation animation;
	private boolean isKeyEnabled = true;

	public CustomKeyboardView(Activity activity, int keyboardId,
			int loginImagesLinear, TextView loginErrorText) {
		this.activity = activity;
		this.loginErrorText = loginErrorText;
		keyboard = new Keyboard(activity, R.xml.keyboard);
		this.keyboardView = ((KeyboardView) activity.findViewById(keyboardId));
		this.keyboardView.setKeyboard(this.keyboard);
		this.keyboardView.setPreviewEnabled(false);
		password = new StringBuffer();
		// editText = (EditText) activity.findViewById(R.id.login_editLogin);
		imagesLayout = (LinearLayout) activity.findViewById(loginImagesLinear);
		// mHandler = new Handler();
		animation = AnimationUtils.loadAnimation(activity, R.anim.shake);
		this.keyboardView.setOnKeyboardActionListener(this);
		iRadioKeyListener = (IRadioKeyListener) activity;
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	public boolean showKeyboard(View paramView) {
		this.keyboardView.setVisibility(View.VISIBLE);
		this.keyboardView.setEnabled(true);
		if (paramView != null) {
			((InputMethodManager) this.activity
					.getSystemService("input_method")).hideSoftInputFromWindow(
					paramView.getWindowToken(), 0);
		}
		return false;
	}

	public void changeKeyBoard(boolean noForgotPIN) {
		dontWaitUntilEnter = true;
		Keyboard keyboard = keyboardView.getKeyboard();
		List<Key> keys = keyboard.getKeys();
		Iterator<Key> iterator = keys.iterator();
		while (iterator.hasNext()) {
			Keyboard.Key key = (Keyboard.Key) iterator.next();
			// if (key.codes[0] == EMPTY_CODE) {
			// key.codes[0] = DELETE_CODE;
			// key.label = "Delete";
			// }
			if (key.codes[0] == DELETE_CODE) {
				if (noForgotPIN) {
					key.label = "";
					key.codes[0] = EMPTY_CODE;
				} else {
					key.codes[0] = FORGOT_PIN_CODE;
					key.label = "Forgot?";
				}
			}
			if (key.codes[0] == DONE_CODE) {
				key.codes[0] = DELETE_CODE;
				key.label = "Delete";
			}
		}
		keyboardView.refreshDrawableState();
		keyboardView.invalidate();
	}

	public void revertKeyBoard() {
		dontWaitUntilEnter = false;
		Keyboard keyboard = keyboardView.getKeyboard();
		List<Key> keys = keyboard.getKeys();
		Iterator<Key> iterator = keys.iterator();
		while (iterator.hasNext()) {
			Keyboard.Key key = (Keyboard.Key) iterator.next();
			if (key.codes[0] == DELETE_CODE) {
				key.codes[0] = DONE_CODE;
				key.label = "Ok";
			}
			if (key.codes[0] == EMPTY_CODE) {
				key.codes[0] = DELETE_CODE;
				key.label = "Delete";
			}
			if (key.codes[0] == FORGOT_PIN_CODE) {
				key.codes[0] = DELETE_CODE;
				key.label = "Delete";
			}
		}
		keyboardView.refreshDrawableState();
		keyboardView.invalidate();
		keyboardView.invalidateAllKeys();
	}

	public void clearRadios(boolean b, boolean animateRadios) {
		keyCount = 0;
		if (animateRadios) {
			imagesLayout.startAnimation(animation);
			MyVaultUtils.startVibrate(activity);
		} else {
			for (int i = 0; i < imagesLayout.getChildCount(); i++) {
				imagesLayout.getChildAt(i).setSelected(false);
			}
			password.delete(0, password.length());
			loginErrorText.setVisibility(View.INVISIBLE);
		}
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				isKeyEnabled = false;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				isKeyEnabled = true;
				for (int i = 0; i < imagesLayout.getChildCount(); i++) {
					imagesLayout.getChildAt(i).setSelected(false);
				}
				password.delete(0, password.length());
				loginErrorText.setVisibility(View.INVISIBLE);
			}
		});
		// mHandler.postDelayed(new Runnable() {
		// public void run() {
		// imagesLayout.startAnimation(animation);
		// animation.setAnimationListener(new AnimationListener() {
		// @Override
		// public void onAnimationStart(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// for (int i = 0; i < imagesLayout.getChildCount(); i++) {
		// imagesLayout.getChildAt(i).setSelected(false);
		// }
		// password.delete(0, password.length());
		// loginErrorText.setVisibility(View.INVISIBLE);
		// }
		// });
		// }
		// }, 0);
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// View focusCurrent = activity.getWindow().getCurrentFocus();
		// if (focusCurrent == null || focusCurrent.getClass() !=
		// EditText.class)
		// return;
		// EditText edittext = (EditText) focusCurrent;
		// Editable editable = edittext.getText();
		// int start = edittext.getSelectionStart();
		if (!isKeyEnabled) {
			return;
		}
		if (primaryCode == DELETE_CODE) {
			if (keyCount > 0) {
				imagesLayout.getChildAt(password.length() - 1).setSelected(
						false);
				password.deleteCharAt(password.length() - 1);
				keyCount--;
			}
		} else if (primaryCode == DONE_CODE) {
			iRadioKeyListener.onEnterKeyPressed(password.toString());
		} else if (primaryCode == FORGOT_PIN_CODE) {
			iRadioKeyListener.onForgotPIN();
		} else if (primaryCode == EMPTY_CODE) {
		} else {
			if (keyCount < 4) {
				keyCount++;
				loginErrorText.setVisibility(View.INVISIBLE);
				password.append(Character.toString((char) primaryCode));
				View childAt = imagesLayout.getChildAt((password.length() - 1));
				if (childAt != null) {
					childAt.setSelected(true);
				}
				if (dontWaitUntilEnter) {
					if (keyCount == 4) {
						iRadioKeyListener
								.onEnterKeyPressed(password.toString());
					}
				}
			}
		}
	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeUp() {
	}
}

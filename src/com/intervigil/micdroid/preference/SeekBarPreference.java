/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.intervigil.micdroid.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private float mMax = 0.0f;
	private float mMin = 0.0f;
	private float mDefault, mValue = 0.0f;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeFloatValue(androidns, "defaultValue", 0);
		mMin = attrs.getAttributeFloatValue(androidns, "min", 0.0f);
		mMax = attrs.getAttributeFloatValue(androidns, "max", 10.0f);
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null) {
			mSplashText.setText(mDialogMessage);
		}
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist())
			mValue = getPersistedFloat(mDefault);

		mSeekBar.setMax(getProgressBarMax());
		mSeekBar.setProgress(getProgressBarValue());
		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(getProgressBarMax());
		mSeekBar.setProgress(getProgressBarValue());
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedFloat(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		if (shouldPersist())
			persistInt(value);
		callChangeListener(new Integer(value));
	}

	public void onStartTrackingTouch(SeekBar seek) {
		
	}

	public void onStopTrackingTouch(SeekBar seek) {
		
	}
	
	public void setMin(float min) {
		mMin = min;
	}

	public void setMax(float max) {
		mMax = max;
	}
	
	public float getMin() {
		return mMin;
	}

	public float getMax() {
		return mMax;
	}

	public void setValue(float value) {
		mValue = value;
		if (mSeekBar != null) {
			mSeekBar.setProgress(getProgressBarValue());
		}
	}

	public float getValue() {
		return mValue;
	}
	
	private int getProgressBarMax() {
		return (int) (mMax - mMin) * 10;
	}
	
	private int getProgressBarValue() {
		return (int) (mValue - mMin) * 10;
	}
}
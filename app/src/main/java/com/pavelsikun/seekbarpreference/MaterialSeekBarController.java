/* MaterialSeekBarController.java

   Copyright (c) 2015 Pavel Sikun

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to
   deal in the Software without restriction, including without limitation the
   rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
   sell copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
   IN THE SOFTWARE.
 */

package com.pavelsikun.seekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.intervigil.micdroid.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MaterialSeekBarController implements TextWatcher, DiscreteSeekBar.OnProgressChangeListener {

    private final String TAG = getClass().getName();

    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_INTERVAL = 1;
    private static final String DEFAULT_MEASUREMENT_UNIT = "";
    private static final int DEFAULT_TEXT_SIZE = 12;

    private int mMaxValue;
    private int mMinValue;
    private int mInterval;
    private int mCurrentValue;
    private String mMeasurementUnit;

    private DiscreteSeekBar mSeekBar;
    private EditText mSeekBarValue;
    private TextView mMeasurementUnitView;
    private TextView mTitleTxt;
    private TextView mSummaryTxt;

    private int mValueTextSize;

    private String mTitle;
    private String mSummary;

    private Context mContext;

    private Persistable mPersistable;

    public MaterialSeekBarController(Context context, AttributeSet attrs, Persistable persistable) {
        mContext = context;
        mPersistable = persistable;
        init(attrs, null);
    }

    public MaterialSeekBarController(Context context, AttributeSet attrs, View view, Persistable persistable) {
        mContext = context;
        mPersistable = persistable;
        init(attrs, view);
    }

    private void init(AttributeSet attrs, View view) {
        setValuesFromXml(attrs);
        if (view != null) {
            onBindView(view);
        }
    }

    private void setValuesFromXml(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            mCurrentValue = DEFAULT_CURRENT_VALUE;
            mMinValue = DEFAULT_MIN_VALUE;
            mMaxValue = DEFAULT_MAX_VALUE;
            mInterval = DEFAULT_INTERVAL;
            mMeasurementUnit = DEFAULT_MEASUREMENT_UNIT;
            mValueTextSize = DEFAULT_TEXT_SIZE;
        } else {
            TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
            try {
                mMinValue = a.getInt(R.styleable.SeekBarPreference_msbp_minValue, DEFAULT_MIN_VALUE);
                mMaxValue = a.getInt(R.styleable.SeekBarPreference_msbp_maxValue, DEFAULT_MAX_VALUE);
                mInterval = a.getInt(R.styleable.SeekBarPreference_msbp_interval, DEFAULT_INTERVAL);

                mCurrentValue = attrs.getAttributeIntValue(android.R.attr.defaultValue, DEFAULT_CURRENT_VALUE);

                mTitle = a.getString(R.styleable.SeekBarPreference_msbp_title);
                mSummary = a.getString(R.styleable.SeekBarPreference_msbp_summary);

                mValueTextSize = a.getDimensionPixelSize(R.styleable.SeekBarPreference_msbp_valueTextSize, pxFromDp(DEFAULT_TEXT_SIZE, mContext));

                if (mCurrentValue < mMinValue) mCurrentValue = (mMaxValue - mMinValue) / 2;
                mMeasurementUnit = a.getString(R.styleable.SeekBarPreference_msbp_measurementUnit);
                if (mMeasurementUnit == null)
                    mMeasurementUnit = DEFAULT_MEASUREMENT_UNIT;
            } finally {
                a.recycle();
            }
        }
    }

    public void setOnPersistListener(Persistable persistable) {
        mPersistable = persistable;
    }

    public void onBindView(@NonNull View view) {

        mSeekBar = (DiscreteSeekBar) view.findViewById(R.id.seekbar);

        mSeekBar.setMin(mMinValue);
        mSeekBar.setMax(mMaxValue);
        mSeekBar.setOnProgressChangeListener(this);

        mSeekBarValue = (EditText) view.findViewById(R.id.seekbar_value);
        mSeekBarValue.setText(String.valueOf(mCurrentValue));
        mSeekBarValue.addTextChangedListener(this);
        setValueTextSize(mValueTextSize);
        setMaxTextLength();

        mMeasurementUnitView = (TextView) view.findViewById(R.id.measurement_unit);
        mMeasurementUnitView.setText(mMeasurementUnit);

        // Don't move this line
        mSeekBar.setProgress(mCurrentValue);

        if (!view.isEnabled()) {
            mSeekBar.setEnabled(false);
            mSeekBarValue.setEnabled(false);
        }

        mTitleTxt = (TextView) view.findViewById(android.R.id.title);
        mSummaryTxt = (TextView) view.findViewById(android.R.id.summary);

        if (mTitle != null || mSummary != null) {
            if (mTitle != null) mTitleTxt.setText(mTitle);
            if (mSummary != null) mSummaryTxt.setText(mSummary);
        }
    }

    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index) {
        return ta.getInt(index, mCurrentValue);
    }

    protected void onSetInitialValue(boolean restoreValue, @NonNull Object defaultValue) {
        mCurrentValue = (mMaxValue - mMinValue) / 2;
        try {
            mCurrentValue = (Integer) defaultValue;
        } catch (Exception ex) {
            Log.e(TAG, "Invalid default value: " + defaultValue.toString());
        }
    }

    public void setEnabled(boolean enabled) {
        if (mSeekBar != null) mSeekBar.setEnabled(enabled);
        if (mSeekBarValue != null) mSeekBarValue.setEnabled(enabled);
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (mSeekBar != null) mSeekBar.setEnabled(!disableDependent);
        if (mSeekBarValue != null) mSeekBarValue.setEnabled(!disableDependent);
    }

    //SeekBarListener:
    @Override
    public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int progress, boolean fromUser) {
        setError(null);
        mCurrentValue = progress;
        mSeekBarValue.setText(String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
        mSeekBarValue.removeTextChangedListener(this);
    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
        mSeekBarValue.addTextChangedListener(this);
        setCurrentValue(mCurrentValue);
    }

    //TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mSeekBar.setOnProgressChangeListener(null);
    }

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        int value = mMinValue;

        try {
            value = Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        setError(null);
        setCurrentValue(value);

        if (value > mMaxValue)
            showValueTextError();

        mSeekBar.setOnProgressChangeListener(this);
    }

    private void setMaxTextLength() {
        int maxTextLength = String.valueOf(mMaxValue).length();
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxTextLength);
        mSeekBarValue.setFilters(fArray);
    }

    public void showValueTextError() {
        String errorBuilder = mContext.getString(R.string.errors_must_be_between) +
                " " + mMinValue + " " + mMeasurementUnit +
                " " + mContext.getString(R.string.errors_and).toLowerCase() +
                " " + mMaxValue + " " + mMeasurementUnit;
        setError(errorBuilder);
    }

    public void setError(String error) {
        mSeekBarValue.setError(error);

        // android.R.color.holo_red_light: 0x01060016
        int textColor = error == null ? android.R.color.black : 0x01060016;
        mSeekBarValue.setTextColor(ContextCompat.getColor(mContext, textColor));
    }

    //public methods for manipulating this widget from java:
    public void setCurrentValue(int value) {
        mCurrentValue = value;
        if (mSeekBar != null) mSeekBar.setProgress(value);
        if (mPersistable != null) mPersistable.onPersist(value);
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        setMaxTextLength();
        if (mSeekBar != null) mSeekBar.setMax(mMaxValue);
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        if (mSeekBar != null) mSeekBar.setMin(mMinValue);
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }

    public int getInterval() {
        return mInterval;
    }

    public void setMeasurementUnit(String measurementUnit) {
        mMeasurementUnit = measurementUnit;
        if (mMeasurementUnitView != null) mMeasurementUnitView.setText(mMeasurementUnit);
    }

    public String getMeasurementUnit() {
        return mMeasurementUnit;
    }

    public int getValueTextSize() {
        return mValueTextSize;
    }

    public void setValueTextSize(int mTextSize) {
        this.mValueTextSize = mTextSize;
        this.mSeekBarValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
        this.mTitleTxt.setText(title);
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        this.mSummary = summary;
        this.mSummaryTxt.setText(summary);
    }

    static int pxFromDp(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}

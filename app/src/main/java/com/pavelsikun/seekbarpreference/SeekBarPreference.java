/* SeekBarPreference.java

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
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
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

import java.lang.ref.WeakReference;

public class SeekBarPreference extends Preference implements TextWatcher,
        DiscreteSeekBar.OnProgressChangeListener {

    private static final String TAG = "SeekBarPreference";

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
    private int mValueTextSize;

    // Use a weak reference to the ViewHolder so we can manipulate it
    // until the view is garbage collected
    private WeakReference<PreferenceViewHolder> mViewHolder;

    public SeekBarPreference(Context context) {
        super(context);
        init(null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayoutResource(R.layout.seekbar_preference);

        if (attrs == null) {
            mCurrentValue = DEFAULT_CURRENT_VALUE;
            mMinValue = DEFAULT_MIN_VALUE;
            mMaxValue = DEFAULT_MAX_VALUE;
            mInterval = DEFAULT_INTERVAL;
            mMeasurementUnit = DEFAULT_MEASUREMENT_UNIT;
            mValueTextSize = DEFAULT_TEXT_SIZE;
        } else {
            TypedArray ta = getContext().obtainStyledAttributes(attrs,
                    R.styleable.SeekBarPreference);
            try {
                mMinValue = ta.getInt(R.styleable.SeekBarPreference_msbp_minValue,
                        DEFAULT_MIN_VALUE);
                mMaxValue = ta.getInt(R.styleable.SeekBarPreference_msbp_maxValue,
                        DEFAULT_MAX_VALUE);
                mInterval = ta.getInt(R.styleable.SeekBarPreference_msbp_interval,
                        DEFAULT_INTERVAL);

                mCurrentValue = attrs.getAttributeIntValue(android.R.attr.defaultValue,
                        DEFAULT_CURRENT_VALUE);

                mValueTextSize = ta.getDimensionPixelSize(
                        R.styleable.SeekBarPreference_msbp_valueTextSize,
                        (int) (getContext().getResources().getDisplayMetrics().density *
                                DEFAULT_TEXT_SIZE));

                if (mCurrentValue < mMinValue) {
                    mCurrentValue = (mMaxValue - mMinValue) / 2;
                }
                mMeasurementUnit = ta.getString(R.styleable.SeekBarPreference_msbp_measurementUnit);
                if (mMeasurementUnit == null) {
                    mMeasurementUnit = DEFAULT_MEASUREMENT_UNIT;
                }
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        Log.i(TAG, "onBindViewHolder: " + getTitle() + " entry");
        super.onBindViewHolder(holder);

        holder.setIsRecyclable(false);
        mViewHolder = new WeakReference<>(holder);

        DiscreteSeekBar seekBar = (DiscreteSeekBar) holder.findViewById(R.id.seekbar);
        seekBar.setMin(mMinValue);
        seekBar.setMax(mMaxValue);
        seekBar.setOnProgressChangeListener(this);

        EditText seekBarValue = (EditText) holder.findViewById(R.id.seekbar_value);
        seekBarValue.setText(String.valueOf(mCurrentValue));
        seekBarValue.addTextChangedListener(this);
        setValueTextSize(mValueTextSize);
        setMaxTextLength();

        TextView measurementUnitView = (TextView) holder.findViewById(R.id.measurement_unit);
        measurementUnitView.setText(mMeasurementUnit);

        seekBar.setProgress(mCurrentValue);

        seekBar.setEnabled(isEnabled());
        seekBarValue.setEnabled(isEnabled());

        Log.i(TAG, "onBindViewHolder: " + getTitle() + " exit");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        return ta.getInt(index, mCurrentValue);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int average = (mMaxValue - mMinValue) / 2 + mMinValue;
        int value;
        if (restoreValue) {
            value = getPersistedInt(average);
        } else {
            try {
                value = (Integer) defaultValue;
            } catch (Exception ex) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString());
                value = average;
            }
        }
        setCurrentValue(value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewFromRef(mViewHolder, R.id.seekbar);
        if (seekBar != null) {
            seekBar.setEnabled(enabled);
        }
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setEnabled(enabled);
        }
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);

        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewFromRef(mViewHolder, R.id.seekbar);
        if (seekBar != null) {
            seekBar.setEnabled(!disableDependent);
        }
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setEnabled(!disableDependent);
        }
    }

    /* BEGIN SeekBarListener */
    @Override
    public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int progress, boolean fromUser) {
        setError(null);
        mCurrentValue = progress;
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setText(String.valueOf(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.removeTextChangedListener(this);
        }
    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.addTextChangedListener(this);
        }
        setCurrentValue(mCurrentValue);
    }
    /* END SeekBarListener */

    /* BEGIN TextWatcher */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewFromRef(mViewHolder, R.id.seekbar);
        if (seekBar != null) {
            seekBar.setOnProgressChangeListener(null);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Empty method
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

        if (value > mMaxValue) {
            String errorBuilder = getContext().getString(R.string.errors_must_be_between) +
                    " " + mMinValue + " " + mMeasurementUnit +
                    " " + getContext().getString(R.string.errors_and).toLowerCase() +
                    " " + mMaxValue + " " + mMeasurementUnit;
            setError(errorBuilder);
        }

        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewFromRef(mViewHolder, R.id.seekbar);
        if (seekBar != null) {
            seekBar.setOnProgressChangeListener(this);
        }

    }
    /* END TextWatcher */

    private void setMaxTextLength() {
        int maxTextLength = String.valueOf(mMaxValue).length();
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxTextLength);
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setFilters(fArray);
        }
    }

    private void setError(String error) {
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setError(error);
            // android.R.color.holo_red_light: 0x01060016
            int textColor = error == null ? android.R.color.black : 0x01060016;
            seekBarValue.setTextColor(ContextCompat.getColor(getContext(), textColor));
        }
    }

    private static View findViewFromRef(WeakReference<PreferenceViewHolder> reference,
                                        int resId) {
        if (reference == null) {
            return null;
        }
        PreferenceViewHolder viewHolder = reference.get();
        if (viewHolder == null) {
            return null;
        }

        return viewHolder.findViewById(resId);
    }

    /* For interacting with preference */
    public String getMeasurementUnit() {
        return mMeasurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        mMeasurementUnit = measurementUnit;
    }

    public int getInterval() {
        return mInterval;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public void setCurrentValue(int value) {
        mCurrentValue = value;
        persistInt(value);

        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewFromRef(mViewHolder, R.id.seekbar);
        if (seekBar != null) {
            seekBar.setProgress(value);
        }
    }

    public int getValueTextSize() {
        return mValueTextSize;
    }

    public void setValueTextSize(int mTextSize) {
        mValueTextSize = mTextSize;
        EditText seekBarValue = (EditText) findViewFromRef(mViewHolder, R.id.seekbar_value);
        if (seekBarValue != null) {
            seekBarValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
    }
}

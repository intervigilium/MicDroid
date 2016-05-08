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
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TextView;

import com.intervigil.micdroid.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class SeekBarPreference extends Preference {

    private static final String TAG = "SeekBarPreference";

    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_INTERVAL = 1;
    private static final String DEFAULT_MEASUREMENT_UNIT = "";
    private static final int DEFAULT_TEXT_SIZE = 12;

    private int mDefaultValue;
    private int mMaxValue;
    private int mMinValue;
    private int mInterval;
    private String mMeasurementUnit;
    private int mValueTextSize;

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

                mDefaultValue = attrs.getAttributeIntValue(android.R.attr.defaultValue,
                        DEFAULT_CURRENT_VALUE);

                mValueTextSize = ta.getDimensionPixelSize(
                        R.styleable.SeekBarPreference_msbp_valueTextSize,
                        (int) (getContext().getResources().getDisplayMetrics().density *
                                DEFAULT_TEXT_SIZE));

                if (mDefaultValue < mMinValue) {
                    mDefaultValue = (mMaxValue - mMinValue) / 2;
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
        super.onBindViewHolder(holder);

        /* HACK: v7 Preference seems to keep references to the recycled view's object somehow */
        holder.setIsRecyclable(false);

        final int currentValue = getPersistedInt(mDefaultValue);

        final DiscreteSeekBar seekBar = (DiscreteSeekBar) holder.findViewById(R.id.seekbar);
        final EditText seekBarValue = (EditText) holder.findViewById(R.id.seekbar_value);
        final TextView measurementUnitView = (TextView) holder.findViewById(R.id.measurement_unit);

        seekBar.setMin(mMinValue);
        seekBar.setMax(mMaxValue);
        seekBar.setProgress(currentValue);
        seekBar.setEnabled(isEnabled());
        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setError(getContext(), seekBarValue, null);
                persistInt(value);
                seekBarValue.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                // Do nothing
            }
        });

        seekBarValue.setText(String.valueOf(currentValue));
        seekBarValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, mValueTextSize);
        seekBarValue.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(String.valueOf(mMaxValue).length()),
        });
        seekBarValue.setEnabled(false);

        measurementUnitView.setText(mMeasurementUnit);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        return ta.getInt(index, mDefaultValue);
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
        mDefaultValue = value;
        persistInt(value);
    }

    private static void setError(Context context, EditText textView, String error) {
        textView.setError(error);
        // android.R.color.holo_red_light: 0x01060016
        int textColor = error == null ? android.R.color.black : 0x01060016;
        textView.setTextColor(ContextCompat.getColor(context, textColor));
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
        return getPersistedInt(mDefaultValue);
    }
}

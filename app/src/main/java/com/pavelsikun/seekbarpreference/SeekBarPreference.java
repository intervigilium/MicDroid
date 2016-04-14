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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.intervigil.micdroid.R;

public class SeekBarPreference extends Preference implements Persistable {

    private MaterialSeekBarController mController;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayoutResource(R.layout.seekbar_preference);
        mController = new MaterialSeekBarController(getContext(), attrs, this);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mController.onBindView(holder.findViewById(R.id.seekbar_layout));
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index) {
        return ta.getInt(index, 50);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, @NonNull Object defaultValue) {
        int average = (mController.getMaxValue() - mController.getMinValue()) / 2;
        if (restoreValue) {
            mController.setCurrentValue(getPersistedInt(average));
        } else {
            mController.onSetInitialValue(false, defaultValue);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mController.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        mController.onDependencyChanged(dependency, disableDependent);
    }

    @Override
    public void onPersist(int value) {
        persistInt(value);
    }

    public String getMeasurementUnit() {
        return mController.getMeasurementUnit();
    }

    public void setMeasurementUnit(String measurementUnit) {
        mController.setMeasurementUnit(measurementUnit);
    }

    public int getInterval() {
        return mController.getInterval();
    }

    public void setInterval(int interval) {
        mController.setInterval(interval);
    }

    public int getMinValue() {
        return mController.getMinValue();
    }

    public void setMinValue(int minValue) {
        mController.setMinValue(minValue);
    }

    public int getMaxValue() {
        return mController.getMaxValue();
    }

    public void setMaxValue(int maxValue) {
        mController.setMaxValue(maxValue);
    }

    public int getCurrentValue() {
        return mController.getCurrentValue();
    }

    public void setCurrentValue(int value) {
        mController.setCurrentValue(value);
    }
}

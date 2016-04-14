/* MaterialSeekBarView.java

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
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.intervigil.micdroid.R;

public class MaterialSeekBarView extends FrameLayout {

    private MaterialSeekBarController mController;
    private Persistable mPersistable;

    public MaterialSeekBarView(Context context) {
        super(context);
        init(null);
    }

    public MaterialSeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSeekBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public MaterialSeekBarView(Context context, Persistable persistable) {
        super(context);
        mPersistable = persistable;
        init(null);
    }

    public MaterialSeekBarView(Context context, AttributeSet attrs, Persistable persistable) {
        super(context, attrs);
        mPersistable = persistable;
        init(attrs);
    }

    public MaterialSeekBarView(Context context, AttributeSet attrs, int defStyleAttr, Persistable persistable) {
        super(context, attrs, defStyleAttr);
        mPersistable = persistable;
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSeekBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Persistable persistable) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPersistable = persistable;
        init(attrs);
    }

    void init(AttributeSet attrs) {
        View view = inflate(getContext(), R.layout.seekbar_preference, this);
        mController = new MaterialSeekBarController(getContext(), attrs, view, mPersistable);
    }

    public void setOnPersistListener(Persistable persistable) {
        mPersistable = persistable;
        mController.setOnPersistListener(mPersistable);
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

    public int getValueTextSize() {
        return mController.getCurrentValue();
    }

    public void setValueTextSize(int textSize) {
        mController.setValueTextSize(textSize);
    }

    public String getTitle() {
        return mController.getTitle();
    }

    public void setTitle(String title) {
        mController.setTitle(title);
    }

    public String getSummary() {
        return mController.getSummary();
    }

    public void setSummary(String summary) {
        mController.setSummary(summary);
    }

    public void showValueTxtError() {
        mController.showValueTextError();
    }

    public void hideValueTxtError() {
        mController.setError(null);
    }
}
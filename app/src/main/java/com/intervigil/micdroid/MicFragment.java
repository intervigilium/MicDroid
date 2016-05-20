package com.intervigil.micdroid;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MicFragment extends Fragment {

    private static final String TAG = "Mic";

    public interface MicListener {
        boolean onMicStart();

        void onMicStop();
    }

    private MicListener mListener;
    private TimerDisplay mTimerDisplay;

    public MicFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MicListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MicListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        ToggleButton mRecordButton = (ToggleButton) getView().findViewById(R.id.recording_button);
        mRecordButton.setChecked(false);
        mRecordButton.setOnCheckedChangeListener(recordBtnListener);

        TextView timerText = (TextView) getView().findViewById(R.id.recording_timer);
        Typeface timerFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Clockopia.ttf");
        timerText.setTypeface(timerFont);

        mTimerDisplay = new TimerDisplay(timerText);
    }

    private CompoundButton.OnCheckedChangeListener recordBtnListener =
            new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                    if (btn.isChecked()) {
                        if (mListener.onMicStart()) {
                            mTimerDisplay.reset();
                            mTimerDisplay.start();
                        } else {
                            // Failed to start correctly, reset fragment state
                            btn.setChecked(false);
                        }
                    } else {
                        mListener.onMicStop();
                        mTimerDisplay.stop();
                    }
                }
            };
}

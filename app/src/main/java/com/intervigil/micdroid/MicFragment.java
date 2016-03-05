package com.intervigil.micdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.HeadsetHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.UpdateHelper;
import com.intervigil.micdroid.interfaces.DependentTask;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.micdroid.recorder.SipdroidRecorder;

import net.sourceforge.autotalent.Autotalent;

public class MicFragment extends Fragment {
    private static final float CONCERT_A = 440.0f;

    private static final int DEFAULT_SCALE_ROTATE = 0;
    private static final float DEFAULT_FIXED_PITCH = 0.0f;
    private static final float DEFAULT_LFO_DEPTH = 0.0f;
    private static final float DEFAULT_LFO_RATE = 5.0f;
    private static final float DEFAULT_LFO_SHAPE = 0.0f;
    private static final float DEFAULT_LFO_SYM = 0.0f;
    private static final int DEFAULT_LFO_QUANT = 0;

    private TimerDisplay mTimerDisplay;
    private ToggleButton mRecordButton;

    private Recorder mRecorder;
    private AutotalentTask mAutotalentTask;
    private AudioController mAudioControl;

    public MicFragment() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAutotalentTask = new AutotalentTask(getActivity(), postAutotalentTask);

        mAudioControl = new AudioController(getActivity());

        if (UpdateHelper.isAppUpdated(getActivity())) {
            UpdateHelper.onAppUpdate(getActivity());
        } else {
            mAudioControl.configureRecorder();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPrefs.registerOnSharedPreferenceChangeListener(mScreenLockListener);

        mRecordButton = ((ToggleButton) getView().findViewById(R.id.recording_button));
        mRecordButton.setChecked(false);
        mRecordButton.setOnCheckedChangeListener(recordBtnListener);

        Typeface timerFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Clockopia.ttf");
        TextView timerDisplay = (TextView) getView().findViewById(R.id.recording_timer);
        timerDisplay.setTypeface(timerFont);

        mTimerDisplay = new TimerDisplay();
        mTimerDisplay.registerDisplay(timerDisplay);

        if (PreferenceHelper.getScreenLock(getActivity())) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroy() {
        Autotalent.destroyAutotalent();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.INTENT_FILENAME_ENTRY:
                if (resultCode == Activity.RESULT_OK) {
                    String fileName = data.getStringExtra(
                            Constants.INTENT_EXTRA_FILE_NAME).trim()
                            + ".wav";
                    updateAutoTalentPreferences();
                    mAutotalentTask.runAutotalentTask(fileName);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getActivity(), R.string.recording_save_canceled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.INTENT_PREFERENCES:
                if (mRecorder != null && !mRecorder.isRunning()) {
                    mRecorder.cleanup();
                    mRecorder = null;
                }
                break;
            default:
                break;
        }
    }

    private void updateAutoTalentPreferences() {
        char key = PreferenceHelper.getKey(getActivity());
        float fixedPull = PreferenceHelper.getPullToFixedPitch(getActivity());
        float pitchShift = PreferenceHelper.getPitchShift(getActivity());
        float strength = PreferenceHelper.getCorrectionStrength(getActivity());
        float smooth = PreferenceHelper.getCorrectionSmoothness(getActivity());
        int formantCorrection = PreferenceHelper.getFormantCorrection(getActivity()) ? 1 : 0;
        float formantWarp = PreferenceHelper.getFormantWarp(getActivity());
        float mix = PreferenceHelper.getMix(getActivity());

        Autotalent.instantiateAutotalent(PreferenceHelper.getSampleRate(getActivity()));
        Autotalent.setKey(key);
        Autotalent.setConcertA(CONCERT_A);
        Autotalent.setFixedPitch(DEFAULT_FIXED_PITCH);
        Autotalent.setFixedPull(fixedPull);
        Autotalent.setCorrectionStrength(strength);
        Autotalent.setCorrectionSmoothness(smooth);
        Autotalent.setPitchShift(pitchShift);
        Autotalent.setScaleRotate(DEFAULT_SCALE_ROTATE);
        Autotalent.setLfoDepth(DEFAULT_LFO_DEPTH);
        Autotalent.setLfoRate(DEFAULT_LFO_RATE);
        Autotalent.setLfoShape(DEFAULT_LFO_SHAPE);
        Autotalent.setLfoSymmetric(DEFAULT_LFO_SYM);
        Autotalent.setLfoQuantization(DEFAULT_LFO_QUANT);
        Autotalent.setFormantCorrection(formantCorrection);
        Autotalent.setFormantWarp(formantWarp);
        Autotalent.setMix(mix);
    }

    private DependentTask postAutotalentTask = new DependentTask() {
        @Override
        public void handleError() {
            Autotalent.destroyAutotalent();
        }

        @Override
        public void doTask() {
            Autotalent.destroyAutotalent();
            Toast.makeText(getActivity(), R.string.recording_save_success,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private DependentTask postRecordTask = new DependentTask() {
        @Override
        public void doTask() {
            if (PreferenceHelper.getLiveMode(getActivity())) {
                Autotalent.destroyAutotalent();
            }
            Toast.makeText(getActivity(),
                    R.string.recording_finished_toast,
                    Toast.LENGTH_SHORT).show();
            Intent saveFileIntent = new Intent(getActivity(),
                    FileNameEntry.class);
            startActivityForResult(saveFileIntent,
                    Constants.INTENT_FILENAME_ENTRY);
        }

        @Override
        public void handleError() {
            if (PreferenceHelper.getLiveMode(getActivity())) {
                Autotalent.destroyAutotalent();
            }
            mRecordButton.setOnCheckedChangeListener(null);
            mRecordButton.setChecked(false);
            mRecordButton.setOnCheckedChangeListener(recordBtnListener);
        }
    };

    private CompoundButton.OnCheckedChangeListener recordBtnListener =
            new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                    if (!canWriteToSdCard()) {
                        btn.setChecked(false);
                        DialogHelper.showWarning(getActivity(),
                                R.string.no_external_storage_title,
                                R.string.no_external_storage_warning);
                    } else if (!mAudioControl.isValidRecorder()) {
                        btn.setChecked(false);
                        DialogHelper.showWarning(getActivity(),
                                R.string.unconfigured_audio_title,
                                R.string.unconfigured_audio_warning);
                    } else {
                        if (btn.isChecked()) {
                            boolean isLiveMode = PreferenceHelper.getLiveMode(getActivity());
                            if (isLiveMode
                                    && !HeadsetHelper.isHeadsetPluggedIn(getActivity())) {
                                btn.setChecked(false);
                                DialogHelper.showWarning(getActivity(),
                                        R.string.no_headset_plugged_in_title,
                                        R.string.no_headset_plugged_in_warning);
                            } else {
                                mTimerDisplay.reset();
                                if (isLiveMode) {
                                    updateAutoTalentPreferences();
                                }
                                if (mRecorder == null) {
                                    mRecorder = new SipdroidRecorder(getActivity(), postRecordTask,
                                            mAudioControl);
                                }
                                mRecorder.start();
                                mTimerDisplay.start();
                            }
                        } else {
                            if (mRecorder != null && mRecorder.isRunning()) {
                                // only do this if it was running, otherwise an error
                                // message triggered the check state change
                                mRecorder.stop();
                                mTimerDisplay.stop();
                            }
                        }
                    }
                }
            };

    private static boolean canWriteToSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mScreenLockListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (getString(R.string.prefs_prevent_screen_lock_key).equals(key)) {
                        if (PreferenceHelper.getScreenLock(getActivity())) {
                            getActivity().getWindow().addFlags(
                                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else {
                            getActivity().getWindow().clearFlags(
                                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                }
            };
}

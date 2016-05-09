package com.intervigil.micdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.intervigil.micdroid.model.Recording;

public class RecordingOptionsDialogFragment extends DialogFragment {

    private static final String TAG = "RecordingOptionsDialog";

    public static final String RECORDING_OPTIONS_DIALOG_ARG_RECORDING = "recording";

    public interface RecordingOptionsDialogListener {
        void onPlay(Recording r);

        void onDelete(Recording r);

        void onRename(Recording r);

        void onExport(Recording r);

        void onSetRingtone(Recording r);

        void onSetNotification(Recording r);

        void onShare(Recording r);
    }

    // Must match ordering of R.array.recording_options_array
    private static final int[] mRecordingOptions = {
            R.string.recording_options_play,
            R.string.recording_options_delete,
            R.string.recording_options_rename,
            R.string.recording_options_export,
            R.string.recording_options_set_ringtone,
            R.string.recording_options_set_notification,
            R.string.recording_options_share,
    };

    private RecordingOptionsDialogListener mListener;
    private Recording mRecording;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RecordingOptionsDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RecordingOptionsDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle icicle) {
        mRecording = getArguments().getParcelable(RECORDING_OPTIONS_DIALOG_ARG_RECORDING);
        if (mRecording == null) {
            Log.w(TAG, "Dialog requires Recording argument!");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.recording_options_title)
                .setItems(R.array.recording_options_array, mOnClickListener);
        return builder.create();
    }

    private DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (mRecordingOptions[which]) {
                case R.string.recording_options_play:
                    mListener.onPlay(mRecording);
                    break;
                case R.string.recording_options_delete:
                    mListener.onDelete(mRecording);
                    break;
                case R.string.recording_options_rename:
                    mListener.onRename(mRecording);
                    break;
                case R.string.recording_options_export:
                    mListener.onExport(mRecording);
                    break;
                case R.string.recording_options_set_ringtone:
                    mListener.onSetRingtone(mRecording);
                    break;
                case R.string.recording_options_set_notification:
                    mListener.onSetNotification(mRecording);
                    break;
                case R.string.recording_options_share:
                    mListener.onShare(mRecording);
                    break;
            }
        }
    };
}

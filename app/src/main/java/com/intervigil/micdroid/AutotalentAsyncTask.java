package com.intervigil.micdroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AutotalentAsyncTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "AutotalentAsyncTask";

    private static final int AUTOTALENT_CHUNK_SIZE = 8192;

    private final Context mContext;
    private ProgressDialog mBusySpinner;
    private AutotalentController mAutotalentControl;
    private int mSampleRate;

    public AutotalentAsyncTask(Context context, int sampleRate) {
        mContext = context;
        mSampleRate = sampleRate;
        mAutotalentControl = new AutotalentController(mContext);
        mBusySpinner = new ProgressDialog(mContext);
        mBusySpinner.setCancelable(false);
    }

    @Override
    protected void onCancelled(Void result) {
        DialogHelper.showWarning(mContext,
                R.string.recording_io_error_title,
                R.string.recording_io_error_warning);
        mAutotalentControl.closeAutotalent();
    }

    @Override
    protected void onPreExecute() {
        mBusySpinner.setMessage(mContext.getString(R.string.autotalent_progress_msg));
        mAutotalentControl.initializeAutotalent(mSampleRate);
        mBusySpinner.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        // maybe ugly but we only pass one string in anyway
        String fileName = params[0];

        try {
            processPitchCorrection(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            cancel(true);
        }

        return null;
    }

    private void processPitchCorrection(String file) throws IOException {
        WaveReader reader = null;
        WaveWriter writer = null;
        short[] buf = new short[AUTOTALENT_CHUNK_SIZE];
        try {
            FileInputStream in = mContext.openFileInput(
                    mContext.getString(R.string.default_recording_name));
            FileOutputStream out = mContext.openFileOutput(file, Context.MODE_WORLD_READABLE);
            reader = new WaveReader(in);
            reader.openWave();
            writer = new WaveWriter(out, reader.getSampleRate(), reader.getChannels(),
                    reader.getPcmFormat());
            writer.createWaveFile();
            while (true) {
                int samplesRead = reader.read(buf, AUTOTALENT_CHUNK_SIZE);
                if (samplesRead > 0) {
                    mAutotalentControl.process(buf, samplesRead);
                    writer.write(buf, 0, samplesRead);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.closeWaveFile();
                }
                if (writer != null) {
                    writer.closeWaveFile();
                    mContext.deleteFile(mContext.getString(R.string.default_recording_name));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        mAutotalentControl.closeAutotalent();
        mBusySpinner.dismiss();
        Toast.makeText(mContext, R.string.recording_save_success,
                Toast.LENGTH_SHORT).show();
    }
}

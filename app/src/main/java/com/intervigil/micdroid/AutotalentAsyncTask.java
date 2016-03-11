package com.intervigil.micdroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

import net.sourceforge.autotalent.Autotalent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class AutotalentAsyncTask extends AsyncTask<String, Void, Void> {

    private static final int AUTOTALENT_CHUNK_SIZE = 8192;

    private final Context mContext;
    private ProgressDialog mBusySpinner;
    private boolean mIsLive;

    public AutotalentAsyncTask(Context context, boolean isLive) {
        mContext = context;
        mBusySpinner = new ProgressDialog(mContext);
        mBusySpinner.setCancelable(false);
        mIsLive = isLive;
    }

    @Override
    protected void onCancelled(Void result) {
        DialogHelper.showWarning(mContext,
                R.string.recording_io_error_title,
                R.string.recording_io_error_warning);
    }

    @Override
    protected void onPreExecute() {
        if (mIsLive) {
            mBusySpinner.setMessage(mContext.getString(R.string.saving_recording_progress_msg));
        } else {
            mBusySpinner.setMessage(mContext.getString(R.string.autotalent_progress_msg));
        }
        mBusySpinner.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        // maybe ugly but we only pass one string in anyway
        String fileName = params[0];

        if (mIsLive) {
            try {
                // do a file copy since renameTo doesn't work
                moveFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
            }
        } else {
            try {
                processPitchCorrection(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
            }
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
                    Autotalent.processSamples(buf, samplesRead);
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

    private void moveFile(String file) throws IOException {
        int len;
        InputStream in = null;
        OutputStream out = null;
        byte[] buf = new byte[1024];
        // TODO: Refactor this to use app local storage
        File src = new File(
                mContext.getCacheDir().getAbsolutePath()
                        + File.separator
                        + mContext.getString(R.string.default_recording_name));
        File dst = new File(
                ApplicationHelper.getLibraryDirectory()
                        + File.separator
                        + file);
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        mBusySpinner.dismiss();
        Toast.makeText(mContext, R.string.recording_save_success,
                Toast.LENGTH_SHORT).show();
    }
}

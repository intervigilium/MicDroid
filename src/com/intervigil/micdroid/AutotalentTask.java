package com.intervigil.micdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.autotalent.Autotalent;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.interfaces.DependentTask;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;


public class AutotalentTask {

    private static final int AUTOTALENT_CHUNK_SIZE = 8192;
    
    public static final int AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR = 48105;

    private final Context context;
    private final DependentTask dependentTask;

    public AutotalentTask(Context context, DependentTask task) {
        this.context = context;
        this.dependentTask = task;
    }

    public void runAutotalentTask(String file) {
        new ProcessAutotalentTask().execute(file);
    }

    private Handler autotalentTaskHandler = new Handler() {
        // use the handler to receive error messages from the recorder object
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR:
                    // received error that the writer couldn't create the recording
                    DialogHelper.showWarning(context,
                            R.string.recording_io_error_title,
                            R.string.recording_io_error_warning);
                    dependentTask.handleError();
                    break;
            }
        }
    };

    private class ProcessAutotalentTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog spinner;
        private boolean isLiveMode;

        public ProcessAutotalentTask() {
            spinner = new ProgressDialog(context);
            spinner.setCancelable(false);
            isLiveMode = PreferenceHelper.getLiveMode(context);
        }

        @Override
        protected void onPreExecute() {
            if (isLiveMode) {
                spinner.setMessage(context.getString(R.string.saving_recording_progress_msg));
            } else {
                spinner.setMessage(context.getString(R.string.autotalent_progress_msg));
            }
            spinner.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            // maybe ugly but we only pass one string in anyway
            String fileName = params[0];
            Message msg = null;

            if (isLiveMode) {
                try {
                    // do a file copy since renameTo doesn't work
                    moveFile(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = autotalentTaskHandler.obtainMessage(AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR);
                }
            } else {
                try {
                    processPitchCorrection(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = autotalentTaskHandler.obtainMessage(AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR);
                }
            }
            if (msg != null) {
                autotalentTaskHandler.sendMessage(msg);
            }
            return null;
        }
        
        private void processPitchCorrection(String file) throws IOException {
            WaveReader reader = null;
            WaveWriter writer = null;
            short[] buf = new short[AUTOTALENT_CHUNK_SIZE];
            try {
                reader = new WaveReader(
                        context.getCacheDir().getAbsolutePath(),
                        context.getString(R.string.default_recording_name));
                reader.openWave();
                writer = new WaveWriter(
                        ApplicationHelper.getLibraryDirectory(),
                        file,
                        reader.getSampleRate(),
                        reader.getChannels(),
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
                    }
                } catch (IOException e) {
                    // I hate you sometimes java
                    e.printStackTrace();
                }
            }
        }

        private void moveFile(String file) throws IOException {
            int len;
            InputStream in = null;
            OutputStream out = null;
            byte[] buf = new byte[1024];
            File src = new File(
                    context.getCacheDir().getAbsolutePath()
                    + File.separator
                    + context.getString(R.string.default_recording_name));
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
                    // I hate you sometimes java
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            spinner.dismiss();
            dependentTask.doTask();
        }
    }

}

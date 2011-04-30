package com.intervigil.micdroiddonate.recorder;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.autotalent.Autotalent;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.intervigil.micdroiddonate.Constants;
import com.intervigil.micdroiddonate.R;
import com.intervigil.micdroiddonate.helper.AudioHelper;
import com.intervigil.micdroiddonate.helper.DialogHelper;
import com.intervigil.micdroiddonate.helper.PreferenceHelper;
import com.intervigil.micdroiddonate.interfaces.DependentTask;
import com.intervigil.micdroiddonate.interfaces.Recorder;
import com.intervigil.wave.WaveWriter;
import com.intervigil.wave.exception.InvalidWaveException;

public class SimpleRecorder implements Recorder {

    private static final int SIMPLE_RECORDER_BUFFER_SIZE = 8192;

    private static final int RECORDER_MESSAGE_IO_ERROR = 8675308;
    private static final int RECORDER_MESSAGE_RECORD_ERROR = 8675310;
    private static final int RECORDER_MESSAGE_FINISHED = 8675307;

    private final Context context;
    private MicWriter writerThread;
    private final boolean isLiveMode;
    private final int sampleRate;
    private final DependentTask postRecordTask;

    public SimpleRecorder(Context context, DependentTask postRecordTask, boolean isLiveMode) {
        this.context = context;
        this.sampleRate = PreferenceHelper.getSampleRate(context);
        this.postRecordTask = postRecordTask;
        this.isLiveMode = isLiveMode;
    }

    @Override
    public void start() {
        try {
            writerThread = new MicWriter();
            writerThread.start();
            Toast.makeText(context,
                    R.string.recording_started_toast,
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            DialogHelper.showWarning(context,
                    R.string.audio_record_exception_title,
                    R.string.audio_record_exception_warning);
            postRecordTask.handleError();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            writerThread.close();
            try {
                writerThread.join();
            } catch (InterruptedException e) {}
            writerThread = null;
        }
    }

    @Override
    public void cleanup() {
        stop();
    }

    @Override
    public boolean isRunning() {
        return (writerThread != null
                && writerThread.getState() != Thread.State.NEW && writerThread
                .getState() != Thread.State.TERMINATED);
    }

    private Handler recorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORDER_MESSAGE_RECORD_ERROR:
                    DialogHelper.showWarning(context,
                            R.string.audio_record_exception_title,
                            R.string.audio_record_exception_warning);
                    postRecordTask.handleError();
                    break;
                case RECORDER_MESSAGE_IO_ERROR:
                    DialogHelper.showWarning(context,
                            R.string.recording_io_error_title,
                            R.string.recording_io_error_warning);
                    postRecordTask.handleError();
                    break;
                case RECORDER_MESSAGE_FINISHED:
                    postRecordTask.doTask();
                    break;
            }
        }
    };

    private class MicWriter extends Thread {
        private final AudioRecord audioRecord;
        private AudioTrack audioTrack;
        private final WaveWriter writer;
        private boolean running;

        public MicWriter() throws IllegalArgumentException {
            this.running = false;
            this.audioRecord = AudioHelper.getRecorder(context);
            this.writer = new WaveWriter(context.getCacheDir().getAbsolutePath(),
                    context.getString(R.string.default_recording_name), sampleRate,
                    AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG),
                    AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));

            if (isLiveMode) {
                this.audioTrack = AudioHelper.getPlayer(context);
            }
        }

        public synchronized void close() {
            running = false;
        }

        public void initialize() throws FileNotFoundException, InvalidWaveException, IOException {
            writer.createWaveFile();
            if (isLiveMode) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setMode(AudioManager.MODE_NORMAL);
            }
        }

        public void cleanup() {
            // stop things
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            if (isLiveMode) {
                audioTrack.stop();
                audioTrack.release();
            }
            // close file
            try {
                writer.closeWaveFile();
            } catch (IOException e) {
                // no recovery possible here
                e.printStackTrace();
            }
        }

        public void run() {
            Message msg;
            int numSamples;
            short[] buf = new short[SIMPLE_RECORDER_BUFFER_SIZE];

            try {
                initialize();
                running = true;
                audioRecord.startRecording();
                if (isLiveMode) {
                    audioTrack.play();
                }
                while (running) {
                    numSamples = audioRecord.read(buf, 0, SIMPLE_RECORDER_BUFFER_SIZE);
                    if (isLiveMode) {
                        processLiveAudio(buf, numSamples);
                        audioTrack.write(buf, 0, numSamples);
                    }
                    writer.write(buf, 0, numSamples);
                }
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_FINISHED);
            } catch (IllegalStateException e) {
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_RECORD_ERROR);
            } catch (IOException e) {
                // file IO error, no recovery possible?
                e.printStackTrace();
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_IO_ERROR);
            }
            cleanup();
            recorderHandler.sendMessage(msg);
        }

        private void processLiveAudio(short[] samples, int numSamples) throws IOException {
            Autotalent.processSamples(samples, numSamples);
        }
    }
}

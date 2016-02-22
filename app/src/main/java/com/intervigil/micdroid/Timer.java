package com.intervigil.micdroid;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class Timer {
    private static final int DEFAULT_TIMER_COUNT = 1000;
    private static final int TIMER_START = 8;
    private static final int TIMER_STOP = 9;
    private static final int TIMER_RESET = 10;
    private static final int TIMER_COUNT_UP = 11;

    private TimerHandler mHandler;
    private TextView mDisplay;
    private int mTimeCounter;

    public Timer(TextView view) {
        mDisplay = view;
        mHandler = new TimerHandler();
        mTimeCounter = 0;
    }

    public void registerDisplay(TextView view) {
        mDisplay = view;
    }

    public void start() {
        Message startMsg = mHandler.obtainMessage(TIMER_START);
        mHandler.sendMessage(startMsg);
    }

    public void stop() {
        Message stopMsg = mHandler.obtainMessage(TIMER_STOP);
        mHandler.sendMessage(stopMsg);
    }

    public void reset() {
        Message resetMsg = mHandler.obtainMessage(TIMER_RESET);
        mHandler.sendMessage(resetMsg);
    }

    private String getTime() {
        int minutes = mTimeCounter / 60;
        int seconds = mTimeCounter % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private class TimerHandler extends Handler {
        // handler processes updates to the mTimeCounter
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_START:
                    mDisplay.setText(getTime());
                    this.sleep(DEFAULT_TIMER_COUNT);
                    break;
                case TIMER_STOP:
                    this.removeMessages(TIMER_COUNT_UP);
                    this.removeMessages(TIMER_START);
                    this.removeMessages(TIMER_STOP);
                    this.removeMessages(TIMER_RESET);
                    break;
                case TIMER_RESET:
                    mTimeCounter = 0;
                    mDisplay.setText(getTime());
                    break;
                case TIMER_COUNT_UP:
                    mDisplay.setText(getTime());
                    mTimeCounter++;
                    sleep(DEFAULT_TIMER_COUNT);
                    break;
            }
        }

        public void sleep(long delayMillis) {
            this.removeMessages(TIMER_COUNT_UP);
            sendMessageDelayed(obtainMessage(TIMER_COUNT_UP), delayMillis);
        }
    }
}
